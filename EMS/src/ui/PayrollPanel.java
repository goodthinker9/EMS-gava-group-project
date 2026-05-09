package ui;

import models.Employee;
import models.Payroll;
import services.EmployeeService;
import services.PayrollService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Advanced Payroll panel – works for both admin (all records) and employee (own records).
 */
public class PayrollPanel extends JPanel {

    private final Integer        filterEmpId;
    private final PayrollService payService  = new PayrollService();
    private final EmployeeService empService = new EmployeeService();

    private JTable            table;
    private DefaultTableModel tableModel;

    private JComboBox<Employee> empCombo;
    private JTextField payMonthField, salaryField, bonusField, deductionField;
    private JLabel     netSalaryPreview;

    private final boolean isAdminView;

    public PayrollPanel(Integer filterEmpId) {
        this.filterEmpId = filterEmpId;
        this.isAdminView = (filterEmpId == null);

        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        if (isAdminView) add(buildAdminForm(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadPayroll();
    }

    // ── Admin entry form ──────────────────────────────────────

    private JPanel buildAdminForm() {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 14));

        JLabel title = UIUtils.sectionLabel("Generate / Edit Payroll");
        card.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(0, 0, 0, 14);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        // Labels row
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 2.0; fields.add(UIUtils.formLabel("Employee"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.5; fields.add(UIUtils.formLabel("Pay Month"), gbc);
        gbc.gridx = 2; gbc.weightx = 1.0; fields.add(UIUtils.formLabel("Salary ($)"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0; fields.add(UIUtils.formLabel("Bonus ($)"), gbc);
        gbc.gridx = 4; gbc.weightx = 1.0; fields.add(UIUtils.formLabel("Deduction ($)"), gbc);
        gbc.gridx = 5; gbc.weightx = 1.2; fields.add(UIUtils.formLabel("Net Salary"), gbc);

        // Inputs row
        gbc.gridy = 1; gbc.insets = new Insets(6, 0, 0, 14);

        empCombo = new JComboBox<>();
        empCombo.setFont(UIUtils.FONT_BODY);
        empCombo.setBackground(Color.WHITE);
        empCombo.setPreferredSize(new Dimension(0, 40));
        populateEmployeeCombo();
        gbc.gridx = 0; gbc.weightx = 2.0; fields.add(empCombo, gbc);

        payMonthField = UIUtils.styledField(10);
        payMonthField.setToolTipText("e.g. April 2026");
        gbc.gridx = 1; gbc.weightx = 1.5; fields.add(payMonthField, gbc);

        salaryField = UIUtils.styledField(8);
        salaryField.setToolTipText("Base salary");
        gbc.gridx = 2; gbc.weightx = 1.0; fields.add(salaryField, gbc);

        bonusField = UIUtils.styledField(8);
        bonusField.setToolTipText("Bonus amount");
        gbc.gridx = 3; gbc.weightx = 1.0; fields.add(bonusField, gbc);

        deductionField = UIUtils.styledField(8);
        deductionField.setToolTipText("Deduction amount");
        gbc.gridx = 4; gbc.weightx = 1.0; fields.add(deductionField, gbc);

        // Live net salary preview
        netSalaryPreview = new JLabel("$0.00");
        netSalaryPreview.setFont(new Font("Segoe UI", Font.BOLD, 15));
        netSalaryPreview.setForeground(UIUtils.ACCENT);
        netSalaryPreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        netSalaryPreview.setPreferredSize(new Dimension(0, 40));
        gbc.gridx = 5; gbc.weightx = 1.2; fields.add(netSalaryPreview, gbc);

        // Live calculation listeners
        java.awt.event.KeyAdapter calcListener = new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { updateNetPreview(); }
        };
        salaryField.addKeyListener(calcListener);
        bonusField.addKeyListener(calcListener);
        deductionField.addKeyListener(calcListener);

        card.add(fields, BorderLayout.CENTER);

        // Action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton clearBtn  = UIUtils.outlineButton("Clear");
        JButton deleteBtn = UIUtils.dangerButton("Delete");
        JButton updateBtn = UIUtils.outlineButton("Update");
        JButton addBtn    = UIUtils.primaryButton("Add Record");

        clearBtn.setPreferredSize(new Dimension(90, 36));
        deleteBtn.setPreferredSize(new Dimension(90, 36));
        updateBtn.setPreferredSize(new Dimension(90, 36));
        addBtn.setPreferredSize(new Dimension(120, 36));

        clearBtn.addActionListener(e  -> clearForm());
        deleteBtn.addActionListener(e -> handleDelete());
        updateBtn.addActionListener(e -> handleUpdate());
        addBtn.addActionListener(e    -> handleAdd());

        btnRow.add(clearBtn);
        btnRow.add(deleteBtn);
        btnRow.add(updateBtn);
        btnRow.add(addBtn);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    private void updateNetPreview() {
        try {
            BigDecimal s = parseBD(salaryField.getText());
            BigDecimal b = parseBD(bonusField.getText());
            BigDecimal d = parseBD(deductionField.getText());
            BigDecimal net = s.add(b).subtract(d);
            netSalaryPreview.setText(String.format("$%.2f", net));
            netSalaryPreview.setForeground(net.compareTo(BigDecimal.ZERO) >= 0 ? UIUtils.ACCENT : UIUtils.DANGER);
        } catch (NumberFormatException ignored) {
            netSalaryPreview.setText("—");
            netSalaryPreview.setForeground(UIUtils.TEXT_MUTED);
        }
    }

    private BigDecimal parseBD(String s) {
        s = s.trim();
        return s.isEmpty() ? BigDecimal.ZERO : new BigDecimal(s);
    }

    // ── Table card ────────────────────────────────────────────

    private JPanel buildTableCard() {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Header row
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel(isAdminView ? "All Payroll Records" : "My Payroll History");
        title.setFont(UIUtils.FONT_HEADER);
        title.setForeground(UIUtils.TEXT);
        header.add(title, BorderLayout.WEST);

        JButton refreshBtn = UIUtils.successButton("↻ Refresh");
        refreshBtn.setPreferredSize(new Dimension(110, 34));
        refreshBtn.addActionListener(e -> loadPayroll());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(refreshBtn);
        header.add(btnWrap, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Employee", "Pay Month", "Salary", "Bonus", "Deduction", "Net Salary"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);

        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);

        if (isAdminView) {
            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) fillFormFromSelection();
            });
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1, true));
        scroll.getViewport().setBackground(UIUtils.CARD);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ── Data helpers ──────────────────────────────────────────

    private void populateEmployeeCombo() {
        try {
            empCombo.removeAllItems();
            List<Employee> employees = empService.getAllEmployees();
            for (Employee e : employees) empCombo.addItem(e);
        } catch (SQLException ex) {
            UIUtils.showError(this, "Could not load employees: " + ex.getMessage());
        }
    }

    /** Called by AdminDashboard when switching to the Payroll tab,
     *  so newly added employees always appear in the dropdown. */
    public void reloadEmployeeCombo() {
        if (isAdminView && empCombo != null) {
            populateEmployeeCombo();
        }
    }

    void loadPayroll() {
        tableModel.setRowCount(0);
        try {
            List<Payroll> records = (filterEmpId == null)
                    ? payService.getAllPayroll()
                    : payService.getPayrollByEmployee(filterEmpId);

            for (Payroll p : records) {
                tableModel.addRow(new Object[]{
                        p.getId(),
                        p.getEmpName(),
                        p.getPayMonth(),
                        String.format("$%,.2f", p.getSalary()),
                        String.format("$%,.2f", p.getBonus()),
                        String.format("$%,.2f", p.getDeduction()),
                        String.format("$%,.2f", p.getNetSalary())
                });
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Failed to load payroll: " + ex.getMessage());
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0 || !isAdminView) return;

        int payId = (int) tableModel.getValueAt(row, 0);
        try {
            Payroll p = payService.getPayrollById(payId);
            if (p == null) return;

            payMonthField.setText(p.getPayMonth());
            salaryField.setText(p.getSalary().toPlainString());
            bonusField.setText(p.getBonus().toPlainString());
            deductionField.setText(p.getDeduction().toPlainString());
            updateNetPreview();

            for (int i = 0; i < empCombo.getItemCount(); i++) {
                if (empCombo.getItemAt(i).getId() == p.getEmpId()) {
                    empCombo.setSelectedIndex(i);
                    break;
                }
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Error loading record: " + ex.getMessage());
        }
    }

    // ── CRUD handlers ─────────────────────────────────────────

    private void handleAdd() {
        PayrollInput input = readForm();
        if (input == null) return;
        try {
            payService.addPayroll(input.empId, input.payMonth,
                    input.salary, input.bonus, input.deduction);
            loadPayroll();
            clearForm();
            UIUtils.showInfo(this, "Payroll record added successfully.");
        } catch (SQLException ex) {
            UIUtils.showError(this, "Add failed: " + ex.getMessage());
        }
    }

    private void handleUpdate() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Select a record to update."); return; }
        int payId = (int) tableModel.getValueAt(row, 0);

        PayrollInput input = readForm();
        if (input == null) return;
        try {
            payService.updatePayroll(payId, input.payMonth,
                    input.salary, input.bonus, input.deduction);
            loadPayroll();
            UIUtils.showInfo(this, "Payroll record updated.");
        } catch (SQLException ex) {
            UIUtils.showError(this, "Update failed: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Select a record to delete."); return; }
        int payId = (int) tableModel.getValueAt(row, 0);

        if (!UIUtils.confirm(this, "Delete this payroll record? This cannot be undone.")) return;
        try {
            payService.deletePayroll(payId);
            loadPayroll();
            clearForm();
        } catch (SQLException ex) {
            UIUtils.showError(this, "Delete failed: " + ex.getMessage());
        }
    }

    // ── Form helpers ──────────────────────────────────────────

    private static class PayrollInput {
        int empId; String payMonth;
        BigDecimal salary, bonus, deduction;
    }

    private PayrollInput readForm() {
        PayrollInput in = new PayrollInput();

        Employee emp = (Employee) empCombo.getSelectedItem();
        if (emp == null) { UIUtils.showError(this, "Select an employee."); return null; }
        in.empId = emp.getId();

        in.payMonth = payMonthField.getText().trim();
        if (in.payMonth.isEmpty()) { UIUtils.showError(this, "Pay month is required (e.g. April 2026)."); return null; }

        try {
            String salStr = salaryField.getText().trim();
            if (salStr.isEmpty()) { UIUtils.showError(this, "Salary is required."); return null; }
            in.salary    = new BigDecimal(salStr);
            in.bonus     = parseBD(bonusField.getText());
            in.deduction = parseBD(deductionField.getText());
        } catch (NumberFormatException ex) {
            UIUtils.showError(this, "Salary, bonus, and deduction must be valid numbers.");
            return null;
        }

        if (in.salary.compareTo(BigDecimal.ZERO) < 0) {
            UIUtils.showError(this, "Salary cannot be negative."); return null;
        }

        return in;
    }

    private void clearForm() {
        if (!isAdminView) return;
        payMonthField.setText("");
        salaryField.setText("");
        bonusField.setText("");
        deductionField.setText("");
        netSalaryPreview.setText("$0.00");
        netSalaryPreview.setForeground(UIUtils.ACCENT);
        if (empCombo.getItemCount() > 0) empCombo.setSelectedIndex(0);
        table.clearSelection();
    }
}