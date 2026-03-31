package com.expense.gui;

import com.expense.model.*;
import com.expense.service.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SmartSplitApp extends JFrame {

    private List<Group> groups = new ArrayList<>();
    private ExpenseService service = new ExpenseService();
    private User loggedInUser;
    private int nextUserId = 1;
    private FileManager.AppState appState;
    private String currentUsername;

    private CardLayout mainCardLayout;
    private JPanel mainCardPanel;

    private JPanel sidebarGroupListPanel;
    private CardLayout contentCardLayout;
    private JPanel contentPanel;

    public SmartSplitApp() {
        UITheme.setupLookAndFeel();
        setTitle("SmartSplit - Expense Manager");
        setSize(1200, 750);
        setMinimumSize(new Dimension(950, 620));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);
        appState = FileManager.loadAppState();
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { persistCurrentUser(); }
        });

        mainCardLayout = new CardLayout();
        mainCardPanel = new JPanel(mainCardLayout);
        mainCardPanel.setBackground(UITheme.BG_DARK);
        add(mainCardPanel);

        buildLoginScreen();
        buildMainScreen();

        mainCardLayout.show(mainCardPanel, "login");

        AnimatedGlassPane glass = new AnimatedGlassPane();
        setGlassPane(glass);
        glass.setVisible(true);

        setVisible(true);
    }

    // ══════════════════════════════════ LOGIN ══════════════════════════════════
    private void buildLoginScreen() {
        UITheme.GradientPanel loginPanel = new UITheme.GradientPanel(
            new Color(0x0D1117), new Color(0x161B22));
        loginPanel.setLayout(new GridBagLayout());

        UITheme.CardPanel card = new UITheme.CardPanel(20, new Color(0x161B22), UITheme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(44, 52, 44, 52));
        card.setPreferredSize(new Dimension(440, 560));

        // Logo area
        JLabel emoji = new JLabel("SS", SwingConstants.CENTER);
        emoji.setFont(new Font("Segoe UI", Font.BOLD, 42));
        emoji.setForeground(UITheme.ACCENT);
        emoji.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("SmartSplit");
        title.setFont(UITheme.FONT_DISPLAY);
        title.setForeground(UITheme.ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Split smarter, settle faster.");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Fields
        JTextField userField   = UITheme.styledField("Username", 336);
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        userField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField passField = UITheme.styledPassField("Password", 336);
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = UITheme.pillButton("Sign In", UITheme.ACCENT, 336, 44);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel hint = new JLabel("Existing user: enter password | New user: account auto-created");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_DIM);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(emoji);
        card.add(Box.createVerticalStrut(8));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(28));
        card.add(sep);
        card.add(Box.createVerticalStrut(24));
        card.add(UITheme.sectionLabel("Username"));
        card.add(Box.createVerticalStrut(4));
        card.add(userField);
        card.add(Box.createVerticalStrut(14));
        card.add(UITheme.sectionLabel("Password"));
        card.add(Box.createVerticalStrut(4));
        card.add(passField);
        card.add(Box.createVerticalStrut(24));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(hint);

        loginPanel.add(card);

        Runnable doLogin = () -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            if (u.isEmpty() || p.isEmpty()) {
                UITheme.showThemedMessage(this, "Validation", "Username and password are required.", true);
                return;
            }
            if (!loginOrCreateAccount(u, p)) return;
            refreshSidebar();
            showHome();
            mainCardLayout.show(mainCardPanel, "main");
        };
        loginBtn.addActionListener(e -> doLogin.run());
        passField.addActionListener(e -> doLogin.run());

        mainCardPanel.add(loginPanel, "login");
    }

    // ══════════════════════════════════ MAIN ══════════════════════════════════
    private void buildMainScreen() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BG_DARK);

        main.add(buildSidebar(), BorderLayout.WEST);

        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(UITheme.BG_DARK);
        main.add(contentPanel, BorderLayout.CENTER);

        mainCardPanel.add(main, "main");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UITheme.BG_MEDIUM);
        sidebar.setPreferredSize(new Dimension(240, 0));

        // Top branding
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(22, 16, 12, 16));

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandRow.setOpaque(false);
        JLabel brandEmoji = new JLabel("SS");
        brandEmoji.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandEmoji.setForeground(UITheme.ACCENT);
        JLabel brand = new JLabel("SmartSplit");
        brand.setFont(UITheme.FONT_HEADING);
        brand.setForeground(UITheme.TEXT);
        brandRow.add(brandEmoji);
        brandRow.add(brand);
        brandRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(brandRow);
        top.add(Box.createVerticalStrut(18));

        JButton homeBtn = UITheme.sidebarNavBtn("", "Home");
        ((JButton)homeBtn).addActionListener(e -> { setAllSidebarInactive(); ((JButton)homeBtn).setSelected(true); showHome(); });
        homeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(homeBtn);
        top.add(Box.createVerticalStrut(6));
        top.add(UITheme.divider());
        top.add(Box.createVerticalStrut(8));

        JLabel groupsLbl = UITheme.sectionLabel("Groups");
        groupsLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(groupsLbl);
        top.add(Box.createVerticalStrut(4));

        sidebar.add(top, BorderLayout.NORTH);

        sidebarGroupListPanel = new JPanel();
        sidebarGroupListPanel.setOpaque(false);
        sidebarGroupListPanel.setLayout(new BoxLayout(sidebarGroupListPanel, BoxLayout.Y_AXIS));
        sidebarGroupListPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        JScrollPane scroll = UITheme.styledScroll(sidebarGroupListPanel);
        scroll.setBackground(UITheme.BG_MEDIUM);
        scroll.getViewport().setBackground(UITheme.BG_MEDIUM);
        sidebar.add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 20, 0));
        JButton newGroupBtn = UITheme.pillButton("New Group", UITheme.ACCENT, 200, 40);
        newGroupBtn.addActionListener(e -> showCreateGroupDialog());
        footer.add(newGroupBtn);
        JButton logoutBtn = UITheme.ghostButton("Log out", UITheme.TEXT_MUTED);
        logoutBtn.setPreferredSize(new Dimension(200, 34));
        logoutBtn.addActionListener(e -> logout());
        footer.add(logoutBtn);
        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private void setAllSidebarInactive() {
        for (Component c : sidebarGroupListPanel.getComponents()) {
            if (c instanceof JButton b) { b.setSelected(false); b.repaint(); }
        }
    }

    // ══════════════════════════════════ NAVIGATION ══════════════════════════════════
    void refreshSidebar() {
        sidebarGroupListPanel.removeAll();
        for (Group g : groups) {
            JButton btn = UITheme.sidebarNavBtn("", g.getName());
            btn.addActionListener(e -> { setAllSidebarInactive(); btn.setSelected(true); showGroup(g); });
            sidebarGroupListPanel.add(btn);
            sidebarGroupListPanel.add(Box.createVerticalStrut(2));
        }
        sidebarGroupListPanel.revalidate();
        sidebarGroupListPanel.repaint();
    }

    void showHome() {
        removePanel("home");
        HomePanel hp = new HomePanel(this, groups, service);
        hp.setName("home");
        contentPanel.add(hp, "home");
        contentCardLayout.show(contentPanel, "home");
    }

    void showGroup(Group g) {
        String key = "group_" + g.getId();
        removePanel(key);
        GroupPanel gp = new GroupPanel(this, g, service);
        gp.setName(key);
        contentPanel.add(gp, key);
        contentCardLayout.show(contentPanel, key);
    }

    private void removePanel(String name) {
        for (Component c : contentPanel.getComponents()) {
            if (name.equals(c.getName())) { contentPanel.remove(c); break; }
        }
    }

    // ══════════════════════════════════ CREATE GROUP DIALOG ══════════════════════════════════
    void showCreateGroupDialog() {
        CreateGroupDialog dlg = new CreateGroupDialog(this);
        dlg.setVisible(true);
        Group g = dlg.getResult();
        if (g != null) {
            groups.add(g);
            persistCurrentUser();
            refreshSidebar();
            showGroup(g);
        }
    }

    void showProfileDialog() {
        JDialog dlg = new JDialog(this, "Profile Settings", true);
        dlg.getContentPane().setBackground(UITheme.BG_MEDIUM);
        dlg.setSize(460, 440);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        JLabel title = new JLabel("Configure Profile");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = UITheme.styledField("Display name", 390);
        nameField.setText(loggedInUser.getName());
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField photoPath = UITheme.styledField("Optional profile photo path", 390);
        photoPath.setText(loggedInUser.getProfileImagePath() == null ? "" : loggedInUser.getProfileImagePath());
        photoPath.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        photoPath.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton browse = UITheme.ghostButton("Choose Photo", UITheme.ACCENT);
        browse.setPreferredSize(new Dimension(120, 34));
        browse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "webp"));
            if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                photoPath.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JComboBox<com.expense.model.Currency> curBox = UITheme.styledCombo(com.expense.model.Currency.values());
        FileManager.UserWorkspace wss = appState.users.get(currentUsername);
        if (wss != null && wss.currency != null) curBox.setSelectedItem(wss.currency);
        curBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JButton save = UITheme.pillButton("Save Profile", UITheme.ACCENT_GREEN, 180, 40);
        save.addActionListener(e -> {
            String nm = nameField.getText().trim();
            if (nm.isEmpty()) {
                UITheme.showThemedMessage(dlg, "Validation Error", "Name cannot be empty.", true);
                return;
            }
            loggedInUser.setName(nm);
            loggedInUser.setProfileImagePath(photoPath.getText().trim().isEmpty() ? null : photoPath.getText().trim());
            FileManager.UserWorkspace ws = appState.users.get(currentUsername);
            if (ws != null) {
                ws.currency = (com.expense.model.Currency) curBox.getSelectedItem();
                UITheme.CURRENCY_SYMBOL = ws.currency.getSymbol();
            }
            persistCurrentUser();
            dlg.dispose();
            showHome();
            UITheme.showThemedMessage(this, "Profile Updated", "Your profile is updated successfully.", false);
        });

        root.add(title);
        root.add(Box.createVerticalStrut(14));
        root.add(UITheme.sectionLabel("Display Name"));
        root.add(Box.createVerticalStrut(4));
        root.add(nameField);
        root.add(Box.createVerticalStrut(10));
        root.add(UITheme.sectionLabel("Preferred Currency"));
        root.add(Box.createVerticalStrut(4));
        root.add(curBox);
        root.add(Box.createVerticalStrut(10));
        root.add(UITheme.sectionLabel("Profile Photo (Optional)"));
        root.add(Box.createVerticalStrut(4));
        root.add(photoPath);
        root.add(Box.createVerticalStrut(8));
        root.add(browse);
        root.add(Box.createVerticalStrut(20));
        root.add(save);
        
        dlg.add(root);
        dlg.setVisible(true);


    }

    // ══════════════════════════════════ SAMPLE DATA ══════════════════════════════════
    private void loadSampleData() {
        User u1 = loggedInUser;
        User u2 = new User(nextUserId++, "Rahul");
        User u3 = new User(nextUserId++, "Priya");
        User u4 = new User(nextUserId++, "Amit");

        // Flat / Roommates
        Group flat = new Group("Flat 402", GroupType.ROOMMATES);
        flat.addMember(u1); flat.addMember(u2); flat.addMember(u3);
        flat.setBudget(30000);
        addEqually(flat, "Monthly Rent",    15000, Category.RENT,      u1, "March rent",    u1, u2, u3);
        addEqually(flat, "Electricity",      1500, Category.UTILITIES, u2, "March bill",     u1, u2, u3);
        addEqually(flat, "Weekly Groceries", 2400, Category.GROCERIES, u3, "D-Mart run",     u1, u2, u3);
        groups.add(flat);

        // Office
        Group office = new Group("Tech Team", GroupType.OFFICE);
        office.addMember(u1); office.addMember(u2); office.addMember(u4);
        addEqually(office, "Friday Lunch",   1800, Category.FOOD,  u1, "Biryani",        u1, u2, u4);
        addEqually(office, "Birthday Party", 1500, Category.PARTY, u4, "Rohan's bday",   u1, u2, u4);
        groups.add(office);

        // Travel
        Group trip = new Group("Goa Trip", GroupType.TRAVEL);
        trip.addMember(u1); trip.addMember(u2); trip.addMember(u3); trip.addMember(u4);
        addEqually(trip, "Hotel Stay",    8000, Category.TRAVEL, u1, "2 nights",     u1, u2, u3, u4);
        addEqually(trip, "Beach Dinner",  3000, Category.FOOD,   u3, "Seafood",      u1, u2, u3, u4);
        addEqually(trip, "Taxi & Rides",  2000, Category.TRANSPORT, u2, "All rides", u1, u2, u3, u4);
        groups.add(trip);
        persistCurrentUser();
    }

    private void addEqually(Group g, String title, double amount, Category cat, User paidBy, String note, User... members) {
        double each = amount / members.length;
        Map<User, Double> split = new LinkedHashMap<>();
        for (User m : members) split.put(m, each);
        g.addExpense(new Expense(title, amount, cat, paidBy, split, note));
    }

    public List<Group> getGroups()    { return groups; }
    public ExpenseService getService() { return service; }
    public User getLoggedInUser()      { return loggedInUser; }
    public int getNextUserId()         { return nextUserId++; }

    void persistCurrentUser() {
        if (currentUsername == null || loggedInUser == null) return;
        FileManager.UserWorkspace ws = appState.users.get(currentUsername);
        if (ws == null) return;
        ws.profile = loggedInUser;
        ws.groups = groups;
        ws.nextUserId = nextUserId;
        FileManager.saveAppState(appState);
    }

    private boolean loginOrCreateAccount(String username, String password) {
        FileManager.UserWorkspace ws = appState.users.get(username);
        if (ws == null) {
            ws = new FileManager.UserWorkspace();
            ws.username = username;
            ws.password = password;
            ws.profile = new User(1, username);
            ws.nextUserId = 2;
            appState.users.put(username, ws);
        } else if (!Objects.equals(ws.password, password)) {
            UITheme.showThemedMessage(this, "Invalid login", "Password does not match this user.", true);
            return false;
        }
        this.currentUsername = username;
        this.loggedInUser = ws.profile;
        this.groups = ws.groups != null ? ws.groups : new ArrayList<>();
        this.nextUserId = Math.max(2, ws.nextUserId);
        UITheme.CURRENCY_SYMBOL = ws.currency != null ? ws.currency.getSymbol() : "₹";
        if (groups.isEmpty()) {
            loadSampleData();
        }
        persistCurrentUser();
        return true;
    }

    private void logout() {
        persistCurrentUser();
        currentUsername = null;
        loggedInUser = null;
        groups = new ArrayList<>();
        nextUserId = 1;
        contentPanel.removeAll();
        mainCardLayout.show(mainCardPanel, "login");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartSplitApp::new);
    }
}
