package com.giavico.rnd.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "rnd_document_approvals")
public class DocumentApprovalEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "document_id") private RndDocumentEntity document;
    @Column(nullable = false, length = 60) private String action;
    @Column(nullable = false, length = 160) private String actor;
    @Column(length = 160) private String role;
    private Integer revision;
    @Column(length = 1000) private String comment;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected DocumentApprovalEntity() {}
    DocumentApprovalEntity(RndDocumentEntity document, String action, String actor, String role, int revision, String comment) {
        this.document = document; this.action = action; this.actor = actor.trim(); this.role = blankToNull(role); this.revision = revision;
        this.comment = comment == null ? "" : comment.trim(); this.createdAt = Instant.now();
    }
    public String getAction() { return action; } public String getActor() { return actor; }
    public String getRole() { return role; } public Integer getRevision() { return revision; }
    public String getComment() { return comment; } public Instant getCreatedAt() { return createdAt; }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
