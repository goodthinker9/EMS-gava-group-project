package ui;

import models.User;
import services.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * Login screen with split-panel design and dark/light mode toggle.
 */
public class LoginFrame extends JFrame {

  private final AuthService authService = new AuthService();
  private JTextField usernameField;
  private JPasswordField passwordField;
  private JLabel errorLabel;

  public LoginFrame() {
    setTitle("EMS – Login");
    setSize(860, 520);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);
    buildUI();
  }

  private void buildUI() {
    getContentPane().removeAll();

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(UIUtils.bg());

    root.add(buildLeftPanel(), BorderLayout.WEST);
    root.add(buildRightPanel(), BorderLayout.CENTER);

    setContentPane(root);
    revalidate();
    repaint();
  }

  // ── Left branding panel ───────────────────────────────────

  private JPanel buildLeftPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setPreferredSize(new Dimension(320, 0));

    // Gradient background via GradientPaint
    panel.setBackground(UIUtils.isDark() ? new Color(30, 35, 70) : new Color(79, 70, 229));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Logo badge
    JLabel logoBadge = new JLabel("EMS", SwingConstants.CENTER);
    logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 20));
    logoBadge.setForeground(Color.WHITE);
    logoBadge.setOpaque(true);
    logoBadge.setBackground(new Color(255, 255, 255, 40));
    logoBadge.setPreferredSize(new Dimension(64, 64));
    logoBadge.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 2, true));
    gbc.gridy = 0;
    gbc.insets = new Insets(0, 128, 16, 128);
    panel.add(logoBadge, gbc);

    JLabel title = new JLabel("EMS Portal", SwingConstants.CENTER);
    title.setFont(new Font("Segoe UI", Font.BOLD, 22));
    title.setForeground(Color.WHITE);
    gbc.gridy = 1;
    gbc.insets = new Insets(0, 20, 6, 20);
    panel.add(title, gbc);

    JLabel sub = new JLabel("Employee Management System", SwingConstants.CENTER);
    sub.setFont(UIUtils.FONT_SMALL);
    sub.setForeground(new Color(200, 210, 255));
    gbc.gridy = 2;
    gbc.insets = new Insets(0, 20, 32, 20);
    panel.add(sub, gbc);

    // Feature list
    String[][] features = {
        { "👥", "Role-based Access Control" },
        { "💰", "Payroll Management" },
        { "📅", "Attendance Tracking" },
        { "📊", "Analytics & Reports" },
        { "🔍", "Advanced Search" }
    };
    for (int i = 0; i < features.length; i++) {
      JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
      row.setOpaque(false);
      JLabel icon = new JLabel(features[i][0]);
      icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
      JLabel lbl = new JLabel(features[i][1]);
      lbl.setFont(UIUtils.FONT_BODY);
      lbl.setForeground(new Color(210, 220, 255));
      row.add(icon);
      row.add(lbl);
      gbc.gridy = 3 + i;
      gbc.insets = new Insets(0, 28, 0, 20);
      panel.add(row, gbc);
    }

    return panel;
  }

  // ── Right form panel ──────────────────────────────────────

  private JPanel buildRightPanel() {
    JPanel outer = new JPanel(new GridBagLayout());
    outer.setBackground(UIUtils.bg());

    JPanel card = new JPanel(new GridBagLayout());
    card.setBackground(UIUtils.card());
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UIUtils.border(), 1, true),
        new EmptyBorder(36, 44, 36, 44)));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    // Dark mode toggle row
    JPanel toggleRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    toggleRow.setOpaque(false);
    JButton darkBtn = UIUtils.darkModeToggle();
    darkBtn.addActionListener(e -> {
      UIUtils.fireThemeChanged();
      buildUI(); // rebuild login screen with new theme
    });
    toggleRow.add(darkBtn);
    gbc.gridy = 0;
    gbc.insets = new Insets(0, 0, 16, 0);
    card.add(toggleRow, gbc);

    // Title
    JLabel welcome = new JLabel("Welcome back 👋");
    welcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
    welcome.setForeground(UIUtils.text());
    gbc.gridy = 1;
    gbc.insets = new Insets(0, 0, 4, 0);
    card.add(welcome, gbc);

    JLabel sub = new JLabel("Sign in to your account");
    sub.setFont(UIUtils.FONT_BODY);
    sub.setForeground(UIUtils.textMuted());
    gbc.gridy = 2;
    gbc.insets = new Insets(0, 0, 28, 0);
    card.add(sub, gbc);

    // Username
    gbc.gridy = 3;
    gbc.insets = new Insets(0, 0, 6, 0);
    card.add(UIUtils.formLabel("Username"), gbc);
    usernameField = UIUtils.styledField(22);
    usernameField.setPreferredSize(new Dimension(300, 42));
    gbc.gridy = 4;
    gbc.insets = new Insets(0, 0, 16, 0);
    card.add(usernameField, gbc);

    // Password
    gbc.gridy = 5;
    gbc.insets = new Insets(0, 0, 6, 0);
    card.add(UIUtils.formLabel("Password"), gbc);
    passwordField = UIUtils.styledPasswordField(22);
    passwordField.setPreferredSize(new Dimension(300, 42));
    gbc.gridy = 6;
    gbc.insets = new Insets(0, 0, 6, 0);
    card.add(passwordField, gbc);

    // Error label
    errorLabel = new JLabel(" ");
    errorLabel.setFont(UIUtils.FONT_SMALL);
    errorLabel.setForeground(UIUtils.DANGER);
    gbc.gridy = 7;
    gbc.insets = new Insets(0, 0, 16, 0);
    card.add(errorLabel, gbc);

    // Sign in button
    JButton loginBtn = UIUtils.primaryButton("Sign In");
    loginBtn.setPreferredSize(new Dimension(300, 44));
    loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
    loginBtn.addActionListener(e -> handleLogin());
    gbc.gridy = 8;
    gbc.insets = new Insets(0, 0, 20, 0);
    card.add(loginBtn, gbc);

    // Hint
    JLabel hint = new JLabel("Default: admin / admin123", SwingConstants.CENTER);
    hint.setFont(UIUtils.FONT_SMALL);
    hint.setForeground(UIUtils.textMuted());
    gbc.gridy = 9;
    gbc.insets = new Insets(0, 0, 0, 0);
    card.add(hint, gbc);

    outer.add(card);
    getRootPane().setDefaultButton(loginBtn);
    return outer;
  }

  // ── Login handler ─────────────────────────────────────────

  private void handleLogin() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
      showError("Please enter both username and password.");
      return;
    }
    try {
      User user = authService.login(username, password);
      if (user == null) {
        showError("Invalid username or password.");
        passwordField.setText("");
        return;
      }
      dispose();
      if (user.getRole() == User.Role.ADMIN) {
        new AdminDashboard(user).setVisible(true);
      } else {
        new EmployeeDashboard(user).setVisible(true);
      }
    } catch (SQLException ex) {
      showError("Database error: " + ex.getMessage());
    }
  }

  private void showError(String msg) {
    if (errorLabel != null)
      errorLabel.setText("⚠  " + msg);
  }

  public static void main(String[] args) {
    UIUtils.applyLookAndFeel();
    SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
  }
}