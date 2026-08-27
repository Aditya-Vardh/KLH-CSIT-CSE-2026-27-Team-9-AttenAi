# AttendAI — Smart Attendance Management System

A production-ready microservices application for employee attendance and leave management, powered by AI.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        React Frontend                        │
│            (Vite + Tailwind + React Router + Chart.js)       │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP  (port 3000 / 5173 dev)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway  :8080                       │
│          (Spring Cloud Gateway + JWT validation filter)       │
└──┬────┬─────┬──────┬──────┬────────┬──────────────────────┘
   │    │     │      │      │        │
   ▼    ▼     ▼      ▼      ▼        ▼
 Auth  Emp  Attend Leave  Notif    AI Agent
 8081  8082  8083  8084   8085      8086
   │    │     │      │      │        │
   └────┴─────┴──────┴──────┴────────┘
                    │  Eureka Discovery  :8761
                    └─────────────────────────

Each service → its own MySQL database
Inter-service communication via OpenFeign (lb:// Eureka)
```

---

## Services

| Service              | Port | Database          | Description                                      |
|----------------------|------|-------------------|--------------------------------------------------|
| Eureka Server        | 8761 | —                 | Service registry (Netflix Eureka)                |
| API Gateway          | 8080 | —                 | Single entry point, JWT validation, CORS          |
| Auth Service         | 8081 | `auth_db`         | Register, login, JWT, refresh tokens             |
| Employee Service     | 8082 | `employee_db`     | Employee/Department/Designation CRUD             |
| Attendance Service   | 8083 | `attendance_db`   | Check-in/out, history, analytics                 |
| Leave Service        | 8084 | `leave_db`        | Leave requests, approvals, balance               |
| Notification Service | 8085 | `notification_db` | Email notifications via SMTP                     |
| AI Agent Service     | 8086 | `ai_db`           | NL attendance, leave recommendation, chatbot, PDF|
| React Frontend       | 3000 | —                 | Full-stack web UI                                |

---

## Quick Start (Local)

### Prerequisites
- Java 17
- Maven 3.9+
- MySQL 8.0 (running on port 3306)
- Node.js 20+

### 1. Clone & configure

```bash
git clone https://github.com/your-org/attendai.git
cd attendai
cp .env.example .env   # fill in your values
```

### 2. Start all backend services (in order)

```bash
# 1. Eureka Server
cd backend/eureka-server/eureka-server
mvn spring-boot:run

# 2. Auth Service
cd backend/auth-service
mvn spring-boot:run

# 3. API Gateway
cd backend/api-gateway
mvn spring-boot:run

# 4. Employee Service
cd backend/employee-service
mvn spring-boot:run

# 5. Attendance Service
cd backend/attendance-service
mvn spring-boot:run

# 6. Leave Service
cd backend/leave-service
mvn spring-boot:run

# 7. Notification Service
cd backend/notification-service
mvn spring-boot:run

# 8. AI Agent Service
cd backend/ai-agent-service
mvn spring-boot:run
```

### 3. Start the frontend

```bash
cd frontend
npm install --legacy-peer-deps
npm run dev   # → http://localhost:5173
```

### 4. Docker Compose (all-in-one)

```bash
cp .env.example .env
# Build all JARs first
find backend -name "pom.xml" -maxdepth 3 ! -path "*/eureka-server/*" | xargs -I{} sh -c 'cd $(dirname {}) && mvn package -DskipTests -q'
cd backend/eureka-server/eureka-server && mvn package -DskipTests -q
# Then bring everything up
docker compose up --build
```

Access: http://localhost:3000  
Eureka Dashboard: http://localhost:8761

---

## API Reference

All requests route through the gateway at `http://localhost:8080`.

### Authentication

| Method | Endpoint               | Description            |
|--------|------------------------|------------------------|
| POST   | `/api/auth/register`   | Create account         |
| POST   | `/api/auth/login`      | Login → JWT            |
| POST   | `/api/auth/refresh`    | Refresh access token   |
| POST   | `/api/auth/validate`   | Validate JWT           |
| POST   | `/api/auth/forgot-password` | Request reset token |
| POST   | `/api/auth/reset-password`  | Reset password      |

### Employees

| Method | Endpoint                        | Description                |
|--------|---------------------------------|----------------------------|
| GET    | `/api/employees`                | List (paginated, searchable)|
| GET    | `/api/employees/{id}`           | Get by ID                  |
| GET    | `/api/employees/by-user/{uid}`  | Get by auth userId         |
| POST   | `/api/employees`                | Create employee            |
| PUT    | `/api/employees/{id}`           | Update employee            |
| DELETE | `/api/employees/{id}`           | Delete employee            |
| GET    | `/api/departments`              | List departments           |
| POST   | `/api/departments`              | Create department          |
| GET    | `/api/designations`             | List designations          |
| POST   | `/api/designations`             | Create designation         |

### Attendance

| Method | Endpoint                              | Description              |
|--------|---------------------------------------|--------------------------|
| POST   | `/api/attendance/check-in`            | Check in                 |
| POST   | `/api/attendance/check-out`           | Check out                |
| PUT    | `/api/attendance/manual`              | HR override              |
| GET    | `/api/attendance/history/{empId}`     | History (paginated)      |
| GET    | `/api/attendance/monthly/{empId}`     | Monthly summary          |
| GET    | `/api/attendance/late-arrivals`       | Late arrivals list       |
| GET    | `/api/attendance/analytics`           | Team analytics           |

### Leave

| Method | Endpoint                           | Description              |
|--------|------------------------------------|--------------------------|
| POST   | `/api/leave/requests`              | Submit request           |
| GET    | `/api/leave/requests/employee/{id}`| Employee history         |
| GET    | `/api/leave/requests/pending`      | All pending (HR)         |
| PATCH  | `/api/leave/requests/{id}/approve` | Approve                  |
| PATCH  | `/api/leave/requests/{id}/reject`  | Reject                   |
| PATCH  | `/api/leave/requests/{id}/cancel`  | Cancel                   |
| GET    | `/api/leave/balance/{empId}`       | Leave balance            |
| GET    | `/api/leave/types`                 | Leave type catalogue     |

### Audit Logs

| Method | Endpoint               | Description                         |
|--------|------------------------|-------------------------------------|
| GET    | `/api/audit`           | Paginated audit log (Admin only)    |
| POST   | `/api/audit`           | Record event (internal/service use) |

### AI Agent

| Method | Endpoint                        | Description                       |
|--------|---------------------------------|-----------------------------------|
| POST   | `/api/ai/attendance/nl`         | NL attendance processing          |
| POST   | `/api/ai/leave/recommend`       | AI leave recommendation           |
| POST   | `/api/ai/analytics/query`       | NL analytics query                |
| POST   | `/api/ai/chat`                  | HR chatbot                        |
| GET    | `/api/ai/report/attendance`     | Download PDF attendance report    |

---

## Security

- JWT (HS512, jjwt 0.12.5) issued by auth-service
- API Gateway validates every token before forwarding
- Downstream services trust `X-Auth-User-Email`, `X-Auth-User-Id`, `X-Auth-User-Role` headers
- Roles: `ADMIN`, `HR`, `MANAGER`, `EMPLOYEE`
- Authorization is enforced **server-side** on every mutating endpoint (e.g. only `ADMIN`/`HR`/`MANAGER` can approve/reject leave — a `403 Forbidden` is returned for any other role regardless of frontend state)
- Audit events are written for all important employee/department/leave actions and are viewable by `ADMIN` only via `/api/audit`

---

## Seed Data

A demo seed script is provided at `seed/seed_demo_data.sql`.

It inserts:

| Entity | Data |
|---|---|
| Departments | Engineering, Human Resources, Finance, Operations |
| Designations | Junior Developer, Senior Developer, HR Manager, Department Head, Analyst |
| Leave types | Annual, Sick, Casual, Maternity/Paternity, Unpaid |
| Users | admin@attendai.com (ADMIN), hr@attendai.com (HR), manager@attendai.com (MANAGER), emp1–3@attendai.com (EMPLOYEE) |
| Employees | Linked to the above users |

**All demo passwords are:** `Password@123`

Run after creating the databases:

```bash
# Apply to each relevant database
mysql -u root -p auth_db       < seed/seed_demo_data.sql
mysql -u root -p employee_db   < seed/seed_demo_data.sql
mysql -u root -p leave_db      < seed/seed_demo_data.sql
```

> The seed script is safe to skip — the application creates its own schema via Hibernate `ddl-auto: update` and works with any data you add through the UI.

---

## ER Diagram (text)

```
auth_db
  users (id, email, password, firstName, lastName, role, enabled)
  refresh_tokens (id, token, expiresAt, revoked, userId→users)
  password_reset_tokens (id, token, expiresAt, used, userId→users)

employee_db
  departments   (id, name, description, headUserId, active)
  designations  (id, title, description, grade, active)
  employees     (id, userId, employeeCode, firstName, lastName, email,
                 phone, address, dateOfBirth, joiningDate, status,
                 departmentId→departments, designationId→designations,
                 annualLeaveQuota)

attendance_db
  attendance (id, employeeId, attendanceDate, checkInTime, checkOutTime,
              workingHours, status, expectedCheckIn, lateArrival,
              lateMinutes, note, aiGenerated)
              UNIQUE(employeeId, attendanceDate)

leave_db
  leave_types    (id, name, defaultDays, carryOver, allowNegative,
                  requiresDocument, active)
  leave_requests (id, employeeId, leaveTypeId→leave_types, startDate,
                  endDate, totalDays, status, reason, reviewComment,
                  reviewedBy, reviewedAt, aiRecommended, aiConfidenceScore)
  leave_balances (id, employeeId, leaveTypeId→leave_types, leave_year,
                  allocatedDays, usedDays, pendingDays)
                  UNIQUE(employeeId, leaveTypeId, leave_year)

notification_db
  notification_logs (id, recipientEmployeeId, recipientEmail, type,
                     subject, body, status, errorMessage, referenceId)
```

---

## Deployment

### Vercel (Frontend)

1. Push repository to GitHub
2. Import project in Vercel
3. Set **Root Directory** to `frontend`
4. Set environment variable: `VITE_API_URL` → your Railway gateway URL
5. Update `frontend/vercel.json` rewrites destination to your Railway URL
6. Deploy

### Railway (Backend)

1. Create a new Railway project
2. Add MySQL plugins (one per service database)
3. For each service, add a new service pointing to the service subfolder
4. Set environment variables from `.env.example`
5. Deploy in order: Eureka → Auth → Gateway → Employee → Attendance → Leave → Notification → AI

---

## Swagger UI

Each backend service exposes Swagger at `/swagger-ui.html`:

| Service           | URL                              |
|-------------------|----------------------------------|
| Auth              | http://localhost:8081/swagger-ui.html |
| Employee          | http://localhost:8082/swagger-ui.html |
| Attendance        | http://localhost:8083/swagger-ui.html |
| Leave             | http://localhost:8084/swagger-ui.html |
| Notification      | http://localhost:8085/swagger-ui.html |
| AI Agent          | http://localhost:8086/swagger-ui.html |

---

## Running Tests

```bash
# All backend services
find backend -name "pom.xml" -maxdepth 3 | xargs -I{} sh -c 'cd $(dirname {}) && mvn test -q'

# Frontend build check
cd frontend && npm run build
```

---

## Project Structure

```
Soa_pro/
├── backend/
│   ├── eureka-server/
│   ├── auth-service/
│   ├── api-gateway/
│   ├── employee-service/
│   ├── attendance-service/
│   ├── leave-service/
│   ├── notification-service/
│   └── ai-agent-service/
├── frontend/
│   ├── src/
│   │   ├── components/     # Shared UI components
│   │   ├── context/        # Auth context
│   │   ├── layouts/        # Page layouts
│   │   ├── lib/            # Axios instance
│   │   └── pages/          # All pages
│   └── public/
├── docker-compose.yml
├── .env.example
├── .github/
│   └── workflows/
│       ├── ci.yml           # Build & test on every push
│       └── docker-publish.yml # Build & push Docker images on main
└── README.md
```

---

## Technology Stack

| Layer        | Technology                                                      |
|--------------|-----------------------------------------------------------------|
| Frontend     | React 18, Vite, Tailwind CSS, React Router 6, Chart.js, Axios  |
| API Gateway  | Spring Cloud Gateway (WebFlux), JWT validation filter           |
| Microservices| Spring Boot 3.5.5, Java 17, Spring Cloud 2025.0.0              |
| Discovery    | Netflix Eureka                                                  |
| Auth         | JWT (jjwt 0.12.5), BCrypt, Spring Security                     |
| Database     | MySQL 8.0 (one per service), Spring Data JPA, Hibernate        |
| Messaging    | OpenFeign (synchronous, Eureka lb://)                          |
| Email        | Spring Mail + JavaMailSender + HTML templates                  |
| AI/NLP       | Rule-based NLP (offline), iText PDF generation                 |
| CI/CD        | GitHub Actions                                                  |
| Deployment   | Docker, Docker Compose, Railway (backend), Vercel (frontend)   |
# Smart-attendance-employee-System
