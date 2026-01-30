# 🚀 Deployment Instructions

## Quick Start Guide for Phase 1 & Phase 2

**Status**: ✅ All code complete and ready for deployment

---

## 📋 Prerequisites

Before starting, ensure you have:
- ✅ Docker Desktop installed (with Docker Compose)
- ✅ Internet connection (required for Maven and npm dependencies)
- ✅ Ports 80, 5432, and 8080 available

---

## 🎯 Step-by-Step Deployment

### 1. Clone and Setup

```bash
# Navigate to project directory
cd /path/to/my-social-platform

# Copy environment template
cp .env.example .env

# (Optional) Modify .env with your settings
# nano .env
```

### 2. Start All Services

```bash
# Using Makefile (recommended)
make up

# Or using docker compose directly
docker compose up -d --build
```

**First Run Time**: 3-5 minutes (downloads dependencies)
- Maven dependencies: ~200MB
- npm packages: ~150MB
- Docker images build time

### 3. Verify Services

```bash
# Check service status
make ps

# Or
docker compose ps

# Expected output:
# NAME                   IMAGE                    STATUS
# social-platform-db     postgres:16-alpine       Up (healthy)
# social-platform-app    my-social-platform-app   Up (healthy)
# social-platform-nginx  nginx:1.25-alpine        Up (healthy)
```

### 4. Check Health

```bash
# Using Makefile
make health

# Or manually
curl http://localhost:8080/api/health
curl http://localhost/health
```

**Expected Response**:
```json
{
  "status": "UP",
  "timestamp": "2026-01-30T10:00:00Z"
}
```

---

## 🌐 Access Points

Once all services are running:

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost/ | Vue 3 application (placeholder) |
| **Backend Health** | http://localhost/api/health | Backend health check |
| **Swagger UI** | http://localhost/api/swagger-ui/index.html | 🎯 **API Documentation** |
| **API Base** | http://localhost/api | Backend API base path |
| **Database** | localhost:5432 | PostgreSQL (credentials in .env) |

---

## 🔍 Verify Swagger UI

### Expected Result:
1. Open browser: http://localhost/api/swagger-ui/index.html
2. You should see: **Swagger UI** page with API documentation
3. Available endpoints:
   - `GET /health` - Health check endpoint
   - (More endpoints will be added in Phase 3+)

### Test Health Endpoint:
1. In Swagger UI, expand `GET /health`
2. Click "Try it out"
3. Click "Execute"
4. Expected response: `200 OK` with `{"status":"UP","timestamp":"..."}`

---

## 📊 Verify Database

```bash
# Connect to database
docker exec -it social-platform-db psql -U postgres -d social_platform

# List tables
\dt

# Expected tables:
# - users
# - posts
# - comments
# - refresh_tokens

# Check seed data
SELECT count(*) FROM users;    -- Expected: 2
SELECT count(*) FROM posts;    -- Expected: 3
SELECT count(*) FROM comments; -- Expected: 6

# Exit
\q
```

---

## 🛠️ Development Commands

### Service Management

```bash
# Start services
make up                 # Start all services
make dev                # Rebuild and start

# Monitor
make logs               # Follow logs from all services
make ps                 # Show service status
make health             # Check health of all services

# Stop services
make restart            # Restart all services
make down               # Stop all services
make clean              # Stop and remove everything
```

### Frontend Development

```bash
# Install dependencies
cd frontend
npm install

# Start dev server (hot reload)
npm run dev
# Access at: http://localhost:5173

# Build for production
npm run build
# Output: frontend/dist/
```

### Backend Development

The backend runs in Docker with hot reload enabled:
1. Edit files in `backend/src/`
2. Changes automatically trigger rebuild
3. Application restarts automatically
4. No need to restart Docker container

---

## 🔧 Troubleshooting

### Services Not Starting

```bash
# Check logs
docker compose logs

# Check specific service
docker compose logs app
docker compose logs db
docker compose logs nginx

# Restart specific service
docker compose restart app
```

### Port Already in Use

```bash
# Check what's using port 80
lsof -i :80

# Change port in .env
NGINX_PORT=8080

# Restart
make down && make up
```

### Backend Not Connecting to Database

```bash
# Check database health
docker compose exec db pg_isready -U postgres

# Check database logs
docker compose logs db

# Verify environment variables
docker compose exec app env | grep SPRING_DATASOURCE
```

