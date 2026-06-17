package com.giavico.beverage.service;

import com.giavico.beverage.api.dto.FormulaGenerationRequest;
import com.giavico.beverage.api.dto.FormulaGenerationResponse;
import com.giavico.beverage.api.dto.FormulaDetailResponse;
import com.giavico.beverage.api.dto.FormulaListItem;
import com.giavico.beverage.api.dto.FormulaStoreRequest;
import com.giavico.beverage.api.dto.IngredientComponent;
import com.giavico.beverage.api.dto.SavedFormulaMetadata;
import com.giavico.beverage.domain.FormulationValidator;
import com.giavico.beverage.exception.FormulaNotFoundException;
import com.giavico.beverage.persistence.entity.FormulaSessionEntity;
import com.giavico.beverage.persistence.entity.ProductEntity;
import com.giavico.beverage.persistence.entity.ProductVariantEntity;
import com.giavico.beverage.persistence.repository.FormulaSessionRepository;
import com.giavico.beverage.persistence.repository.ProductRepository;
import com.giavico.beverage.persistence.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FormulaPersistenceService {

    private final FormulaSessionRepository formulaSessionRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final FormulationValidator validator;

    public FormulaPersistenceService(
            FormulaSessionRepository formulaSessionRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            FormulationValidator validator
    ) {
        this.formulaSessionRepository = formulaSessionRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.validator = validator;
    }

    @Transactional
    public FormulaGenerationResponse persist(FormulaGenerationRequest request, FormulaGenerationResponse response) {
        FormulaSessionEntity session = new FormulaSessionEntity(
                request.drinkName(),
                request.marketDestination(),
                request.targetBrix(),
                request.isAcidified(),
                request.productionArea(),
                request.customerSpecification(),
                request.baselineBOM(),
                response.varianceAnalysis(),
                buildSummary(request, response)
        );
        session.addRegionalRestrictions(request.regionalRestrictions());
        session.addStabilityAlerts(response.stabilityAlerts());

        for (IngredientComponent ingredient : response.ingredients()) {
            session.addIngredient(ingredient.rawMaterialKey(), ingredient.massPercentage(), ingredient.costProjection());
        }

        FormulaSessionEntity savedSession = formulaSessionRepository.save(session);

        replaceProductVariant(request, savedSession);

        SavedFormulaMetadata metadata = new SavedFormulaMetadata(
                savedSession.getId(),
                savedSession.getName(),
                savedSession.getSummary(),
                savedSession.getCreatedAt()
        );

        return new FormulaGenerationResponse(
                response.ingredients(),
                response.varianceAnalysis(),
                response.stabilityAlerts(),
                metadata
        );
    }

    @Transactional
    public FormulaGenerationResponse store(FormulaStoreRequest request) {
        FormulaGenerationResponse response = new FormulaGenerationResponse(
                request.ingredients(),
                request.varianceAnalysis(),
                request.stabilityAlerts(),
                null
        );
        return persist(request.toGenerationRequest(), validator.sanitizeAndValidate(response));
    }

    @Transactional
    public FormulaDetailResponse update(UUID id, FormulaStoreRequest request) {
        FormulaSessionEntity session = formulaSessionRepository.findById(id)
                .orElseThrow(() -> new FormulaNotFoundException(id));

        FormulaGenerationResponse validated = validator.sanitizeAndValidate(new FormulaGenerationResponse(
                request.ingredients(),
                request.varianceAnalysis(),
                request.stabilityAlerts(),
                null
        ));
        FormulaGenerationRequest generationRequest = request.toGenerationRequest();

        session.replaceFormula(
                generationRequest.drinkName(),
                generationRequest.marketDestination(),
                generationRequest.targetBrix(),
                generationRequest.isAcidified(),
                generationRequest.productionArea(),
                generationRequest.customerSpecification(),
                generationRequest.baselineBOM(),
                validated.varianceAnalysis(),
                buildSummary(generationRequest, validated),
                generationRequest.regionalRestrictions(),
                validated.stabilityAlerts()
        );

        for (IngredientComponent ingredient : validated.ingredients()) {
            session.addIngredient(ingredient.rawMaterialKey(), ingredient.massPercentage(), ingredient.costProjection());
        }

        FormulaSessionEntity savedSession = formulaSessionRepository.save(session);
        replaceProductVariant(generationRequest, savedSession);
        return toDetail(savedSession);
    }

    @Transactional
    public void delete(UUID id) {
        if (!formulaSessionRepository.existsById(id)) {
            throw new FormulaNotFoundException(id);
        }
        productVariantRepository.deleteByFormulaSession_Id(id);
        formulaSessionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<FormulaListItem> list(Pageable pageable) {
        return formulaSessionRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toListItem);
    }

    @Transactional(readOnly = true)
    public FormulaDetailResponse get(UUID id) {
        FormulaSessionEntity session = formulaSessionRepository.findById(id)
                .orElseThrow(() -> new FormulaNotFoundException(id));
        return toDetail(session);
    }

    private String buildSummary(FormulaGenerationRequest request, FormulaGenerationResponse response) {
        String ingredientKeys = response.ingredients().stream()
                .limit(5)
                .map(IngredientComponent::rawMaterialKey)
                .collect(Collectors.joining(", "));
        return "%s for %s at %.2f Brix. Key materials: %s"
                .formatted(request.drinkName(), request.marketDestination(), request.targetBrix(), ingredientKeys);
    }

    private void replaceProductVariant(FormulaGenerationRequest request, FormulaSessionEntity savedSession) {
        productVariantRepository.deleteByFormulaSession_Id(savedSession.getId());

        ProductEntity product = productRepository.findByDrinkName(request.drinkName())
                .orElseGet(() -> productRepository.save(new ProductEntity(request.drinkName())));

        ProductVariantEntity variant = new ProductVariantEntity(
                product,
                savedSession,
                request.productionArea(),
                request.marketDestination(),
                request.customerSpecification(),
                String.join("\n", request.regionalRestrictions() == null ? List.of() : request.regionalRestrictions()),
                Instant.now()
        );
        productVariantRepository.save(variant);
    }

    private FormulaListItem toListItem(FormulaSessionEntity session) {
        return new FormulaListItem(
                session.getId(),
                session.getName(),
                session.getMarketDestination(),
                session.getTargetBrix(),
                session.getAcidified(),
                session.getProductionArea(),
                session.getBaselineBOM(),
                session.getSummary(),
                session.getCreatedAt(),
                session.getIngredients().size(),
                session.getStabilityAlerts().size()
        );
    }

    private FormulaDetailResponse toDetail(FormulaSessionEntity session) {
        List<IngredientComponent> ingredients = session.getIngredients().stream()
                .map(ingredient -> new IngredientComponent(
                        ingredient.getRawMaterialKey(),
                        ingredient.getMassPercentage(),
                        ingredient.getCostProjection()
                ))
                .toList();

        return new FormulaDetailResponse(
                session.getId(),
                session.getName(),
                session.getMarketDestination(),
                session.getTargetBrix(),
                session.getAcidified(),
                List.copyOf(session.getRegionalRestrictions()),
                session.getProductionArea(),
                session.getCustomerSpecification(),
                session.getBaselineBOM(),
                ingredients,
                session.getVarianceAnalysis(),
                List.copyOf(session.getStabilityAlerts()),
                session.getSummary(),
                session.getCreatedAt()
        );
    }
}
