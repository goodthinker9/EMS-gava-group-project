package ui; // package name

import services.EmployeeService; // importing EmployeeService
import services.PayrollService; // importing PayrollService
import services.DepartmentService; // importing DepartmentService

import javax.swing.*; // importing Swing components
import javax.swing.border.EmptyBorder; // importing EmptyBorder
import javax.swing.table.DefaultTableModel; // importing table model
import java.awt.*; // importing AWT classes
import java.math.BigDecimal; // importing BigDecimal for money calculations
import java.sql.SQLException; // importing SQLException
import java.util.LinkedHashMap; // importing LinkedHashMap
import java.util.Map; // importing Map

/**
 * Analytics & Reports panel for the Admin Dashboard.
 */
public class AnalyticsPanel extends JPanel { // creating AnalyticsPanel class

    // creating service objects
    private final EmployeeService empService = new EmployeeService();
    private final PayrollService payService = new PayrollService();
    private final DepartmentService deptService = new DepartmentService();

    // KPI labels
    private JLabel kpiEmpCount;
    private JLabel kpiTotalPaid;
    private JLabel kpiAvgSalary;
    private JLabel kpiDeptCount;

    // Chart panels
    private BarChartPanel monthlyBarChart;
    private PieChartPanel deptPieChart;
    private BarChartPanel deptSalaryBarChart;

    // Table model
    private DefaultTableModel deptTableModel;

    // constructor
    public AnalyticsPanel() {

        setLayout(new BorderLayout()); // setting layout
        setBackground(UIUtils.BG); // setting background color

        // building content panel
        JPanel content = buildContent();

        // creating scroll pane
        JScrollPane scroll = new JScrollPane(content);

        scroll.setBorder(BorderFactory.createEmptyBorder()); // removing border

        scroll.getVerticalScrollBar().setUnitIncrement(16); // smooth scrolling

        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        ); // disabling horizontal scroll

        scroll.getViewport().setBackground(UIUtils.BG); // viewport background

        add(scroll, BorderLayout.CENTER); // adding scroll pane

