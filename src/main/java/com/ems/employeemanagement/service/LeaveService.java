package com.ems.employeemanagement.service;

import com.ems.employeemanagement.dto.request.LeaveApplicationRequest;
import com.ems.employeemanagement.dto.request.LeaveBalanceInitRequest;
import com.ems.employeemanagement.dto.request.LeaveReviewRequest;
import com.ems.employeemanagement.dto.response.LeaveBalanceResponse;
import com.ems.employeemanagement.dto.response.LeaveRequestResponse;
import com.ems.employeemanagement.enums.LeaveStatus;
import com.ems.employeemanagement.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service contract for managing leave requests, approvals, cancellations, and balances.
 */
public interface LeaveService {

    /**
     * Submits a new leave request after checking date validity, overlaps, and balances.
     */
    LeaveRequestResponse applyLeave(LeaveApplicationRequest request, CustomUserDetails currentUser);

    /**
     * Approves a pending leave request and deducts leave balance quota.
     */
    LeaveRequestResponse approveLeave(Long leaveId, LeaveReviewRequest reviewRequest, CustomUserDetails reviewer);

    /**
     * Rejects a pending leave request with optional reviewer notes.
     */
    LeaveRequestResponse rejectLeave(Long leaveId, LeaveReviewRequest reviewRequest, CustomUserDetails reviewer);

    /**
     * Cancels a pending or approved leave request, restoring balance if previously approved.
     */
    LeaveRequestResponse cancelLeave(Long leaveId, CustomUserDetails currentUser);

    /**
     * Retrieves a leave request by its unique identifier.
     */
    LeaveRequestResponse getLeaveById(Long leaveId, CustomUserDetails currentUser);

    /**
     * Fetches leave requests matching optional employee and status filters (paginated).
     */
    Page<LeaveRequestResponse> getAllLeaves(Long employeeId, LeaveStatus status, Pageable pageable);

    /**
     * Fetches leave history of the currently authenticated employee.
     */
    Page<LeaveRequestResponse> getMyLeaves(CustomUserDetails currentUser, Pageable pageable);

    /**
     * Retrieves annual leave quotas and remaining balances for an employee.
     */
    List<LeaveBalanceResponse> getLeaveBalances(Long employeeId, Integer year);

    /**
     * Initializes annual leave quotas for an employee.
     */
    List<LeaveBalanceResponse> initializeLeaveBalances(LeaveBalanceInitRequest request);
}