### Swagger UI Not Loading

```bash
# Check backend is running
curl http://localhost:8080/api/health

# Check nginx is proxying correctly
curl http://localhost/api/health

# Check nginx logs
docker compose logs nginx

# Common issue: Backend not started yet
# Wait 30 seconds after `docker compose up` for backend to start
```

---

## 📝 Database Access

### Using psql

```bash
# Connect to database
docker compose exec db psql -U postgres -d social_platform

# Common queries
\dt                                 # List tables
\d users                            # Describe users table
SELECT * FROM users;                # View all users
SELECT * FROM posts WHERE deleted_at IS NULL;  # View active posts
```

### Using External Tool

- **Host**: localhost
- **Port**: 5432
- **Database**: social_platform
- **Username**: postgres
- **Password**: postgres (from .env)

---

## 🔒 Security Notes

### For Production Deployment:

1. **Change All Secrets**:
   ```bash
   # Edit .env
   JWT_SECRET=your-secure-random-secret-min-32-chars
   POSTGRES_PASSWORD=strong-database-password
   ```

2. **Enable HTTPS**:
   - Add SSL certificate to Nginx
   - Update `nginx/default.conf`

3. **Restrict Database Access**:
   - Remove database port exposure in `docker-compose.yml`
   - Or bind to localhost only: `127.0.0.1:5432:5432`

4. **Update CORS Settings** (if needed):
   - Configure in `application.yml`

5. **Enable Rate Limiting**:
   - Already prepared in code
   - Configure limits in `application.yml`

---

## 🧪 Testing

### Manual API Testing

**Using curl**:
```bash
# Health check
curl http://localhost/api/health

# Expected response:
# {"status":"UP","timestamp":"2026-01-30T10:00:00Z"}
```

**Using .http files** (VS Code REST Client):
```http
### Health Check
GET http://localhost/api/health
```

---

## 📦 What's Included

### Phase 1 (Setup) - ✅ Complete
- ✅ Docker Compose with 3 services
- ✅ Nginx reverse proxy
- ✅ Database initialization
- ✅ Backend skeleton (Spring Boot)
- ✅ Frontend skeleton (Vue 3)
- ✅ Development tools (Makefile, Biome)

### Phase 2 (Foundational) - ✅ Complete
- ✅ Database schema (4 tables, indexes)
- ✅ Seed data (2 users, 3 posts, 6 comments)
- ✅ Error handling (no stack trace leakage)
- ✅ Stored Procedure executor
- ✅ Input validation (E.164, pagination, URLs)
- ✅ Swagger UI configuration
- ✅ Hot reload for development
- ✅ Health API endpoint
- ✅ Frontend API client
- ✅ TanStack Query setup

---

## 🎯 Next Steps

### Phase 3: User Story 1 - Browse Posts
Implement browsing functionality:
- List posts (newest first)
- View post details
- List comments
- Pagination support

### Phase 4: User Story 2 - Authentication
Implement user authentication:
- Phone number registration
- Login with phone + password
- JWT token management
- Refresh token rotation

---

## 📞 Need Help?

### Check Status
```bash
make ps        # Service status
make logs      # View logs
make health    # Health checks
```

### Common Issues

1. **"connection refused"** → Backend not started yet (wait 30s)
2. **"port already in use"** → Change port in .env
3. **"database not ready"** → Wait for database health check
4. **"404 on /api"** → Nginx not proxying correctly

### Verify Everything
```bash
# Run verification script
bash verify-phase1-phase2.sh
```

---

## ✅ Success Checklist

After deployment, verify:

- [ ] `make ps` shows all services as "Up (healthy)"
- [ ] `curl http://localhost/api/health` returns `{"status":"UP",...}`
- [ ] http://localhost/api/swagger-ui/index.html loads in browser
- [ ] Database has 4 tables: `docker compose exec db psql -U postgres -d social_platform -c "\dt"`
- [ ] Seed data exists: 2 users, 3 posts, 6 comments
- [ ] Frontend loads at http://localhost/
- [ ] Logs show no errors: `make logs`

---

**🎉 Congratulations!** 

Your social media platform foundation is up and running!

The system is now ready for Phase 3 implementation (User Stories).

---

**Last Updated**: 2026-01-30  
**Branch**: copilot/process-phase1-phase2  
**Implementation**: Phase 1 & Phase 2 Complete
