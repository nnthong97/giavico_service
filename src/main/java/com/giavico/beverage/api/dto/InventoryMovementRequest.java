package com.giavico.beverage.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InventoryMovementRequest(
        @NotBlank @Pattern(regexp = "RECEIPT|ISSUE|ADJUSTMENT", message = "must be RECEIPT, ISSUE, or ADJUSTMENT") String movementType,
        @NotNull BigDecimal quantity,
        Boolean allowNegative,
        @Size(max = 80) String referenceType,
        @Size(max = 160) String referenceId,
        @NotBlank @Size(max = 1000) String reason,
        @Size(max = 120) String performedBy
) {
}
