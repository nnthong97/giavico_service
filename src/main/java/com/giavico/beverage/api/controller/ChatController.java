package com.giavico.beverage.api.controller;

import com.giavico.beverage.api.dto.ChatRequest;
import com.giavico.beverage.api.dto.ChatMessageResponse;
import com.giavico.beverage.api.dto.ChatMessageStoreRequest;
import com.giavico.beverage.api.dto.ChatResponse;
import com.giavico.beverage.config.OllamaProperties;
import com.giavico.beverage.service.ChatPersistenceService;
import com.giavico.beverage.service.OllamaChatService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final OllamaChatService chatService;
    private final ChatPersistenceService chatPersistenceService;
    private final OllamaProperties ollamaProperties;

    public ChatController(
            OllamaChatService chatService,
            ChatPersistenceService chatPersistenceService,
            OllamaProperties ollamaProperties
    ) {
        this.chatService = chatService;
        this.chatPersistenceService = chatPersistenceService;
        this.ollamaProperties = ollamaProperties;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request.message());
    }

    @PostMapping(value = "/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@Valid @RequestBody ChatRequest request) {
        return chatService.chatStream(request.message());
    }

    @GetMapping("/messages")
    public Page<ChatMessageResponse> listMessages(@PageableDefault(size = 100) Pageable pageable) {
        return chatPersistenceService.list(pageable);
    }

    @PostMapping("/messages")
    public ChatMessageResponse storeMessage(@Valid @RequestBody ChatMessageStoreRequest request) {
        return chatPersistenceService.store(request);
    }

    @DeleteMapping("/messages")
    public void clearMessages() {
        chatPersistenceService.clear();
    }

    @GetMapping("/account/openai-key/status")
    public Map<String, Object> aiProviderStatus() {
        return Map.of(
                "configured", true,
                "provider", "ollama",
                "model", ollamaProperties.model()
        );
    }
}
