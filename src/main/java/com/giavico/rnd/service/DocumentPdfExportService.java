package com.giavico.rnd.service;

import com.giavico.rnd.api.DocumentResponses;
import com.giavico.rnd.domain.DocumentType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import java.util.Set;

@Service
public class DocumentPdfExportService {
    private static final ThreadLocal<PDFont> FONT = new ThreadLocal<>();
    private final DocumentTemplateCatalog templates;
    private final Map<DocumentType, LayoutRenderer> renderers;

    public DocumentPdfExportService(DocumentTemplateCatalog templates) {
        this.templates = templates;
        EnumMap<DocumentType, LayoutRenderer> layouts = new EnumMap<>(DocumentType.class);
        layouts.put(DocumentType.ENGINEERING_CHANGE_NOTICE, this::renderEngineeringChangeNotice);
        layouts.put(DocumentType.SAMPLE_REPORT, this::renderSampleReport);
        layouts.put(DocumentType.CHANGE_PROPOSAL, this::renderChangeProposal);
        layouts.put(DocumentType.ENGINEERING_CHANGE_REQUEST, this::renderEngineeringChangeRequest);
        layouts.put(DocumentType.SEMI_FINISHED_STANDARD_RECEIPT, this::renderReceipt);
        layouts.put(DocumentType.PRODUCT_SPECIFICATION_RECEIPT, this::renderReceipt);
        layouts.put(DocumentType.PRODUCT_CHANGE_NOTICE_RECEIPT, this::renderReceipt);
        layouts.put(DocumentType.MANUFACTURING_NOTICE_RECEIPT, this::renderReceipt);
        layouts.put(DocumentType.PRODUCT_CHANGE_NOTIFICATION, this::renderProductChangeNotification);
        layouts.put(DocumentType.MANUFACTURING_NOTICE, this::renderNewProduct);
        layouts.put(DocumentType.PRODUCT_SPECIFICATION, this::renderNewProduct);
        layouts.put(DocumentType.FINISHED_PRODUCT_ACCEPTANCE, this::renderAcceptance);
        layouts.put(DocumentType.SEMI_FINISHED_ACCEPTANCE, this::renderAcceptance);
        layouts.put(DocumentType.RAW_MATERIAL_ACCEPTANCE, this::renderAcceptance);
        this.renderers = Map.copyOf(layouts);
    }

