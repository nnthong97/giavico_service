package com.giavico.beverage.api.controller;

import com.giavico.beverage.api.dto.InventoryItemRequest;
import com.giavico.beverage.api.dto.InventoryItemResponse;
import com.giavico.beverage.api.dto.InventoryMovementRequest;
import com.giavico.beverage.api.dto.InventoryMovementResponse;
import com.giavico.beverage.service.InventoryManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/inventory")
public class InventoryManagementController {

    private final InventoryManagementService inventoryManagementService;

    public InventoryManagementController(InventoryManagementService inventoryManagementService) {
        this.inventoryManagementService = inventoryManagementService;
    }

    @GetMapping("/items")
    public Page<InventoryItemResponse> listItems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @Pattern(regexp = "ACTIVE|HOLD|EXPIRED|DEPLETED") String status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return inventoryManagementService.list(search, status, pageable);
    }

    @PostMapping("/items")
    public InventoryItemResponse createItem(@Valid @RequestBody InventoryItemRequest request) {
        return inventoryManagementService.create(request);
    }

    @GetMapping("/items/{id}")
    public InventoryItemResponse getItem(@PathVariable UUID id) {
        return inventoryManagementService.get(id);
    }

    @GetMapping("/items/by-raw-material-key/{rawMaterialKey}")
    public InventoryItemResponse getItemByRawMaterialKey(@PathVariable String rawMaterialKey) {
        return inventoryManagementService.getByRawMaterialKey(rawMaterialKey);
    }

    @PutMapping("/items/{id}")
    public InventoryItemResponse updateItem(@PathVariable UUID id, @Valid @RequestBody InventoryItemRequest request) {
        return inventoryManagementService.update(id, request);
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable UUID id) {
        inventoryManagementService.delete(id);
    }

    @PostMapping("/items/{id}/movements")
    public InventoryMovementResponse recordMovement(
            @PathVariable UUID id,
            @Valid @RequestBody InventoryMovementRequest request
    ) {
        return inventoryManagementService.recordMovement(id, request);
    }

    @GetMapping("/items/{id}/movements")
    public Page<InventoryMovementResponse> listMovements(
            @PathVariable UUID id,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return inventoryManagementService.listMovements(id, pageable);
    }

    @GetMapping("/alerts/low-stock")
    public List<InventoryItemResponse> lowStock() {
        return inventoryManagementService.lowStock();
    }
}
