package com.ems.employeemanagement.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for approving or rejecting a leave request.
 *
 * @param reviewerNotes optional feedback or rationale from reviewer (manager or HR)
 */
public record LeaveReviewRequest(

        @Size(max = 500, message = "Reviewer notes cannot exceed 500 characters")
        String reviewerNotes
) {}