    public byte[] render(DocumentResponses.Detail document) {
        if (!templates.find(document.type()).sourceAvailable()) {
            throw new PdfExportException("The source PDF for " + document.type() + " is unavailable or locked.");
        }
        String source = "document-templates/" + templates.sourceFile(document.type());
        try {
            byte[] sourcePdf = new ClassPathResource(source).getInputStream().readAllBytes();
            try (PDDocument pdf = Loader.loadPDF(sourcePdf); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                FONT.set(PDType0Font.load(pdf, new ClassPathResource("document-templates/NotoSansTC.ttf").getInputStream()));
                PDPage page = pdf.getPage(0);
                try (PDPageContentStream canvas = new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    LayoutRenderer renderer = renderers.get(document.type());
                    if (renderer == null) throw new PdfExportException("No PDF layout is configured for " + document.type() + ".");
                    renderer.render(canvas, document);
                }
                pdf.save(output);
                return output.toByteArray();
            } finally {
                FONT.remove();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate PDF for " + document.documentNumber(), exception);
        }
    }

    Set<DocumentType> layoutTypes() { return renderers.keySet(); }

    private void renderEngineeringChangeNotice(PDPageContentStream canvas, DocumentResponses.Detail document) throws IOException {
        Map<String, Object> values = document.fieldValues();
        text(canvas, value(values, "date"), 474, 731, 8);
        wrapped(canvas, value(values, "productName"), 110, 700, 445, 10, 2);
        wrapped(canvas, value(values, "changeItem"), 110, 667, 445, 10, 3);
        wrapped(canvas, value(values, "beforeChange"), 34, 610, 525, 10, 5);
        wrapped(canvas, value(values, "afterChange"), 34, 536, 525, 10, 5);
        wrapped(canvas, value(values, "process"), 151, 480, 404, 10, 4);
        wrapped(canvas, value(values, "notes"), 121, 399, 434, 9, 3);
        text(canvas, value(values, "implementationDate"), 160, 352, 8);

        markOne(canvas, value(values, "changeMethod"), Map.of("Normal", point(140, 320), "Phased", point(294, 320), "Temporary", point(430, 320)));
        markMany(canvas, values.get("changeFactors"), Map.of(
                "Safety", point(140, 291), "Raw material", point(325, 291), "Additive", point(140, 270),
                "Customer requirement", point(328, 270), "Inventory consumption", point(140, 249),
                "Specification / process change", point(140, 228), "Other", point(140, 207)));
        markMany(canvas, values.get("limitSamples"), Map.of(
                "Semi-finished product", point(140, 186), "Before sterilization", point(320, 186), "Finished product", point(140, 165)));
        renderApprovals(canvas, document, 29, 18, 536);
    }

    private void renderSampleReport(PDPageContentStream canvas, DocumentResponses.Detail document) throws IOException {
        Map<String, Object> values = document.fieldValues();
        text(canvas, value(values, "sampleCode"), 174, 774, 8);
        text(canvas, value(values, "productCode"), 86, 758, 7);
        text(canvas, value(values, "manufacturingDate"), 260, 758, 7);
        text(canvas, value(values, "storageCondition"), 460, 758, 7);
        text(canvas, value(values, "productName"), 86, 743, 7);
        text(canvas, value(values, "quantity"), 260, 743, 7);
        text(canvas, value(values, "packaging"), 420, 743, 7);

        List<Map<String, Object>> measurements = rows(values.get("processMeasurements"));
        requireCapacity(measurements, 15, "Sample Report process measurements");
        String[] measurementColumns = {"standard", "sample", "rawMaterial", "peeling", "crushing", "milling", "pulper", "pulperFinisher", "dkDs", "cleaning", "concentration"};
        float[] measurementX = {87, 134, 176, 220, 259, 305, 349, 390, 432, 473, 518};
        for (int row = 0; row < Math.min(15, measurements.size()); row++) {
            float y = 704 - row * 13.2f;
            for (int column = 0; column < measurementColumns.length; column++)
                text(canvas, value(measurements.get(row), measurementColumns[column]), measurementX[column], y, 5.2f);
        }

        List<Map<String, Object>> formula = rows(values.get("formula"));
        requireCapacity(formula, 7, "Sample Report formula");
        String[] formulaColumns = {"material", "brix", "brixPercent", "weightPercent"};
        float[] formulaX = {27, 236, 355, 497};
        for (int row = 0; row < Math.min(7, formula.size()); row++)
            for (int column = 0; column < formulaColumns.length; column++)
                text(canvas, value(formula.get(row), formulaColumns[column]), formulaX[column], 472 - row * 13, 6);

        wrapped(canvas, value(values, "flowChart"), 27, 368, 540, 9, 11);

        List<Map<String, Object>> dispatches = rows(values.get("sampleDispatches"));
        requireCapacity(dispatches, 5, "Sample Report dispatch");
        String[] dispatchColumns = {"orderNumber", "locationCustomer", "sendingDate", "quantity", "condition", "producer", "authorizedBy"};
        float[] dispatchX = {58, 116, 210, 292, 368, 446, 517};
        float[] dispatchWidths = {52, 88, 76, 70, 72, 66, 58};
        for (int row = 0; row < Math.min(5, dispatches.size()); row++) {
            float y = 215 - row * 15.2f;
            for (int column = 0; column < dispatchColumns.length; column++)
                fitted(canvas, value(dispatches.get(row), dispatchColumns[column]), dispatchX[column], y, dispatchWidths[column], 6);
        }
        wrapped(canvas, value(values, "notes"), 55, 137, 510, 8, 5);
        renderApprovals(canvas, document, 24, 52, 540);
    }

    private void renderChangeProposal(PDPageContentStream canvas, DocumentResponses.Detail document) throws IOException {
        Map<String, Object> values = document.fieldValues();
        text(canvas, value(values, "date"), 475, 716, 8);
        text(canvas, value(values, "productCode"), 120, 696, 8);
        markMany(canvas, values.get("changeItems"), Map.of("Specification", point(190, 678), "Formula", point(320, 678), "Process", point(448, 678)));
        wrapped(canvas, value(values, "currentStandard"), 160, 615, 420, 9, 10);
        wrapped(canvas, value(values, "proposedChange"), 160, 454, 420, 9, 10);
        text(canvas, value(values, "startDate"), 160, 303, 8);
        wrapped(canvas, value(values, "reason"), 160, 266, 420, 8, 4);
        wrapped(canvas, value(values, "notes"), 160, 211, 420, 8, 4);
        renderApprovals(canvas, document, 22, 41, 568);
    }

    private void renderEngineeringChangeRequest(PDPageContentStream canvas, DocumentResponses.Detail document) throws IOException {
        Map<String, Object> values = document.fieldValues();
        text(canvas, value(values, "documentNumber"), 120, 744, 7);
        text(canvas, value(values, "notificationDate"), 430, 744, 7);
        text(canvas, value(values, "receivedDate"), 430, 726, 7);
        text(canvas, value(values, "productCode"), 120, 708, 7);
        markOne(canvas, value(values, "urgency"), Map.of("Urgent", point(405, 708), "Normal", point(475, 708)));
        text(canvas, value(values, "packaging"), 120, 687, 7);
        markOne(canvas, value(values, "importance"), Map.of("A", point(428, 687), "B", point(480, 687)));
        text(canvas, value(values, "quantity"), 120, 668, 7);
        text(canvas, value(values, "referenceCost"), 305, 668, 7);
        text(canvas, value(values, "estimatedAnnualQuantity"), 475, 668, 7);
        text(canvas, value(values, "completionDate"), 475, 647, 7);
        text(canvas, value(values, "brix"), 110, 615, 7); text(canvas, value(values, "acid"), 110, 590, 7); text(canvas, value(values, "ph"), 110, 565, 7);
        markManyGrid(canvas, values.get("businessReasons"), 360, 610, 2, 18);
        markManyGrid(canvas, values.get("planningReasons"), 360, 538, 2, 18);
        wrapped(canvas, value(values, "otherReason"), 200, 481, 360, 7, 4);
        wrapped(canvas, value(values, "existingAnalysis"), 200, 418, 360, 7, 5);
        wrapped(canvas, value(values, "recommendedMaterials"), 200, 342, 360, 7, 5);
        markManyGrid(canvas, values.get("requiredResults"), 210, 270, 2, 18);
        wrapped(canvas, value(values, "notes"), 190, 179, 370, 7, 4);
        renderApprovals(canvas, document, 25, 35, 540);
    }

    private void renderReceipt(PDPageContentStream canvas, DocumentResponses.Detail document) throws IOException {
        List<Map<String, Object>> entries = rows(document.fieldValues().get("entries"));
        int maxRows = 16;
        if (entries.size() > maxRows) throw new PdfExportException("Receipt template supports at most " + maxRows + " rows.");
        var field = templates.find(document.type()).fields().get(0);
        int columns = field.columns().size();
        float left = 24, width = 744, columnWidth = width / columns;
        for (int row = 0; row < entries.size(); row++) {
            float y = 448 - row * 22.4f;
            for (int column = 0; column < columns; column++)
                fitted(canvas, value(entries.get(row), field.columns().get(column).key()), left + column * columnWidth + 3, y, columnWidth - 6, 6);
        }
    }

    private void renderProductChangeNotification(PDPageContentStream canvas, DocumentResponses.Detail document) throws IOException {
        Map<String, Object> values = document.fieldValues();
        text(canvas, value(values, "revision"), 120, 742, 7); text(canvas, value(values, "date"), 470, 742, 7);
        text(canvas, value(values, "productCode"), 120, 718, 8);
        markMany(canvas, values.get("changeItems"), Map.of("Specification", point(190, 690), "Formula", point(320, 690), "Process", point(450, 690)));
        wrapped(canvas, value(values, "beforeChange"), 170, 620, 390, 9, 11);
        wrapped(canvas, value(values, "afterChange"), 170, 440, 390, 9, 11);
        wrapped(canvas, value(values, "notes"), 170, 235, 390, 8, 5);
        renderApprovals(canvas, document, 25, 42, 540);
    }

    private void renderNewProduct(PDPageContentStream canvas, DocumentResponses.Detail document) throws IOException {
        Map<String, Object> values = document.fieldValues();
        text(canvas, value(values, "revision"), 95, 742, 7); text(canvas, value(values, "date"), 460, 742, 7);
        text(canvas, value(values, "vnProductCode"), 120, 721, 7); text(canvas, value(values, "taiwanProductCode"), 310, 721, 7);
        text(canvas, value(values, "productName"), 120, 700, 7); text(canvas, value(values, "packaging"), 430, 700, 7);
        fitted(canvas, value(values, "storageAndShelfLife"), 120, 680, 230, 7); text(canvas, value(values, "weight"), 430, 680, 7);
        String[] specs = {"brix", "acid", "ph", "an", "solid", "cps", "ash", "brixAcidRatio", "tpc", "yeastMold", "coliform", "eColi"};
        for (int i = 0; i < specs.length; i++) text(canvas, value(values, specs[i]), 145 + (i % 3) * 155, 642 - (i / 3) * 24, 6.5f);
        wrapped(canvas, value(values, "rawMaterials"), 120, 520, 440, 8, 5);
        if (document.type() == DocumentType.MANUFACTURING_NOTICE) {
            wrapped(canvas, value(values, "additives"), 120, 455, 440, 8, 4);
            wrapped(canvas, value(values, "formula"), 120, 398, 440, 8, 5);
            wrapped(canvas, value(values, "process"), 120, 325, 440, 8, 8);
            wrapped(canvas, value(values, "notes"), 120, 205, 440, 8, 5);
        } else {
            wrapped(canvas, value(values, "process"), 120, 430, 440, 8, 10);
            wrapped(canvas, value(values, "notes"), 120, 265, 440, 8, 6);
        }
        renderApprovals(canvas, document, 25, 34, 540);
    }

    private void renderAcceptance(PDPageContentStream canvas, DocumentResponses.Detail document) throws IOException {
        Map<String, Object> values = document.fieldValues();
        text(canvas, value(values, "revision"), 95, 742, 7); text(canvas, value(values, "date"), 460, 742, 7);
        if (document.type() == DocumentType.FINISHED_PRODUCT_ACCEPTANCE) {
            text(canvas, value(values, "productCode"), 120, 712, 7); text(canvas, value(values, "productName"), 330, 712, 7);
            String[] specs = {"brix", "acid", "ph", "an", "solid", "brixAcidRatio", "ash", "cps", "impurities", "tpc", "yeastMold", "coliform"};
            for (int i = 0; i < specs.length; i++) fitted(canvas, value(values, specs[i]), 360, 663 - i * 26.5f, 150, 7);
            fitted(canvas, value(values, "storageCondition"), 360, 334, 150, 7);
            fitted(canvas, value(values, "shelfLife"), 360, 307, 150, 7);
            fitted(canvas, value(values, "weightPackaging"), 360, 280, 150, 7);
        } else {
            text(canvas, value(values, "materialName"), 120, 712, 7); text(canvas, value(values, "variety"), 330, 712, 7);
            text(canvas, value(values, "materialCode"), 120, 690, 7);
            if (document.type() == DocumentType.SEMI_FINISHED_ACCEPTANCE) markOne(canvas, value(values, "origin"), Map.of("Domestic", point(350, 690), "Imported", point(450, 690)));
            renderRequirementRows(canvas, values.get("acceptanceRequirements"));
            wrapped(canvas, value(values, "heavyMetalLimits"), 180, 395, 370, 7, 6);
            wrapped(canvas, value(values, "usageSpecification"), 180, 290, 370, 7, 6);
            wrapped(canvas, value(values, "pesticideResidue"), 180, 190, 370, 7, 5);
        }
        renderApprovals(canvas, document, 25, 32, 540);
    }

    private void renderRequirementRows(PDPageContentStream canvas, Object value) throws IOException {
        List<Map<String, Object>> rows = rows(value); if (rows.size() > 5) throw new PdfExportException("Acceptance template supports at most 5 requirement rows.");
        String[] keys = {"item", "requirement", "notes", "inspectionMethod"}; float[] x = {40, 145, 330, 455}; float[] widths = {95, 175, 115, 100};
        for (int row = 0; row < rows.size(); row++) for (int column = 0; column < keys.length; column++)
            fitted(canvas, value(rows.get(row), keys[column]), x[column], 618 - row * 47, widths[column], 6.5f);
    }

    private void renderApprovals(PDPageContentStream canvas, DocumentResponses.Detail document, float left, float y, float width) throws IOException {
        List<String> roles = templates.find(document.type()).approvals().stream().map(role -> role.en()).toList(); if (roles.isEmpty()) return;
        float cellWidth = width / roles.size();
        for (int index = 0; index < roles.size(); index++) {
            String role = roles.get(index);
            var approval = document.approvals().stream().filter(item -> "approve".equals(item.action()) && role.equals(item.role()) &&
                    Integer.valueOf(document.revision()).equals(item.revision())).findFirst();
            if (approval.isEmpty()) continue;
            String date = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault()).format(approval.get().createdAt());
            fitted(canvas, approval.get().actor(), left + index * cellWidth + 4, y + 12, cellWidth - 8, 7);
            fitted(canvas, date, left + index * cellWidth + 4, y + 3, cellWidth - 8, 6);
        }
    }

