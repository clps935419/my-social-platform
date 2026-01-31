.PHONY: help dev logs production volumes-reset clean production-down

ifeq ($(OS),Windows_NT)
SHELL := powershell.exe
.SHELLFLAGS := -NoProfile -Command
ENV_CHECK = if (-not (Test-Path ".env")) { Write-Error "ERROR: .env not found. Please copy .env.example to .env first."; exit 1 }
BUILD_BACKEND = docker run --rm -v "$(CURDIR)/backend:/build" -w /build maven:3.9-eclipse-temurin-17-alpine mvn clean package -DskipTests -B; if ($$LASTEXITCODE -ne 0) { Write-Warning "WARNING: Maven build in Docker failed. Trying with local backend/target if exists..."; if (-not (Test-Path "backend/target/*.jar")) { exit 1 } }
DOWN_CMD = docker compose down
DOWN_V_CMD = docker compose down -v
UP_CMD = docker compose up -d
else
ENV_CHECK = test -f .env || (echo "ERROR: .env not found. Please copy .env.example to .env first." && exit 1)
BUILD_BACKEND = docker run --rm -v "$(CURDIR)/backend:/build" -w /build maven:3.9-eclipse-temurin-17-alpine mvn clean package -DskipTests -B || \
		(echo "WARNING: Maven build in Docker failed. Trying with local backend/target if exists..." && test -f backend/target/*.jar)
DOWN_CMD = docker compose down
DOWN_V_CMD = docker compose down -v
UP_CMD = docker compose up -d
endif

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

dev: ## Start services (build if needed)
	@$(ENV_CHECK)
	docker compose up -d --build

logs: ## Show logs from all services (follow mode)
	docker compose logs -f

production: ## Start production deployment (requires only Docker, no Node/Maven)
	@$(ENV_CHECK)
	@echo "Building backend JAR..."
	@$(BUILD_BACKEND)
	@echo "Starting production services..."
	docker compose -f docker-compose.prod.yml up -d --build

volumes-reset: ## Recreate volumes and start services
	$(DOWN_V_CMD)
	$(UP_CMD)

clean: ## Stop and remove all containers
	$(DOWN_CMD)

production-down: ## Stop production deployment
	docker compose -f docker-compose.prod.yml down
