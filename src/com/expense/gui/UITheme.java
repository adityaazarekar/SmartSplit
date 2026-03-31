package com.expense.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.function.Consumer;

public class UITheme {

    // ── Colors ──────────────────────────────────────────────────────────────
    public static final Color BG_DARK        = new Color(0x0D1117);
    public static final Color BG_MEDIUM      = new Color(0x161B22);
    public static final Color BG_CARD        = new Color(0x21262D);
    public static final Color BG_CARD_HOVER  = new Color(0x2D333B);
    public static final Color BG_INPUT       = new Color(0x0D1117);
    public static final Color BORDER         = new Color(0x30363D);
    public static final Color ACCENT         = new Color(0x58A6FF);
    public static final Color ACCENT_PURPLE  = new Color(0xBC8CFF);
    public static final Color ACCENT_GREEN   = new Color(0x3FB950);
    public static final Color ACCENT_ORANGE  = new Color(0xF78166);
    public static final Color ACCENT_YELLOW  = new Color(0xE3B341);
    public static final Color ACCENT_TEAL    = new Color(0x39D353);
    public static final Color SUCCESS        = new Color(0x3FB950);
    public static final Color DANGER         = new Color(0xF85149);
    public static final Color WARNING        = new Color(0xE3B341);
    public static final Color TEXT           = new Color(0xE6EDF3);
    public static final Color TEXT_MUTED     = new Color(0x8B949E);
    public static final Color TEXT_DIM       = new Color(0x484F58);

    // Category emoji map
    public static String getCategoryEmoji(com.expense.model.Category cat) {
        return "";
    }

    // GroupType emoji map
    public static String getGroupEmoji(com.expense.model.GroupType t) {
        return "";
    }

