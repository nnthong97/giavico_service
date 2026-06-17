package com.giavico.beverage.persistence.repository;

import com.giavico.beverage.persistence.entity.InventoryItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, UUID> {

    boolean existsByRawMaterialKeyIgnoreCase(String rawMaterialKey);

    Optional<InventoryItemEntity> findByRawMaterialKeyIgnoreCase(String rawMaterialKey);

    @Query("""
            select item
            from InventoryItemEntity item
            where (:status is null or item.status = :status)
              and (:search is null
                   or lower(item.rawMaterialKey) like lower(concat('%', :search, '%'))
                   or lower(item.materialName) like lower(concat('%', :search, '%'))
                   or lower(item.category) like lower(concat('%', :search, '%'))
                   or lower(item.supplierName) like lower(concat('%', :search, '%'))
                   or lower(item.lotNumber) like lower(concat('%', :search, '%')))
            order by item.materialName asc
            """)
    Page<InventoryItemEntity> search(@Param("search") String search, @Param("status") String status, Pageable pageable);

    @Query("""
            select item
            from InventoryItemEntity item
            where item.quantityOnHand <= item.reorderPoint
              and item.status <> 'HOLD'
            order by item.quantityOnHand asc
            """)
    List<InventoryItemEntity> findLowStockItems();
}
