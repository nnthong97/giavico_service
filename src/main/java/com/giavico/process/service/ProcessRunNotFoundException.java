package com.giavico.process.service;

import java.util.UUID;

public class ProcessRunNotFoundException extends RuntimeException {
    public ProcessRunNotFoundException(UUID id) {
        super("Process run not found: " + id);
    }

    public ProcessRunNotFoundException(String workflowId) {
        super("No active process run found for workflow: " + workflowId);
    }
}
