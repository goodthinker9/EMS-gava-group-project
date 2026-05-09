package ui;

import models.Attendance;
import models.Employee;
import models.User;
import services.AttendanceService;
import services.EmployeeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Modern Employee Dashboard with sidebar and profile card.
 */
public class EmployeeDashboard extends JFrame {

    private final User            currentUser;
    private final EmployeeService empService  = new EmployeeService();
    private final AttendanceService attService = new AttendanceService();
    private Employee currentEmployee;

    private JPanel    contentPanel;
    private CardLayout cardLayout;
    private JButton   btnProfile, btnPayroll, btnAttendance, btnLeave;

    public EmployeeDashboard(User user) {
        this.currentUser = user;
        setTitle("EMS – Employee Portal");
        setSize(1050, 680);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 550));

        try {
            currentEmployee = empService.getEmployeeByUserId(user.getId());
        } catch (SQLException ex) {
            UIUtils.showError(null, "Could not load profile: " + ex.getMessage());
        }

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMain(),    BorderLayout.CENTER);
        add(root);

        UIUtils.addThemeListener(() -> SwingUtilities.invokeLater(() -> {
            root.setBackground(UIUtils.bg());
            UIUtils.repaintWindow(this);
        }));
    }

    // ── Sidebar ───────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UIUtils.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));

        // Logo
        JPanel logo = new JPanel(new GridBagLayout());
        logo.setBackground(new Color(22, 30, 60));
        logo.setPreferredSize(new Dimension(220, 80));
        JPanel logoInner = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        logoInner.setOpaque(false);
        JLabel icon = new JLabel("👤");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JLabel logoText = new JLabel("EMS");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoText.setForeground(Color.WHITE);
        JLabel logoSub = new JLabel("  Employee");
        logoSub.setFont(UIUtils.FONT_SMALL);
        logoSub.setForeground(new Color(150, 165, 210));
        logoInner.add(icon);
        logoInner.add(logoText);
        logoInner.add(logoSub);
        logo.add(logoInner);
        sidebar.add(logo, BorderLayout.NORTH);

        // Nav
        JPanel nav = new JPanel();
        nav.setBackground(UIUtils.SIDEBAR_BG);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 0, 0));

        btnProfile    = sidebarBtn("👤   My Profile",    true);
        btnPayroll    = sidebarBtn("💰   My Payroll",    false);
        btnAttendance = sidebarBtn("📅   My Attendance", false);
        btnLeave      = sidebarBtn("🏖   My Leave",      false);

        btnProfile.addActionListener(e    -> switchTab("profile",    btnProfile));
        btnPayroll.addActionListener(e    -> switchTab("payroll",    btnPayroll));
        btnAttendance.addActionListener(e -> switchTab("attendance", btnAttendance));
        btnLeave.addActionListener(e      -> switchTab("leave",      btnLeave));

        nav.add(btnProfile);
        nav.add(Box.createVerticalStrut(4));
        nav.add(btnPayroll);
        nav.add(Box.createVerticalStrut(4));
        nav.add(btnAttendance);
        nav.add(Box.createVerticalStrut(4));
        nav.add(btnLeave);
        sidebar.add(nav, BorderLayout.CENTER);

        // User info
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(22, 30, 60));
        userPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);
        String displayName = (currentEmployee != null) ? currentEmployee.getName() : currentUser.getUsername();
        JLabel uname = new JLabel(displayName);
        uname.setFont(UIUtils.FONT_SUBHEAD);
        uname.setForeground(Color.WHITE);
        JLabel urole = new JLabel("Employee");
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

        userPanel.add(userInfo,  BorderLayout.CENTER);
        userPanel.add(logoutBtn, BorderLayout.EAST);
        sidebar.add(userPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton sidebarBtn(String text, boolean active) {
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
        for (JButton b : new JButton[]{btnProfile, btnPayroll, btnAttendance, btnLeave}) {
            b.setBackground(UIUtils.SIDEBAR_BG);
            b.setForeground(new Color(180, 195, 235));
        }
        active.setBackground(UIUtils.SIDEBAR_ACTIVE);
        active.setForeground(Color.WHITE);
    }

    // ── Main content ──────────────────────────────────────────

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIUtils.BG);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIUtils.card());
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.border()),
                new EmptyBorder(12, 24, 12, 24)));
        JLabel title = new JLabel("Employee Portal");
        title.setFont(UIUtils.FONT_HEADER);
        title.setForeground(UIUtils.text());
        topBar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel modeLabel = new JLabel(UIUtils.isDark() ? "🌙 Dark" : "☀ Light");
        modeLabel.setFont(UIUtils.FONT_SMALL);
        modeLabel.setForeground(UIUtils.textMuted());
        JButton darkBtn = UIUtils.darkModeToggle();
        darkBtn.addActionListener(e -> {
            UIUtils.fireThemeChanged();
            modeLabel.setText(UIUtils.isDark() ? "🌙 Dark" : "☀ Light");
            UIUtils.repaintWindow(this);
        });
        right.add(modeLabel);
        right.add(darkBtn);
        topBar.add(right, BorderLayout.EAST);
        main.add(topBar, BorderLayout.NORTH);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIUtils.BG);
        contentPanel.add(buildProfilePanel(), "profile");

        if (currentEmployee != null) {
            contentPanel.add(new PayrollPanel(currentEmployee.getId()), "payroll");
            contentPanel.add(buildAttendancePanel(), "attendance");
            contentPanel.add(new LeaveRequestPanel(currentEmployee.getId()), "leave");
        } else {
            contentPanel.add(emptyPanel("No payroll records found."),    "payroll");
            contentPanel.add(emptyPanel("No attendance records found."), "attendance");
            contentPanel.add(emptyPanel("No leave records found."),      "leave");
        }

        main.add(contentPanel, BorderLayout.CENTER);
        return main;
    }

    // ── Profile panel ─────────────────────────────────────────

    private JPanel buildProfilePanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UIUtils.BG);
        outer.setBorder(new EmptyBorder(24, 28, 24, 28));

        if (currentEmployee == null) {
            JLabel msg = new JLabel("No employee profile linked to this account.", SwingConstants.CENTER);
            msg.setFont(UIUtils.FONT_BODY);
            msg.setForeground(UIUtils.TEXT_MUTED);
            outer.add(msg, BorderLayout.CENTER);
            return outer;
        }

        JPanel content = new JPanel(new GridLayout(1, 2, 20, 0));
        content.setOpaque(false);

        // ── Left: Profile info card ───────────────────────────
        JPanel infoCard = UIUtils.cardPanel();
        infoCard.setLayout(new BorderLayout(0, 16));

        // Avatar area
        JPanel avatarArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        avatarArea.setOpaque(false);
        JLabel avatar = new JLabel(getInitials(currentEmployee.getName()));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 28));
        avatar.setForeground(Color.WHITE);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(70, 70));
        avatar.setOpaque(true);
        avatar.setBackground(UIUtils.PRIMARY);
        avatar.setBorder(BorderFactory.createLineBorder(UIUtils.PRIMARY_LIGHT, 3, true));
        avatarArea.add(avatar);

        JPanel nameArea = new JPanel(new GridLayout(2, 1));
        nameArea.setOpaque(false);
        nameArea.setBorder(new EmptyBorder(10, 14, 0, 0));
        JLabel nameLabel = new JLabel(currentEmployee.getName());
        nameLabel.setFont(UIUtils.FONT_HEADER);
        nameLabel.setForeground(UIUtils.TEXT);
        JLabel deptLabel = new JLabel(currentEmployee.getDeptName());
        deptLabel.setFont(UIUtils.FONT_BODY);
        deptLabel.setForeground(UIUtils.TEXT_MUTED);
        nameArea.add(nameLabel);
        nameArea.add(deptLabel);
        avatarArea.add(nameArea);
        infoCard.add(avatarArea, BorderLayout.NORTH);

        // Info rows
        JPanel infoRows = new JPanel(new GridLayout(0, 1, 0, 12));
        infoRows.setOpaque(false);
        infoRows.add(infoRow("📧  Email",      currentEmployee.getEmail()));
        infoRows.add(infoRow("🏢  Department", currentEmployee.getDeptName()));
        infoRows.add(UIUtils.separator());
        infoRows.add(infoRow("📞  Phone",   currentEmployee.getPhone() != null ? currentEmployee.getPhone() : "—"));
        infoRows.add(infoRow("📍  Address", currentEmployee.getAddress() != null ? currentEmployee.getAddress() : "—"));
        infoCard.add(infoRows, BorderLayout.CENTER);

        // ── Right: Edit contact card ──────────────────────────
        JPanel editCard = UIUtils.cardPanel();
        editCard.setLayout(new BorderLayout(0, 16));

        JLabel editTitle = UIUtils.sectionLabel("Update Contact Info");
        editCard.add(editTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; gbc.insets = new Insets(0, 0, 10, 0);

        gbc.gridy = 0; fields.add(UIUtils.formLabel("Phone Number"), gbc);
        JTextField phoneField = UIUtils.styledField(20);
        phoneField.setText(currentEmployee.getPhone());
        gbc.gridy = 1; fields.add(phoneField, gbc);

        gbc.gridy = 2; fields.add(UIUtils.formLabel("Address"), gbc);
        JTextField addressField = UIUtils.styledField(20);
        addressField.setText(currentEmployee.getAddress());
        gbc.gridy = 3; fields.add(addressField, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(16, 0, 0, 0);
        JButton saveBtn = UIUtils.primaryButton("Save Changes");
        saveBtn.setPreferredSize(new Dimension(160, 40));
        saveBtn.addActionListener(e -> {
            String phone   = phoneField.getText().trim();
            String address = addressField.getText().trim();
            try {
                empService.updateContactInfo(currentEmployee.getId(), phone, address);
                currentEmployee.setPhone(phone);
                currentEmployee.setAddress(address);
                UIUtils.showInfo(this, "Contact info updated successfully.");
            } catch (SQLException ex) {
                UIUtils.showError(this, "Update failed: " + ex.getMessage());
            }
        });
        fields.add(saveBtn, gbc);
        editCard.add(fields, BorderLayout.CENTER);

        content.add(infoCard);
        content.add(editCard);
        outer.add(content, BorderLayout.NORTH);
        return outer;
    }

    // ── My Attendance panel ───────────────────────────────────

    private JPanel buildAttendancePanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 16));
        outer.setBackground(UIUtils.BG);
        outer.setBorder(new EmptyBorder(24, 28, 24, 28));

        // ── Header row ────────────────────────────────────────
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel title = new JLabel("📅  My Attendance");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.TEXT);
        headerRow.add(title, BorderLayout.WEST);
        outer.add(headerRow, BorderLayout.NORTH);

        // ── Summary + filter card ─────────────────────────────
        JPanel filterCard = UIUtils.cardPanel();
        filterCard.setLayout(new BorderLayout(0, 12));

        // Month picker row
        JPanel monthRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        monthRow.setOpaque(false);

        monthRow.add(UIUtils.formLabel("Month (YYYY-MM):"));
        JTextField monthField = UIUtils.styledField(10);
        monthField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        monthField.setPreferredSize(new Dimension(140, 38));
        monthRow.add(monthField);

        JButton loadBtn = UIUtils.primaryButton("Load");
        loadBtn.setPreferredSize(new Dimension(100, 38));
        monthRow.add(loadBtn);

        JButton thisMonthBtn = UIUtils.outlineButton("This Month");
        thisMonthBtn.setPreferredSize(new Dimension(120, 38));
        monthRow.add(thisMonthBtn);

        filterCard.add(monthRow, BorderLayout.NORTH);

        // Summary KPI row (present / absent / half day / leave)
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);

        JLabel kpiPresent  = kpiMini("Present",  "0", UIUtils.ACCENT);
        JLabel kpiAbsent   = kpiMini("Absent",   "0", UIUtils.DANGER);
        JLabel kpiHalf     = kpiMini("Half Day", "0", UIUtils.WARNING);
        JLabel kpiLeave    = kpiMini("Leave",    "0", new Color(168, 85, 247));

        kpiRow.add(kpiPresent.getParent());
        kpiRow.add(kpiAbsent.getParent());
        kpiRow.add(kpiHalf.getParent());
        kpiRow.add(kpiLeave.getParent());
        filterCard.add(kpiRow, BorderLayout.CENTER);

        outer.add(filterCard, BorderLayout.CENTER);

        // ── Attendance table card ─────────────────────────────
        JPanel tableCard = UIUtils.cardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel tableTitle = new JLabel("Daily Attendance Records");
        tableTitle.setFont(UIUtils.FONT_HEADER);
        tableTitle.setForeground(UIUtils.TEXT);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Date", "Day", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);

        // Colour-code the Status column
        table.getColumnModel().getColumn(2).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object value,
                            boolean sel, boolean focus, int row, int col) {
                        super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                        setBorder(new EmptyBorder(0, 14, 0, 14));
                        if (!sel) {
                            String v = value == null ? "" : value.toString();
                            setBackground(switch (v) {
                                case "✅ Present"  -> new Color(220, 252, 231);
                                case "❌ Absent"   -> new Color(254, 226, 226);
                                case "🌓 Half Day" -> new Color(254, 249, 195);
                                case "🏖 Leave"    -> new Color(237, 233, 254);
                                default            -> UIUtils.CARD;
                            });
                            setForeground(UIUtils.TEXT);
                        }
                        return this;
                    }
                });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1, true));
        scroll.getViewport().setBackground(UIUtils.CARD);
        tableCard.add(scroll, BorderLayout.CENTER);

        outer.add(tableCard, BorderLayout.SOUTH);

        // ── Load action ───────────────────────────────────────
        Runnable loadData = () -> {
            String month = monthField.getText().trim();
            if (!month.matches("\\d{4}-\\d{2}")) {
                UIUtils.showError(outer, "Month must be in YYYY-MM format."); return;
            }
            model.setRowCount(0);
            int present = 0, absent = 0, half = 0, leave = 0;
            try {
                List<Attendance> records =
                        attService.getByEmployeeAndMonth(currentEmployee.getId(), month);

                for (Attendance a : records) {
                    // Parse date to get day name
                    String dayName = "";
                    try {
                        LocalDate d = LocalDate.parse(a.getAttDate());
                        dayName = d.getDayOfWeek().toString().charAt(0)
                                + d.getDayOfWeek().toString().substring(1).toLowerCase();
                    } catch (Exception ignored) {}

                    model.addRow(new Object[]{
                            a.getAttDate(),
                            dayName,
                            a.getStatusDisplay()
                    });

                    switch (a.getStatus()) {
                        case PRESENT  -> present++;
                        case ABSENT   -> absent++;
                        case HALF_DAY -> half++;
                        case LEAVE    -> leave++;
                    }
                }

                // Update KPI labels
                kpiPresent.setText(String.valueOf(present));
                kpiAbsent.setText(String.valueOf(absent));
                kpiHalf.setText(String.valueOf(half));
                kpiLeave.setText(String.valueOf(leave));

                if (records.isEmpty()) {
                    model.addRow(new Object[]{"—", "—", "No records for this month"});
                }

            } catch (SQLException ex) {
                UIUtils.showError(outer, "Failed to load attendance: " + ex.getMessage());
            }
        };

        loadBtn.addActionListener(e -> loadData.run());
        thisMonthBtn.addActionListener(e -> {
            monthField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
            loadData.run();
        });

        // Auto-load current month on open
        loadData.run();

        return outer;
    }

    /** Creates a mini KPI card and returns the value label for later updates. */
    private JLabel kpiMini(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(UIUtils.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIUtils.FONT_SMALL);
        titleLbl.setForeground(UIUtils.TEXT_MUTED);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLbl.setForeground(color);

        JPanel accent = new JPanel();
        accent.setBackground(color);
        accent.setPreferredSize(new Dimension(0, 3));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        card.add(accent,   BorderLayout.SOUTH);

        // Return the value label so the caller can update it
        return valueLbl;
    }

    private JPanel emptyPanel(String message) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIUtils.BG);
        JLabel msg = new JLabel(message, SwingConstants.CENTER);
        msg.setFont(UIUtils.FONT_BODY);
        msg.setForeground(UIUtils.TEXT_MUTED);
        p.add(msg);
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────

    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_SMALL);
        lbl.setForeground(UIUtils.TEXT_MUTED);
        JLabel val = new JLabel(value);
        val.setFont(UIUtils.FONT_BODY);
        val.setForeground(UIUtils.TEXT);
        row.add(lbl, BorderLayout.NORTH);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}