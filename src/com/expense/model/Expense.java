package com.expense.model;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Map;

public class Expense implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int counter = 0;
    private int id;
    private String title;
    private double amount;
    private Category category;
    private User paidBy;
    private Map<User, Double> split;
    private LocalDateTime dateTime;
    private String note;
    private String receiptImagePath;

    public Expense(String title, double amount, Category category, User paidBy, Map<User, Double> split, String note) {
        this(title, amount, category, paidBy, split, note, null);
    }

    public Expense(String title, double amount, Category category, User paidBy, Map<User, Double> split, String note, String receiptImagePath) {
        this.id = ++counter;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.paidBy = paidBy;
        this.split = split;
        this.dateTime = LocalDateTime.now();
        this.note = note;
        this.receiptImagePath = receiptImagePath;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public Category getCategory() { return category; }
    public User getPaidBy() { return paidBy; }
    public Map<User, Double> getSplit() { return split; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getNote() { return note; }
    public String getReceiptImagePath() { return receiptImagePath; }

    @Override
    public String toString() { return title + " - Rs." + String.format("%.2f", amount); }
}
