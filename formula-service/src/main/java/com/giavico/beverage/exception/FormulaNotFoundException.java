package com.giavico.beverage.exception;

import java.util.UUID;

public class FormulaNotFoundException extends RuntimeException {

    public FormulaNotFoundException(UUID id) {
        super("Formula was not found for id: " + id);
    }
}
