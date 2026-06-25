package com.giavico.rnd.api;

import com.giavico.rnd.domain.DocumentType;

import java.util.List;

public final class DocumentTemplateResponses {
    private DocumentTemplateResponses() {}

    public record Text(String en, String vi, String zhTw) {}

    public record Field(
            String key,
            Text label,
            String type,
            boolean required,
            String section,
            List<Text> options,
            List<Field> columns
    ) {}

    public record Template(
            DocumentType type,
            String formNumber,
            Text name,
            String category,
            boolean sourceAvailable,
            String sourceUrl,
            List<Field> fields,
            List<Text> approvals
    ) {}
}
