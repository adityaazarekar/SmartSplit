package com.expense.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AnimatedGlassPane extends JComponent {
    private int mouseX = -1000;
    private int mouseY = -1000;
    private float time = 0;
    
    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    public AnimatedGlassPane() {
        setOpaque(false);
        // Pass events down by making this completely transparent to clicks
        
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) event;
                if (me.getID() == MouseEvent.MOUSE_MOVED || me.getID() == MouseEvent.MOUSE_DRAGGED) {
                    Point p = me.getLocationOnScreen();
                    SwingUtilities.convertPointFromScreen(p, this);
                    mouseX = p.x;
                    mouseY = p.y;
                }
            }
        }, AWTEvent.MOUSE_MOTION_EVENT_MASK);

        Timer t = new Timer(33, e -> {
            time += 0.05f;
            repaint();
        });
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Determine base color based on active currency
        Color baseAccent = UITheme.ACCENT;
        if (UITheme.CURRENCY_SYMBOL.contains("$") || UITheme.CURRENCY_SYMBOL.contains("A$")) baseAccent = new Color(0x3FB950); // Green
        else if (UITheme.CURRENCY_SYMBOL.contains("€")) baseAccent = new Color(0x58A6FF); // Blue
        else if (UITheme.CURRENCY_SYMBOL.contains("£")) baseAccent = new Color(0xBC8CFF); // Purple
        else if (UITheme.CURRENCY_SYMBOL.contains("¥")) baseAccent = new Color(0xFF7B72); // Red
        else baseAccent = new Color(0xFFA657); // Rupee/Other Orange

        // Draw ambient slow-moving abstract orbs in corners
        float orbitX1 = (float) (w / 2 + Math.cos(time) * w / 3);
        float orbitY1 = (float) (h / 2 + Math.sin(time * 0.8) * h / 3);
        
        float orbitX2 = (float) (w / 2 + Math.cos(time * 1.2 + Math.PI) * w / 3);
        float orbitY2 = (float) (h / 2 + Math.sin(time * 0.9 + Math.PI) * h / 3);

        Color orb1 = new Color(baseAccent.getRed(), baseAccent.getGreen(), baseAccent.getBlue(), 12);
        Color orb2 = new Color(UITheme.ACCENT_PURPLE.getRed(), UITheme.ACCENT_PURPLE.getGreen(), UITheme.ACCENT_PURPLE.getBlue(), 12);
        
        RadialGradientPaint rgp1 = new RadialGradientPaint(orbitX1, orbitY1, w/1.5f, new float[]{0f, 1f}, new Color[]{orb1, new Color(0,0,0,0)});
        g2.setPaint(rgp1);
        g2.fillRect(0, 0, w, h);
        
        RadialGradientPaint rgp2 = new RadialGradientPaint(orbitX2, orbitY2, w/1.5f, new float[]{0f, 1f}, new Color[]{orb2, new Color(0,0,0,0)});
        g2.setPaint(rgp2);
        g2.fillRect(0, 0, w, h);

        // Draw interactive mouse spotlight flashlight
        Color spotColor = new Color(255, 255, 255, 15);
        RadialGradientPaint spot = new RadialGradientPaint(
            Math.max(0, mouseX), Math.max(0, mouseY), 300, 
            new float[]{0f, 1f}, 
            new Color[]{spotColor, new Color(255, 255, 255, 0)}
        );
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.setPaint(spot);
        g2.fillRect(0, 0, w, h);

        g2.dispose();
    }
}
