package com.giavico.rnd.service;
import java.util.UUID;
public class DocumentNotFoundException extends RuntimeException { 
    public DocumentNotFoundException(UUID id) { 
        super("R&D document not found: " + id); 
    } 
}
