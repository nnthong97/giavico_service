package com.giavico.beverage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giavico.beverage.api.dto.FormulaGenerationRequest;
import com.giavico.beverage.api.dto.FormulaGenerationResponse;
import com.giavico.beverage.config.OllamaProperties;
import com.giavico.beverage.domain.FormulationValidator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OllamaServiceIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void formulaCompleteAndStreamUseTheOllamaContract() {
        FormulaPersistenceService persistence = mock(FormulaPersistenceService.class);
        when(persistence.persist(any(), any())).thenAnswer(invocation -> invocation.getArgument(1));

        AtomicInteger calls = new AtomicInteger();
        WebClient webClient = stubClient(calls, """
                {"response":"{\\"ingredients\\":[{\\"rawMaterialKey\\":\\"WATER\\",\\"massPercentage\\":100.0,\\"costProjection\\":0.1}],\\"varianceAnalysis\\":\\"stub\\",\\"stabilityAlerts\\":[]}","done":true}
                """, """
                {"response":"{\\"ingredients\\":[]}","done":false}
                {"response":"","done":true}
                """);

        OllamaFormulationService service = new OllamaFormulationService(
                webClient,
                properties(),
                objectMapper,
                new FormulationValidator(),
                persistence
        );
        FormulaGenerationRequest request = new FormulaGenerationRequest(
                "Mango",
                "Vietnam",
                12.0,
                true,
                List.of(),
                "Pilot",
                "Stable",
                null
        );

        FormulaGenerationResponse complete = service.generateComplete(request);
        assertThat(complete.ingredients()).hasSize(1);
        assertThat(complete.ingredients().get(0).rawMaterialKey()).isEqualTo("WATER");

        List<String> events = service.generateStream(request)
                .map(event -> event.event() + ":" + event.data())
                .collectList()
                .block(Duration.ofSeconds(5));
        assertThat(events).contains("fragment:{\"ingredients\":[]}", "complete:[DONE]");
    }

    @Test
    void chatCompleteAndStreamUseTheOllamaContract() {
        ChatPersistenceService persistence = mock(ChatPersistenceService.class);
        AtomicInteger calls = new AtomicInteger();
        WebClient webClient = stubClient(calls,
                "{\"response\":\"stub reply\",\"done\":true}",
                """
                        {"response":"stub ","done":false}
                        {"response":"reply","done":true}
                        """);
        OllamaChatService service = new OllamaChatService(webClient, properties(), objectMapper, persistence);

        assertThat(service.chat("hello").message()).isEqualTo("stub reply");

        List<String> events = service.chatStream("hello")
                .map(event -> event.event() + ":" + event.data())
                .collectList()
                .block(Duration.ofSeconds(5));
        assertThat(events).contains("fragment:stub ", "done:reply", "complete:[DONE]");
    }

    private WebClient stubClient(AtomicInteger calls, String completeBody, String streamBody) {
        return WebClient.builder()
                .exchangeFunction(request -> {
                    boolean stream = calls.getAndIncrement() > 0;
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, stream
                                    ? MediaType.APPLICATION_NDJSON_VALUE
                                    : MediaType.APPLICATION_JSON_VALUE)
                            .body(stream ? streamBody : completeBody)
                            .build());
                })
                .build();
    }

    private OllamaProperties properties() {
        return new OllamaProperties(
                "http://stub.invalid",
                "/api/generate",
                "stub-model",
                Duration.ofSeconds(5),
                Duration.ofSeconds(1)
        );
    }
}
