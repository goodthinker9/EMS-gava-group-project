package ui;

import models.LeaveRequest;
import services.LeaveService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin panel – view all leave requests, approve or reject them.
 */
public class LeaveApprovalPanel extends JPanel {

    private final LeaveService leaveService = new LeaveService();

    private DefaultTableModel tableModel;
    private JTable            table;
    private JLabel            pendingBadge;
    private JComboBox<String> filterCombo;

    public LeaveApprovalPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        loadRequests();
    }

    // ── Header ────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("🏖  Leave Approval");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.text());
        left.add(title);

        // Pending badge
        pendingBadge = new JLabel("0 pending");
        pendingBadge.setFont(UIUtils.FONT_SUBHEAD);
        pendingBadge.setForeground(Color.WHITE);
        pendingBadge.setBackground(UIUtils.WARNING);
        pendingBadge.setOpaque(true);
        pendingBadge.setBorder(new EmptyBorder(3, 10, 3, 10));
        left.add(pendingBadge);
        titleRow.add(left, BorderLayout.WEST);

        // Filter + refresh
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        right.add(UIUtils.formLabel("Filter:"));
        filterCombo = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected"});
        filterCombo.setFont(UIUtils.FONT_BODY);
        filterCombo.setBackground(UIUtils.inputBg());
        filterCombo.setPreferredSize(new Dimension(130, 36));
        filterCombo.addActionListener(e -> loadRequests());
        right.add(filterCombo);

        JButton refreshBtn = UIUtils.successButton("↻ Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 36));
        refreshBtn.addActionListener(e -> loadRequests());
        right.add(refreshBtn);

        titleRow.add(right, BorderLayout.EAST);
        p.add(titleRow, BorderLayout.NORTH);

        // Stats row
        p.add(buildStatsRow(), BorderLayout.CENTER);

        return p;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 80));

        // We'll populate these after loading
        row.add(buildMiniStat("⏳ Pending",  "—", UIUtils.WARNING));
        row.add(buildMiniStat("✅ Approved", "—", UIUtils.ACCENT));
        row.add(buildMiniStat("❌ Rejected", "—", UIUtils.DANGER));

        return row;
    }

    // Mini stat card
    private JPanel buildMiniStat(String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(UIUtils.card());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.border(), 1, true),
                new EmptyBorder(10, 16, 10, 16)));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_SMALL);
        lbl.setForeground(UIUtils.textMuted());

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(color);
        val.setName(label); // used to find and update later

        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    // ── Table ─────────────────────────────────────────────────

    private JPanel buildTable() {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 10));

        String[] cols = {"ID", "Employee", "Leave Type", "From", "To", "Days", "Reason", "Status", "Applied On"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
        table.getColumnModel().getColumn(7).setPreferredWidth(110);
        table.getColumnModel().getColumn(8).setPreferredWidth(130);

        // Colour-code status column (index 7)
        table.getColumnModel().getColumn(7).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object value,
                            boolean sel, boolean focus, int row, int col) {
                        super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                        setBorder(new EmptyBorder(0, 10, 0, 10));
                        if (!sel) {
                            String v = value == null ? "" : value.toString();
                            Color bg = row % 2 == 0 ? UIUtils.card() : UIUtils.tableAlt();
                            setBackground(bg);
                            setForeground(switch (v) {
                                case "✅ Approved" -> UIUtils.ACCENT;
                                case "❌ Rejected" -> UIUtils.DANGER;
                                default            -> UIUtils.WARNING;
                            });
                            setFont(UIUtils.FONT_SUBHEAD);
                        }
                        return this;
                    }
                });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.border(), 1, true));
        scroll.getViewport().setBackground(UIUtils.card());
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ── Action buttons ────────────────────────────────────────

    private JPanel buildActions() {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 0));

        JLabel hint = new JLabel("Select a request above, then:");
        hint.setFont(UIUtils.FONT_BODY);
        hint.setForeground(UIUtils.textMuted());

        JButton approveBtn = UIUtils.successButton("✅  Approve");
        JButton rejectBtn  = UIUtils.dangerButton("❌  Reject");
        JButton detailBtn  = UIUtils.outlineButton("👁  Details");

        approveBtn.setPreferredSize(new Dimension(140, 40));
        rejectBtn.setPreferredSize(new Dimension(140, 40));
        detailBtn.setPreferredSize(new Dimension(120, 40));

        approveBtn.addActionListener(e -> handleApprove());
        rejectBtn.addActionListener(e  -> handleReject());
        detailBtn.addActionListener(e  -> showDetails());

        card.add(hint);
        card.add(detailBtn);
        card.add(approveBtn);
        card.add(rejectBtn);
        return card;
    }

    // ── Data loading ──────────────────────────────────────────

    public void loadRequests() {
        tableModel.setRowCount(0);
        try {
            String filter = (String) filterCombo.getSelectedItem();
            List<LeaveRequest> requests;

            if ("Pending".equals(filter)) {
                requests = leaveService.getByStatus(LeaveRequest.Status.PENDING);
            } else if ("Approved".equals(filter)) {
                requests = leaveService.getByStatus(LeaveRequest.Status.APPROVED);
            } else if ("Rejected".equals(filter)) {
                requests = leaveService.getByStatus(LeaveRequest.Status.REJECTED);
            } else {
                requests = leaveService.getAllRequests();
            }

            int pending = 0, approved = 0, rejected = 0;
            for (LeaveRequest lr : requests) {
                tableModel.addRow(new Object[]{
                        lr.getId(),
                        lr.getEmpName(),
                        lr.getLeaveType(),
                        lr.getStartDate(),
                        lr.getEndDate(),
                        lr.getDays() + "d",
                        lr.getReason() != null && lr.getReason().length() > 40
                                ? lr.getReason().substring(0, 40) + "…"
                                : lr.getReason(),
                        lr.getStatusDisplay(),
                        lr.getAppliedOn() != null
                                ? lr.getAppliedOn().substring(0, 10)
                                : "—"
                });
                switch (lr.getStatus()) {
                    case PENDING  -> pending++;
                    case APPROVED -> approved++;
                    case REJECTED -> rejected++;
                }
            }

            // Update pending badge
            int totalPending = leaveService.countPending();
            pendingBadge.setText(totalPending + " pending");
            pendingBadge.setBackground(totalPending > 0 ? UIUtils.WARNING : UIUtils.ACCENT);

        } catch (Exception ex) {
            UIUtils.showError(this, "Failed to load requests: " + ex.getMessage());
        }
    }

    // ── Actions ───────────────────────────────────────────────

    private void handleApprove() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Select a request to approve."); return; }

        String status = tableModel.getValueAt(row, 7).toString();
        if (!status.contains("Pending")) {
            UIUtils.showError(this, "Only pending requests can be approved."); return;
        }

        int id   = (int) tableModel.getValueAt(row, 0);
        String emp = (String) tableModel.getValueAt(row, 1);

        if (!UIUtils.confirm(this, "Approve leave request for \"" + emp + "\"?")) return;

        try {
            leaveService.updateStatus(id, LeaveRequest.Status.APPROVED);
            loadRequests();
            UIUtils.showInfo(this, "Leave request approved.");
        } catch (Exception ex) {
            UIUtils.showError(this, "Approve failed: " + ex.getMessage());
        }
    }

    private void handleReject() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Select a request to reject."); return; }

        String status = tableModel.getValueAt(row, 7).toString();
        if (!status.contains("Pending")) {
            UIUtils.showError(this, "Only pending requests can be rejected."); return;
        }

        int id   = (int) tableModel.getValueAt(row, 0);
        String emp = (String) tableModel.getValueAt(row, 1);

        if (!UIUtils.confirm(this, "Reject leave request for \"" + emp + "\"?")) return;

        try {
            leaveService.updateStatus(id, LeaveRequest.Status.REJECTED);
            loadRequests();
            UIUtils.showInfo(this, "Leave request rejected.");
        } catch (Exception ex) {
            UIUtils.showError(this, "Reject failed: " + ex.getMessage());
        }
    }

    private void showDetails() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Select a request to view."); return; }

        String emp       = (String) tableModel.getValueAt(row, 1);
        String type      = (String) tableModel.getValueAt(row, 2);
        String from      = (String) tableModel.getValueAt(row, 3);
        String to        = (String) tableModel.getValueAt(row, 4);
        String days      = (String) tableModel.getValueAt(row, 5);
        Object reasonObj = tableModel.getValueAt(row, 6);
        String status    = (String) tableModel.getValueAt(row, 7);
        String applied   = (String) tableModel.getValueAt(row, 8);

        String msg = String.format(
                "Employee  : %s%n" +
                "Type      : %s%n" +
                "Period    : %s  →  %s  (%s)%n" +
                "Status    : %s%n" +
                "Applied   : %s%n%n" +
                "Reason:%n%s",
                emp, type, from, to, days, status, applied,
                reasonObj != null ? reasonObj.toString() : "—");

        JTextArea ta = new JTextArea(msg);
        ta.setFont(UIUtils.FONT_BODY);
        ta.setEditable(false);
        ta.setBackground(UIUtils.card());
        ta.setForeground(UIUtils.text());
        ta.setBorder(new EmptyBorder(10, 10, 10, 10));

        JOptionPane.showMessageDialog(this, new JScrollPane(ta),
                "Leave Request Details", JOptionPane.INFORMATION_MESSAGE);
    }
}package ui; // package name

