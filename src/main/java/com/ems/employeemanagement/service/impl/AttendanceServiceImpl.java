package com.ems.employeemanagement.service.impl;

import com.ems.employeemanagement.constants.AppConstants;
import com.ems.employeemanagement.dto.request.CheckInRequest;
import com.ems.employeemanagement.dto.request.CheckOutRequest;
import com.ems.employeemanagement.dto.request.ManualAttendanceRequest;
import com.ems.employeemanagement.dto.response.AttendanceResponse;
import com.ems.employeemanagement.dto.response.AttendanceSummaryResponse;
import com.ems.employeemanagement.entity.Attendance;
import com.ems.employeemanagement.entity.Employee;
import com.ems.employeemanagement.enums.AttendanceStatus;
import com.ems.employeemanagement.exception.AccessDeniedException;
import com.ems.employeemanagement.exception.InvalidRequestException;
import com.ems.employeemanagement.exception.ResourceNotFoundException;
import com.ems.employeemanagement.mapper.AttendanceMapper;
import com.ems.employeemanagement.repository.AttendanceRepository;
import com.ems.employeemanagement.repository.EmployeeRepository;
import com.ems.employeemanagement.security.CustomUserDetails;
import com.ems.employeemanagement.service.AttendanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link AttendanceService}.
 */
