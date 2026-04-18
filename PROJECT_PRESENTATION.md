# SmartSplit — Expense Sharing System
### Complete Project Presentation Guide for Faculty Review

---

## 1. PROJECT OVERVIEW

**Project Name:** SmartSplit — Advanced Expense Manager  
**Language:** Java (JDK 17+, tested on JDK 25)  
**UI Framework:** Java Swing (No external libraries)  
**Type:** Desktop GUI Application  
**Category:** Expense Sharing / Personal Finance Management  
**Persistence:** Binary file serialization (`smartsplit-data.bin`)

### What does SmartSplit do?
SmartSplit is a **desktop application** that helps groups of people (friends, roommates, colleagues, travellers) track shared expenses and automatically calculate who owes whom — and by exactly how much. It also lets users record settlements with multiple payment methods.

---

## 2. OBJECTIVES OF THE PROJECT

1. **Track group expenses** across multiple groups with different types (Travel, Roommates, Office, etc.)
2. **Automatically calculate balances** — who owes whom after all expenses are logged.
3. **Minimize the number of transactions** needed to settle all debts (debt simplification algorithm).
4. **Provide visual analytics** — pie chart (category-wise), bar chart (person-wise), trend chart (date-wise).
5. **Support multiple currencies** — INR, USD, EUR, GBP, JPY, AUD.
6. **Persist data** between sessions using Java Object Serialization.
7. **Deliver a premium UI** — glassmorphic cards, animated background, hover effects, animated list entries.

---

## 3. PROJECT STRUCTURE & ARCHITECTURE

### 3.1 Folder Structure

```
ExpenseSharingSystem/
│
├── src/
│   ├── Main.java                        ← (Placeholder, not used)
│   └── com/expense/
│       ├── model/                       ← Data Layer (POJOs / Entities)
│       │   ├── User.java
│       │   ├── Group.java
│       │   ├── Expense.java
│       │   ├── Settlement.java
│       │   ├── Category.java            ← Enum
│       │   ├── Currency.java            ← Enum
│       │   ├── GroupType.java           ← Enum
│       │   ├── PaymentMethod.java       ← Enum
│       │   └── UserRole.java            ← Enum
│       ├── service/                     ← Business Logic Layer
│       │   ├── ExpenseService.java
│       │   ├── FileManager.java
│       │   └── PaymentGatewayService.java
│       └── gui/                         ← Presentation Layer
│           ├── SmartSplitApp.java       ← Main JFrame / Entry Point
│           ├── HomePanel.java
│           ├── GroupPanel.java          ← Largest file (1682 lines)
│           ├── AddExpenseDialog.java
│           ├── CreateGroupDialog.java
│           ├── AnimatedGlassPane.java
│           └── UITheme.java             ← Centralized Design System
│
├── out/                                 ← Compiled .class files
├── smartsplit-data.bin                  ← Persistent data store
├── SmartSplit.bat                       ← One-click build + run script
└── README.md
```

### 3.2 Architecture Pattern: 3-Tier (MVC-Inspired)

```
┌──────────────────────────────────────┐
│         PRESENTATION LAYER           │
│  (gui package — Swing UI components) │
│  SmartSplitApp, GroupPanel, UITheme  │
└──────────────┬───────────────────────┘
               │ calls
┌──────────────▼───────────────────────┐
│         BUSINESS LOGIC LAYER         │
│   (service package)                  │
│  ExpenseService, PaymentGateway,     │
│  FileManager                         │
└──────────────┬───────────────────────┘
               │ reads/writes
┌──────────────▼───────────────────────┐
│           DATA LAYER                 │
│   (model package)                    │
│  User, Group, Expense, Settlement... │
│  Persisted via smartsplit-data.bin   │
└──────────────────────────────────────┘
```

---

## 4. DATA MODEL (Model Classes in Detail)

### 4.1 `User.java`
Represents a person (member) in a group.

| Field | Type | Description |
|---|---|---|
| `id` | `int` | Unique identifier, auto-incremented |
| `name` | `String` | Display name of the user |
| `profileColor` | `Color` | Auto-assigned color from a palette (10 colors) |
| `profileImagePath` | `String` | Optional path to a profile photo |

