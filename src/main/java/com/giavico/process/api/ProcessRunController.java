package com.giavico.process.api;

import com.giavico.process.domain.ProcessRunStatus;
import com.giavico.process.service.ProcessRunManagementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/process-runs")
public class ProcessRunController {
    private final ProcessRunManagementService service;

    public ProcessRunController(ProcessRunManagementService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcessRunResponses.Detail create(@Valid @RequestBody ProcessRunRequests.Save request) {
        return service.create(request);
    }

    @GetMapping
    public Page<ProcessRunResponses.Summary> list(
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable,
            @RequestParam(required = false) String workflowId,
            @RequestParam(required = false) ProcessRunStatus status
    ) {
        return service.list(pageable, workflowId, status);
    }

    @GetMapping("/latest")
    public ProcessRunResponses.Detail latest(@RequestParam String workflowId) {
        return service.latestOpen(workflowId);
    }

    @GetMapping("/{id}")
    public ProcessRunResponses.Detail get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public ProcessRunResponses.Detail update(
            @PathVariable UUID id,
            @Valid @RequestBody ProcessRunRequests.Save request
    ) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/complete")
    public ProcessRunResponses.Detail complete(
            @PathVariable UUID id,
            @Valid @RequestBody ProcessRunRequests.Save request
    ) {
        return service.complete(id, request);
    }
}
