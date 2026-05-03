package models;

/**
 * Model representing a row in the `employees` table,
 * joined with department name for display purposes.
 */
public class Employee {

    private int    id;
    private int    userId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private int    deptId;
    private String deptName;      // from JOIN
    private String joiningDate;   // stored as "YYYY-MM-DD" string

    public Employee() {}

    public Employee(int id, int userId, String name, String email,
                    String phone, String address, int deptId, String deptName) {
        this.id       = id;
        this.userId   = userId;
        this.name     = name;
        this.email    = email;
        this.phone    = phone;
        this.address  = address;
        this.deptId   = deptId;
        this.deptName = deptName;
    }

    // ── Getters & Setters ────────────────────────────────────

    public int    getId()              { return id; }
    public void   setId(int id)        { this.id = id; }

    public int    getUserId()          { return userId; }
    public void   setUserId(int uid)   { this.userId = uid; }

    public String getName()            { return name; }
    public void   setName(String n)    { this.name = n; }

    public String getEmail()           { return email; }
    public void   setEmail(String e)   { this.email = e; }

    public String getPhone()           { return phone; }
    public void   setPhone(String p)   { this.phone = p; }

    public String getAddress()         { return address; }
    public void   setAddress(String a) { this.address = a; }

    public int    getDeptId()          { return deptId; }
    public void   setDeptId(int d)     { this.deptId = d; }

    public String getDeptName()        { return deptName; }
    public void   setDeptName(String d){ this.deptName = d; }

    public String getJoiningDate()           { return joiningDate; }
    public void   setJoiningDate(String jd)  { this.joiningDate = jd; }

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}