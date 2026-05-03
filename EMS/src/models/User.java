package models;

/**
 * Model representing a row in the `users` table.
 */
public class User {

    public enum Role { ADMIN, EMPLOYEE }

    private int    id;
    private String username;
    private String password; // hashed
    private Role   role;

    public User() {}

    public User(int id, String username, String password, Role role) {
        this.id       = id;
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    // ── Getters & Setters ────────────────────────────────────

    public int    getId()       { return id; }
    public void   setId(int id) { this.id = id; }

    public String getUsername()              { return username; }
    public void   setUsername(String u)      { this.username = u; }

    public String getPassword()              { return password; }
    public void   setPassword(String p)      { this.password = p; }

    public Role   getRole()                  { return role; }
    public void   setRole(Role r)            { this.role = r; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role + "}";
    }
}