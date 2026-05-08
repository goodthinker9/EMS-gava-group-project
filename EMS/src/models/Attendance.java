package models;

/**
 * Model representing a row in the `attendance` table.
 */
public class Attendance {

    public enum Status { PRESENT, ABSENT, HALF_DAY, LEAVE }

    private int    id;
    private int    empId;
    private String empName;   // from JOIN
    private String attDate;   // "YYYY-MM-DD"
    private Status status;

    public Attendance() {}

    public Attendance(int id, int empId, String empName, String attDate, Status status) {
        this.id      = id;
        this.empId   = empId;
        this.empName = empName;
        this.attDate = attDate;
        this.status  = status;
    }

    public int    getId()                  { return id; }
    public void   setId(int id)            { this.id = id; }

    public int    getEmpId()               { return empId; }
    public void   setEmpId(int e)          { this.empId = e; }

    public String getEmpName()             { return empName; }
    public void   setEmpName(String n)     { this.empName = n; }

    public String getAttDate()             { return attDate; }
    public void   setAttDate(String d)     { this.attDate = d; }

    public Status getStatus()              { return status; }
    public void   setStatus(Status s)      { this.status = s; }

    /** Display-friendly status with colour hint. */
    public String getStatusDisplay() {
        return switch (status) {
            case PRESENT  -> "✅ Present";
            case ABSENT   -> "❌ Absent";
            case HALF_DAY -> "🌓 Half Day";
            case LEAVE    -> "🏖 Leave";
        };
    }
}