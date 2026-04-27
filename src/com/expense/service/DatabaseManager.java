package com.expense.service;

import com.expense.model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DatabaseManager {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static void saveAppState(FileManager.AppState state) {
        DBConnection.initializeDatabase();
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction

            // Clear existing data to replace it
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM expense_splits");
                stmt.execute("DELETE FROM expenses");
                stmt.execute("DELETE FROM settlements");
                stmt.execute("DELETE FROM group_members");
                stmt.execute("DELETE FROM groups");
                stmt.execute("DELETE FROM users");
                stmt.execute("DELETE FROM workspaces");
            }

            String insertWorkspace = "INSERT INTO workspaces (username, password, next_user_id, currency) VALUES (?, ?, ?, ?)";
            String insertUser = "INSERT INTO users (workspace_username, local_id, name, profile_color, profile_image_path, is_profile) VALUES (?, ?, ?, ?, ?, ?)";
            String insertGroup = "INSERT INTO groups (local_id, workspace_username, name, type, budget) VALUES (?, ?, ?, ?, ?)";
            String insertGroupMember = "INSERT INTO group_members (group_id, user_id, role) VALUES (?, ?, ?)";
            String insertExpense = "INSERT INTO expenses (local_id, group_id, title, amount, category, paid_by_id, date_time, note, receipt_image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String insertSplit = "INSERT INTO expense_splits (expense_id, user_id, amount) VALUES (?, ?, ?)";
            String insertSettlement = "INSERT INTO settlements (group_id, from_id, to_id, amount, date_time, settled, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement psWorkspace = conn.prepareStatement(insertWorkspace);
                 PreparedStatement psUser = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psGroup = conn.prepareStatement(insertGroup, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psGrpMem = conn.prepareStatement(insertGroupMember);
                 PreparedStatement psExpense = conn.prepareStatement(insertExpense, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psSplit = conn.prepareStatement(insertSplit);
                 PreparedStatement psSettlement = conn.prepareStatement(insertSettlement)) {

                for (Map.Entry<String, FileManager.UserWorkspace> entry : state.users.entrySet()) {
                    String username = entry.getKey();
                    FileManager.UserWorkspace ws = entry.getValue();

                    psWorkspace.setString(1, ws.username);
                    psWorkspace.setString(2, ws.password);
                    psWorkspace.setInt(3, ws.nextUserId);
                    psWorkspace.setString(4, ws.currency.name());
                    psWorkspace.executeUpdate();

                    // Maps local User ID to DB User ID
                    Map<Integer, Integer> userIdMap = new HashMap<>();

                    // Helper to insert user
                    java.util.function.BiConsumer<User, Boolean> saveUser = (u, isProfile) -> {
                        if (userIdMap.containsKey(u.getId())) return;
                        try {
                            psUser.setString(1, username);
                            psUser.setInt(2, u.getId());
                            psUser.setString(3, u.getName());
                            psUser.setInt(4, u.getProfileColor().getRGB());
                            psUser.setString(5, u.getProfileImagePath());
                            psUser.setInt(6, isProfile ? 1 : 0);
                            psUser.executeUpdate();
                            try (ResultSet rs = psUser.getGeneratedKeys()) {
                                if (rs.next()) userIdMap.put(u.getId(), rs.getInt(1));
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    };

                    if (ws.profile != null) saveUser.accept(ws.profile, true);

                    for (Group g : ws.groups) {
                        for (User u : g.getMembers()) saveUser.accept(u, false);

                        psGroup.setInt(1, g.getId());
                        psGroup.setString(2, username);
                        psGroup.setString(3, g.getName());
                        psGroup.setString(4, g.getType().name());
                        psGroup.setDouble(5, g.getBudget());
                        psGroup.executeUpdate();

                        int dbGroupId = -1;
                        try (ResultSet rs = psGroup.getGeneratedKeys()) {
                            if (rs.next()) dbGroupId = rs.getInt(1);
                        }

                        for (User u : g.getMembers()) {
                            psGrpMem.setInt(1, dbGroupId);
                            psGrpMem.setInt(2, userIdMap.get(u.getId()));
                            psGrpMem.setString(3, g.getRole(u).name());
                            psGrpMem.executeUpdate();
                        }

                        for (Expense e : g.getExpenses()) {
                            psExpense.setInt(1, e.getId());
                            psExpense.setInt(2, dbGroupId);
                            psExpense.setString(3, e.getTitle());
                            psExpense.setDouble(4, e.getAmount());
                            psExpense.setString(5, e.getCategory().name());
                            psExpense.setInt(6, userIdMap.get(e.getPaidBy().getId()));
                            psExpense.setString(7, e.getDateTime().format(formatter));
                            psExpense.setString(8, e.getNote());
                            psExpense.setString(9, e.getReceiptImagePath());
                            psExpense.executeUpdate();

                            int dbExpenseId = -1;
                            try (ResultSet rs = psExpense.getGeneratedKeys()) {
                                if (rs.next()) dbExpenseId = rs.getInt(1);
                            }

                            if (e.getSplit() != null) {
                                for (Map.Entry<User, Double> splitEntry : e.getSplit().entrySet()) {
                                    psSplit.setInt(1, dbExpenseId);
                                    psSplit.setInt(2, userIdMap.get(splitEntry.getKey().getId()));
                                    psSplit.setDouble(3, splitEntry.getValue());
                                    psSplit.executeUpdate();
                                }
                            }
                        }

                        for (Settlement s : g.getManualSettlements()) {
                            psSettlement.setInt(1, dbGroupId);
                            psSettlement.setInt(2, userIdMap.get(s.getFrom().getId()));
                            psSettlement.setInt(3, userIdMap.get(s.getTo().getId()));
                            psSettlement.setDouble(4, s.getAmount());
                            psSettlement.setString(5, s.getDateTime().format(formatter));
                            psSettlement.setInt(6, s.isSettled() ? 1 : 0);
                            psSettlement.setString(7, s.getPaymentMethod().name());
                            psSettlement.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static FileManager.AppState loadAppState() {
        FileManager.AppState state = new FileManager.AppState();
        try (Connection conn = DBConnection.getConnection()) {
            String qWorkspace = "SELECT * FROM workspaces";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(qWorkspace)) {
                if (!rs.next()) return null; // Use old FileManager serialization or start empty
            }

            try (Statement stmt = conn.createStatement(); ResultSet wsRs = stmt.executeQuery("SELECT * FROM workspaces")) {
                while (wsRs.next()) {
                    FileManager.UserWorkspace ws = new FileManager.UserWorkspace();
                    ws.username = wsRs.getString("username");
                    ws.password = wsRs.getString("password");
                    ws.nextUserId = wsRs.getInt("next_user_id");
                    ws.currency = com.expense.model.Currency.valueOf(wsRs.getString("currency"));
                    state.users.put(ws.username, ws);

                    Map<Integer, User> dbIdToUserMap = new HashMap<>();
                    Map<Integer, User> localIdToUserMap = new HashMap<>();

                    try (PreparedStatement pst = conn.prepareStatement("SELECT * FROM users WHERE workspace_username = ?")) {
                        pst.setString(1, ws.username);
                        try (ResultSet uRs = pst.executeQuery()) {
                            while (uRs.next()) {
                                int localId = uRs.getInt("local_id");
                                String name = uRs.getString("name");
                                User u = new User(localId, name);
                                String imgPath = uRs.getString("profile_image_path");
                                if (imgPath != null && !imgPath.isBlank()) {
                                    u.setProfileImagePath(imgPath);
                                }
                                
                                int dbId = uRs.getInt("id");
                                dbIdToUserMap.put(dbId, u);
                                localIdToUserMap.put(localId, u);

                                if (uRs.getInt("is_profile") == 1) {
                                    ws.profile = u;
                                }
                            }
                        }
                    }

                    try (PreparedStatement gSt = conn.prepareStatement("SELECT * FROM groups WHERE workspace_username = ?")) {
                        gSt.setString(1, ws.username);
                        try (ResultSet gRs = gSt.executeQuery()) {
                            while (gRs.next()) {
                                int dbGroupId = gRs.getInt("id");
                                Group g = new Group(gRs.getString("name"), GroupType.valueOf(gRs.getString("type")));
                                g.setBudget(gRs.getDouble("budget"));

                                // Wait, restoring ID is important for Group? Group constructor uses static counter.
                                // It might break if we recreate it, but counter will keep increasing.

                                // Members & Roles
                                try (PreparedStatement mSt = conn.prepareStatement("SELECT * FROM group_members WHERE group_id = ?")) {
                                    mSt.setInt(1, dbGroupId);
                                    try (ResultSet mRs = mSt.executeQuery()) {
                                        while (mRs.next()) {
                                            User u = dbIdToUserMap.get(mRs.getInt("user_id"));
                                            g.addMember(u);
                                            g.setRole(u, UserRole.valueOf(mRs.getString("role")));
                                        }
                                    }
                                }

                                // Expenses
                                try (PreparedStatement eSt = conn.prepareStatement("SELECT * FROM expenses WHERE group_id = ?")) {
                                    eSt.setInt(1, dbGroupId);
                                    try (ResultSet eRs = eSt.executeQuery()) {
                                        while (eRs.next()) {
                                            int dbExpenseId = eRs.getInt("id");
                                            
                                            Map<User, Double> splits = new HashMap<>();
                                            try (PreparedStatement spSt = conn.prepareStatement("SELECT * FROM expense_splits WHERE expense_id = ?")) {
                                                spSt.setInt(1, dbExpenseId);
                                                try (ResultSet spRs = spSt.executeQuery()) {
                                                    while (spRs.next()) {
                                                        splits.put(dbIdToUserMap.get(spRs.getInt("user_id")), spRs.getDouble("amount"));
                                                    }
                                                }
                                            }

                                            Expense exp = new Expense(
                                                eRs.getString("title"),
                                                eRs.getDouble("amount"),
                                                Category.valueOf(eRs.getString("category")),
                                                dbIdToUserMap.get(eRs.getInt("paid_by_id")),
                                                splits,
                                                eRs.getString("note"),
                                                eRs.getString("receipt_image_path")
                                            );
                                            // The dateTime is set to now in constructor. Override using reflection or let it be for now
                                            // Ideally we'd modify Expense class or inject it:
                                            try {
                                                java.lang.reflect.Field df = Expense.class.getDeclaredField("dateTime");
                                                df.setAccessible(true);
                                                df.set(exp, LocalDateTime.parse(eRs.getString("date_time"), formatter));
                                            } catch(Exception ignored) {}
                                            
                                            g.addExpense(exp);
                                        }
                                    }
                                }

                                // manual settlements
                                try (PreparedStatement sSt = conn.prepareStatement("SELECT * FROM settlements WHERE group_id = ?")) {
                                    sSt.setInt(1, dbGroupId);
                                    try (ResultSet sRs = sSt.executeQuery()) {
                                        while (sRs.next()) {
                                            Settlement s = new Settlement(
                                                dbIdToUserMap.get(sRs.getInt("from_id")),
                                                dbIdToUserMap.get(sRs.getInt("to_id")),
                                                sRs.getDouble("amount")
                                            );
                                            if (sRs.getInt("settled") == 1) {
                                                s.markSettled(PaymentMethod.valueOf(sRs.getString("payment_method")));
                                            }
                                            try {
                                                java.lang.reflect.Field df = Settlement.class.getDeclaredField("dateTime");
                                                df.setAccessible(true);
                                                df.set(s, LocalDateTime.parse(sRs.getString("date_time"), formatter));
                                            } catch(Exception ignored) {}

                                            g.addSettlement(s);
                                        }
                                    }
                                }

                                ws.groups.add(g);
                            }
                        }
                    }
                }
            }
            return state;
        } catch (SQLException e) {
            System.err.println("Database empty or not initialized yet: " + e.getMessage());
            return null; // Signals we should use fallback
        }
    }
}