import models.LeaveRequest; // importing LeaveRequest model class
import services.LeaveService; // importing LeaveService class

import javax.swing.*; // importing Swing components
import javax.swing.border.EmptyBorder; // importing EmptyBorder for spacing
import javax.swing.table.DefaultTableCellRenderer; // importing table cell renderer
import javax.swing.table.DefaultTableModel; // importing table model
import java.awt.*; // importing AWT classes
import java.util.List; // importing List interface

/**
 * Admin panel – view all leave requests, approve or reject them.
 */
public class LeaveApprovalPanel extends JPanel { // creating LeaveApprovalPanel class extending JPanel

    private final LeaveService leaveService = new LeaveService(); // creating LeaveService object

    private DefaultTableModel tableModel; // table model for JTable
    private JTable table; // table component
    private JLabel pendingBadge; // label showing pending requests count
    private JComboBox<String> filterCombo; // combo box for filtering requests

    public LeaveApprovalPanel() { // constructor

        setLayout(new BorderLayout(0, 16)); // setting panel layout
        setBackground(UIUtils.BG); // setting background color
        setBorder(new EmptyBorder(20, 24, 20, 24)); // setting outer padding

        add(buildHeader(), BorderLayout.NORTH); // adding header panel
        add(buildTable(), BorderLayout.CENTER); // adding table panel
        add(buildActions(), BorderLayout.SOUTH); // adding action buttons panel

        loadRequests(); // loading leave requests into table
    }

