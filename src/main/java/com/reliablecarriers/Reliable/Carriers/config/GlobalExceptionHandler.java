package com.reliablecarriers.Reliable.Carriers.config;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler for consistent error responses across all controllers
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.warn("Validation error [{}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("requestId", requestId);
        body.put("error", "Validation failed");

        Map<String, String> errors = new HashMap<>();
        String[] firstError = {null}; // Use array to allow modification in lambda
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            if (firstError[0] == null) {
                firstError[0] = errorMessage;
            }
        });
        body.put("errors", errors);
        
        // Also include a top-level error message for easier frontend handling
        if (firstError[0] != null) {
            body.put("message", firstError[0]);
        }

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        String requestId = UUID.randomUUID().toString();
        logger.warn("Constraint violation [{}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("requestId", requestId);
        body.put("error", "Invalid input");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        String requestId = UUID.randomUUID().toString();
        logger.warn("Entity not found [{}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("requestId", requestId);
        body.put("error", "Resource not found");
        body.put("message", ex.getMessage() != null ? ex.getMessage() : "The requested resource was not found");
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        String requestId = UUID.randomUUID().toString();
        logger.warn("Access denied [{}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("requestId", requestId);
        body.put("error", "Access denied");
        body.put("message", "You do not have permission to access this resource");
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        String requestId = UUID.randomUUID().toString();
        logger.warn("Bad credentials [{}]", requestId);
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("requestId", requestId);
        body.put("error", "Authentication failed");
        body.put("message", "Invalid email or password");
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Object> handleLocked(LockedException ex) {
        String requestId = UUID.randomUUID().toString();
        logger.warn("Account locked [{}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.LOCKED.value());
        body.put("requestId", requestId);
        body.put("error", "Account locked");
        body.put("message", ex.getMessage() != null ? ex.getMessage() : "Your account has been locked due to too many failed login attempts");
        return new ResponseEntity<>(body, HttpStatus.LOCKED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        String requestId = UUID.randomUUID().toString();
        logger.warn("Illegal argument [{}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("requestId", requestId);
        body.put("error", "Invalid request");
        body.put("message", ex.getMessage() != null ? ex.getMessage() : "Invalid input provided");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntime(RuntimeException ex) {
        String requestId = UUID.randomUUID().toString();
        logger.error("Runtime error [{}]: {}", requestId, ex.getMessage(), ex);
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("requestId", requestId);
        body.put("error", "Internal server error");
        
        // Only include detailed message in development mode
        boolean isDevelopment = System.getProperty("production.mode") == null || 
                               !System.getProperty("production.mode").equals("true");
        if (isDevelopment) {
            body.put("message", ex.getMessage());
            body.put("details", ex.getClass().getSimpleName());
        } else {
            body.put("message", "An error occurred while processing your request. Please try again later.");
        }
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        String requestId = UUID.randomUUID().toString();
        logger.error("Unexpected error [{}]: {}", requestId, ex.getMessage(), ex);
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("requestId", requestId);
        body.put("error", "Internal server error");
        body.put("message", "An unexpected error occurred. Please contact support if the problem persists.");
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}



