package com.giavico.beverage.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FormulaGenerationResponse(
        @Valid @NotNull List<IngredientComponent> ingredients,
        String varianceAnalysis,
        List<String> stabilityAlerts,
        SavedFormulaMetadata savedFormula
) {
}
