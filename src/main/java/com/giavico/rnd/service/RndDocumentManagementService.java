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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RndDocumentManagementService {
    private static final Map<DocumentStatus, Map<String, DocumentStatus>> TRANSITIONS = transitions();
    private final RndDocumentRepository repository;
    private final ObjectMapper objectMapper;
    private final DocumentFieldValidator fieldValidator;
    private final DocumentTemplateCatalog templates;

    public RndDocumentManagementService(RndDocumentRepository repository, ObjectMapper objectMapper, DocumentFieldValidator fieldValidator,
                                        DocumentTemplateCatalog templates) {
        this.repository = repository; this.objectMapper = objectMapper; this.fieldValidator = fieldValidator; this.templates = templates;
    }

    @Transactional
    public DocumentResponses.Detail create(DocumentRequests.Save request) {
        fieldValidator.validate(request.type(), request.fieldValues());
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
        fieldValidator.validate(request.type(), request.fieldValues());
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
        if ("approve".equals(action) && entity.getStatus() == DocumentStatus.UNDER_REVIEW) {
            approveRole(entity, request);
            entity.addRevision(request.actor(), "Approved as " + request.role().trim());
            return detail(repository.save(entity));
        }
        DocumentStatus next = TRANSITIONS.getOrDefault(entity.getStatus(), Map.of()).get(action);
        if (next == null) throw new InvalidWorkflowException("Action '%s' is not allowed while document is %s.".formatted(action, entity.getStatus()));
        if ("request-changes".equals(action) && (request.comment() == null || request.comment().isBlank())) {
            throw new InvalidWorkflowException("A comment is required when requesting changes.");
        }
        entity.transition(next); entity.addApproval(action, request.actor(), null, request.comment());
        entity.addRevision(request.actor(), request.comment() == null || request.comment().isBlank() ? "Workflow action: " + action : request.comment().trim());
        return detail(repository.save(entity));
    }

    private void approveRole(RndDocumentEntity entity, DocumentRequests.Workflow request) {
        List<String> allowedRoles = templates.find(entity.getType()).approvals().stream().map(role -> role.en()).toList();
        if (allowedRoles.isEmpty()) {
            entity.addApproval("approve", request.actor(), null, request.comment());
            entity.transition(DocumentStatus.APPROVED);
            return;
        }
        String role = request.role() == null ? "" : request.role().trim();
        if (!allowedRoles.contains(role)) throw new InvalidWorkflowException("Approval role is required and must belong to the document template.");
        boolean duplicate = entity.getApprovals().stream().anyMatch(item -> "approve".equals(item.getAction()) &&
                Integer.valueOf(entity.getRevision()).equals(item.getRevision()) && role.equals(item.getRole()));
        if (duplicate) throw new InvalidWorkflowException("Role '%s' has already approved revision %d.".formatted(role, entity.getRevision()));
        entity.addApproval("approve", request.actor(), role, request.comment());
        long approved = entity.getApprovals().stream().filter(item -> "approve".equals(item.getAction()) &&
                Integer.valueOf(entity.getRevision()).equals(item.getRevision()) && item.getRole() != null).map(item -> item.getRole()).distinct().count();
        if (approved == allowedRoles.size()) entity.transition(DocumentStatus.APPROVED);
    }

    @Transactional
    public void delete(UUID id) {
        RndDocumentEntity entity = find(id);
        if (entity.getStatus() != DocumentStatus.DRAFT && entity.getStatus() != DocumentStatus.CHANGES_REQUESTED) {
            throw new InvalidWorkflowException("Only draft documents or documents with requested changes can be deleted.");
        }
        repository.delete(entity);
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
                value.getApprovals().stream().map(item -> new DocumentResponses.Approval(item.getAction(), item.getActor(), item.getRole(), item.getRevision(), item.getComment(), item.getCreatedAt())).toList(),
                value.getRevisions().stream().map(item -> new DocumentResponses.Revision(item.getRevision(), item.getStatus(), item.getChangedBy(), item.getChangeSummary(), item.getCreatedAt())).toList());
    }
    private static Map<DocumentStatus, Map<String, DocumentStatus>> transitions() {
        Map<DocumentStatus, Map<String, DocumentStatus>> result = new EnumMap<>(DocumentStatus.class);
        result.put(DocumentStatus.DRAFT, Map.of("submit", DocumentStatus.SUBMITTED));
        result.put(DocumentStatus.CHANGES_REQUESTED, Map.of("submit", DocumentStatus.SUBMITTED));
        result.put(DocumentStatus.SUBMITTED, Map.of("start-review", DocumentStatus.UNDER_REVIEW));
        result.put(DocumentStatus.UNDER_REVIEW, Map.of("request-changes", DocumentStatus.CHANGES_REQUESTED));
        result.put(DocumentStatus.APPROVED, Map.of("issue", DocumentStatus.ISSUED));
        result.put(DocumentStatus.ISSUED, Map.of("acknowledge", DocumentStatus.ACKNOWLEDGED));
        return Map.copyOf(result);
    }
}
