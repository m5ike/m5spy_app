#!/usr/bin/env python
"""
Django Module Testing Script
@author Michael KOJDL
@version 1.0.0

This script tests:
- All Django apps and models
- All API endpoints
- Authentication system
- Data validation
- Database operations
- Module connections
"""

import os
import sys
import django
import json
from pathlib import Path
from datetime import datetime, timedelta
from decimal import Decimal

# Add project directory to path
BASE_DIR = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE_DIR))

# Setup Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings.production')
django.setup()

from django.test import Client
from django.contrib.auth import get_user_model
from django.utils import timezone
from apps.devices.models import Device
from apps.monitoring_sms.models import SmsMessage
from apps.monitoring_calls.models import CallLog
from apps.monitoring_location.models import LocationLog
from apps.monitoring_apps.models import InstalledApp
from apps.monitoring_browser.models import BrowserHistory
from apps.monitoring_media.models import MediaFile
from apps.monitoring_screenshots.models import Screenshot

User = get_user_model()

# Color codes for terminal output
class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'

class TestResults:
    def __init__(self):
        self.passed = 0
        self.failed = 0
        self.tests = []

    def add_pass(self, test_name):
        self.passed += 1
        self.tests.append((test_name, True, None))
        print(f"{Colors.GREEN}✓ {test_name}{Colors.ENDC}")

    def add_fail(self, test_name, error):
        self.failed += 1
        self.tests.append((test_name, False, error))
        print(f"{Colors.RED}✗ {test_name}: {error}{Colors.ENDC}")

    def print_summary(self):
        total = self.passed + self.failed
        print(f"\n{Colors.BOLD}{'='*60}")
        print(f"TEST SUMMARY")
        print(f"{'='*60}{Colors.ENDC}")
        print(f"Total: {total}")
        print(f"{Colors.GREEN}Passed: {self.passed}{Colors.ENDC}")
        print(f"{Colors.RED}Failed: {self.failed}{Colors.ENDC}")

        if self.failed > 0:
            print(f"\n{Colors.RED}Failed tests:{Colors.ENDC}")
            for name, passed, error in self.tests:
                if not passed:
                    print(f"  - {name}: {error}")

results = TestResults()

def print_section(title):
    """Print section header"""
    print(f"\n{Colors.BLUE}{Colors.BOLD}{'='*60}")
    print(f"{title}")
    print(f"{'='*60}{Colors.ENDC}\n")

def test_user_model():
    """Test User model"""
    print_section("Testing User Model")

    try:
        # Create test user
        user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            password='testpass123',
            role='USER'
        )
        results.add_pass("User creation")

        # Test user properties
        assert user.username == 'testuser'
        assert user.email == 'test@example.com'
        assert user.role == 'USER'
        results.add_pass("User properties")

        # Clean up
        user.delete()
        results.add_pass("User deletion")

    except Exception as e:
        results.add_fail("User model test", str(e))

def test_device_model():
    """Test Device model"""
    print_section("Testing Device Model")

    try:
        # Create test user and device
        user = User.objects.create_user(
            username='devicetestuser',
            email='device@example.com',
            password='testpass123'
        )

        device = Device.objects.create(
            user=user,
            device_id='TEST-DEVICE-001',
            device_name='Test Device',
            model='Test Model',
            manufacturer='Test Manufacturer',
            os_version='13',
            app_version='1.0.0'
        )
        results.add_pass("Device creation")

        # Test API key generation
        assert device.api_key is not None
        assert len(device.api_key) > 0
        results.add_pass("API key generation")

        # Clean up
        device.delete()
        user.delete()
        results.add_pass("Device cleanup")

    except Exception as e:
        results.add_fail("Device model test", str(e))

