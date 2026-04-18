# 🚀 SmartSplit: Team Developer Guide

Welcome to the **SmartSplit** developer guide! This document explains how our Java mini-project works internally, how we built it, and how the different components talk to each other. 

Use this guide to understand the codebase and to prepare for questions from the faculty.

---

## 1. How Does the Project Work?

SmartSplit is an Expense Sharing desktop application. It allows users to:
1. Create groups (e.g., "Goa Trip" or "Flatmates").
2. Log expenses and state who paid and how it should be split.
3. Automatically calculate the **minimum number of transactions** (Debt Simplification) required to settle all debts.
4. Visualize spending through analytics (Charts and Graphs).
5. (Newly Added!) Save all this data into an **SQLite Relational Database** using JDBC.

### The 3-Tier Architecture
Our project follows a standard MVC-inspired 3-tier architecture:

1. **Presentation Layer (`com.expense.gui`)**
   - The UI is entirely built using **Java Swing**.
   - `SmartSplitApp.java`: The main window containing the `CardLayout` that switches between the Login screen and Main dashboard.
   
   - `GroupPanel.java`: The largest component. It holds tabs for Expenses, Balances, Analytics, History, and Lifestyle.
   - `UITheme.java`: We built our own design library! All buttons, inputs, themes, and colors pull from this file so the app looks consistent and modern.

2. **Business Logic Layer (`com.expense.service`)**
   - `ExpenseService.java`: The brain of the app. It calculates category totals, balances, and runs the Greedy Algorithm for settling debts.
   - `DatabaseManager.java` & `DBConnection.java`: (New!) Handles connecting to our database and executing SQL queries to persist user data.
   - `FileManager.java`: The original persistence layer (now acts as a fallback and bridge).

3. **Data Layer (`com.expense.model`)**
   - Pure Java Objects (POJOs) like `User`, `Expense`, `Group`, and `Settlement`.
   - Used to store data in memory while the application is actively running.

---

## 2. How We Made It

### A. The User Interface (Swing Magic)
We did not use any external UI libraries. To make it look modern:
- We avoided default borders and colors.
- We used `Graphics2D` alongside `RadialGradientPaint` to draw the animated background blobs and mouse-tracking spotlight (`AnimatedGlassPane.java`).
- We built custom chart drawing logic for the Pie Charts and Bar Charts instead of relying on JFreeChart.

### B. The Core Logic (Debt Simplification)
When calculating who owes whom, if **A owes B ₹500** and **B owes C ₹500**, A should just pay C directly. 
We wrote an algorithm in `ExpenseService.simplifyDebts()` that:
1. Calculates every user's **Net Balance** (Total amount they are owed MINUS the total amount they owe).
2. Separates users into **Creditors** (positive balance) and **Debtors** (negative balance).
3. Matches the largest debtor to the largest creditor, eliminating debts iteratively until everyone's balance is 0.

---

## 3. The New JDBC Database Integration

**Initially**, the app used Java Object Serialization (`ObjectOutputStream`) to dump all data into a flat `smartsplit-data.bin` file.

**Now**, we have upgraded this to use **JDBC with a relational SQLite Database** (`smartsplit.db`), fulfilling the database connectivity requirement for the project!

### How we added it:
1. **Added the SQLite Driver (`sqlite-jdbc.jar`)**
   We placed this `.jar` file inside a new `lib/` folder and updated `SmartSplit.bat` to include it in the classpath (`-cp "out\production\ExpenseSharingSystem;lib\*"`).

2. **`DBConnection.java`**
   Provides a `getConnection()` method and a `initializeDatabase()` method. 
   On startup, it executes standard `CREATE TABLE IF NOT EXISTS` commands for 6 main tables:
   - `users`, `groups`, `group_members`, `expenses`, `expense_splits`, `settlements`.

3. **`DatabaseManager.java`**
   - **Saving:** When you exit or save, `DatabaseManager.saveAppState()` begins a transaction, clears the old tables, and writes all your group data, expenses, and users recursively into the relational tables using `PreparedStatement`.
   - **Loading:** On application launch, `DatabaseManager.loadAppState()` runs `SELECT *` from all the tables and completely reconstructs the `AppState` object graph with object references perfectly matched.

4. **Fallback Mechanism**
   If the database is not found or empty (like on a fresh install), `FileManager` will automatically try migrating data from the old `smartsplit-data.bin` to the database, ensuring zero data loss.

---

## 4. Running the Project

To compile and launch the project cleanly, simply double-click the **`SmartSplit.bat`** file. 

If you prefer using the terminal (to read logs carefully):
```powershell
# Compile the latest code:
javac -cp "lib\*" -d "out\production\ExpenseSharingSystem" src\com\expense\model\*.java src\com\expense\service\*.java src\com\expense\gui\*.java

# Run it:
java -cp "out\production\ExpenseSharingSystem;lib\*" com.expense.gui.SmartSplitApp
```

### Team Tips for Faculty Review
- **Emphasize the architecture:** Mention we separated logic (service) from UI (gui).
- **Emphasize the database:** Show them the `smartsplit.db` file. Take them through `DatabaseManager.java` to prove you understand SQL `PreparedStatement` and JDBC.
- **Show off the UI:** The animated background and custom charts will definitely secure extra marks!
