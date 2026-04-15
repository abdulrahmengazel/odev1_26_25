package config;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class Config {

    // ── Common Colors ───────────────────────────────────────────────────────
    public static final Color BG_DARK    = new Color(30, 33, 40);
    public static final Color BG_PANEL   = new Color(40, 44, 52);
    public static final Color BG_INPUT   = new Color(50, 54, 63);
    public static final Color FG_TEXT    = new Color(220, 220, 220);
    public static final Color FG_DIM     = new Color(130, 135, 145);
    public static final Color ACCENT     = new Color(98, 179, 246);
    public static final Color SUCCESS    = new Color(80, 200, 120);
    public static final Color DANGER     = new Color(230, 80, 80);
    public static final Color GOLD       = new Color(255, 195,  60);
    public static final Color PURPLE     = new Color(180, 130, 255);

    // ── Client Specific Colors ──────────────────────────────────────────────
    public static final Color BG_APP      = new Color(18,  20,  28);
    public static final Color BG_SIDE     = new Color(24,  26,  36);
    public static final Color BG_BUBBLE_R = new Color( 0, 120, 255);   // own  → blue
    public static final Color BG_BUBBLE_L = new Color(48,  53,  68);   // others → dark
    public static final Color BG_BUBBLE_P = new Color(90,  60, 140);   // private → purple
    public static final Color BG_BUBBLE_F = new Color(90,  70,  10);   // file → gold-dark

    // ── Fonts ───────────────────────────────────────────────────────────────
    public static final Font MONO_FONT  = new Font("Consolas", Font.PLAIN, 12);
    public static final Font UI_FONT    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BOLD_FONT  = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 11);

    // ── Shared UI Components ────────────────────────────────────────────────
    public static JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = color.darker().darker(); // or dark variant based on theme
                if      (getModel().isPressed())   g2.setColor(base.darker());
                else if (getModel().isRollover())  g2.setColor(color.darker());
                else                               g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // slightly rounded
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(color);
        btn.setFont(BOLD_FONT);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTextField styledTextField(String text, int cols) {
        JTextField tf = new JTextField(text, cols);
        tf.setBackground(BG_INPUT);
        tf.setForeground(FG_TEXT);
        tf.setCaretColor(FG_TEXT);
        tf.setFont(UI_FONT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(58, 63, 84), 1),
                BorderFactory.createEmptyBorder(5, 9, 5, 9)));
        return tf;
    }

    public static JLabel styledLabel(String text, Font font) {
        JLabel l = new JLabel(text);
        l.setForeground(FG_TEXT);
        if (font != null) l.setFont(font);
        return l;
    }

    public static TitledBorder titledBorder(String title) {
        TitledBorder b = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BG_INPUT, 1), " " + title + " ");
        b.setTitleColor(ACCENT);
        b.setTitleFont(SMALL_FONT.deriveFont(Font.BOLD));
        return b;
    }
}
