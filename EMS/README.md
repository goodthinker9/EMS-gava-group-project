# Employee Management System (EMS)

A full-featured desktop application built with **Java Swing** and **MySQL (MAMP)**, developed as a collaborative group project.

---

## Project Description

This is a Java-based Employee Management System developed as a group project.
The system allows management of employees, departments, payroll, attendance, and leave requests with role-based access for Admin and Employee users.

---

## Technologies Used

| Technology | Purpose |
|---|---|
| Java 17+ | Core language |
| Java Swing | Desktop UI framework |
| JDBC | Database connectivity |
| MySQL (MAMP) | Database server |
| Git & GitHub | Version control & collaboration |

---

## Features

- Role-based login (Admin / Employee)
- Employee CRUD (Add, Edit, Delete, Search)
-  Department management
- Payroll generation & reports
- Attendance marking & monthly reports
- Leave request & approval system
- Analytics dashboard with charts (Bar, Pie)
- Advanced search & filters
-  Dark / Light mode toggle

---

## Project Structure & Responsibilities

```
ems/
├── database/                           → SQL scripts
│   ├── schema.sql          ← kalid (Kalid)
│   ├── migrate.sql         ← kalid (Kalid)
│   └── reset_and_rebuild.sql
│
├── lib/
│   └── mysql-connector-j-9.7.0.jar    → JDBC driver
│
├── docs/                               → Diagrams & screenshots
│
├── src/
│   ├── Main.java
│   │
│   ├── database/                       → Utilities (DB connection)
│   │   └── DBConnection.java           ← kalid (Kalid)
│   │
│   ├── models/                         → Data classes
│   │   ├── User.java                   ← kalid
│   │   ├── Employee.java               ← kalid
│   │   ├── Department.java             ← kalid
│   │   ├── Payroll.java                ← kalid
│   │   ├── Attendance.java             ← kalid
│   │   └── LeaveRequest.java           ← kalid
│   │
│   ├── services/                       → Business logic
│   │   ├── AuthService.java            ← amir
│   │   ├── EmployeeService.java        ← amir
│   │   ├── DepartmentService.java      ← amir
│   │   ├── PayrollService.java         ← esknder
│   │   ├── AttendanceService.java      ← Keriya
│   │   └── LeaveService.java           ← Keriya
│   │
│   └── ui/                             → User interface
│       ├── UIUtils.java                ← Rehima
│       ├── LoginFrame.java             ← amir
│       ├── AdminDashboard.java         ← esknder
│       ├── EmployeeDashboard.java      ← Keriya
│       ├── EmployeeFormDialog.java     ← amir
│       ├── PayrollPanel.java           ← esknder
│       ├── SearchPanel.java            ← esknder
│       ├── AttendancePanel.java        ← Keriya
│       ├── LeaveRequestPanel.java      ← Keriya
│       ├── LeaveApprovalPanel.java     ← Rehima
│       ├── AnalyticsPanel.java         ← Rehima
│       ├── BarChartPanel.java          ← Rehima
│       └── PieChartPanel.java          ← Rehima
│
├── .classpath
├── .project
├── .gitignore
├── .vscode/
│   ├── settings.json
│   └── launch.json
├── compile_and_run.bat                 → Windows build & run
└── compile_and_run.sh                  → macOS/Linux build & run
```

---

## Team Responsibilities

### Kalid — Database & Utility
**Branch:** `feature-kalid-database` | **ID:** wour/1105/16

- **Folder:** `database/`, `src/database/`, `src/models/`
- **Tasks:**
  - Create `DBConnection.java` — singleton MySQL connection
  - Create `schema.sql` — all database tables
  - Create `migrate.sql` — upgrade script for existing databases
  - Setup MySQL connection with MAMP
  - Define all model classes (User, Employee, Department, Payroll, Attendance, LeaveRequest)

---

### amir — Authentication & Service
**Branch:** `feature-member2-auth` | **ID:** wour/0000/16

- **Folder:** `src/services/`, `src/ui/`
- **Tasks:**
  - `AuthService.java` — login system, SHA-256 password hashing, user creation
  - `EmployeeService.java` — employee CRUD, search & filter
  - `DepartmentService.java` — department CRUD
  - `LoginFrame.java` — login screen UI
  - `EmployeeFormDialog.java` — add/edit employee form