    // ── Fonts ────────────────────────────────────────────────────────────────
    public static final Font FONT_DISPLAY    = new Font("Segoe UI", Font.BOLD, 38);
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_HEADING    = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBHEAD    = new Font("Segoe UI", Font.BOLD, 19);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 18);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_BUTTON     = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_MONO       = new Font("Consolas", Font.PLAIN, 17);

    public static String CURRENCY_SYMBOL = "₹";
    public static String formatAmt(double amount) {
        return CURRENCY_SYMBOL + String.format("%,.0f", amount);
    }

    // ── Global L&F setup ───────────────────────────────────────────────────
    public static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIManager.put("Panel.background", BG_DARK);
        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("ScrollBar.thumb", BG_CARD_HOVER);
        UIManager.put("ScrollBar.track", BG_DARK);
        UIManager.put("ScrollBar.width", 6);
        UIManager.put("TabbedPane.background", BG_DARK);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("TabbedPane.selected", BG_CARD);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabsOverlapBorder", true);
        UIManager.put("Table.background", BG_CARD);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.selectionBackground", BG_CARD_HOVER);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("TableHeader.background", BG_MEDIUM);
        UIManager.put("TableHeader.foreground", TEXT_MUTED);
        UIManager.put("ViewPort.background", BG_DARK);
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", BG_CARD_HOVER);
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("List.background", BG_INPUT);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("List.selectionBackground", ACCENT);
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("TextArea.background", BG_INPUT);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("CheckBox.background", BG_CARD);
        UIManager.put("CheckBox.foreground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.background", BG_CARD);
        UIManager.put("Button.foreground", TEXT);
    }

    // ─────────────────────── COMPONENTS ─────────────────────────────────────

    // Modern pill button
    public static JButton pillButton(String text, Color bg, int w, int h) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true; repaint(); }
                public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = hov ? bg.brighter() : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(FONT_BUTTON);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton pillButton(String text, Color bg) {
        return pillButton(text, bg, 160, 42); // increased default size for larger fonts
    }

    // Ghost (outline) button
    public static JButton ghostButton(String text, Color borderColor) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true; repaint(); }
                public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hov) { g2.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 30)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight()); }
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, getHeight(), getHeight());
                g2.setFont(FONT_BUTTON);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setForeground(borderColor);
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Icon text button (emoji + text)
    public static JButton iconButton(String emoji, String text, Color bg) {
        return pillButton(text, bg);
    }

    // Styled input field
    public static JTextField styledField(String placeholder, int width) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(hasFocus() ? ACCENT : BORDER);
                g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setColor(TEXT_DIM);
                    g3.setFont(FONT_BODY);
                    g3.drawString(placeholder, 12, getHeight()/2 + 5);
                    g3.dispose();
                }
            }
        };
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        field.setFont(FONT_BODY);
        field.setForeground(TEXT);
        field.setCaretColor(ACCENT);
        field.setPreferredSize(new Dimension(width, 46));
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { field.repaint(); }
            public void focusLost(FocusEvent e)   { field.repaint(); }
        });
        return field;
    }

    // Styled password field
    public static JPasswordField styledPassField(String placeholder, int width) {
        JPasswordField field = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(hasFocus() ? ACCENT : BORDER);
                g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
                if (getPassword().length == 0 && !hasFocus()) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setColor(TEXT_DIM);
                    g3.setFont(FONT_BODY);
                    g3.drawString(placeholder, 12, getHeight()/2 + 5);
                    g3.dispose();
                }
            }
        };
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        field.setFont(FONT_BODY);
        field.setForeground(TEXT);
        field.setCaretColor(ACCENT);
        field.setPreferredSize(new Dimension(width, 46));
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { field.repaint(); }
            public void focusLost(FocusEvent e)   { field.repaint(); }
        });
        return field;
    }

    // Styled combobox
    public static <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT);
        combo.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        combo.setPreferredSize(new Dimension(220, 46));
        ((JLabel) combo.getRenderer()).setHorizontalAlignment(JLabel.LEFT);
        return combo;
    }

    // Section label
    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_DIM);
        l.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 0));
        return l;
    }

    // ─────────── Rounded Panel ───────────
    public static class RoundedPanel extends JPanel {
        private int r; private Color bg;
        public RoundedPanel(int r, Color bg) {
            this.r = r; this.bg = bg; setOpaque(false);
        }
        public void setBgColor(Color c) { bg = c; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Bordered rounded panel
    public static class CardPanel extends JPanel {
        private int r; private Color bg, border;
        public CardPanel(int r, Color bg, Color border) {
            this.r = r; this.bg = bg; this.border = border; setOpaque(false);
        }
        public void setBgColor(Color c) { bg = c; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, r, r);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Reflective card for premium/glassy surfaces
    public static class ReflectiveCardPanel extends CardPanel {
        public ReflectiveCardPanel(int r, Color bg, Color border) {
            super(r, bg, border);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Paint sheen = new GradientPaint(0, 0, new Color(255, 255, 255, 26), 0, getHeight() / 2f, new Color(255, 255, 255, 0));
            g2.setPaint(sheen);
            g2.fillRoundRect(2, 2, Math.max(0, getWidth() - 4), Math.max(0, getHeight() / 2), 14, 14);
            g2.dispose();
        }
    }

    // Gradient header panel
    public static class GradientPanel extends JPanel {
        private Color c1, c2;
        public GradientPanel(Color c1, Color c2) { this.c1=c1; this.c2=c2; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0,0,c1,getWidth(),getHeight(),c2));
            g2.fillRect(0,0,getWidth(),getHeight());
            g2.dispose(); super.paintComponent(g);
        }
    }

    // ──── User Avatar (circle with initial) ────
    public static JPanel avatar(String initial, Color color, int size) {
        return new JPanel() {
            { setOpaque(false); setPreferredSize(new Dimension(size, size)); setMinimumSize(new Dimension(size,size)); setMaximumSize(new Dimension(size,size)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0,0,size,size);
                g2.setColor(Color.WHITE);
                int fs = size/2;
                g2.setFont(new Font("Segoe UI", Font.BOLD, fs));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (size-fm.stringWidth(initial))/2, (size+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
    }

    public static JPanel avatar(com.expense.model.User user, int size) {
        return new JPanel() {
            { setOpaque(false); setPreferredSize(new Dimension(size, size)); setMinimumSize(new Dimension(size,size)); setMaximumSize(new Dimension(size,size)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Shape clip = new Ellipse2D.Float(0, 0, size, size);
                g2.setClip(clip);

                String path = user != null ? user.getProfileImagePath() : null;
                boolean painted = false;
                if (path != null && !path.isBlank()) {
                    try {
                        BufferedImage img = ImageIO.read(new File(path));
                        if (img != null) {
                            g2.drawImage(img, 0, 0, size, size, null);
                            painted = true;
                        }
                    } catch (Exception ignored) {}
                }
                if (!painted) {
                    Color c = user != null ? user.getProfileColor() : ACCENT;
                    String init = user != null ? user.getInitial() : "?";
                    g2.setColor(c);
                    g2.fillOval(0,0,size,size);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, size/2));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(init, (size-fm.stringWidth(init))/2, (size+fm.getAscent()-fm.getDescent())/2);
                }
                g2.setClip(null);
                g2.setColor(new Color(255,255,255,35));
                g2.drawOval(0,0,size-1,size-1);
                g2.dispose();
            }
        };
    }

    // ──── Stat card (for home screen) ────
    public static JPanel statCard(String label, String value, Color accent, Color bg) {
        CardPanel card = new CardPanel(14, bg, BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        card.setPreferredSize(new Dimension(185, 105));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel val = new JLabel(value);
        val.setFont(FONT_TITLE);
        val.setForeground(accent);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Color dot
        JPanel dot = new JPanel() {
            { setOpaque(false); setPreferredSize(new Dimension(8,8)); setMaximumSize(new Dimension(8,8)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0,0,8,8);
                g2.dispose();
            }
        };

        card.add(lbl);
        card.add(Box.createVerticalStrut(8));
        card.add(val);
        return card;
    }

    // ──── Sidebar nav button ────
    public static JButton sidebarNavBtn(String emoji, String text) {
        JButton btn = new JButton() {
            boolean hov=false, active=false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov=true; repaint(); }
                public void mouseExited(MouseEvent e)  { hov=false; repaint(); }
            }); setName(text); }
            public void setActive(boolean a) { active=a; repaint(); }
            public boolean isActive() { return active; }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if(hov||active) {
                    g2.setColor(active ? new Color(ACCENT.getRed(),ACCENT.getGreen(),ACCENT.getBlue(),25) : new Color(255,255,255,8));
                    g2.fillRoundRect(6,2,getWidth()-12,getHeight()-4,8,8);
                }
                if(active) {
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(0,6,3,getHeight()-12,3,3);
                }
                g2.setColor(active ? TEXT : TEXT_MUTED);
                int tx = 18;
                if (emoji != null && !emoji.isBlank()) {
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    g2.drawString(emoji, 18, getHeight()/2+5);
                    tx = 46;
                }
                g2.setFont(active ? new Font("Segoe UI",Font.BOLD,14) : FONT_BODY);
                g2.drawString(text, tx, getHeight()/2+5);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(220,42); }
            @Override public Dimension getMaximumSize()   { return new Dimension(Integer.MAX_VALUE,42); }
        };
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ──── Fade-in animation ────
    public static void fadeIn(JComponent comp, Runnable onDone) {
        final float[] alpha = {0f};
        javax.swing.Timer t = new javax.swing.Timer(16, null);

        t.addActionListener(e -> {
            alpha[0] = Math.min(1f, alpha[0] + 0.07f);
            comp.setVisible(true);
            comp.repaint();
            if (alpha[0] >= 1f) { t.stop(); if (onDone != null) onDone.run(); }
        });
        t.start();
    }

    // ──── Themed JScrollPane ────
    public static JScrollPane styledScroll(JComponent view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(null);
        sp.setBackground(BG_DARK);
        sp.getViewport().setBackground(BG_DARK);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        return sp;
    }

    // ──── Divider ────
    public static JPanel divider() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER);
                g.fillRect(0, getHeight()/2, getWidth(), 1);
            }
        };
        d.setOpaque(false);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        d.setPreferredSize(new Dimension(1, 1));
        return d;
    }

    // ──── Tag badge ────
    public static JLabel badge(String text, Color fg, Color bg) {
        JLabel l = new JLabel(" " + text + " ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),getHeight(),getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(fg);
        l.setOpaque(false);
        return l;
    }

    public static void installAmountValidation(JTextField field, int maxDigitsBeforeDecimal, int maxDigitsAfterDecimal) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = current.substring(0, offset) + text + current.substring(offset + length);
                if (isValidAmount(next, maxDigitsBeforeDecimal, maxDigitsAfterDecimal)) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }
        });
    }

    private static boolean isValidAmount(String raw, int before, int after) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) return true;
        if (!s.matches("\\d{0," + before + "}(\\.\\d{0," + after + "})?")) return false;
        return !s.startsWith(".");
    }

    public static void attachHoverLift(JComponent comp, int liftPx) {
        Border base = comp.getBorder();
        comp.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                comp.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(0, 0, liftPx, 0),
                        base
                ));
                comp.repaint();
            }

            @Override public void mouseExited(MouseEvent e) {
                comp.setBorder(base);
                comp.repaint();
            }
        });
    }

    public static void showThemedMessage(Component parent, String title, String message, boolean error) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().setBackground(BG_MEDIUM);
        dlg.setSize(420, 220);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(parent);

        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel icon = new JLabel(error ? "⚠️ Validation" : "✅ Success");
        icon.setFont(FONT_HEADING);
        icon.setForeground(error ? DANGER : ACCENT_GREEN);
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel body = new JLabel("<html><body style='width:340px;color:#8B949E'>" + htmlEscape(message) + "</body></html>");
        body.setFont(FONT_BODY);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton ok = pillButton("OK", error ? DANGER : ACCENT, 100, 36);
        ok.setAlignmentX(Component.LEFT_ALIGNMENT);
        ok.addActionListener(e -> dlg.dispose());

        root.add(icon);
        root.add(Box.createVerticalStrut(10));
        root.add(body);
        root.add(Box.createVerticalGlue());
        root.add(ok);
        dlg.add(root);
        dlg.setVisible(true);
    }

    private static String htmlEscape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ──── Progress bar ────
    public static JPanel progressBar(double pct, Color color, int height) {
        return new JPanel() {
            { setOpaque(false); setPreferredSize(new Dimension(100, height)); setMaximumSize(new Dimension(Integer.MAX_VALUE, height)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER);
                g2.fillRoundRect(0,0,getWidth(),height,height,height);
                int w = (int)(getWidth() * Math.min(1.0, pct));
                if(w>0) { g2.setColor(pct>0.9?DANGER:color); g2.fillRoundRect(0,0,w,height,height,height); }
                g2.dispose();
            }
        };
    }
}
