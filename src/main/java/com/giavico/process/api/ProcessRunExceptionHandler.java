package com.giavico.process.api;

import com.giavico.process.service.InvalidProcessRunException;
import com.giavico.process.service.ProcessRunNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.giavico.process")
public class ProcessRunExceptionHandler {
    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
    }

    @ExceptionHandler(ProcessRunNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(ProcessRunNotFoundException error, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, error.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidProcessRunException.class)
    ResponseEntity<ErrorResponse> badRequest(InvalidProcessRunException error, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, error.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException error, HttpServletRequest request) {
        Map<String, String> fields = error.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        item -> item.getField(),
                        item -> item.getDefaultMessage() == null ? "Invalid value" : item.getDefaultMessage(),
                        (left, right) -> left
                ));
        return response(HttpStatus.BAD_REQUEST, "Request validation failed.", request, fields);
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fields
    ) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), message,
                request.getRequestURI(), fields
        ));
    }
}
