package com.expense.service;

import com.expense.model.*;
import java.io.*;
import java.util.*;

public class FileManager {
    public static class UserWorkspace implements Serializable {
        private static final long serialVersionUID = 1L;
        public String username;
        public String password;
        public User profile;
        public List<Group> groups = new ArrayList<>();
        public int nextUserId = 1;
        public com.expense.model.Currency currency = com.expense.model.Currency.INR;
    }

    public static class AppState implements Serializable {
        private static final long serialVersionUID = 1L;
        public Map<String, UserWorkspace> users = new LinkedHashMap<>();
    }

    private static final String DATA_FILE = "smartsplit-data.bin";

    public static void exportGroupToCSV(Group group, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Title,Amount,Category,Paid By,Date,Note");
            for (Expense exp : group.getExpenses()) {
                writer.printf("%s,%.2f,%s,%s,%s,%s%n",
                    exp.getTitle(), exp.getAmount(), exp.getCategory().getDisplayName(),
                    exp.getPaidBy().getName(), exp.getDateTime().toString(),
                    exp.getNote() != null ? exp.getNote() : "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateReport(Group group, ExpenseService service) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("  ").append(group.getName()).append(" - Report\n");
        sb.append("========================================\n\n");
        sb.append("Type: ").append(group.getType().getDisplayName()).append("\n");
        sb.append("Members: ").append(group.getMembers().size()).append("\n");
        sb.append("Total Spent: Rs.").append(String.format("%.2f", group.getTotalSpent())).append("\n");
        if (group.getBudget() > 0) {
            sb.append("Budget: Rs.").append(String.format("%.2f", group.getBudget())).append("\n");
            sb.append("Remaining: Rs.").append(String.format("%.2f", group.getBudget() - group.getTotalSpent())).append("\n");
        }
        sb.append("\n--- Expenses ---\n");
        for (Expense exp : group.getExpenses()) {
            sb.append(String.format("  %-20s | Rs.%8.2f | %-15s | Paid by %s\n",
                exp.getTitle(), exp.getAmount(), exp.getCategory(), exp.getPaidBy().getName()));
        }
        sb.append("\n--- Simplified Settlements ---\n");
        for (Settlement s : service.simplifyDebts(group)) {
            sb.append("  ").append(s.toString()).append("\n");
        }
        return sb.toString();
    }

    public static AppState loadAppState() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return new AppState();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof AppState s) return s;
        } catch (Exception ignored) {}
        return new AppState();
    }

    public static void saveAppState(AppState state) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
