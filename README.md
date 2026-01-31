# Social Media Platform

A simple social media platform with phone number authentication, posts, and comments. Built with Spring Boot, Vue 3, and PostgreSQL.

## 🏗️ Architecture

- **Frontend**: Vue 3 + Vite + TypeScript + TanStack Query
- **Backend**: Spring Boot 3 (Java 17) + JdbcTemplate + Stored Procedures
- **Database**: PostgreSQL 16
- **Web Server**: Nginx (reverse proxy + static files)
- **Deployment**: Docker Compose

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- (Optional) Node.js 20+ for local frontend development
- (Optional) Java 17+ & Maven for local backend development

### Development Mode

#### 1. Start All Services

```bash
# Copy environment template
cp .env.example .env

# Start services (this will take a few minutes on first run)
make up

# Or use docker compose directly
docker compose up -d --build
```

#### 2. Check Service Health

```bash
make health
```

#### 3. Access the Application

- **Frontend**: http://localhost/
- **Backend API**: http://localhost/api/health
- **Swagger UI** (for developers): http://localhost:8080/api/swagger-ui/index.html
  - Access directly via backend port (bypasses nginx)
  - Not exposed through reverse proxy
- **Database**: localhost:5432

### Production Mode (T082)

**One-command deployment** - requires only Docker, no Node.js or Maven needed:

```bash
# Copy environment template (if not already done)
cp .env.example .env

# Start production services
make production
```

**What's different in production:**
- ✅ Uses pre-built frontend (`frontend/dist` is committed)
- ✅ Backend is compiled to JAR (multi-stage build)
- ✅ Faster startup (no dependency downloads)
- ✅ No source code volume mounts
- ✅ Runs with `java -jar` (not `mvn spring-boot:run`)

**Access Points** (same as development):
- **Frontend**: http://localhost/
- **Backend API**: http://localhost/api/health

**Stop production:**
```bash
make production-down
```

## 🛠️ Development

### Make Commands

#### Development Commands
```bash
make help        # Show all available commands
make up          # Start all services
make down        # Stop all services
make restart     # Restart all services
make logs        # Show logs (follow mode)
make ps          # Show service status
make clean       # Stop and remove everything
make health      # Check service health
```

#### Production Commands
```bash
make production       # Start production deployment (one-command, Docker only)
make production-down  # Stop production deployment
```

### Local Development

#### Frontend
```bash
cd frontend
npm install
npm run dev      # Dev server on http://localhost:5173
npm run build    # Build for production
npm run gen:sdk  # Generate API SDK from backend OpenAPI spec
```

##### TypeScript Conventions

- Frontend uses TypeScript only (`.ts` and `<script lang="ts">` in `.vue`).
- Path alias: `@` maps to `frontend/src/`.
- Prefer absolute imports with `@/` and avoid deep relative imports (e.g., `../../`).

##### SDK Generation Workflow

