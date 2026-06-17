package com.giavico.beverage.persistence.repository;

import com.giavico.beverage.persistence.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    Page<ChatMessageEntity> findAllByOrderByCreatedAtAsc(Pageable pageable);
}
