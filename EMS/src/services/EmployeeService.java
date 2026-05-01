package services;

import database.DBConnection;
import models.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD operations for the `employees` table.
 * Defensive: works with both old schema (no user_id/joining_date) and new
 * schema.
 */
public class EmployeeService {

  // ── Shared row mapper ─────────────────────────────────────

  private Employee mapRow(ResultSet rs) throws SQLException {
    int id = rs.getInt("id");
    String name = rs.getString("name");
    String email = rs.getString("email");

    // Optional columns – safe fallback if column doesn't exist
    int userId = safeGetInt(rs, "user_id", 0);
    String phone = safeGetString(rs, "phone", "");
    String address = safeGetString(rs, "address", "");
    int deptId = safeGetInt(rs, "dept_id", 0);
    String deptName = safeGetString(rs, "dept_name", "Unassigned");

    Employee e = new Employee(id, userId, name, email, phone, address, deptId, deptName);
    e.setJoiningDate(safeGetString(rs, "joining_date", null));
    return e;
  }

  private int safeGetInt(ResultSet rs, String col, int def) {
    try {
      int v = rs.getInt(col);
      return rs.wasNull() ? def : v;
    } catch (SQLException e) {
      return def;
    }
  }

  private String safeGetString(ResultSet rs, String col, String def) {
    try {
      String v = rs.getString(col);
      return v == null ? def : v;
    } catch (SQLException e) {
      return def;
    }
  }

  // ── Queries ───────────────────────────────────────────────

