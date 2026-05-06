package ui;

import models.Attendance;
import models.Employee;
import services.AttendanceService;
import services.EmployeeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Attendance Management panel.
 *
 * Tab 1 – Mark Attendance: pick a date, set status for every employee, save all at once.
 * Tab 2 – Monthly Report:  pick YYYY-MM, see per-employee summary with attendance %.
 * Tab 3 – Payroll Link:    pick month + working days, auto-calculate attendance deductions.
 */
public class AttendancePanel extends JPanel {

    private final AttendanceService attService = new AttendanceService();
    private final EmployeeService   empService = new EmployeeService();

    public AttendancePanel() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel title = new JLabel("📅  Attendance Management");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.TEXT);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIUtils.FONT_SUBHEAD);
        tabs.addTab("✅  Mark Attendance",  buildMarkTab());
        tabs.addTab("📊  Monthly Report",   buildReportTab());
        tabs.addTab("💰  Payroll Link",     buildPayrollLinkTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 1 – Mark Attendance
    // ══════════════════════════════════════════════════════════

    private JTextField markDateField;
    private JTable     markTable;
    private DefaultTableModel markTableModel;

    private JPanel buildMarkTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(UIUtils.BG);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));

        // ── Date picker row ───────────────────────────────────
        JPanel topRow = UIUtils.cardPanel();
        topRow.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));

        topRow.add(UIUtils.formLabel("Date (YYYY-MM-DD):"));
        markDateField = UIUtils.styledField(12);
        markDateField.setText(LocalDate.now().toString());
        markDateField.setPreferredSize(new Dimension(160, 40));
        topRow.add(markDateField);

        JButton loadBtn = UIUtils.primaryButton("Load Employees");
        loadBtn.setPreferredSize(new Dimension(160, 40));
        loadBtn.addActionListener(e -> loadMarkTable());
        topRow.add(loadBtn);

        JButton todayBtn = UIUtils.outlineButton("Today");
        todayBtn.setPreferredSize(new Dimension(90, 40));
        todayBtn.addActionListener(e -> {
            markDateField.setText(LocalDate.now().toString());
            loadMarkTable();
        });
        topRow.add(todayBtn);

        JButton saveBtn = UIUtils.successButton("💾  Save All");
        saveBtn.setPreferredSize(new Dimension(120, 40));
        saveBtn.addActionListener(e -> saveAttendance());
        topRow.add(saveBtn);

        panel.add(topRow, BorderLayout.NORTH);

        // ── Attendance table ──────────────────────────────────
        JPanel tableCard = UIUtils.cardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel hint = new JLabel("Click a Status cell to change it. Then click Save All.");
        hint.setFont(UIUtils.FONT_SMALL);
        hint.setForeground(UIUtils.TEXT_MUTED);
        tableCard.add(hint, BorderLayout.NORTH);

        String[] cols = {"Emp ID", "Employee Name", "Status"};
        markTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 2 ? String.class : Object.class;
            }
        };
        markTable = new JTable(markTableModel);
        UIUtils.styleTable(markTable);
        markTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        markTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        markTable.getColumnModel().getColumn(2).setPreferredWidth(160);

        // Status column uses a combo box editor
        String[] statuses = {"PRESENT", "ABSENT", "HALF_DAY", "LEAVE"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setFont(UIUtils.FONT_BODY);
        markTable.getColumnModel().getColumn(2)
                .setCellEditor(new DefaultCellEditor(statusCombo));

        // Colour-code status cells
        markTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    String v = value == null ? "" : value.toString();
                    setBackground(switch (v) {
                        case "PRESENT"  -> new Color(220, 252, 231);
                        case "ABSENT"   -> new Color(254, 226, 226);
                        case "HALF_DAY" -> new Color(254, 249, 195);
                        case "LEAVE"    -> new Color(237, 233, 254);
                        default         -> UIUtils.CARD;
                    });
                    setForeground(UIUtils.TEXT);
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(markTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1, true));
        scroll.getViewport().setBackground(UIUtils.CARD);
        tableCard.add(scroll, BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);
        loadMarkTable();
        return panel;
    }

    private void loadMarkTable() {
        String date = markDateField.getText().trim();
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            UIUtils.showError(this, "Date must be in YYYY-MM-DD format."); return;
        }
        markTableModel.setRowCount(0);
        try {
            // Get existing records for this date
            Map<Integer, String> existing = new HashMap<>();
            for (Attendance a : attService.getByDate(date)) {
                existing.put(a.getEmpId(), a.getStatus().name());
            }
            // Load all employees
            for (Employee e : empService.getAllEmployees()) {
                String status = existing.getOrDefault(e.getId(), "PRESENT");
                markTableModel.addRow(new Object[]{e.getId(), e.getName(), status});
            }
        } catch (Exception ex) {
            UIUtils.showError(this, "Failed to load: " + ex.getMessage());
        }
    }

    private void saveAttendance() {
        String date = markDateField.getText().trim();
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            UIUtils.showError(this, "Date must be in YYYY-MM-DD format."); return;
        }
        // Stop any active cell editing
        if (markTable.isEditing()) markTable.getCellEditor().stopCellEditing();

        Map<Integer, Attendance.Status> map = new LinkedHashMap<>();
        for (int r = 0; r < markTableModel.getRowCount(); r++) {
            int empId = (int) markTableModel.getValueAt(r, 0);
            String statusStr = (String) markTableModel.getValueAt(r, 2);
            try {
                map.put(empId, Attendance.Status.valueOf(statusStr));
            } catch (IllegalArgumentException ignored) {
                map.put(empId, Attendance.Status.PRESENT);
            }
        }
        try {
            attService.bulkMark(date, map);
            UIUtils.showInfo(this, "Attendance saved for " + date + " (" + map.size() + " records).");
        } catch (Exception ex) {
            UIUtils.showError(this, "Save failed: " + ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 2 – Monthly Report
    // ══════════════════════════════════════════════════════════

    private JTextField reportMonthField;
    private JTextField reportWorkingDaysField;
    private DefaultTableModel reportTableModel;

    private JPanel buildReportTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(UIUtils.BG);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Controls
        JPanel topRow = UIUtils.cardPanel();
        topRow.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));

        topRow.add(UIUtils.formLabel("Month (YYYY-MM):"));
        reportMonthField = UIUtils.styledField(10);
        reportMonthField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        reportMonthField.setPreferredSize(new Dimension(140, 40));
        topRow.add(reportMonthField);

        topRow.add(UIUtils.formLabel("Working Days:"));
        reportWorkingDaysField = UIUtils.styledField(4);
        reportWorkingDaysField.setText("26");
        reportWorkingDaysField.setPreferredSize(new Dimension(70, 40));
        topRow.add(reportWorkingDaysField);

        JButton loadBtn = UIUtils.primaryButton("Generate Report");
        loadBtn.setPreferredSize(new Dimension(160, 40));
        loadBtn.addActionListener(e -> loadReport());
        topRow.add(loadBtn);

        panel.add(topRow, BorderLayout.NORTH);

        // Table
        JPanel tableCard = UIUtils.cardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        String[] cols = {"Employee", "Present", "Absent", "Half Day", "Leave", "Total Marked", "Attendance %"};
        reportTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable reportTable = new JTable(reportTableModel);
        UIUtils.styleTable(reportTable);
        reportTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        for (int i = 1; i <= 6; i++) reportTable.getColumnModel().getColumn(i).setPreferredWidth(100);

        // Colour attendance % column
        reportTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel && value != null) {
                    try {
                        double pct = Double.parseDouble(value.toString().replace("%", ""));
                        setForeground(pct >= 80 ? UIUtils.ACCENT : pct >= 60 ? UIUtils.WARNING : UIUtils.DANGER);
                        setFont(UIUtils.FONT_SUBHEAD);
                    } catch (NumberFormatException ignored) {}
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(reportTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1, true));
        scroll.getViewport().setBackground(UIUtils.CARD);
        tableCard.add(scroll, BorderLayout.CENTER);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    private void loadReport() {
        String month = reportMonthField.getText().trim();
        if (!month.matches("\\d{4}-\\d{2}")) {
            UIUtils.showError(this, "Month must be in YYYY-MM format."); return;
        }
        int workingDays = 26;
        try { workingDays = Integer.parseInt(reportWorkingDaysField.getText().trim()); }
        catch (NumberFormatException ignored) {}

        reportTableModel.setRowCount(0);
        try {
            List<AttendanceService.MonthlySummary> summaries = attService.getMonthlySummary(month);
            for (AttendanceService.MonthlySummary s : summaries) {
                double pct = s.getAttendancePct(workingDays);
                reportTableModel.addRow(new Object[]{
                        s.empName,
                        s.presentDays,
                        s.absentDays,
                        s.halfDays,
                        s.leaveDays,
                        s.totalMarked,
                        String.format("%.1f%%", pct)
                });
            }
        } catch (Exception ex) {
            UIUtils.showError(this, "Failed to load report: " + ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 3 – Payroll Link
    // ══════════════════════════════════════════════════════════

    private JTextField linkMonthField;
    private JTextField linkWorkingDaysField;
    private JTextField linkBaseSalaryField;
    private DefaultTableModel linkTableModel;

    private JPanel buildPayrollLinkTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(UIUtils.BG);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Controls
        JPanel topRow = UIUtils.cardPanel();
        topRow.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));

        topRow.add(UIUtils.formLabel("Month (YYYY-MM):"));
        linkMonthField = UIUtils.styledField(10);
        linkMonthField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        linkMonthField.setPreferredSize(new Dimension(140, 40));
        topRow.add(linkMonthField);

        topRow.add(UIUtils.formLabel("Working Days:"));
        linkWorkingDaysField = UIUtils.styledField(4);
        linkWorkingDaysField.setText("26");
        linkWorkingDaysField.setPreferredSize(new Dimension(70, 40));
        topRow.add(linkWorkingDaysField);

        topRow.add(UIUtils.formLabel("Default Base Salary ($):"));
        linkBaseSalaryField = UIUtils.styledField(8);
        linkBaseSalaryField.setText("5000");
        linkBaseSalaryField.setPreferredSize(new Dimension(110, 40));
        topRow.add(linkBaseSalaryField);

        JButton calcBtn = UIUtils.primaryButton("Calculate Deductions");
        calcBtn.setPreferredSize(new Dimension(190, 40));
        calcBtn.addActionListener(e -> calcDeductions());
        topRow.add(calcBtn);

        panel.add(topRow, BorderLayout.NORTH);

        // Info label
        JPanel infoCard = UIUtils.cardPanel();
        infoCard.setLayout(new BorderLayout());
        JLabel info = new JLabel(
            "<html>Formula: <b>Deduction = (Absent Days + Half Days × 0.5) ÷ Working Days × Base Salary</b><br>" +
            "This table shows the suggested attendance-based deduction per employee for the selected month.</html>");
        info.setFont(UIUtils.FONT_SMALL);
        info.setForeground(UIUtils.TEXT_MUTED);
        infoCard.add(info);

        // Table
        JPanel tableCard = UIUtils.cardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(infoCard, BorderLayout.NORTH);

        String[] cols = {"Employee", "Present", "Absent", "Half Day", "Attendance %", "Base Salary", "Suggested Deduction", "Net After Deduction"};
        linkTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable linkTable = new JTable(linkTableModel);
        UIUtils.styleTable(linkTable);
        linkTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        linkTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        linkTable.getColumnModel().getColumn(6).setPreferredWidth(160);
        linkTable.getColumnModel().getColumn(7).setPreferredWidth(160);

        JScrollPane scroll = new JScrollPane(linkTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1, true));
        scroll.getViewport().setBackground(UIUtils.CARD);
        tableCard.add(scroll, BorderLayout.CENTER);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    private void calcDeductions() {
        String month = linkMonthField.getText().trim();
        if (!month.matches("\\d{4}-\\d{2}")) {
            UIUtils.showError(this, "Month must be in YYYY-MM format."); return;
        }
        int workingDays = 26;
        try { workingDays = Integer.parseInt(linkWorkingDaysField.getText().trim()); }
        catch (NumberFormatException ignored) {}

        java.math.BigDecimal defaultBase;
        try {
            defaultBase = new java.math.BigDecimal(linkBaseSalaryField.getText().trim());
        } catch (NumberFormatException e) {
            UIUtils.showError(this, "Base salary must be a valid number."); return;
        }

        linkTableModel.setRowCount(0);
        try {
            List<AttendanceService.MonthlySummary> summaries = attService.getMonthlySummary(month);
            int wd = workingDays;
            java.math.BigDecimal base = defaultBase;

            for (AttendanceService.MonthlySummary s : summaries) {
                double deductDays = s.absentDays + s.halfDays * 0.5;
                double ratio = deductDays / Math.max(wd, 1);
                java.math.BigDecimal deduction = base.multiply(
                        java.math.BigDecimal.valueOf(ratio))
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal net = base.subtract(deduction);
                double pct = s.getAttendancePct(wd);

                linkTableModel.addRow(new Object[]{
                        s.empName,
                        s.presentDays,
                        s.absentDays,
                        s.halfDays,
                        String.format("%.1f%%", pct),
                        String.format("$%,.2f", base),
                        String.format("$%,.2f", deduction),
                        String.format("$%,.2f", net)
                });
            }
        } catch (Exception ex) {
            UIUtils.showError(this, "Calculation failed: " + ex.getMessage());
        }
    }
}