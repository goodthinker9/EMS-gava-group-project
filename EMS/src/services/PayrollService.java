package services;

import database.DBConnection;
import models.Payroll;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD operations for the `payroll` table.
 */
public class PayrollService {

    // ── Shared row mapper ─────────────────────────────────────

    private Payroll mapRow(ResultSet rs) throws SQLException {
        return new Payroll(
                rs.getInt("id"),
                rs.getInt("emp_id"),
                rs.getString("emp_name"),
                rs.getString("pay_month"),
                rs.getBigDecimal("salary"),
                rs.getBigDecimal("bonus"),
                rs.getBigDecimal("deduction"),
                rs.getBigDecimal("net_salary")
        );
    }

    // ── Queries ───────────────────────────────────────────────

    /** Returns all payroll records with employee names. */
    public List<Payroll> getAllPayroll() throws SQLException {
        List<Payroll> list = new ArrayList<>();
        String sql = """
                SELECT p.id, p.emp_id, e.name AS emp_name,
                       p.pay_month, p.salary, p.bonus, p.deduction, p.net_salary
                FROM payroll p
                JOIN employees e ON p.emp_id = e.id
                ORDER BY p.pay_month DESC, e.name
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Returns payroll records for a specific employee. */
    public List<Payroll> getPayrollByEmployee(int empId) throws SQLException {
        List<Payroll> list = new ArrayList<>();
        String sql = """
                SELECT p.id, p.emp_id, e.name AS emp_name,
                       p.pay_month, p.salary, p.bonus, p.deduction, p.net_salary
                FROM payroll p
                JOIN employees e ON p.emp_id = e.id
                WHERE p.emp_id = ?
                ORDER BY p.pay_month DESC
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Returns a single payroll record by ID. */
    public Payroll getPayrollById(int id) throws SQLException {
        String sql = """
                SELECT p.id, p.emp_id, e.name AS emp_name,
                       p.pay_month, p.salary, p.bonus, p.deduction, p.net_salary
                FROM payroll p
                JOIN employees e ON p.emp_id = e.id
                WHERE p.id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ── Mutations ─────────────────────────────────────────────

    /** Inserts a new payroll record and returns the generated ID. */
    public int addPayroll(int empId, String payMonth,
                          BigDecimal salary, BigDecimal bonus,
                          BigDecimal deduction) throws SQLException {
        // Calculate net_salary in Java (salary + bonus - deduction)
        BigDecimal netSalary = salary.add(bonus).subtract(deduction);

        String sql = """
                INSERT INTO payroll (emp_id, pay_month, salary, bonus, deduction, net_salary)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, empId);
            ps.setString(2, payMonth.trim());
            ps.setBigDecimal(3, salary);
            ps.setBigDecimal(4, bonus);
            ps.setBigDecimal(5, deduction);
            ps.setBigDecimal(6, netSalary);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Payroll insert failed.");
    }

    /** Updates an existing payroll record. */
    public void updatePayroll(int id, String payMonth,
                              BigDecimal salary, BigDecimal bonus,
                              BigDecimal deduction) throws SQLException {
        // Recalculate net_salary in Java
        BigDecimal netSalary = salary.add(bonus).subtract(deduction);

        String sql = """
                UPDATE payroll
                SET pay_month = ?, salary = ?, bonus = ?, deduction = ?, net_salary = ?
                WHERE id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, payMonth.trim());
            ps.setBigDecimal(2, salary);
            ps.setBigDecimal(3, bonus);
            ps.setBigDecimal(4, deduction);
            ps.setBigDecimal(5, netSalary);
            ps.setInt(6, id);
            ps.executeUpdate();
        }
    }

    /** Deletes a payroll record. */
    public void deletePayroll(int id) throws SQLException {
        String sql = "DELETE FROM payroll WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── Analytics queries ─────────────────────────────────────

    /**
     * Returns total net salary expense grouped by pay_month.
     * Result: LinkedHashMap of { "April 2026" -> 15000.00 }
     */
    public java.util.LinkedHashMap<String, java.math.BigDecimal> getMonthlySalaryExpense() throws SQLException {
        java.util.LinkedHashMap<String, java.math.BigDecimal> map = new java.util.LinkedHashMap<>();
        String sql = """
                SELECT pay_month, SUM(net_salary) AS total
                FROM payroll
                GROUP BY pay_month
                ORDER BY MIN(id)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("pay_month"), rs.getBigDecimal("total"));
            }
        }
        return map;
    }

    /**
     * Returns total net salary expense grouped by department name.
     * Result: LinkedHashMap of { "Engineering" -> 45000.00 }
     */
    public java.util.LinkedHashMap<String, java.math.BigDecimal> getSalaryByDepartment() throws SQLException {
        java.util.LinkedHashMap<String, java.math.BigDecimal> map = new java.util.LinkedHashMap<>();
        String sql = """
                SELECT COALESCE(d.name, 'Unassigned') AS dept_name,
                       SUM(p.net_salary) AS total
                FROM payroll p
                JOIN employees e ON p.emp_id = e.id
                LEFT JOIN departments d ON e.dept_id = d.id
                GROUP BY dept_name
                ORDER BY total DESC
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("dept_name"), rs.getBigDecimal("total"));
            }
        }
        return map;
    }
}