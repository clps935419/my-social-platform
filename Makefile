.PHONY: help build up down restart logs ps clean install-frontend build-frontend dev

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
