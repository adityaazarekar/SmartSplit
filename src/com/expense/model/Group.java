package com.expense.model;

import java.util.ArrayList;
import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int counter = 0;
    private int id;
    private String name;
    private GroupType type;
    private List<User> members;
    private List<Expense> expenses;
    private List<Settlement> manualSettlements;
    private double budget;
    private java.util.Map<User, UserRole> roles;


    public Group(String name, GroupType type) {
        this.id = ++counter;
        this.name = name;
        this.type = type;
        this.members = new ArrayList<>();
        this.expenses = new ArrayList<>();
        this.manualSettlements = new ArrayList<>();
        this.budget = 0;

    }

    public void addMember(User user) { members.add(user); }
    public void addExpense(Expense expense) { expenses.add(expense); }
    public void addSettlement(Settlement s) { manualSettlements.add(s); }
    public void setBudget(double budget) { this.budget = budget; }


    public int getId() { return id; }
    public String getName() { return name; }
    public GroupType getType() { return type; }
    public List<User> getMembers() { return members; }
    public List<Expense> getExpenses() { return expenses; }
    public List<Settlement> getManualSettlements() { return manualSettlements; }
    public double getBudget() { return budget; }

    public void setRole(User user, UserRole role) {
        if (roles == null) roles = new java.util.HashMap<>();
        roles.put(user, role);
    }
    
    public UserRole getRole(User user) {
        if (roles == null) roles = new java.util.HashMap<>();
        return roles.getOrDefault(user, UserRole.VIEWER);
    }


    public double getTotalSpent() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    @Override
    public String toString() { return name; }
}