        loadAll(); // loading all analytics data
    }

    // ───────────────── CONTENT PANEL ─────────────────

    private JPanel buildContent() {

        JPanel p = new JPanel(); // creating main panel

        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); // vertical layout

        p.setBackground(UIUtils.BG); // background color

        p.setBorder(new EmptyBorder(20, 24, 24, 24)); // padding

        // ───────── HEADER ROW ─────────

        JPanel headerRow = new JPanel(new BorderLayout());

        headerRow.setOpaque(false);

        headerRow.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 44)
        );

        JLabel title = new JLabel("Analytics & Reports");

        title.setFont(UIUtils.FONT_TITLE);

        title.setForeground(UIUtils.TEXT);

        headerRow.add(title, BorderLayout.WEST);

        // refresh button
        JButton refreshBtn = UIUtils.successButton("↻ Refresh");

        refreshBtn.setPreferredSize(new Dimension(110, 34));

        refreshBtn.addActionListener(e -> loadAll());

        JPanel btnWrap = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 4)
        );

        btnWrap.setOpaque(false);

        btnWrap.add(refreshBtn);

        headerRow.add(btnWrap, BorderLayout.EAST);

        // adding components
        p.add(headerRow);

        p.add(Box.createVerticalStrut(16));

        // ───────── KPI CARDS ─────────

        p.add(buildKpiRow());

        p.add(Box.createVerticalStrut(20));

        // ───────── CHART ROW 1 ─────────

        JPanel chartsRow1 = new JPanel(
                new GridLayout(1, 2, 16, 0)
        );

        chartsRow1.setOpaque(false);

        chartsRow1.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 300)
        );

        // creating charts
        monthlyBarChart = new BarChartPanel(
                "Monthly Salary Expense",
                "$"
        );

        deptPieChart = new PieChartPanel(
                "Employees per Department"
        );

        chartsRow1.add(wrapChart(monthlyBarChart));

        chartsRow1.add(wrapChart(deptPieChart));

        p.add(chartsRow1);

        p.add(Box.createVerticalStrut(16));

        // ───────── CHART ROW 2 ─────────

        JPanel chartsRow2 = new JPanel(
                new GridLayout(1, 1, 0, 0)
        );

        chartsRow2.setOpaque(false);

        chartsRow2.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 280)
        );

        deptSalaryBarChart = new BarChartPanel(
                "Total Salary Expense by Department",
                "$"
        );

        chartsRow2.add(wrapChart(deptSalaryBarChart));

        p.add(chartsRow2);

        p.add(Box.createVerticalStrut(20));

        // ───────── TABLE ─────────

        p.add(buildDeptTable());

        p.add(Box.createVerticalStrut(8));

        return p; // returning panel
    }

    // ───────────────── KPI ROW ─────────────────

    private JPanel buildKpiRow() {

        JPanel row = new JPanel(
                new GridLayout(1, 4, 14, 0)
        );

        row.setOpaque(false);

        row.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 100)
        );

        // creating KPI labels
        kpiEmpCount = kpiLabel("0");

        kpiTotalPaid = kpiLabel("$0");

        kpiAvgSalary = kpiLabel("$0");

        kpiDeptCount = kpiLabel("0");

        // adding KPI cards
        row.add(
                buildKpiCard(
                        "Total Employees",
                        kpiEmpCount,
                        UIUtils.PRIMARY,
                        "👥"
                )
        );

        row.add(
                buildKpiCard(
                        "Total Salary Paid",
                        kpiTotalPaid,
                        UIUtils.ACCENT,
                        "💰"
                )
        );

        row.add(
                buildKpiCard(
                        "Avg Net Salary",
                        kpiAvgSalary,
                        UIUtils.WARNING,
                        "📊"
                )
        );

        row.add(
                buildKpiCard(
                        "Departments",
                        kpiDeptCount,
                        new Color(168, 85, 247),
                        "🏢"
                )
        );

        return row;
    }

    // method to create KPI label
    private JLabel kpiLabel(String text) {

        JLabel l = new JLabel(text);

        l.setFont(new Font("Segoe UI", Font.BOLD, 22));

        return l;
    }

    // ───────────────── KPI CARD ─────────────────

    private JPanel buildKpiCard(
            String title,
            JLabel valueLabel,
            Color accent,
            String emoji
    ) {

        JPanel card = new JPanel(
                new BorderLayout(0, 4)
        );

        card.setBackground(UIUtils.CARD);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                UIUtils.BORDER,
                                1,
                                true
                        ),
                        new EmptyBorder(16, 18, 16, 18)
                )
        );

        JPanel top = new JPanel(new BorderLayout());

        top.setOpaque(false);

        JLabel titleLbl = new JLabel(title);

        titleLbl.setFont(UIUtils.FONT_SMALL);

        titleLbl.setForeground(UIUtils.TEXT_MUTED);

        top.add(titleLbl, BorderLayout.CENTER);

        JLabel emojiLbl = new JLabel(emoji);

        emojiLbl.setFont(
                new Font("Segoe UI Emoji", Font.PLAIN, 18)
        );

        top.add(emojiLbl, BorderLayout.EAST);

        valueLabel.setForeground(accent);

        card.add(top, BorderLayout.NORTH);

        card.add(valueLabel, BorderLayout.CENTER);

        // accent border bottom
        JPanel accent2 = new JPanel();

        accent2.setBackground(accent);

        accent2.setPreferredSize(new Dimension(0, 3));

        card.add(accent2, BorderLayout.SOUTH);

        return card;
    }

    // ───────────────── CHART WRAPPER ─────────────────

    private JPanel wrapChart(JPanel chart) {

        JPanel card = new JPanel(new BorderLayout());

        card.setBackground(UIUtils.CARD);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                UIUtils.BORDER,
                                1,
                                true
                        ),
                        new EmptyBorder(14, 14, 14, 14)
                )
        );

        card.add(chart, BorderLayout.CENTER);

        return card;
    }

    // ───────────────── DEPARTMENT TABLE ─────────────────

    private JPanel buildDeptTable() {

        JPanel card = UIUtils.cardPanel();

        card.setLayout(new BorderLayout(0, 10));

        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 260)
        );

        JLabel title = UIUtils.sectionLabel(
                "Department Breakdown"
        );

        card.add(title, BorderLayout.NORTH);

        // table columns
        String[] cols = {
                "Department",
                "Employees",
                "Total Salary Paid",
                "Avg Salary"
        };

        // creating table model
        deptTableModel = new DefaultTableModel(cols, 0) {

            @Override
            public boolean isCellEditable(int r, int c) {
                return false; // disabling editing
            }
        };

        JTable table = new JTable(deptTableModel);

        UIUtils.styleTable(table);

        // setting widths
        table.getColumnModel().getColumn(0).setPreferredWidth(200);

        table.getColumnModel().getColumn(1).setPreferredWidth(100);

        table.getColumnModel().getColumn(2).setPreferredWidth(180);

        table.getColumnModel().getColumn(3).setPreferredWidth(160);

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createLineBorder(
                        UIUtils.BORDER,
                        1,
                        true
                )
        );

        scroll.getViewport().setBackground(UIUtils.CARD);

        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ───────────────── LOAD ALL DATA ─────────────────

    public void loadAll() {

        SwingUtilities.invokeLater(() -> {

            loadKpis();

            loadMonthlyChart();

            loadDeptPieChart();

            loadDeptSalaryChart();

            loadDeptTable();
        });
    }

    // ───────────────── LOAD KPI DATA ─────────────────

    private void loadKpis() {

        try {

            int empCount = empService
                    .getAllEmployees()
                    .size();

            int deptCount = deptService
                    .getAllDepartments()
                    .size();

            kpiEmpCount.setText(
                    String.valueOf(empCount)
            );

            kpiDeptCount.setText(
                    String.valueOf(deptCount)
            );

        } catch (SQLException ignored) {
        }

        try {

            LinkedHashMap<String, BigDecimal> monthly =
                    payService.getMonthlySalaryExpense();

            BigDecimal total = monthly.values()
                    .stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            kpiTotalPaid.setText(
                    String.format("$%,.0f", total)
            );

            // calculating average salary
            int payCount = payService
                    .getAllPayroll()
                    .size();

            if (payCount > 0) {

                BigDecimal avg = total.divide(
                        BigDecimal.valueOf(payCount),
                        2,
                        java.math.RoundingMode.HALF_UP
                );

                kpiAvgSalary.setText(
                        String.format("$%,.0f", avg)
                );

            } else {

                kpiAvgSalary.setText("$0");
            }

        } catch (SQLException ignored) {
        }
    }

    // ───────────────── LOAD MONTHLY CHART ─────────────────

    private void loadMonthlyChart() {

        try {

            LinkedHashMap<String, BigDecimal> raw =
                    payService.getMonthlySalaryExpense();

            LinkedHashMap<String, Double> chartData =
                    new LinkedHashMap<>();

            raw.forEach((k, v) ->
                    chartData.put(k, v.doubleValue())
            );

            monthlyBarChart.setData(chartData);

        } catch (SQLException ignored) {

            monthlyBarChart.setData(
                    new LinkedHashMap<>()
            );
        }
    }

    // ───────────────── LOAD PIE CHART ─────────────────

    private void loadDeptPieChart() {

        try {

            LinkedHashMap<String, Integer> raw =
                    empService.getEmployeeCountByDepartment();

            LinkedHashMap<String, Double> chartData =
                    new LinkedHashMap<>();

            raw.forEach((k, v) ->
                    chartData.put(k, v.doubleValue())
            );

            deptPieChart.setData(chartData);

        } catch (SQLException ignored) {

            deptPieChart.setData(
                    new LinkedHashMap<>()
            );
        }
    }

    // UI updated by Rehima

    // ───────────────── LOAD DEPARTMENT SALARY CHART ─────────────────

    private void loadDeptSalaryChart() {

        try {

            LinkedHashMap<String, BigDecimal> raw =
                    payService.getSalaryByDepartment();

            LinkedHashMap<String, Double> chartData =
                    new LinkedHashMap<>();

            raw.forEach((k, v) ->
                    chartData.put(k, v.doubleValue())
            );

            deptSalaryBarChart.setData(chartData);

        } catch (SQLException ignored) {

            deptSalaryBarChart.setData(
                    new LinkedHashMap<>()
            );
        }
    }

    // ───────────────── LOAD TABLE DATA ─────────────────

    private void loadDeptTable() {

        deptTableModel.setRowCount(0);

        try {

            LinkedHashMap<String, Integer> empByDept =
                    empService.getEmployeeCountByDepartment();

            LinkedHashMap<String, BigDecimal> salByDept =
                    payService.getSalaryByDepartment();

            for (Map.Entry<String, Integer> entry :
                    empByDept.entrySet()) {

                String dept = entry.getKey();

                int empCnt = entry.getValue();

                BigDecimal sal = salByDept.getOrDefault(
                        dept,
                        BigDecimal.ZERO
                );

                BigDecimal avg = empCnt > 0
                        ? sal.divide(
                        BigDecimal.valueOf(empCnt),
                        2,
                        java.math.RoundingMode.HALF_UP
                )
                        : BigDecimal.ZERO;

                deptTableModel.addRow(new Object[]{

                        dept,

                        empCnt,

                        String.format("$%,.2f", sal),

                        String.format("$%,.2f", avg)
                });
            }

        } catch (SQLException ex) {

            UIUtils.showError(
                    this,
                    "Failed to load department data: "
                            + ex.getMessage()
            );
        }
    }
}