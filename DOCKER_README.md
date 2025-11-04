# My Spy - Docker Deployment Guide

Complete Docker setup with separate containers for all services.

**Version:** 2.0.0
**Author:** Michael KOJDL
**Python:** 3.11
**Django:** 4.2.11 LTS

---

## 📦 Architecture

The application is deployed using Docker Compose with the following containers:

```
┌─────────────────────────────────────────────────────────────┐
│                         Nginx (Port 80/443)                  │
│                    Reverse Proxy & Static Files              │
└──────────────────────┬──────────────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
┌───────▼────────┐          ┌─────────▼──────────┐
│  Django WebApp │          │   Celery Workers   │
│   (Port 8000)  │          │   + Beat Scheduler │
└───────┬────────┘          └─────────┬──────────┘
        │                             │
        └──────────┬──────────────────┘
                   │
        ┌──────────┴──────────────┐
        │                         │
┌───────▼────────┐    ┌──────────▼─────────┐
│  MySQL 8.0     │    │    Redis 7         │
│  (Port 3306)   │    │   (Port 6379)      │
└────────────────┘    └────────────────────┘
                              │
                   ┌──────────▼─────────┐
                   │  RabbitMQ 3.12     │
                   │  (Port 5672/15672) │
                   └────────────────────┘
```

### Services:

1. **webapp** - Django application (Gunicorn)
2. **mysql** - MySQL 8.0 database
3. **redis** - Redis cache and session store
4. **rabbitmq** - Message broker for Celery
5. **celery_worker** - Async task execution
6. **celery_beat** - Periodic task scheduler
7. **nginx** - Reverse proxy and static files

---

## 🚀 Quick Start

### Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- 4GB RAM minimum
- 20GB disk space

### 1. Clone and Configure

```bash
# Clone the repository
cd m5spy_app

# Copy environment file
cp .env.example .env

# Edit .env with your settings
nano .env
```

### 2. Build and Start

```bash
# Build all containers
docker-compose build

# Start all services
docker-compose up -d

# Check status
docker-compose ps
```

### 3. Initialize Database

```bash
# Run migrations
docker-compose exec webapp python manage.py migrate

# Create superuser (optional if not using environment variables)
docker-compose exec webapp python manage.py createsuperuser

# Collect static files
docker-compose exec webapp python manage.py collectstatic --noinput
```

### 4. Access Application

- **Web Application:** http://localhost:8000
- **Admin Panel:** http://localhost:8000/admin
- **API:** http://localhost:8000/api/v1/
- **RabbitMQ Management:** http://localhost:15672 (user: myspy, password: from .env)
- **Nginx:** http://localhost:80

---

## ⚙️ Configuration

### Environment Variables

Key variables in `.env`:

```bash
# Django
SECRET_KEY=your-secret-key-here
DEBUG=False
ALLOWED_HOSTS=localhost,127.0.0.1

# Database
DB_NAME=myspy_db
DB_USER=myspy_user
DB_PASSWORD=strong_password_here
DB_ROOT_PASSWORD=root_strong_password

# Redis
REDIS_PASSWORD=redis_strong_password

# RabbitMQ
RABBITMQ_USER=myspy
RABBITMQ_PASSWORD=rabbitmq_strong_password
RABBITMQ_VHOST=myspy_vhost

# Superuser (created automatically on first run)
DJANGO_SUPERUSER_USERNAME=admin
DJANGO_SUPERUSER_EMAIL=admin@example.com
DJANGO_SUPERUSER_PASSWORD=AdminPassword123
```

### Security Checklist

- [ ] Change all default passwords
- [ ] Set strong `SECRET_KEY`
- [ ] Set `DEBUG=False` in production
- [ ] Configure `ALLOWED_HOSTS` properly
- [ ] Enable HTTPS (SSL/TLS)
- [ ] Configure firewall rules
- [ ] Regular backups

---

## 🔧 Management Commands

### Container Management

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Restart specific service
docker-compose restart webapp

# View logs
docker-compose logs -f webapp
docker-compose logs -f celery_worker

# Execute commands in container
docker-compose exec webapp python manage.py shell
docker-compose exec webapp python manage.py dbshell
```

### Django Commands

```bash
# Run migrations
docker-compose exec webapp python manage.py migrate

# Create superuser
docker-compose exec webapp python manage.py createsuperuser

# Collect static files
docker-compose exec webapp python manage.py collectstatic

# Check deployment
docker-compose exec webapp python manage.py check --deploy

# Database shell
docker-compose exec webapp python manage.py dbshell
```

### Database Operations

```bash
# MySQL shell
docker-compose exec mysql mysql -u root -p

# Backup database
docker-compose exec mysql mysqldump -u root -p myspy_db > backup.sql

# Restore database
docker-compose exec -T mysql mysql -u root -p myspy_db < backup.sql

# Copy database backup from container
docker cp myspy_mysql:/backup.sql ./backup.sql
```

### Redis Operations

```bash
# Redis CLI
docker-compose exec redis redis-cli -a your_redis_password

# Monitor Redis
docker-compose exec redis redis-cli -a your_redis_password monitor

# Check memory usage
docker-compose exec redis redis-cli -a your_redis_password INFO memory
```

---

## 📊 Monitoring

### Health Checks

All services have health checks configured:

```bash
# Check all services health
docker-compose ps

# View specific service health
docker inspect --format='{{.State.Health.Status}}' myspy_webapp
```

### Logs

```bash
# All logs
docker-compose logs

