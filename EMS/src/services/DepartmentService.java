package services;

import database.DBConnection;
import models.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD operations for the `departments` table.
 */
public class DepartmentService {

  /** Returns all departments ordered by name. */
  public List<Department> getAllDepartments() throws SQLException {
    List<Department> list = new ArrayList<>();
    String sql = "SELECT id, name FROM departments ORDER BY name";
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(new Department(rs.getInt("id"), rs.getString("name")));
      }
    }
    return list;
  }

  /** Finds a department by its primary key. */
  public Department getDepartmentById(int id) throws SQLException {
    String sql = "SELECT id, name FROM departments WHERE id = ?";
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new Department(rs.getInt("id"), rs.getString("name"));
        }
      }
    }
    return null;
  }

  /** Inserts a new department and returns its generated ID. */
  public int addDepartment(String name) throws SQLException {
    String sql = "INSERT INTO departments (name) VALUES (?)";
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql,
            Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, name.trim());
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next())
          return keys.getInt(1);
      }
    }
    throw new SQLException("Department insert failed.");
  }

  /** Updates the name of an existing department. */
  public void updateDepartment(int id, String newName) throws SQLException {
    String sql = "UPDATE departments SET name = ? WHERE id = ?";
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, newName.trim());
      ps.setInt(2, id);
      ps.executeUpdate();
    }
  }

  /** Deletes a department (employees in it will have dept_id set to NULL). */
  public void deleteDepartment(int id) throws SQLException {
    String sql = "DELETE FROM departments WHERE id = ?";
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, id);
      ps.executeUpdate();
    }
  }
}