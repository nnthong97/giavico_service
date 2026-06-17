package com.giavico.beverage.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatMessageStoreRequest(
        @NotBlank @Pattern(regexp = "user|assistant") String role,
        @NotBlank @Size(max = 20000) String content
) {
}
