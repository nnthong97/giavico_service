package com.giavico.process.persistence;

import com.giavico.process.api.ProcessRunRequests;
import com.giavico.process.domain.ProcessRunStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "process_runs", indexes = {
        @Index(name = "idx_process_run_workflow_status", columnList = "workflow_id,status"),
        @Index(name = "idx_process_run_updated", columnList = "updated_at")
})
public class ProcessRunEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workflow_id", nullable = false, length = 80)
    private String workflowId;

    @Column(nullable = false, length = 240)
    private String title;

    @Column(nullable = false, length = 160)
    private String owner;

    @Column(name = "current_step_id", nullable = false, length = 120)
    private String currentStepId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProcessRunStatus status;

    @Lob
    @Column(name = "step_statuses", nullable = false, columnDefinition = "TEXT")
    private String stepStatuses;

    @Lob
    @Column(name = "step_data", nullable = false, columnDefinition = "TEXT")
    private String stepData;

    @Lob
    @Column(name = "document_records", nullable = false, columnDefinition = "TEXT")
    private String documentRecords;

    @Lob
    @Column(name = "activity_log", nullable = false, columnDefinition = "TEXT")
    private String activityLog;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ProcessRunEntity() {
    }

    public ProcessRunEntity(
            ProcessRunRequests.Save request,
            String stepStatuses,
            String stepData,
            String documentRecords,
            String activityLog
    ) {
        status = ProcessRunStatus.ACTIVE;
        createdAt = Instant.now();
        updatedAt = createdAt;
        apply(request, stepStatuses, stepData, documentRecords, activityLog);
    }

    public void apply(
            ProcessRunRequests.Save request,
            String stepStatuses,
            String stepData,
            String documentRecords,
            String activityLog
    ) {
        workflowId = request.workflowId().trim();
        title = request.title().trim();
        owner = request.owner().trim();
        currentStepId = request.currentStepId().trim();
        status = request.status() == ProcessRunStatus.COMPLETED ? ProcessRunStatus.ACTIVE : request.status();
        this.stepStatuses = stepStatuses;
        this.stepData = stepData;
        this.documentRecords = documentRecords;
        this.activityLog = activityLog;
        updatedAt = Instant.now();
    }

    public void complete() {
        status = ProcessRunStatus.COMPLETED;
        completedAt = Instant.now();
        updatedAt = completedAt;
    }

    public UUID getId() { return id; }
    public String getWorkflowId() { return workflowId; }
    public String getTitle() { return title; }
    public String getOwner() { return owner; }
    public String getCurrentStepId() { return currentStepId; }
    public ProcessRunStatus getStatus() { return status; }
    public String getStepStatuses() { return stepStatuses; }
    public String getStepData() { return stepData; }
    public String getDocumentRecords() { return documentRecords; }
    public String getActivityLog() { return activityLog; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