- Implements `Serializable` for persistence.
- `getInitial()` — returns first character for avatar display.
- `equals()` and `hashCode()` based on `id` for correct `Map`/`Set` behavior.

### 4.2 `Group.java`
Represents a group of users sharing expenses.

| Field | Type | Description |
|---|---|---|
| `id` | `int` | Auto-incremented using static counter |
| `name` | `String` | Group name (e.g., "Goa Trip") |
| `type` | `GroupType` | Enum — ROOMMATES, TRAVEL, OFFICE, etc. |
| `members` | `List<User>` | List of group members |
| `expenses` | `List<Expense>` | All expenses in the group |
| `manualSettlements` | `List<Settlement>` | Manually recorded settlements |
| `budget` | `double` | Optional spending limit |
| `roles` | `Map<User, UserRole>` | Role per user (Owner/Editor/Viewer) |

- `getTotalSpent()` — streams all expenses and sums amounts.

### 4.3 `Expense.java`
Represents a single expense entry.

| Field | Type | Description |
|---|---|---|
| `id` | `int` | Auto-incremented |
| `title` | `String` | Description (e.g., "Hotel Stay") |
| `amount` | `double` | Total amount paid |
| `category` | `Category` | Enum — FOOD, RENT, TRAVEL, etc. |
| `paidBy` | `User` | Person who paid |
| `split` | `Map<User, Double>` | How much each person owes |
| `dateTime` | `LocalDateTime` | Timestamp of entry |
| `note` | `String` | Optional remark |
| `receiptImagePath` | `String` | Optional path to receipt image |

### 4.4 `Settlement.java`
Tracks a debt payment between two people.

| Field | Type | Description |
|---|---|---|
| `from` | `User` | Person paying |
| `to` | `User` | Person receiving |
| `amount` | `double` | Amount paid |
| `settled` | `boolean` | Whether it's been marked as paid |
| `paymentMethod` | `PaymentMethod` | CASH / UPI / CARD / etc. |
| `dateTime` | `LocalDateTime` | When recorded |

### 4.5 Enums

| Enum | Values | Purpose |
|---|---|---|
| `Category` | FOOD, TRAVEL, RENT, UTILITIES, GROCERIES, ENTERTAINMENT, SHOPPING, TRANSPORT, MEDICAL, GIFT, PARTY, SUBSCRIPTION, OTHER | Expense category labels |
| `Currency` | INR, USD, EUR, GBP, JPY, AUD | Multi-currency support with symbols |
| `GroupType` | GENERAL, ROOMMATES, OFFICE, TRAVEL, COUPLE, EVENT, STUDENTS | Context-aware groups with quick actions |
| `PaymentMethod` | CASH, UPI, CREDIT_CARD, DEBIT_CARD, NET_BANKING | Settlement payment modes |
| `UserRole` | OWNER, EDITOR, VIEWER | Role-based access per group |

---

## 5. BUSINESS LOGIC (Service Layer)

### 5.1 `ExpenseService.java` — Core Calculation Engine

#### Method: `computeBalances(Group group)`
Calculates a **nested Map** of who owes whom for each expense.

```
For each expense:
   For each participant in split:
      If participant ≠ paidBy:
         participant owes paidBy their share amount
```

Returns: `Map<User, Map<User, Double>>` — "User A owes User B: ₹500"

#### Method: `simplifyDebts(Group group)` ⭐ *Most Important Algorithm*
Implements the **Greedy Debt Simplification Algorithm** to minimize the number of transactions needed to settle all debts.

**Algorithm Explained (Step-by-Step):**
1. Compute net balance for each user (positives = owed money, negatives = owes money).
2. Separate users into **creditors** (net positive) and **debtors** (net negative).
3. Use a greedy two-pointer approach: match the largest debtor with the largest creditor.
4. Generate `Settlement` objects for minimum transactions.

**Example:**
```
A owes B: ₹500
C owes B: ₹300
A owes D: ₹200

Without simplification: 3 transactions
After simplification:
  Net: A = -700, C = -300, B = +800, D = +200
  → A pays B: ₹700 (covers both debts)
  → C pays B: ₹100, C pays D: ₹200
  Total: 3 transactions (but amounts are optimized)
```

This is directly inspired by the **minimum cash flow problem** in graph theory.

