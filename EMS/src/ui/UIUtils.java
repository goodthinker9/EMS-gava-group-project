package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Central UI theme engine – Light / Dark mode.
 * All colour methods are static and safe to call at any time.
 */
public final class UIUtils {

    // ── Theme state ───────────────────────────────────────────
    private static boolean darkMode = false;
    private static final List<Runnable> THEME_LISTENERS = new ArrayList<>();

    public static boolean isDark()        { return darkMode; }
    public static void    setDark(boolean d) { darkMode = d; }
    public static void    toggleDark()    { darkMode = !darkMode; }

    public static void addThemeListener(Runnable r)  { THEME_LISTENERS.add(r); }
    public static void fireThemeChanged() {
        refreshLegacyConstants();
        THEME_LISTENERS.forEach(r -> {
            try { r.run(); } catch (Exception ignored) {}
        });
    }

    // ── Dynamic colours ───────────────────────────────────────
    public static Color bg()           { return darkMode ? new Color(18, 20, 32)    : new Color(245, 247, 251); }
    public static Color card()         { return darkMode ? new Color(26, 30, 50)    : Color.WHITE; }
    public static Color border()       { return darkMode ? new Color(45, 52, 85)    : new Color(220, 225, 240); }
    public static Color text()         { return darkMode ? new Color(220, 225, 245) : new Color(22, 33, 62); }
    public static Color textMuted()    { return darkMode ? new Color(110, 120, 165) : new Color(120, 130, 160); }
    public static Color tableHeader()  { return darkMode ? new Color(22, 26, 44)    : new Color(248, 249, 255); }
    public static Color tableAlt()     { return darkMode ? new Color(22, 26, 42)    : new Color(252, 253, 255); }
    public static Color tableSel()     { return darkMode ? new Color(50, 65, 130)   : new Color(235, 240, 255); }
    public static Color inputBg()      { return darkMode ? new Color(22, 26, 44)    : Color.WHITE; }
    public static Color sidebarBg()    { return darkMode ? new Color(10, 12, 24)    : new Color(18, 24, 52); }
    public static Color sidebarHover() { return darkMode ? new Color(28, 34, 60)    : new Color(30, 38, 75); }

    // ── Fixed accent colours ──────────────────────────────────
    public static final Color PRIMARY        = new Color(99,  102, 241);
    public static final Color PRIMARY_DARK   = new Color(79,  82,  221);
    public static final Color PRIMARY_LIGHT  = new Color(238, 242, 255);
    public static final Color ACCENT         = new Color(16,  185, 129);
    public static final Color ACCENT_DARK    = new Color(5,   150, 105);
    public static final Color DANGER         = new Color(239, 68,  68);
    public static final Color DANGER_DARK    = new Color(220, 38,  38);
    public static final Color WARNING        = new Color(245, 158, 11);
    public static final Color PURPLE         = new Color(168, 85,  247);
    public static final Color SIDEBAR_ACTIVE = new Color(99,  102, 241);

    // ── Legacy constant aliases (delegate to dynamic methods) ─
    // These allow older files to compile unchanged while still
    // respecting the current theme at the time they are read.
    // NOTE: Because Java constants are resolved at compile time,
    // these are fixed values. Use the method forms (bg(), card()
    // etc.) in new code for true dark-mode support.
    public static Color BG          = new Color(245, 247, 251);
    public static Color CARD        = Color.WHITE;
    public static Color TEXT        = new Color(22,  33,  62);
    public static Color TEXT_MUTED  = new Color(120, 130, 160);
    public static Color BORDER      = new Color(220, 225, 240);
    public static Color TABLE_HEADER= new Color(248, 249, 255);
    public static Color TABLE_ALT   = new Color(252, 253, 255);
    public static Color TABLE_SEL   = new Color(235, 240, 255);
    public static Color SIDEBAR_BG  = new Color(18,  24,  52);
    public static Color SIDEBAR_HOVER = new Color(30, 38,  75);

    /** Call this after toggling dark mode to refresh the legacy constants. */
    private static void refreshLegacyConstants() {
        BG           = bg();
        CARD         = card();
        TEXT         = text();
        TEXT_MUTED   = textMuted();
        BORDER       = border();
        TABLE_HEADER = tableHeader();
        TABLE_ALT    = tableAlt();
        TABLE_SEL    = tableSel();
        SIDEBAR_BG   = sidebarBg();
        SIDEBAR_HOVER= sidebarHover();
    }

    // ── Fonts ─────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_SUBHEAD = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_MONO    = new Font("Consolas",  Font.PLAIN, 13);

    private UIUtils() {}

