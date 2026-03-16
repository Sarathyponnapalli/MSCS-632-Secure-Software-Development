# Secure Employee Portal
### MSCS-632 Secure Software Development — Parthasarathi Ponnapalli

## Project 1 

A Spring Boot web application demonstrating defense against phishing attacks
and SQL injection through HTTPS/TLS, Spring Security, BCrypt password hashing,
role-based access control, and JPA parameterized database queries.

The whole point is to demonstrate how to defend against two attacks from
our course material:

1. **Phishing** — attacker sends fake IT email asking for passwords
   → We defend with HTTPS encryption + BCrypt password hashing

2. **SQL Injection** — attacker types malicious SQL into search fields
   → We defend with parameterized queries (Spring Data JPA)

## Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+**

## Quick Start

```bash
# 1. Navigate to the project directory
cd secure-employee-portal

# 2. Build the project
mvn clean package -DskipTests

# 3. Run the application
mvn spring-boot:run
```

The application starts on **https://localhost:8443**

> Your browser will show a certificate warning because the KeyStore contains
> a self-signed certificate. Click "Advanced" → "Proceed" to continue.
> In production, you would use a CA-signed certificate.

## Demo Credentials

| Username   | Password          | Role       | Access Level               |
|------------|-------------------|------------|----------------------------|
| `hradmin`  | `Admin@2024Secure`| ADMIN+USER | Full CRUD on all records   |
| `employee` | `Emp@2024Secure`  | USER       | Read-only access           |
| `manager`  | `Mgr@2024Secure`  | MANAGER+USER | Read-only + dept filter |

## Web Interface

- **Login:** https://localhost:8443/login
- **Dashboard:** https://localhost:8443/
- **Employee List:** https://localhost:8443/employees
- **H2 Console (admin):** https://localhost:8443/h2-console
  - JDBC URL: `jdbc:h2:mem:employeedb`
  - Username: `sa` / Password: *(empty)*

## REST API Endpoints

All API endpoints require HTTP Basic Authentication over HTTPS.

```bash
# List all employees (USER, MANAGER, or ADMIN)
curl -k -u employee:Emp@2024Secure https://localhost:8443/api/employees

# Search by last name — parameterized query, SQL injection impossible
curl -k -u employee:Emp@2024Secure \
  "https://localhost:8443/api/employees/search?lastName=King"

# SQL injection attempt — returns empty array, NOT all records
curl -k -u employee:Emp@2024Secure \
  "https://localhost:8443/api/employees/search?lastName=%27%20OR%20%271%27%3D%271"

# Keyword search across name + email
curl -k -u employee:Emp@2024Secure \
  "https://localhost:8443/api/employees/search/keyword?q=admin"

# Get by department
curl -k -u employee:Emp@2024Secure \
  https://localhost:8443/api/employees/department/60

# Create employee (ADMIN only)
curl -k -u hradmin:Admin@2024Secure \
  -X POST -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jdoe@company.com","jobId":"IT_PROG","salary":7500,"departmentId":60}' \
  https://localhost:8443/api/employees

# Update employee (ADMIN only)
curl -k -u hradmin:Admin@2024Secure \
  -X PUT -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jdoe@company.com","jobId":"SA_MAN","salary":12000,"departmentId":80}' \
  https://localhost:8443/api/employees/14

# Delete employee (ADMIN only)
curl -k -u hradmin:Admin@2024Secure \
  -X DELETE https://localhost:8443/api/employees/14

# Unauthorized attempt — returns 401
curl -k https://localhost:8443/api/employees

# Insufficient privileges — USER trying DELETE returns 403
curl -k -u employee:Emp@2024Secure \
  -X DELETE https://localhost:8443/api/employees/1
```

## Security Layers

| Layer | Defense Against | Implementation |
|-------|----------------|----------------|
| HTTPS/TLS | Network sniffing, MITM | PKCS12 KeyStore + `requiresChannel().requiresSecure()` |
| BCrypt Hashing | Password compromise | `BCryptPasswordEncoder` with random salt |
| Role-Based Access | Privilege escalation | `ROLE_USER` (read), `ROLE_ADMIN` (CRUD) |
| Parameterized Queries | SQL injection | Spring Data JPA + `@Param` bind variables |
| CSRF Protection | Cross-site request forgery | Spring Security CSRF tokens on web forms |
| Input Validation | Malformed data | Bean Validation (`@Valid`, `@NotBlank`, `@Email`) |

## How to Test Each Security Feature

### TEST 1: Login works (Spring Security)

