package com.giavico.rnd.persistence;

import com.giavico.rnd.api.DocumentRequests;
import com.giavico.rnd.domain.DocumentStatus;
import com.giavico.rnd.domain.DocumentType;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rnd_documents", indexes = {@Index(name = "idx_rnd_document_status", columnList = "status"), @Index(name = "idx_rnd_document_formula", columnList = "formula_uuid")})
public class RndDocumentEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "document_number", nullable = false, unique = true, length = 80) private String documentNumber;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50) private DocumentType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private DocumentStatus status;
    @Column(nullable = false, length = 240) private String title;
    @Column(name = "product_name", nullable = false, length = 180) private String productName;
    @Column(name = "formula_uuid", length = 80) private String formulaUuid;
    @Column(nullable = false, length = 160) private String market;
    @Column(nullable = false, length = 160) private String owner;
    @Column(name = "effective_date") private LocalDate effectiveDate;
    @Lob @Column(name = "field_values", nullable = false, columnDefinition = "LONGTEXT") private String fieldValues;
    @Column(nullable = false) private int revision;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true) @OrderBy("createdAt DESC") private List<DocumentApprovalEntity> approvals = new ArrayList<>();
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true) @OrderBy("revision DESC") private List<DocumentRevisionEntity> revisions = new ArrayList<>();

    protected RndDocumentEntity() {}
    public RndDocumentEntity(String documentNumber, DocumentRequests.Save request, String fieldValues) {
        this.documentNumber = documentNumber; this.status = DocumentStatus.DRAFT; this.revision = 1;
        apply(request, fieldValues); this.createdAt = Instant.now(); this.updatedAt = createdAt;
    }
    public void apply(DocumentRequests.Save request, String fieldValues) {
        type = request.type(); title = request.title().trim(); productName = request.productName().trim();
        formulaUuid = blankToNull(request.formulaUuid()); market = request.market().trim(); owner = request.owner().trim();
        effectiveDate = request.effectiveDate(); this.fieldValues = fieldValues; updatedAt = Instant.now();
    }
    public void transition(DocumentStatus nextStatus) { status = nextStatus; updatedAt = Instant.now(); }
    public void incrementRevision() { revision++; }
    public void addApproval(String action, String actor, String comment) { approvals.add(new DocumentApprovalEntity(this, action, actor, comment)); }
    public void addRevision(String changedBy, String summary) { revisions.add(new DocumentRevisionEntity(this, revision, status, changedBy, summary)); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    public UUID getId() { return id; } public String getDocumentNumber() { return documentNumber; }
    public DocumentType getType() { return type; } public DocumentStatus getStatus() { return status; }
    public String getTitle() { return title; } public String getProductName() { return productName; }
    public String getFormulaUuid() { return formulaUuid; } public String getMarket() { return market; }
    public String getOwner() { return owner; } public LocalDate getEffectiveDate() { return effectiveDate; }
    public String getFieldValues() { return fieldValues; } public int getRevision() { return revision; }
    public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
    public List<DocumentApprovalEntity> getApprovals() { return approvals; } public List<DocumentRevisionEntity> getRevisions() { return revisions; }
}
