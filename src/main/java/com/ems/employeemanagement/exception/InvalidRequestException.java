package com.ems.employeemanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a request is structurally valid but violates business rules.
 *
 * <p>Examples:
 * <ul>
 *   <li>Applying for leave on dates that overlap with an existing request</li>
 *   <li>Approving a leave that has already been cancelled</li>
 *   <li>Checking in when already checked in for the day</li>
 *   <li>Deleting a department that has active employees</li>
 * </ul>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}

