package com.giavico.process.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giavico.process.api.ProcessRunRequests;
import com.giavico.process.api.ProcessRunResponses;
import com.giavico.process.domain.ProcessRunStatus;
import com.giavico.process.persistence.ProcessRunEntity;
import com.giavico.process.persistence.ProcessRunRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProcessRunManagementService {
    private static final List<ProcessRunStatus> OPEN_STATUSES = List.of(ProcessRunStatus.ACTIVE, ProcessRunStatus.BLOCKED);

    private final ProcessRunRepository repository;
    private final ObjectMapper objectMapper;

    public ProcessRunManagementService(ProcessRunRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProcessRunResponses.Detail create(ProcessRunRequests.Save request) {
        ProcessRunEntity entity = new ProcessRunEntity(
                request,
                writeJson(request.stepStatuses()),
                writeJson(request.stepData()),
                writeJson(request.documentRecords()),
                writeJson(request.activityLog())
        );
        return detail(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ProcessRunResponses.Detail get(UUID id) {
        return detail(find(id));
    }

    @Transactional(readOnly = true)
    public ProcessRunResponses.Detail latestOpen(String workflowId) {
        return repository.findFirstByWorkflowIdAndStatusInOrderByUpdatedAtDesc(workflowId, OPEN_STATUSES)
                .map(this::detail)
                .orElseThrow(() -> new ProcessRunNotFoundException(workflowId));
    }

    @Transactional(readOnly = true)
    public Page<ProcessRunResponses.Summary> list(
            Pageable pageable,
            String workflowId,
            ProcessRunStatus status
    ) {
        Specification<ProcessRunEntity> specification = Specification.where(null);
        if (workflowId != null && !workflowId.isBlank()) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("workflowId"), workflowId.trim()));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("status"), status));
        }
        return repository.findAll(specification, pageable).map(this::summary);
    }

    @Transactional
    public ProcessRunResponses.Detail update(UUID id, ProcessRunRequests.Save request) {
        ProcessRunEntity entity = find(id);
        requireOpen(entity);
        requireSameWorkflow(entity, request);
        apply(entity, request);
        return detail(repository.save(entity));
    }

    @Transactional
    public ProcessRunResponses.Detail complete(UUID id, ProcessRunRequests.Save request) {
        ProcessRunEntity entity = find(id);
        requireOpen(entity);
        requireSameWorkflow(entity, request);
        if (request.stepStatuses().isEmpty() || request.stepStatuses().values().stream().anyMatch(status -> !"done".equals(status))) {
            throw new InvalidProcessRunException("Every process step must be done before the run can be completed.");
        }
        apply(entity, request);
        entity.complete();
        return detail(repository.save(entity));
    }

    private void apply(ProcessRunEntity entity, ProcessRunRequests.Save request) {
        entity.apply(
                request,
                writeJson(request.stepStatuses()),
                writeJson(request.stepData()),
                writeJson(request.documentRecords()),
                writeJson(request.activityLog())
        );
    }

    private void requireOpen(ProcessRunEntity entity) {
        if (entity.getStatus() == ProcessRunStatus.COMPLETED) {
            throw new InvalidProcessRunException("Completed process runs are immutable. Start a new run instead.");
        }
    }

    private void requireSameWorkflow(ProcessRunEntity entity, ProcessRunRequests.Save request) {
        if (!entity.getWorkflowId().equals(request.workflowId().trim())) {
            throw new InvalidProcessRunException("The workflow type of a process run cannot be changed.");
        }
    }

    private ProcessRunEntity find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ProcessRunNotFoundException(id));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException error) {
            throw new InvalidProcessRunException("Process run data is not valid JSON.");
        }
    }

    private <T> T readJson(String value, TypeReference<T> type) {
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("Stored process run data is invalid.", error);
        }
    }

    private ProcessRunResponses.Summary summary(ProcessRunEntity value) {
        return new ProcessRunResponses.Summary(
                value.getId(), value.getWorkflowId(), value.getTitle(), value.getOwner(),
                value.getCurrentStepId(), value.getStatus(), value.getCreatedAt(),
                value.getUpdatedAt(), value.getCompletedAt()
        );
    }

    private ProcessRunResponses.Detail detail(ProcessRunEntity value) {
        return new ProcessRunResponses.Detail(
                value.getId(), value.getWorkflowId(), value.getTitle(), value.getOwner(),
                value.getCurrentStepId(), value.getStatus(),
                readJson(value.getStepStatuses(), new TypeReference<Map<String, String>>() {}),
                readJson(value.getStepData(), new TypeReference<Map<String, Object>>() {}),
                readJson(value.getDocumentRecords(), new TypeReference<Map<String, Object>>() {}),
                readJson(value.getActivityLog(), new TypeReference<List<Map<String, Object>>>() {}),
                value.getCreatedAt(), value.getUpdatedAt(), value.getCompletedAt()
        );
    }
}
