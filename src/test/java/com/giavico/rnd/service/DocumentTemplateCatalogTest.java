package com.giavico.rnd.service;

import com.giavico.rnd.domain.DocumentType;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.EnumSet;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTemplateCatalogTest {
    private final DocumentTemplateCatalog catalog = new DocumentTemplateCatalog();

    @Test
    void exposesOneTemplateAndBundledSourceForEveryDocumentType() {
        assertThat(catalog.findAll()).hasSize(DocumentType.values().length);
        assertThat(catalog.findAll()).extracting(template -> template.type())
                .containsExactlyInAnyOrderElementsOf(EnumSet.allOf(DocumentType.class));

        for (DocumentType type : DocumentType.values()) {
            var template = catalog.find(type);
            assertThat(template.formNumber()).isEqualTo(type.formNumber());
            assertThat(template.name().en()).isNotBlank();
            assertThat(template.name().vi()).isNotBlank();
            assertThat(template.name().zhTw()).isNotBlank();
            assertThat(new ClassPathResource("document-templates/" + catalog.sourceFile(type)).exists()).isTrue();
            assertThat(template.fields()).extracting(field -> field.key()).doesNotHaveDuplicates();
            assertThat(template.fields()).allSatisfy(field -> {
                assertThat(field.label().en()).isNotBlank();
                assertThat(field.label().vi()).isNotBlank();
                assertThat(field.label().zhTw()).isNotBlank();
            });
        }
    }

    @Test
    void allTemplateTypesAreUnique() {
        var types = new HashSet<>(catalog.findAll().stream().map(template -> template.type()).toList());
        assertThat(types).hasSize(catalog.findAll().size());
    }
}
