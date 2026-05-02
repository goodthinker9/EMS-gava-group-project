package services;

import database.DBConnection;
import models.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * CRUD + reporting for the `attendance` table.
 */
public class AttendanceService {

    // ── Row mapper ────────────────────────────────────────────

    private Attendance mapRow(ResultSet rs) throws SQLException {
        return new Attendance(
                rs.getInt("id"),
                rs.getInt("emp_id"),
                rs.getString("emp_name"),
                rs.getString("att_date"),
                Attendance.Status.valueOf(rs.getString("status"))
        );
    }

    // ── Queries ───────────────────────────────────────────────

    /** All attendance records for a given month (format: "YYYY-MM"). */
    public List<Attendance> getByMonth(String yearMonth) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = """
                SELECT a.id, a.emp_id, e.name AS emp_name,
                       DATE_FORMAT(a.att_date, '%Y-%m-%d') AS att_date, a.status
                FROM attendance a
                JOIN employees e ON a.emp_id = e.id
                WHERE DATE_FORMAT(a.att_date, '%Y-%m') = ?
                ORDER BY a.att_date, e.name
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, yearMonth);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** All attendance records for one employee in a given month. */
    public List<Attendance> getByEmployeeAndMonth(int empId, String yearMonth) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = """
                SELECT a.id, a.emp_id, e.name AS emp_name,
                       DATE_FORMAT(a.att_date, '%Y-%m-%d') AS att_date, a.status
                FROM attendance a
                JOIN employees e ON a.emp_id = e.id
                WHERE a.emp_id = ? AND DATE_FORMAT(a.att_date, '%Y-%m') = ?
                ORDER BY a.att_date
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setString(2, yearMonth);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Attendance for a specific date (all employees). */
    public List<Attendance> getByDate(String date) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = """
                SELECT a.id, a.emp_id, e.name AS emp_name,
                       DATE_FORMAT(a.att_date, '%Y-%m-%d') AS att_date, a.status
                FROM attendance a
                JOIN employees e ON a.emp_id = e.id
                WHERE a.att_date = ?
                ORDER BY e.name
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Mutations ─────────────────────────────────────────────

    /**
     * Inserts or updates attendance for one employee on one date.
     * Uses INSERT ... ON DUPLICATE KEY UPDATE (uq_emp_date constraint).
     */
    public void markAttendance(int empId, String date, Attendance.Status status) throws SQLException {
        String sql = """
                INSERT INTO attendance (emp_id, att_date, status)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE status = VALUES(status)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setString(2, date);
            ps.setString(3, status.name());
            ps.executeUpdate();
        }
    }

    /** Bulk mark all employees for a date. empStatusMap: { empId -> status } */
    public void bulkMark(String date, java.util.Map<Integer, Attendance.Status> empStatusMap)
            throws SQLException {
        String sql = """
                INSERT INTO attendance (emp_id, att_date, status)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE status = VALUES(status)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (java.util.Map.Entry<Integer, Attendance.Status> e : empStatusMap.entrySet()) {
                ps.setInt(1, e.getKey());
                ps.setString(2, date);
                ps.setString(3, e.getValue().name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ── Monthly summary ───────────────────────────────────────

    /**
     * Returns per-employee attendance summary for a month.
     * Result: list of { empId, empName, present, absent, halfDay, leave, attendancePct }
     */
    public List<MonthlySummary> getMonthlySummary(String yearMonth) throws SQLException {
        List<MonthlySummary> list = new ArrayList<>();
        String sql = """
                SELECT e.id AS emp_id, e.name AS emp_name,
                       SUM(CASE WHEN a.status = 'PRESENT'  THEN 1 ELSE 0 END) AS present_days,
                       SUM(CASE WHEN a.status = 'ABSENT'   THEN 1 ELSE 0 END) AS absent_days,
                       SUM(CASE WHEN a.status = 'HALF_DAY' THEN 1 ELSE 0 END) AS half_days,
                       SUM(CASE WHEN a.status = 'LEAVE'    THEN 1 ELSE 0 END) AS leave_days,
                       COUNT(a.id) AS total_marked
                FROM employees e
                LEFT JOIN attendance a
                    ON a.emp_id = e.id
                    AND DATE_FORMAT(a.att_date, '%Y-%m') = ?
                GROUP BY e.id, e.name
                ORDER BY e.name
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, yearMonth);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MonthlySummary s = new MonthlySummary();
                    s.empId       = rs.getInt("emp_id");
                    s.empName     = rs.getString("emp_name");
                    s.presentDays = rs.getInt("present_days");
                    s.absentDays  = rs.getInt("absent_days");
                    s.halfDays    = rs.getInt("half_days");
                    s.leaveDays   = rs.getInt("leave_days");
                    s.totalMarked = rs.getInt("total_marked");
                    list.add(s);
                }
            }
        }
        return list;
    }

    /**
     * Calculates attendance-based deduction for payroll.
     * Formula: (absentDays + halfDays * 0.5) / workingDays * baseSalary
     *
     * @param empId       employee ID
     * @param yearMonth   "YYYY-MM"
     * @param baseSalary  monthly base salary
     * @param workingDays total working days in the month (e.g. 26)
     */
    public java.math.BigDecimal calcAttendanceDeduction(int empId, String yearMonth,
            java.math.BigDecimal baseSalary, int workingDays) throws SQLException {
        List<MonthlySummary> summaries = getMonthlySummary(yearMonth);
        for (MonthlySummary s : summaries) {
            if (s.empId == empId) {
                double deductDays = s.absentDays + s.halfDays * 0.5;
                double ratio = deductDays / Math.max(workingDays, 1);
                double deduction = baseSalary.doubleValue() * ratio;
                return java.math.BigDecimal.valueOf(deduction)
                        .setScale(2, java.math.RoundingMode.HALF_UP);
            }
        }
        return java.math.BigDecimal.ZERO;
    }

    // ── Inner summary DTO ─────────────────────────────────────

    public static class MonthlySummary {
        public int    empId;
        public String empName;
        public int    presentDays;
        public int    absentDays;
        public int    halfDays;
        public int    leaveDays;
        public int    totalMarked;

        public double getAttendancePct(int workingDays) {
            if (workingDays == 0) return 0;
            double effective = presentDays + halfDays * 0.5;
            return (effective / workingDays) * 100.0;
        }
    }
}