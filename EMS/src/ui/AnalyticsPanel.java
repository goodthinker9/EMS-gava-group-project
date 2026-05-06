package ui;

import services.EmployeeService;
import services.PayrollService;
import services.DepartmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Analytics & Reports panel for the Admin Dashboard.
 *
 * Sections:
 *  1. KPI summary row  (total employees, total salary paid, avg salary, dept count)
 *  2. Bar chart        – Monthly salary expense
 *  3. Pie chart        – Employees per department
 *  4. Bar chart        – Salary expense per department
 *  5. Department breakdown table
 */
public class AnalyticsPanel extends JPanel {

    private final EmployeeService   empService  = new EmployeeService();
    private final PayrollService    payService  = new PayrollService();
    private final DepartmentService deptService = new DepartmentService();

    // KPI labels
    private JLabel kpiEmpCount, kpiTotalPaid, kpiAvgSalary, kpiDeptCount;

    // Charts
    private BarChartPanel monthlyBarChart;
    private PieChartPanel deptPieChart;
    private BarChartPanel deptSalaryBarChart;

    // Dept breakdown table
    private DefaultTableModel deptTableModel;

    public AnalyticsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG);

        // Scrollable content
        JPanel content = buildContent();
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(UIUtils.BG);
        add(scroll, BorderLayout.CENTER);

        loadAll();
    }

    // ── Layout ────────────────────────────────────────────────

    private JPanel buildContent() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UIUtils.BG);
        p.setBorder(new EmptyBorder(20, 24, 24, 24));

        // ── Header row ────────────────────────────────────────
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel title = new JLabel("Analytics & Reports");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.TEXT);
        headerRow.add(title, BorderLayout.WEST);

        JButton refreshBtn = UIUtils.successButton("↻ Refresh");
        refreshBtn.setPreferredSize(new Dimension(110, 34));
        refreshBtn.addActionListener(e -> loadAll());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        btnWrap.setOpaque(false);
        btnWrap.add(refreshBtn);
        headerRow.add(btnWrap, BorderLayout.EAST);

        p.add(headerRow);
        p.add(Box.createVerticalStrut(16));

        // ── KPI cards ─────────────────────────────────────────
        p.add(buildKpiRow());
        p.add(Box.createVerticalStrut(20));

        // ── Charts row 1: Monthly salary bar + Dept pie ───────
        JPanel chartsRow1 = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow1.setOpaque(false);
        chartsRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        monthlyBarChart = new BarChartPanel("Monthly Salary Expense", "$");
        deptPieChart    = new PieChartPanel("Employees per Department");

        chartsRow1.add(wrapChart(monthlyBarChart));
        chartsRow1.add(wrapChart(deptPieChart));
        p.add(chartsRow1);
        p.add(Box.createVerticalStrut(16));

        // ── Charts row 2: Dept salary bar ─────────────────────
        JPanel chartsRow2 = new JPanel(new GridLayout(1, 1, 0, 0));
        chartsRow2.setOpaque(false);
        chartsRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        deptSalaryBarChart = new BarChartPanel("Total Salary Expense by Department", "$");
        chartsRow2.add(wrapChart(deptSalaryBarChart));
        p.add(chartsRow2);
        p.add(Box.createVerticalStrut(20));

        // ── Department breakdown table ─────────────────────────
        p.add(buildDeptTable());
        p.add(Box.createVerticalStrut(8));

        return p;
    }

    // ── KPI row ───────────────────────────────────────────────

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        kpiEmpCount  = kpiLabel("0");
        kpiTotalPaid = kpiLabel("$0");
        kpiAvgSalary = kpiLabel("$0");
        kpiDeptCount = kpiLabel("0");

        row.add(buildKpiCard("Total Employees",    kpiEmpCount,  UIUtils.PRIMARY, "👥"));
        row.add(buildKpiCard("Total Salary Paid",  kpiTotalPaid, UIUtils.ACCENT,  "💰"));
        row.add(buildKpiCard("Avg Net Salary",     kpiAvgSalary, UIUtils.WARNING, "📊"));
        row.add(buildKpiCard("Departments",        kpiDeptCount, new Color(168, 85, 247), "🏢"));
        return row;
    }

    private JLabel kpiLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        return l;
    }

    private JPanel buildKpiCard(String title, JLabel valueLabel, Color accent, String emoji) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(UIUtils.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIUtils.FONT_SMALL);
        titleLbl.setForeground(UIUtils.TEXT_MUTED);
        top.add(titleLbl, BorderLayout.CENTER);

        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        top.add(emojiLbl, BorderLayout.EAST);

        valueLabel.setForeground(accent);
        card.add(top,        BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        // Accent bottom border
        JPanel accent2 = new JPanel();
        accent2.setBackground(accent);
        accent2.setPreferredSize(new Dimension(0, 3));
        card.add(accent2, BorderLayout.SOUTH);

        return card;
    }

    // ── Chart wrapper ─────────────────────────────────────────

    private JPanel wrapChart(JPanel chart) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIUtils.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)));
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    // ── Department breakdown table ────────────────────────────

    private JPanel buildDeptTable() {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel title = UIUtils.sectionLabel("Department Breakdown");
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Department", "Employees", "Total Salary Paid", "Avg Salary"};
        deptTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(deptTableModel);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1, true));
        scroll.getViewport().setBackground(UIUtils.CARD);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ── Data loading ──────────────────────────────────────────

    public void loadAll() {
        SwingUtilities.invokeLater(() -> {
            loadKpis();
            loadMonthlyChart();
            loadDeptPieChart();
            loadDeptSalaryChart();
            loadDeptTable();
        });
    }

    private void loadKpis() {
        try {
            int empCount  = empService.getAllEmployees().size();
            int deptCount = deptService.getAllDepartments().size();
            kpiEmpCount.setText(String.valueOf(empCount));
            kpiDeptCount.setText(String.valueOf(deptCount));
        } catch (SQLException ignored) {}

        try {
            LinkedHashMap<String, BigDecimal> monthly = payService.getMonthlySalaryExpense();
            BigDecimal total = monthly.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            kpiTotalPaid.setText(String.format("$%,.0f", total));

            // Avg = total / number of payroll records
            int payCount = payService.getAllPayroll().size();
            if (payCount > 0) {
                BigDecimal avg = total.divide(BigDecimal.valueOf(payCount), 2, java.math.RoundingMode.HALF_UP);
                kpiAvgSalary.setText(String.format("$%,.0f", avg));
            } else {
                kpiAvgSalary.setText("$0");
            }
        } catch (SQLException ignored) {}
    }

    private void loadMonthlyChart() {
        try {
            LinkedHashMap<String, BigDecimal> raw = payService.getMonthlySalaryExpense();
            LinkedHashMap<String, Double> chartData = new LinkedHashMap<>();
            raw.forEach((k, v) -> chartData.put(k, v.doubleValue()));
            monthlyBarChart.setData(chartData);
        } catch (SQLException ignored) {
            monthlyBarChart.setData(new LinkedHashMap<>());
        }
    }

    private void loadDeptPieChart() {
        try {
            LinkedHashMap<String, Integer> raw = empService.getEmployeeCountByDepartment();
            LinkedHashMap<String, Double> chartData = new LinkedHashMap<>();
            raw.forEach((k, v) -> chartData.put(k, v.doubleValue()));
            deptPieChart.setData(chartData);
        } catch (SQLException ignored) {
            deptPieChart.setData(new LinkedHashMap<>());
        }
    }

    private void loadDeptSalaryChart() {
        try {
            LinkedHashMap<String, BigDecimal> raw = payService.getSalaryByDepartment();
            LinkedHashMap<String, Double> chartData = new LinkedHashMap<>();
            raw.forEach((k, v) -> chartData.put(k, v.doubleValue()));
            deptSalaryBarChart.setData(chartData);
        } catch (SQLException ignored) {
            deptSalaryBarChart.setData(new LinkedHashMap<>());
        }
    }

    private void loadDeptTable() {
        deptTableModel.setRowCount(0);
        try {
            LinkedHashMap<String, Integer>    empByDept  = empService.getEmployeeCountByDepartment();
            LinkedHashMap<String, BigDecimal> salByDept  = payService.getSalaryByDepartment();

            for (Map.Entry<String, Integer> entry : empByDept.entrySet()) {
                String dept    = entry.getKey();
                int    empCnt  = entry.getValue();
                BigDecimal sal = salByDept.getOrDefault(dept, BigDecimal.ZERO);
                BigDecimal avg = empCnt > 0
                        ? sal.divide(BigDecimal.valueOf(empCnt), 2, java.math.RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                deptTableModel.addRow(new Object[]{
                        dept,
                        empCnt,
                        String.format("$%,.2f", sal),
                        String.format("$%,.2f", avg)
                });
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Failed to load department data: " + ex.getMessage());
        }
    }
}