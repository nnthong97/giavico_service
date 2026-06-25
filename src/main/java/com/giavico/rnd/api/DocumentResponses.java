package com.giavico.rnd.api;

import com.giavico.rnd.domain.DocumentStatus;
import com.giavico.rnd.domain.DocumentType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DocumentResponses {
    private DocumentResponses() {}
    public record Summary(UUID uuid, String documentNumber, DocumentType type, String title, String productName,
                          String formulaUuid, DocumentStatus status, int revision, String owner, Instant createdAt, Instant updatedAt) {}
    public record Approval(String action, String actor, String role, Integer revision, String comment, Instant createdAt) {}
    public record Revision(int revision, DocumentStatus status, String changedBy, String changeSummary, Instant createdAt) {}
    public record Detail(UUID uuid, String documentNumber, DocumentType type, String title, String productName,
                         String formulaUuid, DocumentStatus status, int revision, String owner, Instant createdAt,
                         Instant updatedAt, String market, LocalDate effectiveDate, Map<String, Object> fieldValues,
                         List<Approval> approvals, List<Revision> revisions) {}
}
