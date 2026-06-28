package com.giavico.process.api;

import com.giavico.process.domain.ProcessRunStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ProcessRunResponses {
    private ProcessRunResponses() {
    }

    public record Summary(
            UUID uuid,
            String workflowId,
            String title,
            String owner,
            String currentStepId,
            ProcessRunStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt
    ) {
    }

    public record Detail(
            UUID uuid,
            String workflowId,
            String title,
            String owner,
            String currentStepId,
            ProcessRunStatus status,
            Map<String, String> stepStatuses,
            Map<String, Object> stepData,
            Map<String, Object> documentRecords,
            List<Map<String, Object>> activityLog,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt
    ) {
    }
}
