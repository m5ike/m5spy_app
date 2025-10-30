# My Spy - Django Web Aplikace - Implementační Průvodce

**Stav:** Základní struktura vytvořena
**Autor:** Michael KOJDL
**Verze:** 1.0.0
**Datum:** 2025-10-30

---

## ✅ Co je hotovo

- ✅ Základní adresářová struktura
- ✅ manage.py
- ✅ requirements.txt
- ✅ .env.example template
- ✅ Adresáře pro všechny Django apps

---

## 📋 Co zbývá implementovat

Postupujte podle souboru **`WebovaAplikace.sourcew`** pro dokončení implementace.

### 1. Django Configuration (Priorita: VYSOKÁ)

**Lokace:** `config/`

#### 1.1 Settings
- [ ] `config/settings/__init__.py`
- [ ] `config/settings/base.py` - Základní settings
- [ ] `config/settings/development.py` - Development config
- [ ] `config/settings/production.py` - Production config
- [ ] `config/settings/testing.py` - Testing config

#### 1.2 URLs & ASGI/WSGI
- [ ] `config/urls.py` - Root URL configuration
- [ ] `config/wsgi.py` - WSGI application
- [ ] `config/asgi.py` - ASGI application (WebSocket)

### 2. Users App (Priorita: VYSOKÁ)

**Lokace:** `apps/users/`

- [ ] `models.py` - Custom User model s roles
- [ ] `serializers.py` - DRF serializers
- [ ] `views.py` - Views & ViewSets
- [ ] `admin.py` - Admin configuration
- [ ] `permissions.py` - Custom permissions
- [ ] `urls.py` - App URLs

### 3. Devices App (Priorita: VYSOKÁ)

**Lokace:** `apps/devices/`

- [ ] `models.py` - Device registration model
- [ ] `serializers.py` - Device serializers
- [ ] `views.py` - Device ViewSets
- [ ] `services.py` - Business logic
- [ ] `admin.py` - Admin interface

### 4. API App (Priorita: VYSOKÁ)

**Lokace:** `apps/api/`

#### 4.1 Version 1
- [ ] `v1/urls.py` - API v1 URLs
- [ ] `v1/views.py` - API endpoints
- [ ] `v1/serializers.py` - API serializers

#### 4.2 Authentication
- [ ] `authentication.py` - Device authentication
- [ ] `exceptions.py` - Custom exception handlers
- [ ] `permissions.py` - API permissions
- [ ] `throttling.py` - Rate limiting

### 5. Monitoring Apps (Priorita: STŘEDNÍ)

**Lokace:** `apps/monitoring/`

#### 5.1 SMS App
- [ ] `sms/models.py` - SmsMessage model
- [ ] `sms/serializers.py`
- [ ] `sms/views.py`
- [ ] `sms/widgets.py` - Dashboard widgets
- [ ] `sms/services.py`

#### 5.2 Calls App
- [ ] `calls/models.py` - CallLog model
- [ ] `calls/serializers.py`
- [ ] `calls/views.py`
- [ ] `calls/widgets.py`

#### 5.3 Location App
- [ ] `location/models.py` - Location model
- [ ] `location/serializers.py`
- [ ] `location/views.py`
- [ ] `location/widgets.py`
- [ ] `location/services.py` - KML generation

#### 5.4 Apps App
- [ ] `apps/models.py` - AppUsage model
- [ ] `apps/serializers.py`
- [ ] `apps/views.py`
- [ ] `apps/widgets.py`

#### 5.5 Internet App
- [ ] `internet/models.py` - BrowserHistory model
- [ ] `internet/serializers.py`
- [ ] `internet/views.py`
- [ ] `internet/widgets.py`

#### 5.6 Keylogger App (⚠️ Citlivé!)
- [ ] `keylogger/models.py`
- [ ] `keylogger/serializers.py`
- [ ] `keylogger/views.py`
- [ ] `keylogger/widgets.py`

#### 5.7 Media App
- [ ] `media/models.py` - Screenshot, Video, Audio models
- [ ] `media/serializers.py`
- [ ] `media/views.py`
- [ ] `media/widgets.py`
- [ ] `media/storage.py` - Custom storage backend

#### 5.8 Files App
- [ ] `files/models.py`
- [ ] `files/serializers.py`
- [ ] `files/views.py` - File operations
- [ ] `files/webdav.py` - WebDAV implementation

### 6. WebSocket App (Priorita: VYSOKÁ)

**Lokace:** `apps/websocket/`

- [ ] `consumers.py` - WebSocket consumers
  - [ ] `DeviceConsumer` - Device connection
  - [ ] `DashboardConsumer` - Dashboard real-time
- [ ] `routing.py` - WebSocket URL routing
- [ ] `middleware.py` - WebSocket auth middleware

### 7. Dashboard App (Priorita: STŘEDNÍ)

**Lokace:** `apps/dashboard/`

- [ ] `views.py` - Dashboard views
- [ ] `urls.py` - Dashboard URLs
- [ ] `widgets/base.py` - Base widget class
- [ ] `widgets/` - Implementace všech widgetů
- [ ] `templates/dashboard/` - Dashboard templates

### 8. Hooks App (Priorita: NÍZKÁ)

**Lokace:** `apps/hooks/`

