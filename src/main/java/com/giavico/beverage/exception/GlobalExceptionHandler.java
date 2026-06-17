package com.giavico.beverage.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giavico.beverage.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Request validation failed.", request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(CompleteResponseParsingException.class)
    ResponseEntity<ErrorResponse> handleCompleteParsing(CompleteResponseParsingException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY,
                "Ollama returned a complete response, but it could not be parsed into the formula contract. " + exception.getMessage(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(FormulaNotFoundException.class)
    ResponseEntity<ErrorResponse> handleFormulaNotFound(FormulaNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InventoryItemNotFoundException.class)
    ResponseEntity<ErrorResponse> handleInventoryItemNotFound(InventoryItemNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InventoryValidationException.class)
    ResponseEntity<ErrorResponse> handleInventoryValidation(InventoryValidationException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(StreamChunkParsingException.class)
    ResponseEntity<ErrorResponse> handleStreamParsing(StreamChunkParsingException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY,
                "Ollama returned a malformed incremental stream chunk. " + exception.getMessage(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler({WebClientRequestException.class, OllamaServiceException.class})
    ResponseEntity<ErrorResponse> handleOllamaNetwork(RuntimeException exception, HttpServletRequest request) {
        Throwable root = rootCause(exception);
        HttpStatus status = isTimeout(root) ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.SERVICE_UNAVAILABLE;
        String message = isTimeout(root)
                ? "The local Ollama request exceeded the configured R&D processing timeout. The model may be overloaded or the prompt may be too large."
                : "Unable to reach the local Ollama service at http://localhost:11434. Confirm Ollama is running, the model is available, and local resources are sufficient.";

        if (root instanceof ConnectException) {
            message = "Connection to local Ollama at http://localhost:11434 was refused. Start Ollama or verify the configured endpoint.";
        }

        return build(status, message, request.getRequestURI(), null);
    }

    @ExceptionHandler(WebClientResponseException.class)
    ResponseEntity<ErrorResponse> handleOllamaHttp(WebClientResponseException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY,
                "Local Ollama returned HTTP %s while generating the formula. Response body: %s"
                        .formatted(exception.getStatusCode().value(), exception.getResponseBodyAsString()),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(JsonProcessingException.class)
    ResponseEntity<ErrorResponse> handleJson(JsonProcessingException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid JSON payload: " + exception.getOriginalMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected formulation engine error: " + exception.getMessage(), request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String path, Map<String, String> fieldErrors) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fieldErrors
        ));
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private boolean isTimeout(Throwable throwable) {
        return throwable instanceof TimeoutException || throwable instanceof SocketTimeoutException;
    }
}
