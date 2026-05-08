package ui;

import models.LeaveRequest;
import services.LeaveService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Employee panel – submit leave requests and view their history.
 */
public class LeaveRequestPanel extends JPanel {

    private final int          empId;
    private final LeaveService leaveService = new LeaveService();

    // Form fields
    private JComboBox<String> leaveTypeCombo;
    private JTextField        startDateField, endDateField;
    private JTextArea         reasonArea;
    private JLabel            daysLabel;

    // History table
    private DefaultTableModel tableModel;

    private static final String[] LEAVE_TYPES = {
        "Annual Leave", "Sick Leave", "Emergency Leave",
        "Maternity Leave", "Paternity Leave", "Unpaid Leave", "Other"
    };

    public LeaveRequestPanel(int empId) {
        this.empId = empId;
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildHeader(),      BorderLayout.NORTH);
        add(buildContent(),     BorderLayout.CENTER);

        loadHistory();
    }

    // ── Header ────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel("🏖  Leave Management");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.text());
        p.add(title, BorderLayout.WEST);
        return p;
    }

    // ── Main content: form (left) + history (right) ───────────

    private JPanel buildContent() {
        JPanel split = new JPanel(new GridLayout(1, 2, 20, 0));
        split.setOpaque(false);
        split.add(buildRequestForm());
        split.add(buildHistoryCard());
        return split;
    }

    // ── Request form card ─────────────────────────────────────

    private JPanel buildRequestForm() {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 14));

        JLabel cardTitle = UIUtils.sectionLabel("Submit Leave Request");
        card.add(cardTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIUtils.card());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.gridx = 0;
        g.insets = new Insets(0, 0, 10, 0);

        // Leave type
        g.gridy = 0; form.add(UIUtils.formLabel("Leave Type *"), g);
        leaveTypeCombo = new JComboBox<>(LEAVE_TYPES);
        leaveTypeCombo.setFont(UIUtils.FONT_BODY);
        leaveTypeCombo.setBackground(UIUtils.inputBg());
        leaveTypeCombo.setPreferredSize(new Dimension(0, 40));
        g.gridy = 1; form.add(leaveTypeCombo, g);

        // Start date
        g.gridy = 2; form.add(UIUtils.formLabel("Start Date * (YYYY-MM-DD)"), g);
        startDateField = UIUtils.styledField(14);
        startDateField.setText(LocalDate.now().toString());
        startDateField.addActionListener(e -> updateDaysLabel());
        g.gridy = 3; form.add(startDateField, g);

        // End date
        g.gridy = 4; form.add(UIUtils.formLabel("End Date * (YYYY-MM-DD)"), g);
        endDateField = UIUtils.styledField(14);
        endDateField.setText(LocalDate.now().toString());
        endDateField.addActionListener(e -> updateDaysLabel());
        g.gridy = 5; form.add(endDateField, g);

        // Days preview
        daysLabel = new JLabel("Duration: 1 day");
        daysLabel.setFont(UIUtils.FONT_SUBHEAD);
        daysLabel.setForeground(UIUtils.PRIMARY);
        g.gridy = 6; g.insets = new Insets(0, 0, 14, 0);
        form.add(daysLabel, g);
        g.insets = new Insets(0, 0, 10, 0);

        // Reason
        g.gridy = 7; form.add(UIUtils.formLabel("Reason (optional)"), g);
        reasonArea = new JTextArea(4, 20);
        reasonArea.setFont(UIUtils.FONT_BODY);
        reasonArea.setForeground(UIUtils.text());
        reasonArea.setBackground(UIUtils.inputBg());
        reasonArea.setCaretColor(UIUtils.text());
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.border(), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setBorder(BorderFactory.createEmptyBorder());
        g.gridy = 8; form.add(reasonScroll, g);

        card.add(form, BorderLayout.CENTER);

        // Submit button
        JButton submitBtn = UIUtils.primaryButton("Submit Request");
        submitBtn.setPreferredSize(new Dimension(0, 42));
        submitBtn.addActionListener(e -> handleSubmit());

        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setOpaque(false);
        btnRow.add(submitBtn, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        // Live days update on focus lost
        startDateField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { updateDaysLabel(); }
        });
        endDateField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { updateDaysLabel(); }
        });

        return card;
    }

    // ── History card ──────────────────────────────────────────

    private JPanel buildHistoryCard() {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Header row
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = UIUtils.sectionLabel("My Leave History");
        header.add(title, BorderLayout.WEST);

        JButton refreshBtn = UIUtils.successButton("↻ Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadHistory());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(refreshBtn);
        header.add(btnWrap, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"#", "Type", "From", "To", "Days", "Status", "Applied On"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(50);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);

        // Colour-code status column
        table.getColumnModel().getColumn(5).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object value,
                            boolean sel, boolean focus, int row, int col) {
                        super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                        setBorder(new EmptyBorder(0, 10, 0, 10));
                        if (!sel) {
                            String v = value == null ? "" : value.toString();
                            setForeground(switch (v) {
                                case "✅ Approved" -> UIUtils.ACCENT;
                                case "❌ Rejected" -> UIUtils.DANGER;
                                default            -> UIUtils.WARNING;
                            });
                            setBackground(row % 2 == 0 ? UIUtils.card() : UIUtils.tableAlt());
                            setFont(UIUtils.FONT_SUBHEAD);
                        }
                        return this;
                    }
                });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.border(), 1, true));
        scroll.getViewport().setBackground(UIUtils.card());
        card.add(scroll, BorderLayout.CENTER);

        // Cancel button (for pending requests)
        JButton cancelBtn = UIUtils.dangerButton("Cancel Selected");
        cancelBtn.setPreferredSize(new Dimension(0, 38));
        cancelBtn.setToolTipText("Cancel a pending request");
        cancelBtn.addActionListener(e -> handleCancel(table));
        JPanel cancelRow = new JPanel(new BorderLayout());
        cancelRow.setOpaque(false);
        cancelRow.add(cancelBtn, BorderLayout.CENTER);
        card.add(cancelRow, BorderLayout.SOUTH);

        return card;
    }

    // ── Data loading ──────────────────────────────────────────

    private void loadHistory() {
        tableModel.setRowCount(0);
        try {
            List<LeaveRequest> requests = leaveService.getByEmployee(empId);
            for (LeaveRequest lr : requests) {
                tableModel.addRow(new Object[]{
                        lr.getId(),
                        lr.getLeaveType(),
                        lr.getStartDate(),
                        lr.getEndDate(),
                        lr.getDays() + "d",
                        lr.getStatusDisplay(),
                        lr.getAppliedOn() != null
                                ? lr.getAppliedOn().substring(0, 10)
                                : "—"
                });
            }
        } catch (Exception ex) {
            UIUtils.showError(this, "Failed to load history: " + ex.getMessage());
        }
    }

    // ── Actions ───────────────────────────────────────────────

    private void handleSubmit() {
        String leaveType  = (String) leaveTypeCombo.getSelectedItem();
        String startDate  = startDateField.getText().trim();
        String endDate    = endDateField.getText().trim();
        String reason     = reasonArea.getText().trim();

        // Validate
        if (!startDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            UIUtils.showError(this, "Start date must be in YYYY-MM-DD format."); return;
        }
        if (!endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            UIUtils.showError(this, "End date must be in YYYY-MM-DD format."); return;
        }
        if (endDate.compareTo(startDate) < 0) {
            UIUtils.showError(this, "End date cannot be before start date."); return;
        }

        try {
            leaveService.submitRequest(empId, leaveType, startDate, endDate, reason);
            UIUtils.showInfo(this, "Leave request submitted successfully!\nYour manager will review it shortly.");
            reasonArea.setText("");
            startDateField.setText(LocalDate.now().toString());
            endDateField.setText(LocalDate.now().toString());
            updateDaysLabel();
            loadHistory();
        } catch (Exception ex) {
            UIUtils.showError(this, "Submit failed: " + ex.getMessage());
        }
    }

    private void handleCancel(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Select a request to cancel."); return; }

        String status = tableModel.getValueAt(row, 5).toString();
        if (!status.contains("Pending")) {
            UIUtils.showError(this, "Only pending requests can be cancelled."); return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        if (!UIUtils.confirm(this, "Cancel this leave request?")) return;

        try {
            leaveService.cancelRequest(id);
            loadHistory();
            UIUtils.showInfo(this, "Request cancelled.");
        } catch (Exception ex) {
            UIUtils.showError(this, "Cancel failed: " + ex.getMessage());
        }
    }

    private void updateDaysLabel() {
        try {
            java.time.LocalDate s = java.time.LocalDate.parse(startDateField.getText().trim());
            java.time.LocalDate e = java.time.LocalDate.parse(endDateField.getText().trim());
            long days = java.time.temporal.ChronoUnit.DAYS.between(s, e) + 1;
            if (days < 1) {
                daysLabel.setText("⚠ End date before start date");
                daysLabel.setForeground(UIUtils.DANGER);
            } else {
                daysLabel.setText("Duration: " + days + " day" + (days > 1 ? "s" : ""));
                daysLabel.setForeground(UIUtils.PRIMARY);
            }
        } catch (Exception ex) {
            daysLabel.setText("Duration: —");
            daysLabel.setForeground(UIUtils.TEXT_MUTED);
        }
    }
}