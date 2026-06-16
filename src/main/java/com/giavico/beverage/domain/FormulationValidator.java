package com.giavico.beverage.domain;

import com.giavico.beverage.api.dto.FormulaGenerationResponse;
import com.giavico.beverage.api.dto.IngredientComponent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class FormulationValidator {

    private static final double TARGET_TOTAL_MASS_PERCENT = 100.0d;
    private static final double MASS_BALANCE_TOLERANCE_PERCENT = 0.5d;
    private static final String DEFAULT_VARIANCE_ANALYSIS = "No variance analysis provided by the LLM model.";

    public FormulaGenerationResponse sanitizeAndValidate(FormulaGenerationResponse response) {
        List<IngredientComponent> ingredients = response.ingredients() == null ? List.of() : response.ingredients();
        String varianceAnalysis = StringUtils.hasText(response.varianceAnalysis())
                ? response.varianceAnalysis()
                : DEFAULT_VARIANCE_ANALYSIS;

        List<String> alerts = response.stabilityAlerts() == null
                ? new ArrayList<>()
                : new ArrayList<>(response.stabilityAlerts());

        double totalMass = ingredients.stream()
                .map(IngredientComponent::massPercentage)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .sum();

        double deviation = Math.abs(totalMass - TARGET_TOTAL_MASS_PERCENT);
        if (deviation > MASS_BALANCE_TOLERANCE_PERCENT) {
            alerts.add("Mass-balance warning: ingredient mass percentages sum to %.3f%%, outside the allowed 100.0%% +/- %.1f%% tolerance."
                    .formatted(totalMass, MASS_BALANCE_TOLERANCE_PERCENT));
        }

        return new FormulaGenerationResponse(
                ingredients,
                varianceAnalysis,
                List.copyOf(alerts),
                response.savedFormula()
        );
    }
}
