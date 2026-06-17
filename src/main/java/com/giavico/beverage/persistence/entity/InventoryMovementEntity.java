package com.giavico.beverage.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItemEntity inventoryItem;

    @Column(nullable = false, length = 32)
    private String movementType;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantityDelta;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal resultingQuantity;

    @Column(length = 80)
    private String referenceType;

    @Column(length = 160)
    private String referenceId;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(length = 120)
    private String performedBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected InventoryMovementEntity() {
    }

    public InventoryMovementEntity(
            InventoryItemEntity inventoryItem,
            String movementType,
            BigDecimal quantityDelta,
            BigDecimal resultingQuantity,
            String referenceType,
            String referenceId,
            String reason,
            String performedBy
    ) {
        this.inventoryItem = inventoryItem;
        this.movementType = movementType;
        this.quantityDelta = quantityDelta;
        this.resultingQuantity = resultingQuantity;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.reason = reason;
        this.performedBy = performedBy;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public InventoryItemEntity getInventoryItem() {
        return inventoryItem;
    }

    public String getMovementType() {
        return movementType;
    }

    public BigDecimal getQuantityDelta() {
        return quantityDelta;
    }

    public BigDecimal getResultingQuantity() {
        return resultingQuantity;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getReason() {
        return reason;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
