package com.giavico.beverage.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record SavedFormulaMetadata(
        @NotNull UUID uuid,
        @NotBlank String name,
        @NotBlank String summary,
        @NotNull Instant timestamp
) {
}
