package com.giavico.beverage.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "ollama")
public record OllamaProperties(
        @NotBlank String baseUrl,
        @NotBlank String generatePath,
        @NotBlank String model,
        @NotNull Duration timeout,
        @NotNull Duration connectTimeout
) {
}
