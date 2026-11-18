package com.ems.employeemanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the authenticated user does not have permission for the operation.
 *
 * <p>Different from Spring Security's AccessDeniedException in that this is
 * thrown explicitly by business logic (e.g., employee trying to access
 * another employee's private records).
 *
 * <p>HTTP 403 Forbidden — the user is authenticated but not authorized.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException() {
        super("You do not have permission to perform this action");
    }
}

