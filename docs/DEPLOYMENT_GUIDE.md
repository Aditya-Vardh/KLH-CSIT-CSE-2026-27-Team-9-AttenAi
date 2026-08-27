# AttendAI Deployment Guide

## Local Development

### Prerequisites
- Java 17 (Temurin recommended)
- Maven 3.9+
- MySQL 8.0 (or Docker)
- Node.js 20+

### Start MySQL with Docker
```bash
docker run -d --name mysql-local \
  -e MYSQL_ROOT_PASSWORD=root \
  -p 3306:3306 \
  mysql:8.0
```

### Start all services
See README.md Quick Start section.

---

## Docker Compose (Full Stack)

```bash
# 1. Build all JARs
for svc in eureka-server/eureka-server auth-service api-gateway \
           employee-service attendance-service leave-service \
           notification-service ai-agent-service; do
  (cd backend/$svc && mvn package -DskipTests -q)
done

# 2. Copy .env
cp .env.example .env
# Edit .env with your values

# 3. Start
docker compose up -d

# 4. Check logs
docker compose logs -f eureka-server
docker compose logs -f api-gateway
```

### Service URLs (Docker)
- Frontend:          http://localhost:3000
- API Gateway:       http://localhost:8080
- Eureka Dashboard:  http://localhost:8761
- Auth Swagger:      http://localhost:8081/swagger-ui.html

---

## Railway Deployment (Backend)

### Step-by-step

1. **Create Railway project** at railway.app
2. **Add MySQL databases** (New → Database → MySQL) — one per service:
   - auth_db, employee_db, attendance_db, leave_db, notification_db, ai_db
3. **Add each service** (New → GitHub Repo → select folder):
   - Set **Root Directory** to the service folder (e.g. `backend/auth-service`)
   - Railway auto-detects the Dockerfile
4. **Set environment variables** for each service (from .env.example)
5. **Set start order**: Eureka first, then all others

### Required env vars per service

**All services:**
```
EUREKA_CLIENT_SERVICE_URL=http://eureka-server.railway.internal:8761/eureka/
```

**Auth service:**
```
AUTH_DB_URL=jdbc:mysql://${{MySQL.MYSQL_HOST}}:${{MySQL.MYSQL_PORT}}/auth_db?...
AUTH_DB_USERNAME=${{MySQL.MYSQL_USER}}
AUTH_DB_PASSWORD=${{MySQL.MYSQL_PASSWORD}}
JWT_SECRET=<your-secret>
```

---

## Vercel Deployment (Frontend)

1. Push to GitHub
2. Import project in Vercel → select `frontend` as root directory
3. Framework preset: **Vite**
4. Environment variables:
   ```
   VITE_API_URL=https://your-railway-gateway.up.railway.app
   ```
5. Update `frontend/vercel.json`:
   ```json
   "destination": "https://your-railway-gateway.up.railway.app/api/$1"
   ```
6. Also update `frontend/src/lib/axios.js` baseURL for production:
   ```js
   baseURL: import.meta.env.VITE_API_URL + '/api'
   ```

---

## GitHub Actions CI/CD

Two workflows are included:

| Workflow               | Trigger          | What it does                              |
|------------------------|------------------|-------------------------------------------|
| `ci.yml`               | push/PR to main  | Build + test all services                 |
| `docker-publish.yml`   | push to main/tag | Build + push Docker images to GHCR        |

### Required GitHub Secrets
- `GITHUB_TOKEN` — automatically provided by GitHub Actions

### Optional secrets for production
- `RAILWAY_TOKEN` — for automated Railway deploys
- `VERCEL_TOKEN` — for automated Vercel deploys
