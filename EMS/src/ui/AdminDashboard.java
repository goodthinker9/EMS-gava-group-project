package ui;

import models.Employee;
import models.User;
import services.EmployeeService;
import services.PayrollService;
import services.DepartmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Advanced Admin Dashboard with sidebar navigation and stat cards.
 */
public class AdminDashboard extends JFrame {

    private final User            currentUser;
    private final EmployeeService empService  = new EmployeeService();
    private final PayrollService  payService  = new PayrollService();
    private final DepartmentService deptService = new DepartmentService();

    private JTable            empTable;
    private DefaultTableModel empTableModel;

    // Sidebar buttons
    private JButton btnEmployees, btnPayroll, btnAnalytics, btnSearch, btnAttendance, btnLeave;
    private JPanel  contentPanel;
    private CardLayout cardLayout;

    // Stat card value labels
    private JLabel statEmpLabel;
    private JLabel statDeptLabel;
    private JLabel statPayLabel;

    // Panel references for refresh
    private PayrollPanel      payrollPanel;
    private AnalyticsPanel    analyticsPanel;
    private SearchPanel       searchPanel;
    private LeaveApprovalPanel leavePanel;

    public AdminDashboard(User user) {
        this.currentUser = user;
        setTitle("EMS – Admin Dashboard");
        setSize(1200, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.bg());
        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildMain(),     BorderLayout.CENTER);
        add(root);

