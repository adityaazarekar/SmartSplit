package com.expense.gui;

import com.expense.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class CreateGroupDialog extends JDialog {
    private Group result = null;

    public CreateGroupDialog(SmartSplitApp parent) {
        super(parent, "Create New Group", true);
        setBackground(UITheme.BG_MEDIUM);
        getContentPane().setBackground(UITheme.BG_MEDIUM);
        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel root = new JPanel();
        root.setBackground(UITheme.BG_MEDIUM);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        JLabel title = new JLabel("✨ Create New Group");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Set up a group and start tracking expenses together.");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = UITheme.styledField("e.g. Goa Trip or Flat 9B", 400);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<GroupType> typeBox = UITheme.styledCombo(GroupType.values());
        typeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        typeBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(GroupType.values()[0].getDescription());
        descLabel.setFont(UITheme.FONT_SMALL);
        descLabel.setForeground(UITheme.ACCENT);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeBox.addActionListener(e -> {
            GroupType gt = (GroupType) typeBox.getSelectedItem();
            descLabel.setText(gt != null ? UITheme.getGroupEmoji(gt) + "  " + gt.getDescription() : "");
        });

        JTextField membersField = UITheme.styledField("e.g. Rahul, Priya, Amit", 400);
        membersField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        membersField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField budgetField = UITheme.styledField("Optional (e.g. 10000)", 400);
        budgetField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        budgetField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.installAmountValidation(budgetField, 9, 2);

        JButton createBtn = UITheme.pillButton("Create Group", UITheme.ACCENT, 400, 44);
        createBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton cancelBtn = UITheme.ghostButton("Cancel", UITheme.TEXT_MUTED);
        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cancelBtn.addActionListener(e -> dispose());

        root.add(title);
        root.add(Box.createVerticalStrut(4));
        root.add(sub);
        root.add(Box.createVerticalStrut(22));
        root.add(UITheme.sectionLabel("Group Name"));
        root.add(Box.createVerticalStrut(4));
        root.add(nameField);
        root.add(Box.createVerticalStrut(14));
        root.add(UITheme.sectionLabel("Lifestyle Category"));
        root.add(Box.createVerticalStrut(4));
        root.add(typeBox);
        root.add(Box.createVerticalStrut(4));
        root.add(descLabel);
        root.add(Box.createVerticalStrut(14));
        root.add(UITheme.sectionLabel("Members (comma separated)"));
        root.add(Box.createVerticalStrut(4));
        root.add(membersField);
        root.add(Box.createVerticalStrut(14));
        root.add(UITheme.sectionLabel("Budget (optional)"));
        root.add(Box.createVerticalStrut(4));
        root.add(budgetField);
        root.add(Box.createVerticalStrut(24));
        root.add(createBtn);
        root.add(Box.createVerticalStrut(8));
        root.add(cancelBtn);

        createBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String membersStr = membersField.getText().trim();
            if (name.isEmpty() || membersStr.isEmpty()) {
                UITheme.showThemedMessage(this, "Validation Error", "Group name and members are required.", true);
                return;
            }
            GroupType type = (GroupType) typeBox.getSelectedItem();
            result = new Group(name, type);
            result.addMember(parent.getLoggedInUser());
            result.setRole(parent.getLoggedInUser(), UserRole.OWNER);
            for (String m : membersStr.split(",")) {
                String mn = m.trim();
                if (!mn.isEmpty()) {
                    User u = new User(parent.getNextUserId(), mn);
                    result.addMember(u);
                    result.setRole(u, UserRole.VIEWER);
                }
            }
            String budgetText = budgetField.getText().trim();
            if (!budgetText.isEmpty()) {
                try {
                    double budget = Double.parseDouble(budgetText);
                    if (budget < 0) {
                        UITheme.showThemedMessage(this, "Validation Error", "Budget cannot be negative.", true);
                        return;
                    }
                    result.setBudget(budget);
                } catch (Exception ex) {
                    UITheme.showThemedMessage(this, "Validation Error", "Budget must contain only numbers.", true);
                    return;
                }
            }
            dispose();
        });

        add(root);
    }

    public Group getResult() { return result; }
}
