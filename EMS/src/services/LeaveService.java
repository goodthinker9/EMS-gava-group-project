package services;

import database.DBConnection;
import models.LeaveRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD + approval operations for the `leave_requests` table.
 */
public class LeaveService {

    // ── Row mapper ────────────────────────────────────────────

    private LeaveRequest mapRow(ResultSet rs) throws SQLException {
        LeaveRequest lr = new LeaveRequest();
        lr.setId(rs.getInt("id"));
        lr.setEmpId(rs.getInt("emp_id"));
        lr.setEmpName(rs.getString("emp_name"));
        lr.setLeaveType(rs.getString("leave_type"));
        lr.setStartDate(rs.getString("start_date"));
        lr.setEndDate(rs.getString("end_date"));
        lr.setReason(rs.getString("reason"));
        lr.setStatus(LeaveRequest.Status.valueOf(rs.getString("status")));
        lr.setAppliedOn(rs.getString("applied_on"));
        lr.setReviewedOn(rs.getString("reviewed_on"));
        return lr;
    }

    // ── Queries ───────────────────────────────────────────────

    /** All leave requests, newest first. */
    public List<LeaveRequest> getAllRequests() throws SQLException {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = """
                SELECT lr.id, lr.emp_id, e.name AS emp_name,
                       lr.leave_type, lr.start_date, lr.end_date,
                       lr.reason, lr.status, lr.applied_on, lr.reviewed_on
                FROM leave_requests lr
                JOIN employees e ON lr.emp_id = e.id
                ORDER BY lr.applied_on DESC
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** All requests filtered by status. */
    public List<LeaveRequest> getByStatus(LeaveRequest.Status status) throws SQLException {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = """
                SELECT lr.id, lr.emp_id, e.name AS emp_name,
                       lr.leave_type, lr.start_date, lr.end_date,
                       lr.reason, lr.status, lr.applied_on, lr.reviewed_on
                FROM leave_requests lr
                JOIN employees e ON lr.emp_id = e.id
                WHERE lr.status = ?
                ORDER BY lr.applied_on DESC
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** All requests for a specific employee. */
    public List<LeaveRequest> getByEmployee(int empId) throws SQLException {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = """
                SELECT lr.id, lr.emp_id, e.name AS emp_name,
                       lr.leave_type, lr.start_date, lr.end_date,
                       lr.reason, lr.status, lr.applied_on, lr.reviewed_on
                FROM leave_requests lr
                JOIN employees e ON lr.emp_id = e.id
                WHERE lr.emp_id = ?
                ORDER BY lr.applied_on DESC
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

    /** Count pending requests (for admin badge). */
    public int countPending() throws SQLException {
        String sql = "SELECT COUNT(*) FROM leave_requests WHERE status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ── Mutations ─────────────────────────────────────────────

    /** Employee submits a new leave request. Returns generated ID. */
    public int submitRequest(int empId, String leaveType,
                             String startDate, String endDate,
                             String reason) throws SQLException {
        String sql = """
                INSERT INTO leave_requests (emp_id, leave_type, start_date, end_date, reason)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, empId);
            ps.setString(2, leaveType.trim());
            ps.setString(3, startDate.trim());
            ps.setString(4, endDate.trim());
            ps.setString(5, reason == null ? "" : reason.trim());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Leave request insert failed.");
    }

    /** Admin approves or rejects a request. */
    public void updateStatus(int requestId, LeaveRequest.Status status) throws SQLException {
        String sql = """
                UPDATE leave_requests
                SET status = ?, reviewed_on = NOW()
                WHERE id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, requestId);
            ps.executeUpdate();
        }
    }

    /** Employee cancels their own PENDING request. */
    public void cancelRequest(int requestId) throws SQLException {
        String sql = "DELETE FROM leave_requests WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }
}