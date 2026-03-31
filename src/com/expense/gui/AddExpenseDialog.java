package com.expense.gui;

import com.expense.model.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.*;


public class AddExpenseDialog extends JDialog {
    private Expense result = null;

    public AddExpenseDialog(SmartSplitApp parent, Group group) {
        super(parent, "Add Expense", true);
        setBackground(UITheme.BG_MEDIUM);
        getContentPane().setBackground(UITheme.BG_MEDIUM);
        setSize(520, 820);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel root = new JPanel();
        root.setBackground(UITheme.BG_MEDIUM);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel dlgTitle = new JLabel("💰 Add New Expense");
        dlgTitle.setFont(UITheme.FONT_HEADING);
        dlgTitle.setForeground(UITheme.TEXT);
        dlgTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Log a new expense and we'll handle the rest.");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField titleField = UITheme.styledField("e.g. Hotel Stay, Birthday Cake", 428);
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField amountField = UITheme.styledField(UITheme.CURRENCY_SYMBOL + "$1 Amount", 428);
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.installAmountValidation(amountField, 9, 2);

        // Category with suggested first
        Category[] suggestedCats = group.getType().getSuggestedCategories();
        Category[] allCats = Category.values();
        Set<Category> seen = new LinkedHashSet<>(Arrays.asList(suggestedCats));
        seen.addAll(Arrays.asList(allCats));
        Category[] orderedCats = seen.toArray(new Category[0]);

        JComboBox<Category> catBox = UITheme.styledCombo(orderedCats);
        catBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        catBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        catBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof Category c) setText(UITheme.getCategoryEmoji(c) + "  " + c.getDisplayName());
                setBackground(s ? UITheme.BG_CARD_HOVER : UITheme.BG_INPUT);
                setForeground(UITheme.TEXT);
                return this;
            }
        });

        JComboBox<User> paidByBox = UITheme.styledCombo(group.getMembers().toArray(new User[0]));
        paidByBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        paidByBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField noteField = UITheme.styledField("Optional note", 428);
        noteField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        noteField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField receiptField = UITheme.styledField("Optional image path for bill/receipt", 428);
        receiptField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        receiptField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton browseReceipt = UITheme.ghostButton("Attach Receipt Photo", UITheme.ACCENT);
        browseReceipt.setAlignmentX(Component.LEFT_ALIGNMENT);
        browseReceipt.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "webp"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                receiptField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JCheckBox equalSplit = new JCheckBox("Split equally among all members");
        equalSplit.setFont(UITheme.FONT_BODY);
        equalSplit.setForeground(UITheme.TEXT);
        equalSplit.setOpaque(false);
        equalSplit.setSelected(true);
        equalSplit.setAlignmentX(Component.LEFT_ALIGNMENT);
        equalSplit.setFocusPainted(false);

        JButton addBtn = UITheme.pillButton("Add Expense", UITheme.ACCENT_GREEN, 428, 44);
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton cancelBtn = UITheme.ghostButton("Cancel", UITheme.TEXT_MUTED);
        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cancelBtn.addActionListener(e -> dispose());

        root.add(dlgTitle);
        root.add(Box.createVerticalStrut(4));
        root.add(sub);
        root.add(Box.createVerticalStrut(20));
        root.add(UITheme.sectionLabel("Title"));
        root.add(Box.createVerticalStrut(4));
        root.add(titleField);
        root.add(Box.createVerticalStrut(12));
        root.add(UITheme.sectionLabel("Amount (" + UITheme.CURRENCY_SYMBOL + ")"));
        root.add(Box.createVerticalStrut(4));
        root.add(amountField);
        root.add(Box.createVerticalStrut(12));
        root.add(UITheme.sectionLabel("Category"));
        root.add(Box.createVerticalStrut(4));
        root.add(catBox);
        root.add(Box.createVerticalStrut(12));
        root.add(UITheme.sectionLabel("Paid By"));
        root.add(Box.createVerticalStrut(4));
        root.add(paidByBox);
        root.add(Box.createVerticalStrut(12));
        root.add(UITheme.sectionLabel("Note"));
        root.add(Box.createVerticalStrut(4));
        root.add(noteField);
        root.add(Box.createVerticalStrut(10));
        root.add(UITheme.sectionLabel("Receipt / Proof Photo (Optional)"));
        root.add(Box.createVerticalStrut(4));
        root.add(receiptField);
        root.add(Box.createVerticalStrut(6));
        root.add(browseReceipt);
        root.add(Box.createVerticalStrut(10));
        root.add(equalSplit);
        root.add(Box.createVerticalStrut(20));
        root.add(addBtn);
        root.add(Box.createVerticalStrut(8));
        root.add(cancelBtn);

        addBtn.addActionListener(e -> {
            try {
                String t = titleField.getText().trim();
                String amountRaw = amountField.getText().trim().replace(UITheme.CURRENCY_SYMBOL + "$1", "").trim();
                if (amountRaw.isEmpty()) {
                    UITheme.showThemedMessage(this, "Validation Error", "Add only numbers in Amount.", true);
                    return;
                }
                double amount = Double.parseDouble(amountRaw);
                if (amount <= 0) {
                    UITheme.showThemedMessage(this, "Validation Error", "Amount should be greater than 0.", true);
                    return;
                }
                Category cat = (Category) catBox.getSelectedItem();
                User paidBy = (User) paidByBox.getSelectedItem();
                String note = noteField.getText().trim();

                Map<User, Double> split = new LinkedHashMap<>();
                if (equalSplit.isSelected()) {
                    double each = amount / group.getMembers().size();
                    for (User u : group.getMembers()) split.put(u, each);
                } else {
                    for (User u : group.getMembers()) {
                        JTextField uf = UITheme.styledField("Amount for " + u.getName(), 300);
                        UITheme.installAmountValidation(uf, 9, 2);
                        int r = JOptionPane.showConfirmDialog(this, uf, "Amount for " + u.getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                        if (r == JOptionPane.OK_OPTION) {
                            if (uf.getText().trim().isEmpty()) {
                                UITheme.showThemedMessage(this, "Validation Error", "Add only numbers for split amount.", true);
                                return;
                            }
                            split.put(u, Double.parseDouble(uf.getText().trim()));
                        } else { dispose(); return; }
                    }
                }
                String receiptPath = receiptField.getText().trim().isEmpty() ? null : receiptField.getText().trim();
                result = new Expense(t.isEmpty() ? "Expense" : t, amount, cat, paidBy, split, note, receiptPath);
                dispose();
            } catch (NumberFormatException ex) {
                UITheme.showThemedMessage(this, "Validation Error", "Add only numbers in Amount.", true);
            }
        });

        add(root);
    }

    public Expense getResult() { return result; }
}
