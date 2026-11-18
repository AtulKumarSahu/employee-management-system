package com.ems.employeemanagement.service.impl;

import com.ems.employeemanagement.constants.AppConstants;
import com.ems.employeemanagement.dto.request.LeaveApplicationRequest;
import com.ems.employeemanagement.dto.request.LeaveBalanceInitRequest;
import com.ems.employeemanagement.dto.request.LeaveReviewRequest;
import com.ems.employeemanagement.dto.response.LeaveBalanceResponse;
import com.ems.employeemanagement.dto.response.LeaveRequestResponse;
import com.ems.employeemanagement.entity.Employee;
import com.ems.employeemanagement.entity.LeaveBalance;
import com.ems.employeemanagement.entity.LeaveRequest;
import com.ems.employeemanagement.entity.User;
import com.ems.employeemanagement.enums.LeaveStatus;
import com.ems.employeemanagement.enums.LeaveType;
import com.ems.employeemanagement.exception.AccessDeniedException;
import com.ems.employeemanagement.exception.InvalidRequestException;
import com.ems.employeemanagement.exception.ResourceNotFoundException;
import com.ems.employeemanagement.mapper.LeaveMapper;
import com.ems.employeemanagement.repository.EmployeeRepository;
import com.ems.employeemanagement.repository.LeaveBalanceRepository;
import com.ems.employeemanagement.repository.LeaveRequestRepository;
import com.ems.employeemanagement.repository.UserRepository;
import com.ems.employeemanagement.security.CustomUserDetails;
import com.ems.employeemanagement.service.LeaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link LeaveService}.
 */
