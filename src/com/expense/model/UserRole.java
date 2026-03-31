package com.expense.model;

import java.io.Serializable;

public enum UserRole implements Serializable {
    OWNER("Owner"),
    EDITOR("Editor"),
    VIEWER("Viewer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
