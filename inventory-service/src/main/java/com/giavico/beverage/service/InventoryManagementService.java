package com.giavico.beverage.service;

import com.giavico.beverage.api.dto.InventoryItemRequest;
import com.giavico.beverage.api.dto.InventoryItemResponse;
import com.giavico.beverage.api.dto.InventoryMovementRequest;
import com.giavico.beverage.api.dto.InventoryMovementResponse;
import com.giavico.beverage.exception.InventoryItemNotFoundException;
import com.giavico.beverage.exception.InventoryValidationException;
import com.giavico.beverage.persistence.entity.InventoryItemEntity;
import com.giavico.beverage.persistence.entity.InventoryMovementEntity;
import com.giavico.beverage.persistence.repository.InventoryItemRepository;
import com.giavico.beverage.persistence.repository.InventoryMovementRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryManagementService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public InventoryManagementService(
            InventoryItemRepository inventoryItemRepository,
            InventoryMovementRepository inventoryMovementRepository
    ) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @Transactional(readOnly = true)
    public Page<InventoryItemResponse> list(String search, String status, Pageable pageable) {
        String normalizedSearch = StringUtils.hasText(search) ? search.trim() : null;
        String normalizedStatus = StringUtils.hasText(status) ? status.trim().toUpperCase() : null;
        return inventoryItemRepository.search(normalizedSearch, normalizedStatus, pageable)
                .map(this::toItemResponse);
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse get(UUID id) {
        return toItemResponse(findItem(id));
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getByRawMaterialKey(String rawMaterialKey) {
        InventoryItemEntity item = inventoryItemRepository.findByRawMaterialKeyIgnoreCase(rawMaterialKey)
                .orElseThrow(() -> new InventoryItemNotFoundException(rawMaterialKey));
        return toItemResponse(item);
    }

    @Transactional
    public InventoryItemResponse create(InventoryItemRequest request) {
        if (inventoryItemRepository.existsByRawMaterialKeyIgnoreCase(request.rawMaterialKey())) {
            throw new InventoryValidationException("Raw material key already exists: " + request.rawMaterialKey());
        }

        try {
            InventoryItemEntity saved = inventoryItemRepository.save(new InventoryItemEntity(
                    request.rawMaterialKey(),
                    request.materialName(),
                    request.category(),
                    request.supplierName(),
                    request.lotNumber(),
                    request.warehouseLocation(),
                    request.unitOfMeasure(),
                    request.quantityOnHand(),
                    request.reorderPoint(),
                    request.unitCost(),
                    request.expirationDate(),
                    request.status()
            ));
            return toItemResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new InventoryValidationException("Inventory item could not be saved. Check raw material key uniqueness.");
        }
    }

    @Transactional
    public InventoryItemResponse update(UUID id, InventoryItemRequest request) {
        InventoryItemEntity item = findItem(id);
        inventoryItemRepository.findByRawMaterialKeyIgnoreCase(request.rawMaterialKey())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new InventoryValidationException("Raw material key already exists: " + request.rawMaterialKey());
                });

        item.replace(
                request.rawMaterialKey(),
                request.materialName(),
                request.category(),
                request.supplierName(),
                request.lotNumber(),
                request.warehouseLocation(),
                request.unitOfMeasure(),
                request.quantityOnHand(),
                request.reorderPoint(),
                request.unitCost(),
                request.expirationDate(),
                request.status()
        );
        return toItemResponse(inventoryItemRepository.save(item));
    }

    @Transactional
    public void delete(UUID id) {
        if (!inventoryItemRepository.existsById(id)) {
            throw new InventoryItemNotFoundException(id);
        }
        inventoryItemRepository.deleteById(id);
    }

    @Transactional
    public InventoryMovementResponse recordMovement(UUID itemId, InventoryMovementRequest request) {
        InventoryItemEntity item = findItem(itemId);
        BigDecimal delta = resolveQuantityDelta(request);
        BigDecimal resultingQuantity = item.getQuantityOnHand().add(delta);
        if (!Boolean.TRUE.equals(request.allowNegative()) && resultingQuantity.signum() < 0) {
            throw new InventoryValidationException("Movement would make inventory quantity negative.");
        }

        item.applyQuantityDelta(delta);
        InventoryMovementEntity movement = inventoryMovementRepository.save(new InventoryMovementEntity(
                item,
                request.movementType(),
                delta,
                item.getQuantityOnHand(),
                request.referenceType(),
                request.referenceId(),
                request.reason(),
                request.performedBy()
        ));
        inventoryItemRepository.save(item);
        return toMovementResponse(movement);
    }

    @Transactional(readOnly = true)
    public Page<InventoryMovementResponse> listMovements(UUID itemId, Pageable pageable) {
        if (!inventoryItemRepository.existsById(itemId)) {
            throw new InventoryItemNotFoundException(itemId);
        }
        return inventoryMovementRepository.findByInventoryItem_IdOrderByCreatedAtDesc(itemId, pageable)
                .map(this::toMovementResponse);
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> lowStock() {
        return inventoryItemRepository.findLowStockItems().stream()
                .map(this::toItemResponse)
                .toList();
    }

    private InventoryItemEntity findItem(UUID id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new InventoryItemNotFoundException(id));
    }

    private BigDecimal resolveQuantityDelta(InventoryMovementRequest request) {
        if (request.quantity() == null || request.quantity().signum() == 0) {
            throw new InventoryValidationException("Movement quantity must not be zero.");
        }

        return switch (request.movementType()) {
            case "RECEIPT" -> request.quantity().abs();
            case "ISSUE" -> request.quantity().abs().negate();
            case "ADJUSTMENT" -> request.quantity();
            default -> throw new InventoryValidationException("Unsupported movement type: " + request.movementType());
        };
    }

    private InventoryItemResponse toItemResponse(InventoryItemEntity item) {
        return new InventoryItemResponse(
                item.getId(),
                item.getRawMaterialKey(),
                item.getMaterialName(),
                item.getCategory(),
                item.getSupplierName(),
                item.getLotNumber(),
                item.getWarehouseLocation(),
                item.getUnitOfMeasure(),
                item.getQuantityOnHand(),
                item.getReorderPoint(),
                item.getUnitCost(),
                item.getQuantityOnHand().multiply(item.getUnitCost()),
                item.getExpirationDate(),
                item.getStatus(),
                item.getQuantityOnHand().compareTo(item.getReorderPoint()) <= 0,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private InventoryMovementResponse toMovementResponse(InventoryMovementEntity movement) {
        InventoryItemEntity item = movement.getInventoryItem();
        return new InventoryMovementResponse(
                movement.getId(),
                item.getId(),
                item.getRawMaterialKey(),
                movement.getMovementType(),
                movement.getQuantityDelta(),
                movement.getResultingQuantity(),
                movement.getReferenceType(),
                movement.getReferenceId(),
                movement.getReason(),
                movement.getPerformedBy(),
                movement.getCreatedAt()
        );
    }
}
