package com.giavico.beverage.exception;

import java.util.UUID;

public class InventoryItemNotFoundException extends RuntimeException {

    public InventoryItemNotFoundException(UUID id) {
        super("Inventory item was not found for id: " + id);
    }

    public InventoryItemNotFoundException(String rawMaterialKey) {
        super("Inventory item was not found for raw material key: " + rawMaterialKey);
    }
}
