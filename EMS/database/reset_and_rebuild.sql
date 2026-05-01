-- ============================================================
--  EMS  –  Full Reset & Rebuild
--  Run this in phpMyAdmin to fix all "unknown column" errors.
--  WARNING: This deletes all existing data and rebuilds clean.
-- ============================================================

USE ems_db;

-- ── Step 1: Drop all tables (order matters for FK constraints) ─
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS payroll;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS departments;
SET FOREIGN_KEY_CHECKS = 1;

-- ── Step 2: Recreate departments ──────────────────────────────
CREATE TABLE departments (
    id   INT          AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- ── Step 3: Recreate users ────────────────────────────────────
CREATE TABLE users (
    id       INT          AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     ENUM('ADMIN','EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE'
);

-- ── Step 4: Recreate employees ────────────────────────────────
CREATE TABLE employees (
    id           INT          AUTO_INCREMENT PRIMARY KEY,
    user_id      INT          NOT NULL UNIQUE,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    phone        VARCHAR(20)  DEFAULT NULL,
    address      VARCHAR(255) DEFAULT NULL,
    dept_id      INT          DEFAULT NULL,
    joining_date DATE         DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)  ON DELETE CASCADE,
    FOREIGN KEY (dept_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- ── Step 5: Recreate payroll ──────────────────────────────────
CREATE TABLE payroll (
    id         INT            AUTO_INCREMENT PRIMARY KEY,
    emp_id     INT            NOT NULL,
    pay_month  VARCHAR(20)    NOT NULL,
    salary     DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    bonus      DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    deduction  DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    net_salary DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    FOREIGN KEY (emp_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ── Step 6: Recreate attendance ───────────────────────────────
CREATE TABLE attendance (
    id       INT  AUTO_INCREMENT PRIMARY KEY,
    emp_id   INT  NOT NULL,
    att_date DATE NOT NULL,
    status   ENUM('PRESENT','ABSENT','HALF_DAY','LEAVE') NOT NULL DEFAULT 'PRESENT',
    UNIQUE KEY uq_emp_date (emp_id, att_date),
    FOREIGN KEY (emp_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ── Step 7: Seed departments ──────────────────────────────────
INSERT INTO departments (name) VALUES
    ('Engineering'),
    ('Human Resources'),
    ('Finance'),
    ('Marketing'),
    ('Operations');

-- ── Step 8: Seed admin user  (password = "admin123") ─────────
INSERT INTO users (username, password, role) VALUES (
    'admin',
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    'ADMIN'
);

-- ── Step 9: Verify ────────────────────────────────────────────
SELECT '=== TABLES ===' AS info;
SHOW TABLES;

SELECT '=== EMPLOYEES COLUMNS ===' AS info;
DESCRIBE employees;

SELECT '=== USERS ===' AS info;
SELECT id, username, role, LENGTH(password) AS hash_len FROM users;

SELECT '=== DEPARTMENTS ===' AS info;
SELECT * FROM departments;