1. Open https://localhost:8443
2. You see the login page — you CANNOT skip it
3. Type wrong password → you get "Invalid username or password"
4. Type `hradmin` / `Admin@2024Secure` → you get into the dashboard
5. This proves: Spring Security is protecting the app

### TEST 2: HTTPS is working (KeyStore / TLS)

1. Look at your browser's address bar
2. It says `https://` (not `http://`)
3. You see a lock icon (might have a warning because self-signed)
4. Try going to `http://localhost:8443` → it redirects to `https://`
5. This proves: all traffic is encrypted, nobody can sniff passwords

### TEST 3: Role-based access control (least privilege)

1. Log in as `employee` / `Emp@2024Secure`
2. Go to Employees page — you can see the list
3. Notice: there is NO "Add" button, NO "Edit" button, NO "Delete" button
4. Log out (button in top right)
5. Log in as `hradmin` / `Admin@2024Secure`
6. Go to Employees page — now you see Add, Edit, Delete buttons
7. This proves: regular users can only VIEW, admins can MODIFY

### TEST 4: SQL injection is blocked (parameterized queries)

This is the big one from Assignment 2.

1. Log in as `hradmin` / `Admin@2024Secure`
2. Go to the Employees page
3. In the search box, type: `Aarav` and click Search
   → You see Steven King's record. Normal.

4. Now try the SQL injection from Assignment 2.
   In the search box, type exactly: `' OR '1'='1`
   and click Search.

5. **If the app was VULNERABLE:**
   It would show ALL 13 employees (the whole database gets dumped)

6. **Actual result (because we're protected):**
   It shows "0 found" — empty results.
   The attack was treated as a literal search string, not as SQL code.

7. This proves: parameterized queries make SQL injection impossible

### TEST 5: REST API with curl (optional, for the command line)

Open a NEW terminal window (keep the app running in the first one).

```
# This works — authenticated request
curl -k -u employee:Emp@2024Secure https://localhost:8443/api/employees

# This fails — no credentials → 401 Unauthorized
curl -k https://localhost:8443/api/employees

# This fails — regular user trying to delete → 403 Forbidden
curl -k -u employee:Emp@2024Secure -X DELETE https://localhost:8443/api/employees/1

# This works — admin can delete → 204 No Content
curl -k -u hradmin:Admin@2024Secure -X DELETE https://localhost:8443/api/employees/1

# SQL injection via API — returns empty array []
curl -k -u employee:Emp@2024Secure "https://localhost:8443/api/employees/search?lastName=%27%20OR%20%271%27%3D%271"
```

The `-k` flag tells curl to accept the self-signed certificate.

---

## Project Structure

```
secure-employee-portal/
├── pom.xml                                         # Dependencies
├── README.md                                       # This file
└── src/main/
    ├── java/secureportal/
    │   ├── SecureEmployeePortalApplication.java    # Entry point
    │   ├── model/
    │   │   └── Employee.java                       # JPA entity
    │   ├── repository/
    │   │   └── EmployeeRepository.java             # Parameterized queries
    │   ├── controller/
    │   │   ├── EmployeeController.java             # REST API
    │   │   └── WebController.java                  # Web UI
    │   └── config/
    │       └── SecurityConfig.java                 # Spring Security
    └── resources/
        ├── application.properties                  # HTTPS + DB config
        ├── keystore.p12                            # TLS certificate
        ├── data.sql                                # Seed data
        └── templates/
            ├── login.html                          # Login page
            ├── dashboard.html                      # Dashboard
            ├── employees.html                      # Employee list
            └── employee-form.html                  # Add/Edit form
```

---

## What Each File Does

| File | What it does |
|------|-------------|
| `pom.xml` | Lists all libraries (Spring Boot, Security, JPA, H2, Thymeleaf) |
| `SecureEmployeePortalApplication.java` | Starts the app (main method) |
| `Employee.java` | Maps to the database table — same fields as Oracle HR schema |
| `EmployeeRepository.java` | Database queries — all parameterized, blocks SQL injection |
| `EmployeeController.java` | REST API endpoints (GET/POST/PUT/DELETE) |
| `WebController.java` | Serves the HTML pages (login, dashboard, employees, form) |
| `SecurityConfig.java` | BCrypt passwords + role-based access + HTTPS enforcement |
| `application.properties` | Port 8443, keystore path, database URL |
| `keystore.p12` | TLS certificate generated with Java keytool |
| `data.sql` | 13 sample employees loaded on startup |
| `login.html` | Login page with styled form |
| `dashboard.html` | Home page showing security status and employee count |
| `employees.html` | Employee table with search, edit, delete |
| `employee-form.html` | Form for adding or editing an employee |

---