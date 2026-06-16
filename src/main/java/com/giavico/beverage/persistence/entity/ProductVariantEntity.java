package com.giavico.beverage.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
public class ProductVariantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_session_id")
    private FormulaSessionEntity formulaSession;

    @Column(nullable = false, length = 240)
    private String productionArea;

    @Column(nullable = false, length = 240)
    private String marketDestination;

    @Lob
    @Column(nullable = false)
    private String customerSpecification;

    @Lob
    private String regulatorySnapshot;

    @Column(nullable = false)
    private Instant effectiveFrom;

    private Instant effectiveTo;

    protected ProductVariantEntity() {
    }

    public ProductVariantEntity(
            ProductEntity product,
            FormulaSessionEntity formulaSession,
            String productionArea,
            String marketDestination,
            String customerSpecification,
            String regulatorySnapshot,
            Instant effectiveFrom
    ) {
        this.product = product;
        this.formulaSession = formulaSession;
        this.productionArea = productionArea;
        this.marketDestination = marketDestination;
        this.customerSpecification = customerSpecification;
        this.regulatorySnapshot = regulatorySnapshot;
        this.effectiveFrom = effectiveFrom;
    }
}
