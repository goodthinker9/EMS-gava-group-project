package ui;

import models.Department;
import models.Employee;
import services.DepartmentService;
import services.EmployeeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Advanced Search & Filter panel for employees.
 * Filters: keyword (name/ID), department, salary range, joining date range.
 */
public class SearchPanel extends JPanel {

    private final EmployeeService   empService  = new EmployeeService();
    private final DepartmentService deptService = new DepartmentService();

    // Filter controls
    private JTextField  keywordField;
    private JComboBox<Department> deptCombo;
    private JTextField  minSalaryField, maxSalaryField;
    private JTextField  joinFromField, joinToField;

    // Results table
    private JTable            table;
    private DefaultTableModel tableModel;
    private JLabel            resultCountLabel;

    public SearchPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIUtils.BG);

        add(buildFilterCard(), BorderLayout.NORTH);
        add(buildResultsCard(), BorderLayout.CENTER);

        loadDepartments();
        runSearch(); // show all on open
    }

    // ── Filter card ───────────────────────────────────────────

    private JPanel buildFilterCard() {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)));

        // Title
        JLabel title = UIUtils.sectionLabel("🔍  Advanced Search & Filters");
        card.add(title, BorderLayout.NORTH);

        // Filter fields grid
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 8, 14);
        g.weightx = 1.0;

        // Row 0 labels
        g.gridy = 0;
        g.gridx = 0; grid.add(UIUtils.formLabel("Search (Name or ID)"), g);
        g.gridx = 1; grid.add(UIUtils.formLabel("Department"), g);
        g.gridx = 2; grid.add(UIUtils.formLabel("Min Salary ($)"), g);
        g.gridx = 3; grid.add(UIUtils.formLabel("Max Salary ($)"), g);
        g.gridx = 4; grid.add(UIUtils.formLabel("Joined From (YYYY-MM-DD)"), g);
        g.gridx = 5; g.insets = new Insets(0, 0, 8, 0);
        grid.add(UIUtils.formLabel("Joined To (YYYY-MM-DD)"), g);

        // Row 1 inputs
        g.gridy = 1; g.insets = new Insets(0, 0, 0, 14);

        g.gridx = 0;
        keywordField = UIUtils.styledField(14);
        keywordField.setToolTipText("Search by name or employee ID");
        grid.add(keywordField, g);

        g.gridx = 1;
        deptCombo = new JComboBox<>();
        deptCombo.setFont(UIUtils.FONT_BODY);
        deptCombo.setBackground(Color.WHITE);
        deptCombo.setPreferredSize(new Dimension(0, 40));
        grid.add(deptCombo, g);

        g.gridx = 2;
        minSalaryField = UIUtils.styledField(8);
        minSalaryField.setToolTipText("Minimum net salary");
        grid.add(minSalaryField, g);

        g.gridx = 3;
        maxSalaryField = UIUtils.styledField(8);
        maxSalaryField.setToolTipText("Maximum net salary");
        grid.add(maxSalaryField, g);

        g.gridx = 4;
        joinFromField = UIUtils.styledField(10);
        joinFromField.setToolTipText("e.g. 2024-01-01");
        grid.add(joinFromField, g);

        g.gridx = 5; g.insets = new Insets(0, 0, 0, 0);
        joinToField = UIUtils.styledField(10);
        joinToField.setToolTipText("e.g. 2025-12-31");
        grid.add(joinToField, g);

        card.add(grid, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton clearBtn  = UIUtils.outlineButton("Clear");
        JButton searchBtn = UIUtils.primaryButton("🔍  Search");
        searchBtn.setPreferredSize(new Dimension(130, 38));
        clearBtn.setPreferredSize(new Dimension(90, 38));

        clearBtn.addActionListener(e  -> clearFilters());
        searchBtn.addActionListener(e -> runSearch());

        // Also search on Enter in keyword field
        keywordField.addActionListener(e -> runSearch());

        btnRow.add(clearBtn);
        btnRow.add(searchBtn);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    // ── Results card ──────────────────────────────────────────

    private JPanel buildResultsCard() {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Search Results");
        title.setFont(UIUtils.FONT_HEADER);
        title.setForeground(UIUtils.TEXT);
        header.add(title, BorderLayout.WEST);

        resultCountLabel = new JLabel("0 employees found");
        resultCountLabel.setFont(UIUtils.FONT_SMALL);
        resultCountLabel.setForeground(UIUtils.TEXT_MUTED);
        header.add(resultCountLabel, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Email", "Phone", "Department", "Joining Date", "Address"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(180);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1, true));
        scroll.getViewport().setBackground(UIUtils.CARD);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ── Data helpers ──────────────────────────────────────────

    private void loadDepartments() {
        try {
            deptCombo.addItem(new Department(0, "All Departments"));
            for (Department d : deptService.getAllDepartments())
                deptCombo.addItem(d);
        } catch (SQLException ex) {
            UIUtils.showError(this, "Could not load departments.");
        }
    }

    // ── Search logic ──────────────────────────────────────────

    public void runSearch() {
        String keyword  = keywordField.getText().trim();
        Department dept = (Department) deptCombo.getSelectedItem();
        int deptId      = (dept != null) ? dept.getId() : 0;

        BigDecimal minSal = parseBD(minSalaryField.getText());
        BigDecimal maxSal = parseBD(maxSalaryField.getText());
        String joinFrom   = joinFromField.getText().trim();
        String joinTo     = joinToField.getText().trim();

        // Validate date format
        if (!joinFrom.isEmpty() && !joinFrom.matches("\\d{4}-\\d{2}-\\d{2}")) {
            UIUtils.showError(this, "Joined From must be in YYYY-MM-DD format."); return;
        }
        if (!joinTo.isEmpty() && !joinTo.matches("\\d{4}-\\d{2}-\\d{2}")) {
            UIUtils.showError(this, "Joined To must be in YYYY-MM-DD format."); return;
        }

        tableModel.setRowCount(0);
        try {
            List<Employee> results = empService.searchEmployees(
                    keyword.isEmpty() ? null : keyword,
                    deptId, minSal, maxSal,
                    joinFrom.isEmpty() ? null : joinFrom,
                    joinTo.isEmpty()   ? null : joinTo);

            for (Employee e : results) {
                tableModel.addRow(new Object[]{
                        e.getId(), e.getName(), e.getEmail(), e.getPhone(),
                        e.getDeptName(),
                        e.getJoiningDate() != null ? e.getJoiningDate() : "—",
                        e.getAddress()
                });
            }
            int n = results.size();
            resultCountLabel.setText(n + " employee" + (n == 1 ? "" : "s") + " found");
            resultCountLabel.setForeground(n > 0 ? UIUtils.ACCENT : UIUtils.DANGER);

        } catch (SQLException ex) {
            UIUtils.showError(this, "Search failed: " + ex.getMessage());
        }
    }

    private void clearFilters() {
        keywordField.setText("");
        if (deptCombo.getItemCount() > 0) deptCombo.setSelectedIndex(0);
        minSalaryField.setText("");
        maxSalaryField.setText("");
        joinFromField.setText("");
        joinToField.setText("");
        runSearch();
    }

    private BigDecimal parseBD(String s) {
        try {
            s = s.trim();
            return s.isEmpty() ? null : new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}