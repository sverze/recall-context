package com.recallcontext.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiKeyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleApiKeyNotFound(ApiKeyNotFoundException ex) {
        log.error("API key not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        "API_KEY_NOT_CONFIGURED",
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED.value()
                ));
    }

    @ExceptionHandler(InvalidFilenameException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFilename(InvalidFilenameException ex) {
        log.error("Invalid filename: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "INVALID_FILENAME",
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    @ExceptionHandler(TranscriptProcessingException.class)
    public ResponseEntity<ErrorResponse> handleTranscriptProcessing(TranscriptProcessingException ex) {
        log.error("Transcript processing error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "PROCESSING_FAILED",
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                ));
    }

    @ExceptionHandler(AnthropicApiException.class)
    public ResponseEntity<ErrorResponse> handleAnthropicApi(AnthropicApiException ex) {
        log.error("Anthropic API error: {} (status: {})", ex.getMessage(), ex.getStatusCode());

        // Map Anthropic errors to appropriate HTTP status
        HttpStatus status = switch (ex.getStatusCode()) {
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;
            case 400 -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_GATEWAY;
        };

        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        "AI_SERVICE_ERROR",
                        ex.getMessage(),
                        status.value()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation error: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "VALIDATION_ERROR",
                        "Invalid request parameters",
                        HttpStatus.BAD_REQUEST.value(),
                        errors
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again later.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                ));
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String code;
        private String message;
        private int status;
        private LocalDateTime timestamp;
        private Map<String, String> details;

        public ErrorResponse(String code, String message, int status) {
            this.code = code;
            this.message = message;
            this.status = status;
            this.timestamp = LocalDateTime.now();
            this.details = null;
        }

        public ErrorResponse(String code, String message, int status, Map<String, String> details) {
            this.code = code;
            this.message = message;
            this.status = status;
            this.timestamp = LocalDateTime.now();
            this.details = details;
        }
    }
}
