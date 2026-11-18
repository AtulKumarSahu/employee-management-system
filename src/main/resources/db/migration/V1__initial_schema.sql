-- ============================================================
-- V1__initial_schema.sql
-- Employee Management System — Initial Database Schema
--
-- Naming conventions:
--   Tables:  snake_case, plural (users, employees, departments)
--   Columns: snake_case
--   PKs:     id BIGSERIAL
--   FKs:     fk_<table>_<referenced_table>
--   Indexes: idx_<table>_<column(s)>
--   UQs:     uq_<table>_<column(s)>
--
-- Flyway runs this script exactly once and records it in
-- flyway_schema_history with a checksum. Do NOT modify this
-- file after it is applied — create a new V2__ script instead.
-- ============================================================

-- ==================== USERS ====================
CREATE TABLE users (
    id          BIGSERIAL       PRIMARY KEY,
    username    VARCHAR(100)    NOT NULL,
    email       VARCHAR(150)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    is_locked   BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,

    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_username ON users (username);

COMMENT ON TABLE  users              IS 'System authentication accounts';
COMMENT ON COLUMN users.username     IS 'Login username — unique across the system';
COMMENT ON COLUMN users.email        IS 'Login email — also used for notifications';
COMMENT ON COLUMN users.password     IS 'BCrypt-hashed password — never plaintext';
COMMENT ON COLUMN users.is_active    IS 'Inactive accounts cannot log in';
COMMENT ON COLUMN users.is_locked    IS 'Locked after too many failed attempts';


-- ==================== USER_ROLES (join table for roles collection) ====================
CREATE TABLE user_roles (
    user_id     BIGINT          NOT NULL,
    role        VARCHAR(20)     NOT NULL,

    CONSTRAINT pk_user_roles         PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_user_roles_role   CHECK (role IN ('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'))
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);

COMMENT ON TABLE user_roles IS 'User role assignments — a user may have multiple roles';


-- ==================== DEPARTMENTS ====================
-- Note: employees table references departments (department_id FK),
-- and departments references employees (manager_id FK).
-- This is a circular dependency. We handle it by:
--   1. Creating departments WITHOUT the manager_id FK first.
--   2. Creating employees with the department_id FK.
--   3. Adding the manager_id FK to departments afterward with ALTER TABLE.

CREATE TABLE departments (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    description TEXT,
    code        VARCHAR(20),
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    manager_id  BIGINT,         -- FK added after employees table (see ALTER TABLE below)
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,

    CONSTRAINT uq_departments_name UNIQUE (name),
    CONSTRAINT uq_departments_code UNIQUE (code)
);

CREATE INDEX idx_departments_name ON departments (name);

COMMENT ON TABLE  departments            IS 'Organizational departments';
COMMENT ON COLUMN departments.code       IS 'Short identifier like ENG, HR, FIN';
COMMENT ON COLUMN departments.manager_id IS 'The employee who manages this department';


-- ==================== EMPLOYEES ====================
CREATE TABLE employees (
    id                  BIGSERIAL       PRIMARY KEY,
    employee_code       VARCHAR(20)     NOT NULL,
    first_name          VARCHAR(50)     NOT NULL,
    last_name           VARCHAR(50)     NOT NULL,
    email               VARCHAR(150)    NOT NULL,
    phone_number        VARCHAR(20),
    date_of_birth       DATE,
    gender              VARCHAR(20),
    address             TEXT,
    designation         VARCHAR(100),
    joining_date        DATE            NOT NULL,
    salary              DECIMAL(15, 2),
    employment_status   VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    department_id       BIGINT,
    manager_id          BIGINT,         -- self-referential FK
    user_id             BIGINT,
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,

    CONSTRAINT uq_employees_code    UNIQUE (employee_code),
    CONSTRAINT uq_employees_email   UNIQUE (email),
    CONSTRAINT uq_employees_user    UNIQUE (user_id),

    CONSTRAINT fk_employees_department  FOREIGN KEY (department_id)
        REFERENCES departments (id) ON DELETE SET NULL,

    CONSTRAINT fk_employees_manager     FOREIGN KEY (manager_id)
        REFERENCES employees (id) ON DELETE SET NULL,

    CONSTRAINT fk_employees_user        FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE SET NULL,

    CONSTRAINT chk_employees_gender     CHECK (
        gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY')
    ),
    CONSTRAINT chk_employees_status     CHECK (
        employment_status IN ('ACTIVE', 'ON_LEAVE', 'SUSPENDED', 'TERMINATED')
    ),
    CONSTRAINT chk_employees_salary     CHECK (salary IS NULL OR salary >= 0)
);

-- Composite index for the common search + filter pattern
CREATE INDEX idx_emp_code        ON employees (employee_code);
CREATE INDEX idx_emp_email       ON employees (email);
CREATE INDEX idx_emp_department  ON employees (department_id);
CREATE INDEX idx_emp_manager     ON employees (manager_id);
CREATE INDEX idx_emp_status      ON employees (employment_status);
-- Index for soft-delete filter (WHERE is_deleted = false is added to every query)
CREATE INDEX idx_emp_not_deleted ON employees (is_deleted) WHERE is_deleted = FALSE;
-- Full-name search index
CREATE INDEX idx_emp_name        ON employees (first_name, last_name);

COMMENT ON TABLE  employees                  IS 'Employee HR profiles — soft-deleted, never hard-deleted';
COMMENT ON COLUMN employees.employee_code    IS 'Human-readable ID: EMP-000001';
COMMENT ON COLUMN employees.is_deleted       IS 'Soft-delete flag — true means deleted';
COMMENT ON COLUMN employees.manager_id       IS 'Self-referential FK for org hierarchy';


-- ==================== Circular FK — departments.manager_id → employees ====================
ALTER TABLE departments
    ADD CONSTRAINT fk_departments_manager
    FOREIGN KEY (manager_id) REFERENCES employees (id) ON DELETE SET NULL;

CREATE INDEX idx_departments_manager ON departments (manager_id);


-- ==================== REFRESH_TOKENS ====================
CREATE TABLE refresh_tokens (
    id          BIGSERIAL       PRIMARY KEY,
    token       VARCHAR(512)    NOT NULL,
    user_id     BIGINT          NOT NULL,
    expires_at  TIMESTAMPTZ     NOT NULL,
    is_revoked  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_refresh_token     UNIQUE (token),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_value  ON refresh_tokens (token);
CREATE INDEX idx_refresh_token_user   ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_token_expiry ON refresh_tokens (expires_at);

COMMENT ON TABLE refresh_tokens IS 'JWT refresh tokens — deleted on logout, expire automatically';


-- ==================== LEAVE_REQUESTS ====================
CREATE TABLE leave_requests (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL,
    leave_type      VARCHAR(20)     NOT NULL,
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    total_days      INTEGER         NOT NULL,
    reason          TEXT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    reviewed_by     BIGINT,
    reviewed_at     TIMESTAMP,
    reviewer_notes  TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,

    CONSTRAINT fk_leave_req_employee    FOREIGN KEY (employee_id)
        REFERENCES employees (id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_req_reviewer    FOREIGN KEY (reviewed_by)
        REFERENCES users (id) ON DELETE SET NULL,

    CONSTRAINT chk_leave_req_type       CHECK (
        leave_type IN ('CASUAL', 'SICK', 'EARNED', 'UNPAID')
    ),
    CONSTRAINT chk_leave_req_status     CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')
    ),
    CONSTRAINT chk_leave_req_dates      CHECK (end_date >= start_date),
    CONSTRAINT chk_leave_req_days       CHECK (total_days > 0)
);

CREATE INDEX idx_leave_emp      ON leave_requests (employee_id);
CREATE INDEX idx_leave_status   ON leave_requests (status);
CREATE INDEX idx_leave_type     ON leave_requests (leave_type);
CREATE INDEX idx_leave_dates    ON leave_requests (start_date, end_date);

COMMENT ON TABLE leave_requests IS 'Employee leave requests and their approval status';


-- ==================== LEAVE_BALANCES ====================
CREATE TABLE leave_balances (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL,
    leave_type      VARCHAR(20)     NOT NULL,
    total_days      INTEGER         NOT NULL DEFAULT 0,
    used_days       INTEGER         NOT NULL DEFAULT 0,
    remaining_days  INTEGER         NOT NULL DEFAULT 0,
    leave_year      INTEGER         NOT NULL,
    updated_at      TIMESTAMP,

    CONSTRAINT uq_leave_balance_emp_type UNIQUE (employee_id, leave_type, leave_year),

    CONSTRAINT fk_leave_balance_employee FOREIGN KEY (employee_id)
        REFERENCES employees (id) ON DELETE CASCADE,

    CONSTRAINT chk_leave_balance_type   CHECK (
        leave_type IN ('CASUAL', 'SICK', 'EARNED', 'UNPAID')
    ),
    CONSTRAINT chk_leave_balance_days   CHECK (
        total_days >= 0 AND used_days >= 0 AND remaining_days >= 0
    ),
    CONSTRAINT chk_leave_balance_math   CHECK (remaining_days = total_days - used_days)
);

CREATE INDEX idx_leave_balance_emp ON leave_balances (employee_id);

COMMENT ON TABLE leave_balances IS 'Per-employee per-year leave balance tracking';


-- ==================== ATTENDANCE ====================
CREATE TABLE attendance (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL,
    attendance_date DATE            NOT NULL,
    check_in_time   TIME,
    check_out_time  TIME,
    working_hours   DECIMAL(4, 2),
    status          VARCHAR(20)     NOT NULL DEFAULT 'ABSENT',
    is_late         BOOLEAN         NOT NULL DEFAULT FALSE,
    notes           TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,

    CONSTRAINT uq_attendance_emp_date   UNIQUE (employee_id, attendance_date),

    CONSTRAINT fk_attendance_employee   FOREIGN KEY (employee_id)
        REFERENCES employees (id) ON DELETE CASCADE,

    CONSTRAINT chk_attendance_status    CHECK (
        status IN ('PRESENT', 'ABSENT', 'HALF_DAY', 'LATE')
    ),
    CONSTRAINT chk_attendance_hours     CHECK (
        working_hours IS NULL OR working_hours >= 0
    )
);

CREATE INDEX idx_attendance_employee  ON attendance (employee_id);
CREATE INDEX idx_attendance_date      ON attendance (attendance_date);
CREATE INDEX idx_attendance_emp_date  ON attendance (employee_id, attendance_date);

COMMENT ON TABLE attendance IS 'Daily employee attendance records';

-- ============================================================
-- End of V1__initial_schema.sql
-- ============================================================

