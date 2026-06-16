package com.giavico.beverage.service;

import com.giavico.beverage.api.dto.ChatMessageResponse;
import com.giavico.beverage.api.dto.ChatMessageStoreRequest;
import com.giavico.beverage.persistence.entity.ChatMessageEntity;
import com.giavico.beverage.persistence.repository.ChatMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatPersistenceService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatPersistenceService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> list(Pageable pageable) {
        return chatMessageRepository.findAllByOrderByCreatedAtAsc(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ChatMessageResponse store(ChatMessageStoreRequest request) {
        ChatMessageEntity savedMessage = chatMessageRepository.save(new ChatMessageEntity(
                request.role(),
                request.content()
        ));

        return toResponse(savedMessage);
    }

    @Transactional
    public void clear() {
        chatMessageRepository.deleteAllInBatch();
    }

    private ChatMessageResponse toResponse(ChatMessageEntity entity) {
        return new ChatMessageResponse(
                entity.getId(),
                entity.getRole(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}
