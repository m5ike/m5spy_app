# My Spy - Makefile
# @author Michael KOJDL
# @version 2.0.0

.PHONY: help build up down restart logs shell migrate createsuperuser collectstatic backup clean deploy test

# Default target
.DEFAULT_GOAL := help

help: ## Show this help message
	@echo "My Spy - Docker Management Commands"
	@echo "===================================="
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build all Docker containers
	docker-compose build

up: ## Start all services
	docker-compose up -d

down: ## Stop all services
	docker-compose down

restart: ## Restart all services
	docker-compose restart

logs: ## View logs (all services)
	docker-compose logs -f

logs-webapp: ## View webapp logs
	docker-compose logs -f webapp

logs-celery: ## View celery worker logs
	docker-compose logs -f celery_worker

shell: ## Open Django shell
	docker-compose exec webapp python manage.py shell

bash: ## Open bash in webapp container
	docker-compose exec webapp /bin/bash

mysql: ## Open MySQL shell
	docker-compose exec mysql mysql -u root -p

redis-cli: ## Open Redis CLI
	docker-compose exec redis redis-cli

migrate: ## Run database migrations
	docker-compose exec webapp python manage.py migrate

makemigrations: ## Create new migrations
	docker-compose exec webapp python manage.py makemigrations

createsuperuser: ## Create Django superuser
	docker-compose exec webapp python manage.py createsuperuser

collectstatic: ## Collect static files
	docker-compose exec webapp python manage.py collectstatic --noinput

backup: ## Run backup script
	@chmod +x docker/scripts/backup.sh
	@./docker/scripts/backup.sh

deploy: ## Deploy application (first time or update)
	@chmod +x docker/scripts/deploy.sh
	@./docker/scripts/deploy.sh

clean: ## Remove all containers, volumes and images
	docker-compose down -v
	docker system prune -af

status: ## Show containers status
	docker-compose ps

test: ## Run tests
	docker-compose exec webapp python manage.py test

check: ## Run Django checks
	docker-compose exec webapp python manage.py check --deploy

dbshell: ## Open database shell
	docker-compose exec webapp python manage.py dbshell

fixtures-load: ## Load fixtures (e.g., make fixtures-load FILE=users.json)
	docker-compose exec webapp python manage.py loaddata $(FILE)

fixtures-dump: ## Dump fixtures (e.g., make fixtures-dump APP=users)
	docker-compose exec webapp python manage.py dumpdata $(APP) --indent 2

rebuild: ## Rebuild and restart services
	docker-compose down
	docker-compose build --no-cache
	docker-compose up -d

scale-celery: ## Scale celery workers (e.g., make scale-celery N=3)
	docker-compose up -d --scale celery_worker=$(N)

ps: ## Show running containers
	docker-compose ps

top: ## Show running processes
	docker-compose top

stats: ## Show container stats
	docker stats

volumes: ## List volumes
	docker volume ls

networks: ## List networks
	docker network ls
