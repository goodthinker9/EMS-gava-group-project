package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom vertical bar chart – no external library needed.
 * Accepts a LinkedHashMap of { label -> value }.
 */
public class BarChartPanel extends JPanel {

    private LinkedHashMap<String, Double> data = new LinkedHashMap<>();
    private String title;
    private String yUnit;   // e.g. "$" or ""

    private static final Color[] BAR_COLORS = {
        UIUtils.PRIMARY,
        UIUtils.ACCENT,
        UIUtils.WARNING,
        new Color(168, 85, 247),   // purple
        new Color(236, 72, 153),   // pink
        new Color(20, 184, 166),   // teal
        new Color(245, 101, 101),  // red
        new Color(251, 191, 36),   // amber
    };

    private static final int PAD_LEFT   = 70;
    private static final int PAD_RIGHT  = 20;
    private static final int PAD_TOP    = 40;
    private static final int PAD_BOTTOM = 60;
    private static final int BAR_GAP    = 12;

    public BarChartPanel(String title, String yUnit) {
        this.title = title;
        this.yUnit = yUnit;
        setBackground(UIUtils.CARD);
        setPreferredSize(new Dimension(400, 280));
    }

    public void setData(LinkedHashMap<String, Double> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Title
        g2.setFont(UIUtils.FONT_SUBHEAD);
        g2.setColor(UIUtils.TEXT);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, 22);

        if (data.isEmpty()) {
            g2.setFont(UIUtils.FONT_BODY);
            g2.setColor(UIUtils.TEXT_MUTED);
            String msg = "No data available";
            g2.drawString(msg, (w - g2.getFontMetrics().stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        int chartW = w - PAD_LEFT - PAD_RIGHT;
        int chartH = h - PAD_TOP  - PAD_BOTTOM;

        double maxVal = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
        if (maxVal == 0) maxVal = 1;

        // Grid lines + Y labels
        g2.setFont(UIUtils.FONT_SMALL);
        g2.setColor(UIUtils.BORDER);
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int y = PAD_TOP + chartH - (int)(chartH * i / (double) gridLines);
            g2.setColor(UIUtils.BORDER);
            g2.drawLine(PAD_LEFT, y, PAD_LEFT + chartW, y);
            double val = maxVal * i / gridLines;
            String label = yUnit.equals("$")
                    ? String.format("$%,.0f", val)
                    : String.valueOf((int) val);
            g2.setColor(UIUtils.TEXT_MUTED);
            FontMetrics sfm = g2.getFontMetrics();
            g2.drawString(label, PAD_LEFT - sfm.stringWidth(label) - 6,
                    y + sfm.getAscent() / 2 - 2);
        }

        // Bars
        int n = data.size();
        int totalGap = BAR_GAP * (n + 1);
        int barW = Math.max(10, (chartW - totalGap) / n);

        int i = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double val = entry.getValue();
            int barH = (int)(chartH * val / maxVal);
            int x = PAD_LEFT + BAR_GAP + i * (barW + BAR_GAP);
            int y = PAD_TOP + chartH - barH;

            Color c = BAR_COLORS[i % BAR_COLORS.length];

            // Bar shadow
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fill(new RoundRectangle2D.Float(x + 3, y + 3, barW, barH, 6, 6));

            // Bar fill
            g2.setColor(c);
            g2.fill(new RoundRectangle2D.Float(x, y, barW, barH, 6, 6));

            // Value label on top of bar
            g2.setFont(UIUtils.FONT_SMALL);
            g2.setColor(UIUtils.TEXT);
            String valStr = yUnit.equals("$")
                    ? String.format("$%,.0f", val)
                    : String.valueOf((int) val);
            FontMetrics vfm = g2.getFontMetrics();
            int vx = x + (barW - vfm.stringWidth(valStr)) / 2;
            if (barH > 18) {
                g2.setColor(Color.WHITE);
                g2.drawString(valStr, vx, y + 16);
            } else {
                g2.setColor(UIUtils.TEXT);
                g2.drawString(valStr, vx, y - 4);
            }

            // X label (rotated if long)
            String lbl = entry.getKey();
            g2.setColor(UIUtils.TEXT_MUTED);
            g2.setFont(UIUtils.FONT_SMALL);
            FontMetrics lfm = g2.getFontMetrics();
            int lx = x + (barW - lfm.stringWidth(lbl)) / 2;
            int ly = PAD_TOP + chartH + 16;
            if (lfm.stringWidth(lbl) > barW + BAR_GAP) {
                // Rotate label
                Graphics2D gr = (Graphics2D) g2.create();
                gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                gr.translate(x + barW / 2, ly);
                gr.rotate(Math.toRadians(-35));
                gr.setFont(UIUtils.FONT_SMALL);
                gr.setColor(UIUtils.TEXT_MUTED);
                gr.drawString(lbl, 0, 0);
                gr.dispose();
            } else {
                g2.drawString(lbl, lx, ly);
            }

            i++;
        }

        // Y axis line
        g2.setColor(UIUtils.BORDER);
        g2.drawLine(PAD_LEFT, PAD_TOP, PAD_LEFT, PAD_TOP + chartH);

        g2.dispose();
    }
}