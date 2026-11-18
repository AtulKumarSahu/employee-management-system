package com.ems.employeemanagement.controller;

import com.ems.employeemanagement.constants.AppConstants;
import com.ems.employeemanagement.dto.request.CheckInRequest;
import com.ems.employeemanagement.dto.request.CheckOutRequest;
import com.ems.employeemanagement.dto.request.ManualAttendanceRequest;
import com.ems.employeemanagement.dto.response.ApiResponse;
import com.ems.employeemanagement.dto.response.AttendanceResponse;
import com.ems.employeemanagement.dto.response.AttendanceSummaryResponse;
import com.ems.employeemanagement.dto.response.PaginationResponse;
import com.ems.employeemanagement.security.CustomUserDetails;
import com.ems.employeemanagement.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller exposing endpoints for daily attendance check-in, check-out,
 * and reporting.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/attendance")
@Slf4j
@Tag(name = "Attendance Management", description = "Endpoints for employee check-in, check-out, working hours, and monthly summaries")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * POST /api/v1/attendance/check-in — Check-in for current day.
     */
    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    @Operation(summary = "Employee check-in", description = "Records daily check-in timestamp and assesses late arrival status")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
            @RequestBody(required = false) CheckInRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.info("REST request for check-in by user: {}", currentUser.getUsername());
        AttendanceResponse response = attendanceService.checkIn(request, currentUser);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Check-in successful", response));
    }

    /**
     * POST /api/v1/attendance/check-out — Check-out for current day.
     */
    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    @Operation(summary = "Employee check-out", description = "Records daily check-out timestamp, calculates working hours, and sets attendance status")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(
            @RequestBody(required = false) CheckOutRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.info("REST request for check-out by user: {}", currentUser.getUsername());
        AttendanceResponse response = attendanceService.checkOut(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Check-out successful", response));
    }

    /**
     * GET /api/v1/attendance/me — Get today's attendance record for current user.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    @Operation(summary = "Get today's attendance", description = "Retrieves the check-in and check-out status of the authenticated employee for today")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getTodayAttendance(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        AttendanceResponse response = attendanceService.getTodayAttendance(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Today's attendance retrieved", response));
    }

    /**
     * GET /api/v1/attendance/my-history — Get current user's attendance records
     * (paginated).
     */
    @GetMapping("/my-history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    @Operation(summary = "Get personal attendance history", description = "Returns a paginated list of attendance entries for the current user")
    public ResponseEntity<ApiResponse<PaginationResponse<AttendanceResponse>>> getMyAttendance(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("attendanceDate").descending());
        Page<AttendanceResponse> pageResult = attendanceService.getMyAttendance(currentUser, pageable);
        return ResponseEntity
                .ok(ApiResponse.success("Attendance history retrieved", PaginationResponse.from(pageResult)));
    }

    /**
     * GET /api/v1/attendance/summary — Monthly attendance summary report.
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    @Operation(summary = "Get monthly attendance summary", description = "Aggregates total days present, late, half-day, absent, and total working hours")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> getMonthlySummary(
            @Parameter(description = "Target employee ID (optional for self)") @RequestParam(required = false) Long employeeId,
            @Parameter(description = "Year (defaults to current year)") @RequestParam(required = false) Integer year,
            @Parameter(description = "Month 1-12 (defaults to current month)") @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        AttendanceSummaryResponse summary = attendanceService.getMonthlySummary(employeeId, year, month, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Monthly attendance summary retrieved", summary));
    }

    /**
     * GET /api/v1/attendance — List attendance records across all employees
     * (Admin/HR/Manager).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "List attendance records", description = "Filtered by optional employee ID and date")
    public ResponseEntity<ApiResponse<PaginationResponse<AttendanceResponse>>> getAllAttendance(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("attendanceDate").descending());
        Page<AttendanceResponse> pageResult = attendanceService.getAllAttendance(employeeId, date, pageable);
        return ResponseEntity
                .ok(ApiResponse.success("Attendance records retrieved", PaginationResponse.from(pageResult)));
    }

    /**
     * POST /api/v1/attendance/manual — Manual attendance creation or correction by
     * HR/Admin.
     */
    @PostMapping("/manual")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Manual attendance entry", description = "Allows HR/Admin to record or adjust attendance for past dates")
    public ResponseEntity<ApiResponse<AttendanceResponse>> recordManualAttendance(
            @Valid @RequestBody ManualAttendanceRequest request) {

        AttendanceResponse response = attendanceService.recordManualAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Manual attendance entry saved", response));
    }
}
