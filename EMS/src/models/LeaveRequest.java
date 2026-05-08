package models;

/**
 * Model representing a row in the `leave_requests` table.
 */
public class LeaveRequest {

    public enum Status { PENDING, APPROVED, REJECTED }

    private int    id;
    private int    empId;
    private String empName;      // from JOIN
    private String leaveType;
    private String startDate;    // "YYYY-MM-DD"
    private String endDate;      // "YYYY-MM-DD"
    private String reason;
    private Status status;
    private String appliedOn;    // "YYYY-MM-DD HH:mm:ss"
    private String reviewedOn;

    public LeaveRequest() {}

    // ── Getters & Setters ────────────────────────────────────

    public int    getId()                   { return id; }
    public void   setId(int id)             { this.id = id; }

    public int    getEmpId()                { return empId; }
    public void   setEmpId(int e)           { this.empId = e; }

    public String getEmpName()              { return empName; }
    public void   setEmpName(String n)      { this.empName = n; }

    public String getLeaveType()            { return leaveType; }
    public void   setLeaveType(String t)    { this.leaveType = t; }

    public String getStartDate()            { return startDate; }
    public void   setStartDate(String d)    { this.startDate = d; }

    public String getEndDate()              { return endDate; }
    public void   setEndDate(String d)      { this.endDate = d; }

    public String getReason()               { return reason; }
    public void   setReason(String r)       { this.reason = r; }

    public Status getStatus()               { return status; }
    public void   setStatus(Status s)       { this.status = s; }

    public String getAppliedOn()            { return appliedOn; }
    public void   setAppliedOn(String a)    { this.appliedOn = a; }

    public String getReviewedOn()           { return reviewedOn; }
    public void   setReviewedOn(String r)   { this.reviewedOn = r; }

    /** Display-friendly status with emoji. */
    public String getStatusDisplay() {
        return switch (status) {
            case PENDING  -> "⏳ Pending";
            case APPROVED -> "✅ Approved";
            case REJECTED -> "❌ Rejected";
        };
    }

    /** Number of days requested (inclusive). */
    public long getDays() {
        try {
            java.time.LocalDate s = java.time.LocalDate.parse(startDate);
            java.time.LocalDate e = java.time.LocalDate.parse(endDate);
            return java.time.temporal.ChronoUnit.DAYS.between(s, e) + 1;
        } catch (Exception ex) { return 0; }
    }
}