@Service
@Slf4j
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository     employeeRepository;
    private final UserRepository         userRepository;
    private final LeaveMapper            leaveMapper;

    public LeaveServiceImpl(
            LeaveRequestRepository leaveRequestRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            LeaveMapper leaveMapper) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository     = employeeRepository;
        this.userRepository         = userRepository;
        this.leaveMapper            = leaveMapper;
    }

    @Override
    @Transactional
    public LeaveRequestResponse applyLeave(LeaveApplicationRequest request, CustomUserDetails currentUser) {
        log.info("Processing leave application for type: {}", request.leaveType());

        // 1. Validate date logic
        if (request.startDate().isAfter(request.endDate())) {
            throw new InvalidRequestException("Leave start date cannot be after end date");
        }

        // 2. Resolve target employee
        Employee employee = resolveTargetEmployee(request.employeeId(), currentUser);

        // 3. Calculate total requested leave days
        int totalDays = (int) ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1;

        // 4. Overlap verification
        boolean hasOverlap = leaveRequestRepository.hasOverlappingLeave(
                employee.getId(),
                request.startDate(),
                request.endDate(),
                -1L
        );
        if (hasOverlap) {
            throw new InvalidRequestException("An active (pending or approved) leave request already overlaps with these dates");
        }

        // 5. Balance verification for non-UNPAID leaves
        int leaveYear = request.startDate().getYear();
        if (request.leaveType() != LeaveType.UNPAID) {
            LeaveBalance balance = leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeAndLeaveYear(employee.getId(), request.leaveType(), leaveYear)
                    .orElseThrow(() -> new InvalidRequestException(
                            String.format("No leave balance configured for %s leave in year %d. Please contact HR.",
                                    request.leaveType(), leaveYear)));

            if (!balance.hasSufficientBalance(totalDays)) {
                throw new InvalidRequestException(
                        String.format("Insufficient %s leave balance. Requested: %d, Available: %d",
                                request.leaveType(), totalDays, balance.getRemainingDays()));
            }
        }

        // 6. Build and persist leave request
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.leaveType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .totalDays(totalDays)
                .reason(request.reason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request created successfully with id: {}", saved.getId());

        return leaveMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LeaveRequestResponse approveLeave(Long leaveId, LeaveReviewRequest reviewRequest, CustomUserDetails reviewer) {
        log.info("Approving leave request id: {} by reviewer id: {}", leaveId, reviewer.getId());

        LeaveRequest leave = findLeaveByIdOrThrow(leaveId);

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidRequestException("Only PENDING leave requests can be approved. Current status: " + leave.getStatus());
        }

        User reviewerUser = userRepository.findById(reviewer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reviewer.getId()));

        // Deduct balance for non-UNPAID leaves
        if (leave.getLeaveType() != LeaveType.UNPAID) {
            int leaveYear = leave.getStartDate().getYear();
            LeaveBalance balance = leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeAndLeaveYear(leave.getEmployee().getId(), leave.getLeaveType(), leaveYear)
                    .orElseThrow(() -> new InvalidRequestException("Leave balance record not found for deduction"));

            if (!balance.hasSufficientBalance(leave.getTotalDays())) {
                throw new InvalidRequestException("Employee no longer has sufficient leave balance to approve this request");
            }

            balance.deductDays(leave.getTotalDays());
            leaveBalanceRepository.save(balance);
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setReviewedBy(reviewerUser);
        leave.setReviewedAt(LocalDateTime.now());
        if (reviewRequest != null && reviewRequest.reviewerNotes() != null) {
            leave.setReviewerNotes(reviewRequest.reviewerNotes());
        }

        LeaveRequest updated = leaveRequestRepository.save(leave);
        log.info("Leave request id: {} marked APPROVED", leaveId);

        return leaveMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public LeaveRequestResponse rejectLeave(Long leaveId, LeaveReviewRequest reviewRequest, CustomUserDetails reviewer) {
        log.info("Rejecting leave request id: {} by reviewer id: {}", leaveId, reviewer.getId());

        LeaveRequest leave = findLeaveByIdOrThrow(leaveId);

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidRequestException("Only PENDING leave requests can be rejected. Current status: " + leave.getStatus());
        }

        User reviewerUser = userRepository.findById(reviewer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reviewer.getId()));

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setReviewedBy(reviewerUser);
        leave.setReviewedAt(LocalDateTime.now());
        if (reviewRequest != null && reviewRequest.reviewerNotes() != null) {
            leave.setReviewerNotes(reviewRequest.reviewerNotes());
        }

        LeaveRequest updated = leaveRequestRepository.save(leave);
        log.info("Leave request id: {} marked REJECTED", leaveId);

        return leaveMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public LeaveRequestResponse cancelLeave(Long leaveId, CustomUserDetails currentUser) {
        log.info("Cancelling leave request id: {}", leaveId);

        LeaveRequest leave = findLeaveByIdOrThrow(leaveId);

        // Security check: only the employee who submitted or ADMIN/HR may cancel
        boolean isOwner = isRequestOwner(leave, currentUser);
        boolean isPrivileged = hasPrivilegedRole(currentUser);
        if (!isOwner && !isPrivileged) {
            throw new AccessDeniedException("You are not authorized to cancel this leave request");
        }

        if (leave.getStatus() == LeaveStatus.CANCELLED || leave.getStatus() == LeaveStatus.REJECTED) {
            throw new InvalidRequestException("Cannot cancel leave request that is already " + leave.getStatus());
        }

        // If it was already approved, restore the deducted days
        if (leave.getStatus() == LeaveStatus.APPROVED && leave.getLeaveType() != LeaveType.UNPAID) {
            int leaveYear = leave.getStartDate().getYear();
            leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeAndLeaveYear(leave.getEmployee().getId(), leave.getLeaveType(), leaveYear)
                    .ifPresent(balance -> {
                        balance.restoreDays(leave.getTotalDays());
                        leaveBalanceRepository.save(balance);
                    });
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest updated = leaveRequestRepository.save(leave);
        log.info("Leave request id: {} marked CANCELLED", leaveId);

        return leaveMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveRequestResponse getLeaveById(Long leaveId, CustomUserDetails currentUser) {
        LeaveRequest leave = findLeaveByIdOrThrow(leaveId);

        if (!isRequestOwner(leave, currentUser) && !hasPrivilegedRole(currentUser)) {
            throw new AccessDeniedException("You do not have permission to view this leave request");
        }

        return leaveMapper.toResponse(leave);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestResponse> getAllLeaves(Long employeeId, LeaveStatus status, Pageable pageable) {
        if (employeeId != null && status != null) {
            return leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, status, pageable)
                    .map(leaveMapper::toResponse);
        } else if (employeeId != null) {
            return leaveRequestRepository.findByEmployeeId(employeeId, pageable)
                    .map(leaveMapper::toResponse);
        } else if (status != null) {
            return leaveRequestRepository.findByStatus(status, pageable)
                    .map(leaveMapper::toResponse);
        } else {
            return leaveRequestRepository.findAll(pageable)
                    .map(leaveMapper::toResponse);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestResponse> getMyLeaves(CustomUserDetails currentUser, Pageable pageable) {
        Employee employee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No employee profile found for current user"));

        return leaveRequestRepository.findByEmployeeId(employee.getId(), pageable)
                .map(leaveMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getLeaveBalances(Long employeeId, Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        return leaveBalanceRepository.findByEmployeeIdAndLeaveYear(employeeId, targetYear)
                .stream()
                .map(leaveMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<LeaveBalanceResponse> initializeLeaveBalances(LeaveBalanceInitRequest request) {
        log.info("Initializing leave balances for employee: {} for year: {}",
                request.employeeId(), request.leaveYear());

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        List<LeaveBalance> balances = new ArrayList<>();
        balances.add(createOrUpdateBalance(employee, LeaveType.CASUAL, request.casualDays(), request.leaveYear()));
        balances.add(createOrUpdateBalance(employee, LeaveType.SICK, request.sickDays(), request.leaveYear()));
        balances.add(createOrUpdateBalance(employee, LeaveType.EARNED, request.earnedDays(), request.leaveYear()));

        List<LeaveBalance> saved = leaveBalanceRepository.saveAll(balances);
        return saved.stream().map(leaveMapper::toResponse).toList();
    }

    // ==================== Private Helpers ====================

    private LeaveBalance createOrUpdateBalance(Employee employee, LeaveType type, int totalDays, int year) {
        return leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeAndLeaveYear(employee.getId(), type, year)
                .map(existing -> {
                    existing.setTotalDays(totalDays);
                    existing.setRemainingDays(totalDays - existing.getUsedDays());
                    return existing;
                })
                .orElseGet(() -> LeaveBalance.builder()
                        .employee(employee)
                        .leaveType(type)
                        .totalDays(totalDays)
                        .usedDays(0)
                        .remainingDays(totalDays)
                        .leaveYear(year)
                        .build());
    }

    private Employee resolveTargetEmployee(Long requestEmployeeId, CustomUserDetails currentUser) {
        if (requestEmployeeId != null) {
            if (!hasPrivilegedRole(currentUser)) {
                Employee callerEmp = employeeRepository.findByUserId(currentUser.getId())
                        .orElseThrow(() -> new AccessDeniedException("Unauthorized to submit for other employees"));
                if (!callerEmp.getId().equals(requestEmployeeId)) {
                    throw new AccessDeniedException("You can only submit leave requests for yourself");
                }
            }
            return employeeRepository.findById(requestEmployeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", requestEmployeeId));
        }

        return employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new InvalidRequestException("Current user is not associated with an employee profile"));
    }

    private LeaveRequest findLeaveByIdOrThrow(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
    }

    private boolean isRequestOwner(LeaveRequest leave, CustomUserDetails user) {
        return leave.getEmployee().getUser() != null &&
               leave.getEmployee().getUser().getId().equals(user.getId());
    }

    private boolean hasPrivilegedRole(CustomUserDetails user) {
        return user.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals(AppConstants.ROLE_ADMIN) ||
                a.getAuthority().equals(AppConstants.ROLE_HR) ||
                a.getAuthority().equals(AppConstants.ROLE_MANAGER));
    }
}