#### Other Methods:
- `getCategoryTotals(Group)` — returns `Map<Category, Double>` for pie chart
- `getUserSpending(Group)` — returns `Map<User, Double>` for bar chart
- `getUserOwes(Group, User)` — total amount a user owes across all debts
- `getUserIsOwed(Group, User)` — total amount a user is owed

### 5.2 `FileManager.java` — Persistence Layer

Uses **Java Object Serialization** (`ObjectInputStream` / `ObjectOutputStream`) to save and load the entire application state.

#### Inner Classes:
- `UserWorkspace` — holds one user's username, password, profile, list of groups, preferred currency.
- `AppState` — holds a `Map<String, UserWorkspace>` — multiple user accounts in one file.

#### Key Methods:
- `saveAppState(AppState)` — serializes the entire state to `smartsplit-data.bin`
- `loadAppState()` — deserializes on startup
- `exportGroupToCSV(Group, filename)` — exports expenses to a CSV file
- `generateReport(Group, ExpenseService)` — generates a formatted text report

**Why Binary File?** Binary serialization preserves full Java object graphs (including nested `List`, `Map`, and custom objects) with a single method call — simpler than JSON or XML for this assignment.

### 5.3 `PaymentGatewayService.java` — Payment Integration

Handles real payment initiation:
- **UPI:** Constructs a `upi://pay?pa=...` deep-link and launches it via `Desktop.browse()`, opening the user's UPI app.
- **Card/Net Banking:** Opens a hosted checkout URL (configured via `SMARTSPLIT_CHECKOUT_URL` environment variable) in the browser.
- **Cash:** Records immediately without launching anything.

---

## 6. GRAPHICAL USER INTERFACE (GUI Layer)

### 6.1 `SmartSplitApp.java` — Main Application Window (JFrame)

- Extends `JFrame` — the primary window.
- Uses `CardLayout` for switching between **Login Screen** and **Main Screen** without opening multiple windows.
- **Login / Registration:** A single screen handles both — if the username exists, it validates the password; if not, a new account is auto-created (no separate "Sign Up" flow).
- On login success, loads sample data (3 demo groups) if the user has no groups yet.
- Builds a **sidebar** (left panel) with a list of all groups, "New Group" button, and "Log out" button.
- Uses another `CardLayout` for the content area — each group panel is swapped in/out.

### 6.2 `HomePanel.java` — Dashboard

Shows:
- **4 Stat Cards:** Total I owe, Total owed to me, Total spent across all groups, Number of groups.
- **Tip Banner** explaining debt simplification is active.
- **Group Cards Grid** — clickable cards for each group, showing type badges, member avatars, total spent, and budget progress bar (if set).
- Group cards have hover lift animation and click navigation.

### 6.3 `GroupPanel.java` — Group Detail View (1682 lines)

The most complex class. It contains a `JTabbedPane` with **5 tabs:**

#### Tab 1: Expenses
- Lists all expenses in reverse chronological order (newest first).
- Each row shows: category icon circle, title + category badge + "paid by" + note + "View Bill" button (if receipt attached).
- Action buttons: **Add Expense, Settle with Person, Add Member, Members, Set Budget.**
- **Staggered slide-in animation** — each expense row slides in with a small delay (40ms per row) creating a cascade effect.

#### Tab 2: Balances
- Calls `ExpenseService.simplifyDebts()` and shows each settlement as a row: `Avatar → Arrow → Avatar + Amount + PAID/DEBT badge + Mark Paid button`.
- **Mark Paid button** opens the Payment Gateway dialog (select method, record).
- Settlement row turns green upon marking paid.
- **"Mark All Settled"** button settles everything at once with a pulse animation.

#### Tab 3: Analytics
A 2×2 grid of charts, all custom-drawn using `Graphics2D`:
- **Pie Chart (Donut):** Category-wise spending breakdown with a legend and percentage labels.
- **Bar Chart:** Per-person spending with gradient bars and avatar color dots.
- **Monthly Summary Cards:** Avg expense, Top spender, Top category, Total spend.
- **Trend Chart:** Spending by date — line graph showing spending movement over time.

