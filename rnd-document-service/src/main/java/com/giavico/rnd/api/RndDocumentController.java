package com.giavico.rnd.api;

import com.giavico.rnd.domain.DocumentStatus;
import com.giavico.rnd.service.RndDocumentManagementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/rnd-documents")
public class RndDocumentController {
    private final RndDocumentManagementService service;
    public RndDocumentController(RndDocumentManagementService service) { this.service = service; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public DocumentResponses.Detail create(@Valid @RequestBody DocumentRequests.Save request) { return service.create(request); }
    @GetMapping public Page<DocumentResponses.Summary> list(@PageableDefault(size = 20, sort = "updatedAt") Pageable pageable,
            @RequestParam(required = false) DocumentStatus status, @RequestParam(required = false) String search) { return service.list(pageable, status, search); }
    @GetMapping("/{id}") public DocumentResponses.Detail get(@PathVariable UUID id) { return service.get(id); }
    @PutMapping("/{id}") public DocumentResponses.Detail update(@PathVariable UUID id, @Valid @RequestBody DocumentRequests.Save request) { return service.update(id, request); }
    @PostMapping("/{id}/{action:submit|start-review|approve|request-changes|issue|acknowledge}")
    public DocumentResponses.Detail transition(@PathVariable UUID id, @PathVariable String action, @Valid @RequestBody DocumentRequests.Workflow request) { return service.transition(id, action, request); }
}
