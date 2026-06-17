package com.giavico.beverage.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record IngredientComponent(
        @NotBlank String rawMaterialKey,
        @NotNull @DecimalMin("0.0") Double massPercentage,
        @NotNull @DecimalMin("0.0") BigDecimal costProjection
) {
}
