package com.giavico.rnd.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giavico.rnd.api.DocumentRequests;
import com.giavico.rnd.domain.DocumentStatus;
import com.giavico.rnd.domain.DocumentType;
import com.giavico.rnd.persistence.RndDocumentEntity;
import com.giavico.rnd.persistence.RndDocumentRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RndDocumentManagementServiceTest {
    private final DocumentTemplateCatalog catalog = new DocumentTemplateCatalog();
    private final RndDocumentRepository repository = mock(RndDocumentRepository.class);
    private final RndDocumentManagementService service = new RndDocumentManagementService(repository, new ObjectMapper(),
            new DocumentFieldValidator(catalog), catalog);

    @Test
    void remainsUnderReviewUntilEveryRoleApprovesAndRejectsDuplicates() {
        RndDocumentEntity entity = entityUnderReview();
        when(repository.findById(any())).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var roles = catalog.find(DocumentType.SAMPLE_REPORT).approvals();

        service.transition(UUID.randomUUID(), "approve", new DocumentRequests.Workflow("Alice", "", roles.get(0).en()));
        assertThat(entity.getStatus()).isEqualTo(DocumentStatus.UNDER_REVIEW);
        assertThatThrownBy(() -> service.transition(UUID.randomUUID(), "approve", new DocumentRequests.Workflow("Other", "", roles.get(0).en())))
                .isInstanceOf(InvalidWorkflowException.class).hasMessageContaining("already approved");
        service.transition(UUID.randomUUID(), "approve", new DocumentRequests.Workflow("Bob", "", roles.get(1).en()));
        service.transition(UUID.randomUUID(), "approve", new DocumentRequests.Workflow("Carol", "", roles.get(2).en()));

        assertThat(entity.getStatus()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(entity.getApprovals()).allSatisfy(approval -> assertThat(approval.getRevision()).isEqualTo(1));
    }

    @Test
    void requiresAConfiguredApprovalRole() {
        RndDocumentEntity entity = entityUnderReview();
        when(repository.findById(any())).thenReturn(Optional.of(entity));
        assertThatThrownBy(() -> service.transition(UUID.randomUUID(), "approve", new DocumentRequests.Workflow("Alice", "", "Unknown")))
                .isInstanceOf(InvalidWorkflowException.class).hasMessageContaining("role is required");
    }

    private static RndDocumentEntity entityUnderReview() {
        var request = new DocumentRequests.Save(DocumentType.SAMPLE_REPORT, "Title", "Product", null, "Vietnam", "Owner", null,
                Map.of("sampleCode", "S-1", "productCode", "P-1", "productName", "Product", "flowChart", "Mix"));
        RndDocumentEntity entity = new RndDocumentEntity("P-RS1 003-01.03-2026-0001", request, "{}");
        entity.transition(DocumentStatus.SUBMITTED);
        entity.transition(DocumentStatus.UNDER_REVIEW);
        return entity;
    }
}
