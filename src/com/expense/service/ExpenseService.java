package com.expense.service;

import com.expense.model.*;
import java.util.*;

public class ExpenseService {

    public Map<User, Map<User, Double>> computeBalances(Group group) {
        Map<User, Map<User, Double>> balances = new HashMap<>();
        for (Expense expense : group.getExpenses()) {
            User paidBy = expense.getPaidBy();
            for (Map.Entry<User, Double> entry : expense.getSplit().entrySet()) {
                User user = entry.getKey();
                double amount = entry.getValue();
                if (!user.equals(paidBy)) {
                    balances.computeIfAbsent(user, k -> new HashMap<>());
                    balances.get(user).merge(paidBy, amount, Double::sum);
                }
            }
        }
        return balances;
    }

    public List<Settlement> simplifyDebts(Group group) {
        Map<User, Double> netBalances = new HashMap<>();
        Map<User, Map<User, Double>> balances = computeBalances(group);

        for (User debtor : balances.keySet()) {
            for (User creditor : balances.get(debtor).keySet()) {
                double amount = balances.get(debtor).get(creditor);
                netBalances.merge(debtor, -amount, Double::sum);
                netBalances.merge(creditor, amount, Double::sum);
            }
        }

        List<double[]> debtAmts = new ArrayList<>();
        List<double[]> creditAmts = new ArrayList<>();
        List<User> debtorUsers = new ArrayList<>();
        List<User> creditorUsers = new ArrayList<>();

        for (Map.Entry<User, Double> entry : netBalances.entrySet()) {
            if (entry.getValue() < -0.01) {
                debtorUsers.add(entry.getKey());
                debtAmts.add(new double[]{Math.abs(entry.getValue())});
            } else if (entry.getValue() > 0.01) {
                creditorUsers.add(entry.getKey());
                creditAmts.add(new double[]{entry.getValue()});
            }
        }

        List<Settlement> settlements = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debtAmts.size() && j < creditAmts.size()) {
            double settleAmt = Math.min(debtAmts.get(i)[0], creditAmts.get(j)[0]);
            settlements.add(new Settlement(debtorUsers.get(i), creditorUsers.get(j), settleAmt));
            debtAmts.get(i)[0] -= settleAmt;
            creditAmts.get(j)[0] -= settleAmt;
            if (debtAmts.get(i)[0] < 0.01) i++;
            if (creditAmts.get(j)[0] < 0.01) j++;
        }
        return settlements;
    }

    public Map<Category, Double> getCategoryTotals(Group group) {
        Map<Category, Double> totals = new HashMap<>();
        for (Expense expense : group.getExpenses()) {
            totals.merge(expense.getCategory(), expense.getAmount(), Double::sum);
        }
        return totals;
    }

    public Map<User, Double> getUserSpending(Group group) {
        Map<User, Double> totals = new HashMap<>();
        for (Expense expense : group.getExpenses()) {
            totals.merge(expense.getPaidBy(), expense.getAmount(), Double::sum);
        }
        return totals;
    }

    public double getUserOwes(Group group, User user) {
        Map<User, Map<User, Double>> balances = computeBalances(group);
        double total = 0;
        if (balances.containsKey(user)) {
            for (double amt : balances.get(user).values()) total += amt;
        }
        return total;
    }

    public double getUserIsOwed(Group group, User user) {
        Map<User, Map<User, Double>> balances = computeBalances(group);
        double total = 0;
        for (User debtor : balances.keySet()) {
            if (balances.get(debtor).containsKey(user)) {
                total += balances.get(debtor).get(user);
            }
        }
        return total;
    }
}