#### Tab 4: History
- **JTable** listing all expenses and manual settlements combined.
- Columns: Type, Title, Amount, Category/Method, Paid By, Date, Note.
- **Double-click a row** to view the receipt image (if attached).
- **Export CSV** button — generates a `.csv` file.
- **View Report** button — shows a formatted text summary.

#### Tab 5: Lifestyle Tab (Group-Type Specific)
- Tab label = the group type (e.g., "Roommates", "Travel / Trip").
- Shows context-aware **Quick Action buttons** (e.g., "Split Rent", "Hotel Bill", "Gift Collection").
- Shows **contextual insight cards** with summaries relevant to the group type (Rent breakdown for Roommates, Trip summary for Travel, etc.).

### 6.4 `AddExpenseDialog.java` — Add Expense Dialog
A modal `JDialog` that collects:
- **Title** — expense description.
- **Amount** — with live validation (numbers only).
- **Category** — dropdown with suggested categories for this group type listed first.
- **Paid By** — dropdown of group members.
- **Note** — optional remark.
- **Receipt Photo** — browse for image, path stored in expense.
- **Split Mode** — checkbox: "Split equally among all members" (checked by default). If unchecked, prompts for custom amount per person.

### 6.5 `CreateGroupDialog.java` — New Group Dialog
Collects:
- Group Name.
- Group Type (dropdown with descriptions for each type).
- Optional Budget.
- Adds the logged-in user as the first member (Owner role).

### 6.6 `AnimatedGlassPane.java` — Background Animation

A transparent `JComponent` placed as the **glass pane** (overlay on the entire window).

- Tracks mouse position using a global AWT event listener.
- Runs a `javax.swing.Timer` at ~30fps (33ms interval).
- On each tick:
  1. Draws two slowly **orbiting color orbs** (using `RadialGradientPaint`) that drift around in sine/cosine paths.
  2. Draws a **mouse-following spotlight** — a soft white radial gradient centered on cursor (radius 300px, 15% opacity).
- The orb color changes based on the selected currency (e.g., green for USD, purple for GBP).
- The `contains()` method returns `false` so all mouse events pass through to the actual UI below.

### 6.7 `UITheme.java` — Centralized Design System (~800 lines)

Acts as a **design token library and component factory.** All colors, fonts, and component builders are defined here.

#### Key Design Tokens:
```java
BG_DARK   = #0D1117  (primary background)
BG_MEDIUM = #161B22  (sidebar, dialogs)
BG_CARD   = #21262D  (expense/settlement cards)
ACCENT    = #58A6FF  (primary blue)
ACCENT_GREEN  = #3FB950
ACCENT_PURPLE = #BC8CFF
ACCENT_YELLOW = #E3B341
DANGER    = #F78166  (debt amounts)
TEXT      = #E6EDF3
TEXT_MUTED = #8B949E
```

#### Component Factories (methods that create styled components):
- `pillButton(text, color, width, height)` — rounded pill-shaped button with hover color shift.
- `ghostButton(text, color)` — transparent button with border.
- `styledField(placeholder, width)` — dark text field with placeholder and focus highlight.
- `styledCombo(array)` — styled dropdown.
- `statCard(label, value, color, bg)` — stat card panel.
- `avatar(user, size)` — circular avatar with initials or profile image.
- `badge(text, fg, bg)` — small pill-shaped label.
- `progressBar(pct, color, height)` — custom progress bar.
- `styledScroll(panel)` — styled `JScrollPane` with thin scrollbar.
- `sectionLabel(text)` — uppercase muted label.
- `attachHoverLift(panel, pixels)` — adds a subtle upward shift on hover via `Border` change.
- `showThemedMessage(parent, title, msg, isError)` — styled `JOptionPane` dialog.
- `getCategoryEmoji(category)` — maps each category to an emoji string.
- `getGroupEmoji(type)` — maps each group type to an emoji.
- `CURRENCY_SYMBOL` — globally mutable field updated when user changes currency preference.

---

## 7. OOP CONCEPTS APPLIED

