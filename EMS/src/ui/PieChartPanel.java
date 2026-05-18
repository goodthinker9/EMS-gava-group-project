package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom pie / donut chart – no external library needed.
 * Accepts a LinkedHashMap of { label -> value }.
 */
public class PieChartPanel extends JPanel {

    private LinkedHashMap<String, Double> data = new LinkedHashMap<>();
    private String title;

    private static final Color[] SLICE_COLORS = {
        UIUtils.PRIMARY,
        UIUtils.ACCENT,
        UIUtils.WARNING,
        new Color(168, 85, 247),
        new Color(236, 72, 153),
        new Color(20, 184, 166),
        new Color(245, 101, 101),
        new Color(251, 191, 36),
    };

    public PieChartPanel(String title) {
        this.title = title;
        setBackground(UIUtils.CARD);
        setPreferredSize(new Dimension(340, 280));
    }

    public void setData(LinkedHashMap<String, Double> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
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

        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) total = 1;

        // Pie area: left half
        int legendW = 130;
        int pieSize = Math.min(w - legendW - 20, h - 50);
        int px = 10;
        int py = (h - pieSize) / 2 + 10;

        // Donut hole
        int holeSize = (int)(pieSize * 0.42);
        int hx = px + (pieSize - holeSize) / 2;
        int hy = py + (pieSize - holeSize) / 2;

        double startAngle = 0;
        int i = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double sweep = 360.0 * entry.getValue() / total;
            Color c = SLICE_COLORS[i % SLICE_COLORS.length];

            // Slice
            g2.setColor(c);
            g2.fill(new Arc2D.Double(px, py, pieSize, pieSize, startAngle, sweep, Arc2D.PIE));

            // Thin white border between slices
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Arc2D.Double(px, py, pieSize, pieSize, startAngle, sweep, Arc2D.PIE));

            startAngle += sweep;
            i++;
        }

        // Donut hole (white circle in center)
        g2.setColor(UIUtils.CARD);
        g2.fill(new Ellipse2D.Double(hx, hy, holeSize, holeSize));

        // Center text: total count
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.setColor(UIUtils.TEXT);
        String centerText = String.valueOf(data.values().stream().mapToInt(Double::intValue).sum());
        FontMetrics cfm = g2.getFontMetrics();
        g2.drawString(centerText,
                hx + (holeSize - cfm.stringWidth(centerText)) / 2,
                hy + holeSize / 2 + cfm.getAscent() / 2 - 4);
        g2.setFont(UIUtils.FONT_SMALL);
        g2.setColor(UIUtils.TEXT_MUTED);
        String sub = "total";
        FontMetrics sfm = g2.getFontMetrics();
        g2.drawString(sub,
                hx + (holeSize - sfm.stringWidth(sub)) / 2,
                hy + holeSize / 2 + cfm.getAscent() / 2 + 12);

        // Legend (right side)
        int lx = px + pieSize + 16;
        int ly = py + 10;
        int swatchSize = 12;
        int lineH = 22;

        i = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            Color c = SLICE_COLORS[i % SLICE_COLORS.length];
            double pct = 100.0 * entry.getValue() / total;

            // Color swatch
            g2.setColor(c);
            g2.fillRoundRect(lx, ly + i * lineH + 2, swatchSize, swatchSize, 4, 4);

            // Label
            g2.setFont(UIUtils.FONT_SMALL);
            g2.setColor(UIUtils.TEXT);
            String lbl = entry.getKey();
            if (lbl.length() > 14) lbl = lbl.substring(0, 12) + "…";
            g2.drawString(lbl, lx + swatchSize + 6, ly + i * lineH + 12);

            // Percentage
            g2.setColor(UIUtils.TEXT_MUTED);
            g2.drawString(String.format("%.1f%%", pct), lx + swatchSize + 6, ly + i * lineH + 22);

            i++;
            if (ly + i * lineH > h - 20) break; // don't overflow
        }

        g2.dispose();
    }
}