- [ ] `models.py` - Hook model
- [ ] `engine.py` - Hook execution engine
- [ ] `actions.py` - Hook actions (notify, block, atd.)
- [ ] `filters.py` - Regex filters

### 9. Celery Tasks (Priorita: STŘEDNÍ)

**Lokace:** všude kde je potřeba

- [ ] `celery.py` v config/ - Celery application
- [ ] `tasks.py` v každé app - Asynchronní úlohy
  - Data cleanup tasks
  - Email notifications
  - Report generation
  - Backup tasks

### 10. Templates (Priorita: STŘEDNÍ)

**Lokace:** `templates/`

- [ ] `base.html` - Base template
- [ ] `auth/login.html` - Login page
- [ ] `auth/register.html` - Registration
- [ ] `dashboard/index.html` - Main dashboard
- [ ] `dashboard/device_detail.html` - Device dashboard
- [ ] `widgets/` - Widget templates
- [ ] `components/` - Reusable components

### 11. Static Files (Priorita: NÍZKÁ)

**Lokace:** `static/`

- [ ] `css/main.css` - Custom CSS (Tailwind build)
- [ ] `js/app.js` - Main JavaScript
- [ ] `js/websocket.js` - WebSocket handler
- [ ] `js/widgets.js` - Widget interactions

### 12. Management Commands (Priorita: NÍZKÁ)

**Lokace:** každá app může mít `management/commands/`

- [ ] `create_superuser_with_key.py` - Create admin with API key
- [ ] `cleanup_old_data.py` - Data retention cleanup
- [ ] `backup_database.py` - Database backup
- [ ] `sync_devices.py` - Device sync command

### 13. Tests (Priorita: NÍZKÁ)

**Lokace:** `tests/`

- [ ] `unit/` - Unit tests
- [ ] `integration/` - Integration tests
- [ ] `e2e/` - End-to-end tests

---

## 🚀 Postup implementace

### Fáze 1: Setup & Configuration (Den 1-2)
1. Vytvořit .env soubor z .env.example
2. Implementovat config/settings/
3. Setup database (PostgreSQL)
4. Setup Redis, RabbitMQ
5. Migrate: `python manage.py migrate`

### Fáze 2: Core Apps (Den 3-5)
1. Users app s custom User model
2. Devices app pro registrace
3. API app s basic endpoints
4. Migrate & test registrace

### Fáze 3: Monitoring Apps (Den 6-10)
1. SMS app
2. Calls app
3. Location app
4. Apps app
5. Internet app
6. Pro každou: models → serializers → views → test

### Fáze 4: Advanced Features (Den 11-15)
1. WebSocket implementation
2. Keylogger, Media, Files apps
3. Dashboard & widgets
4. Hooks system

### Fáze 5: Frontend & Polish (Den 16-20)
1. Templates implementation
2. Tailwind CSS build
3. JavaScript functionality
4. Dashboard customization

### Fáze 6: Celery & Background (Den 21-23)
1. Celery setup
2. Asynchronní tasks
3. Scheduled jobs

### Fáze 7: Testing & Security (Den 24-28)
1. Unit tests
2. Integration tests
3. Security audit
4. Performance optimization

### Fáze 8: Deployment (Den 29-30)
1. Production settings
2. Static files collection
3. Nginx/Gunicorn setup
4. SSL certificates
5. Systemd services

---

## 📚 Další zdroje

- **README.md** - Obecná dokumentace Web aplikace
- **WebovaAplikace.sourcew** - Detailní step-by-step průvodce
- **docs/API.md** - API dokumentace
- **docs/SECURITY.md** - Bezpečnostní guidelines
- **Server/README.md** - Deployment dokumentace

---

## ⚠️ Důležité poznámky

### Před začátkem
1. **Virtual Environment**: Vždy používejte venv/conda
2. **Dependencies**: `pip install -r requirements.txt`
3. **Environment Variables**: Nastavte všechny v .env
4. **Database**: Vytvořte PostgreSQL databázi
5. **Redis**: Musí běžet na localhostu nebo remote

### Bezpečnost
1. **SECRET_KEY**: Vygenerujte silný random klíč
2. **DEBUG**: Vždy False v production
3. **ALLOWED_HOSTS**: Nastavte správné domény
4. **CSRF**: Configure trusted origins
5. **SQL Injection**: Používejte pouze ORM

### Performance
1. **Database Indexes**: Přidejte indexy na často dotazovaná pole
2. **Caching**: Využívejte Redis cache
3. **Query Optimization**: Používejte select_related, prefetch_related
4. **Static Files**: Servírujte přes Nginx v production

### GDPR
1. **Consent**: Implementujte consent management
2. **Data Export**: Funkce pro export všech dat
3. **Data Deletion**: Kompletní mazání uživatelských dat
4. **Retention**: Automatické mazání starých dat

---

## 🔧 Užitečné příkazy

```bash
# Vytvoření migrace
python manage.py makemigrations

# Spuštění migrací
python manage.py migrate

# Vytvoření superusera
python manage.py createsuperuser

# Sběr static files
python manage.py collectstatic

# Spuštění dev serveru
python manage.py runserver

# Spuštění Celery workera
celery -A config worker -l info

# Spuštění Celery beat
celery -A config beat -l info

# Shell
python manage.py shell

# Tests
pytest
```

---

**Autor:** Michael KOJDL
**Poslední aktualizace:** 2025-10-30
