package com.expense.model;

public enum GroupType {
    GENERAL("General", "For any expense sharing scenario",
        new String[]{"Add Expense", "Split Bill", "View Balances"},
        new Category[]{Category.FOOD, Category.ENTERTAINMENT, Category.OTHER}),

    ROOMMATES("Roommates", "For flatmates sharing rent & bills",
        new String[]{"Split Rent", "Utility Bill", "Grocery Run", "Monthly Summary"},
        new Category[]{Category.RENT, Category.UTILITIES, Category.GROCERIES, Category.FOOD}),

    OFFICE("Office / Work", "For colleagues and work events",
        new String[]{"Lunch Split", "Party Fund", "Gift Collection", "Team Outing"},
        new Category[]{Category.FOOD, Category.PARTY, Category.GIFT, Category.ENTERTAINMENT}),

    TRAVEL("Travel / Trip", "For trip companions",
        new String[]{"Hotel Bill", "Transport", "Food Split", "Trip Summary"},
        new Category[]{Category.TRAVEL, Category.FOOD, Category.TRANSPORT, Category.ENTERTAINMENT}),

    COUPLE("Couple / Family", "For partners or family members",
        new String[]{"Split 50/50", "Custom Ratio", "Add Subscription"},
        new Category[]{Category.FOOD, Category.GROCERIES, Category.SUBSCRIPTION, Category.ENTERTAINMENT}),

    EVENT("Event / Party", "For one-time events like weddings or birthdays",
        new String[]{"Add Contribution", "Track Budget", "Per-Head Cost"},
        new Category[]{Category.PARTY, Category.FOOD, Category.ENTERTAINMENT, Category.OTHER}),

    STUDENTS("Students / Classmates", "For project groups & college friends",
        new String[]{"Project Expense", "Snack Fund", "Shared Supplies"},
        new Category[]{Category.FOOD, Category.SHOPPING, Category.OTHER});

    private final String displayName;
    private final String description;
    private final String[] quickActions;
    private final Category[] suggestedCategories;

    GroupType(String displayName, String description, String[] quickActions, Category[] suggestedCategories) {
        this.displayName = displayName;
        this.description = description;
        this.quickActions = quickActions;
        this.suggestedCategories = suggestedCategories;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String[] getQuickActions() { return quickActions; }
    public Category[] getSuggestedCategories() { return suggestedCategories; }

    @Override
    public String toString() { return displayName; }
}
