# My Spy - Server Konfigurace a Deployment

**Modul:** Server Infrastructure
**Autor:** Michael KOJDL
**Verze:** 1.0.0
**Poslední revize:** 2025-10-30
**OS:** Debian 12.12 / Ubuntu 22.04 LTS / Docker
**Podporované platformy:** Linux VPS, Docker, Bare Metal

---

## Obsah

1. [Přehled](#přehled)
2. [Požadavky](#požadavky)
3. [Instalace](#instalace)
4. [Konfigurace služeb](#konfigurace-služeb)
5. [Systemd Services](#systemd-services)
6. [Nginx/Apache2 Setup](#nginx-apache2-setup)
7. [SSL Certifikáty](#ssl-certifikáty)
8. [Docker Deployment](#docker-deployment)
9. [Backup & Restore](#backup--restore)
10. [Monitoring](#monitoring)
11. [Troubleshooting](#troubleshooting)

---

## Přehled

Serverová část My Spy zahrnuje:

- ✅ **Automatizovaný instalační script** - Bezproblémová instalace všech závislostí
- ✅ **Systemd Services** - Správa všech komponent jako system services
- ✅ **Web Server** - Nginx nebo Apache2 konfigurace
- ✅ **Database** - PostgreSQL setup a optimalizace
- ✅ **Cache Layer** - Redis + Memcached
- ✅ **Message Queue** - RabbitMQ pro Celery
- ✅ **SSL/TLS** - Let's Encrypt automatizace
- ✅ **Backup System** - Automatické zálohování
- ✅ **Monitoring** - Prometheus + Grafana ready

### Instalační struktura

```
/home/m5spy/
├── framework/          # Aplikační kód
│   └── m5spy_app/
│       ├── WebApp/
│       ├── MobilApp/
│       └── Server/
├── venvs/              # Virtual environments
│   └── myspy/
├── temp/               # Dočasné soubory
├── sbin/               # Skripty
├── bin/                # Binaries
├── backups/            # Zálohy
├── fs/                 # File storage
│   ├── media/
│   └── static/
└── logs/               # Logy
    ├── nginx/
    ├── gunicorn/
    ├── celery/
    └── application/
```

---

## Požadavky

### Minimální systémové požadavky

- **OS**: Debian 12.12, Ubuntu 22.04 LTS nebo Docker
- **CPU**: 2+ cores (doporučeno 4+)
- **RAM**: 4GB minimum (doporučeno 8GB+)
- **Disk**: 50GB SSD minimum (doporučeno 100GB+)
- **Network**: Veřejná IP adresa, otevřené porty 80, 443

### Doménové požadavky

Před instalací zajistěte následující domény s DNS záznamy pointing na server IP:

- `api.myspy.fir.ma` → REST API endpoint
- `wss.myspy.fir.ma` → WebSocket endpoint
- `app.myspy.fir.ma` → Web aplikace

### Software požadavky (instaluje se automaticky)

- Python 3.13+
- PostgreSQL 15+
- Redis 7.0+
- RabbitMQ 3.12+
- Nginx 1.24+ nebo Apache2 2.4+
- Certbot (Let's Encrypt)

---

## Instalace

### Rychlá instalace

```bash
# 1. Stáhnout instalační script
cd /tmp
wget https://raw.githubusercontent.com/m5ike/m5spy_app/main/Server/install.sh

# 2. Nastavit executable permission
chmod +x install.sh

# 3. Spustit instalaci jako root
sudo ./install.sh

# 4. Postupovat podle pokynů instalátoru
```

Instalátor provede:
- ✅ Kontrolu systémových požadavků
- ✅ Instalaci všech závislostí
- ✅ Vytvoření systemového uživatele `m5spy`
- ✅ Setup databáze PostgreSQL
- ✅ Konfiguraci Redis a RabbitMQ
- ✅ Clone Git repository
- ✅ Setup Python virtual environment
- ✅ Instalaci Python dependencies
- ✅ Django migrace
- ✅ Konfiguraci web serveru (Nginx/Apache2)
- ✅ Setup SSL certifikátů
- ✅ Vytvoření systemd services
- ✅ Spuštění všech služeb

### Manuální instalace

Pro pokročilé uživatele viz [MANUAL_INSTALL.md](MANUAL_INSTALL.md)

---

## Konfigurace služeb

### PostgreSQL

**Konfigurace:** `/etc/postgresql/15/main/postgresql.conf`

```ini
# Memory Configuration
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
work_mem = 4MB

# Connection Settings
max_connections = 100

# Write-Ahead Log
wal_buffers = 16MB
checkpoint_completion_target = 0.9

# Query Planner
random_page_cost = 1.1
effective_io_concurrency = 200
```

**Restart:**
```bash
sudo systemctl restart postgresql
```

### Redis

**Konfigurace:** `/etc/redis/redis.conf`

```ini
# Network
bind 127.0.0.1
port 6379
protected-mode yes

# Memory
maxmemory 512mb
maxmemory-policy allkeys-lru

# Persistence (optional - pro cache není nutné)
save ""
appendonly no

# Performance
tcp-backlog 511
timeout 0
tcp-keepalive 300
```

**Restart:**
```bash
sudo systemctl restart redis-server
```

### RabbitMQ

**Setup:**
```bash
# Vytvořit uživatele a vhost
sudo rabbitmqctl add_user m5spy secure_password_here
sudo rabbitmqctl add_vhost myspy_vhost
sudo rabbitmqctl set_permissions -p myspy_vhost m5spy ".*" ".*" ".*"

# Povolit management plugin
sudo rabbitmq-plugins enable rabbitmq_management

# Restart
sudo systemctl restart rabbitmq-server
```

**Management UI:** http://localhost:15672 (guest/guest na localhostu)

### Memcached (Optional)

**Konfigurace:** `/etc/memcached.conf`

```ini
-m 128
-p 11211
-u memcache
-l 127.0.0.1
```

**Restart:**
```bash
sudo systemctl restart memcached
```

---

## Systemd Services

### Hlavní M5Spy Service

**Soubor:** `/etc/systemd/system/m5spy.service`

```ini
[Unit]
Description=My Spy Master Service
After=network.target postgresql.service redis.service rabbitmq-server.service
Wants=m5spy-gunicorn.service m5spy-daphne.service m5spy-celery.service m5spy-celerybeat.service

[Service]
Type=oneshot
ExecStart=/bin/true
RemainAfterExit=yes

[Install]
WantedBy=multi-user.target
```

### Gunicorn Service (WSGI - Django)

**Soubor:** `/etc/systemd/system/m5spy-gunicorn.service`

```ini
[Unit]
Description=My Spy Gunicorn Service
After=network.target postgresql.service
PartOf=m5spy.service

[Service]
Type=notify
User=m5spy
Group=m5spy
WorkingDirectory=/home/m5spy/framework/m5spy_app/WebApp
Environment="PATH=/home/m5spy/venvs/myspy/bin"
ExecStart=/home/m5spy/venvs/myspy/bin/gunicorn \
    --bind 127.0.0.1:8000 \
    --workers 4 \
    --worker-class sync \
    --threads 2 \
    --timeout 120 \
    --max-requests 1000 \
    --max-requests-jitter 50 \
    --access-logfile /home/m5spy/logs/gunicorn/access.log \
    --error-logfile /home/m5spy/logs/gunicorn/error.log \
    --log-level info \
    config.wsgi:application

Restart=on-failure
RestartSec=5s

[Install]
WantedBy=m5spy.service
```

### Daphne Service (ASGI - WebSocket)

**Soubor:** `/etc/systemd/system/m5spy-daphne.service`

```ini
[Unit]
Description=My Spy Daphne Service (WebSocket)
After=network.target redis.service
PartOf=m5spy.service

[Service]
Type=simple
User=m5spy
Group=m5spy
WorkingDirectory=/home/m5spy/framework/m5spy_app/WebApp
Environment="PATH=/home/m5spy/venvs/myspy/bin"
ExecStart=/home/m5spy/venvs/myspy/bin/daphne \
    --bind 127.0.0.1 \
    --port 8001 \
    --access-log /home/m5spy/logs/daphne/access.log \
    --verbosity 1 \
    config.asgi:application

Restart=on-failure
RestartSec=5s

[Install]
WantedBy=m5spy.service
```

### Celery Worker Service

**Soubor:** `/etc/systemd/system/m5spy-celery.service`

```ini
[Unit]
Description=My Spy Celery Worker
After=network.target rabbitmq-server.service redis.service
PartOf=m5spy.service

[Service]
Type=forking
User=m5spy
Group=m5spy
WorkingDirectory=/home/m5spy/framework/m5spy_app/WebApp
Environment="PATH=/home/m5spy/venvs/myspy/bin"
ExecStart=/home/m5spy/venvs/myspy/bin/celery \
    -A config worker \
    --loglevel=info \
    --concurrency=4 \
    --max-tasks-per-child=1000 \
    --logfile=/home/m5spy/logs/celery/worker.log \
    --pidfile=/home/m5spy/temp/celery-worker.pid

Restart=on-failure
RestartSec=10s

[Install]
WantedBy=m5spy.service
```

### Celery Beat Service (Scheduler)

**Soubor:** `/etc/systemd/system/m5spy-celerybeat.service`

```ini
[Unit]
Description=My Spy Celery Beat Scheduler
After=network.target rabbitmq-server.service redis.service
PartOf=m5spy.service

[Service]
Type=simple
User=m5spy
Group=m5spy
WorkingDirectory=/home/m5spy/framework/m5spy_app/WebApp
Environment="PATH=/home/m5spy/venvs/myspy/bin"
ExecStart=/home/m5spy/venvs/myspy/bin/celery \
    -A config beat \
    --loglevel=info \
    --scheduler django_celery_beat.schedulers:DatabaseScheduler \
    --logfile=/home/m5spy/logs/celery/beat.log \
    --pidfile=/home/m5spy/temp/celery-beat.pid

Restart=on-failure
RestartSec=10s

[Install]
WantedBy=m5spy.service
```

### Service Management

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable services (start on boot)
sudo systemctl enable m5spy
sudo systemctl enable m5spy-gunicorn
sudo systemctl enable m5spy-daphne
sudo systemctl enable m5spy-celery
sudo systemctl enable m5spy-celerybeat

# Start all services
sudo systemctl start m5spy

# Check status
sudo systemctl status m5spy
sudo systemctl status m5spy-gunicorn
sudo systemctl status m5spy-daphne
sudo systemctl status m5spy-celery
sudo systemctl status m5spy-celerybeat

# Stop all services
sudo systemctl stop m5spy

# Restart services
sudo systemctl restart m5spy
```

---

## Nginx/Apache2 Setup

### Nginx (Doporučeno)

#### API Server Config

**Soubor:** `/etc/nginx/sites-available/api.myspy.fir.ma`

```nginx
# API Server - api.myspy.fir.ma
# Author: Michael KOJDL
# Version: 1.0.0
# Revision: 2025-10-30

upstream django_api {
    server 127.0.0.1:8000;
}

server {
    listen 80;
    server_name api.myspy.fir.ma;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.myspy.fir.ma;

    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/api.myspy.fir.ma/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.myspy.fir.ma/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Security Headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Logging
    access_log /home/m5spy/logs/nginx/api-access.log;
    error_log /home/m5spy/logs/nginx/api-error.log;

    # Max body size (pro file uploads)
    client_max_body_size 100M;

    # Timeouts
    proxy_connect_timeout 60s;
    proxy_send_timeout 60s;
    proxy_read_timeout 60s;

    # Proxy to Gunicorn
    location / {
        proxy_pass http://django_api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_redirect off;
    }

    # Static files
    location /static/ {
        alias /home/m5spy/fs/static/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # Media files (with auth)
    location /media/ {
        internal;
        alias /home/m5spy/fs/media/;
    }
}
```

#### WebSocket Server Config

**Soubor:** `/etc/nginx/sites-available/wss.myspy.fir.ma`

```nginx
# WebSocket Server - wss.myspy.fir.ma
# Author: Michael KOJDL
# Version: 1.0.0

upstream daphne {
    server 127.0.0.1:8001;
}

server {
    listen 80;
    server_name wss.myspy.fir.ma;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name wss.myspy.fir.ma;

    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/wss.myspy.fir.ma/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/wss.myspy.fir.ma/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;

    # Logging
    access_log /home/m5spy/logs/nginx/wss-access.log;
    error_log /home/m5spy/logs/nginx/wss-error.log;

    # WebSocket specific
    location / {
        proxy_pass http://daphne;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket timeouts
        proxy_connect_timeout 7d;
        proxy_send_timeout 7d;
        proxy_read_timeout 7d;
    }
}
```

#### Web App Config

**Soubor:** `/etc/nginx/sites-available/app.myspy.fir.ma`

```nginx
# Web Application - app.myspy.fir.ma
# Author: Michael KOJDL
# Version: 1.0.0

server {
    listen 80;
    server_name app.myspy.fir.ma;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name app.myspy.fir.ma;

    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/app.myspy.fir.ma/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/app.myspy.fir.ma/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;

    # Security Headers
    add_header Strict-Transport-Security "max-age=31536000" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;

    # Logging
    access_log /home/m5spy/logs/nginx/app-access.log;
    error_log /home/m5spy/logs/nginx/app-error.log;

    client_max_body_size 100M;

    # Django application
    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Static files
    location /static/ {
        alias /home/m5spy/fs/static/;
        expires 30d;
    }

    # Media files
    location /media/ {
        alias /home/m5spy/fs/media/;
        expires 7d;
    }
}
```

**Aktivace sites:**
```bash
sudo ln -s /etc/nginx/sites-available/api.myspy.fir.ma /etc/nginx/sites-enabled/
sudo ln -s /etc/nginx/sites-available/wss.myspy.fir.ma /etc/nginx/sites-enabled/
sudo ln -s /etc/nginx/sites-available/app.myspy.fir.ma /etc/nginx/sites-enabled/

# Test konfigurace
sudo nginx -t

# Restart Nginx
sudo systemctl restart nginx
```

---

## SSL Certifikáty

### Let's Encrypt Setup

```bash
# Instalace Certbot
sudo apt install certbot python3-certbot-nginx

# Získání certifikátů pro všechny domény
sudo certbot --nginx -d api.myspy.fir.ma
sudo certbot --nginx -d wss.myspy.fir.ma
sudo certbot --nginx -d app.myspy.fir.ma

# Test automatického renewal
sudo certbot renew --dry-run
```

Certbot automaticky obnoví certifikáty před expirací.

---

## Docker Deployment

Viz [docker/README.md](docker/README.md) pro Docker deployment.

### Quick Start

```bash
cd Server/docker
docker-compose up -d
```

---

## Backup & Restore

### Automatické zálohy

**Backup script:** `/home/m5spy/sbin/backup.sh`

```bash
#!/bin/bash
# My Spy Backup Script
# Author: Michael KOJDL
# Version: 1.0.0

BACKUP_DIR="/home/m5spy/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# Database backup
pg_dump -U m5spy myspy_db | gzip > "$BACKUP_DIR/db_$DATE.sql.gz"

# Media files backup
tar -czf "$BACKUP_DIR/media_$DATE.tar.gz" /home/m5spy/fs/media/

# Keep only last 30 days
find "$BACKUP_DIR" -type f -mtime +30 -delete
```

**Cron job:**
```bash
# Daily backup at 2 AM
0 2 * * * /home/m5spy/sbin/backup.sh
```

### Restore

```bash
# Restore database
gunzip < /home/m5spy/backups/db_20251030_020000.sql.gz | psql -U m5spy myspy_db

# Restore media
tar -xzf /home/m5spy/backups/media_20251030_020000.tar.gz -C /
```

---

## Monitoring

### Prometheus + Grafana (Optional)

Připravené dashboardy a exporters v `monitoring/` adresáři.

### Basic Monitoring

```bash
# Check services
sudo systemctl status m5spy

# Check logs
journalctl -u m5spy-gunicorn -f
journalctl -u m5spy-celery -f

# Check Nginx logs
tail -f /home/m5spy/logs/nginx/api-access.log
```

---

## Troubleshooting

### Services nekončí

```bash
# Hard restart
sudo systemctl daemon-reload
sudo systemctl reset-failed
sudo systemctl restart m5spy
```

### Database connection issues

```bash
# Check PostgreSQL
sudo systemctl status postgresql
sudo -u postgres psql -c "\conninfo"

# Check credentials in .env
cat /home/m5spy/framework/m5spy_app/WebApp/.env | grep DATABASE
```

### WebSocket nefunguje

```bash
# Check Daphne
sudo systemctl status m5spy-daphne
journalctl -u m5spy-daphne -n 50

# Check Redis
redis-cli ping
```

### High CPU usage

```bash
# Check Celery workers
celery -A config inspect active
celery -A config inspect stats

# Restart workers
sudo systemctl restart m5spy-celery
```

---

## Changelog

### Version 1.0.0 (2025-10-30)
- ✨ Iniciální verze dokumentace
- ✨ Instalační skripty
- ✨ Systemd services
- ✨ Nginx konfigurace
- ✨ Backup systém

---

**Autor:** Michael KOJDL
**Poslední aktualizace:** 2025-10-30
**Verze:** 1.0.0