# Specific service
docker-compose logs webapp
docker-compose logs celery_worker

# Follow logs in real-time
docker-compose logs -f --tail=100 webapp

# Save logs to file
docker-compose logs > logs.txt
```

### Resource Usage

```bash
# Container stats
docker stats

# Disk usage
docker system df

# Clean up unused resources
docker system prune -a
```

---

## 🔄 Updates and Maintenance

### Update Application Code

```bash
# Pull latest code
git pull origin main

# Rebuild and restart
docker-compose down
docker-compose build --no-cache webapp
docker-compose up -d

# Run migrations
docker-compose exec webapp python manage.py migrate

# Collect static files
docker-compose exec webapp python manage.py collectstatic --noinput
```

### Database Migrations

```bash
# Create new migrations
docker-compose exec webapp python manage.py makemigrations

# Apply migrations
docker-compose exec webapp python manage.py migrate

# Show migrations status
docker-compose exec webapp python manage.py showmigrations
```

### Backup Strategy

```bash
#!/bin/bash
# backup.sh - Daily backup script

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"

# Database backup
docker-compose exec -T mysql mysqldump -u root -p${DB_ROOT_PASSWORD} myspy_db \
    | gzip > ${BACKUP_DIR}/db_backup_${DATE}.sql.gz

# Media files backup
docker cp myspy_webapp:/app/mediafiles ${BACKUP_DIR}/media_${DATE}

# Logs backup
docker-compose logs > ${BACKUP_DIR}/logs_${DATE}.txt

echo "Backup completed: ${DATE}"
```

---

## 🐛 Troubleshooting

### Common Issues

#### 1. Database Connection Refused

```bash
# Check MySQL is running
docker-compose ps mysql

# Check MySQL logs
docker-compose logs mysql

# Restart MySQL
docker-compose restart mysql

# Wait for MySQL to be ready
docker-compose exec webapp python manage.py wait_for_db
```

#### 2. Redis Connection Error

```bash
# Check Redis
docker-compose ps redis

# Test Redis connection
docker-compose exec redis redis-cli -a ${REDIS_PASSWORD} ping

# Restart Redis
docker-compose restart redis
```

#### 3. Celery Not Processing Tasks

```bash
# Check Celery worker logs
docker-compose logs celery_worker

# Check RabbitMQ
docker-compose logs rabbitmq

# Restart Celery
docker-compose restart celery_worker celery_beat
```

#### 4. Permission Denied Errors

```bash
# Fix permissions
docker-compose exec webapp chown -R myspy:myspy /app

# Or rebuild with correct permissions
docker-compose down
docker-compose build --no-cache webapp
docker-compose up -d
```

#### 5. Static Files Not Loading

```bash
# Collect static files
docker-compose exec webapp python manage.py collectstatic --clear --noinput

# Restart Nginx
docker-compose restart nginx
```

### Debug Mode

Enable debug mode temporarily:

```bash
# Set DEBUG=True in .env
echo "DEBUG=True" >> .env

# Restart webapp
docker-compose restart webapp

# Remember to disable after debugging!
```

---

## 🔐 Production Deployment

### SSL/TLS Configuration

1. Obtain SSL certificates (Let's Encrypt recommended)
2. Update `docker/nginx/conf.d/myspy.conf`
3. Uncomment HTTPS server block
4. Update `.env`:

```bash
SECURE_SSL_REDIRECT=True
SESSION_COOKIE_SECURE=True
CSRF_COOKIE_SECURE=True
SECURE_HSTS_SECONDS=31536000
```

### Performance Tuning

```bash
# Increase Gunicorn workers
# In docker-compose.yml, webapp service:
command: gunicorn config.wsgi:application --bind 0.0.0.0:8000 --workers 8

# Increase Celery concurrency
# In docker-compose.yml, celery_worker service:
command: celery -A config worker --loglevel=info --concurrency=4
```

### Scaling

```bash
# Scale Celery workers
docker-compose up -d --scale celery_worker=3

# Scale webapp (with load balancer)
docker-compose up -d --scale webapp=2
```

---

## 📝 API Testing

### Using curl

```bash
# Register device
curl -X POST http://localhost:8000/api/v1/register/ \
  -H "Content-Type: application/json" \
  -d '{
    "manufacturer": "Samsung",
    "model": "Galaxy S21",
    "serial": "ABC123456",
    "os": "Android",
    "os_version": "13",
    "imei": "123456789012345",
    "uuid": "550e8400-e29b-41d4-a716-446655440000"
  }'

# Upload SMS (with authentication)
curl -X POST http://localhost:8000/api/v1/sms/ \
  -H "Content-Type: application/json" \
  -H "X-Device-UUID: 550e8400-e29b-41d4-a716-446655440000" \
  -H "X-API-Key: your-device-api-key" \
  -d '[{
    "message_type": "INCOMING",
    "address": "+420123456789",
    "body": "Test message",
    "timestamp": "2025-11-04T10:00:00.000Z",
    "thread_id": 1
  }]'
```

---

## 📚 Additional Resources

- [Django Documentation](https://docs.djangoproject.com/)
- [Docker Documentation](https://docs.docker.com/)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [Redis Documentation](https://redis.io/documentation)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [Celery Documentation](https://docs.celeryproject.org/)

---

## 🆘 Support

For issues and questions:
- Check logs: `docker-compose logs`
- Review this documentation
- Check Docker and service health
- Verify environment configuration

---

**Last Updated:** 2025-11-04
**Version:** 2.0.0
