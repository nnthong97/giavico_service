package com.giavico.beverage.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InventoryItemResponse(
        UUID uuid,
        String rawMaterialKey,
        String materialName,
        String category,
        String supplierName,
        String lotNumber,
        String warehouseLocation,
        String unitOfMeasure,
        BigDecimal quantityOnHand,
        BigDecimal reorderPoint,
        BigDecimal unitCost,
        BigDecimal inventoryValue,
        LocalDate expirationDate,
        String status,
        boolean lowStock,
        Instant createdAt,
        Instant updatedAt
) {
}