    // ───────────────── HEADER SECTION ─────────────────

    private JPanel buildHeader() { // method to build header section

        JPanel p = new JPanel(new BorderLayout(0, 8)); // creating header panel
        p.setOpaque(false); // making panel transparent

        // TITLE ROW

        JPanel titleRow = new JPanel(new BorderLayout()); // panel for title row
        titleRow.setOpaque(false); // transparent panel

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // left side panel
        left.setOpaque(false); // transparent

        JLabel title = new JLabel("🏖 Leave Approval"); // title label
        title.setFont(UIUtils.FONT_TITLE); // setting title font
        title.setForeground(UIUtils.text()); // setting title color

        left.add(title); // adding title to left panel

        // PENDING BADGE

        pendingBadge = new JLabel("0 pending"); // creating pending badge label
        pendingBadge.setFont(UIUtils.FONT_SUBHEAD); // setting font
        pendingBadge.setForeground(Color.WHITE); // text color
        pendingBadge.setBackground(UIUtils.WARNING); // background color
        pendingBadge.setOpaque(true); // enabling background color
        pendingBadge.setBorder(new EmptyBorder(3, 10, 3, 10)); // padding

        left.add(pendingBadge); // adding badge to left panel

        titleRow.add(left, BorderLayout.WEST); // adding left panel to title row

        // RIGHT SIDE PANEL

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // right side panel
        right.setOpaque(false); // transparent

        right.add(UIUtils.formLabel("Filter:")); // adding filter label

        filterCombo = new JComboBox<>(new String[]{
                "All",
                "Pending",
                "Approved",
                "Rejected"
        }); // creating combo box with filter options

        filterCombo.setFont(UIUtils.FONT_BODY); // setting font
        filterCombo.setBackground(UIUtils.inputBg()); // background color
        filterCombo.setPreferredSize(new Dimension(130, 36)); // setting size

        filterCombo.addActionListener(e -> loadRequests()); // reload table when selection changes

        right.add(filterCombo); // adding combo box to right panel

        // REFRESH BUTTON

        JButton refreshBtn = UIUtils.successButton("↻ Refresh"); // creating refresh button
        refreshBtn.setPreferredSize(new Dimension(100, 36)); // button size

        refreshBtn.addActionListener(e -> loadRequests()); // reload data on click

        right.add(refreshBtn); // adding button to panel

        titleRow.add(right, BorderLayout.EAST); // adding right panel

        p.add(titleRow, BorderLayout.NORTH); // adding title row to header

        // ADDING STATISTICS ROW

        p.add(buildStatsRow(), BorderLayout.CENTER);

        return p; // returning header panel
    }

