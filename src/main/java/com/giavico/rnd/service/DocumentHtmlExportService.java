package com.giavico.rnd.service;

import com.giavico.rnd.api.DocumentResponses;
import com.giavico.rnd.api.DocumentTemplateResponses.Field;
import com.giavico.rnd.api.DocumentTemplateResponses.Template;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class DocumentHtmlExportService {
    private final DocumentTemplateCatalog catalog;

    public DocumentHtmlExportService(DocumentTemplateCatalog catalog) {
        this.catalog = catalog;
    }

    public String render(DocumentResponses.Detail document) {
        Template template = catalog.find(document.type());
        StringBuilder body = new StringBuilder();
        String currentSection = null;
        for (Field field : template.fields()) {
            if (!field.section().equals(currentSection)) {
                if (currentSection != null) body.append("</section>");
                currentSection = field.section();
                body.append("<section><h2>").append(title(currentSection)).append("</h2>");
            }
            body.append(renderField(field, document.fieldValues().get(field.key())));
        }
        if (currentSection != null) body.append("</section>");
        return """
                <!doctype html><html lang="en"><head><meta charset="UTF-8"><title>%s</title>
                <style>
                @page{size:A4;margin:12mm}*{box-sizing:border-box}body{font-family:Arial,"Noto Sans",sans-serif;color:#172c27;font-size:11px;margin:0}
                header{text-align:center;border-bottom:2px solid #173f35;padding-bottom:10px;margin-bottom:12px}header small{display:block;color:#557069;margin:3px 0}h1{font-size:20px;margin:4px 0;letter-spacing:0}h2{font-size:12px;text-transform:uppercase;border-bottom:1px solid #9dafaa;padding-bottom:4px;margin:14px 0 8px}
                .meta,.fields{display:grid;grid-template-columns:1fr 1fr;gap:7px 12px}.meta{border:1px solid #9dafaa;padding:8px}.field{break-inside:avoid;margin-bottom:8px}.field label{display:block;font-weight:700}.field label small{display:block;font-weight:400;color:#60756f}.value{min-height:24px;border-bottom:1px solid #aab8b4;padding:5px 2px;white-space:pre-wrap}.wide{grid-column:1/-1}
                table{width:100%%;border-collapse:collapse;margin:6px 0 10px;font-size:9px}th,td{border:1px solid #7f918c;padding:4px;vertical-align:top}th small{display:block;font-weight:400}footer{margin-top:18px;border-top:1px solid #9dafaa;padding-top:7px;color:#60756f;display:flex;justify-content:space-between}
                @media print{button{display:none}}
                </style></head><body>
                <header><strong>GIAVICO INTERNATIONAL FOOD COMPANY LTD</strong><small>%s</small><h1>%s</h1><small>%s · %s</small></header>
                <div class="meta"><div><b>Document number</b><br>%s</div><div><b>Status / Revision</b><br>%s / %d</div><div><b>Product</b><br>%s</div><div><b>Market</b><br>%s</div><div><b>Owner</b><br>%s</div><div><b>Effective date</b><br>%s</div></div>
                %s
                <footer><span>Created: %s</span><span>Updated: %s</span></footer>
                <script>window.addEventListener('load',()=>window.print())</script></body></html>
                """.formatted(
                escape(document.title()), escape(template.formNumber()), escape(template.name().en()), escape(template.name().vi()), escape(template.name().zhTw()),
                escape(document.documentNumber()), escape(document.status().name()), document.revision(), escape(document.productName()), escape(document.market()),
                escape(document.owner()), document.effectiveDate() == null ? "" : escape(document.effectiveDate().toString()), body,
                escape(document.createdAt().toString()), escape(document.updatedAt().toString()));
    }

    private String renderField(Field field, Object value) {
        if ("table".equals(field.type())) return renderTable(field, value);
        return "<div class=\"field %s\"><label>%s<small>%s · %s</small></label><div class=\"value\">%s</div></div>".formatted(
                ("textarea".equals(field.type()) || "checkbox-group".equals(field.type())) ? "wide" : "",
                escape(field.label().en()), escape(field.label().vi()), escape(field.label().zhTw()), display(value));
    }

    private String renderTable(Field field, Object value) {
        StringBuilder html = new StringBuilder("<div class=\"field wide\"><label>")
                .append(escape(field.label().en())).append("<small>").append(escape(field.label().vi())).append(" · ").append(escape(field.label().zhTw())).append("</small></label><table><thead><tr>");
        for (Field column : field.columns()) html.append("<th>").append(escape(column.label().en())).append("<small>").append(escape(column.label().vi())).append(" · ").append(escape(column.label().zhTw())).append("</small></th>");
        html.append("</tr></thead><tbody>");
        if (value instanceof List<?> rows) {
            for (Object row : rows) {
                html.append("<tr>");
                Map<?, ?> values = row instanceof Map<?, ?> map ? map : Map.of();
                for (Field column : field.columns()) html.append("<td>").append(display(values.get(column.key()))).append("</td>");
                html.append("</tr>");
            }
        }
        return html.append("</tbody></table></div>").toString();
    }

    private String display(Object value) {
        if (value == null) return "";
        if (value instanceof Collection<?> values) return values.stream().map(String::valueOf).map(DocumentHtmlExportService::escape).reduce((a, b) -> a + ", " + b).orElse("");
        if (value instanceof Map<?, ?> values) return values.entrySet().stream().map(entry -> escape(String.valueOf(entry.getKey())) + ": " + escape(String.valueOf(entry.getValue()))).reduce((a, b) -> a + "<br>" + b).orElse("");
        return escape(String.valueOf(value));
    }

    private String title(String key) {
        return switch (key) {
            case "general" -> "General information / Thông tin chung / 一般資訊";
            case "specification" -> "Specifications / Quy cách / 規格";
            case "process" -> "Process and change details / Quy trình và thay đổi / 製程與變更";
            case "approval" -> "Approval / Phê duyệt / 核准";
            case "tracking" -> "Tracking / Theo dõi / 追蹤";
            default -> escape(key);
        };
    }

    private static String escape(String value) { return HtmlUtils.htmlEscape(value == null ? "" : value); }
}
