package com.devdecision.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API endpoints.
 * Provides consistent error responses and proper logging for all exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Validation error on request to {}: {}", request.getDescription(false), ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Request validation failed",
            errors,
            request.getDescription(false),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle constraint violations from @Validated annotations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        logger.warn("Constraint violation on request to {}: {}", request.getDescription(false), ex.getMessage());
        
        Map<String, String> errors = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage
            ));

        ErrorResponse errorResponse = new ErrorResponse(
            "CONSTRAINT_VIOLATION",
            "Request constraint validation failed",
            errors,
            request.getDescription(false),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle illegal argument exceptions (business logic validation)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("Illegal argument on request to {}: {}", request.getDescription(false), ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_ARGUMENT",
            ex.getMessage(),
            null,
            request.getDescription(false),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle illegal state exceptions (system state issues)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        logger.error("Illegal state on request to {}: {}", request.getDescription(false), ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_STATE",
            "System is in an invalid state for this operation",
            null,
            request.getDescription(false),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle runtime exceptions (unexpected errors)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        logger.error("Runtime exception on request to {}", request.getDescription(false), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred while processing the request",
            null,
            request.getDescription(false),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected exception on request to {}", request.getDescription(false), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "UNKNOWN_ERROR",
            "An unknown error occurred while processing the request",
            null,
            request.getDescription(false),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Standardized error response structure
     */
    public record ErrorResponse(
        String errorCode,
        String message,
        Map<String, String> details,
        String path,
        LocalDateTime timestamp
    ) {}
}