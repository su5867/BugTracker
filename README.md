# 🐛 BugTracker — Production-Ready Bug Tracking System

A full-stack Bug Tracking System built with **Java Spring Boot 3**, **Thymeleaf**, **Spring Security**, **Spring Data JPA (Hibernate)**, and **MySQL**.

---

## 📋 Features

| Feature | Description |
|---|---|
| **User Roles** | ADMIN, DEVELOPER, TESTER with full RBAC |
| **Project Management** | Create, update, delete, assign members |
| **Bug Tracking** | Full CRUD with priority, severity, status, assignee |
| **Comments** | Threaded discussion on each bug |
| **File Attachments** | Upload and download files per bug |
| **Activity History** | Audit trail of all bug changes |
| **Dashboards** | Role-specific dashboards with charts |
| **Search & Filter** | Filter by status, priority, project, assignee, keyword |
| **Pagination** | Server-side pagination on bug lists |
| **Notifications** | Log-based notification simulation |
| **Seed Data** | Auto-populated on first start |
| **Docker Support** | One-command startup with docker-compose |

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### Option 1: Run with Maven (local MySQL)

**1. Create the database:**
```sql
CREATE DATABASE bugtracker CHARACTER SET utf8mb4;
```

**2. Update credentials** in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bugtracker?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

**3. Build and run:**
```bash
mvn clean spring-boot:run
```

**4. Open browser:** http://localhost:8080

---

### Option 2: Docker Compose (recommended)

```bash
docker-compose up --build
```

This starts both MySQL and the app automatically.

---

### Option 3: Build JAR and run

```bash
mvn clean package -DskipTests
java -jar target/bugtracker-1.0.0.jar
```

---

## 🔑 Default Login Credentials

| Role | Username | Password |
|---|---|---|
| **Admin** | `admin` | `admin123` |
| **Developer** | `dev1` | `dev123` |
| **Developer** | `dev2` | `dev123` |
| **Tester** | `tester1` | `test123` |

---

## 📁 Project Structure

```
src/main/java/com/bugtracker/
├── BugTrackerApplication.java   # Entry point
├── config/
│   ├── SecurityConfig.java      # Spring Security configuration
│   ├── WebMvcConfig.java        # Static resource mapping
│   └── DataInitializer.java     # Seed data on first run
├── controller/
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── ProjectController.java
│   ├── BugController.java
│   └── UserController.java
├── service/
│   ├── UserService.java
│   ├── ProjectService.java
│   ├── BugService.java
│   ├── CommentService.java
│   ├── FileStorageService.java
│   └── NotificationService.java
├── repository/              # Spring Data JPA repositories
├── entity/                  # JPA entities
├── dto/                     # Data Transfer Objects
├── security/                # UserDetails + UserDetailsService
└── exception/               # Global exception handling

src/main/resources/
├── application.properties
├── static/css/style.css
├── static/js/main.js
└── templates/
    ├── fragments/layout.html
    ├── auth/{login,register}.html
    ├── dashboard/{admin,developer,tester}.html
    ├── project/{list,form,detail}.html
    ├── bug/{list,form,detail}.html
    ├── user/{list,form}.html
    └── error/{403,404,500}.html
```

---

## 🔐 Security

- Spring Security session-based authentication
- BCrypt password encoding
- CSRF protection on all forms
- Role-based access control via `@PreAuthorize` and `sec:authorize`
- Protected file downloads

---

## 🗃️ Database

Schema is auto-created by Hibernate (`ddl-auto=update`).  
For manual setup, run `schema.sql`.

---

## 📦 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2 |
| Frontend | Thymeleaf, Bootstrap 5, Chart.js |
| Security | Spring Security (Session) |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8.0 |
| Build | Maven |
| Container | Docker + Docker Compose |
