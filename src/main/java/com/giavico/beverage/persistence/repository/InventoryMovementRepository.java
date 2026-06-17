package com.giavico.beverage.persistence.repository;

import com.giavico.beverage.persistence.entity.InventoryMovementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovementEntity, UUID> {

    Page<InventoryMovementEntity> findByInventoryItem_IdOrderByCreatedAtDesc(UUID inventoryItemId, Pageable pageable);
}
