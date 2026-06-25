package com.giavico.rnd.api;

import com.giavico.rnd.domain.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;

public final class DocumentRequests {
    private DocumentRequests() {}
    public record Save(@NotNull DocumentType type, @NotBlank String title, @NotBlank String productName,
                       String formulaUuid, @NotBlank String market, @NotBlank String owner,
                       LocalDate effectiveDate, @NotNull Map<String, Object> fieldValues) {}
    public record Workflow(@NotBlank String actor, String comment, String role) {}
}
