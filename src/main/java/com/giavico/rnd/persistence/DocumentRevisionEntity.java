package com.giavico.rnd.persistence;

import com.giavico.rnd.domain.DocumentStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "rnd_document_revisions")
public class DocumentRevisionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "document_id") private RndDocumentEntity document;
    @Column(nullable = false) private int revision;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private DocumentStatus status;
    @Column(name = "changed_by", nullable = false, length = 160) private String changedBy;
    @Column(name = "change_summary", nullable = false, length = 1000) private String changeSummary;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected DocumentRevisionEntity() {}
    DocumentRevisionEntity(RndDocumentEntity document, int revision, DocumentStatus status, String changedBy, String changeSummary) {
        this.document = document; this.revision = revision; this.status = status; this.changedBy = changedBy; this.changeSummary = changeSummary; this.createdAt = Instant.now();
    }
    public int getRevision() { return revision; } public DocumentStatus getStatus() { return status; }
    public String getChangedBy() { return changedBy; } public String getChangeSummary() { return changeSummary; }
    public Instant getCreatedAt() { return createdAt; }
}
