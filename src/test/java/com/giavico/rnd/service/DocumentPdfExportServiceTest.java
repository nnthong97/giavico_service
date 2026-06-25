package com.giavico.rnd.service;

import com.giavico.rnd.api.DocumentResponses;
import com.giavico.rnd.domain.DocumentStatus;
import com.giavico.rnd.domain.DocumentType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.time.Instant;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentPdfExportServiceTest {
    private final DocumentTemplateCatalog catalog = new DocumentTemplateCatalog();
    private final DocumentPdfExportService exporter = new DocumentPdfExportService(catalog);

    @Test
    void registryCoversEveryAvailableSourceTemplate() {
        assertThat(exporter.layoutTypes()).containsExactlyInAnyOrderElementsOf(
                catalog.findAll().stream().filter(template -> template.sourceAvailable()).map(template -> template.type()).toList());
    }

    @Test
    void exportsEveryAvailableTemplateAndPreservesItsPageDimensions() throws Exception {
        Path fixtures = Files.createDirectories(Path.of("target", "test-pdf-fixtures"));
        for (DocumentType type : EnumSet.allOf(DocumentType.class)) {
            if (!catalog.find(type).sourceAvailable()) continue;
            byte[] output = exporter.render(detail(type, representativeValues(type)));
            Files.write(fixtures.resolve(type.name().toLowerCase() + ".pdf"), output);
            byte[] source = new ClassPathResource("document-templates/" + catalog.sourceFile(type)).getInputStream().readAllBytes();
            try (var sourcePdf = Loader.loadPDF(source); var outputPdf = Loader.loadPDF(output)) {
                assertThat(output).startsWith("%PDF".getBytes());
                assertThat(outputPdf.getNumberOfPages()).isEqualTo(sourcePdf.getNumberOfPages());
                assertThat(outputPdf.getPage(0).getMediaBox().getWidth()).isEqualTo(sourcePdf.getPage(0).getMediaBox().getWidth());
                assertThat(outputPdf.getPage(0).getMediaBox().getHeight()).isEqualTo(sourcePdf.getPage(0).getMediaBox().getHeight());
            }
        }
    }

    @Test
    void embedsVietnameseTraditionalChineseAndCurrentRevisionSignature() throws Exception {
        String unicode = "Tiếng Việt 中文";
        DocumentResponses.Detail document = detail(DocumentType.ENGINEERING_CHANGE_NOTICE, Map.of("productName", unicode));
        var role = catalog.find(DocumentType.ENGINEERING_CHANGE_NOTICE).approvals().get(0).en();
        document = new DocumentResponses.Detail(document.uuid(), document.documentNumber(), document.type(), document.title(), document.productName(),
                document.formulaUuid(), document.status(), document.revision(), document.owner(), document.createdAt(), document.updatedAt(), document.market(),
                document.effectiveDate(), document.fieldValues(), List.of(new DocumentResponses.Approval("approve", unicode, role, 1, "", Instant.parse("2026-06-19T00:00:00Z"))), List.of());

        try (var pdf = Loader.loadPDF(exporter.render(document))) {
            String text = new PDFTextStripper().getText(pdf);
            // PDFBox normalizes some CJK glyphs to their compatibility code points during extraction.
            assertThat(text).contains("Tiếng Việt").contains("中").contains("2026-06-19");
        }
    }

    @Test
    void rejectsRowsBeyondThePhysicalTemplateCapacity() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int index = 0; index < 17; index++) rows.add(Map.of("date", "2026-06-19"));
        assertThatThrownBy(() -> exporter.render(detail(DocumentType.PRODUCT_SPECIFICATION_RECEIPT, Map.of("entries", rows))))
                .isInstanceOf(PdfExportException.class).hasMessageContaining("at most 16 rows");
    }

    @Test
    void rejectsLockedProductConfirmationSource() {
        assertThatThrownBy(() -> exporter.render(detail(DocumentType.PRODUCT_CONFIRMATION, Map.of())))
                .isInstanceOf(PdfExportException.class).hasMessageContaining("unavailable or locked");
    }

    private static DocumentResponses.Detail detail(DocumentType type, Map<String, Object> fields) {
        Instant now = Instant.parse("2026-06-19T00:00:00Z");
        return new DocumentResponses.Detail(UUID.randomUUID(), type.formNumber() + "-2026-0001", type, "Title", "Product", null,
                DocumentStatus.UNDER_REVIEW, 1, "R&D User", now, now, "Vietnam", null, fields, List.of(), List.of());
    }

    private Map<String, Object> representativeValues(DocumentType type) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (var field : catalog.find(type).fields()) {
            if ("table".equals(field.type())) {
                Map<String, Object> row = new LinkedHashMap<>();
                field.columns().forEach(column -> row.put(column.key(), "Mẫu 中文"));
                values.put(field.key(), List.of(row));
            } else if ("checkbox-group".equals(field.type())) {
                values.put(field.key(), field.options().stream().limit(2).map(option -> option.en()).toList());
            } else if ("select".equals(field.type()) && !field.options().isEmpty()) {
                values.put(field.key(), field.options().get(0).en());
            } else {
                values.put(field.key(), "Mẫu Tiếng Việt 中文");
            }
        }
        return values;
    }
}
