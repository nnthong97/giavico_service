package com.giavico.rnd.api;

import com.giavico.rnd.domain.DocumentStatus;
import com.giavico.rnd.domain.DocumentType;
import com.giavico.rnd.service.DocumentHtmlExportService;
import com.giavico.rnd.service.DocumentPdfExportService;
import com.giavico.rnd.service.DocumentTemplateCatalog;
import com.giavico.rnd.service.PdfExportException;
import com.giavico.rnd.service.RndDocumentManagementService;
import jakarta.validation.Valid;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rnd-documents")
public class RndDocumentController {
    private final RndDocumentManagementService service;
    private final DocumentTemplateCatalog templates;
    private final DocumentHtmlExportService exporter;
    private final DocumentPdfExportService pdfExporter;

    public RndDocumentController(RndDocumentManagementService service, DocumentTemplateCatalog templates,
                                 DocumentHtmlExportService exporter, DocumentPdfExportService pdfExporter) {
        this.service = service;
        this.templates = templates;
        this.exporter = exporter;
        this.pdfExporter = pdfExporter;
    }

    @GetMapping("/templates")
    public List<DocumentTemplateResponses.Template> templates() {
        return templates.findAll();
    }

    @GetMapping("/templates/{type}")
    public DocumentTemplateResponses.Template template(@PathVariable DocumentType type) {
        return templates.find(type);
    }

    @GetMapping("/templates/{type}/source")
    public ResponseEntity<Resource> templateSource(@PathVariable DocumentType type) {
        if (!templates.find(type).sourceAvailable()) {
            throw new PdfExportException("The source file for " + type + " is unavailable or locked.");
        }
        String filename = templates.sourceFile(type);
        Resource resource = new ClassPathResource("document-templates/" + filename);
        if (!resource.exists()) throw new PdfExportException("The source file for " + type + " is missing.");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaTypeFactory.getMediaType(filename).orElse(MediaType.APPLICATION_OCTET_STREAM)).body(resource);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponses.Detail create(@Valid @RequestBody DocumentRequests.Save request) {
        return service.create(request);
    }

    @GetMapping
    public Page<DocumentResponses.Summary> list(@PageableDefault(size = 20, sort = "updatedAt") Pageable pageable,
                                                @RequestParam(required = false) DocumentStatus status,
                                                @RequestParam(required = false) String search) {
        return service.list(pageable, status, search);
    }

    @GetMapping("/{id}")
    public DocumentResponses.Detail get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public DocumentResponses.Detail update(@PathVariable UUID id, @Valid @RequestBody DocumentRequests.Save request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @GetMapping(value = "/{id}/print", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> print(@PathVariable UUID id) {
        DocumentResponses.Detail document = service.get(id);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.documentNumber() + ".html\"")
                .contentType(MediaType.TEXT_HTML).body(exporter.render(document));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
        DocumentResponses.Detail document = service.get(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.documentNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfExporter.render(document));
    }
    @PostMapping("/{id}/{action:submit|start-review|approve|request-changes|issue|acknowledge}")
    public DocumentResponses.Detail transition(@PathVariable UUID id, @PathVariable String action,
                                               @Valid @RequestBody DocumentRequests.Workflow request) {
        return service.transition(id, action, request);
    }
}
