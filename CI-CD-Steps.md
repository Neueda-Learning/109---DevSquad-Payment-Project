# DevSquad — Payment_Management — End-to-End CI/CD Setup Guide

## Spring Boot & MySQL Backend + React (Vite) Frontend (Monorepo)

### GitHub Actions (CI) + Jenkins (CD) + Docker + GHCR + ngrok

---

# 1. Solution Overview

Monorepo CI/CD pipeline:

- **Backend** (`backend/payment_processing/`): Spring Boot (Java 17) + MySQL
- **Frontend** (`frontend/`): React + Vite (baked `VITE_API_BASE_URL`)
- **Database**: MySQL (`payment_processing`)

---

# 2. Repo Layout (files to add)

```
109---DevSquad-Payment-Project/
│
├── .github/
│      └── workflows/
│              backend-ci.yml
│              frontend-ci.yml
│
├── backend/payment_processing/
│      ├── Dockerfile
│      └── .dockerignore
│
├── frontend/
│      ├── Dockerfile
│      └── .dockerignore
│
├── docker-compose.yml
└── CI-CD-Steps.md
```

---

# 3. CI — Backend

`.github/workflows/backend-ci.yml` — push/PR to `main`.

```
Checkout → JDK 17 → MySQL 8.4 service (payment_processing)
    → chmod +x mvnw → ./mvnw clean verify
    → (push only) build/push ghcr.io/neueda-learning/devsquad-api:latest
    → Trigger Jenkins devsquad-api-deploy-job
```

The app's `application.properties` reads `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (defaults `root`/`n3u3da!`) and runs `schema.sql`/`data.sql` on startup (`spring.sql.init.mode=always`) against `payment_processing`.

### GitHub secrets required

| Secret | Value |
|--------|-------|
| `JENKINS_URL` | `https://<your-ngrok-subdomain>.ngrok-free.app` |
| `JENKINS_TOKEN` | Jenkins build token (e.g. `deploy1234`) |

---

# 4. CI — Frontend

`.github/workflows/frontend-ci.yml` — push/PR to `main`.

```
Checkout → Node 22 → npm ci → npm run build
    → (push only) build/push ghcr.io/neueda-learning/devsquad-ui:latest
    → Trigger Jenkins devsquad-ui-deploy-job
```

The Dockerfile bakes `VITE_API_BASE_URL=http://localhost:8082/api` so the browser calls the API on the compose-mapped port (`8082`). The frontend has a built-in demo-data fallback, so it stays usable even if the API is down.

---

# 5. CD — Jenkins

Two Freestyle jobs with **Trigger builds remotely**:

| Job | Trigger |
|-----|---------|
| `devsquad-api-deploy-job` | `...?job=devsquad-api-deploy-job&token=<token>` |
| `devsquad-ui-deploy-job` | `...?job=devsquad-ui-deploy-job&token=<token>` |

```bash
cd /opt/devsquad
docker compose pull
docker compose down
docker compose up -d
docker compose ps
```

---

# 6. ngrok

```bash
ngrok config add-authtoken <YOUR_NGROK_AUTHTOKEN>
ngrok http 8080
```

Use the `https://xxxx.ngrok-free.app` URL as `JENKINS_URL`.

---

# 7. Docker Architecture & Credentials

```
      Docker Network
+---------------------------------------+
| devsquad-mysql  (MySQL 8.4)           |
|        ^                              |
|        | JDBC (root/n3u3da!)          |
| devsquad-api  (Spring Boot, 8082:8080)|
|        ^                              |
| devsquad-ui  (Nginx SPA, 8081:80)     |
+---------------------------------------+
```

---

# 8. Verification

```bash
open http://<server-ip>:8081
curl http://<server-ip>:8082/actuator/health
open http://<server-ip>:8082/swagger-ui.html
docker ps
```

---

# 9. End-to-End Flow

```
Developer → git push (main)
  → GitHub Actions (build + test + docker)
  → GHCR (devsquad-api / devsquad-ui)
  → curl (ngrok) → Jenkins (api / ui deploy jobs)
  → docker compose up -d
  → Running Application (MySQL + API + UI)
```

---

# 10. Summary

- Monorepo: React (Vite) frontend + Spring Boot (Java 17) backend.
- Images: `devsquad-api:latest`, `devsquad-ui:latest`.
- Jenkins: `devsquad-api-deploy-job`, `devsquad-ui-deploy-job`.
- Docker Compose deployment: MySQL + API + UI.
