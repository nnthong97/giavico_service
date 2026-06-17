package com.giavico.gateway.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "giavico.routes")
public record GatewayRouteProperties(
        @NotBlank String formulaServiceUrl,
        @NotBlank String inventoryServiceUrl,
        @NotBlank String chatAiServiceUrl
) {
}
