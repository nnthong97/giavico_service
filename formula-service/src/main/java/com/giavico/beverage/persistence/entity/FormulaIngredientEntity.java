package com.giavico.beverage.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "formula_ingredients")
public class FormulaIngredientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formula_session_id", nullable = false)
    private FormulaSessionEntity formulaSession;

    @Column(nullable = false, length = 160)
    private String rawMaterialKey;

    @Column(nullable = false)
    private Double massPercentage;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal costProjection;

    protected FormulaIngredientEntity() {
    }

    public FormulaIngredientEntity(FormulaSessionEntity formulaSession, String rawMaterialKey, Double massPercentage, BigDecimal costProjection) {
        this.formulaSession = formulaSession;
        this.rawMaterialKey = rawMaterialKey;
        this.massPercentage = massPercentage;
        this.costProjection = costProjection;
    }

    public UUID getId() {
        return id;
    }

    public String getRawMaterialKey() {
        return rawMaterialKey;
    }

    public Double getMassPercentage() {
        return massPercentage;
    }

    public BigDecimal getCostProjection() {
        return costProjection;
    }
}
