package com.ems.employeemanagement.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for daily check-in.
 *
 * @param employeeId optional target employee ID (if submitted by HR/Admin on behalf;
 *                   if omitted, inferred from the authenticated user)
 * @param notes      optional remarks (e.g. "Work from client site")
 */
public record CheckInRequest(

        Long employeeId,

        @Size(max = 255, message = "Notes cannot exceed 255 characters")
        String notes
) {}