  /** Returns all employees. Works with both old and new schema. */
  public List<Employee> getAllEmployees() throws SQLException {
    List<Employee> list = new ArrayList<>();

    // Detect which columns exist
    boolean hasUserId = columnExists("employees", "user_id");
    boolean hasJoiningDate = columnExists("employees", "joining_date");

    StringBuilder sql = new StringBuilder("SELECT e.id, e.name, e.email");
    if (hasUserId)
      sql.append(", e.user_id");
    sql.append(", COALESCE(e.phone, '') AS phone");
    sql.append(", COALESCE(e.address, '') AS address");
    sql.append(", COALESCE(e.dept_id, 0) AS dept_id");
    sql.append(", COALESCE(d.name, 'Unassigned') AS dept_name");
    if (hasJoiningDate)
      sql.append(", e.joining_date");
    sql.append(" FROM employees e LEFT JOIN departments d ON e.dept_id = d.id ORDER BY e.name");

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString());
        ResultSet rs = ps.executeQuery()) {
      while (rs.next())
        list.add(mapRow(rs));
    }
    return list;
  }

  /** Finds an employee by primary key. */
  public Employee getEmployeeById(int id) throws SQLException {
    boolean hasUserId = columnExists("employees", "user_id");
    boolean hasJoiningDate = columnExists("employees", "joining_date");

    StringBuilder sql = new StringBuilder("SELECT e.id, e.name, e.email");
    if (hasUserId)
      sql.append(", e.user_id");
    sql.append(", COALESCE(e.phone, '') AS phone");
    sql.append(", COALESCE(e.address, '') AS address");
    sql.append(", COALESCE(e.dept_id, 0) AS dept_id");
    sql.append(", COALESCE(d.name, 'Unassigned') AS dept_name");
    if (hasJoiningDate)
      sql.append(", e.joining_date");
    sql.append(" FROM employees e LEFT JOIN departments d ON e.dept_id = d.id WHERE e.id = ?");

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next())
          return mapRow(rs);
      }
    }
    return null;
  }

  /** Finds an employee by linked user_id. */
  public Employee getEmployeeByUserId(int userId) throws SQLException {
    if (!columnExists("employees", "user_id"))
      return null;

    boolean hasJoiningDate = columnExists("employees", "joining_date");
    StringBuilder sql = new StringBuilder(
        "SELECT e.id, e.user_id, e.name, e.email" +
            ", COALESCE(e.phone, '') AS phone" +
            ", COALESCE(e.address, '') AS address" +
            ", COALESCE(e.dept_id, 0) AS dept_id" +
            ", COALESCE(d.name, 'Unassigned') AS dept_name");
    if (hasJoiningDate)
      sql.append(", e.joining_date");
    sql.append(" FROM employees e LEFT JOIN departments d ON e.dept_id = d.id WHERE e.user_id = ?");

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      ps.setInt(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next())
          return mapRow(rs);
      }
    }
    return null;
  }

  /** Advanced search with optional filters. */
  public List<Employee> searchEmployees(String keyword, int deptId,
      java.math.BigDecimal minSalary, java.math.BigDecimal maxSalary,
      String joinFrom, String joinTo) throws SQLException {

    boolean hasJoiningDate = columnExists("employees", "joining_date");
    boolean hasUserId = columnExists("employees", "user_id");

    List<Employee> list = new ArrayList<>();
    StringBuilder sql = new StringBuilder("SELECT e.id, e.name, e.email");
    if (hasUserId)
      sql.append(", e.user_id");
    sql.append(", COALESCE(e.phone,'') AS phone");
    sql.append(", COALESCE(e.address,'') AS address");
    sql.append(", COALESCE(e.dept_id,0) AS dept_id");
    sql.append(", COALESCE(d.name,'Unassigned') AS dept_name");
    if (hasJoiningDate)
      sql.append(", e.joining_date");
    sql.append(" FROM employees e LEFT JOIN departments d ON e.dept_id = d.id");

    boolean salaryFilter = (minSalary != null || maxSalary != null);
    if (salaryFilter) {
      sql.append(
          " LEFT JOIN (SELECT emp_id, MAX(net_salary) AS max_sal FROM payroll GROUP BY emp_id) p ON p.emp_id = e.id");
    }

    List<Object> params = new ArrayList<>();
    List<String> where = new ArrayList<>();

    if (keyword != null && !keyword.isBlank()) {
      where.add("(e.name LIKE ? OR CAST(e.id AS CHAR) LIKE ?)");
      params.add("%" + keyword.trim() + "%");
      params.add("%" + keyword.trim() + "%");
    }
    if (deptId > 0) {
      where.add("e.dept_id = ?");
      params.add(deptId);
    }
    if (minSalary != null) {
      where.add("COALESCE(p.max_sal,0) >= ?");
      params.add(minSalary);
    }
    if (maxSalary != null) {
      where.add("COALESCE(p.max_sal,0) <= ?");
      params.add(maxSalary);
    }
    if (hasJoiningDate && joinFrom != null && !joinFrom.isBlank()) {
      where.add("e.joining_date >= ?");
      params.add(joinFrom);
    }
    if (hasJoiningDate && joinTo != null && !joinTo.isBlank()) {
      where.add("e.joining_date <= ?");
      params.add(joinTo);
    }

    if (!where.isEmpty())
      sql.append(" WHERE ").append(String.join(" AND ", where));
    sql.append(" ORDER BY e.name");

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      for (int i = 0; i < params.size(); i++) {
        Object p = params.get(i);
        if (p instanceof String s)
          ps.setString(i + 1, s);
        else if (p instanceof Integer iv)
          ps.setInt(i + 1, iv);
        else if (p instanceof java.math.BigDecimal bd)
          ps.setBigDecimal(i + 1, bd);
      }
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next())
          list.add(mapRow(rs));
      }
    }
    return list;
  }

  // ── Mutations ─────────────────────────────────────────────

  public int addEmployee(int userId, String name, String email,
      String phone, String address, int deptId) throws SQLException {
    return addEmployee(userId, name, email, phone, address, deptId, null);
  }

  public int addEmployee(int userId, String name, String email,
      String phone, String address, int deptId,
      String joiningDate) throws SQLException {

    boolean hasUserId = columnExists("employees", "user_id");
    boolean hasJoiningDate = columnExists("employees", "joining_date");

    StringBuilder cols = new StringBuilder("name, email, phone, address");
    StringBuilder vals = new StringBuilder("?, ?, ?, ?");
    if (hasUserId) {
      cols.insert(0, "user_id, ");
      vals.insert(0, "?, ");
    }
    if (deptId > 0) {
      cols.append(", dept_id");
      vals.append(", ?");
    }
    if (hasJoiningDate) {
      cols.append(", joining_date");
      vals.append(", ?");
    }

    String sql = "INSERT INTO employees (" + cols + ") VALUES (" + vals + ")";

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      int idx = 1;
      if (hasUserId)
        ps.setInt(idx++, userId);
      ps.setString(idx++, name.trim());
      ps.setString(idx++, email.trim());
      ps.setString(idx++, phone == null ? "" : phone.trim());
      ps.setString(idx++, address == null ? "" : address.trim());
      if (deptId > 0)
        ps.setInt(idx++, deptId);
      if (hasJoiningDate) {
        if (joiningDate != null && !joiningDate.isBlank())
          ps.setString(idx++, joiningDate);
        else
          ps.setNull(idx++, Types.DATE);
      }
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next())
          return keys.getInt(1);
      }
    }
    throw new SQLException("Employee insert failed.");
  }

  public void updateEmployee(int id, String name, String email,
      String phone, String address, int deptId) throws SQLException {
    updateEmployee(id, name, email, phone, address, deptId, null);
  }

  public void updateEmployee(int id, String name, String email,
      String phone, String address, int deptId,
      String joiningDate) throws SQLException {

    boolean hasJoiningDate = columnExists("employees", "joining_date");

    StringBuilder sql = new StringBuilder(
        "UPDATE employees SET name=?, email=?, phone=?, address=?, dept_id=?");
    if (hasJoiningDate)
      sql.append(", joining_date=?");
    sql.append(" WHERE id=?");

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      int idx = 1;
      ps.setString(idx++, name.trim());
      ps.setString(idx++, email.trim());
      ps.setString(idx++, phone == null ? "" : phone.trim());
      ps.setString(idx++, address == null ? "" : address.trim());
      if (deptId > 0)
        ps.setInt(idx++, deptId);
      else
        ps.setNull(idx++, Types.INTEGER);
      if (hasJoiningDate) {
        if (joiningDate != null && !joiningDate.isBlank())
          ps.setString(idx++, joiningDate);
        else
          ps.setNull(idx++, Types.DATE);
      }
      ps.setInt(idx, id);
      ps.executeUpdate();
    }
  }

  public void updateContactInfo(int id, String phone, String address) throws SQLException {
    String sql = "UPDATE employees SET phone=?, address=? WHERE id=?";
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, phone == null ? "" : phone.trim());
      ps.setString(2, address == null ? "" : address.trim());
      ps.setInt(3, id);
      ps.executeUpdate();
    }
  }

  public void deleteEmployee(int id) throws SQLException {
    String sql = "DELETE FROM employees WHERE id=?";
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, id);
      ps.executeUpdate();
    }
  }

  // ── Analytics ─────────────────────────────────────────────

  public java.util.LinkedHashMap<String, Integer> getEmployeeCountByDepartment()
      throws SQLException {
    java.util.LinkedHashMap<String, Integer> map = new java.util.LinkedHashMap<>();
    String sql = """
        SELECT COALESCE(d.name,'Unassigned') AS dept_name, COUNT(e.id) AS emp_count
        FROM employees e
        LEFT JOIN departments d ON e.dept_id = d.id
        GROUP BY dept_name ORDER BY emp_count DESC
        """;
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next())
        map.put(rs.getString("dept_name"), rs.getInt("emp_count"));
    }
    return map;
  }

  // ── Schema introspection ──────────────────────────────────

  /**
   * Returns true if the given column exists in the given table.
   * Cached per session to avoid repeated INFORMATION_SCHEMA queries.
   */
  private static final java.util.Set<String> COLUMN_CACHE = new java.util.HashSet<>();
  private static final java.util.Set<String> MISSING_CACHE = new java.util.HashSet<>();

  public boolean columnExists(String table, String column) {
    String key = table + "." + column;
    if (COLUMN_CACHE.contains(key))
      return true;
    if (MISSING_CACHE.contains(key))
      return false;
    try (Connection conn = DBConnection.getConnection()) {
      DatabaseMetaData meta = conn.getMetaData();
      try (ResultSet rs = meta.getColumns(null, null, table, column)) {
        boolean exists = rs.next();
        if (exists)
          COLUMN_CACHE.add(key);
        else
          MISSING_CACHE.add(key);
        return exists;
      }
    } catch (SQLException e) {
      return false;
    }
  }
}