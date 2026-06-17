package com.giavico.beverage.api.controller;

import com.giavico.beverage.api.dto.FormulaDetailResponse;
import com.giavico.beverage.api.dto.FormulaGenerationRequest;
import com.giavico.beverage.api.dto.FormulaGenerationResponse;
import com.giavico.beverage.api.dto.FormulaListItem;
import com.giavico.beverage.api.dto.FormulaStoreRequest;
import com.giavico.beverage.service.FormulaPersistenceService;
import com.giavico.beverage.service.OllamaFormulationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/formulas")
public class FormulaController {

    private final OllamaFormulationService formulationService;
    private final FormulaPersistenceService persistenceService;

    public FormulaController(OllamaFormulationService formulationService, FormulaPersistenceService persistenceService) {
        this.formulationService = formulationService;
        this.persistenceService = persistenceService;
    }

    @PostMapping
    public FormulaGenerationResponse store(@Valid @RequestBody FormulaStoreRequest request) {
        return persistenceService.store(request);
    }

    @GetMapping
    public Page<FormulaListItem> list(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return persistenceService.list(pageable);
    }

    @GetMapping("/{id}")
    public FormulaDetailResponse get(@PathVariable UUID id) {
        return persistenceService.get(id);
    }

    @PutMapping("/{id}")
    public FormulaDetailResponse update(@PathVariable UUID id, @Valid @RequestBody FormulaStoreRequest request) {
        return persistenceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        persistenceService.delete(id);
    }

    @PostMapping("/generate")
    public FormulaGenerationResponse generate(@Valid @RequestBody FormulaGenerationRequest request) {
        return formulationService.generateComplete(request);
    }

    @PostMapping(value = "/generate/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateStream(@Valid @RequestBody FormulaGenerationRequest request) {
        return formulationService.generateStream(request);
    }

    @GetMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateStreamFromQuery(@Valid @ModelAttribute FormulaGenerationRequest request) {
        return formulationService.generateStream(request);
    }
}
