-- ============================================================
--  EMS Migration Script  –  run this in phpMyAdmin SQL tab
--  Safe to run multiple times on an existing ems_db database.
-- ============================================================

USE ems_db;

-- ── Ensure departments table exists ──────────────────────────
CREATE TABLE IF NOT EXISTS departments (
    id   INT          AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- ── Ensure users table exists ─────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id       INT          AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     ENUM('ADMIN','EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE'
);

-- ── Ensure employees table exists with ALL columns ───────────
CREATE TABLE IF NOT EXISTS employees (
    id           INT          AUTO_INCREMENT PRIMARY KEY,
    user_id      INT          DEFAULT NULL,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL,
    phone        VARCHAR(20),
    address      VARCHAR(255),
    dept_id      INT,
    joining_date DATE         DEFAULT NULL
);

-- Add missing columns to employees if upgrading from old schema
ALTER TABLE employees ADD COLUMN IF NOT EXISTS user_id      INT  DEFAULT NULL;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS joining_date DATE DEFAULT NULL;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS phone        VARCHAR(20)  DEFAULT NULL;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS address      VARCHAR(255) DEFAULT NULL;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS dept_id      INT  DEFAULT NULL;

-- ── Ensure payroll table exists ───────────────────────────────
CREATE TABLE IF NOT EXISTS payroll (
    id         INT            AUTO_INCREMENT PRIMARY KEY,
    emp_id     INT            NOT NULL,
    pay_month  VARCHAR(20)    NOT NULL,
    salary     DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    bonus      DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    deduction  DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    net_salary DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    FOREIGN KEY (emp_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ── Ensure attendance table exists ───────────────────────────
CREATE TABLE IF NOT EXISTS attendance (
    id       INT  AUTO_INCREMENT PRIMARY KEY,
    emp_id   INT  NOT NULL,
    att_date DATE NOT NULL,
    status   ENUM('PRESENT','ABSENT','HALF_DAY','LEAVE') NOT NULL DEFAULT 'PRESENT',
    UNIQUE KEY uq_emp_date (emp_id, att_date),
    FOREIGN KEY (emp_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ── Seed departments ──────────────────────────────────────────
INSERT IGNORE INTO departments (name) VALUES
    ('Engineering'),
    ('Human Resources'),
    ('Finance'),
    ('Marketing'),
    ('Operations');

-- ── Seed admin user (password = "admin123") ───────────────────
INSERT IGNORE INTO users (username, password, role) VALUES
    ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN');

-- ── Verify everything ─────────────────────────────────────────
SHOW TABLES;
DESCRIBE employees;
SELECT id, username, role FROM users;