---

### esknder — Admin Dashboard & Payroll
**Branch:** `feature-member3-employee` | **ID:** wour/0000/16

- **Folder:** `src/services/`, `src/ui/`
- **Tasks:**
  - `PayrollService.java` — payroll CRUD, salary reports
  - `AdminDashboard.java` — admin sidebar dashboard
  - `PayrollPanel.java` — payroll entry form and table
  - `SearchPanel.java` — advanced search with filters

---

### Keriya — Department, Attendance & Leave
**Branch:** `feature-member4-department` | **ID:** wour/0000/16

- **Folder:** `src/services/`, `src/ui/`
- **Tasks:**
  - `AttendanceService.java` — attendance CRUD, monthly summary
  - `LeaveService.java` — leave request submit, approve/reject
  - `EmployeeDashboard.java` — employee sidebar dashboard
  - `AttendancePanel.java` — mark attendance, monthly report
  - `LeaveRequestPanel.java` — employee leave request form & history

---

### Rehima — UI/UX Design & Analytics
**Branch:** `feature-member5-ui` | **ID:** wour/0000/16

- **Folder:** `src/ui/`
- **Tasks:**
  - `UIUtils.java` — shared theme engine, dark/light mode, all colours & fonts
  - `AnalyticsPanel.java` — KPI cards, charts, department breakdown
  - `BarChartPanel.java` — custom bar chart (no external library)
  - `PieChartPanel.java` — custom donut pie chart (no external library)
  - `LeaveApprovalPanel.java` — admin leave approval panel

> ⚠️ **UIUtils.java is shared by all members.** Only Rehima should edit it.

---

## Team Members & Branch Names

| Name     | Role                        | Branch Name                  | ID           |
|----------|-----------------------------|------------------------------|--------------|
| Kalid    | Database & Utility          | feature-member1-database       | wour/0000/16 |
| amir | Authentication & Service    | feature-member2-auth         | wour/0000/16 |
| esknder | Admin Dashboard & Payroll   | feature-esknder-employee     | wour/0720/16 |
| Keriya | Department, Attendance & Leave | feature-member4-department | wour/0000/16 |
| Rehima | UI/UX & Analytics           | feature-member5-ui           | wour/0000/16 |

---

## How to Run

1. Clone the repository
   ```bash
   git clone https://github.com/your-org/ems.git
   ```
2. Start **MAMP** and make sure MySQL is running on port **3306**
3. Open **phpMyAdmin** → SQL tab → run `database/schema.sql`
4. Open `src/database/DBConnection.java` and confirm the DB name and password match your MAMP setup
5. Run the application:
   - **Windows:** double-click `compile_and_run.bat` or run it in CMD
   - **VS Code:** open `src/Main.java` → click **▶ Run**
6. Login with `admin` / `admin123`

---

## Database Tables

| Table | Description | Owner |
|---|---|---|
| `users` | Login credentials and roles | kalid |
| `departments` | Department list | kalid |
| `employees` | Employee profiles | kalid |
| `payroll` | Monthly salary records | esknder |
| `attendance` | Daily attendance per employee | Keriya |
| `leave_requests` | Leave applications and approvals | Keriya |

---

## Git Workflow

```
main          ← stable, working code only
  └── dev     ← integration branch (merge here first)
        ├── feature-kalid-database
        ├── feature-member2-auth
        ├── feature-member3-employee
        ├── feature-member4-department
        └── feature-member5-ui
```

**Daily workflow:**
```bash
git checkout dev
git pull origin dev
git checkout feature-kalid-database
git merge dev
# ... do your work ...
git add .
git commit -m "feat: add DBConnection singleton"
git push origin feature-kalid-database
# then open a Pull Request into dev on GitHub
```

---

## Rules for Contributors

- ✅ Each member must work on their own branch
- ✅ Do **NOT** push directly to `main` or `dev`
- ✅ Write meaningful commit messages (`feat:`, `fix:`, `docs:`)
- ✅ Create a Pull Request before merging
- ✅ Pull from `dev` before starting each work session
- ❌ Do not edit files owned by another member without discussing first
- ❌ Do not commit the `out/` folder (compiled files — it's in `.gitignore`)
