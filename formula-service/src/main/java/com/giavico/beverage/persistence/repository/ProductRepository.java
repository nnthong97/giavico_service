package com.giavico.beverage.persistence.repository;

import com.giavico.beverage.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    Optional<ProductEntity> findByDrinkName(String drinkName);
}
