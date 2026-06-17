package com.giavico.beverage.ollama;

public record OllamaGenerateChunk(
        String model,
        String created_at,
        String response,
        Boolean done
) {
}