| Concept | Where & How |
|---|---|
| **Classes & Objects** | All model classes (User, Group, Expense, Settlement) are objects with encapsulated state and behavior. |
| **Inheritance** | `SmartSplitApp extends JFrame`, `GroupPanel extends JPanel`, `HomePanel extends JPanel`, `AnimatedGlassPane extends JComponent`. Inner classes like `PieChartPanel extends JPanel`. |
| **Encapsulation** | All model class fields are `private` with public getters/setters. Business logic is isolated in service classes. |
| **Polymorphism** | `paintComponent(Graphics g)` is overridden in `PieChartPanel`, `BarChartPanel`, `TrendChartPanel`, `AnimatedGlassPane` — each draws differently. |
| **Abstraction** | `ExpenseService` abstracts balance computation; callers don't need to know the algorithm internals. |
| **Interfaces** | `Serializable` implemented by all model classes for persistence. `ActionListener`, `MouseAdapter` (anonymous classes / lambdas) used for event handling. |
| **Enums** | `Category`, `Currency`, `GroupType`, `PaymentMethod`, `UserRole` — type-safe constants with additional fields and methods. |
| **Generics** | `List<Group>`, `Map<User, Double>`, `Map<Category, Double>`, `JComboBox<Currency>` — type-safe collections. |
| **Lambda Expressions** | Used extensively: `addActionListener(e -> ...)`, `stream().mapToDouble(...)`, `Comparator.comparing(...)` |
| **Streams (Java 8+)** | `getTotalSpent()` uses `expenses.stream().mapToDouble(...).sum()`, analytic methods use stream operations. |
| **Serialization** | `ObjectOutputStream`/`ObjectInputStream` to persist the full object graph to binary file. |
| **Anonymous Inner Classes** | `MouseAdapter`, `DefaultTableModel`, `DefaultListCellRenderer`, `ReflectiveCardPanel` (inner class in UITheme). |
| **Static Members** | Auto-increment counters in `User`, `Group`, `Expense`; `UITheme.CURRENCY_SYMBOL` as a mutable global. |

---

## 8. KEY FEATURES — SUMMARY TABLE

| Feature | Description |
|---|---|
| **User Authentication** | Login with username + password; auto-creates account if user is new. Multi-user accounts stored in one file. |
| **Multiple Groups** | Create unlimited groups, each with a type (Roommates, Travel, Office, etc.) |
| **Add Expense** | Log expense with title, amount, category, paid-by, note, optional receipt image. Equal or custom split. |
| **Debt Simplification** | Greedy algorithm computes minimum transactions needed to settle all debts. |
| **Settlement Recording** | Mark individual or all debts as paid; choose payment method (Cash, UPI, Card, etc.) |
| **UPI Payment Launch** | Generates `upi://pay` deep-link and opens the phone's UPI app. |
| **Analytics Charts** | Donut pie chart, bar chart, trend line chart — all custom-drawn with Java 2D API. |
| **Monthly Summary** | Avg expense, top spender, top category, total spent. |
| **Budget Tracking** | Set a budget per group; progress bar shows how close to limit. |
| **History Table** | Combined view of expenses + settlements in a JTable with CSV export. |
| **Report Generation** | Text-format group report with all expenses and settlements. |
| **Receipt Attachment** | Attach image file to any expense; view it later via "View Bill" button. |
| **Member Management** | Add members, assign roles (Owner/Editor/Viewer), view avatar overlays. |
| **Multi-Currency** | Change currency symbol globally; glass pane orb changes color to match. |
| **Profile Settings** | Edit display name, upload profile photo, change currency. |
| **Sample Data** | First-time users get 3 demo groups (Flat 402, Tech Team, Goa Trip) pre-populated. |
| **Animated UI** | Glass pane with orbiting color blobs + mouse spotlight; tab fade-in; staggered list slide-in; pulse on settle. |
| **Data Persistence** | All data saved to `smartsplit-data.bin` via Java Object Serialization on every change. |
| **Lifestyle Tab** | Context-sensitive tab per group type with quick-action buttons and relevant tips. |

---

## 9. DATA FLOW — HOW IT ALL CONNECTS

