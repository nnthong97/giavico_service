package com.giavico.beverage.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        String role,
        String content,
        Instant createdAt
) {
}
