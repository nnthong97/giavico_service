package com.giavico.process.service;

public class InvalidProcessRunException extends RuntimeException {
    public InvalidProcessRunException(String message) {
        super(message);
    }
}