```
User launches app
      │
      ▼
FileManager.loadAppState()  ──→  reads smartsplit-data.bin
      │
      ▼
SmartSplitApp (Login Screen)
      │  (username + password entered)
      ▼
loginOrCreateAccount()
   - New user → creates UserWorkspace
   - Existing user → validates password
   - Loads user's groups from workspace
   - Calls loadSampleData() if no groups
      │
      ▼
MainScreen (sidebar + HomePanel)
      │  (clicks a group)
      ▼
GroupPanel displayed
      │
      ├──[Add Expense]──→ AddExpenseDialog
      │                    → creates Expense object
      │                    → group.addExpense(expense)
      │                    → persistCurrentUser() → saves to .bin
      │
      ├──[Balances Tab]──→ ExpenseService.simplifyDebts(group)
      │                    → displays Settlement rows
      │                    → [Mark Paid] → PaymentGatewayService
      │                                  → s.markSettled(method)
      │                                  → persistCurrentUser()
      │
      ├──[Analytics Tab]──→ ExpenseService.getCategoryTotals()
      │                    → PieChartPanel.paintComponent()
      │                    → ExpenseService.getUserSpending()
      │                    → BarChartPanel.paintComponent()
      │
      └──[History Tab]──→ FileManager.exportGroupToCSV()
                         → FileManager.generateReport()
```

---

## 10. HOW TO RUN THE PROJECT

### Prerequisites
- **Java JDK 17 or higher** installed on Windows.
- The `JAVA_HOME` path in `SmartSplit.bat` must match your JDK installation.

### Steps
1. Open the project folder in File Explorer.
2. Double-click `SmartSplit.bat`.
3. The script:
   - Calls `javac` to compile all `.java` files.
   - Outputs `.class` files to `out/production/ExpenseSharingSystem/`.
   - Runs `com.expense.gui.SmartSplitApp` as the entry point.

### Manual Compilation (Alternative)
```powershell
javac -d out\production\ExpenseSharingSystem src\com\expense\model\*.java src\com\expense\service\*.java src\com\expense\gui\*.java
java -cp out\production\ExpenseSharingSystem com.expense.gui.SmartSplitApp
```

---

## 11. DESIGN DECISIONS & WHY

| Decision | Reason |
|---|---|
| **No database** | Project scope is desktop-only; Java serialization is simpler and sufficient. |
| **No external libraries** | Demonstrates core Java capabilities; avoids dependency issues. |
| **CardLayout for navigation** | Avoids opening multiple windows; keeps state in one JFrame. |
| **Greedy algorithm for debt simplification** | Simple, optimal for small group sizes; runs in O(n log n). |
| **Static UITheme** | Centralizing all styling prevents inconsistency and makes theme changes easy. |
| **Serializable model classes** | Enables one-shot serialization of the entire object graph without manual mapping. |
| **GroupType enum with metadata** | Makes each enum self-describing (quick actions, categories) — avoids large switch-case blocks scattered throughout code. |
| **GlassPane for animations** | The glass pane is the only correct way to draw overlays on a JFrame without interfering with component layout. |

---

## 12. ANTICIPATED FACULTY QUESTIONS (Q&A)

**Q1: What is the entry point of the application?**  
A: `SmartSplitApp.java` in `com.expense.gui`. Its `main()` method calls `SwingUtilities.invokeLater(SmartSplitApp::new)` which is the standard safe way to launch Swing applications on the **Event Dispatch Thread (EDT)**.

**Q2: Why use `SwingUtilities.invokeLater()`?**  
A: Swing is **not thread-safe**. All Swing component creation and modification must happen on the EDT. `invokeLater` ensures this.

**Q3: What is `CardLayout` and why is it used?**  
A: `CardLayout` is a layout manager that stacks multiple panels and shows one at a time (like deck of cards). It's used to switch between the Login screen and Main screen, and between different group panels — without opening new windows.

**Q4: Explain the debt simplification algorithm.**  
A: It uses a greedy approach: compute each person's net balance (total paid minus total owed). Separate into creditors (positive net) and debtors (negative net). Iteratively match debtors with creditors, settling the minimum of both amounts at each step. This minimizes the number of payment transactions.

**Q5: How is data persisted?**  
A: Via Java Object Serialization. All model classes implement the `Serializable` interface. The `FileManager` class uses `ObjectOutputStream` to write the entire `AppState` object (which contains all user accounts and group data) to `smartsplit-data.bin`. On startup, `ObjectInputStream` reads it back.

**Q6: What is `serialVersionUID`?**  
A: It's a version control ID for serialized objects. If the class structure changes, this ID helps Java detect incompatible versions. All model classes declare it explicitly.