    // ══════════════════════════════════════════════════════════
    //  BUTTONS
    // ══════════════════════════════════════════════════════════

    public static JButton primaryButton(String text) {
        return makeButton(text, PRIMARY, PRIMARY_DARK, Color.WHITE);
    }

    public static JButton dangerButton(String text) {
        return makeButton(text, DANGER, DANGER_DARK, Color.WHITE);
    }

    public static JButton successButton(String text) {
        return makeButton(text, ACCENT, ACCENT_DARK, Color.WHITE);
    }

    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(text());
        btn.setBackground(card());
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border(), 1, true),
                new EmptyBorder(6, 14, 6, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 36));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(tableAlt()); }
            public void mouseExited (MouseEvent e) { btn.setBackground(card()); }
        });
        return btn;
    }

    private static JButton makeButton(String text, Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 36));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited (MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    /**
     * Dark/Light mode toggle button.
     * Clicking it toggles the theme and fires all registered listeners.
     */
    public static JButton darkModeToggle() {
        JButton btn = new JButton();
        updateToggleAppearance(btn);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, 30));
        btn.addActionListener(e -> {
            toggleDark();
            updateToggleAppearance(btn);
            fireThemeChanged();
        });
        return btn;
    }

    private static void updateToggleAppearance(JButton btn) {
        if (darkMode) {
            btn.setText("☀  Light");
            btn.setBackground(new Color(60, 65, 100));
            btn.setForeground(new Color(220, 225, 255));
        } else {
            btn.setText("🌙  Dark");
            btn.setBackground(new Color(30, 35, 70));
            btn.setForeground(Color.WHITE);
        }
        btn.setFont(FONT_SMALL);
    }

    // ══════════════════════════════════════════════════════════
    //  TEXT FIELDS
    // ══════════════════════════════════════════════════════════

    public static JTextField styledField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setForeground(text());
        tf.setBackground(inputBg());
        tf.setCaretColor(text());
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border(), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 40));
        addFocusBorder(tf);
        return tf;
    }

    public static JPasswordField styledPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_BODY);
        pf.setForeground(text());
        pf.setBackground(inputBg());
        pf.setCaretColor(text());
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border(), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        pf.setPreferredSize(new Dimension(pf.getPreferredSize().width, 40));
        addFocusBorder(pf);
        return pf;
    }

    private static void addFocusBorder(JComponent c) {
        c.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2, true),
                        new EmptyBorder(7, 11, 7, 11)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(border(), 1, true),
                        new EmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    // ══════════════════════════════════════════════════════════
    //  LABELS
    // ══════════════════════════════════════════════════════════

    public static JLabel headerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(text());
        return l;
    }

    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADER);
        l.setForeground(PRIMARY);
        return l;
    }

    public static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SUBHEAD);
        l.setForeground(textMuted());
        return l;
    }

    // ══════════════════════════════════════════════════════════
    //  CARDS & PANELS
    // ══════════════════════════════════════════════════════════

    public static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(card());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border(), 1, true),
                new EmptyBorder(20, 24, 20, 24)));
        return p;
    }

    public static JPanel statCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(card());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border(), 1, true),
                new EmptyBorder(18, 20, 18, 20)));

        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(4, 0));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(textMuted());

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(accent);

        card.add(stripe,   BorderLayout.WEST);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    // ══════════════════════════════════════════════════════════
    //  TABLE
    // ══════════════════════════════════════════════════════════

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(tableSel());
        table.setSelectionForeground(text());
        table.setBackground(card());
        table.setFillsViewportHeight(true);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? card() : tableAlt());
                    setForeground(text());
                }
                return this;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SUBHEAD);
        header.setBackground(tableHeader());
        header.setForeground(textMuted());
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, border()));
        header.setReorderingAllowed(false);
        DefaultTableCellRenderer hr = (DefaultTableCellRenderer) header.getDefaultRenderer();
        hr.setBorder(new EmptyBorder(0, 14, 0, 14));
        hr.setHorizontalAlignment(SwingConstants.LEFT);
        hr.setBackground(tableHeader());
        hr.setForeground(textMuted());
    }

    // ══════════════════════════════════════════════════════════
    //  MISC
    // ══════════════════════════════════════════════════════════

    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(border());
        return sep;
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void applyLookAndFeel() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("OptionPane.buttonFont",  FONT_BUTTON);
        UIManager.put("TabbedPane.font",        FONT_SUBHEAD);
    }

    /**
     * Rebuilds a JFrame by disposing and re-showing it.
     * Used for full theme refresh.
     */
    public static void repaintWindow(Window w) {
        if (w == null) return;
        SwingUtilities.invokeLater(w::repaint);
    }
}