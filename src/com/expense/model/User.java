package com.expense.model;

import java.awt.Color;
import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private Color profileColor;
    private String profileImagePath;

    private static final Color[] COLORS = {
        new Color(0xE94560), new Color(0x4ECDC4), new Color(0x66C0F4),
        new Color(0xF4A460), new Color(0x9B59B6), new Color(0x3498DB),
        new Color(0xE67E22), new Color(0x1ABC9C), new Color(0xE74C3C),
        new Color(0x2ECC71)
    };

    public User(int id, String name) {
        this.id = id;
        this.name = name;
        this.profileColor = COLORS[(id - 1) % COLORS.length];
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Color getProfileColor() { return profileColor; }
    public String getInitial() { return name.substring(0, 1).toUpperCase(); }
    public String getProfileImagePath() { return profileImagePath; }
    public void setName(String name) { if (name != null && !name.isBlank()) this.name = name.trim(); }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((User) o).id;
    }

    @Override
    public int hashCode() { return id; }
}