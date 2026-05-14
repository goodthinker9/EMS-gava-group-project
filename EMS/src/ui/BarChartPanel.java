package ui; // package declaration (UI layer of your project)

import javax.swing.*; // Swing components (JPanel, etc.)
import javax.swing.border.EmptyBorder; // border utility (not used here but imported)
import java.awt.*; // AWT graphics (Graphics, Color, Font, etc.)
import java.awt.geom.RoundRectangle2D; // used for rounded bars
import java.util.LinkedHashMap; // ordered map for chart data
import java.util.Map; // map interface for iteration

/**
 * Custom vertical bar chart – no external library needed.
 * Accepts a LinkedHashMap of { label -> value }.
 */
public class BarChartPanel extends JPanel { // custom Swing panel for bar chart

    private LinkedHashMap<String, Double> data = new LinkedHashMap<>();
    // stores chart data (label -> value), keeps insertion order

    private String title; // chart title
    private String yUnit;  // unit for Y axis (e.g. "$" or "")

    // predefined colors for bars (cycled if more bars than colors)
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

    // padding constants for layout spacing
    private static final int PAD_LEFT   = 70; // space for Y-axis labels
    private static final int PAD_RIGHT  = 20; // right spacing
    private static final int PAD_TOP    = 40; // top space for title
    private static final int PAD_BOTTOM = 60; // bottom space for X labels
    private static final int BAR_GAP    = 12; // space between bars

    // constructor: sets title and unit
    public BarChartPanel(String title, String yUnit) {
        this.title = title; // assign title
        this.yUnit = yUnit; // assign unit
        setBackground(UIUtils.CARD); // set background color of panel
        setPreferredSize(new Dimension(400, 280)); // default size
    }

    // method to set chart data and repaint UI
    public void setData(LinkedHashMap<String, Double> data) {
        this.data = data; // store new data
        repaint(); // redraw chart
    }

    @Override // override Swing paint method
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // clear background first

        Graphics2D g2 = (Graphics2D) g.create(); // create copy of graphics context

        // enable smooth rendering (anti-aliasing)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // smooth text rendering
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();  // panel width
        int h = getHeight(); // panel height

        // ================= TITLE =================
        g2.setFont(UIUtils.FONT_SUBHEAD); // set title font
        g2.setColor(UIUtils.TEXT); // title color

        FontMetrics fm = g2.getFontMetrics(); // measure text width
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, 22);
        // draw centered title at top

        // ================= EMPTY DATA CHECK =================
        if (data.isEmpty()) {
            g2.setFont(UIUtils.FONT_BODY); // normal font
            g2.setColor(UIUtils.TEXT_MUTED); // gray text

            String msg = "No data available"; // message
            g2.drawString(msg, (w - g2.getFontMetrics().stringWidth(msg)) / 2, h / 2);

            g2.dispose(); // release graphics
            return; // stop drawing
        }

        // ================= CHART AREA =================
        int chartW = w - PAD_LEFT - PAD_RIGHT;   // usable width
        int chartH = h - PAD_TOP  - PAD_BOTTOM;  // usable height

        // find maximum value for scaling bars
        double maxVal = data.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1);

        if (maxVal == 0) maxVal = 1; // avoid division by zero

        // ================= GRID LINES (Y AXIS) =================
        g2.setFont(UIUtils.FONT_SMALL); // small font for labels
        g2.setColor(UIUtils.BORDER); // grid color

        int gridLines = 5; // number of horizontal grid lines

        for (int i = 0; i <= gridLines; i++) {

            // compute Y position for each grid line
            int y = PAD_TOP + chartH - (int)(chartH * i / (double) gridLines);

            g2.setColor(UIUtils.BORDER); // draw grid line
            g2.drawLine(PAD_LEFT, y, PAD_LEFT + chartW, y);

            // compute value for this grid level
            double val = maxVal * i / gridLines;

            // format label depending on unit
            String label = yUnit.equals("$")
                    ? String.format("$%,.0f", val)
                    : String.valueOf((int) val);

            g2.setColor(UIUtils.TEXT_MUTED); // label color

            FontMetrics sfm = g2.getFontMetrics(); // measure text
            g2.drawString(label,
                    PAD_LEFT - sfm.stringWidth(label) - 6,
                    y + sfm.getAscent() / 2 - 2);
        }

        // ================= BARS =================
        int n = data.size(); // number of bars

        int totalGap = BAR_GAP * (n + 1); // total spacing
        int barW = Math.max(10, (chartW - totalGap) / n); // bar width

        int i = 0; // bar index

        // loop through data entries
        for (Map.Entry<String, Double> entry : data.entrySet()) {

            double val = entry.getValue(); // bar value

            // compute bar height proportional to max value
            int barH = (int)(chartH * val / maxVal);

            // compute X position
            int x = PAD_LEFT + BAR_GAP + i * (barW + BAR_GAP);

            // compute Y position (bottom aligned)
            int y = PAD_TOP + chartH - barH;

            // pick color
            Color c = BAR_COLORS[i % BAR_COLORS.length];

            // ========== BAR SHADOW ==========
            g2.setColor(new Color(0, 0, 0, 18)); // light shadow
            g2.fill(new RoundRectangle2D.Float(x + 3, y + 3, barW, barH, 6, 6));

            // ========== BAR MAIN ==========
            g2.setColor(c);
            g2.fill(new RoundRectangle2D.Float(x, y, barW, barH, 6, 6));

            // ========== VALUE LABEL ==========
            g2.setFont(UIUtils.FONT_SMALL);
            g2.setColor(UIUtils.TEXT);

            String valStr = yUnit.equals("$")
                    ? String.format("$%,.0f", val)
                    : String.valueOf((int) val);

            FontMetrics vfm = g2.getFontMetrics();
            int vx = x + (barW - vfm.stringWidth(valStr)) / 2;

            // if bar is tall enough, show value inside
            if (barH > 18) {
                g2.setColor(Color.WHITE);
                g2.drawString(valStr, vx, y + 16);
            } else {
                g2.setColor(UIUtils.TEXT);
                g2.drawString(valStr, vx, y - 4);
            }

            // ========== X LABEL ==========
            String lbl = entry.getKey(); // label text

            g2.setColor(UIUtils.TEXT_MUTED);
            g2.setFont(UIUtils.FONT_SMALL);

            FontMetrics lfm = g2.getFontMetrics();

            int lx = x + (barW - lfm.stringWidth(lbl)) / 2;
            int ly = PAD_TOP + chartH + 16;

            // if label is too long → rotate it
            if (lfm.stringWidth(lbl) > barW + BAR_GAP) {

                Graphics2D gr = (Graphics2D) g2.create();

                gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                gr.translate(x + barW / 2, ly); // move origin
                gr.rotate(Math.toRadians(-35)); // rotate label

                gr.setFont(UIUtils.FONT_SMALL);
                gr.setColor(UIUtils.TEXT_MUTED);

                gr.drawString(lbl, 0, 0); // draw rotated text

                gr.dispose(); // cleanup
            } else {
                g2.drawString(lbl, lx, ly); // normal label
            }

            i++; // next bar
        }

        // ================= Y AXIS LINE =================
        g2.setColor(UIUtils.BORDER);
        g2.drawLine(PAD_LEFT, PAD_TOP, PAD_LEFT, PAD_TOP + chartH);

        g2.dispose(); // free graphics resources
    }
}