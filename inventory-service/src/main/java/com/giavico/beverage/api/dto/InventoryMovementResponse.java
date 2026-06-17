package com.giavico.beverage.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventoryMovementResponse(
        UUID uuid,
        UUID inventoryItemId,
        String rawMaterialKey,
        String movementType,
        BigDecimal quantityDelta,
        BigDecimal resultingQuantity,
        String referenceType,
        String referenceId,
        String reason,
        String performedBy,
        Instant timestamp
) {
}
