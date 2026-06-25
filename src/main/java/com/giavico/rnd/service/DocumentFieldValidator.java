package com.giavico.rnd.service;

import com.giavico.rnd.api.DocumentTemplateResponses.Field;
import com.giavico.rnd.domain.DocumentType;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class DocumentFieldValidator {
    private final DocumentTemplateCatalog catalog;

    public DocumentFieldValidator(DocumentTemplateCatalog catalog) {
        this.catalog = catalog;
    }

    public void validate(DocumentType type, Map<String, Object> values) {
        var template = catalog.find(type);
        if (!template.sourceAvailable()) {
            throw new IllegalArgumentException("Template %s requires an unlocked source PDF before documents can be created.".formatted(template.formNumber()));
        }
        for (Field field : template.fields()) {
            if (field.required() && isEmpty(values.get(field.key()))) {
                throw new IllegalArgumentException("Required template field is missing: " + field.key());
            }
        }
    }

    private boolean isEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String text) return text.isBlank();
        if (value instanceof Collection<?> collection) return collection.isEmpty();
        if (value instanceof Map<?, ?> map) return map.isEmpty();
        return false;
    }
}
