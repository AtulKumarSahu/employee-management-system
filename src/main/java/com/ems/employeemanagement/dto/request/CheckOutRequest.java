package com.ems.employeemanagement.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for daily check-out.
 *
 * @param employeeId optional target employee ID (if submitted by HR/Admin on behalf;
 *                   if omitted, inferred from the authenticated user)
 * @param notes      optional closing remarks
 */
public record CheckOutRequest(

        Long employeeId,

        @Size(max = 255, message = "Notes cannot exceed 255 characters")
        String notes
) {}

