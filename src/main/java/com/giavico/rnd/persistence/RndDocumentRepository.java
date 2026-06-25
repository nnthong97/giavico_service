package com.giavico.rnd.persistence;

import com.giavico.rnd.domain.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.UUID;

public interface RndDocumentRepository extends JpaRepository<RndDocumentEntity, UUID>, JpaSpecificationExecutor<RndDocumentEntity> {
    long countByTypeAndCreatedAtBetween(com.giavico.rnd.domain.DocumentType type, java.time.Instant start, java.time.Instant end);
    Page<RndDocumentEntity> findByStatus(DocumentStatus status, Pageable pageable);
}
