package com.expense.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String URL = "jdbc:sqlite:smartsplit.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            // Create AppState/Workspaces Table
            stmt.execute("CREATE TABLE IF NOT EXISTS workspaces (" +
                    "username TEXT PRIMARY KEY, " +
                    "password TEXT, " +
                    "next_user_id INTEGER, " +
                    "currency TEXT)");

            // Create Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "workspace_username TEXT, " +
                    "local_id INTEGER, " +
                    "name TEXT, " +
                    "profile_color INTEGER, " +
                    "profile_image_path TEXT, " +
                    "is_profile INTEGER, " + // 1 if it's the workspace profile user
                    "FOREIGN KEY(workspace_username) REFERENCES workspaces(username))");

            // Create Groups Table
            stmt.execute("CREATE TABLE IF NOT EXISTS groups (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "local_id INTEGER, " +
                    "workspace_username TEXT, " +
                    "name TEXT, " +
                    "type TEXT, " +
                    "budget REAL, " +
                    "FOREIGN KEY(workspace_username) REFERENCES workspaces(username))");

            // Create Group Members Table
            stmt.execute("CREATE TABLE IF NOT EXISTS group_members (" +
                    "group_id INTEGER, " +
                    "user_id INTEGER, " +
                    "role TEXT, " +
                    "FOREIGN KEY(group_id) REFERENCES groups(id), " +
                    "FOREIGN KEY(user_id) REFERENCES users(id))");

            // Create Expenses Table
            stmt.execute("CREATE TABLE IF NOT EXISTS expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "local_id INTEGER, " +
                    "group_id INTEGER, " +
                    "title TEXT, " +
                    "amount REAL, " +
                    "category TEXT, " +
                    "paid_by_id INTEGER, " +
                    "date_time TEXT, " +
                    "note TEXT, " +
                    "receipt_image_path TEXT, " +
                    "FOREIGN KEY(group_id) REFERENCES groups(id), " +
                    "FOREIGN KEY(paid_by_id) REFERENCES users(id))");

            // Create Expense Splits Table
            stmt.execute("CREATE TABLE IF NOT EXISTS expense_splits (" +
                    "expense_id INTEGER, " +
                    "user_id INTEGER, " +
                    "amount REAL, " +
                    "FOREIGN KEY(expense_id) REFERENCES expenses(id), " +
                    "FOREIGN KEY(user_id) REFERENCES users(id))");

            // Create Settlements Table
            stmt.execute("CREATE TABLE IF NOT EXISTS settlements (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "group_id INTEGER, " +
                    "from_id INTEGER, " +
                    "to_id INTEGER, " +
                    "amount REAL, " +
                    "date_time TEXT, " +
                    "settled INTEGER, " +
                    "payment_method TEXT, " +
                    "FOREIGN KEY(group_id) REFERENCES groups(id), " +
                    "FOREIGN KEY(from_id) REFERENCES users(id), " +
                    "FOREIGN KEY(to_id) REFERENCES users(id))");

            System.out.println("✅ Database schema initialized successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
