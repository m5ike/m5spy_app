#!/bin/bash
# My Spy - Deployment Script
# @author Michael KOJDL
# @version 2.0.0

set -e

echo "========================================="
echo "My Spy - Deployment Script"
echo "========================================="

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if .env exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}Warning: .env file not found!${NC}"
    echo "Creating .env from .env.example..."
    cp .env.example .env
    echo -e "${GREEN}✓ Created .env file. Please edit it with your settings.${NC}"
    exit 1
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running!${NC}"
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}Error: Docker Compose is not installed!${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker and Docker Compose are available${NC}"
echo ""

# Build containers
echo "Building Docker containers..."
docker-compose build --no-cache

echo -e "${GREEN}✓ Containers built successfully${NC}"
echo ""

# Start services
echo "Starting services..."
docker-compose up -d

echo -e "${GREEN}✓ Services started${NC}"
echo ""

# Wait for services to be healthy
echo "Waiting for services to be healthy..."
sleep 10

# Check services
echo "Checking service status..."
docker-compose ps

echo ""
echo "Waiting for database to be ready..."
until docker-compose exec -T mysql mysqladmin ping -h localhost --silent 2>/dev/null; do
    echo "Waiting for MySQL..."
    sleep 2
done
echo -e "${GREEN}✓ MySQL is ready${NC}"

# Run migrations
echo ""
echo "Running database migrations..."
docker-compose exec -T webapp python manage.py migrate --noinput

echo -e "${GREEN}✓ Migrations completed${NC}"

# Collect static files
echo ""
echo "Collecting static files..."
docker-compose exec -T webapp python manage.py collectstatic --noinput --clear

echo -e "${GREEN}✓ Static files collected${NC}"

# Create superuser (if environment variables are set)
if grep -q "DJANGO_SUPERUSER_USERNAME" .env; then
    echo ""
    echo "Creating superuser..."
    docker-compose exec -T webapp python manage.py shell << EOF
from apps.users.models import User
import os
username = os.getenv('DJANGO_SUPERUSER_USERNAME', 'admin')
email = os.getenv('DJANGO_SUPERUSER_EMAIL', 'admin@example.com')
password = os.getenv('DJANGO_SUPERUSER_PASSWORD', 'admin123')
if not User.objects.filter(username=username).exists():
    User.objects.create_superuser(username, email, password, role='ADMIN')
    print('✓ Superuser created')
else:
    print('✓ Superuser already exists')
EOF
fi

echo ""
echo "========================================="
echo -e "${GREEN}Deployment completed successfully!${NC}"
echo "========================================="
echo ""
echo "Application URLs:"
echo "  - Web Application: http://localhost:8000"
echo "  - Admin Panel: http://localhost:8000/admin"
echo "  - API: http://localhost:8000/api/v1/"
echo "  - RabbitMQ Management: http://localhost:15672"
echo "  - Nginx: http://localhost:80"
echo ""
echo "Useful commands:"
echo "  - View logs: docker-compose logs -f"
echo "  - Stop services: docker-compose down"
echo "  - Restart: docker-compose restart"
echo ""
