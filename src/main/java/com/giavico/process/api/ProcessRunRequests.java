package com.giavico.process.api;

import com.giavico.process.domain.ProcessRunStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public final class ProcessRunRequests {
    private ProcessRunRequests() {
    }

    public record Save(
            @NotBlank String workflowId,
            @NotBlank String title,
            @NotBlank String owner,
            @NotBlank String currentStepId,
            @NotNull ProcessRunStatus status,
            @NotNull Map<String, String> stepStatuses,
            @NotNull Map<String, Object> stepData,
            @NotNull Map<String, Object> documentRecords,
            @NotNull List<Map<String, Object>> activityLog
    ) {
    }
}