**Automatic SDK Generation**:
The frontend uses [@hey-api/openapi-ts](https://github.com/hey-api/openapi-ts) to generate TypeScript API client from backend's OpenAPI specification.

**How to regenerate SDK**:
```bash
# 1. Ensure backend is running (API docs must be accessible)
docker compose up -d app

# 2. Generate SDK
cd frontend
npm run gen:sdk
```

**Important Notes**:
- **Generated code location**: `frontend/src/api/generated/`
- **DO NOT manually edit generated files** - they will be overwritten on next generation
- **Generated code is NOT committed to git** - it's treated as build artifact
- **Regenerate SDK whenever backend API changes** - keep frontend in sync
- SDK uses `@tanstack/vue-query` for data fetching and `axios` for HTTP client

#### Backend
```bash
cd backend
mvn spring-boot:run  # Requires Java 17+
```

### Hot Reload

Both frontend and backend support hot reload in Docker:
- **Frontend**: Changes to `frontend/src/` trigger rebuild
- **Backend**: Changes to `backend/src/` trigger auto-restart (via spring-boot-devtools)

## 📁 Project Structure

```
├── DB/                  # Database initialization scripts
│   ├── 001_schema.sql   # Schema and extensions
│   ├── 010_tables.sql   # Core tables
│   ├── 020_indexes.sql  # Indexes and constraints
│   └── 030_seed.sql     # Seed data
├── backend/             # Spring Boot application
│   ├── src/main/java/
│   │   └── com/example/platform/
│   │       ├── api/              # REST controllers
│   │       ├── service/          # Business logic
│   │       ├── dao/              # Data access (SP calls)
│   │       ├── security/         # Auth & JWT
│   │       └── common/           # Error handling
│   └── pom.xml
├── frontend/            # Vue 3 application
│   ├── src/
│   │   ├── pages/      # Vue pages
│   │   ├── components/ # Vue components
│   │   └── api/        # API client
│   └── package.json
├── nginx/              # Nginx configuration
│   └── default.conf
└── docker-compose.yml
```

## 🔒 Security Features

- **SP-First Architecture**: All database access via stored procedures (no SQL injection)
- **Parameterized Queries**: Only SimpleJdbcCall with named parameters
- **No Stack Traces**: Error responses never leak internal details
- **E.164 Phone Validation**: Consistent phone number format
- **JWT Authentication**: Stateless access tokens + refresh token rotation
- **Password Hashing**: Salt + hash (PBKDF2/BCrypt)
- **Rate Limiting**: Protection against brute force attacks
- **XSS Prevention**: No unsafe rendering in frontend

## 📋 Features (Roadmap)

### ✅ Phase 1 & 2: Foundation (Completed)
- [x] Project setup with Docker Compose
- [x] Database schema with stored procedures
- [x] Backend error handling framework
- [x] Frontend API client with TanStack Query
- [x] Health check endpoint

### 🚧 Phase 3: User Story 1 - Browse Posts (Next)
- [ ] List posts (newest first)
- [ ] View post comments
- [ ] Pagination support

### 📅 Phase 4: User Story 2 - Authentication
- [ ] Phone number registration
- [ ] Login with JWT
- [ ] Refresh token rotation
- [ ] Get user profile

### 📅 Phase 5: User Story 3 - Post Management
- [ ] Create post
- [ ] Update post (author only)
- [ ] Soft delete post (author only)

### 📅 Phase 6: User Story 4 - Comments
- [ ] Add comment to post
- [ ] View comments (all users)

## 🧪 Testing

### API Testing

Use the provided acceptance scripts:
```bash
# Coming in Phase 3
./docs/us1-acceptance.http
```

Or use curl:
```bash
# Health check
curl http://localhost/api/health

# List posts (after US1 implementation)
curl http://localhost/api/posts?limit=10&offset=0
```

### Database Testing

Connect to PostgreSQL:
```bash
docker compose exec db psql -U postgres -d social_platform
```

## 📝 Implementation Status

See [IMPLEMENTATION_SUMMARY.md](./reports/IMPLEMENTATION_SUMMARY.md) for detailed progress.

**Completed Tasks**: 20/20 (Phase 1 & 2)
- Phase 1 (Setup): 9/9 ✅
- Phase 2 (Foundation): 11/11 ✅

## 🔧 Configuration

### Environment Variables

Edit `.env` to customize:
```bash
# Database
POSTGRES_DB=social_platform
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_PORT=5432

# JWT
JWT_SECRET=your-secret-here-min-32-chars
JWT_EXPIRATION_SECONDS=3600

# Refresh Token
REFRESH_TOKEN_EXPIRATION_SECONDS=2592000

# Nginx
NGINX_PORT=80
```

## 📚 Documentation

- [Implementation Summary](./reports/IMPLEMENTATION_SUMMARY.md) - Detailed implementation report
- [Deployment Instructions](./reports/DEPLOYMENT_INSTRUCTIONS.md) - Complete deployment guide
- [Phase 1 & 2 Status](./reports/PHASE1_PHASE2_STATUS.md) - Detailed task completion status
- [Specification](./specs/001-social-platform/spec.md) - Feature requirements
- [Technical Plan](./specs/001-social-platform/plan.md) - Architecture decisions
- [Data Model](./specs/001-social-platform/data-model.md) - Database schema
- [API Contract](./specs/001-social-platform/contracts/openapi.yaml) - OpenAPI spec

## 🐛 Troubleshooting

### Cannot connect to API through nginx (502/503 errors)

The reverse proxy requires the backend service to be running. Check:

```bash
# 1. Verify all services are running
docker compose ps

# 2. Check backend logs
docker compose logs app

# 3. Test backend directly (bypassing nginx)
curl http://localhost:8080/api/health

# 4. Check nginx logs
docker compose logs nginx

# 5. Restart services in order
docker compose restart app
docker compose restart nginx
```

**Common causes:**
- Backend is still starting (Maven downloading dependencies on first run - can take 3-5 minutes)
- Backend failed to start (check logs for errors)
- Network connectivity issues between containers

### Services won't start
```bash
# Check logs
make logs

# Check status
make ps

# Clean restart
make clean
make up
```

### Database connection issues
```bash
# Wait for database to be ready
docker compose exec db pg_isready -U postgres

# Check database logs
docker compose logs db
```

### Port conflicts
```bash
# Change ports in .env
POSTGRES_PORT=5433
NGINX_PORT=8000
```

### Backend takes long time to start

On first run, Maven needs to download all dependencies (Spring Boot, PostgreSQL driver, etc.). This is normal and can take 3-5 minutes depending on your internet connection.

```bash
# Monitor backend startup
docker compose logs -f app

# You should see Maven downloading dependencies, then Spring Boot starting
```

## 📄 License

This is a demo project for educational purposes.

## 🤝 Contributing

This is a reference implementation. Feel free to use it as a starting point for your own projects.

## 📧 Contact

For questions or feedback, please refer to the project documentation in the `specs/` directory.
