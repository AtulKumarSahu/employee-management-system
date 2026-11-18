package com.ems.employeemanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a CREATE or UPDATE request would violate a uniqueness constraint.
 *
 * <p>Examples:
 * <ul>
 *   <li>Registering an email that already exists</li>
 *   <li>Creating a department with a name already in use</li>
 *   <li>Creating an employee with an email already assigned</li>
 * </ul>
 *
 * <p>HTTP 409 Conflict is the semantically correct status for duplicate resource errors.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }

    public DuplicateResourceException(String message) {
        super(message);
    }
}

