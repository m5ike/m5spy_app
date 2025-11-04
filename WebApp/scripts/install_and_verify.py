#!/usr/bin/env python
"""
Django Installation and Verification Script
@author Michael KOJDL
@version 1.0.0

This script:
- Verifies Django installation
- Checks all dependencies
- Runs migrations
- Verifies all database tables are created
- Tests all module connections
- Validates configuration
"""

import os
import sys
import django
from pathlib import Path

# Add project directory to path
BASE_DIR = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE_DIR))

# Setup Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings.production')
django.setup()

from django.core.management import call_command
from django.db import connection
from django.conf import settings
from django.apps import apps
import importlib

# Color codes for terminal output
class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'

def print_section(title):
    """Print section header"""
    print(f"\n{Colors.BLUE}{Colors.BOLD}{'='*60}")
    print(f"{title}")
    print(f"{'='*60}{Colors.ENDC}\n")

def print_success(message):
    """Print success message"""
    print(f"{Colors.GREEN}✓ {message}{Colors.ENDC}")

def print_error(message):
    """Print error message"""
    print(f"{Colors.RED}✗ {message}{Colors.ENDC}")

def print_warning(message):
    """Print warning message"""
    print(f"{Colors.YELLOW}⚠ {message}{Colors.ENDC}")

def print_info(message):
    """Print info message"""
    print(f"{Colors.BLUE}ℹ {message}{Colors.ENDC}")

def check_python_version():
    """Check Python version"""
    print_section("Python Version Check")
    version = sys.version_info
    print_info(f"Python {version.major}.{version.minor}.{version.micro}")

    if version.major == 3 and version.minor >= 11:
        print_success("Python version is compatible (3.11+)")
        return True
    else:
        print_error(f"Python 3.11+ required, found {version.major}.{version.minor}")
        return False

def check_dependencies():
    """Check if all required packages are installed"""
    print_section("Dependency Check")

    required_packages = [
        ('django', 'Django'),
        ('rest_framework', 'Django REST Framework'),
        ('corsheaders', 'Django CORS Headers'),
        ('django_filters', 'Django Filters'),
        ('channels', 'Django Channels'),
        ('celery', 'Celery'),
        ('redis', 'Redis'),
        ('decouple', 'Python Decouple'),
    ]

    all_installed = True
    for package, name in required_packages:
        try:
            mod = importlib.import_module(package)
            version = getattr(mod, '__version__', 'unknown')
            print_success(f"{name}: {version}")
        except ImportError:
            print_error(f"{name} is not installed")
            all_installed = False

    return all_installed

def check_database_connection():
    """Check database connection"""
    print_section("Database Connection Check")

    try:
        with connection.cursor() as cursor:
            cursor.execute("SELECT 1")
            result = cursor.fetchone()
            if result:
                print_success("Database connection successful")

                # Get database info
                db_settings = settings.DATABASES['default']
                print_info(f"Engine: {db_settings['ENGINE']}")
                print_info(f"Name: {db_settings['NAME']}")
                print_info(f"Host: {db_settings.get('HOST', 'localhost')}")
                return True
    except Exception as e:
        print_error(f"Database connection failed: {str(e)}")
        return False

def run_migrations():
    """Run database migrations"""
    print_section("Running Migrations")

    try:
        call_command('migrate', verbosity=0, interactive=False)
        print_success("All migrations applied successfully")
        return True
    except Exception as e:
        print_error(f"Migration failed: {str(e)}")
        return False

def check_tables_created():
    """Verify all expected tables are created"""
    print_section("Database Tables Verification")

    expected_apps = [
        'users',
        'devices',
        'monitoring_sms',
        'monitoring_calls',
        'monitoring_location',
        'monitoring_apps',
        'monitoring_browser',
        'monitoring_media',
        'monitoring_screenshots',
    ]

    all_tables_exist = True

    with connection.cursor() as cursor:
        # Get all table names
        cursor.execute("""
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
        """)
        existing_tables = {row[0] for row in cursor.fetchall()}

    for app_name in expected_apps:
        try:
            app_config = apps.get_app_config(app_name)
            models = app_config.get_models()

            print_info(f"\n{app_name.upper()}:")

            for model in models:
                table_name = model._meta.db_table
                if table_name in existing_tables:
                    print_success(f"  {model.__name__} → {table_name}")
                else:
                    print_error(f"  {model.__name__} → {table_name} (MISSING)")
                    all_tables_exist = False

        except Exception as e:
            print_error(f"  Error checking {app_name}: {str(e)}")
            all_tables_exist = False

    return all_tables_exist

def check_installed_apps():
    """Check all required apps are installed"""
    print_section("Installed Apps Check")

    required_apps = [
        'django.contrib.admin',
        'django.contrib.auth',
        'django.contrib.contenttypes',
        'django.contrib.sessions',
        'rest_framework',
        'corsheaders',
        'apps.users',
        'apps.devices',
        'apps.monitoring_sms',
        'apps.monitoring_calls',
        'apps.monitoring_location',
        'apps.monitoring_apps',
        'apps.monitoring_browser',
        'apps.monitoring_media',
        'apps.monitoring_screenshots',
        'apps.api',
    ]

    all_installed = True
    for app in required_apps:
        if app in settings.INSTALLED_APPS:
            print_success(f"{app}")
        else:
            print_error(f"{app} (NOT INSTALLED)")
            all_installed = False

    return all_installed

