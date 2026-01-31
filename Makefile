.PHONY: help dev logs production volumes-reset clean production-down

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

dev: ## Start services (build if needed)
	@test -f .env || (echo "ERROR: .env not found. Please copy .env.example to .env first." && exit 1)
	docker compose up -d --build

logs: ## Show logs from all services (follow mode)
	docker compose logs -f

production: ## Start production deployment (requires only Docker, no Node/Maven)
	@test -f .env || (echo "ERROR: .env not found. Please copy .env.example to .env first." && exit 1)
	@echo "Building backend JAR..."
	@docker run --rm -v "$(PWD)/backend:/build" -w /build maven:3.9-eclipse-temurin-17-alpine mvn clean package -DskipTests -B || \
		(echo "WARNING: Maven build in Docker failed. Trying with local backend/target if exists..." && test -f backend/target/*.jar)
	@echo "Starting production services..."
	docker compose -f docker-compose.prod.yml up -d --build

volumes-reset: ## Recreate volumes and start services
	docker compose down -v
	docker compose up -d

clean: ## Stop and remove all containers, networks, and volumes
	docker compose down -v
	rm -rf frontend/node_modules frontend/dist
	rm -rf backend/target

production-down: ## Stop production deployment
	docker compose -f docker-compose.prod.yml down
