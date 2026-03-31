package com.expense.gui;

import com.expense.model.*;
import com.expense.service.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GroupPanel extends JPanel {

    private SmartSplitApp app;
    private Group group;
    private ExpenseService service;
    private PaymentGatewayService paymentGatewayService;

    public GroupPanel(SmartSplitApp app, Group group, ExpenseService service) {
        this.app = app; this.group = group; this.service = service;
        this.paymentGatewayService = new PaymentGatewayService();
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = buildTabs();
        add(tabs, BorderLayout.CENTER);
    }

    // ════════════════════ HEADER ════════════════════
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(UITheme.BG_MEDIUM);
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(18, 28, 18, 28)));

        // Left
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        nameRow.setOpaque(false);
        JLabel emojiLbl = new JLabel(UITheme.getGroupEmoji(group.getType()));
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        JLabel nameLbl = new JLabel(group.getName());
        nameLbl.setFont(UITheme.FONT_TITLE);
        nameLbl.setForeground(UITheme.TEXT);
        nameRow.add(emojiLbl);
        nameRow.add(nameLbl);
        nameRow.add(Box.createHorizontalStrut(8));
        nameRow.add(UITheme.badge(group.getType().getDisplayName(), UITheme.ACCENT, new Color(0x0D1F33)));
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        metaRow.setOpaque(false);
        metaRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Avatars
        for (int i = 0; i < Math.min(group.getMembers().size(), 6); i++) {
            User u = group.getMembers().get(i);
            metaRow.add(UITheme.avatar(u, 28));
        }
        JLabel mLbl = new JLabel("  " + group.getMembers().size() + " members  ·  " + group.getExpenses().size() + " expenses");
        mLbl.setFont(UITheme.FONT_SMALL);
        mLbl.setForeground(UITheme.TEXT_MUTED);
        metaRow.add(mLbl);

        left.add(nameRow);
        left.add(Box.createVerticalStrut(6));
        left.add(metaRow);

        // Right - totals
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel totalLbl = new JLabel(UITheme.formatAmt(group.getTotalSpent()));
        totalLbl.setFont(UITheme.FONT_TITLE);
        totalLbl.setForeground(UITheme.ACCENT);
        totalLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel totalSub = new JLabel("Total Spent");
        totalSub.setFont(UITheme.FONT_SMALL);
        totalSub.setForeground(UITheme.TEXT_MUTED);
        totalSub.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(totalSub);
        right.add(totalLbl);

        if (group.getBudget() > 0) {
            double pct = group.getTotalSpent() / group.getBudget();
            JLabel budgetLbl = new JLabel(String.format("%.0f%% of " + UITheme.CURRENCY_SYMBOL + "$1%s budget", pct*100, UITheme.formatAmt(group.getBudget())));
            budgetLbl.setFont(UITheme.FONT_SMALL);
            budgetLbl.setForeground(pct > 0.9 ? UITheme.DANGER : UITheme.ACCENT_GREEN);
            budgetLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
            right.add(Box.createVerticalStrut(4));
            right.add(budgetLbl);
            JPanel prog = UITheme.progressBar(pct, UITheme.ACCENT_GREEN, 5);
            prog.setPreferredSize(new Dimension(180, 5));
            prog.setMaximumSize(new Dimension(180, 5));
            prog.setAlignmentX(Component.RIGHT_ALIGNMENT);
            right.add(Box.createVerticalStrut(4));
            right.add(prog);
        }

        hdr.add(left, BorderLayout.WEST);
        hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    // ════════════════════ TABS ════════════════════
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BUTTON);
        tabs.setBackground(UITheme.BG_DARK);
        tabs.setForeground(UITheme.TEXT);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        UIManager.put("TabbedPane.tabInsets", new Insets(8, 16, 8, 16));
        tabs.addTab("Expenses",    buildExpensesTab());
        tabs.addTab("Balances",    buildBalancesTab());
        tabs.addTab("Analytics",   buildAnalyticsTab());
        tabs.addTab("History",     buildHistoryTab());
        tabs.addTab(group.getType().getDisplayName(), buildLifestyleTab());

        // ── Tab switch fade-in animation ──
        tabs.addChangeListener(e -> {
            Component sel = tabs.getSelectedComponent();
            if (sel instanceof JPanel p) animateFadeIn(p);
        });
        return tabs;
    }

    /** Fades the alpha of a panel from 0→1 using a Timer-driven repaint trick via composite. */
    private void animateFadeIn(JPanel panel) {
        final float[] alpha = {0f};
        javax.swing.Timer t = new javax.swing.Timer(12, null);
        t.addActionListener(ev -> {
            alpha[0] = Math.min(1f, alpha[0] + 0.10f);
            panel.putClientProperty("alpha", alpha[0]);
            panel.repaint();
            if (alpha[0] >= 1f) t.stop();
        });
        panel.putClientProperty("alpha", 0f);
        t.start();
    }

    // ════════════════════ EXPENSES TAB ════════════════════
    private JPanel buildExpensesTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        // Action bar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        
        UserRole role = group.getRole(app.getLoggedInUser());

        JButton addBtn = UITheme.pillButton("Add Expense", UITheme.ACCENT, 170, 38);
        addBtn.addActionListener(e -> {
            AddExpenseDialog dlg = new AddExpenseDialog(app, group);
            dlg.setVisible(true);
            Expense exp = dlg.getResult();
            if (exp != null) { group.addExpense(exp); refresh(); }
        });
        actions.add(addBtn);

        JButton settleBtn = UITheme.ghostButton("Settle with Person", UITheme.ACCENT_GREEN);
        settleBtn.setPreferredSize(new Dimension(200, 38));
        settleBtn.addActionListener(e -> showIndividualSettleDialog());
        actions.add(settleBtn);

        JButton addMember = UITheme.ghostButton("Add Member", UITheme.TEXT_MUTED);
        addMember.setPreferredSize(new Dimension(130, 40));
        addMember.addActionListener(e -> showAddMemberDialog());
        actions.add(addMember);

        JButton manageMembers = UITheme.ghostButton("Members", UITheme.ACCENT_PURPLE);
        manageMembers.setPreferredSize(new Dimension(130, 40));
        manageMembers.addActionListener(e -> showManageMembersDialog());
        actions.add(manageMembers);

        JButton setBudget = UITheme.ghostButton("Set Budget", UITheme.ACCENT_YELLOW);
        setBudget.setPreferredSize(new Dimension(150, 38));
        setBudget.addActionListener(e -> showBudgetSetDialog());
        actions.add(setBudget);

        panel.add(actions, BorderLayout.NORTH);

        // Expense list with staggered entrance animation
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        if (group.getExpenses().isEmpty()) {
            JPanel empty = buildEmptyState("", "No expenses yet", "Click 'Add Expense' to start tracking.");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(empty);
        } else {
            List<Expense> exps = group.getExpenses();
            for (int i = exps.size() - 1; i >= 0; i--) {
                JPanel row = buildExpenseRow(exps.get(i));
                list.add(row);
                list.add(Box.createVerticalStrut(8));
                // Staggered slide-down animation
                final int delay = (exps.size() - 1 - i) * 40;
                scheduleSlideIn(row, delay);
            }
        }

        panel.add(UITheme.styledScroll(list), BorderLayout.CENTER);
        return panel;
    }

    /** Slides a row in from slightly above after `delayMs` ms. */
    private void scheduleSlideIn(JPanel row, int delayMs) {
        row.setVisible(false);
        javax.swing.Timer start = new javax.swing.Timer(delayMs, e -> {
            row.setVisible(true);
            final int[] offsetY = {-14};
            javax.swing.Timer anim = new javax.swing.Timer(10, null);
            anim.addActionListener(ev -> {
                offsetY[0] = Math.min(0, offsetY[0] + 2);
                row.setBorder(BorderFactory.createEmptyBorder(Math.max(0, -offsetY[0]), 0, 0, 0));
                if (offsetY[0] >= 0) anim.stop();
            });
            anim.start();
        });
        start.setRepeats(false);
        start.start();
    }

    private JPanel buildExpenseRow(Expense exp) {
        UITheme.ReflectiveCardPanel row = new UITheme.ReflectiveCardPanel(12, UITheme.BG_CARD, UITheme.BORDER);
        row.setLayout(new BorderLayout(14, 0));
        row.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        // Left - category icon circle
        JPanel iconCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x1F3249));
                g2.fillOval(0, 0, 42, 42);
                g2.setFont(UITheme.FONT_BUTTON);
                String em = exp.getCategory().name().substring(0, 1);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(em, (42 - fm.stringWidth(em)) / 2, 30);
                g2.dispose();
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setPreferredSize(new Dimension(42, 42));
        iconCircle.setMinimumSize(new Dimension(42, 42));
        iconCircle.setMaximumSize(new Dimension(42, 42));

        // Center - info
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(exp.getTitle());
        titleLbl.setFont(UITheme.FONT_SUBHEAD);
        titleLbl.setForeground(UITheme.TEXT);

        JPanel subRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        subRow.setOpaque(false);
        subRow.add(UITheme.badge(exp.getCategory().getDisplayName(), UITheme.TEXT_MUTED, UITheme.BG_DARK));
        JLabel paidByLbl = new JLabel("paid by " + exp.getPaidBy().getName());
        paidByLbl.setFont(UITheme.FONT_SMALL);
        paidByLbl.setForeground(UITheme.TEXT_MUTED);
        subRow.add(paidByLbl);
        if (exp.getNote() != null && !exp.getNote().isEmpty()) {
            JLabel noteLbl = new JLabel("· " + exp.getNote());
            noteLbl.setFont(UITheme.FONT_SMALL);
            noteLbl.setForeground(UITheme.TEXT_DIM);
            subRow.add(noteLbl);
        }
        if (exp.getReceiptImagePath() != null) {
            JButton viewBill = UITheme.ghostButton("View Bill", UITheme.ACCENT);
            viewBill.setPreferredSize(new Dimension(94, 24));
            viewBill.addActionListener(e -> showReceiptPreview(exp));
            subRow.add(viewBill);
        }

        center.add(titleLbl);
        center.add(subRow);

        // Right - amount + date
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel amtLbl = new JLabel(UITheme.formatAmt(exp.getAmount()));
        amtLbl.setFont(UITheme.FONT_SUBHEAD);
        amtLbl.setForeground(UITheme.ACCENT);
        amtLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel dateLbl = new JLabel(exp.getDateTime().toLocalDate().toString());
        dateLbl.setFont(UITheme.FONT_SMALL);
        dateLbl.setForeground(UITheme.TEXT_DIM);
        dateLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(amtLbl);
        right.add(Box.createVerticalStrut(2));
        right.add(dateLbl);

        row.add(iconCircle, BorderLayout.WEST);
        row.add(center, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { row.setBgColor(UITheme.BG_CARD_HOVER); }
            public void mouseExited(MouseEvent e)  { row.setBgColor(UITheme.BG_CARD); }
        });
        UITheme.attachHoverLift(row, 2);

        return row;
    }

    // ════════════════════ BALANCES TAB ════════════════════
    private JPanel buildBalancesTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        List<Settlement> settlements = service.simplifyDebts(group);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Header row
        JPanel hdrRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        hdrRow.setOpaque(false);
        hdrRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hdrLbl = new JLabel("Simplified Settlements");
        hdrLbl.setFont(UITheme.FONT_HEADING);
        hdrLbl.setForeground(UITheme.TEXT);
        hdrRow.add(hdrLbl);
        hdrRow.add(UITheme.badge(settlements.size() + " payments needed", UITheme.ACCENT_GREEN, new Color(0x0D2218)));
        content.add(hdrRow);
        content.add(Box.createVerticalStrut(4));

        JLabel desc = new JLabel("Our algorithm reduces " + group.getMembers().size() + " members' debts to the minimum transactions.");
        desc.setFont(UITheme.FONT_SMALL);
        desc.setForeground(UITheme.TEXT_MUTED);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(desc);
        content.add(Box.createVerticalStrut(6));

        // Manual settle shortcut
        JLabel manualHint = new JLabel("You can settle individually using 'Settle with Person' in the Expenses tab.");
        manualHint.setFont(UITheme.FONT_SMALL);
        manualHint.setForeground(UITheme.TEXT_DIM);
        manualHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(manualHint);
        content.add(Box.createVerticalStrut(18));

        if (settlements.isEmpty()) {
            content.add(buildEmptyState("", "All settled up", "Everyone is even. No payments needed."));
        } else {
            for (int i = 0; i < settlements.size(); i++) {
                Settlement s = settlements.get(i);
                JPanel sRow = buildSettlementRow(s, content, settlements);
                content.add(sRow);
                content.add(Box.createVerticalStrut(10));
                // Staggered animation
                final JPanel ref = sRow;
                final int delay = i * 60;
                scheduleSlideIn(ref, delay);
            }
            content.add(Box.createVerticalStrut(8));
            JButton settleAllBtn = UITheme.pillButton("Mark All Settled", UITheme.ACCENT_GREEN, 240, 42);
            settleAllBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            settleAllBtn.addActionListener(e -> {
                settlements.forEach(Settlement::markSettled);
                settleAllBtn.setText("All Settled");
                settleAllBtn.setEnabled(false);
                // Pulse animation on button
                animatePulse(settleAllBtn, UITheme.ACCENT_GREEN);
            });
            content.add(settleAllBtn);
        }

        panel.add(UITheme.styledScroll(content), BorderLayout.CENTER);
        return panel;
    }

    /** Builds an individual settlement row with its own animated Settle button. */
    private JPanel buildSettlementRow(Settlement s, JPanel parentContent, List<Settlement> allSettlements) {
        UITheme.ReflectiveCardPanel row = new UITheme.ReflectiveCardPanel(12, UITheme.BG_CARD, UITheme.BORDER);
        row.setLayout(new BorderLayout(10, 0));
        row.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left: from → to
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(UITheme.avatar(s.getFrom(), 34));
        JLabel fromLbl = new JLabel(s.getFrom().getName());
        fromLbl.setFont(UITheme.FONT_SUBHEAD);
        fromLbl.setForeground(UITheme.TEXT);
        left.add(fromLbl);
        JLabel arrow = new JLabel("  ->  ");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 16));
        arrow.setForeground(UITheme.ACCENT_YELLOW);
        left.add(arrow);
        left.add(UITheme.avatar(s.getTo(), 34));
        JLabel toLbl = new JLabel(s.getTo().getName());
        toLbl.setFont(UITheme.FONT_SUBHEAD);
        toLbl.setForeground(UITheme.TEXT);
        left.add(toLbl);

        // Right: amount + Settle button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel amt = new JLabel(UITheme.formatAmt(s.getAmount()));
        amt.setFont(UITheme.FONT_HEADING);
        amt.setForeground(s.isSettled() ? UITheme.ACCENT_GREEN : UITheme.DANGER);
        right.add(amt);
        right.add(UITheme.badge(s.isSettled() ? "PAID" : "DEBT", s.isSettled() ? UITheme.ACCENT_GREEN : UITheme.DANGER, new Color(0x161B22)));

        if (s.isSettled()) {
            right.add(UITheme.badge(s.getPaymentMethod().getBadgeText(), UITheme.ACCENT_GREEN, new Color(0x0D2218)));
        }

        JButton settleBtn;
        if (s.isSettled()) {
            settleBtn = UITheme.pillButton("Settled", UITheme.ACCENT_GREEN, 110, 34);
            settleBtn.setEnabled(false);
            fromLbl.setForeground(UITheme.TEXT_DIM);
            toLbl.setForeground(UITheme.TEXT_DIM);
            amt.setForeground(UITheme.ACCENT_GREEN);
            row.setBgColor(new Color(0x0D2218));
        } else {
            settleBtn = UITheme.pillButton("Mark Paid", new Color(0x2D333B), 110, 34);
            settleBtn.addActionListener(e -> {
                PaymentMethod selectedMethod = showPaymentGatewayDialog(s.getFrom(), s.getTo(), s.getAmount(), this);
                if (selectedMethod == null) {
                    return;
                }
                s.markSettled(selectedMethod);
                // Animate: green flash → text update
                row.setBgColor(new Color(0x0D2218));
                fromLbl.setForeground(UITheme.TEXT_DIM);
                toLbl.setForeground(UITheme.TEXT_DIM);
                amt.setForeground(UITheme.ACCENT_GREEN);
                settleBtn.setText("Settled");
                settleBtn.setEnabled(false);
                animatePulse(row, UITheme.ACCENT_GREEN);
                showToast(s.getFrom().getName() + " paid " + UITheme.CURRENCY_SYMBOL + "$1" + UITheme.formatAmt(s.getAmount()) + " to " + s.getTo().getName());
            });
        }
        right.add(settleBtn);

        row.add(left, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        // Hover
        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!s.isSettled()) row.setBgColor(UITheme.BG_CARD_HOVER); }
            public void mouseExited(MouseEvent e)  { if (!s.isSettled()) row.setBgColor(UITheme.BG_CARD); }
        });
        UITheme.attachHoverLift(row, 2);
        return row;
    }

    /** Pulse animation: briefly flashes a panel border with a color. */
    private void animatePulse(JComponent comp, Color color) {
        final int[] step = {0};
        javax.swing.Timer t = new javax.swing.Timer(30, null);
        t.addActionListener(ev -> {
            step[0]++;
            int alpha = (int)(128 * Math.abs(Math.sin(step[0] * 0.4)));
            comp.setBorder(BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha), 2));
            comp.repaint();
            if (step[0] > 18) { comp.setBorder(null); t.stop(); }
        });
        t.start();
    }

    /** Dialog to manually record a settlement between any two members. */
    private void showIndividualSettleDialog() {
        JDialog dlg = new JDialog(app, "Settle with Person", true);
        dlg.getContentPane().setBackground(UITheme.BG_MEDIUM);
        dlg.setSize(480, 660);
        dlg.setLocationRelativeTo(app);
        dlg.setResizable(false);

        JPanel root = new JPanel();
        root.setBackground(UITheme.BG_MEDIUM);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JLabel dlgTitle = new JLabel("Record a Manual Settlement");
        dlgTitle.setFont(UITheme.FONT_HEADING);
        dlgTitle.setForeground(UITheme.TEXT);
        dlgTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html><body style='color:#8B949E'>Mark that one member has already paid another directly,<br>outside of the group's suggested settlements.</body></html>");
        desc.setFont(UITheme.FONT_SMALL);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        User[] members = group.getMembers().toArray(new User[0]);
        JComboBox<User> fromBox = UITheme.styledCombo(members);
        fromBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        fromBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<User> toBox = UITheme.styledCombo(members);
        toBox.setSelectedIndex(Math.min(1, members.length - 1));
        toBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        toBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField amtField = UITheme.styledField(UITheme.CURRENCY_SYMBOL + "$1 Amount paid", 416);
        amtField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        amtField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.installAmountValidation(amtField, 9, 2);

        JTextField noteField = UITheme.styledField("e.g. UPI, Cash, Bank Transfer", 416);
        noteField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        noteField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Live preview label
        JLabel preview = new JLabel(" ");
        preview.setFont(UITheme.FONT_BODY);
        preview.setForeground(UITheme.ACCENT);
        preview.setAlignmentX(Component.LEFT_ALIGNMENT);

        ActionListener updatePreview = ev -> {
            Object f = fromBox.getSelectedItem(), t = toBox.getSelectedItem();
            String a = amtField.getText().trim();
            if (f instanceof User fu && t instanceof User tu && !a.isEmpty())
                preview.setText(fu.getName() + " paid " + tu.getName() + " " + UITheme.CURRENCY_SYMBOL + "$1" + a);
        };
        fromBox.addActionListener(updatePreview);
        toBox.addActionListener(updatePreview);
        amtField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { updatePreview.actionPerformed(null); }
        });

        JButton recordBtn = UITheme.pillButton("Record Settlement", UITheme.ACCENT_GREEN, 416, 44);
        recordBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        recordBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton cancelBtn = UITheme.ghostButton("Cancel", UITheme.TEXT_MUTED);
        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cancelBtn.addActionListener(e -> dlg.dispose());

        root.add(dlgTitle);
        root.add(Box.createVerticalStrut(6));
        root.add(desc);
        root.add(Box.createVerticalStrut(20));
        root.add(UITheme.sectionLabel("Who paid?"));
        root.add(Box.createVerticalStrut(4));
        root.add(fromBox);
        root.add(Box.createVerticalStrut(12));
        root.add(UITheme.sectionLabel("Paid to"));
        root.add(Box.createVerticalStrut(4));
        root.add(toBox);
        root.add(Box.createVerticalStrut(12));
        root.add(UITheme.sectionLabel("Amount (" + UITheme.CURRENCY_SYMBOL + ")"));
        root.add(Box.createVerticalStrut(4));
        root.add(amtField);
        root.add(Box.createVerticalStrut(12));
        root.add(UITheme.sectionLabel("Method / Note"));
        root.add(Box.createVerticalStrut(4));
        root.add(noteField);
        root.add(Box.createVerticalStrut(12));
        root.add(preview);
        root.add(Box.createVerticalStrut(16));
        root.add(recordBtn);
        root.add(Box.createVerticalStrut(8));
        root.add(cancelBtn);

        recordBtn.addActionListener(e -> {
            try {
                User from = (User) fromBox.getSelectedItem();
                User to   = (User) toBox.getSelectedItem();
                if (from == to) { showToast("❌ Choose two different people!"); return; }
                String amountText = amtField.getText().trim();
                if (amountText.isEmpty()) {
                    UITheme.showThemedMessage(dlg, "Validation Error", "Add only numbers in Amount.", true);
                    return;
                }
                double amount = Double.parseDouble(amountText);
                Settlement manual = new Settlement(from, to, amount);
                PaymentMethod selectedMethod = showPaymentGatewayDialog(from, to, amount, dlg);
                if (selectedMethod == null) {
                    return;
                }
                manual.markSettled(selectedMethod);
                group.addSettlement(manual);
                dlg.dispose();
                showToast("Recorded: " + from.getName() + " paid " + to.getName() + " " + UITheme.CURRENCY_SYMBOL + "$1" + UITheme.formatAmt(amount));
                refresh();
            } catch (NumberFormatException ex) {
                UITheme.showThemedMessage(dlg, "Validation Error", "Add only numbers in Amount.", true);
            }
        });

        dlg.add(root);
        dlg.setVisible(true);
    }

    // ════════════════════ ANALYTICS TAB ════════════════════
    private JPanel buildAnalyticsTab() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        panel.add(new PieChartPanel(service.getCategoryTotals(group)));
        panel.add(new BarChartPanel(service.getUserSpending(group)));
        panel.add(buildInsightsPanel());
        panel.add(buildTrendPanel());
        return panel;
    }

    private JPanel buildInsightsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("📌 Monthly Summary");
        title.setFont(UITheme.FONT_SUBHEAD);
        title.setForeground(UITheme.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        wrapper.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);

        double total = group.getTotalSpent();
        double avg = group.getExpenses().isEmpty() ? 0 : total / group.getExpenses().size();
        
        User maxUser = null;
        double max = -1;
        for (Map.Entry<User, Double> e : service.getUserSpending(group).entrySet()) {
            if (e.getValue() > max) { max = e.getValue(); maxUser = e.getKey(); }
        }
        
        Category topCat = null;
        double topCatAmt = -1;
        for (Map.Entry<Category, Double> e : service.getCategoryTotals(group).entrySet()) {
            if (e.getValue() > topCatAmt) { topCatAmt = e.getValue(); topCat = e.getKey(); }
        }

        grid.add(UITheme.statCard("Avg Expense", UITheme.formatAmt(avg), UITheme.ACCENT, UITheme.BG_CARD));
        grid.add(UITheme.statCard("Top Spender", maxUser == null ? "-" : maxUser.getName(), UITheme.ACCENT_PURPLE, UITheme.BG_CARD));
        grid.add(UITheme.statCard("Top Category", topCat == null ? "-" : topCat.getDisplayName(), UITheme.ACCENT_ORANGE, UITheme.BG_CARD));
        grid.add(UITheme.statCard("Total Spend", UITheme.formatAmt(total), UITheme.ACCENT_GREEN, UITheme.BG_CARD));

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel insightLine(String k, String v) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel key = new JLabel(k);
        key.setFont(UITheme.FONT_SMALL);
        key.setForeground(UITheme.TEXT_MUTED);
        JLabel val = new JLabel(v);
        val.setFont(UITheme.FONT_BODY);
        val.setForeground(UITheme.TEXT);
        row.add(key, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return row;
    }

    private JPanel buildTrendPanel() {
        Map<String, Double> byDate = new LinkedHashMap<>();
        group.getExpenses().stream()
                .sorted(Comparator.comparing(Expense::getDateTime))
                .forEach(e -> byDate.merge(e.getDateTime().toLocalDate().toString(), e.getAmount(), Double::sum));
        return new TrendChartPanel(byDate);
    }

    static class PieChartPanel extends JPanel {
        private Map<Category, Double> data;
        private static final Color[] PIE_COLORS = {
            new Color(0x58A6FF), new Color(0x3FB950), new Color(0xF78166),
            new Color(0xE3B341), new Color(0xBC8CFF), new Color(0x39D353),
            new Color(0xFF7B72), new Color(0x79C0FF), new Color(0xD2A8FF),
            new Color(0x56D364), new Color(0xFFA657), new Color(0xFF9BBA),
            new Color(0x7EE787)
        };
        PieChartPanel(Map<Category, Double> data) { this.data = data; setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Card bg
            g2.setColor(UITheme.BG_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(UITheme.BORDER);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);

            // Title
            g2.setFont(UITheme.FONT_SUBHEAD);
            g2.setColor(UITheme.TEXT);
            g2.drawString("Spending by Category", 20, 34);

            if (data.isEmpty()) {
                g2.setFont(UITheme.FONT_BODY);
                g2.setColor(UITheme.TEXT_DIM);
                g2.drawString("No expenses yet", 20, 70);
                g2.dispose(); return;
            }

            double total = data.values().stream().mapToDouble(d -> d).sum();
            int size = (int)(Math.min(getWidth(), getHeight()) * 0.38);
            int cx = size/2 + 28, cy = getHeight()/2 + 10;
            int start = 0, i = 0;

            for (Map.Entry<Category, Double> e : data.entrySet()) {
                int arc = (int) Math.round(e.getValue()/total*360);
                if (i == data.size()-1) arc = 360 - start;
                g2.setColor(PIE_COLORS[i % PIE_COLORS.length]);
                g2.fillArc(cx-size/2, cy-size/2, size, size, start, arc);
                start += arc;

                // Legend
                int lx = size + 50, ly = 56 + i*22;
                if (ly < getHeight()-16) {
                    g2.setColor(PIE_COLORS[i % PIE_COLORS.length]);
                    g2.fillRoundRect(lx, ly, 10, 10, 4, 4);
                    g2.setColor(UITheme.TEXT_MUTED);
                    g2.setFont(UITheme.FONT_SMALL);
                    g2.drawString(UITheme.getCategoryEmoji(e.getKey()) + " " + e.getKey().getDisplayName()
                        + "  " + String.format("%.0f%%", e.getValue()/total*100), lx+16, ly+10);
                }
                i++;
            }
            // Inner circle (donut)
            int inner = (int)(size * 0.45);
            g2.setColor(UITheme.BG_CARD);
            g2.fillOval(cx-inner/2, cy-inner/2, inner, inner);
            g2.setColor(UITheme.TEXT_MUTED);
            g2.setFont(UITheme.FONT_SMALL);
            g2.drawString("Total", cx-18, cy-4);
            g2.setColor(UITheme.TEXT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString(UITheme.CURRENCY_SYMBOL + fmtI(total), cx-22, cy+14);
            g2.dispose();
        }
        private String fmtI(double v) { return String.format("%,.0f", v); }
    }

    static class BarChartPanel extends JPanel {
        private Map<User, Double> data;
        BarChartPanel(Map<User, Double> data) { this.data = data; setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(UITheme.BG_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(UITheme.BORDER);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);

            g2.setFont(UITheme.FONT_SUBHEAD);
            g2.setColor(UITheme.TEXT);
            g2.drawString("Spending by Person", 20, 34);

            if (data.isEmpty()) {
                g2.setFont(UITheme.FONT_BODY);
                g2.setColor(UITheme.TEXT_DIM);
                g2.drawString("No expenses yet", 20, 70);
                g2.dispose(); return;
            }

            double maxVal = data.values().stream().mapToDouble(d -> d).max().orElse(1);
            int padL=40, padR=30, padTop=60, padBot=60;
            int chartW = getWidth()-padL-padR;
            int chartH = getHeight()-padTop-padBot;
            int numBars = data.size();
            int barW = Math.max(20, chartW/numBars - 16);

            // Grid lines
            g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));
            g2.setColor(UITheme.BORDER);
            for (int gr = 0; gr <= 4; gr++) {
                int gy = padTop + chartH - (int)(chartH*(gr/4.0));
                g2.drawLine(padL, gy, padL+chartW, gy);
                g2.setFont(UITheme.FONT_SMALL);
                g2.setColor(UITheme.TEXT_DIM);
                g2.drawString(UITheme.CURRENCY_SYMBOL + fmtI(maxVal*gr/4), 2, gy+4);
                g2.setColor(UITheme.BORDER);
            }

            int x = padL + 8;
            for (Map.Entry<User, Double> e : data.entrySet()) {
                int barH = (int)(e.getValue()/maxVal*chartH);
                int y = padTop + chartH - barH;

                // Bar with gradient
                Graphics2D g3 = (Graphics2D) g2.create();
                g3.setPaint(new java.awt.GradientPaint(x, y, e.getKey().getProfileColor().brighter(), x, y+barH, e.getKey().getProfileColor()));
                g3.fillRoundRect(x, y, barW, barH, 6, 6);
                g3.dispose();

                // Value
                g2.setColor(UITheme.TEXT);
                g2.setFont(UITheme.FONT_SMALL);
                FontMetrics fm = g2.getFontMetrics();
                String val = UITheme.CURRENCY_SYMBOL + "$1"+fmtI(e.getValue());
                g2.drawString(val, x+(barW-fm.stringWidth(val))/2, y-6);

                // Name
                g2.setColor(UITheme.TEXT_MUTED);
                String nm = e.getKey().getName().length()>7 ? e.getKey().getName().substring(0,6)+"." : e.getKey().getName();
                g2.drawString(nm, x+(barW-fm.stringWidth(nm))/2, padTop+chartH+18);

                // Avatar dot
                e.getKey().getProfileColor();
                g2.setColor(e.getKey().getProfileColor());
                g2.fillOval(x+(barW/2)-5, padTop+chartH+24, 10, 10);

                x += barW + 16;
            }
            g2.dispose();
        }
        private String fmtI(double v) { return String.format("%,.0f", v); }
    }

    static class TrendChartPanel extends JPanel {
        private final java.util.List<Map.Entry<String, Double>> points;
        TrendChartPanel(Map<String, Double> data) {
            this.points = new ArrayList<>(data.entrySet());
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UITheme.BG_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(UITheme.BORDER);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);

            g2.setFont(UITheme.FONT_SUBHEAD);
            g2.setColor(UITheme.TEXT);
            g2.drawString("Spending Trend", 20, 34);
            if (points.isEmpty()) {
                g2.setColor(UITheme.TEXT_DIM);
                g2.setFont(UITheme.FONT_BODY);
                g2.drawString("No trend data yet", 20, 66);
                g2.dispose();
                return;
            }
            int x0 = 40, y0 = getHeight()-40, w = getWidth()-70, h = getHeight()-95;
            double max = points.stream().mapToDouble(Map.Entry::getValue).max().orElse(1);
            g2.setColor(UITheme.BORDER);
            g2.drawLine(x0, y0, x0 + w, y0);
            g2.drawLine(x0, y0 - h, x0, y0);

            int prevX = -1, prevY = -1;
            for (int i = 0; i < points.size(); i++) {
                int x = x0 + (int) ((i * 1.0 / Math.max(1, points.size()-1)) * w);
                int y = y0 - (int) ((points.get(i).getValue() / max) * h);
                g2.setColor(UITheme.ACCENT);
                g2.fillOval(x-4, y-4, 8, 8);
                if (prevX != -1) {
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(prevX, prevY, x, y);
                }
                prevX = x; prevY = y;
            }
            g2.setColor(UITheme.TEXT_MUTED);
            g2.setFont(UITheme.FONT_SMALL);
            g2.drawString("Recent spending movement", x0, getHeight()-12);
            g2.dispose();
        }
    }

    // ════════════════════ HISTORY TAB ════════════════════
    private JPanel buildHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        JButton exportBtn = UITheme.pillButton("Export CSV", UITheme.ACCENT, 160, 36);
        exportBtn.addActionListener(e -> {
            String fn = group.getName().replaceAll("\\s+","_") + "_expenses.csv";
            FileManager.exportGroupToCSV(group, fn);
            showToast("Exported to " + fn);
        });
        actions.add(exportBtn);

        JButton reportBtn = UITheme.ghostButton("View Report", UITheme.TEXT_MUTED);
        reportBtn.setPreferredSize(new Dimension(148, 36));
        reportBtn.addActionListener(e -> {
            JTextArea ta = new JTextArea(FileManager.generateReport(group, service), 20, 50);
            ta.setFont(UITheme.FONT_MONO);
            ta.setForeground(UITheme.TEXT);
            ta.setBackground(UITheme.BG_INPUT);
            ta.setEditable(false);
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(600, 420));
            JOptionPane.showMessageDialog(app, sp, group.getName() + " — Report", JOptionPane.PLAIN_MESSAGE);
        });
        actions.add(reportBtn);
        panel.add(actions, BorderLayout.NORTH);

        // Table
        String[] cols = {"", "Type", "Title", "Amount", "Category/Method", "Paid By", "Date", "Note"};
        List<Expense> exps = group.getExpenses();
        List<Settlement> manual = group.getManualSettlements();
        Object[][] rows = new Object[exps.size() + manual.size()][8];
        List<Object> rowRefs = new ArrayList<>();
        int rowIndex = 0;
        for (int i = 0; i < exps.size(); i++) {
            Expense ex = exps.get(exps.size()-1-i);
            rows[rowIndex++] = new Object[]{
                ex.getCategory().name().substring(0, 1),
                "Expense",
                ex.getTitle(), UITheme.CURRENCY_SYMBOL + "$1"+UITheme.formatAmt(ex.getAmount()),
                ex.getCategory().getDisplayName(),
                ex.getPaidBy().getName(),
                ex.getDateTime().toLocalDate().toString(),
                (ex.getNote() != null ? ex.getNote() : "") + (ex.getReceiptImagePath() != null ? "  Receipt attached" : "")
            };
            rowRefs.add(ex);
        }
        for (int i = manual.size() - 1; i >= 0; i--) {
            Settlement s = manual.get(i);
            rows[rowIndex++] = new Object[]{
                    "S",
                    "Settlement",
                    s.getFrom().getName() + " -> " + s.getTo().getName(),
                    UITheme.formatAmt(s.getAmount()),
                    s.getPaymentMethod().getBadgeText(),
                    s.getFrom().getName(),
                    s.getDateTime().toLocalDate().toString(),
                    s.isSettled() ? "Settled" : "Pending"
            };
            rowRefs.add(s);
        }

        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(UITheme.FONT_BODY);
        table.setForeground(UITheme.TEXT);
        table.setBackground(UITheme.BG_CARD);
        table.setGridColor(UITheme.BORDER);
        table.setRowHeight(36);
        table.setSelectionBackground(UITheme.BG_CARD_HOVER);
        table.setSelectionForeground(UITheme.TEXT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 4));
        table.getTableHeader().setFont(UITheme.FONT_BUTTON);
        table.getTableHeader().setBackground(UITheme.BG_MEDIUM);
        table.getTableHeader().setForeground(UITheme.TEXT_MUTED);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < rowRefs.size()) {
                        Object ref = rowRefs.get(row);
                        if (ref instanceof Expense exp && exp.getReceiptImagePath() != null) {
                            showReceiptPreview(exp);
                        }
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        sp.setBackground(UITheme.BG_DARK);
        sp.getViewport().setBackground(UITheme.BG_CARD);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════ LIFESTYLE TAB ════════════════════
    private JPanel buildLifestyleTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Header
        JLabel titleLbl = new JLabel(group.getType().getDisplayName() + " Quick Tools");
        titleLbl.setFont(UITheme.FONT_HEADING);
        titleLbl.setForeground(UITheme.TEXT);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel(group.getType().getDescription());
        subLbl.setFont(UITheme.FONT_BODY);
        subLbl.setForeground(UITheme.TEXT_MUTED);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(titleLbl);
        content.add(Box.createVerticalStrut(4));
        content.add(subLbl);
        content.add(Box.createVerticalStrut(20));

        // Quick action buttons grid
        Color[] btnColors = {UITheme.ACCENT, UITheme.ACCENT_GREEN, UITheme.ACCENT_YELLOW, UITheme.ACCENT_PURPLE, UITheme.ACCENT_ORANGE};
        JPanel actGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        actGrid.setOpaque(false);
        actGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[] actions = group.getType().getQuickActions();
        for (int i = 0; i < actions.length; i++) {
            String act = actions[i];
            JButton b = UITheme.pillButton(act, btnColors[i % btnColors.length], 190, 42);
            b.addActionListener(e -> handleQuickAction(act));
            actGrid.add(b);
        }
        content.add(actGrid);
        content.add(Box.createVerticalStrut(24));
        content.add(UITheme.divider());
        content.add(Box.createVerticalStrut(20));

        // Info cards
        addInsightCards(content);

        panel.add(UITheme.styledScroll(content), BorderLayout.CENTER);
        return panel;
    }

    private void addInsightCards(JPanel parent) {
        switch (group.getType()) {
            case ROOMMATES:
                addInfoCard(parent, "Rent Breakdown", getRentSummary(), UITheme.ACCENT);
                addInfoCard(parent, "Monthly Breakdown", getMonthlyBreakdown(), UITheme.ACCENT_GREEN);
                addInfoCard(parent, "Pro Tip", "Set a monthly group budget to track and control spending. Use Split Rent for instant equal rent distribution.", UITheme.ACCENT_YELLOW);
                break;
            case OFFICE:
                addInfoCard(parent, "Lunch Pool", "Track daily team lunches and split evenly so payments stay transparent.", UITheme.ACCENT);
                addInfoCard(parent, "Gift Fund", getPartySummary(), UITheme.ACCENT_PURPLE);
                addInfoCard(parent, "Pro Tip", "Use Gift Collection to pool money for birthdays with per-person contributions.", UITheme.ACCENT_YELLOW);
                break;
            case TRAVEL:
                addInfoCard(parent, "Trip Summary", getTripSummary(), UITheme.ACCENT);
                addInfoCard(parent, "Per Person Cost", getPerPersonCost(), UITheme.ACCENT_GREEN);
                addInfoCard(parent, "Pro Tip", "Use Analytics to track hotel vs food vs transport spend and adjust quickly.", UITheme.ACCENT_YELLOW);
                break;
            case COUPLE:
                addInfoCard(parent, "Balance Summary", getCoupleBalance(), UITheme.ACCENT);
                addInfoCard(parent, "Pro Tip", "Use Split 50/50 for regular costs and Custom Ratio for income-based sharing.", UITheme.ACCENT_YELLOW);
                break;
            case EVENT:
                addInfoCard(parent, "Event Budget", getEventBudget(), UITheme.ACCENT);
                addInfoCard(parent, "Per Head Cost", getPerPersonCost(), UITheme.ACCENT_GREEN);
                break;
            case STUDENTS:
                addInfoCard(parent, "Group Summary", getTripSummary(), UITheme.ACCENT);
                addInfoCard(parent, "Pro Tip", "Use Snack Fund for shared snacks and Project Expense for materials.", UITheme.ACCENT_YELLOW);
                break;
            default:
                addInfoCard(parent, "Group Overview", getTripSummary(), UITheme.ACCENT);
        }
    }

    private void addInfoCard(JPanel parent, String title, String body, Color accent) {
        UITheme.CardPanel card = new UITheme.CardPanel(12, UITheme.BG_CARD, UITheme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = new JLabel(title);
        t.setFont(UITheme.FONT_BUTTON);
        t.setForeground(UITheme.TEXT);

        JLabel b = new JLabel("<html><body style='width:500px;color:#8B949E'>" + body + "</body></html>");
        b.setFont(UITheme.FONT_BODY);
        b.setForeground(UITheme.TEXT_MUTED);

        card.add(t);
        card.add(Box.createVerticalStrut(4));
        card.add(b);
        parent.add(card);
        parent.add(Box.createVerticalStrut(10));
    }

    // ════════════════════ QUICK ACTIONS ════════════════════
    private void handleQuickAction(String action) {
        switch (action) {
            case "Gift Collection": showGiftDialog(); break;
            case "Monthly Summary": case "Trip Summary": showSummaryDialog(); break;
            case "Track Budget": case "Per-Head Cost": showBudgetSetDialog(); break;
            default: showQuickSplitDialog(action); break;
        }
    }

    private void showQuickSplitDialog(String actionTitle) {
        JDialog dlg = new JDialog(app, actionTitle, true);
        dlg.getContentPane().setBackground(UITheme.BG_MEDIUM);
        dlg.setSize(520, 440);
        dlg.setLocationRelativeTo(app);
        dlg.setResizable(false);

        JPanel root = new JPanel();
        root.setBackground(UITheme.BG_MEDIUM);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel(actionTitle);
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField amtField = UITheme.styledField(UITheme.CURRENCY_SYMBOL + "$1 Amount", 360);
        amtField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        amtField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.installAmountValidation(amtField, 9, 2);

        JComboBox<User> paidBy = UITheme.styledCombo(group.getMembers().toArray(new User[0]));
        paidBy.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        paidBy.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField noteField = UITheme.styledField("Optional note", 360);
        noteField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        noteField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addBtn = UITheme.pillButton("Add & Split Equally", UITheme.ACCENT, 360, 42);
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        root.add(title); root.add(Box.createVerticalStrut(20));
        root.add(UITheme.sectionLabel("Amount")); root.add(Box.createVerticalStrut(4));
        root.add(amtField); root.add(Box.createVerticalStrut(12));
        root.add(UITheme.sectionLabel("Paid By")); root.add(Box.createVerticalStrut(4));
        root.add(paidBy); root.add(Box.createVerticalStrut(12));
        root.add(UITheme.sectionLabel("Note")); root.add(Box.createVerticalStrut(4));
        root.add(noteField); root.add(Box.createVerticalStrut(18));
        root.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(amtField.getText().trim().replace(UITheme.CURRENCY_SYMBOL + "$1","").trim());
                double each = amt / group.getMembers().size();
                Map<User, Double> split = new LinkedHashMap<>();
                for (User u : group.getMembers()) split.put(u, each);
                Category cat = guessCat(actionTitle);
                group.addExpense(new Expense(actionTitle, amt, cat, (User)paidBy.getSelectedItem(), split, noteField.getText().trim()));
                dlg.dispose();
                refresh();
            } catch (NumberFormatException ex) {
                UITheme.showThemedMessage(dlg, "Validation Error", "Add only numbers in Amount.", true);
            }
        });

        dlg.add(root);
        dlg.setVisible(true);
    }

    private void showGiftDialog() {
        JDialog dlg = new JDialog(app, "Gift Collection", true);
        dlg.getContentPane().setBackground(UITheme.BG_MEDIUM);
        dlg.setSize(520, 440);
        dlg.setLocationRelativeTo(app);
        dlg.setResizable(false);

        JPanel root = new JPanel();
        root.setBackground(UITheme.BG_MEDIUM);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel dlgTitle = new JLabel("Gift Collection");
        dlgTitle.setFont(UITheme.FONT_HEADING);
        dlgTitle.setForeground(UITheme.TEXT);
        dlgTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField titleField = UITheme.styledField("Gift for... (e.g. Rahul's Birthday)", 360);
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField ppField = UITheme.styledField("Per person contribution (" + UITheme.CURRENCY_SYMBOL + ")", 360);
        ppField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        ppField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.installAmountValidation(ppField, 9, 2);

        JButton collectBtn = UITheme.pillButton("Collect from All", UITheme.ACCENT_PURPLE, 360, 42);
        collectBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        collectBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        root.add(dlgTitle); root.add(Box.createVerticalStrut(20));
        root.add(UITheme.sectionLabel("What's it for?")); root.add(Box.createVerticalStrut(4));
        root.add(titleField); root.add(Box.createVerticalStrut(12));
        root.add(UITheme.sectionLabel("Per Person (" + UITheme.CURRENCY_SYMBOL + ")")); root.add(Box.createVerticalStrut(4));
        root.add(ppField); root.add(Box.createVerticalStrut(20));
        root.add(collectBtn);

        collectBtn.addActionListener(e -> {
            try {
                double pp = Double.parseDouble(ppField.getText().trim());
                double total = pp * group.getMembers().size();
                Map<User, Double> split = new LinkedHashMap<>();
                for (User u : group.getMembers()) split.put(u, pp);
                group.addExpense(new Expense(titleField.getText().trim().isEmpty() ? "Gift" : titleField.getText(),
                    total, Category.GIFT, group.getMembers().get(0), split, UITheme.CURRENCY_SYMBOL + pp + " per person"));
                dlg.dispose();
                refresh();
            } catch (NumberFormatException ex) {
                UITheme.showThemedMessage(dlg, "Validation Error", "Add only numbers in Amount.", true);
            }
        });

        dlg.add(root);
        dlg.setVisible(true);
    }

    private void showSummaryDialog() {
        JDialog dlg = new JDialog(app, group.getName() + " Summary", true);
        dlg.getContentPane().setBackground(UITheme.BG_MEDIUM);
        dlg.setSize(700, 520);
        dlg.setLocationRelativeTo(app);

        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        root.add(summaryCard("Total Spent", UITheme.formatAmt(group.getTotalSpent()), UITheme.ACCENT));
        root.add(Box.createVerticalStrut(10));
        root.add(summaryCard("Budget", group.getBudget() > 0 ? UITheme.formatAmt(group.getBudget()) : "Not set", UITheme.ACCENT_YELLOW));
        root.add(Box.createVerticalStrut(10));
        double remaining = group.getBudget() > 0 ? group.getBudget() - group.getTotalSpent() : 0;
        root.add(summaryCard("Remaining", group.getBudget() > 0 ? UITheme.formatAmt(remaining) : "-", remaining >= 0 ? UITheme.ACCENT_GREEN : UITheme.DANGER));
        root.add(Box.createVerticalStrut(10));
        root.add(summaryCard("Transactions", String.valueOf(group.getExpenses().size()), UITheme.TEXT));
        root.add(Box.createVerticalGlue());

        JTextArea ta = new JTextArea(FileManager.generateReport(group, service), 10, 30);
        ta.setFont(UITheme.FONT_MONO);
        ta.setForeground(UITheme.TEXT_MUTED);
        ta.setBackground(UITheme.BG_INPUT);
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(640, 220));
        root.add(sp);

        dlg.add(root);
        dlg.setVisible(true);
    }

    private JPanel summaryCard(String title, String value, Color color) {
        UITheme.ReflectiveCardPanel card = new UITheme.ReflectiveCardPanel(12, UITheme.BG_CARD, UITheme.BORDER);
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel l = new JLabel(title);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_MUTED);
        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_SUBHEAD);
        v.setForeground(color);
        card.add(l, BorderLayout.WEST);
        card.add(v, BorderLayout.EAST);
        return card;
    }

    private void showBudgetSetDialog() {
        JTextField f = UITheme.styledField("e.g. 20000", 260);
        UITheme.installAmountValidation(f, 9, 2);
        int r = JOptionPane.showConfirmDialog(app, f, "Set/Update Budget (" + UITheme.CURRENCY_SYMBOL + ")", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            try {
                if (f.getText().trim().isEmpty()) {
                    UITheme.showThemedMessage(app, "Validation Error", "Add only numbers in Amount.", true);
                    return;
                }
                group.setBudget(Double.parseDouble(f.getText().trim()));
                refresh();
            } catch (Exception ignored) {
                UITheme.showThemedMessage(app, "Validation Error", "Add only numbers in Amount.", true);
            }
        }
    }

    private void showAddMemberDialog() {
        JDialog dlg = new JDialog(app, "Add Member", true);
        dlg.getContentPane().setBackground(UITheme.BG_MEDIUM);
        dlg.setSize(430, 480);
        dlg.setLocationRelativeTo(app);
        dlg.setResizable(false);

        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Add New Group Member");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = UITheme.styledField("Member name", 370);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField photoField = UITheme.styledField("Optional photo path", 370);
        photoField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        photoField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton browse = UITheme.ghostButton("Choose Photo", UITheme.ACCENT);
        browse.setAlignmentX(Component.LEFT_ALIGNMENT);
        browse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "webp"));
            if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                photoField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });



        JButton add = UITheme.pillButton("Add Member", UITheme.ACCENT_GREEN, 150, 38);
        add.setAlignmentX(Component.LEFT_ALIGNMENT);
        add.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                UITheme.showThemedMessage(dlg, "Validation Error", "Member name is required.", true);
                return;
            }
            User u = new User(app.getNextUserId(), name);
            if (!photoField.getText().trim().isEmpty()) {
                u.setProfileImagePath(photoField.getText().trim());
            }
            group.addMember(u);
            group.setRole(u, UserRole.EDITOR);
            dlg.dispose();
            refresh();
        });

        root.add(title);
        root.add(Box.createVerticalStrut(14));
        root.add(UITheme.sectionLabel("Name"));
        root.add(Box.createVerticalStrut(4));
        root.add(nameField);
        root.add(Box.createVerticalStrut(10));

        root.add(UITheme.sectionLabel("Profile Picture (Optional)"));
        root.add(Box.createVerticalStrut(4));
        root.add(photoField);
        root.add(Box.createVerticalStrut(8));
        root.add(browse);
        root.add(Box.createVerticalStrut(16));
        root.add(add);
        root.add(Box.createVerticalStrut(8));
        dlg.add(root);
        dlg.setVisible(true);
    }

    private void showManageMembersDialog() {
        JDialog dlg = new JDialog(app, "Manage Members", true);
        dlg.getContentPane().setBackground(UITheme.BG_MEDIUM);
        dlg.setSize(560, 420);
        dlg.setLocationRelativeTo(app);
        dlg.setResizable(false);

        DefaultListModel<User> model = new DefaultListModel<>();
        for (User u : group.getMembers()) model.addElement(u);
        JList<User> list = new JList<>(model);
        list.setFont(UITheme.FONT_BODY);
        list.setBackground(UITheme.BG_INPUT);
        list.setForeground(UITheme.TEXT);
        JScrollPane sp = new JScrollPane(list);

        JButton edit = UITheme.pillButton("Edit", UITheme.ACCENT, 90, 34);
        JButton del = UITheme.ghostButton("Delete", UITheme.DANGER);
        del.setPreferredSize(new Dimension(90, 34));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        btns.add(edit);
        btns.add(del);

        edit.addActionListener(e -> {
            User selected = list.getSelectedValue();
            if (selected == null) return;
            JTextField nameField = UITheme.styledField("Name", 300);
            nameField.setText(selected.getName());
            JTextField photoField = UITheme.styledField("Photo path", 300);
            photoField.setText(selected.getProfileImagePath() == null ? "" : selected.getProfileImagePath());
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(nameField); p.add(Box.createVerticalStrut(8)); p.add(photoField);
            int ok = JOptionPane.showConfirmDialog(dlg, p, "Edit member", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok == JOptionPane.OK_OPTION) {
                selected.setName(nameField.getText().trim());
                selected.setProfileImagePath(photoField.getText().trim().isEmpty() ? null : photoField.getText().trim());
                list.repaint();
                refresh();
            }
        });

        del.addActionListener(e -> {
            User selected = list.getSelectedValue();
            if (selected == null || selected.equals(app.getLoggedInUser())) return;
            group.getMembers().remove(selected);
            model.removeElement(selected);
            refresh();
        });

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        root.add(sp, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        dlg.add(root);
        dlg.setVisible(true);
    }

    private PaymentMethod showPaymentGatewayDialog(User from, User to, double amount, Component parent) {
        JDialog dlg = new JDialog(app, "Payment Gateway", true);
        dlg.getContentPane().setBackground(UITheme.BG_MEDIUM);
        dlg.setSize(500, 420);
        dlg.setLocationRelativeTo(app);
        dlg.setResizable(false);

        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JLabel title = new JLabel("Complete Settlement");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Pay " + UITheme.CURRENCY_SYMBOL + "$1" + UITheme.formatAmt(amount) + " from " + from.getName() + " to " + to.getName());
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<PaymentMethod> methodBox = UITheme.styledCombo(PaymentMethod.values());
        methodBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        methodBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        methodBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PaymentMethod m) {
                    setText(m.getBadgeText());
                }
                setBackground(isSelected ? UITheme.BG_CARD_HOVER : UITheme.BG_INPUT);
                setForeground(UITheme.TEXT);
                return this;
            }
        });

        JTextField upiField = UITheme.styledField("Receiver UPI ID (example: name@bank)", 420);
        upiField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        upiField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField cardField = UITheme.styledField("Card Number", 420);
        cardField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        cardField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel dynamic = new JPanel();
        dynamic.setOpaque(false);
        dynamic.setLayout(new BoxLayout(dynamic, BoxLayout.Y_AXIS));
        dynamic.setAlignmentX(Component.LEFT_ALIGNMENT);

        Runnable refreshDynamic = () -> {
            dynamic.removeAll();
            PaymentMethod selected = (PaymentMethod) methodBox.getSelectedItem();
            if (selected == PaymentMethod.UPI) {
                dynamic.add(UITheme.sectionLabel("UPI ID"));
                dynamic.add(Box.createVerticalStrut(4));
                dynamic.add(upiField);
            } else if (selected == PaymentMethod.CREDIT_CARD || selected == PaymentMethod.DEBIT_CARD) {
                dynamic.add(UITheme.sectionLabel("Card Number"));
                dynamic.add(Box.createVerticalStrut(4));
                dynamic.add(cardField);
            } else if (selected == PaymentMethod.NET_BANKING) {
                JLabel note = new JLabel("You will be redirected to your configured hosted checkout.");
                note.setFont(UITheme.FONT_SMALL);
                note.setForeground(UITheme.TEXT_MUTED);
                note.setAlignmentX(Component.LEFT_ALIGNMENT);
                dynamic.add(note);
            } else {
                JLabel note = new JLabel("Cash payment will be recorded directly.");
                note.setFont(UITheme.FONT_SMALL);
                note.setForeground(UITheme.TEXT_MUTED);
                note.setAlignmentX(Component.LEFT_ALIGNMENT);
                dynamic.add(note);
            }
            dynamic.revalidate();
            dynamic.repaint();
        };
        methodBox.addActionListener(e -> refreshDynamic.run());
        refreshDynamic.run();

        JButton payBtn = UITheme.pillButton("Proceed Payment", UITheme.ACCENT_GREEN, 220, 40);
        payBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        final PaymentMethod[] chosen = {null};
        payBtn.addActionListener(e -> {
            PaymentMethod method = (PaymentMethod) methodBox.getSelectedItem();
            PaymentGatewayService.PaymentResult result = paymentGatewayService.initiatePayment(
                    method, amount, from.getName(), to.getName(), upiField.getText().trim(), cardField.getText().trim());
            if (!result.isSuccess()) {
                UITheme.showThemedMessage(dlg, "Payment Error", result.getMessage(), true);
                return;
            }
            chosen[0] = method;
            dlg.dispose();
            showToast(result.getMessage());
        });

        root.add(title);
        root.add(Box.createVerticalStrut(6));
        root.add(sub);
        root.add(Box.createVerticalStrut(18));
        root.add(UITheme.sectionLabel("Payment Method"));
        root.add(Box.createVerticalStrut(4));
        root.add(methodBox);
        root.add(Box.createVerticalStrut(14));
        root.add(dynamic);
        root.add(Box.createVerticalStrut(16));
        root.add(payBtn);
        dlg.add(root);
        dlg.setVisible(true);
        return chosen[0];
    }

    // ════════════════════ TOAST ════════════════════
    private void showToast(String msg) {
        JDialog toast = new JDialog(app, false);
        toast.setUndecorated(true);
        toast.setBackground(new Color(0,0,0,0));
        JLabel l = new JLabel("  " + msg + "  ");
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT);
        l.setOpaque(true);
        l.setBackground(UITheme.BG_CARD_HOVER);
        l.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        toast.add(l);
        toast.pack();
        toast.setLocationRelativeTo(app);
        toast.setLocation(toast.getX(), toast.getY() + 240);
        toast.setVisible(true);
        javax.swing.Timer t = new javax.swing.Timer(2200, e -> toast.dispose());

        t.setRepeats(false); t.start();
    }

    private void showReceiptPreview(Expense exp) {
        if (exp.getReceiptImagePath() == null || exp.getReceiptImagePath().isBlank()) return;
        JDialog dlg = new JDialog(app, "Attached Bill", true);
        dlg.getContentPane().setBackground(UITheme.BG_DARK);
        dlg.setSize(760, 560);
        dlg.setLocationRelativeTo(app);
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.File(exp.getReceiptImagePath()));
            if (img == null) throw new Exception("Invalid image");
            int w = img.getWidth(), h = img.getHeight();
            double s = Math.min(720.0/w, 500.0/h);
            Image scaled = img.getScaledInstance((int)(w*s), (int)(h*s), Image.SCALE_SMOOTH);
            JLabel pic = new JLabel(new ImageIcon(scaled));
            pic.setHorizontalAlignment(SwingConstants.CENTER);
            dlg.add(new JScrollPane(pic));
            dlg.setVisible(true);
        } catch (Exception ex) {
            UITheme.showThemedMessage(app, "Receipt Preview", "Unable to open image: " + exp.getReceiptImagePath(), true);
            dlg.dispose();
        }
    }

    // ════════════════════ HELPERS ════════════════════
    private void refresh() { app.persistCurrentUser(); app.showGroup(group); app.showHome(); }


    private Category guessCat(String a) {
        String l = a.toLowerCase();
        if (l.contains("rent")) return Category.RENT;
        if (l.contains("utility")||l.contains("bill")||l.contains("electric")) return Category.UTILITIES;
        if (l.contains("grocery")||l.contains("snack")) return Category.GROCERIES;
        if (l.contains("lunch")||l.contains("food")||l.contains("dinner")||l.contains("biryani")) return Category.FOOD;
        if (l.contains("party")||l.contains("outing")||l.contains("event")) return Category.PARTY;
        if (l.contains("hotel")||l.contains("travel")||l.contains("trip")) return Category.TRAVEL;
        if (l.contains("transport")||l.contains("taxi")||l.contains("cab")) return Category.TRANSPORT;
        if (l.contains("gift")||l.contains("birthday")||l.contains("farewell")) return Category.GIFT;
        if (l.contains("sub")) return Category.SUBSCRIPTION;
        if (l.contains("project")||l.contains("supplies")) return Category.SHOPPING;
        return Category.OTHER;
    }

    private String getRentSummary() {
        double rent = group.getExpenses().stream()
            .filter(e -> e.getCategory()==Category.RENT).mapToDouble(Expense::getAmount).sum();
        return String.format("Total rent paid: " + UITheme.CURRENCY_SYMBOL + "$1%s across %d members (" + UITheme.CURRENCY_SYMBOL + "$1%s/person)",
            UITheme.formatAmt(rent), group.getMembers().size(), UITheme.formatAmt(rent/Math.max(1,group.getMembers().size())));
    }

    private String getMonthlyBreakdown() {
        Map<Category, Double> totals = service.getCategoryTotals(group);
        StringBuilder sb = new StringBuilder();
        totals.forEach((cat, amt) ->
            sb.append(UITheme.getCategoryEmoji(cat)).append(" ").append(cat.getDisplayName())
              .append(": " + UITheme.CURRENCY_SYMBOL + "$1").append(UITheme.formatAmt(amt)).append("  |  "));
        return sb.length() > 3 ? sb.substring(0, sb.length()-3) : "No expenses yet";
    }

    private String getPartySummary() {
        double total = group.getExpenses().stream()
            .filter(e -> e.getCategory()==Category.PARTY||e.getCategory()==Category.GIFT||e.getCategory()==Category.ENTERTAINMENT)
            .mapToDouble(Expense::getAmount).sum();
        return String.format("Total events/gifts spend: " + UITheme.CURRENCY_SYMBOL + "$1%s", UITheme.formatAmt(total));
    }

    private String getTripSummary() {
        return String.format("Total: " + UITheme.CURRENCY_SYMBOL + "$1%s  |  %d expenses logged  |  %d members", UITheme.formatAmt(group.getTotalSpent()), group.getExpenses().size(), group.getMembers().size());
    }

    private String getPerPersonCost() {
        return String.format("Average cost per person: " + UITheme.CURRENCY_SYMBOL + "$1%s", UITheme.formatAmt(group.getTotalSpent()/Math.max(1,group.getMembers().size())));
    }

    private String getCoupleBalance() {
        Map<User, Double> sp = service.getUserSpending(group);
        StringBuilder sb = new StringBuilder();
        sp.forEach((u, amt) -> sb.append(u.getName()).append(": " + UITheme.CURRENCY_SYMBOL + "$1").append(UITheme.formatAmt(amt)).append("  |  "));
        return sb.length() > 3 ? sb.substring(0, sb.length()-3) : "No expenses yet";
    }

    private String getEventBudget() {
        if (group.getBudget() > 0) {
            double rem = group.getBudget() - group.getTotalSpent();
            return String.format("Budget: " + UITheme.CURRENCY_SYMBOL + "$1%s  |  Spent: " + UITheme.CURRENCY_SYMBOL + "$1%s  |  Remaining: " + UITheme.CURRENCY_SYMBOL + "$1%s",
                UITheme.formatAmt(group.getBudget()), UITheme.formatAmt(group.getTotalSpent()), UITheme.formatAmt(rem));
        }
        return "Click 'Track Budget' above to set a budget for this event.";
    }

    private JPanel buildEmptyState(String emoji, String title, String sub) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        JLabel el = new JLabel(emoji, SwingConstants.CENTER);
        el.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        el.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel tl = new JLabel(title);
        tl.setFont(UITheme.FONT_SUBHEAD);
        tl.setForeground(UITheme.TEXT);
        tl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sl = new JLabel(sub);
        sl.setFont(UITheme.FONT_BODY);
        sl.setForeground(UITheme.TEXT_MUTED);
        sl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(el); p.add(Box.createVerticalStrut(10)); p.add(tl); p.add(Box.createVerticalStrut(4)); p.add(sl);
        return p;
    }
}