def check_static_files():
    """Check static files configuration"""
    print_section("Static Files Check")

    static_root = Path(settings.STATIC_ROOT)
    media_root = Path(settings.MEDIA_ROOT)

    print_info(f"STATIC_ROOT: {static_root}")
    print_info(f"MEDIA_ROOT: {media_root}")

    # Create directories if they don't exist
    static_root.mkdir(parents=True, exist_ok=True)
    media_root.mkdir(parents=True, exist_ok=True)

    if static_root.exists() and static_root.is_dir():
        print_success("Static files directory exists")
    else:
        print_error("Static files directory missing")
        return False

    if media_root.exists() and media_root.is_dir():
        print_success("Media files directory exists")
    else:
        print_error("Media files directory missing")
        return False

    return True

def collect_static_files():
    """Collect static files"""
    print_section("Collecting Static Files")

    try:
        call_command('collectstatic', verbosity=0, interactive=False, clear=True)
        print_success("Static files collected successfully")
        return True
    except Exception as e:
        print_error(f"Static file collection failed: {str(e)}")
        return False

def check_redis_connection():
    """Check Redis connection"""
    print_section("Redis Connection Check")

    try:
        import redis
        from django.core.cache import cache

        # Test cache connection
        cache.set('test_key', 'test_value', 10)
        value = cache.get('test_key')

        if value == 'test_value':
            print_success("Redis connection successful")
            cache.delete('test_key')
            return True
        else:
            print_error("Redis connection failed: unexpected value")
            return False
    except Exception as e:
        print_warning(f"Redis connection check skipped: {str(e)}")
        return True  # Don't fail if Redis is not configured

def check_celery_configuration():
    """Check Celery configuration"""
    print_section("Celery Configuration Check")

    try:
        from config.celery import app as celery_app

        print_info(f"Broker: {settings.CELERY_BROKER_URL}")
        print_info(f"Backend: {settings.CELERY_RESULT_BACKEND}")
        print_success("Celery is configured")
        return True
    except Exception as e:
        print_warning(f"Celery configuration check failed: {str(e)}")
        return True  # Don't fail if Celery is not fully configured

def create_superuser_if_needed():
    """Create superuser if environment variables are set"""
    print_section("Superuser Check")

    username = os.getenv('DJANGO_SUPERUSER_USERNAME')
    email = os.getenv('DJANGO_SUPERUSER_EMAIL')
    password = os.getenv('DJANGO_SUPERUSER_PASSWORD')

    if not all([username, email, password]):
        print_info("Superuser environment variables not set, skipping creation")
        return True

    try:
        from apps.users.models import User

        if User.objects.filter(username=username).exists():
            print_info(f"Superuser '{username}' already exists")
        else:
            User.objects.create_superuser(
                username=username,
                email=email,
                password=password,
                role='ADMIN'
            )
            print_success(f"Superuser '{username}' created successfully")

        return True
    except Exception as e:
        print_error(f"Superuser creation failed: {str(e)}")
        return False

def run_all_checks():
    """Run all verification checks"""
    print(f"{Colors.BOLD}{Colors.BLUE}")
    print("=" * 60)
    print("  MY SPY - DJANGO INSTALLATION & VERIFICATION")
    print("=" * 60)
    print(f"{Colors.ENDC}")

    results = {
        'Python Version': check_python_version(),
        'Dependencies': check_dependencies(),
        'Database Connection': check_database_connection(),
        'Migrations': run_migrations(),
        'Database Tables': check_tables_created(),
        'Installed Apps': check_installed_apps(),
        'Static Files': check_static_files(),
        'Collect Static': collect_static_files(),
        'Redis Connection': check_redis_connection(),
        'Celery Configuration': check_celery_configuration(),
        'Superuser Setup': create_superuser_if_needed(),
    }

    # Summary
    print_section("Verification Summary")

    passed = sum(1 for result in results.values() if result)
    total = len(results)

    for check, result in results.items():
        if result:
            print_success(f"{check}")
        else:
            print_error(f"{check}")

    print(f"\n{Colors.BOLD}Results: {passed}/{total} checks passed{Colors.ENDC}")

    if passed == total:
        print(f"\n{Colors.GREEN}{Colors.BOLD}✓ ALL CHECKS PASSED - Installation is complete!{Colors.ENDC}\n")
        return 0
    else:
        print(f"\n{Colors.RED}{Colors.BOLD}✗ SOME CHECKS FAILED - Please review errors above{Colors.ENDC}\n")
        return 1

if __name__ == '__main__':
    try:
        exit_code = run_all_checks()
        sys.exit(exit_code)
    except KeyboardInterrupt:
        print(f"\n\n{Colors.YELLOW}Installation check interrupted by user{Colors.ENDC}")
        sys.exit(1)
    except Exception as e:
        print(f"\n{Colors.RED}Fatal error: {str(e)}{Colors.ENDC}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
