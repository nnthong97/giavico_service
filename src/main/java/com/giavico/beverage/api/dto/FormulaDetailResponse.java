package com.giavico.beverage.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FormulaDetailResponse(
        UUID uuid,
        String name,
        String marketDestination,
        Double targetBrix,
        Boolean isAcidified,
        List<String> regionalRestrictions,
        String productionArea,
        String customerSpecification,
        String baselineBOM,
        List<IngredientComponent> ingredients,
        String varianceAnalysis,
        List<String> stabilityAlerts,
        String summary,
        Instant timestamp
) {
}
