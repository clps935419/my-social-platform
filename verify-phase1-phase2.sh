#!/bin/bash

# Verification script for Phase 1 & Phase 2 implementation
# This script checks that all required files are in place

echo "🔍 Verifying Phase 1 & Phase 2 Implementation..."
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1"
        return 0
    else
        echo -e "${RED}✗${NC} $1 (MISSING)"
        return 1
    fi
}

check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $1/"
        return 0
    else
        echo -e "${RED}✗${NC} $1/ (MISSING)"
        return 1
    fi
}

FAILED=0

echo "📁 Directory Structure:"
check_dir "DB" || FAILED=1
check_dir "backend" || FAILED=1
check_dir "frontend" || FAILED=1
check_dir "nginx" || FAILED=1
check_dir "docs" || FAILED=1
echo ""

echo "🗄️  Database Scripts:"
check_file "DB/README.md" || FAILED=1
check_file "DB/001_schema.sql" || FAILED=1
check_file "DB/010_tables.sql" || FAILED=1
check_file "DB/020_indexes.sql" || FAILED=1
check_file "DB/030_seed.sql" || FAILED=1
echo ""

echo "🐳 Docker Configuration:"
check_file "docker-compose.yml" || FAILED=1
check_file "backend/Dockerfile" || FAILED=1
check_file ".env.example" || FAILED=1
check_file ".dockerignore" || FAILED=1
echo ""

echo "🌐 Nginx Configuration:"
check_file "nginx/default.conf" || FAILED=1
echo ""

echo "☕ Backend Files:"
check_file "backend/pom.xml" || FAILED=1
check_file "backend/src/main/java/com/example/platform/SocialPlatformApplication.java" || FAILED=1
check_file "backend/src/main/resources/application.yml" || FAILED=1
check_file "backend/src/main/java/com/example/platform/api/HealthController.java" || FAILED=1
check_file "backend/src/main/java/com/example/platform/common/ErrorResponse.java" || FAILED=1
check_file "backend/src/main/java/com/example/platform/common/GlobalExceptionHandler.java" || FAILED=1
check_file "backend/src/main/java/com/example/platform/dao/StoredProcedureExecutor.java" || FAILED=1
check_file "backend/src/main/java/com/example/platform/api/validation/RequestValidators.java" || FAILED=1
echo ""

echo "⚛️  Frontend Files:"
check_file "frontend/package.json" || FAILED=1
check_file "frontend/vite.config.js" || FAILED=1
check_file "frontend/index.html" || FAILED=1
check_file "frontend/src/main.js" || FAILED=1
check_file "frontend/src/App.vue" || FAILED=1
check_file "frontend/src/api/http.ts" || FAILED=1
echo ""

echo "🛠️  Development Tools:"
check_file "Makefile" || FAILED=1
check_file "biome.json" || FAILED=1
check_file ".gitignore" || FAILED=1
check_file ".eslintignore" || FAILED=1
echo ""

echo "📝 Documentation:"
check_file "IMPLEMENTATION_SUMMARY.md" || FAILED=1
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ All Phase 1 & Phase 2 files verified successfully!${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. Copy .env.example to .env and adjust values if needed"
    echo "  2. Run 'make up' to start all services"
    echo "  3. Check 'make health' to verify services are running"
    echo "  4. Access Swagger UI at http://localhost/api/swagger-ui/index.html"
    exit 0
else
    echo -e "${RED}❌ Some files are missing. Please check the implementation.${NC}"
    exit 1
fi
