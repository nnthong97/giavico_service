package com.giavico.process.persistence;

import com.giavico.process.domain.ProcessRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ProcessRunRepository extends JpaRepository<ProcessRunEntity, UUID>, JpaSpecificationExecutor<ProcessRunEntity> {
    Optional<ProcessRunEntity> findFirstByWorkflowIdAndStatusInOrderByUpdatedAtDesc(
            String workflowId,
            Collection<ProcessRunStatus> statuses
    );
}
