package com.ems.employeemanagement.controller;

import com.ems.employeemanagement.constants.AppConstants;
import com.ems.employeemanagement.dto.request.LeaveApplicationRequest;
import com.ems.employeemanagement.dto.request.LeaveBalanceInitRequest;
import com.ems.employeemanagement.dto.request.LeaveReviewRequest;
import com.ems.employeemanagement.dto.response.ApiResponse;
import com.ems.employeemanagement.dto.response.LeaveBalanceResponse;
import com.ems.employeemanagement.dto.response.LeaveRequestResponse;
import com.ems.employeemanagement.dto.response.PaginationResponse;
import com.ems.employeemanagement.enums.LeaveStatus;
import com.ems.employeemanagement.security.CustomUserDetails;
import com.ems.employeemanagement.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing endpoints for the leave management lifecycle.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/leaves")
@Slf4j
@Tag(name = "Leave Management", description = "Endpoints for applying, reviewing, cancelling leaves, and managing balances")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    /**
     * POST /api/v1/leaves — Submit a new leave application.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    @Operation(summary = "Apply for leave", description = "Submits a leave request with automatic balance and overlap validation")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> applyLeave(
            @Valid @RequestBody LeaveApplicationRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.info("REST request to apply for leave by user: {}", currentUser.getUsername());
        LeaveRequestResponse response = leaveService.applyLeave(request, currentUser);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave application submitted successfully", response));
    }

    /**
     * GET /api/v1/leaves — List leave requests with optional filters (paginated).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get all leave requests", description = "List leaves with optional employeeId and status filters")
    public ResponseEntity<ApiResponse<PaginationResponse<LeaveRequestResponse>>> getAllLeaves(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LeaveRequestResponse> pageResult = leaveService.getAllLeaves(employeeId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved successfully", PaginationResponse.from(pageResult)));
    }

    /**
     * GET /api/v1/leaves/my-leaves — View current employee's leave history.
     */
    @GetMapping("/my-leaves")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    @Operation(summary = "Get current user's leave history")
    public ResponseEntity<ApiResponse<PaginationResponse<LeaveRequestResponse>>> getMyLeaves(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LeaveRequestResponse> pageResult = leaveService.getMyLeaves(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("Leave history retrieved successfully", PaginationResponse.from(pageResult)));
    }

    /**
     * GET /api/v1/leaves/{id} — View leave request details by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    @Operation(summary = "Get leave request by ID")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> getLeaveById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        LeaveRequestResponse response = leaveService.getLeaveById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave request retrieved", response));
    }

    /**
     * PATCH /api/v1/leaves/{id}/approve — Approve a pending leave request.
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Approve leave request", description = "Approves a pending leave and deducts from employee leave balance")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> approveLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveReviewRequest reviewRequest,
            @AuthenticationPrincipal CustomUserDetails reviewer) {

        LeaveRequestResponse response = leaveService.approveLeave(id, reviewRequest, reviewer);
        return ResponseEntity.ok(ApiResponse.success("Leave request approved successfully", response));
    }

    /**
     * PATCH /api/v1/leaves/{id}/reject — Reject a pending leave request.
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Reject leave request", description = "Rejects a pending leave request with reviewer notes")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> rejectLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveReviewRequest reviewRequest,
            @AuthenticationPrincipal CustomUserDetails reviewer) {

        LeaveRequestResponse response = leaveService.rejectLeave(id, reviewRequest, reviewer);
        return ResponseEntity.ok(ApiResponse.success("Leave request rejected", response));
    }

    /**
     * PATCH /api/v1/leaves/{id}/cancel — Cancel a pending or approved leave request.
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    @Operation(summary = "Cancel leave request", description = "Cancels leave and restores balance if previously approved")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> cancelLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        LeaveRequestResponse response = leaveService.cancelLeave(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave request cancelled successfully", response));
    }

    /**
     * GET /api/v1/leaves/balances/{employeeId} — Get employee's leave balance quotas.
     */
    @GetMapping("/balances/{employeeId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    @Operation(summary = "Get leave balance quotas", description = "Retrieves leave balances for an employee for a specific year")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getLeaveBalances(
            @PathVariable Long employeeId,
            @Parameter(description = "Leave year (e.g. 2026); defaults to current year")
            @RequestParam(required = false) Integer year) {

        List<LeaveBalanceResponse> balances = leaveService.getLeaveBalances(employeeId, year);
        return ResponseEntity.ok(ApiResponse.success("Leave balances retrieved successfully", balances));
    }

    /**
     * POST /api/v1/leaves/balances/init — Initialize annual leave quotas for an employee.
     */
    @PostMapping("/balances/init")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Initialize leave balances", description = "Sets annual quota for CASUAL, SICK, and EARNED leaves")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> initializeLeaveBalances(
            @Valid @RequestBody LeaveBalanceInitRequest request) {

        List<LeaveBalanceResponse> balances = leaveService.initializeLeaveBalances(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave balances initialized successfully", balances));
    }
}

