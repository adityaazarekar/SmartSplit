package com.expense.gui;

import com.expense.model.*;
import com.expense.service.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class HomePanel extends JPanel {

    public HomePanel(SmartSplitApp app, List<Group> groups, ExpenseService service) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);

        // ── Top nav bar with welcome ──
        JPanel topBar = buildTopBar(app, groups, service);
        add(topBar, BorderLayout.NORTH);

        // ── Scrollable content ──
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(28, 32, 32, 32));

        // Stat cards row
        content.add(buildStatRow(app, groups, service));
        content.add(Box.createVerticalStrut(32));

        // Quick tip banner
        content.add(buildTipBanner(groups));
        content.add(Box.createVerticalStrut(28));

        // Groups section
        JLabel groupsTitle = new JLabel("Your Groups");
        groupsTitle.setFont(UITheme.FONT_HEADING);
        groupsTitle.setForeground(UITheme.TEXT);
        groupsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(groupsTitle);
        content.add(Box.createVerticalStrut(4));
        JLabel groupsSub = new JLabel("Click a group to view expenses, balances, and analytics");
        groupsSub.setFont(UITheme.FONT_SMALL);
        groupsSub.setForeground(UITheme.TEXT_MUTED);
        groupsSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(groupsSub);
        content.add(Box.createVerticalStrut(16));

        if (groups.isEmpty()) {
            JLabel empty = new JLabel("No groups yet - click '+ New Group' in the sidebar to get started!");
            empty.setFont(UITheme.FONT_BODY);
            empty.setForeground(UITheme.TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(empty);
        } else {
            JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
            grid.setOpaque(false);
            grid.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (Group g : groups) grid.add(buildGroupCard(g, app, service));
            content.add(grid);
        }

        add(UITheme.styledScroll(content), BorderLayout.CENTER);
    }

    private JPanel buildTopBar(SmartSplitApp app, List<Group> groups, ExpenseService service) {
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
        JLabel userLbl = new JLabel(app.getLoggedInUser().getName());
        userLbl.setFont(UITheme.FONT_BODY);
        userLbl.setForeground(UITheme.TEXT_MUTED);
        JButton profileBtn = UITheme.ghostButton("Edit Profile", UITheme.ACCENT);
        profileBtn.setPreferredSize(new Dimension(130, 40));
        profileBtn.addActionListener(e -> app.showProfileDialog());
        JPanel av = UITheme.avatar(app.getLoggedInUser(), 34);
        right.add(profileBtn);
        right.add(userLbl);
        right.add(av);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildStatRow(SmartSplitApp app, List<Group> groups, ExpenseService service) {
        double owes = 0, owed = 0, spent = 0;
        for (Group g : groups) {
            owes  += service.getUserOwes(g, app.getLoggedInUser());
            owed  += service.getUserIsOwed(g, app.getLoggedInUser());
            spent += g.getTotalSpent();
        }

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(UITheme.statCard("In Debt",       UITheme.formatAmt(owes),   UITheme.DANGER,         UITheme.BG_CARD));
        row.add(UITheme.statCard("In Profit",     UITheme.formatAmt(owed),   UITheme.ACCENT_GREEN,   UITheme.BG_CARD));
        row.add(UITheme.statCard("Total Spent",   UITheme.formatAmt(spent),  UITheme.ACCENT,         UITheme.BG_CARD));
        row.add(UITheme.statCard("Groups",         String.valueOf(groups.size()), UITheme.ACCENT_PURPLE, UITheme.BG_CARD));

        return row;
    }

    private JPanel buildTipBanner(List<Group> groups) {
        UITheme.CardPanel banner = new UITheme.CardPanel(12, new Color(0x58A6FF, false) {
            { /* blue tinted bg */ }
            public int getRGB() { return new Color(0x0D1F33).getRGB(); }
        }, new Color(0x1F4068));
        banner.setLayout(new BorderLayout(16, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel("i");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        icon.setForeground(UITheme.ACCENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Debt Simplification Active");
        t.setFont(UITheme.FONT_BUTTON);
        t.setForeground(UITheme.ACCENT);
        JLabel s;
        int totalSettlements = 0;
        for (Group g : groups) totalSettlements += g.getMembers().size() > 1 ? 1 : 0;
        s = new JLabel("Our algorithm minimizes the number of payments. Open any group → Balances to see simplified settlements.");
        s.setFont(UITheme.FONT_SMALL);
        s.setForeground(UITheme.TEXT_MUTED);
        text.add(t); text.add(s);

        banner.add(icon, BorderLayout.WEST);
        banner.add(text, BorderLayout.CENTER);
        return banner;
    }

    private JPanel buildGroupCard(Group g, SmartSplitApp app, ExpenseService service) {
        UITheme.ReflectiveCardPanel card = new UITheme.ReflectiveCardPanel(16, UITheme.BG_CARD, UITheme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(280, 210));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Type badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel badge = UITheme.badge(g.getType().getDisplayName(), UITheme.ACCENT, new Color(0x58A6FF20, true));
        topRow.add(new JLabel(" "), BorderLayout.WEST);
        topRow.add(badge, BorderLayout.EAST);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel(g.getName());
        name.setFont(UITheme.FONT_SUBHEAD);
        name.setForeground(UITheme.TEXT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatars row
        JPanel avatars = new JPanel(new FlowLayout(FlowLayout.LEFT, -5, 0));
        avatars.setOpaque(false);
        avatars.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < Math.min(g.getMembers().size(), 5); i++) {
            User u = g.getMembers().get(i);
            JPanel av = UITheme.avatar(u, 26);
            avatars.add(av);
        }
        JLabel mCount = new JLabel("  " + g.getMembers().size() + " members");
        mCount.setFont(UITheme.FONT_SMALL);
        mCount.setForeground(UITheme.TEXT_MUTED);
        avatars.add(mCount);

        // Budget bar (if set)
        JPanel budgetRow = new JPanel(new BorderLayout(8, 0));
        budgetRow.setOpaque(false);
        budgetRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        budgetRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        if (g.getBudget() > 0) {
            double pct = g.getTotalSpent() / g.getBudget();
            JLabel spentLbl = new JLabel(UITheme.formatAmt(g.getTotalSpent()) + " / " + UITheme.CURRENCY_SYMBOL + "$1" + UITheme.formatAmt(g.getBudget()));
            spentLbl.setFont(UITheme.FONT_SMALL);
            spentLbl.setForeground(pct > 0.9 ? UITheme.DANGER : UITheme.TEXT_MUTED);
            budgetRow.add(spentLbl, BorderLayout.NORTH);
            budgetRow.add(UITheme.progressBar(pct, UITheme.ACCENT_GREEN, 5), BorderLayout.SOUTH);
        } else {
            JLabel spentLbl = new JLabel(UITheme.formatAmt(g.getTotalSpent()) + " total");
            spentLbl.setFont(UITheme.FONT_SUBHEAD);
            spentLbl.setForeground(UITheme.ACCENT);
            budgetRow.add(spentLbl, BorderLayout.CENTER);
        }

        card.add(topRow);
        card.add(Box.createVerticalStrut(8));
        card.add(name);
        card.add(Box.createVerticalStrut(6));
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


}