**Q7: How does the animated glass pane work?**  
A: `AnimatedGlassPane` extends `JComponent` and is set as the JFrame's glass pane (transparent overlay). It overrides `contains()` to return `false` so it never intercepts mouse events. A `javax.swing.Timer` fires every 33ms to repaint; the `paintComponent()` draws two colour orbs (using `RadialGradientPaint` with sine/cosine orbit math) and a mouse-tracking spotlight.

**Q8: How does the pie chart work (no library used)?**  
A: `PieChartPanel` extends `JPanel` and overrides `paintComponent(Graphics g)`. It uses `Graphics2D.fillArc()` to draw each slice, calculating the arc angle as `(category amount / total) × 360`. A smaller inner circle is drawn on top to create the donut hole. A legend is drawn using `fillRoundRect` color patches and text.

**Q9: What are the 5 tabs in the group view?**  
A: Expenses (add/view), Balances (simplified debts/settle), Analytics (4 charts), History (table + export), and a Lifestyle tab (context-aware quick actions for the group type).

**Q10: How is multi-currency handled?**  
A: `UITheme.CURRENCY_SYMBOL` is a mutable `static` field (e.g., `"₹"` or `"$"`). When the user changes currency in Profile Settings, this symbol is updated globally. All amount formatting calls go through `UITheme.formatAmt()` which prepends this symbol. No actual currency conversion is done — the symbol is presentational.

**Q11: What design patterns are used?**  
A: 
- **Factory Pattern** — `UITheme` acts as a factory for UI components.
- **Strategy Pattern (implicit)** — `PaymentGatewayService` selects behavior based on `PaymentMethod` enum using a `switch`.
- **Observer Pattern** — mouse listeners (`MouseAdapter`), action listeners, change listeners on `JTabbedPane`.
- **Singleton-like** — `AppState` is a single object that holds all data.
- **MVC** — Model (model package), View (gui package), Controller (SmartSplitApp + GroupPanel event handlers).

**Q12: What is the difference between `JDialog` and `JFrame`?**  
A: A `JFrame` is a standalone top-level window. A `JDialog` is a popup window that is typically attached to a parent frame. When set as modal (`true`), a `JDialog` blocks interaction with the parent window until the dialog is closed. Used for AddExpenseDialog, CreateGroupDialog, etc.

**Q13: Why does `User.equals()` compare only `id`?**  
A: When users are stored in a `Map<User, Double>` (for split tracking) or a `List<User>`, Java uses `equals()` and `hashCode()` to determine identity. Comparing by ID ensures that the same user object is correctly recognized regardless of name changes.

**Q14: How does "role-based access" work?**  
A: Each `Group` holds a `Map<User, UserRole>`. When a user is added, they get the `VIEWER` role by default. The group creator is the `OWNER`. The UI checks `group.getRole(loggedInUser)` before enabling certain actions (though the current version shows the UI for all roles — extensible for future enforcement).

**Q15: What happens if the `.bin` file is corrupted or missing?**  
A: `FileManager.loadAppState()` wraps the read in a try-catch. If the file doesn't exist or deserialization fails, it silently returns a fresh empty `AppState`. The app starts fresh without crashing.

---

## 13. LIMITATIONS & FUTURE SCOPE

| Limitation | Future Enhancement |
|---|---|
| No actual currency conversion rates | Integrate an exchange rate API (e.g., Open Exchange Rates) |
| Password stored as plain text | Hash passwords using `BCrypt` or `SHA-256` |
| Single `.bin` file for all users | Migrate to SQLite or H2 embedded database |
| No network features | Cloud sync, group invite links, mobile companion app |
| UPI requires phone to complete | Could integrate a payment gateway SDK |
| Role-based access not fully enforced | Full RBAC — Viewers cannot add expenses |
| No expense editing/deletion | Add edit/delete with history log |

---

## 14. SUMMARY

SmartSplit is a **complete, production-quality desktop application** built entirely with core Java and Swing. It demonstrates all major Object-Oriented Programming concepts, uses proper 3-tier architecture, implements a meaningful algorithm (debt simplification), provides rich analytics via custom graphics, and delivers a premium modern UI — all without any external dependencies.

---
*Author: Aditya Azarekar | Sem 6 Java Mini Project*
