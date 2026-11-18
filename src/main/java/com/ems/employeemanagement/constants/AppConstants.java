package com.ems.employeemanagement.constants;

/**
 * Application-wide string constants.
 *
 * <p>Using an interface with constants is a classic Java pattern.
 * Alternatively, a final class with private constructor is preferred
 * per Effective Java (Item 4). We use the latter here.
 *
 * <p>Benefits of centralizing constants:
 * <ul>
 *   <li>Single source of truth — change one place, affects everywhere</li>
 *   <li>Avoid magic strings scattered across the codebase</li>
 *   <li>IDE navigation and refactoring support</li>
 * </ul>
 */
public final class AppConstants {

    // Prevent instantiation of this utility class
    private AppConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== API ====================

    /** Base path prefix for all API endpoints. */
    public static final String API_BASE_PATH = "/api/v1";

    // ==================== Pagination Defaults ====================

    /** Default page number (0-based) for paginated endpoints. */
    public static final int DEFAULT_PAGE_NUMBER = 0;

    /** Default number of records per page. */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** Maximum number of records per page to prevent memory issues. */
    public static final int MAX_PAGE_SIZE = 100;

    /** Default sort field for employee listings. */
    public static final String DEFAULT_SORT_BY = "createdAt";

    /** Default sort direction. */
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // ==================== Security ====================

    /** Authorization header name in HTTP requests. */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** Prefix for Bearer token in Authorization header. */
    public static final String BEARER_PREFIX = "Bearer ";

    /** Length of "Bearer " prefix — used to extract raw JWT token. */
    public static final int BEARER_PREFIX_LENGTH = 7;

    // ==================== Roles ====================

    /** Role name constants used in @PreAuthorize and security config. */
    public static final String ROLE_ADMIN    = "ROLE_ADMIN";
    public static final String ROLE_HR       = "ROLE_HR";
    public static final String ROLE_MANAGER  = "ROLE_MANAGER";
    public static final String ROLE_EMPLOYEE = "ROLE_EMPLOYEE";

    // ==================== Date / Time ====================

    /** Standard date format used throughout the application. */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /** Standard date-time format for API responses. */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    // ==================== Employee Code ====================

    /** Prefix used when generating employee codes (e.g., EMP-001234). */
    public static final String EMPLOYEE_CODE_PREFIX = "EMP-";

    // ==================== Attendance Configuration ====================

    /** Standard shift start hour (09:00 AM). */
    public static final int SHIFT_START_HOUR = 9;

    /** Standard shift start minute. */
    public static final int SHIFT_START_MINUTE = 0;

    /** Grace period in minutes after shift start before an employee is marked late (e.g. 15 mins -> 09:15). */
    public static final int LATE_GRACE_PERIOD_MINUTES = 15;

    /** Minimum working hours required to qualify for a full day (8.0 hours). */
    public static final double MIN_FULL_DAY_HOURS = 8.0;

    /** Minimum working hours required to qualify for half day (4.0 hours). */
    public static final double MIN_HALF_DAY_HOURS = 4.0;

    // ==================== Validation Messages ====================

    public static final String EMAIL_REQUIRED        = "Email address is required";
    public static final String EMAIL_INVALID         = "Invalid email address format";
    public static final String PASSWORD_REQUIRED     = "Password is required";
    public static final String PASSWORD_MIN_LENGTH   = "Password must be at least 8 characters";
    public static final String FIRST_NAME_REQUIRED   = "First name is required";
    public static final String LAST_NAME_REQUIRED    = "Last name is required";
    public static final String PHONE_INVALID         = "Invalid phone number format";
    public static final String SALARY_POSITIVE       = "Salary must be a positive value";
}

