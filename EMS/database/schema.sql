-- ============================================================
--  Employee Management System – Database Schema (MAMP/MySQL)
-- ============================================================

CREATE DATABASE IF NOT EXISTS ems_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ems_db;

-- ── Departments ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS departments (
    id   INT          AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- ── Users (authentication) ───────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id       INT          AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,          -- SHA-256 hex stored
    role     ENUM('ADMIN','EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE'
);

-- ── Employees ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS employees (
    id          INT          AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    phone       VARCHAR(20),
    address     VARCHAR(255),
    dept_id     INT,
    joining_date DATE         DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)  ON DELETE CASCADE,
    FOREIGN KEY (dept_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- ── Payroll ──────────────────────────────────────────────────
-- net_salary is stored as a plain column and calculated in Java
-- (avoids GENERATED ALWAYS AS which requires MySQL 5.7.6+)
CREATE TABLE IF NOT EXISTS payroll (
    id          INT            AUTO_INCREMENT PRIMARY KEY,
    emp_id      INT            NOT NULL,
    pay_month   VARCHAR(20)    NOT NULL,
    salary      DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    bonus       DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    deduction   DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    net_salary  DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    FOREIGN KEY (emp_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ── Attendance ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS attendance (
    id         INT  AUTO_INCREMENT PRIMARY KEY,
    emp_id     INT  NOT NULL,
    att_date   DATE NOT NULL,
    status     ENUM('PRESENT','ABSENT','HALF_DAY','LEAVE') NOT NULL DEFAULT 'PRESENT',
    UNIQUE KEY uq_emp_date (emp_id, att_date),
    FOREIGN KEY (emp_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ── Seed data ────────────────────────────────────────────────
-- Departments
INSERT IGNORE INTO departments (name) VALUES
    ('Engineering'),
    ('Human Resources'),
    ('Finance'),
    ('Marketing'),
    ('Operations');

-- Admin user  (password = "admin123"  → SHA-256)
INSERT IGNORE INTO users (username, password, role) VALUES
    ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN');

-- ── Migration: add joining_date if upgrading from old schema ─
-- Safe to run even if column already exists (MySQL ignores IF NOT EXISTS on ALTER)
ALTER TABLE employees ADD COLUMN IF NOT EXISTS joining_date DATE DEFAULT NULL;