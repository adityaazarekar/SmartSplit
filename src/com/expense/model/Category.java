package com.expense.model;

public enum Category {
    FOOD("Food & Drinks"),
    TRAVEL("Travel"),
    RENT("Rent"),
    UTILITIES("Utilities"),
    GROCERIES("Groceries"),
    ENTERTAINMENT("Entertainment"),
    SHOPPING("Shopping"),
    TRANSPORT("Transport"),
    MEDICAL("Medical"),
    GIFT("Gift Collection"),
    PARTY("Party/Event"),
    SUBSCRIPTION("Subscription"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
