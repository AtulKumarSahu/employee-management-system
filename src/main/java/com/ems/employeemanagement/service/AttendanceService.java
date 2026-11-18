package com.ems.employeemanagement.service;

import com.ems.employeemanagement.dto.request.CheckInRequest;
import com.ems.employeemanagement.dto.request.CheckOutRequest;
import com.ems.employeemanagement.dto.request.ManualAttendanceRequest;
import com.ems.employeemanagement.dto.response.AttendanceResponse;
import com.ems.employeemanagement.dto.response.AttendanceSummaryResponse;
import com.ems.employeemanagement.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Service contract for managing daily employee attendance records and metrics.
 */
public interface AttendanceService {

    /**
     * Records check-in for the current day with late arrival detection.
     */
    AttendanceResponse checkIn(CheckInRequest request, CustomUserDetails currentUser);

    /**
     * Records check-out for the current day and computes working hours and final status.
     */
    AttendanceResponse checkOut(CheckOutRequest request, CustomUserDetails currentUser);

    /**
     * Retrieves today's attendance record for the current user.
     */
    AttendanceResponse getTodayAttendance(CustomUserDetails currentUser);

    /**
     * Retrieves attendance history for the currently authenticated employee.
     */
    Page<AttendanceResponse> getMyAttendance(CustomUserDetails currentUser, Pageable pageable);

    /**
     * Computes monthly attendance metrics (present, late, half-day, absent, work hours).
     */
    AttendanceSummaryResponse getMonthlySummary(Long employeeId, Integer year, Integer month, CustomUserDetails currentUser);

    /**
     * Lists attendance records filtered by optional employee and date (paginated).
     */
    Page<AttendanceResponse> getAllAttendance(Long employeeId, LocalDate date, Pageable pageable);

    /**
     * Manually records or modifies attendance records (restricted to HR / Admin).
     */
    AttendanceResponse recordManualAttendance(ManualAttendanceRequest request);
}