    private void markManyGrid(PDPageContentStream canvas, Object selected, float x, float y, int columns, float rowHeight) throws IOException {
        if (!(selected instanceof Collection<?> values)) return; int index = 0;
        for (Object ignored : values) { text(canvas, "X", x + (index % columns) * 145, y - (index / columns) * rowHeight, 8); index++; }
    }

    private void text(PDPageContentStream canvas, String value, float x, float y, float size) throws IOException {
        if (value.isBlank()) return;
        canvas.beginText(); canvas.setFont(font(), size); canvas.newLineAtOffset(x, y); canvas.showText(value); canvas.endText();
    }

    private void fitted(PDPageContentStream canvas, String value, float x, float y, float width, float maxSize) throws IOException {
        float size = maxSize;
        String safe = pdfText(value);
        while (size > 4 && font().getStringWidth(safe) / 1000 * size > width) size -= 0.5f;
        text(canvas, ellipsize(safe, width, size), x, y, size);
    }

    private void wrapped(PDPageContentStream canvas, String value, float x, float y, float width, float size, int maxLines) throws IOException {
        if (value.isBlank()) return;
        String[] words = pdfText(value).replace('\n', ' ').split("\\s+");
        StringBuilder line = new StringBuilder();
        int count = 0;
        for (int index = 0; index < words.length; index++) {
            String word = words[index];
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && font().getStringWidth(candidate) / 1000 * size > width) {
                boolean finalLine = count == maxLines - 1;
                text(canvas, finalLine ? ellipsize(line + " " + String.join(" ", java.util.Arrays.copyOfRange(words, index, words.length)), width, size)
                        : line.toString(), x, y - count++ * (size + 2), size);
                line = new StringBuilder(word);
                if (finalLine) return;
            } else line = new StringBuilder(candidate);
        }
        if (!line.isEmpty() && count < maxLines) text(canvas, ellipsize(line.toString(), width, size), x, y - count * (size + 2), size);
    }

    private void markOne(PDPageContentStream canvas, String selected, Map<String, float[]> points) throws IOException {
        float[] point = points.get(selected); if (point != null) text(canvas, "X", point[0], point[1], 9);
    }
    private void markMany(PDPageContentStream canvas, Object selected, Map<String, float[]> points) throws IOException {
        if (!(selected instanceof Collection<?> values)) return;
        for (Object value : values) markOne(canvas, String.valueOf(value), points);
    }
    private static float[] point(float x, float y) { return new float[]{x, y}; }
    private static String value(Map<String, ?> values, String key) { Object value = values.get(key); return value == null ? "" : String.valueOf(value); }
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> rows(Object value) { return value instanceof List<?> list ? (List<Map<String, Object>>) (List<?>) list : List.of(); }
    private static void requireCapacity(List<?> rows, int maximum, String label) {
        if (rows.size() > maximum) throw new PdfExportException(label + " supports at most " + maximum + " rows.");
    }
    private static String ellipsize(String value, float width, float size) throws IOException {
        if (font().getStringWidth(value) / 1000 * size <= width) return value;
        String suffix = "...";
        int end = value.length();
        while (end > 0 && font().getStringWidth(value.substring(0, end).stripTrailing() + suffix) / 1000 * size > width) end--;
        return value.substring(0, end).stripTrailing() + suffix;
    }
    private static String pdfText(String value) { return value; }
    private static PDFont font() { return FONT.get(); }

    @FunctionalInterface
    private interface LayoutRenderer {
        void render(PDPageContentStream canvas, DocumentResponses.Detail document) throws IOException;
    }
}
