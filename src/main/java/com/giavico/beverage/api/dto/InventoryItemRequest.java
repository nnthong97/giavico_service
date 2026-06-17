package com.giavico.beverage.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryItemRequest(
        @NotBlank @Size(max = 160) String rawMaterialKey,
        @NotBlank @Size(max = 240) String materialName,
        @NotBlank @Size(max = 120) String category,
        @Size(max = 180) String supplierName,
        @Size(max = 120) String lotNumber,
        @NotBlank @Size(max = 160) String warehouseLocation,
        @NotBlank @Size(max = 32) String unitOfMeasure,
        @NotNull @DecimalMin("0.0") BigDecimal quantityOnHand,
        @NotNull @DecimalMin("0.0") BigDecimal reorderPoint,
        @NotNull @DecimalMin("0.0") BigDecimal unitCost,
        LocalDate expirationDate,
        @NotBlank @Pattern(regexp = "ACTIVE|HOLD|EXPIRED|DEPLETED", message = "must be ACTIVE, HOLD, EXPIRED, or DEPLETED") String status
) {
}
