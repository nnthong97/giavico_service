package com.giavico.beverage.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "formula_sessions")
public class FormulaSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 240)
    private String marketDestination;

    @Column(nullable = false)
    private Double targetBrix;

    @Column(nullable = false)
    private Boolean acidified;

    @Column(nullable = false, length = 240)
    private String productionArea;

    @Lob
    @Column(nullable = false)
    private String customerSpecification;

    @Column(length = 160)
    private String baselineBOM;

    @Lob
    private String varianceAnalysis;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ElementCollection
    @CollectionTable(name = "formula_regional_restrictions", joinColumns = @JoinColumn(name = "formula_session_id"))
    @Column(name = "restriction_text", length = 500)
    private List<String> regionalRestrictions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "formula_stability_alerts", joinColumns = @JoinColumn(name = "formula_session_id"))
    @Column(name = "alert_text", length = 1000)
    private List<String> stabilityAlerts = new ArrayList<>();

    @OneToMany(mappedBy = "formulaSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FormulaIngredientEntity> ingredients = new ArrayList<>();

    protected FormulaSessionEntity() {
    }

    public FormulaSessionEntity(
            String name,
            String marketDestination,
            Double targetBrix,
            Boolean acidified,
            String productionArea,
            String customerSpecification,
            String baselineBOM,
            String varianceAnalysis,
            String summary
    ) {
        this.name = name;
        this.marketDestination = marketDestination;
        this.targetBrix = targetBrix;
        this.acidified = acidified;
        this.productionArea = productionArea;
        this.customerSpecification = customerSpecification;
        this.baselineBOM = baselineBOM;
        this.varianceAnalysis = varianceAnalysis;
        this.summary = summary;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void addRegionalRestrictions(List<String> restrictions) {
        if (restrictions != null) {
            regionalRestrictions.addAll(restrictions);
        }
    }

    public void addStabilityAlerts(List<String> alerts) {
        if (alerts != null) {
            stabilityAlerts.addAll(alerts);
        }
    }

    public void addIngredient(String rawMaterialKey, Double massPercentage, java.math.BigDecimal costProjection) {
        ingredients.add(new FormulaIngredientEntity(this, rawMaterialKey, massPercentage, costProjection));
    }

    public void replaceFormula(
            String name,
            String marketDestination,
            Double targetBrix,
            Boolean acidified,
            String productionArea,
            String customerSpecification,
            String baselineBOM,
            String varianceAnalysis,
            String summary,
            List<String> regionalRestrictions,
            List<String> stabilityAlerts,
            List<com.giavico.beverage.api.dto.IngredientComponent> ingredients
    ) {
        this.name = name;
        this.marketDestination = marketDestination;
        this.targetBrix = targetBrix;
        this.acidified = acidified;
        this.productionArea = productionArea;
        this.customerSpecification = customerSpecification;
        this.baselineBOM = baselineBOM;
        this.varianceAnalysis = varianceAnalysis;
        this.summary = summary;
        this.regionalRestrictions.clear();
        this.stabilityAlerts.clear();
        this.ingredients.clear();
        addRegionalRestrictions(regionalRestrictions);
        addStabilityAlerts(stabilityAlerts);
        if (ingredients != null) {
            for (com.giavico.beverage.api.dto.IngredientComponent ingredient : ingredients) {
                addIngredient(ingredient.rawMaterialKey(), ingredient.massPercentage(), ingredient.costProjection());
            }
        }
    }

    public void replaceFormula(
            String name,
            String marketDestination,
            Double targetBrix,
            Boolean acidified,
            String productionArea,
            String customerSpecification,
            String baselineBOM,
            String varianceAnalysis,
            String summary,
            List<String> regionalRestrictions,
            List<String> stabilityAlerts
    ) {
        this.name = name;
        this.marketDestination = marketDestination;
        this.targetBrix = targetBrix;
        this.acidified = acidified;
        this.productionArea = productionArea;
        this.customerSpecification = customerSpecification;
        this.baselineBOM = baselineBOM;
        this.varianceAnalysis = varianceAnalysis;
        this.summary = summary;

        this.regionalRestrictions.clear();
        addRegionalRestrictions(regionalRestrictions);

        this.stabilityAlerts.clear();
        addStabilityAlerts(stabilityAlerts);

        this.ingredients.clear();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMarketDestination() {
        return marketDestination;
    }

    public Double getTargetBrix() {
        return targetBrix;
    }

    public Boolean getAcidified() {
        return acidified;
    }

    public String getProductionArea() {
        return productionArea;
    }

    public String getCustomerSpecification() {
        return customerSpecification;
    }

    public String getBaselineBOM() {
        return baselineBOM;
    }

    public String getVarianceAnalysis() {
        return varianceAnalysis;
    }

    public String getSummary() {
        return summary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<String> getRegionalRestrictions() {
        return regionalRestrictions;
    }

    public List<String> getStabilityAlerts() {
        return stabilityAlerts;
    }

    public List<FormulaIngredientEntity> getIngredients() {
        return ingredients;
    }
}
