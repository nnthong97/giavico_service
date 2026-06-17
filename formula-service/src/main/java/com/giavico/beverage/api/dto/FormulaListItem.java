package com.giavico.beverage.api.dto;

import java.time.Instant;
import java.util.UUID;

public record FormulaListItem(
        UUID uuid,
        String name,
        String marketDestination,
        Double targetBrix,
        Boolean isAcidified,
        String productionArea,
        String baselineBOM,
        String summary,
        Instant timestamp,
        int ingredientCount,
        int alertCount
) {
}
