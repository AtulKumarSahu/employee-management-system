package com.ems.employeemanagement.exception;

import com.ems.employeemanagement.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler for all REST controllers.
 *
 * <p>@RestControllerAdvice = @ControllerAdvice + @ResponseBody.
 * Any exception thrown from any @RestController is caught here before
 * it reaches the client, ensuring a consistent JSON error format.
 *
 * <p>@Slf4j (Lombok): generates a static {@code log} field using SLF4J.
 * Equivalent to: {@code private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);}
 *
 * <p>Handler priority: Spring calls the most specific handler first.
 * Specific exceptions (ResourceNotFoundException) are handled before
 * the generic Exception fallback.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ==================== Our Custom Exceptions ====================

    /**
     * Handles 404 Not Found — resource does not exist.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles 409 Conflict — duplicate resource.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {

        log.warn("Duplicate resource: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "Conflict",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles 400 Bad Request — invalid business operation.
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(
            InvalidRequestException ex, HttpServletRequest request) {

        log.warn("Invalid request: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles 403 Forbidden — insufficient permissions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        "Forbidden",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    // ==================== Spring / Jakarta Validation ====================

    /**
     * Handles 400 — @Valid validation failures on @RequestBody DTOs.
     *
     * <p>When @Valid fails on a request body, Spring throws
     * MethodArgumentNotValidException containing all field errors.
     * We collect them into a map of field → message for a detailed response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();

        // BindingResult contains all constraint violations
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed on path: {} | Errors: {}", request.getRequestURI(), fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.ofValidation(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed. Please check the request body.",
                        fieldErrors,
                        request.getRequestURI()
                ));
    }

    // ==================== Spring Security ====================

    /**
     * Handles 401 — wrong username or password during authentication.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        // IMPORTANT: Never log the actual credentials — only the fact that it failed.
        log.warn("Authentication failed for path: {}", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized",
                        "Invalid email or password",
                        request.getRequestURI()
                ));
    }

    // ==================== Spring MVC ====================

    /**
     * Handles 400 — path variable or request parameter type mismatch.
     * Example: GET /api/v1/employees/abc where {id} expects a Long.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format(
                "Parameter '%s' should be of type '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        log.warn("Type mismatch: {} | Path: {}", message, request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        message,
                        request.getRequestURI()
                ));
    }

    // ==================== Database ====================

    /**
     * Handles 409 — database constraint violations (unique, not-null, FK).
     *
     * <p>This catches errors that slip through our application-level
     * duplicate checks (e.g., concurrent inserts in a race condition).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Database constraint violation | Path: {} | Cause: {}",
                request.getRequestURI(), ex.getMostSpecificCause().getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "Conflict",
                        "A database constraint was violated. The resource may already exist.",
                        request.getRequestURI()
                ));
    }

    // ==================== Fallback ====================

    /**
     * Catch-all handler for unexpected exceptions.
     *
     * <p>Returns 500 Internal Server Error. The actual exception is logged
     * at ERROR level for investigation. We never expose internal error details
     * (stack traces, SQL queries) to the client — that's a security risk.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        // Log the full stack trace internally but send a generic message externally
        log.error("Unexpected error on path: {} | Error: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An unexpected error occurred. Please try again later.",
                        request.getRequestURI()
                ));
    }
}

