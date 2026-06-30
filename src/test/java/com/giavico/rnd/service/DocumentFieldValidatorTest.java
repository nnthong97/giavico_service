package com.giavico.rnd.service;

import com.giavico.rnd.domain.DocumentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentFieldValidatorTest {
    private final DocumentFieldValidator validator = new DocumentFieldValidator(new DocumentTemplateCatalog());

    @Test
    void rejectsMissingRequiredTemplateFields() {
        assertThatThrownBy(() -> validator.validate(DocumentType.SAMPLE_REPORT, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sampleCode");
    }

    @Test
    void rejectsLockedTemplateUntilUnlockedSourceIsProvided() {
        assertThatThrownBy(() -> validator.validate(DocumentType.PRODUCT_CONFIRMATION, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unlocked source file");
    }
}
