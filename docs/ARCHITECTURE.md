# AttendAI Architecture

## Request Flow

```
Browser
  │
  │  HTTPS
  ▼
[Vercel / nginx]  ← React SPA
  │
  │  /api/* proxy
  ▼
[API Gateway :8080]
  │  ① Validate JWT (JwtUtil.isTokenValid)
  │  ② Add headers: X-Auth-User-Email, X-Auth-User-Id, X-Auth-User-Role
  │  ③ Route to service via Eureka lb://
  │
  ├─/api/auth/**        → auth-service:8081
  ├─/api/employees/**   → employee-service:8082
  ├─/api/departments/** → employee-service:8082
  ├─/api/attendance/**  → attendance-service:8083
  ├─/api/leave/**       → leave-service:8084
  ├─/api/notifications/**→notification-service:8085
  └─/api/ai/**          → ai-agent-service:8086
```

## Inter-Service Communication

```
attendance-service ──OpenFeign──► employee-service  (/api/employees/{id}/summary)
leave-service      ──OpenFeign──► employee-service  (/api/employees/{id}/summary)
ai-agent-service   ──OpenFeign──► employee-service  (summaries)
                   ──OpenFeign──► attendance-service (monthly summaries, late arrivals)
                   ──OpenFeign──► leave-service      (leave balances)
```

## Security Architecture

```
auth-service issues JWT:
  {
    sub: "user@email.com",
    role: "ADMIN" | "HR" | "EMPLOYEE",
    userId: 123,
    exp: <now + 60min>
  }

API Gateway:
  ① Extracts Bearer token from Authorization header
  ② Calls JwtUtil.isTokenValid() (local HMAC verification, no DB)
  ③ If invalid → 401 Unauthorized
  ④ If valid  → adds trusted headers to forwarded request:
     X-Auth-User-Email: user@email.com
     X-Auth-User-Id:    123
     X-Auth-User-Role:  ADMIN

Downstream services:
  ① GatewayJwtAuthFilter reads headers
  ② Builds Spring SecurityContext (no JWT re-parsing needed)
  ③ @PreAuthorize annotations can check roles
```

## Database Per Service

Each microservice owns its own MySQL database:

```
auth-service         → auth_db
  tables: users, refresh_tokens, password_reset_tokens

employee-service     → employee_db
  tables: employees, departments, designations

attendance-service   → attendance_db
  tables: attendance

leave-service        → leave_db
  tables: leave_types, leave_requests, leave_balances

notification-service → notification_db
  tables: notification_logs

ai-agent-service     → ai_db
  (minimal — AI is mostly stateless, calls downstream services)
```

No cross-database foreign keys. Services communicate via APIs only.

## AI Agent Design

```
User: "I'll be 30 min late today"
         │
         ▼
    NlpService.extractIntent()     → LATE_ARRIVAL
    NlpService.extractExpectedArrival() → 09:30
    NlpService.computeLateMinutes() → 30
         │
         ▼
    AttendanceClient.markAiAttendance()  ← OpenFeign to attendance-service
         │
         ▼
    attendance-service saves record
    (status=LATE, lateMinutes=30, aiGenerated=true)
         │
         ▼
    NlAttendanceResponse returned to user
```

```
Leave Recommendation:
  Input: employeeId, leaveTypeId, startDate, endDate
    │
    ├── AttendanceClient.getMonthlySummary() → attendancePct
    ├── LeaveClient.getBalance()             → remainingDays
    └── Scoring:
          - attendancePct >= 85% → +score
          - sufficient balance   → +score
          - long duration (>10d) → -score
          → recommendation: APPROVE / REJECT + confidenceScore [0..1]
```
