package com.giavico.beverage.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "inventory_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventory_raw_material_key", columnNames = "raw_material_key")
)
public class InventoryItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "raw_material_key", nullable = false, length = 160)
    private String rawMaterialKey;

    @Column(nullable = false, length = 240)
    private String materialName;

    @Column(nullable = false, length = 120)
    private String category;

    @Column(length = 180)
    private String supplierName;

    @Column(length = 120)
    private String lotNumber;

    @Column(nullable = false, length = 160)
    private String warehouseLocation;

    @Column(nullable = false, length = 32)
    private String unitOfMeasure;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantityOnHand;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal reorderPoint;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal unitCost;

    private LocalDate expirationDate;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InventoryMovementEntity> movements = new ArrayList<>();

    protected InventoryItemEntity() {
    }

    public InventoryItemEntity(
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
            LocalDate expirationDate,
            String status
    ) {
        replace(rawMaterialKey, materialName, category, supplierName, lotNumber, warehouseLocation, unitOfMeasure,
                quantityOnHand, reorderPoint, unitCost, expirationDate, status);
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void replace(
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
            LocalDate expirationDate,
            String status
    ) {
        this.rawMaterialKey = rawMaterialKey;
        this.materialName = materialName;
        this.category = category;
        this.supplierName = supplierName;
        this.lotNumber = lotNumber;
        this.warehouseLocation = warehouseLocation;
        this.unitOfMeasure = unitOfMeasure;
        this.quantityOnHand = quantityOnHand;
        this.reorderPoint = reorderPoint;
        this.unitCost = unitCost;
        this.expirationDate = expirationDate;
        this.status = status;
    }

    public void applyQuantityDelta(BigDecimal delta) {
        quantityOnHand = quantityOnHand.add(delta);
        if (quantityOnHand.signum() == 0 && !"HOLD".equals(status)) {
            status = "DEPLETED";
        } else if (quantityOnHand.signum() > 0 && "DEPLETED".equals(status)) {
            status = "ACTIVE";
        }
    }

    public UUID getId() {
        return id;
    }

    public String getRawMaterialKey() {
        return rawMaterialKey;
    }

    public String getMaterialName() {
        return materialName;
    }

    public String getCategory() {
        return category;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public BigDecimal getQuantityOnHand() {
        return quantityOnHand;
    }

    public BigDecimal getReorderPoint() {
        return reorderPoint;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