        // Repaint on theme change
        UIUtils.addThemeListener(() -> SwingUtilities.invokeLater(() -> {
            root.setBackground(UIUtils.bg());
            UIUtils.repaintWindow(this);
        }));
    }

    // ── Sidebar ───────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UIUtils.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BorderLayout());

        // Logo area
        JPanel logo = new JPanel(new GridBagLayout());
        logo.setBackground(new Color(22, 30, 60));
        logo.setPreferredSize(new Dimension(220, 80));
        JLabel logoText = new JLabel("EMS");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoText.setForeground(Color.WHITE);
        JLabel logoSub = new JLabel("  Admin Panel");
        logoSub.setFont(UIUtils.FONT_SMALL);
        logoSub.setForeground(new Color(150, 165, 210));
        JPanel logoInner = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        logoInner.setOpaque(false);
        JLabel icon = new JLabel("👥");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        logoInner.add(icon);
        logoInner.add(logoText);
        logoInner.add(logoSub);
        logo.add(logoInner);
        sidebar.add(logo, BorderLayout.NORTH);

        // Nav items
        JPanel nav = new JPanel();
        nav.setBackground(UIUtils.SIDEBAR_BG);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 0, 0));

        btnEmployees  = sidebarButton("👥   Employees",  true);
        btnPayroll    = sidebarButton("💰   Payroll",    false);
        btnAnalytics  = sidebarButton("📊   Analytics",  false);
        btnSearch     = sidebarButton("🔍   Search",     false);
        btnAttendance = sidebarButton("📅   Attendance", false);
        btnLeave      = sidebarButton("🏖   Leave",      false);

        btnEmployees.addActionListener(e  -> switchTab("employees",  btnEmployees));
        btnPayroll.addActionListener(e    -> switchTab("payroll",    btnPayroll));
        btnAnalytics.addActionListener(e  -> switchTab("analytics",  btnAnalytics));
        btnSearch.addActionListener(e     -> switchTab("search",     btnSearch));
        btnAttendance.addActionListener(e -> switchTab("attendance", btnAttendance));
        btnLeave.addActionListener(e      -> switchTab("leave",      btnLeave));

        nav.add(btnEmployees);
        nav.add(Box.createVerticalStrut(4));
        nav.add(btnPayroll);
        nav.add(Box.createVerticalStrut(4));
        nav.add(btnAnalytics);
        nav.add(Box.createVerticalStrut(4));
        nav.add(btnSearch);
        nav.add(Box.createVerticalStrut(4));
        nav.add(btnAttendance);
        nav.add(Box.createVerticalStrut(4));
        nav.add(btnLeave);
        sidebar.add(nav, BorderLayout.CENTER);

        // User info at bottom
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(22, 30, 60));
        userPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel userIcon = new JLabel("🔑");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);
        JLabel uname = new JLabel(currentUser.getUsername());
        uname.setFont(UIUtils.FONT_SUBHEAD);
        uname.setForeground(Color.WHITE);
        JLabel urole = new JLabel("Administrator");
        urole.setFont(UIUtils.FONT_SMALL);
        urole.setForeground(new Color(150, 165, 210));
        userInfo.add(uname);
        userInfo.add(urole);

        JButton logoutBtn = new JButton("⏻");
        logoutBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        logoutBtn.setForeground(new Color(200, 210, 255));
        logoutBtn.setBackground(new Color(22, 30, 60));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setToolTipText("Logout");
        logoutBtn.addActionListener(e -> logout());

        userPanel.add(userIcon,   BorderLayout.WEST);
        userPanel.add(userInfo,   BorderLayout.CENTER);
        userPanel.add(logoutBtn,  BorderLayout.EAST);
        sidebar.add(userPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton sidebarButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtils.FONT_BODY);
        btn.setForeground(active ? Color.WHITE : new Color(180, 195, 235));
        btn.setBackground(active ? UIUtils.SIDEBAR_ACTIVE : UIUtils.SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 24, 12, 24));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(220, 48));
        btn.setPreferredSize(new Dimension(220, 48));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.getBackground() != UIUtils.SIDEBAR_ACTIVE)
                    btn.setBackground(UIUtils.SIDEBAR_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.getBackground() != UIUtils.SIDEBAR_ACTIVE)
                    btn.setBackground(UIUtils.SIDEBAR_BG);
            }
        });
        return btn;
    }

    private void switchTab(String name, JButton active) {
        cardLayout.show(contentPanel, name);
        for (JButton b : new JButton[]{btnEmployees, btnPayroll, btnAnalytics,
                                        btnSearch, btnAttendance, btnLeave}) {
            b.setBackground(UIUtils.SIDEBAR_BG);
            b.setForeground(new Color(180, 195, 235));
        }
        active.setBackground(UIUtils.SIDEBAR_ACTIVE);
        active.setForeground(Color.WHITE);

        if ("payroll".equals(name))    payrollPanel.reloadEmployeeCombo();
        if ("analytics".equals(name))  analyticsPanel.loadAll();
        if ("search".equals(name))     searchPanel.runSearch();
        if ("leave".equals(name))      leavePanel.loadRequests();
    }

    // ── Main content ──────────────────────────────────────────

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIUtils.BG);

        main.add(buildTopBar(), BorderLayout.NORTH);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIUtils.BG);
        contentPanel.add(buildEmployeePanel(), "employees");
        payrollPanel = new PayrollPanel(null);
        contentPanel.add(payrollPanel, "payroll");
        analyticsPanel = new AnalyticsPanel();
        contentPanel.add(analyticsPanel, "analytics");
        searchPanel = new SearchPanel();
        contentPanel.add(searchPanel, "search");
        contentPanel.add(new AttendancePanel(), "attendance");
        leavePanel = new LeaveApprovalPanel();
        contentPanel.add(leavePanel, "leave");

        main.add(contentPanel, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIUtils.card());
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.border()),
                new EmptyBorder(12, 24, 12, 24)));

        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(UIUtils.FONT_HEADER);
        title.setForeground(UIUtils.text());
        bar.add(title, BorderLayout.WEST);

        // Right side: dark mode toggle + label
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel modeLabel = new JLabel(UIUtils.isDark() ? "🌙 Dark" : "☀ Light");
        modeLabel.setFont(UIUtils.FONT_SMALL);
        modeLabel.setForeground(UIUtils.textMuted());
        JButton darkBtn = UIUtils.darkModeToggle();
        darkBtn.addActionListener(e -> {
            UIUtils.fireThemeChanged();
            modeLabel.setText(UIUtils.isDark() ? "🌙 Dark" : "☀ Light");
            refreshTheme();
        });
        right.add(modeLabel);
        right.add(darkBtn);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private void refreshTheme() {
        // Repaint the entire window to pick up new colours
        SwingUtilities.invokeLater(() -> UIUtils.repaintWindow(this));
    }

    // ── Employee panel ────────────────────────────────────────

    private JPanel buildEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UIUtils.BG);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Stat cards row
        panel.add(buildStatCards(), BorderLayout.NORTH);

        // Table card
        JPanel tableCard = UIUtils.cardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        // Table toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel tableTitle = new JLabel("All Employees");
        tableTitle.setFont(UIUtils.FONT_HEADER);
        tableTitle.setForeground(UIUtils.TEXT);
        toolbar.add(tableTitle, BorderLayout.WEST);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setOpaque(false);

        JButton addBtn    = UIUtils.primaryButton("+ Add Employee");
        JButton editBtn   = UIUtils.outlineButton("✏ Edit");
        JButton deleteBtn = UIUtils.dangerButton("🗑 Delete");
        JButton refreshBtn = UIUtils.successButton("↻ Refresh");

        addBtn.setPreferredSize(new Dimension(150, 36));
        editBtn.setPreferredSize(new Dimension(100, 36));
        deleteBtn.setPreferredSize(new Dimension(100, 36));
        refreshBtn.setPreferredSize(new Dimension(110, 36));

        addBtn.addActionListener(e    -> openEmployeeForm(null));
        editBtn.addActionListener(e   -> editSelectedEmployee());
        deleteBtn.addActionListener(e -> deleteSelectedEmployee());
        refreshBtn.addActionListener(e -> loadEmployees());

        btnGroup.add(refreshBtn);
        btnGroup.add(editBtn);
        btnGroup.add(deleteBtn);
        btnGroup.add(addBtn);
        toolbar.add(btnGroup, BorderLayout.EAST);
        tableCard.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Full Name", "Email", "Phone", "Department", "Address"};
        empTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        empTable = new JTable(empTableModel);
        UIUtils.styleTable(empTable);
        empTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        empTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        empTable.getColumnModel().getColumn(2).setPreferredWidth(210);
        empTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        empTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        empTable.getColumnModel().getColumn(5).setPreferredWidth(200);

        JScrollPane scroll = new JScrollPane(empTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1, true));
        scroll.getViewport().setBackground(UIUtils.CARD);
        tableCard.add(scroll, BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);
        loadEmployees();
        return panel;
    }

    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 4, 0));

        // Create stat cards and keep references to their value labels
        int empCount  = 0;
        int deptCount = 0;
        int payCount  = 0;
        try { empCount  = empService.getAllEmployees().size();    } catch (SQLException ignored) {}
        try { deptCount = deptService.getAllDepartments().size(); } catch (SQLException ignored) {}
        try { payCount  = payService.getAllPayroll().size();      } catch (SQLException ignored) {}

        JPanel[] cards = new JPanel[3];
        cards[0] = buildStatCardWithRef("Total Employees",  String.valueOf(empCount),  UIUtils.PRIMARY, 0);
        cards[1] = buildStatCardWithRef("Departments",       String.valueOf(deptCount), UIUtils.ACCENT,  1);
        cards[2] = buildStatCardWithRef("Payroll Records",   String.valueOf(payCount),  UIUtils.WARNING, 2);

        row.add(cards[0]);
        row.add(cards[1]);
        row.add(cards[2]);
        return row;
    }

    /** Builds a stat card and stores the value label reference for later updates. */
    private JPanel buildStatCardWithRef(String title, String value, Color accent, int index) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(UIUtils.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1, true),
                new EmptyBorder(18, 20, 18, 20)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIUtils.FONT_SMALL);
        titleLbl.setForeground(UIUtils.TEXT_MUTED);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(accent);

        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(4, 0));

        card.add(bar,      BorderLayout.WEST);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);

        // Store reference
        if (index == 0) statEmpLabel  = valueLbl;
        if (index == 1) statDeptLabel = valueLbl;
        if (index == 2) statPayLabel  = valueLbl;

        return card;
    }

    /** Refreshes the three stat card numbers from the database. */
    private void refreshStatCards() {
        try { statEmpLabel.setText(String.valueOf(empService.getAllEmployees().size()));    } catch (SQLException ignored) {}
        try { statDeptLabel.setText(String.valueOf(deptService.getAllDepartments().size())); } catch (SQLException ignored) {}
        try { statPayLabel.setText(String.valueOf(payService.getAllPayroll().size()));       } catch (SQLException ignored) {}
    }

    // ── Data loading ──────────────────────────────────────────

    void loadEmployees() {
        empTableModel.setRowCount(0);
        try {
            List<Employee> employees = empService.getAllEmployees();
            for (Employee e : employees) {
                empTableModel.addRow(new Object[]{
                        e.getId(), e.getName(), e.getEmail(),
                        e.getPhone(), e.getDeptName(), e.getAddress()
                });
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Failed to load employees: " + ex.getMessage());
        }
        // Always refresh stat cards after any employee change
        if (statEmpLabel != null) refreshStatCards();
    }

    // ── Actions ───────────────────────────────────────────────

    private void openEmployeeForm(Employee employee) {
        EmployeeFormDialog dialog = new EmployeeFormDialog(this, employee);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadEmployees();
    }

    private void editSelectedEmployee() {
        int row = empTable.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Please select an employee to edit."); return; }
        int empId = (int) empTableModel.getValueAt(row, 0);
        try {
            Employee emp = empService.getEmployeeById(empId);
            openEmployeeForm(emp);
        } catch (SQLException ex) {
            UIUtils.showError(this, "Error loading employee: " + ex.getMessage());
        }
    }

    private void deleteSelectedEmployee() {
        int row = empTable.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Please select an employee to delete."); return; }
        int empId   = (int) empTableModel.getValueAt(row, 0);
        String name = (String) empTableModel.getValueAt(row, 1);

        if (!UIUtils.confirm(this, "Delete \"" + name + "\"?\nThis will also delete their payroll records.")) return;

        try {
            empService.deleteEmployee(empId);
            loadEmployees();
            UIUtils.showInfo(this, "Employee deleted successfully.");
        } catch (SQLException ex) {
            UIUtils.showError(this, "Delete failed: " + ex.getMessage());
        }
    }

    private void logout() {
        if (UIUtils.confirm(this, "Are you sure you want to logout?")) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}