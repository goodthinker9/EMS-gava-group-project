package services;

import database.DBConnection;
import models.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles authentication: login, password hashing, and user creation.
 */
public class AuthService {

    // ── Password hashing ─────────────────────────────────────

    /**
     * Returns the SHA-256 hex digest of the given plain-text password.
     */
    public static String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ── Login ─────────────────────────────────────────────────

    /**
     * Validates credentials and returns the matching User, or null on failure.
     *
     * @param username plain-text username
     * @param password plain-text password (will be hashed before comparison)
     */
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String inputHash  = hashPassword(password);
                    if (storedHash.equals(inputHash)) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setPassword(storedHash);
                        user.setRole(User.Role.valueOf(rs.getString("role")));
                        return user;
                    }
                }
            }
        }
        return null; // invalid credentials
    }

    // ── Create user ───────────────────────────────────────────

    /**
     * Inserts a new user row and returns the generated ID.
     *
     * @param username plain-text username
     * @param password plain-text password (hashed before storage)
     * @param role     ADMIN or EMPLOYEE
     */
    public int createUser(String username, String password, User.Role role) throws SQLException {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username.trim());
            ps.setString(2, hashPassword(password));
            ps.setString(3, role.name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("User creation failed – no generated key.");
    }

    // ── Change password ───────────────────────────────────────

    public void changePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashPassword(newPassword));
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ── Delete user ───────────────────────────────────────────

    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}