package com.giavico.beverage.persistence.repository;

import com.giavico.beverage.persistence.entity.ProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, UUID> {

    void deleteByFormulaSession_Id(UUID formulaSessionId);
}
