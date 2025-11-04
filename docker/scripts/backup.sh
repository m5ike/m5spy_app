#!/bin/bash
# My Spy - Backup Script
# @author Michael KOJDL
# @version 2.0.0

set -e

# Configuration
BACKUP_DIR="${BACKUP_DIR:-./backups}"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=7

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "========================================="
echo "My Spy - Backup Script"
echo "========================================="

# Create backup directory
mkdir -p $BACKUP_DIR

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

echo ""
echo "Backup started: $DATE"
echo "Backup directory: $BACKUP_DIR"
echo ""

# 1. Database Backup
echo "1. Backing up MySQL database..."
docker-compose exec -T mysql mysqldump \
    -u root \
    -p${DB_ROOT_PASSWORD} \
    --single-transaction \
    --quick \
    --lock-tables=false \
    ${DB_NAME} | gzip > ${BACKUP_DIR}/db_backup_${DATE}.sql.gz

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Database backup completed${NC}"
    DB_SIZE=$(du -h ${BACKUP_DIR}/db_backup_${DATE}.sql.gz | cut -f1)
    echo "  Size: $DB_SIZE"
else
    echo -e "${RED}✗ Database backup failed${NC}"
fi

# 2. Media Files Backup
echo ""
echo "2. Backing up media files..."
docker cp myspy_webapp:/app/mediafiles ${BACKUP_DIR}/media_${DATE}
tar -czf ${BACKUP_DIR}/media_${DATE}.tar.gz -C ${BACKUP_DIR} media_${DATE}
rm -rf ${BACKUP_DIR}/media_${DATE}

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Media files backup completed${NC}"
    MEDIA_SIZE=$(du -h ${BACKUP_DIR}/media_${DATE}.tar.gz | cut -f1)
    echo "  Size: $MEDIA_SIZE"
else
    echo -e "${RED}✗ Media files backup failed${NC}"
fi

# 3. Static Files Backup (optional)
echo ""
echo "3. Backing up static files..."
docker cp myspy_webapp:/app/staticfiles ${BACKUP_DIR}/static_${DATE}
tar -czf ${BACKUP_DIR}/static_${DATE}.tar.gz -C ${BACKUP_DIR} static_${DATE}
rm -rf ${BACKUP_DIR}/static_${DATE}

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Static files backup completed${NC}"
else
    echo -e "${YELLOW}⚠ Static files backup skipped or failed${NC}"
fi

# 4. Application Logs Backup
echo ""
echo "4. Backing up application logs..."
docker-compose logs --no-color > ${BACKUP_DIR}/logs_${DATE}.txt 2>&1
gzip ${BACKUP_DIR}/logs_${DATE}.txt

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Logs backup completed${NC}"
else
    echo -e "${YELLOW}⚠ Logs backup failed${NC}"
fi

# 5. Redis Backup
echo ""
echo "5. Backing up Redis data..."
docker-compose exec -T redis redis-cli -a ${REDIS_PASSWORD} --rdb ${BACKUP_DIR}/redis_${DATE}.rdb SAVE
docker cp myspy_redis:/data/dump.rdb ${BACKUP_DIR}/redis_${DATE}.rdb 2>/dev/null || echo -e "${YELLOW}⚠ Redis backup skipped${NC}"

# 6. Configuration Backup
echo ""
echo "6. Backing up configuration..."
cp .env ${BACKUP_DIR}/env_${DATE}.backup
cp docker-compose.yml ${BACKUP_DIR}/docker-compose_${DATE}.yml
tar -czf ${BACKUP_DIR}/config_${DATE}.tar.gz -C ${BACKUP_DIR} env_${DATE}.backup docker-compose_${DATE}.yml
rm ${BACKUP_DIR}/env_${DATE}.backup ${BACKUP_DIR}/docker-compose_${DATE}.yml

echo -e "${GREEN}✓ Configuration backup completed${NC}"

# Clean old backups
echo ""
echo "7. Cleaning old backups (older than ${RETENTION_DAYS} days)..."
find ${BACKUP_DIR} -name "*.gz" -type f -mtime +${RETENTION_DAYS} -delete
find ${BACKUP_DIR} -name "*.sql" -type f -mtime +${RETENTION_DAYS} -delete
find ${BACKUP_DIR} -name "*.txt" -type f -mtime +${RETENTION_DAYS} -delete
find ${BACKUP_DIR} -name "*.rdb" -type f -mtime +${RETENTION_DAYS} -delete

echo -e "${GREEN}✓ Old backups cleaned${NC}"

# Summary
echo ""
echo "========================================="
echo -e "${GREEN}Backup completed successfully!${NC}"
echo "========================================="
echo ""
echo "Backup location: ${BACKUP_DIR}"
echo "Backup files:"
ls -lh ${BACKUP_DIR}/*${DATE}* 2>/dev/null || echo "No backup files created"
echo ""
echo "Total backup size:"
du -sh ${BACKUP_DIR}
echo ""