def test_monitoring_models():
    """Test all monitoring models"""
    print_section("Testing Monitoring Models")

    try:
        # Create test user and device
        user = User.objects.create_user(username='monitoruser', email='monitor@example.com', password='test123')
        device = Device.objects.create(
            user=user,
            device_id='MONITOR-001',
            device_name='Monitor Device'
        )

        # Test SMS
        sms = SmsMessage.objects.create(
            device=device,
            address='+420123456789',
            body='Test message',
            type='RECEIVED',
            date=timezone.now()
        )
        results.add_pass("SmsMessage creation")

        # Test Call
        call = CallLog.objects.create(
            device=device,
            phone_number='+420987654321',
            contact_name='Test Contact',
            call_type='INCOMING',
            duration=120,
            date=timezone.now()
        )
        results.add_pass("CallLog creation")

        # Test Location
        location = LocationLog.objects.create(
            device=device,
            latitude=Decimal('50.0755'),
            longitude=Decimal('14.4378'),
            accuracy=10.5,
            address='Prague, Czech Republic',
            timestamp=timezone.now()
        )
        results.add_pass("LocationLog creation")

        # Test App
        app = InstalledApp.objects.create(
            device=device,
            package_name='com.test.app',
            app_name='Test App',
            version_name='1.0.0',
            version_code='1',
            is_system_app=False,
            install_time=timezone.now(),
            update_time=timezone.now()
        )
        results.add_pass("InstalledApp creation")

        # Test Browser History
        browser = BrowserHistory.objects.create(
            device=device,
            url='https://example.com',
            title='Example',
            visit_count=1,
            last_visit_time=timezone.now()
        )
        results.add_pass("BrowserHistory creation")

        # Test Media
        media = MediaFile.objects.create(
            device=device,
            file_path='/storage/emulated/0/DCIM/test.jpg',
            media_type='PHOTO',
            size=1024000,
            date_added=timezone.now(),
            date_modified=timezone.now()
        )
        results.add_pass("MediaFile creation")

        # Test Screenshot
        screenshot = Screenshot.objects.create(
            device=device,
            file_path='/storage/emulated/0/Pictures/Screenshots/test.png',
            size=512000,
            date_taken=timezone.now()
        )
        results.add_pass("Screenshot creation")

        # Clean up
        device.delete()
        user.delete()
        results.add_pass("Monitoring models cleanup")

    except Exception as e:
        results.add_fail("Monitoring models test", str(e))

def test_api_authentication():
    """Test API authentication"""
    print_section("Testing API Authentication")

    try:
        # Create test device
        user = User.objects.create_user(username='apiuser', email='api@example.com', password='test123')
        device = Device.objects.create(
            user=user,
            device_id='API-TEST-001',
            device_name='API Test Device'
        )

        client = Client()

        # Test without authentication
        response = client.post('/api/v1/sms/', {}, content_type='application/json')
        assert response.status_code in [401, 403]
        results.add_pass("Unauthenticated request rejected")

        # Test with authentication
        response = client.post(
            '/api/v1/sms/',
            json.dumps([]),
            content_type='application/json',
            HTTP_X_DEVICE_ID=device.device_id,
            HTTP_X_API_KEY=device.api_key
        )
        assert response.status_code in [200, 201]
        results.add_pass("Authenticated request accepted")

        # Clean up
        device.delete()
        user.delete()

    except Exception as e:
        results.add_fail("API authentication test", str(e))

