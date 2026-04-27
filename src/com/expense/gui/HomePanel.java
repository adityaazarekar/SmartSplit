package com.expense.gui;

import com.expense.model.*;
import com.expense.service.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class HomePanel extends JPanel {

    public HomePanel(SmartSplitApp app, List<Group> groups, ExpenseService service) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);

        add(buildTopBar(app), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(28, 32, 32, 32));

        // Stat cards
        content.add(buildStatRow(app, groups, service));
        content.add(Box.createVerticalStrut(28));

        // Second row: Recent activity + Quick insights
        JPanel midRow = new JPanel(new BorderLayout(16, 0));
        midRow.setOpaque(false);
        midRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        midRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        midRow.add(buildRecentActivityPanel(groups, app, service), BorderLayout.CENTER);
        midRow.add(buildInsightsPanel(groups, service, app), BorderLayout.EAST);
        content.add(midRow);
        content.add(Box.createVerticalStrut(28));

        // Groups title
        JPanel groupsHeader = new JPanel(new BorderLayout());
        groupsHeader.setOpaque(false);
        groupsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel groupsTitle = new JLabel("Your Groups");
        groupsTitle.setFont(UITheme.FONT_HEADING);
        groupsTitle.setForeground(UITheme.TEXT);
        JLabel groupsSub = new JLabel("Click a group to view expenses, balances, and analytics");
        groupsSub.setFont(UITheme.FONT_SMALL);
        groupsSub.setForeground(UITheme.TEXT_MUTED);
        JPanel groupsTitleCol = new JPanel();
        groupsTitleCol.setOpaque(false);
        groupsTitleCol.setLayout(new BoxLayout(groupsTitleCol, BoxLayout.Y_AXIS));
        groupsTitleCol.add(groupsTitle);
        groupsTitleCol.add(Box.createVerticalStrut(2));
        groupsTitleCol.add(groupsSub);
        groupsHeader.add(groupsTitleCol, BorderLayout.WEST);

        JButton newGrpBtn = UITheme.pillButton("+ New Group", UITheme.ACCENT, 140, 36);
        newGrpBtn.addActionListener(e -> app.showCreateGroupDialog());
        JPanel newGrpWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        newGrpWrap.setOpaque(false);
        newGrpWrap.add(newGrpBtn);
        groupsHeader.add(newGrpWrap, BorderLayout.EAST);

        content.add(groupsHeader);
        content.add(Box.createVerticalStrut(14));

        if (groups.isEmpty()) {
            content.add(buildEmptyGroupsState(app));
        } else {
            JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
            grid.setOpaque(false);
            grid.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (Group g : groups) grid.add(buildGroupCard(g, app, service));
            content.add(grid);
        }

        add(UITheme.styledScroll(content), BorderLayout.CENTER);
    }

    // ─── Top bar ─────────────────────────────────────────────────────────────
    private JPanel buildTopBar(SmartSplitApp app) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_MEDIUM);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(16, 32, 16, 32)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("Dashboard");
        title.setFont(UITheme.FONT_SUBHEAD);
        title.setForeground(UITheme.TEXT);
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton profileBtn = UITheme.ghostButton("Edit Profile", UITheme.ACCENT);
        profileBtn.setPreferredSize(new Dimension(130, 40));
        profileBtn.addActionListener(e -> app.showProfileDialog());
        JLabel userLbl = new JLabel(app.getLoggedInUser().getName());
        userLbl.setFont(UITheme.FONT_BODY);
        userLbl.setForeground(UITheme.TEXT_MUTED);
        JPanel av = UITheme.avatar(app.getLoggedInUser(), 34);
        right.add(profileBtn);
        right.add(userLbl);
        right.add(av);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ─── Stat cards ───────────────────────────────────────────────────────────
    private JPanel buildStatRow(SmartSplitApp app, List<Group> groups, ExpenseService service) {
        double owes = 0, owed = 0, spent = 0;
        int totalExpenses = 0;
        for (Group g : groups) {
            owes  += service.getUserOwes(g, app.getLoggedInUser());
            owed  += service.getUserIsOwed(g, app.getLoggedInUser());
            spent += g.getTotalSpent();
            totalExpenses += g.getExpenses().size();
        }

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(buildStatCard("You Owe",      UITheme.formatAmt(owes),              UITheme.DANGER,        "Total debt across all groups"));
        row.add(buildStatCard("You're Owed",  UITheme.formatAmt(owed),              UITheme.ACCENT_GREEN,  "Total owed to you"));
        row.add(buildStatCard("Total Spent",  UITheme.formatAmt(spent),             UITheme.ACCENT,        "Across all groups"));
        row.add(buildStatCard("Groups",       groups.size() + " active",            UITheme.ACCENT_PURPLE, totalExpenses + " expenses total"));

        return row;
    }

    private JPanel buildStatCard(String label, String value, Color accent, String subtext) {
        UITheme.ReflectiveCardPanel card = new UITheme.ReflectiveCardPanel(14, UITheme.BG_CARD, UITheme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.setPreferredSize(new Dimension(200, 110));

        // Colored top accent bar
        JPanel accentBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        accentBar.setOpaque(false);
        accentBar.setPreferredSize(new Dimension(36, 3));
        accentBar.setMaximumSize(new Dimension(36, 3));
        accentBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(accent);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel(subtext);
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_DIM);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(accentBar);
        card.add(Box.createVerticalStrut(8));
        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(val);
        card.add(Box.createVerticalStrut(2));
        card.add(sub);

        return card;
    }

    // ─── Recent Activity ──────────────────────────────────────────────────────
    private JPanel buildRecentActivityPanel(List<Group> groups, SmartSplitApp app, ExpenseService service) {
        UITheme.ReflectiveCardPanel card = new UITheme.ReflectiveCardPanel(14, UITheme.BG_CARD, UITheme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Recent Activity");
        title.setFont(UITheme.FONT_BUTTON);
        title.setForeground(UITheme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(12));

        // Collect last 5 expenses across all groups, sorted newest first
        List<Map.Entry<Expense, Group>> recent = new ArrayList<>();
        for (Group g : groups) {
            for (Expense e : g.getExpenses()) recent.add(Map.entry(e, g));
        }
        recent.sort((a, b) -> b.getKey().getDateTime().compareTo(a.getKey().getDateTime()));
        recent = recent.stream().limit(5).collect(Collectors.toList());

        if (recent.isEmpty()) {
            JLabel empty = new JLabel("No expenses yet. Add your first expense in any group.");
            empty.setFont(UITheme.FONT_SMALL);
            empty.setForeground(UITheme.TEXT_DIM);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(empty);
        } else {
            for (Map.Entry<Expense, Group> entry : recent) {
                Expense ex = entry.getKey();
                Group g = entry.getValue();
                card.add(buildActivityRow(ex, g, app));
                card.add(Box.createVerticalStrut(6));
            }
        }
        return card;
    }

    private JPanel buildActivityRow(Expense ex, Group g, SmartSplitApp app) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Category color dot
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g2d) {
                Graphics2D g2 = (Graphics2D) g2d.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.getCategoryColor(ex.getCategory()));
                g2.fillOval(0, 4, 8, 8);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(12, 16));

        JLabel name = new JLabel(ex.getTitle() + " · " + g.getName());
        name.setFont(UITheme.FONT_SMALL);
        name.setForeground(UITheme.TEXT_MUTED);
        name.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        name.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { app.showGroup(g); }
            public void mouseEntered(MouseEvent e) { name.setForeground(UITheme.ACCENT); }
            public void mouseExited(MouseEvent e)  { name.setForeground(UITheme.TEXT_MUTED); }
        });

        JLabel amt = new JLabel(UITheme.formatAmt(ex.getAmount()));
        amt.setFont(UITheme.FONT_SMALL);
        amt.setForeground(UITheme.TEXT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        left.setOpaque(false);
        left.add(dot);
        left.add(name);

        row.add(left, BorderLayout.WEST);
        row.add(amt, BorderLayout.EAST);
        return row;
    }

    // ─── Quick Insights ───────────────────────────────────────────────────────
    private JPanel buildInsightsPanel(List<Group> groups, ExpenseService service, SmartSplitApp app) {
        UITheme.ReflectiveCardPanel card = new UITheme.ReflectiveCardPanel(14, UITheme.BG_CARD, UITheme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.setPreferredSize(new Dimension(260, 190));
        card.setMinimumSize(new Dimension(260, 190));
        card.setMaximumSize(new Dimension(260, Integer.MAX_VALUE));

        JLabel title = new JLabel("Quick Insights");
        title.setFont(UITheme.FONT_BUTTON);
        title.setForeground(UITheme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(10));

        // Top spending category across all groups
        Map<Category, Double> catTotals = new HashMap<>();
        int settledCount = 0, pendingCount = 0;
        for (Group g : groups) {
            service.getCategoryTotals(g).forEach((c, v) -> catTotals.merge(c, v, Double::sum));
            for (Settlement s : g.getManualSettlements()) {
                if (s.isSettled()) settledCount++; else pendingCount++;
            }
        }

        Category topCat = catTotals.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse(null);

        card.add(insightRow("Top Category",
            topCat != null ? topCat.getDisplayName() : "None yet", UITheme.ACCENT_YELLOW));
        card.add(Box.createVerticalStrut(6));
        card.add(insightRow("Settlements Done", String.valueOf(settledCount), UITheme.ACCENT_GREEN));
        card.add(Box.createVerticalStrut(6));

        // Most active group
        Group mostActive = groups.stream()
            .max(Comparator.comparingInt(g -> g.getExpenses().size()))
            .orElse(null);
        card.add(insightRow("Most Active Group",
            mostActive != null ? mostActive.getName() : "N/A", UITheme.ACCENT_PURPLE));
        card.add(Box.createVerticalStrut(6));
        card.add(insightRow("Debt Simplification", "Active", UITheme.ACCENT));

        return card;
    }

    private JPanel insightRow(String label, String value, Color accent) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_DIM);
        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_SMALL);
        val.setForeground(accent);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    // ─── Group card ───────────────────────────────────────────────────────────
    private JPanel buildGroupCard(Group g, SmartSplitApp app, ExpenseService service) {
        UITheme.ReflectiveCardPanel card = new UITheme.ReflectiveCardPanel(16, UITheme.BG_CARD, UITheme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        card.setPreferredSize(new Dimension(270, 200));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Type badge — styled text pill (NOT a big circle)
        JPanel typeBadge = buildTypeBadge(g.getType());
        typeBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel(g.getName());
        name.setFont(UITheme.FONT_SUBHEAD);
        name.setForeground(UITheme.TEXT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Expense count sub-label
        JLabel expCount = new JLabel(g.getExpenses().size() + " expenses");
        expCount.setFont(UITheme.FONT_SMALL);
        expCount.setForeground(UITheme.TEXT_DIM);
        expCount.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatars
        JPanel avatars = new JPanel(new FlowLayout(FlowLayout.LEFT, -4, 0));
        avatars.setOpaque(false);
        avatars.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < Math.min(g.getMembers().size(), 5); i++) {
            avatars.add(UITheme.avatar(g.getMembers().get(i), 24));
        }
        if (g.getMembers().size() > 5) {
            JLabel more = new JLabel("+" + (g.getMembers().size()-5));
            more.setFont(UITheme.FONT_SMALL);
            more.setForeground(UITheme.TEXT_DIM);
            avatars.add(more);
        }
        JLabel mCount = new JLabel("  " + g.getMembers().size() + " members");
        mCount.setFont(UITheme.FONT_SMALL);
        mCount.setForeground(UITheme.TEXT_MUTED);
        avatars.add(mCount);

        // Budget or total
        JPanel budgetRow = new JPanel(new BorderLayout(6, 0));
        budgetRow.setOpaque(false);
        budgetRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        budgetRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        if (g.getBudget() > 0) {
            double pct = Math.min(1.0, g.getTotalSpent() / g.getBudget());
            JLabel spentLbl = new JLabel(UITheme.formatAmt(g.getTotalSpent()) + " / " + UITheme.formatAmt(g.getBudget()));
            spentLbl.setFont(UITheme.FONT_SMALL);
            spentLbl.setForeground(pct > 0.9 ? UITheme.DANGER : UITheme.TEXT_MUTED);
            budgetRow.add(spentLbl, BorderLayout.NORTH);
            budgetRow.add(UITheme.progressBar(pct, pct > 0.9 ? UITheme.DANGER : UITheme.ACCENT_GREEN, 4), BorderLayout.SOUTH);
        } else {
            JLabel spentLbl = new JLabel(UITheme.formatAmt(g.getTotalSpent()) + " total");
            spentLbl.setFont(UITheme.FONT_SUBHEAD);
            spentLbl.setForeground(UITheme.ACCENT);
            budgetRow.add(spentLbl, BorderLayout.CENTER);
        }

        card.add(typeBadge);
        card.add(Box.createVerticalStrut(10));
        card.add(name);
        card.add(Box.createVerticalStrut(2));
        card.add(expCount);
        card.add(Box.createVerticalStrut(8));
        card.add(avatars);
        card.add(Box.createVerticalGlue());
        card.add(budgetRow);

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBgColor(UITheme.BG_CARD_HOVER); }
            public void mouseExited(MouseEvent e)  { card.setBgColor(UITheme.BG_CARD); }
            public void mouseClicked(MouseEvent e) { app.showGroup(g); }
        });
        UITheme.attachHoverLift(card, 2);
        return card;
    }

    /** Builds a small colored pill badge for the group type — no big circle */
    private JPanel buildTypeBadge(GroupType type) {
        // Choose colors per type
        Color[] typeColors = {
            new Color(0x3FB950), // GENERAL - green
            new Color(0x58A6FF), // ROOMMATES - blue
            new Color(0xE3B341), // OFFICE - yellow
            new Color(0x79C0FF), // TRAVEL - light blue
            new Color(0xFF9BBA), // COUPLE - pink
            new Color(0xBC8CFF), // EVENT - purple
            new Color(0xFFA657), // STUDENTS - orange
        };
        GroupType[] types = GroupType.values();
        int idx = 0;
        for (int i = 0; i < types.length; i++) {
            if (types[i] == type) { idx = i; break; }
        }
        final Color badgeColor = typeColors[idx % typeColors.length];
        final String displayName = type.getDisplayName();

        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(badgeColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, getHeight(), getHeight());
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(badgeColor);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(displayName.toUpperCase(), (getWidth()-fm.stringWidth(displayName.toUpperCase()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(new Font("Segoe UI", Font.BOLD, 10));
                int w = fm.stringWidth(displayName.toUpperCase()) + 18;
                return new Dimension(w, 20);
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(badge.getPreferredSize());
        badge.setMaximumSize(badge.getPreferredSize());

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        wrap.add(badge);
        return wrap;
    }

    private JPanel buildEmptyGroupsState(SmartSplitApp app) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = new JLabel("No groups yet");
        t.setFont(UITheme.FONT_SUBHEAD);
        t.setForeground(UITheme.TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel s = new JLabel("Click '+ New Group' above to create your first expense group.");
        s.setFont(UITheme.FONT_BODY);
        s.setForeground(UITheme.TEXT_MUTED);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(t);
        p.add(Box.createVerticalStrut(6));
        p.add(s);
        return p;
    }
}
