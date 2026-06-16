package com.giavico.beverage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giavico.beverage.api.dto.ChatMessageStoreRequest;
import com.giavico.beverage.api.dto.ChatResponse;
import com.giavico.beverage.api.dto.FormulaGenerationRequest;
import com.giavico.beverage.api.dto.FormulaGenerationResponse;
import com.giavico.beverage.config.OllamaProperties;
import com.giavico.beverage.domain.FormulationValidator;
import com.giavico.beverage.exception.CompleteResponseParsingException;
import com.giavico.beverage.exception.OllamaServiceException;
import com.giavico.beverage.exception.StreamChunkParsingException;
import com.giavico.beverage.ollama.OllamaGenerateChunk;
import com.giavico.beverage.ollama.OllamaGenerateRequest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OllamaFormulationService {

    private final WebClient ollamaWebClient;
    private final OllamaProperties properties;
    private final ObjectMapper objectMapper;
    private final FormulationValidator validator;
    private final FormulaPersistenceService persistenceService;
    private final ChatPersistenceService chatPersistenceService;

    public OllamaFormulationService(
            WebClient ollamaWebClient,
            OllamaProperties properties,
            ObjectMapper objectMapper,
            FormulationValidator validator,
            FormulaPersistenceService persistenceService,
            ChatPersistenceService chatPersistenceService
    ) {
        this.ollamaWebClient = ollamaWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.persistenceService = persistenceService;
        this.chatPersistenceService = chatPersistenceService;
    }

    public FormulaGenerationResponse generateComplete(FormulaGenerationRequest request) {
        OllamaGenerateRequest ollamaRequest = buildOllamaRequest(request, false);
        OllamaGenerateChunk generated = ollamaWebClient.post()
                .uri(properties.generatePath())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(ollamaRequest))
                .retrieve()
                .bodyToMono(OllamaGenerateChunk.class)
                .timeout(properties.timeout())
                .onErrorMap(error -> !(error instanceof WebClientResponseException),
                        error -> new OllamaServiceException("Complete formulation generation failed.", error))
                .block(properties.timeout().plus(Duration.ofSeconds(5)));

        if (generated == null || generated.response() == null) {
            throw new CompleteResponseParsingException("The complete Ollama response did not include a response field.", null);
        }

        FormulaGenerationResponse parsed = parseCompleteResponse(generated.response());
        FormulaGenerationResponse validated = validator.sanitizeAndValidate(parsed);
        return persistenceService.persist(request, validated);
    }

    public Flux<ServerSentEvent<String>> generateStream(FormulaGenerationRequest request) {
        OllamaGenerateRequest ollamaRequest = buildOllamaRequest(request, true);
        AtomicReference<String> carry = new AtomicReference<>("");

        return ollamaWebClient.post()
                .uri(properties.generatePath())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_NDJSON, MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(ollamaRequest))
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .timeout(properties.timeout())
                .concatMap(dataBuffer -> {
                    String chunk = dataBuffer.toString(StandardCharsets.UTF_8);
                    DataBufferUtils.release(dataBuffer);
                    return Flux.fromIterable(extractCompletedLines(carry, chunk));
                })
                .map(this::parseStreamLine)
                .filter(chunk -> chunk.response() != null && !chunk.response().isBlank())
                .map(chunk -> ServerSentEvent.<String>builder()
                        .event(Boolean.TRUE.equals(chunk.done()) ? "done" : "fragment")
                        .data(chunk.response())
                        .build())
                .concatWith(Mono.fromSupplier(() -> ServerSentEvent.<String>builder()
                        .event("complete")
                        .data("[DONE]")
                        .build()))
                .doOnCancel(() -> carry.set(""))
                .onErrorResume(error -> Flux.just(ServerSentEvent.<String>builder()
                        .event(error instanceof StreamChunkParsingException ? "stream-parse-error" : "ollama-error")
                .data(error.getMessage())
                .build()));
    }

    public ChatResponse chat(String message) {
        chatPersistenceService.store(new ChatMessageStoreRequest("user", message));

        OllamaGenerateRequest ollamaRequest = new OllamaGenerateRequest(
                properties.model(),
                buildChatPrompt(message),
                false,
                null,
                Map.of(
                        "temperature", 0.35,
                        "top_p", 0.9,
                        "num_ctx", 4096
                )
        );
        OllamaGenerateChunk generated = ollamaWebClient.post()
                .uri(properties.generatePath())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(ollamaRequest))
                .retrieve()
                .bodyToMono(OllamaGenerateChunk.class)
                .timeout(properties.timeout())
                .onErrorMap(error -> !(error instanceof WebClientResponseException),
                        error -> new OllamaServiceException("Chat request failed.", error))
                .block(properties.timeout().plus(Duration.ofSeconds(5)));

        String response = generated == null || generated.response() == null
                ? "No response returned from Ollama."
                : generated.response().trim();

        chatPersistenceService.store(new ChatMessageStoreRequest("assistant", response));

        return new ChatResponse(response);
    }

    public Flux<ServerSentEvent<String>> chatStream(String message) {
        chatPersistenceService.store(new ChatMessageStoreRequest("user", message));

        OllamaGenerateRequest ollamaRequest = new OllamaGenerateRequest(
                properties.model(),
                buildChatPrompt(message),
                true,
                null,
                Map.of(
                        "temperature", 0.35,
                        "top_p", 0.9,
                        "num_ctx", 4096
                )
        );
        AtomicReference<String> carry = new AtomicReference<>("");
        AtomicReference<String> assistantResponse = new AtomicReference<>("");

        return ollamaWebClient.post()
                .uri(properties.generatePath())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_NDJSON, MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(ollamaRequest))
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .timeout(properties.timeout())
                .concatMap(dataBuffer -> {
                    String chunk = dataBuffer.toString(StandardCharsets.UTF_8);
                    DataBufferUtils.release(dataBuffer);
                    return Flux.fromIterable(extractCompletedLines(carry, chunk));
                })
                .map(this::parseStreamLine)
                .filter(chunk -> chunk.response() != null && !chunk.response().isBlank())
                .doOnNext(chunk -> assistantResponse.updateAndGet(current -> current + chunk.response()))
                .map(chunk -> ServerSentEvent.<String>builder()
                        .event(Boolean.TRUE.equals(chunk.done()) ? "done" : "fragment")
                        .data(chunk.response())
                        .build())
                .concatWith(Mono.fromSupplier(() -> {
                    String response = assistantResponse.get().trim();
                    if (!response.isBlank()) {
                        chatPersistenceService.store(new ChatMessageStoreRequest("assistant", response));
                    }

                    return ServerSentEvent.<String>builder()
                            .event("complete")
                            .data("[DONE]")
                            .build();
                }))
                .doOnCancel(() -> {
                    carry.set("");
                    assistantResponse.set("");
                })
                .onErrorResume(error -> Flux.just(ServerSentEvent.<String>builder()
                        .event(error instanceof StreamChunkParsingException ? "stream-parse-error" : "ollama-error")
                        .data(error.getMessage())
                        .build()));
    }

    private OllamaGenerateRequest buildOllamaRequest(FormulaGenerationRequest request, boolean stream) {
        return new OllamaGenerateRequest(
                properties.model(),
                buildPrompt(request),
                stream,
                "json",
                Map.of(
                        "temperature", 0.25,
                        "top_p", 0.9,
                        "num_ctx", 8192
                )
        );
    }

    private String buildPrompt(FormulaGenerationRequest request) {
        return """
                You are Giavico's Beverage Formulation & R&D Engine. Generate one JSON object only.
                Use food science judgment for acidified beverages, Brix balance, ingredient compatibility,
                regulatory assumptions, thermal process stability, high-shear processing, and cost projection.

                Required JSON contract:
                {
                  "ingredients": [
                    {"rawMaterialKey": "string", "massPercentage": 0.0, "costProjection": 0.0}
                  ],
                  "varianceAnalysis": "markdown string",
                  "stabilityAlerts": ["string"],
                  "savedFormula": null
                }

                Constraints:
                - Ingredient mass percentages should sum to 100.0.
                - If acidified is true, include pH/acidification processing warnings when relevant.
                - Respect market destination, regional restrictions, production area, and customer specification.
                - Do not include text outside JSON.

                Input:
                drinkName: %s
                marketDestination: %s
                targetBrix: %.2f
                isAcidified: %s
                regionalRestrictions: %s
                productionArea: %s
                customerSpecification: %s
                baselineBOM: %s
                """.formatted(
                request.drinkName(),
                request.marketDestination(),
                request.targetBrix(),
                request.isAcidified(),
                request.regionalRestrictions(),
                request.productionArea(),
                request.customerSpecification(),
                request.baselineBOM()
        );
    }

    private String buildChatPrompt(String message) {
        return """
                You are Giavico's Beverage R&D assistant. Help with beverage formulation, Brix,
                acidification, stability, cost projection, ingredient compatibility, regulatory
                assumptions, process constraints, and saved formula review.

                Answer clearly and practically. If the user asks for a formula, explain the
                reasoning and suggest next steps, but do not claim that you saved data unless a
                tool or API explicitly saved it.

                User message:
                %s
                """.formatted(message);
    }

    private FormulaGenerationResponse parseCompleteResponse(String responseJson) {
        try {
            return objectMapper.readValue(responseJson, FormulaGenerationResponse.class);
        } catch (JsonProcessingException firstFailure) {
            String extracted = extractJsonObject(responseJson);
            try {
                return objectMapper.readValue(extracted, FormulaGenerationResponse.class);
            } catch (JsonProcessingException secondFailure) {
                throw new CompleteResponseParsingException("Unable to parse the LLM JSON object.", secondFailure);
            }
        }
    }

    private OllamaGenerateChunk parseStreamLine(String line) {
        try {
            return objectMapper.readValue(line, OllamaGenerateChunk.class);
        } catch (JsonProcessingException exception) {
            throw new StreamChunkParsingException("Malformed NDJSON line from Ollama stream.", exception);
        }
    }

    private List<String> extractCompletedLines(AtomicReference<String> carry, String chunk) {
        String merged = carry.get() + chunk;
        String[] pieces = merged.split("\\R", -1);
        List<String> completed = new ArrayList<>();
        for (int i = 0; i < pieces.length - 1; i++) {
            if (!pieces[i].isBlank()) {
                completed.add(pieces[i]);
            }
        }
        carry.set(pieces[pieces.length - 1]);
        return completed;
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