def test_api_endpoints():
    """Test all API endpoints"""
    print_section("Testing API Endpoints")

    try:
        # Create test device
        user = User.objects.create_user(username='endpointuser', email='endpoint@example.com', password='test123')
        device = Device.objects.create(
            user=user,
            device_id='ENDPOINT-001',
            device_name='Endpoint Test'
        )

        client = Client()
        headers = {
            'HTTP_X_DEVICE_ID': device.device_id,
            'HTTP_X_API_KEY': device.api_key
        }

        # Test health endpoint
        response = client.get('/api/v1/health/')
        assert response.status_code == 200
        results.add_pass("Health endpoint")

        # Test SMS upload
        sms_data = [{
            'address': '+420123456789',
            'body': 'Test SMS',
            'type': 'RECEIVED',
            'date': timezone.now().isoformat()
        }]
        response = client.post(
            '/api/v1/sms/',
            json.dumps(sms_data),
            content_type='application/json',
            **headers
        )
        assert response.status_code in [200, 201]
        results.add_pass("SMS upload endpoint")

        # Test Calls upload
        calls_data = [{
            'phone_number': '+420987654321',
            'contact_name': 'Test Contact',
            'call_type': 'INCOMING',
            'duration': 120,
            'date': timezone.now().isoformat()
        }]
        response = client.post(
            '/api/v1/calls/',
            json.dumps(calls_data),
            content_type='application/json',
            **headers
        )
        assert response.status_code in [200, 201]
        results.add_pass("Calls upload endpoint")

        # Test Location upload
        location_data = [{
            'latitude': 50.0755,
            'longitude': 14.4378,
            'accuracy': 10.5,
            'address': 'Prague',
            'timestamp': timezone.now().isoformat()
        }]
        response = client.post(
            '/api/v1/location/',
            json.dumps(location_data),
            content_type='application/json',
            **headers
        )
        assert response.status_code in [200, 201]
        results.add_pass("Location upload endpoint")

        # Test Apps upload
        apps_data = [{
            'package_name': 'com.test.app',
            'app_name': 'Test App',
            'version_name': '1.0.0',
            'version_code': '1',
            'is_system_app': False,
            'install_time': timezone.now().isoformat(),
            'update_time': timezone.now().isoformat()
        }]
        response = client.post(
            '/api/v1/apps/',
            json.dumps(apps_data),
            content_type='application/json',
            **headers
        )
        assert response.status_code in [200, 201]
        results.add_pass("Apps upload endpoint")

        # Test Browser History upload
        browser_data = [{
            'url': 'https://example.com',
            'title': 'Example',
            'visit_count': 1,
            'last_visit_time': timezone.now().isoformat()
        }]
        response = client.post(
            '/api/v1/browser-history/',
            json.dumps(browser_data),
            content_type='application/json',
            **headers
        )
        assert response.status_code in [200, 201]
        results.add_pass("Browser History upload endpoint")

        # Test Media upload
        media_data = [{
            'file_path': '/storage/test.jpg',
            'media_type': 'PHOTO',
            'size': 1024000,
            'date_added': timezone.now().isoformat(),
            'date_modified': timezone.now().isoformat()
        }]
        response = client.post(
            '/api/v1/media/',
            json.dumps(media_data),
            content_type='application/json',
            **headers
        )
        assert response.status_code in [200, 201]
        results.add_pass("Media upload endpoint")

        # Test Screenshots upload
        screenshot_data = [{
            'file_path': '/storage/screenshot.png',
            'size': 512000,
            'date_taken': timezone.now().isoformat()
        }]
        response = client.post(
            '/api/v1/screenshots/',
            json.dumps(screenshot_data),
            content_type='application/json',
            **headers
        )
        assert response.status_code in [200, 201]
        results.add_pass("Screenshots upload endpoint")

        # Clean up
        device.delete()
        user.delete()

    except Exception as e:
        results.add_fail("API endpoints test", str(e))

def test_device_registration():
    """Test device registration endpoint"""
    print_section("Testing Device Registration")

    try:
        client = Client()

        # Create user for registration
        user = User.objects.create_user(username='reguser', email='reg@example.com', password='test123')

        # Test registration
        registration_data = {
            'device_id': 'REG-TEST-001',
            'device_name': 'Registration Test',
            'model': 'Test Model',
            'manufacturer': 'Test Manufacturer',
            'os_version': '13',
            'app_version': '1.0.0'
        }

        response = client.post(
            '/api/v1/register/',
            json.dumps(registration_data),
            content_type='application/json'
        )

        if response.status_code in [200, 201]:
            data = response.json()
            assert 'api_key' in data
            assert data['result'] == 'ok'
            results.add_pass("Device registration")

            # Clean up
            Device.objects.filter(device_id='REG-TEST-001').delete()
        else:
            results.add_fail("Device registration", f"Status code: {response.status_code}")

        user.delete()

    except Exception as e:
        results.add_fail("Device registration test", str(e))

def run_all_tests():
    """Run all module tests"""
    print(f"{Colors.BOLD}{Colors.BLUE}")
    print("=" * 60)
    print("  MY SPY - MODULE TESTING")
    print("=" * 60)
    print(f"{Colors.ENDC}")

    # Run all tests
    test_user_model()
    test_device_model()
    test_monitoring_models()
    test_api_authentication()
    test_device_registration()
    test_api_endpoints()

    # Print summary
    results.print_summary()

    # Return exit code
    return 0 if results.failed == 0 else 1

if __name__ == '__main__':
    try:
        exit_code = run_all_tests()
        sys.exit(exit_code)
    except KeyboardInterrupt:
        print(f"\n\n{Colors.YELLOW}Testing interrupted by user{Colors.ENDC}")
        sys.exit(1)
    except Exception as e:
        print(f"\n{Colors.RED}Fatal error: {str(e)}{Colors.ENDC}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
