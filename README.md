# Employee Management System (Java)

## Project Description

This is a Java-based Employee Management System developed as a group project.
The system allows management of employees, departments, and user authentication.

---

##  Technologies Used

* Java
* JDBC
* MySQL
* Git & GitHub

---
##  Project Structure & Responsibilities

src/
 ├── model/     → Data classes
 ├── dao/       → Database operations (JDBC)
 ├── service/   → Business logic
 ├── ui/        → User interface
 └── util/      → Utilities (DB connection)

database/       → SQL scripts
docs/           → Diagrams & screenshots
```

---

## Team Responsibilities

###  Kalid (Database & Utility)

* Folder: `util/`, `database/`
* Tasks:

  * Create DBConnection.java
  * Create database.sql
  * Setup MySQL connection

---

### Member 2 (Authentication & Service)

* Folder: `service/`
* Tasks:

  * Login system
  * User validation
  * AuthService.java

---

### Member 3 (Employee CRUD)

* Folder: `model/`, `dao/`, `service/`
* Tasks:

  * Employee.java
  * EmployeeDAO.java
  * EmployeeService.java
  * CRUD operations

---

###  Member 4 (Department Module)

* Folder: `model/`, `dao/`, `service/`
* Tasks:

  * Department.java
  * DepartmentDAO.java
  * DepartmentService.java

---

###  Member 5 (UI Design)

* Folder: `ui/`
* Tasks:

  * LoginForm.java
  * Dashboard.java
  * Employee UI

---

## Team Members & Branch Names

| Name     | Role                   | Branch Name                 |  ID
| -------- | ---------------------- | ------------------          |--------------------
| kalid    | Database & Integration | feature-kalid-database      |wour/1105/16
| Member 2 | Authentication         | feature-member2-auth        |wour/0000/16
| Member 3 | Employee CRUD          | feature-member3-employee    |wour/0000/16
| Member 4 | Department             | feature-member4-department  |wour/0000/16
| Member 5 | UI/UX                  | feature-member5-ui          |wour/0000/16

---

##  Project Structure

src/
model/ → Classes (Employee, Department, User)
dao/ → Database operations
service/ → Business logic
ui/ → User interface
util/ → Utility classes

---

##  How to Run

1. Clone the repository
2. Import into your IDE (NetBeans / IntelliJ)
3. Setup MySQL database using `database.sql`
4. Run `Main.java`

---

## Features

* Login system
* Employee CRUD
* Department management
* Search functionality

---

## Rules for Contributors

* Each member must work on their own branch
* Do NOT push directly to main
* Write meaningful commit messages
* Create Pull Request before merging

---