    // ───────────────── STATS ROW ─────────────────

    private JPanel buildStatsRow() {

        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0)); // creating grid layout
        row.setOpaque(false); // transparent
        row.setPreferredSize(new Dimension(0, 80)); // row size

        // ADDING MINI STAT CARDS

        row.add(buildMiniStat("⏳ Pending", "—", UIUtils.WARNING));
        row.add(buildMiniStat("✅ Approved", "—", UIUtils.ACCENT));
        row.add(buildMiniStat("❌ Rejected", "—", UIUtils.DANGER));

        return row; // returning row
    }

    // ───────────────── MINI STAT CARD ─────────────────

    private JPanel buildMiniStat(String label, String value, Color color) {

        JPanel card = new JPanel(new BorderLayout(0, 4)); // creating card panel

        card.setBackground(UIUtils.card()); // card background

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIUtils.border(), 1, true),
                        new EmptyBorder(10, 16, 10, 16)
                )
        ); // card border and padding

        JLabel lbl = new JLabel(label); // stat title label
        lbl.setFont(UIUtils.FONT_SMALL); // font
        lbl.setForeground(UIUtils.textMuted()); // text color

        JLabel val = new JLabel(value); // stat value label
        val.setFont(new Font("Segoe UI", Font.BOLD, 22)); // font
        val.setForeground(color); // text color
        val.setName(label); // setting component name

        card.add(lbl, BorderLayout.NORTH); // adding label
        card.add(val, BorderLayout.CENTER); // adding value

        return card; // returning card
    }

    // ───────────────── TABLE SECTION ─────────────────

    private JPanel buildTable() {

        JPanel card = UIUtils.cardPanel(); // creating card panel
        card.setLayout(new BorderLayout(0, 10)); // layout

        // TABLE COLUMN NAMES

        String[] cols = {
                "ID",
                "Employee",
                "Leave Type",
                "From",
                "To",
                "Days",
                "Reason",
                "Status",
                "Applied On"
        };

        // CREATING TABLE MODEL

        tableModel = new DefaultTableModel(cols, 0) {

            @Override
            public boolean isCellEditable(int r, int c) {
                return false; // making table non-editable
            }
        };

        // CREATING TABLE

        table = new JTable(tableModel);

        UIUtils.styleTable(table); // applying custom style

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // single row selection

        // SETTING COLUMN WIDTHS

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
        table.getColumnModel().getColumn(7).setPreferredWidth(110);
        table.getColumnModel().getColumn(8).setPreferredWidth(130);

        // STATUS COLUMN COLOR RENDERER

        table.getColumnModel().getColumn(7).setCellRenderer(
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t,
                            Object value,
                            boolean sel,
                            boolean focus,
                            int row,
                            int col
                    ) {

                        super.getTableCellRendererComponent(
                                t, value, sel, focus, row, col
                        );

                        setBorder(new EmptyBorder(0, 10, 0, 10));

                        if (!sel) {

                            String v = value == null ? "" : value.toString();

                            Color bg = row % 2 == 0
                                    ? UIUtils.card()
                                    : UIUtils.tableAlt();

                            setBackground(bg);

                            setForeground(
                                    switch (v) {
                                        case "✅ Approved" -> UIUtils.ACCENT;
                                        case "❌ Rejected" -> UIUtils.DANGER;
                                        default -> UIUtils.WARNING;
                                    }
                            );

                            setFont(UIUtils.FONT_SUBHEAD);
                        }

                        return this;
                    }
                }
        );

        // SCROLL PANE

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createLineBorder(UIUtils.border(), 1, true)
        );

        scroll.getViewport().setBackground(UIUtils.card());

        card.add(scroll, BorderLayout.CENTER);

        return card;
    }
}