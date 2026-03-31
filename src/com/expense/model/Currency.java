package com.expense.model;

public enum Currency {
    INR("₹", "Indian Rupee"),
    USD("$", "US Dollar"),
    EUR("€", "Euro"),
    GBP("£", "British Pound"),
    JPY("¥", "Japanese Yen"),
    AUD("A$", "Australian Dollar");

    private final String symbol;
    private final String name;

    Currency(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    
    @Override
    public String toString() { return symbol + " " + name(); }
}
