package com.giavico.beverage.persistence.repository;

import com.giavico.beverage.persistence.entity.FormulaSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FormulaSessionRepository extends JpaRepository<FormulaSessionEntity, UUID> {

    Page<FormulaSessionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
