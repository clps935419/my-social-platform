.PHONY: help build up down restart logs ps clean install-frontend build-frontend dev production production-frontend production-down

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build all Docker images
	docker compose build

up: ## Start all services (detached)
	docker compose up -d

down: ## Stop all services
	docker compose down

restart: ## Restart all services
	docker compose restart

logs: ## Show logs from all services (follow mode)
	docker compose logs -f

ps: ## Show status of all services
	docker compose ps

clean: ## Stop and remove all containers, networks, and volumes
	docker compose down -v
	rm -rf frontend/node_modules frontend/dist
	rm -rf backend/target

install-frontend: ## Install frontend dependencies
	cd frontend && npm install

build-frontend: install-frontend ## Build frontend for production
	cd frontend && npm run build

dev: ## Start services and rebuild
	docker compose up -d --build

volumes-reset: ## Recreate volumes and start services
	docker compose down -v
	docker compose up -d

rebuild: clean build up ## Clean rebuild and start

health: ## Check health of all services
	@echo "Checking database..."
	@docker compose exec db pg_isready -U postgres || echo "Database not ready"
	@echo "Checking backend..."
	@curl -f http://localhost:8080/api/health || echo "Backend not ready"
	@echo "Checking nginx..."
	@curl -f http://localhost/health || echo "Nginx not ready"

production: ## Start production deployment (requires only Docker, no Node/Maven)
	@echo "Building backend JAR..."
	@docker run --rm -v "$(PWD)/backend:/build" -w /build maven:3.9-eclipse-temurin-17-alpine mvn clean package -DskipTests -B || \
		(echo "WARNING: Maven build in Docker failed. Trying with local backend/target if exists..." && test -f backend/target/*.jar)
	@echo "Starting production services..."
	docker compose -f docker-compose.prod.yml up -d --build

production-frontend: ## Start production frontend only (nginx serving dist)
	@echo "Starting frontend-only production (nginx + frontend/dist)..."
	docker run -d --name social-platform-nginx-prod \
		-p 80:80 \
		-v "$(PWD)/nginx/default.conf:/etc/nginx/conf.d/default.conf:ro" \
		-v "$(PWD)/frontend/dist:/usr/share/nginx/html:ro" \
		nginx:1.25-alpine
	@echo "✅ Frontend available at http://localhost/"
	@echo "To stop: docker stop social-platform-nginx-prod && docker rm social-platform-nginx-prod"

production-down: ## Stop production deployment
	docker compose -f docker-compose.prod.yml down
