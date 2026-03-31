package com.expense.model;

import java.time.LocalDateTime;
import java.io.Serializable;

public class Settlement implements Serializable {
    private static final long serialVersionUID = 1L;
    private User from;
    private User to;
    private double amount;
    private LocalDateTime dateTime;
    private boolean settled;
    private PaymentMethod paymentMethod;

    public Settlement(User from, User to, double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.dateTime = LocalDateTime.now();
        this.settled = false;
        this.paymentMethod = PaymentMethod.CASH;
    }

    public User getFrom() { return from; }
    public User getTo() { return to; }
    public double getAmount() { return amount; }
    public LocalDateTime getDateTime() { return dateTime; }
    public boolean isSettled() { return settled; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void markSettled() { this.settled = true; }
    public void markSettled(PaymentMethod paymentMethod) {
        this.settled = true;
        this.paymentMethod = paymentMethod == null ? PaymentMethod.CASH : paymentMethod;
    }

    @Override
    public String toString() {
        return from.getName() + " pays " + to.getName() + " Rs." + String.format("%.2f", amount)
                + " [" + paymentMethod.getBadgeText() + "]";
    }
}
