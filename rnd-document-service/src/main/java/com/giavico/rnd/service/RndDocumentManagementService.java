package com.giavico.rnd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giavico.rnd.api.DocumentRequests;
import com.giavico.rnd.api.DocumentResponses;
import com.giavico.rnd.domain.DocumentStatus;
import com.giavico.rnd.domain.DocumentType;
import com.giavico.rnd.persistence.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RndDocumentManagementService {
    private static final Map<DocumentStatus, Map<String, DocumentStatus>> TRANSITIONS = transitions();
    private final RndDocumentRepository repository;
    private final ObjectMapper objectMapper;

    public RndDocumentManagementService(RndDocumentRepository repository, ObjectMapper objectMapper) {
        this.repository = repository; this.objectMapper = objectMapper;
    }

    @Transactional
    public DocumentResponses.Detail create(DocumentRequests.Save request) {
        RndDocumentEntity entity = new RndDocumentEntity(nextDocumentNumber(request.type()), request, writeJson(request.fieldValues()));
        entity.addRevision(request.owner(), "Document created");
        return detail(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponses.Summary> list(Pageable pageable, DocumentStatus status, String search) {
        Specification<RndDocumentEntity> spec = Specification.where(null);
        if (status != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("documentNumber")), pattern),
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("productName")), pattern)));
        }
        return repository.findAll(spec, pageable).map(this::summary);
    }

    @Transactional(readOnly = true)
    public DocumentResponses.Detail get(UUID id) { return detail(find(id)); }

    @Transactional
    public DocumentResponses.Detail update(UUID id, DocumentRequests.Save request) {
        RndDocumentEntity entity = find(id);
        if (entity.getStatus() != DocumentStatus.DRAFT && entity.getStatus() != DocumentStatus.CHANGES_REQUESTED) {
            throw new InvalidWorkflowException("Only draft documents or documents with requested changes can be edited.");
        }
        entity.incrementRevision(); entity.apply(request, writeJson(request.fieldValues()));
        entity.addRevision(request.owner(), "Document fields updated");
        return detail(repository.save(entity));
    }

    @Transactional
    public DocumentResponses.Detail transition(UUID id, String action, DocumentRequests.Workflow request) {
        RndDocumentEntity entity = find(id);
        DocumentStatus next = TRANSITIONS.getOrDefault(entity.getStatus(), Map.of()).get(action);
        if (next == null) throw new InvalidWorkflowException("Action '%s' is not allowed while document is %s.".formatted(action, entity.getStatus()));
        if ("request-changes".equals(action) && (request.comment() == null || request.comment().isBlank())) {
            throw new InvalidWorkflowException("A comment is required when requesting changes.");
        }
        entity.transition(next); entity.addApproval(action, request.actor(), request.comment());
        entity.addRevision(request.actor(), request.comment() == null || request.comment().isBlank() ? "Workflow action: " + action : request.comment().trim());
        return detail(repository.save(entity));
    }

    private RndDocumentEntity find(UUID id) { return repository.findById(id).orElseThrow(() -> new DocumentNotFoundException(id)); }
    private String nextDocumentNumber(DocumentType type) {
        Instant now = Instant.now();
        var date = now.atZone(ZoneOffset.UTC).toLocalDate();
        Instant start = date.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = date.with(TemporalAdjusters.lastDayOfYear()).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        long sequence = repository.countByTypeAndCreatedAtBetween(type, start, end) + 1;
        return "%s-%s-%04d".formatted(type.formNumber(), date.format(DateTimeFormatter.ofPattern("yyyy")), sequence);
    }
    private String writeJson(Map<String, Object> values) { try { return objectMapper.writeValueAsString(values); } catch (JsonProcessingException error) { throw new IllegalArgumentException("Invalid document field values.", error); } }
    private Map<String, Object> readJson(String value) { try { return objectMapper.readValue(value, new TypeReference<>() {}); } catch (JsonProcessingException error) { throw new IllegalStateException("Stored document fields are invalid.", error); } }
    private DocumentResponses.Summary summary(RndDocumentEntity value) { return new DocumentResponses.Summary(value.getId(), value.getDocumentNumber(), value.getType(), value.getTitle(), value.getProductName(), value.getFormulaUuid(), value.getStatus(), value.getRevision(), value.getOwner(), value.getCreatedAt(), value.getUpdatedAt()); }
    private DocumentResponses.Detail detail(RndDocumentEntity value) {
        return new DocumentResponses.Detail(value.getId(), value.getDocumentNumber(), value.getType(), value.getTitle(), value.getProductName(), value.getFormulaUuid(), value.getStatus(), value.getRevision(), value.getOwner(), value.getCreatedAt(), value.getUpdatedAt(), value.getMarket(), value.getEffectiveDate(), readJson(value.getFieldValues()),
                value.getApprovals().stream().map(item -> new DocumentResponses.Approval(item.getAction(), item.getActor(), item.getComment(), item.getCreatedAt())).toList(),
                value.getRevisions().stream().map(item -> new DocumentResponses.Revision(item.getRevision(), item.getStatus(), item.getChangedBy(), item.getChangeSummary(), item.getCreatedAt())).toList());
    }
    private static Map<DocumentStatus, Map<String, DocumentStatus>> transitions() {
        Map<DocumentStatus, Map<String, DocumentStatus>> result = new EnumMap<>(DocumentStatus.class);
        result.put(DocumentStatus.DRAFT, Map.of("submit", DocumentStatus.SUBMITTED));
        result.put(DocumentStatus.CHANGES_REQUESTED, Map.of("submit", DocumentStatus.SUBMITTED));
        result.put(DocumentStatus.SUBMITTED, Map.of("start-review", DocumentStatus.UNDER_REVIEW));
        result.put(DocumentStatus.UNDER_REVIEW, Map.of("approve", DocumentStatus.APPROVED, "request-changes", DocumentStatus.CHANGES_REQUESTED));
        result.put(DocumentStatus.APPROVED, Map.of("issue", DocumentStatus.ISSUED));
        result.put(DocumentStatus.ISSUED, Map.of("acknowledge", DocumentStatus.ACKNOWLEDGED));
        return Map.copyOf(result);
    }
}
