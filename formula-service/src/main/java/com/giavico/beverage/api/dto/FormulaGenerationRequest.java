package com.giavico.beverage.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FormulaGenerationRequest(
        @NotBlank @Size(max = 160) String drinkName,
        @NotBlank @Size(max = 160) String marketDestination,
        @NotNull @DecimalMin("0.0") @DecimalMax("90.0") Double targetBrix,
        @NotNull Boolean isAcidified,
        @Size(max = 50) List<@NotBlank @Size(max = 240) String> regionalRestrictions,
        @NotBlank @Size(max = 240) String productionArea,
        @NotBlank @Size(max = 4000) String customerSpecification,
        @Size(max = 160) String baselineBOM
) {
}
