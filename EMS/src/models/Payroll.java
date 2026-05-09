package models;

import java.math.BigDecimal;
public class Payroll {

    private int        id;
    private int        empId;
    private String     empName;    
    private String     payMonth;
    private BigDecimal salary;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal netSalary; 

    public Payroll() {}

    public Payroll(int id, int empId, String empName, String payMonth,
                   BigDecimal salary, BigDecimal bonus,
                   BigDecimal deduction, BigDecimal netSalary) {
        this.id        = id;
        this.empId     = empId;
        this.empName   = empName;
        this.payMonth  = payMonth;
        this.salary    = salary;
        this.bonus     = bonus;
        this.deduction = deduction;
        this.netSalary = netSalary;
    }


    // ── Getters & Setters Methods────────────────────────────────────

    public int        getId()                  { return id; }
    public void       setId(int id)            { this.id = id; }

    public int        getEmpId()               { return empId; }
    public void       setEmpId(int e)          { this.empId = e; }

    public String     getEmpName()             { return empName; }
    public void       setEmpName(String n)     { this.empName = n; }

    public String     getPayMonth()            { return payMonth; }
    public void       setPayMonth(String m)    { this.payMonth = m; }

    public BigDecimal getSalary()              { return salary; }
    public void       setSalary(BigDecimal s)  { this.salary = s; }

    public BigDecimal getBonus()               { return bonus; }
    public void       setBonus(BigDecimal b)   { this.bonus = b; }

    public BigDecimal getDeduction()           { return deduction; }
    public void       setDeduction(BigDecimal d){ this.deduction = d; }

    public BigDecimal getNetSalary()           { return netSalary; }
    public void       setNetSalary(BigDecimal n){ this.netSalary = n; }
}