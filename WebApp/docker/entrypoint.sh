#!/bin/bash
# My Spy - Docker Entrypoint Script
# @author Michael KOJDL
# @version 2.0.0

set -e

echo "==================================================="
echo "My Spy - Django Application Starting..."
echo "==================================================="

# Wait for MySQL to be ready
echo "Waiting for MySQL database..."
until nc -z -v -w30 $DB_HOST ${DB_PORT:-3306}
do
  echo "Waiting for MySQL connection on $DB_HOST:${DB_PORT:-3306}..."
  sleep 2
done
echo "✓ MySQL is up and running!"

# Wait for Redis to be ready
echo "Waiting for Redis..."
until nc -z -v -w30 $REDIS_HOST ${REDIS_PORT:-6379}
do
  echo "Waiting for Redis connection on $REDIS_HOST:${REDIS_PORT:-6379}..."
  sleep 2
done
echo "✓ Redis is up and running!"

# Wait for RabbitMQ to be ready (if configured)
if [ ! -z "$RABBITMQ_HOST" ]; then
    echo "Waiting for RabbitMQ..."
    until nc -z -v -w30 $RABBITMQ_HOST ${RABBITMQ_PORT:-5672}
    do
      echo "Waiting for RabbitMQ connection on $RABBITMQ_HOST:${RABBITMQ_PORT:-5672}..."
      sleep 2
    done
    echo "✓ RabbitMQ is up and running!"
fi

echo "---------------------------------------------------"

# Collect static files
echo "Collecting static files..."
python manage.py collectstatic --noinput --clear || echo "Warning: collectstatic failed"
echo "✓ Static files collected"

# Apply database migrations
echo "Applying database migrations..."
python manage.py migrate --noinput || {
    echo "ERROR: Database migration failed!"
    exit 1
}
echo "✓ Migrations applied successfully"

# Create superuser if it doesn't exist (only if variables are set)
if [ ! -z "$DJANGO_SUPERUSER_USERNAME" ] && [ ! -z "$DJANGO_SUPERUSER_PASSWORD" ] && [ ! -z "$DJANGO_SUPERUSER_EMAIL" ]; then
    echo "Creating superuser..."
    python manage.py shell << EOF
from apps.users.models import User
if not User.objects.filter(username='$DJANGO_SUPERUSER_USERNAME').exists():
    User.objects.create_superuser('$DJANGO_SUPERUSER_USERNAME', '$DJANGO_SUPERUSER_EMAIL', '$DJANGO_SUPERUSER_PASSWORD', role='ADMIN')
    print('✓ Superuser created successfully')
else:
    print('✓ Superuser already exists')
EOF
fi

echo "==================================================="
echo "All initialization steps completed successfully!"
echo "Starting application..."
echo "==================================================="

# Execute the main command
exec "$@"
