package com.ems.employeemanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource does not exist in the database.
 *
 * <p>@ResponseStatus(HttpStatus.NOT_FOUND) is a Spring MVC annotation.
 * When this exception propagates to Spring's DispatcherServlet, it
 * returns HTTP 404 automatically. However, since we use a
 * @RestControllerAdvice GlobalExceptionHandler, the handler has full
 * control and uses the status from the annotation for consistency.
 *
 * <p>Extends RuntimeException — unchecked. Services throw this
 * without declaring it in the method signature (cleaner API).
 *
 * <p>Usage:
 * <pre>
 * Employee employee = employeeRepository.findById(id)
 *     .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
 * </pre>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param resourceName  the entity type (e.g., "Employee", "Department")
     * @param fieldName     the field used to look it up (e.g., "id", "email")
     * @param fieldValue    the value that wasn't found
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

