-- ============================================================
--  AttendAI  —  Demo Seed Data
--  All passwords are BCrypt hash of:  Password@123
--  Run this AFTER all services have started once so that
--  Hibernate has already created the schema.
-- ============================================================

-- ────────────────────────────────────────────────────────────
--  auth_db  ·  Users
-- ────────────────────────────────────────────────────────────
USE auth_db;

INSERT IGNORE INTO users
    (id, email, password, first_name, last_name, role, enabled, created_at, updated_at)
VALUES
    (1, 'admin@attendai.com',   '$2a$12$92p5ZGP4y0TBFxrfQUi5ieWf9VGNODGBFexSmCvWdZXRF0d8CXzIy', 'Alice',  'Admin',    'ADMIN',    true, NOW(), NOW()),
    (2, 'hr@attendai.com',      '$2a$12$92p5ZGP4y0TBFxrfQUi5ieWf9VGNODGBFexSmCvWdZXRF0d8CXzIy', 'Brian',  'HR',       'HR',       true, NOW(), NOW()),
    (3, 'manager@attendai.com', '$2a$12$92p5ZGP4y0TBFxrfQUi5ieWf9VGNODGBFexSmCvWdZXRF0d8CXzIy', 'Carol',  'Manager',  'MANAGER',  true, NOW(), NOW()),
    (4, 'emp1@attendai.com',    '$2a$12$92p5ZGP4y0TBFxrfQUi5ieWf9VGNODGBFexSmCvWdZXRF0d8CXzIy', 'David',  'Lee',      'EMPLOYEE', true, NOW(), NOW()),
    (5, 'emp2@attendai.com',    '$2a$12$92p5ZGP4y0TBFxrfQUi5ieWf9VGNODGBFexSmCvWdZXRF0d8CXzIy', 'Emma',   'Wilson',   'EMPLOYEE', true, NOW(), NOW()),
    (6, 'emp3@attendai.com',    '$2a$12$92p5ZGP4y0TBFxrfQUi5ieWf9VGNODGBFexSmCvWdZXRF0d8CXzIy', 'Frank',  'Zhang',    'EMPLOYEE', true, NOW(), NOW());


-- ────────────────────────────────────────────────────────────
--  employee_db  ·  Departments, Designations, Employees
-- ────────────────────────────────────────────────────────────
USE employee_db;

INSERT IGNORE INTO departments (id, name, description, active)
VALUES
    (1, 'Engineering',       'Software development and infrastructure',  true),
    (2, 'Human Resources',   'People operations and talent management',  true),
    (3, 'Finance',           'Accounting, payroll and financial planning', true),
    (4, 'Operations',        'Day-to-day business operations',           true);

INSERT IGNORE INTO designations (id, title, description, grade, active)
VALUES
    (1, 'Junior Developer',   'Entry-level software engineer', 'L1', true),
    (2, 'Senior Developer',   'Experienced software engineer', 'L3', true),
    (3, 'HR Manager',         'Manages HR processes',          'M1', true),
    (4, 'Department Head',    'Leads a business unit',         'M2', true),
    (5, 'Financial Analyst',  'Analyses financial data',       'L2', true);

INSERT IGNORE INTO employees
    (id, user_id, employee_code, first_name, last_name, email,
     joining_date, status, annual_leave_quota, department_id, designation_id)
VALUES
    (1, 1, 'EMP-001', 'Alice',  'Admin',   'admin@attendai.com',   '2022-01-01', 'ACTIVE', 24, 2, 4),
    (2, 2, 'EMP-002', 'Brian',  'HR',      'hr@attendai.com',      '2022-03-15', 'ACTIVE', 24, 2, 3),
    (3, 3, 'EMP-003', 'Carol',  'Manager', 'manager@attendai.com', '2022-06-01', 'ACTIVE', 24, 1, 4),
    (4, 4, 'EMP-004', 'David',  'Lee',     'emp1@attendai.com',    '2023-01-10', 'ACTIVE', 24, 1, 1),
    (5, 5, 'EMP-005', 'Emma',   'Wilson',  'emp2@attendai.com',    '2023-04-01', 'ACTIVE', 24, 1, 2),
    (6, 6, 'EMP-006', 'Frank',  'Zhang',   'emp3@attendai.com',    '2023-07-01', 'ACTIVE', 24, 3, 5);


-- ────────────────────────────────────────────────────────────
--  leave_db  ·  Leave types + balances
-- ────────────────────────────────────────────────────────────
USE leave_db;

INSERT IGNORE INTO leave_types
    (id, name, default_days, carry_over, allow_negative, requires_document, active)
VALUES
    (1, 'Annual Leave',       24,  true,  false, false, true),
    (2, 'Sick Leave',         10,  false, false, true,  true),
    (3, 'Casual Leave',       6,   false, false, false, true),
    (4, 'Maternity Leave',    90,  false, false, true,  true),
    (5, 'Paternity Leave',    14,  false, false, false, true),
    (6, 'Unpaid Leave',       30,  false, true,  false, true);

-- Seed leave balances for employees 4,5,6 for the current year
SET @yr = YEAR(NOW());

INSERT IGNORE INTO leave_balances
    (employee_id, leave_type_id, leave_year, allocated_days, used_days, pending_days)
VALUES
    -- David Lee
    (4, 1, @yr, 24, 2, 0),
    (4, 2, @yr, 10, 1, 0),
    (4, 3, @yr,  6, 0, 0),
    -- Emma Wilson
    (5, 1, @yr, 24, 5, 3),
    (5, 2, @yr, 10, 2, 0),
    (5, 3, @yr,  6, 1, 0),
    -- Frank Zhang
    (6, 1, @yr, 24, 0, 0),
    (6, 2, @yr, 10, 0, 1),
    (6, 3, @yr,  6, 0, 0);

-- Sample pending leave request (Frank — sick leave, tomorrow)
INSERT IGNORE INTO leave_requests
    (employee_id, leave_type_id, start_date, end_date, total_days, status, reason)
SELECT
    6,
    2,
    DATE_ADD(CURDATE(), INTERVAL 1 DAY),
    DATE_ADD(CURDATE(), INTERVAL 2 DAY),
    2,
    'PENDING',
    'Scheduled medical check-up'
WHERE NOT EXISTS (
    SELECT 1 FROM leave_requests WHERE employee_id = 6 AND status = 'PENDING'
);