@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository   employeeRepository;
    private final AttendanceMapper     attendanceMapper;

    public AttendanceServiceImpl(
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            AttendanceMapper attendanceMapper) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository   = employeeRepository;
        this.attendanceMapper     = attendanceMapper;
    }

    @Override
    @Transactional
    public AttendanceResponse checkIn(CheckInRequest request, CustomUserDetails currentUser) {
        Employee employee = resolveTargetEmployee(request != null ? request.employeeId() : null, currentUser);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        log.info("Processing check-in for employee id: {} on date: {}", employee.getId(), today);

        Optional<Attendance> existingOpt = attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), today);
        if (existingOpt.isPresent() && existingOpt.get().getCheckInTime() != null) {
            throw new InvalidRequestException(
                    "Employee has already checked in today at " + existingOpt.get().getCheckInTime());
        }

        // Evaluate late arrival: shift start (09:00) + grace (15 mins) = 09:15
        LocalTime lateCutoff = LocalTime.of(AppConstants.SHIFT_START_HOUR, AppConstants.SHIFT_START_MINUTE)
                .plusMinutes(AppConstants.LATE_GRACE_PERIOD_MINUTES);
        boolean isLate = now.isAfter(lateCutoff);
        AttendanceStatus status = isLate ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;

        String notes = (request != null && request.notes() != null) ? request.notes() : null;

        Attendance attendance = existingOpt.orElseGet(() -> Attendance.builder()
                .employee(employee)
                .attendanceDate(today)
                .build());

        attendance.setCheckInTime(now);
        attendance.setStatus(status);
        attendance.setLate(isLate);
        if (notes != null) {
            attendance.setNotes(notes);
        }

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Check-in recorded: employee={}, time={}, late={}", employee.getId(), now, isLate);

        return attendanceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(CheckOutRequest request, CustomUserDetails currentUser) {
        Employee employee = resolveTargetEmployee(request != null ? request.employeeId() : null, currentUser);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        log.info("Processing check-out for employee id: {} on date: {}", employee.getId(), today);

        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), today)
                .orElseThrow(() -> new InvalidRequestException("No check-in record found for today. Please check in first."));

        if (attendance.getCheckInTime() == null) {
            throw new InvalidRequestException("No check-in timestamp found. Please check in first.");
        }
        if (attendance.getCheckOutTime() != null) {
            throw new InvalidRequestException("Employee has already checked out today at " + attendance.getCheckOutTime());
        }
        if (now.isBefore(attendance.getCheckInTime())) {
            throw new InvalidRequestException("Check-out time cannot be earlier than check-in time");
        }

        attendance.setCheckOutTime(now);

        // Calculate working hours
        Duration duration = Duration.between(attendance.getCheckInTime(), now);
        double hours = duration.toMinutes() / 60.0;
        double roundedHours = Math.round(hours * 100.0) / 100.0;
        attendance.setWorkingHours(roundedHours);

        // Derive final status based on total working hours
        if (roundedHours < AppConstants.MIN_HALF_DAY_HOURS) {
            attendance.setStatus(AttendanceStatus.ABSENT);
        } else if (roundedHours < AppConstants.MIN_FULL_DAY_HOURS) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        } else {
            attendance.setStatus(attendance.isLate() ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);
        }

        if (request != null && request.notes() != null && !request.notes().isBlank()) {
            String existingNotes = attendance.getNotes() != null ? attendance.getNotes() + " | " : "";
            attendance.setNotes(existingNotes + request.notes());
        }

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Check-out recorded: employee={}, time={}, hours={}, status={}",
                employee.getId(), now, roundedHours, saved.getStatus());

        return attendanceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getTodayAttendance(CustomUserDetails currentUser) {
        Employee employee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No employee profile found for user"));

        return attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), LocalDate.now())
                .map(attendanceMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getMyAttendance(CustomUserDetails currentUser, Pageable pageable) {
        Employee employee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No employee profile found for user"));

        return attendanceRepository.findByEmployeeId(employee.getId(), pageable)
                .map(attendanceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getMonthlySummary(
            Long employeeId, Integer year, Integer month, CustomUserDetails currentUser) {

        Employee employee;
        if (employeeId != null) {
            if (!hasPrivilegedRole(currentUser)) {
                Employee callerEmp = employeeRepository.findByUserId(currentUser.getId())
                        .orElseThrow(() -> new AccessDeniedException("Unauthorized to view other employee summaries"));
                if (!callerEmp.getId().equals(employeeId)) {
                    throw new AccessDeniedException("You are only permitted to view your own attendance summary");
                }
            }
            employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        } else {
            employee = employeeRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("No employee profile found for current user"));
        }

        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();

        LocalDate from = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        List<Attendance> records = attendanceRepository.findByEmployeeIdAndDateRange(employee.getId(), from, to);

        long presentCount = 0;
        long lateCount = 0;
        long halfDayCount = 0;
        long absentCount = 0;
        double totalHours = 0.0;

        for (Attendance a : records) {
            if (a.getStatus() == AttendanceStatus.PRESENT) {
                presentCount++;
            } else if (a.getStatus() == AttendanceStatus.LATE || a.isLate()) {
                lateCount++;
            } else if (a.getStatus() == AttendanceStatus.HALF_DAY) {
                halfDayCount++;
            } else if (a.getStatus() == AttendanceStatus.ABSENT) {
                absentCount++;
            }

            if (a.getWorkingHours() != null) {
                totalHours += a.getWorkingHours();
            }
        }

        double roundedTotalHours = Math.round(totalHours * 100.0) / 100.0;
        long workingDays = presentCount + lateCount + halfDayCount;
        double avgHours = workingDays > 0 ? (totalHours / workingDays) : 0.0;
        double roundedAvgHours = Math.round(avgHours * 100.0) / 100.0;

        return new AttendanceSummaryResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                targetYear,
                targetMonth,
                records.size(),
                presentCount,
                lateCount,
                halfDayCount,
                absentCount,
                roundedTotalHours,
                roundedAvgHours
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAllAttendance(Long employeeId, LocalDate date, Pageable pageable) {
        if (employeeId != null && date != null) {
            Optional<Attendance> attendanceOpt = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, date);
            if (attendanceOpt.isPresent()) {
                List<AttendanceResponse> list = List.of(attendanceMapper.toResponse(attendanceOpt.get()));
                return new org.springframework.data.domain.PageImpl<>(list, pageable, 1);
            }
            return org.springframework.data.domain.Page.empty(pageable);
        } else if (employeeId != null) {
            return attendanceRepository.findByEmployeeId(employeeId, pageable)
                    .map(attendanceMapper::toResponse);
        } else {
            return attendanceRepository.findAll(pageable)
                    .map(attendanceMapper::toResponse);
        }
    }

    @Override
    @Transactional
    public AttendanceResponse recordManualAttendance(ManualAttendanceRequest request) {
        log.info("Recording manual attendance for employee: {} on date: {}",
                request.employeeId(), request.attendanceDate());

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employee.getId(), request.attendanceDate())
                .orElseGet(() -> Attendance.builder()
                        .employee(employee)
                        .attendanceDate(request.attendanceDate())
                        .build());

        attendance.setCheckInTime(request.checkInTime());
        attendance.setCheckOutTime(request.checkOutTime());

        Double workingHours = null;
        if (request.checkInTime() != null && request.checkOutTime() != null) {
            Duration duration = Duration.between(request.checkInTime(), request.checkOutTime());
            double hours = duration.toMinutes() / 60.0;
            workingHours = Math.round(hours * 100.0) / 100.0;
        }
        attendance.setWorkingHours(workingHours);

        // Derive or override status
        AttendanceStatus status = request.status();
        if (status == null) {
            if (workingHours == null || workingHours < AppConstants.MIN_HALF_DAY_HOURS) {
                status = AttendanceStatus.ABSENT;
            } else if (workingHours < AppConstants.MIN_FULL_DAY_HOURS) {
                status = AttendanceStatus.HALF_DAY;
            } else {
                status = AttendanceStatus.PRESENT;
            }
        }
        attendance.setStatus(status);

        if (request.checkInTime() != null) {
            LocalTime lateCutoff = LocalTime.of(AppConstants.SHIFT_START_HOUR, AppConstants.SHIFT_START_MINUTE)
                    .plusMinutes(AppConstants.LATE_GRACE_PERIOD_MINUTES);
            attendance.setLate(request.checkInTime().isAfter(lateCutoff));
        }

        if (request.notes() != null) {
            attendance.setNotes(request.notes());
        }

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Manual attendance saved: id={}, status={}", saved.getId(), saved.getStatus());

        return attendanceMapper.toResponse(saved);
    }

    // ==================== Private Helpers ====================

    private Employee resolveTargetEmployee(Long requestEmployeeId, CustomUserDetails currentUser) {
        if (requestEmployeeId != null) {
            if (!hasPrivilegedRole(currentUser)) {
                Employee callerEmp = employeeRepository.findByUserId(currentUser.getId())
                        .orElseThrow(() -> new AccessDeniedException("Unauthorized to submit for other employees"));
                if (!callerEmp.getId().equals(requestEmployeeId)) {
                    throw new AccessDeniedException("You can only record attendance for yourself");
                }
            }
            return employeeRepository.findById(requestEmployeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", requestEmployeeId));
        }

        return employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new InvalidRequestException("Current user is not associated with an employee profile"));
    }

    private boolean hasPrivilegedRole(CustomUserDetails user) {
        return user.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals(AppConstants.ROLE_ADMIN) ||
                a.getAuthority().equals(AppConstants.ROLE_HR) ||
                a.getAuthority().equals(AppConstants.ROLE_MANAGER));
    }
}

