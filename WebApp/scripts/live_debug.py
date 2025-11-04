#!/usr/bin/env python
"""
Django Live Debugging Script
@author Michael KOJDL
@version 1.0.0

This script provides real-time monitoring of:
- Django logs
- Database queries
- API requests/responses
- Celery tasks
- System metrics
"""

import os
import sys
import django
import time
import threading
from pathlib import Path
from datetime import datetime
from collections import deque

# Add project directory to path
BASE_DIR = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE_DIR))

# Setup Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings.production')
django.setup()

from django.db import connection
from django.conf import settings
import logging

# Color codes for terminal output
class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    MAGENTA = '\033[95m'
    CYAN = '\033[96m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'

class LiveDebugger:
    def __init__(self):
        self.running = False
        self.log_buffer = deque(maxlen=100)
        self.query_buffer = deque(maxlen=50)
        self.api_buffer = deque(maxlen=50)

    def print_header(self):
        """Print header"""
        os.system('clear' if os.name == 'posix' else 'cls')
        print(f"{Colors.BOLD}{Colors.BLUE}")
        print("=" * 80)
        print("  MY SPY - LIVE DEBUGGING MONITOR")
        print("=" * 80)
        print(f"{Colors.ENDC}")
        print(f"{Colors.CYAN}Press Ctrl+C to exit{Colors.ENDC}\n")

    def format_timestamp(self):
        """Get formatted timestamp"""
        return datetime.now().strftime('%H:%M:%S')

    def monitor_database_queries(self):
        """Monitor database queries"""
        print(f"{Colors.YELLOW}{Colors.BOLD}Recent Database Queries:{Colors.ENDC}")

        if not self.query_buffer:
            print(f"{Colors.CYAN}  No queries yet...{Colors.ENDC}")
        else:
            for timestamp, query in list(self.query_buffer)[-10:]:
                # Truncate long queries
                query_short = query[:100] + '...' if len(query) > 100 else query
                print(f"  {Colors.CYAN}[{timestamp}]{Colors.ENDC} {query_short}")
        print()

    def monitor_api_requests(self):
        """Monitor API requests"""
        print(f"{Colors.GREEN}{Colors.BOLD}Recent API Requests:{Colors.ENDC}")

        if not self.api_buffer:
            print(f"{Colors.CYAN}  No API requests yet...{Colors.ENDC}")
        else:
            for timestamp, method, path, status in list(self.api_buffer)[-10:]:
                color = Colors.GREEN if status < 400 else Colors.RED
                print(f"  {Colors.CYAN}[{timestamp}]{Colors.ENDC} {method} {path} → {color}{status}{Colors.ENDC}")
        print()

    def monitor_logs(self):
        """Monitor application logs"""
        print(f"{Colors.MAGENTA}{Colors.BOLD}Recent Logs:{Colors.ENDC}")

        if not self.log_buffer:
            print(f"{Colors.CYAN}  No logs yet...{Colors.ENDC}")
        else:
            for timestamp, level, message in list(self.log_buffer)[-15:]:
                if level == 'ERROR':
                    color = Colors.RED
                elif level == 'WARNING':
                    color = Colors.YELLOW
                elif level == 'INFO':
                    color = Colors.GREEN
                else:
                    color = Colors.CYAN

                print(f"  {Colors.CYAN}[{timestamp}]{Colors.ENDC} {color}{level:8}{Colors.ENDC} {message}")
        print()

    def get_system_stats(self):
        """Get system statistics"""
        print(f"{Colors.BLUE}{Colors.BOLD}System Statistics:{Colors.ENDC}")

        try:
            from apps.devices.models import Device
            from apps.monitoring_sms.models import SmsMessage
            from apps.monitoring_calls.models import CallLog
            from apps.monitoring_location.models import LocationLog

            device_count = Device.objects.count()
            sms_count = SmsMessage.objects.count()
            call_count = CallLog.objects.count()
            location_count = LocationLog.objects.count()

            print(f"  Devices: {Colors.GREEN}{device_count}{Colors.ENDC}")
            print(f"  SMS Messages: {Colors.GREEN}{sms_count}{Colors.ENDC}")
            print(f"  Call Logs: {Colors.GREEN}{call_count}{Colors.ENDC}")
            print(f"  Location Logs: {Colors.GREEN}{location_count}{Colors.ENDC}")

        except Exception as e:
            print(f"  {Colors.RED}Error fetching stats: {str(e)}{Colors.ENDC}")
        print()

    def get_database_info(self):
        """Get database information"""
        print(f"{Colors.BLUE}{Colors.BOLD}Database Information:{Colors.ENDC}")

        try:
            db_settings = settings.DATABASES['default']
            print(f"  Engine: {Colors.GREEN}{db_settings['ENGINE']}{Colors.ENDC}")
            print(f"  Database: {Colors.GREEN}{db_settings['NAME']}{Colors.ENDC}")

            if 'HOST' in db_settings:
                print(f"  Host: {Colors.GREEN}{db_settings['HOST']}:{db_settings.get('PORT', 'default')}{Colors.ENDC}")

            # Get connection status
            with connection.cursor() as cursor:
                cursor.execute("SELECT 1")
                print(f"  Status: {Colors.GREEN}Connected{Colors.ENDC}")

        except Exception as e:
            print(f"  Status: {Colors.RED}Error: {str(e)}{Colors.ENDC}")
        print()

    def get_cache_info(self):
        """Get cache information"""
        print(f"{Colors.BLUE}{Colors.BOLD}Cache Information:{Colors.ENDC}")

        try:
            from django.core.cache import cache

            # Test cache
            cache.set('debug_test', 'ok', 10)
            value = cache.get('debug_test')

            if value == 'ok':
                print(f"  Redis Status: {Colors.GREEN}Connected{Colors.ENDC}")
                cache.delete('debug_test')
            else:
                print(f"  Redis Status: {Colors.YELLOW}Unknown{Colors.ENDC}")

        except Exception as e:
            print(f"  Redis Status: {Colors.RED}Error: {str(e)}{Colors.ENDC}")
        print()

    def get_celery_info(self):
        """Get Celery information"""
        print(f"{Colors.BLUE}{Colors.BOLD}Celery Information:{Colors.ENDC}")

        try:
            from config.celery import app as celery_app

            # Check broker connection
            inspector = celery_app.control.inspect()
            stats = inspector.stats()

            if stats:
                print(f"  Status: {Colors.GREEN}Running{Colors.ENDC}")
                print(f"  Workers: {Colors.GREEN}{len(stats)}{Colors.ENDC}")

                for worker, info in stats.items():
                    print(f"    - {worker}")
            else:
                print(f"  Status: {Colors.YELLOW}No workers detected{Colors.ENDC}")

        except Exception as e:
            print(f"  Status: {Colors.YELLOW}Not available: {str(e)}{Colors.ENDC}")
        print()

    def capture_queries(self):
        """Capture database queries"""
        # This is a simplified version - in production, you'd use Django debug toolbar or similar
        if connection.queries:
            for query in connection.queries[-5:]:
                sql = query.get('sql', '')
                timestamp = self.format_timestamp()
                self.query_buffer.append((timestamp, sql))

    def refresh_display(self):
        """Refresh the display"""
        self.print_header()
        self.get_database_info()
        self.get_cache_info()
        self.get_celery_info()
        self.get_system_stats()
        self.monitor_database_queries()
        self.monitor_api_requests()
        self.monitor_logs()

        print(f"{Colors.CYAN}Last updated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}{Colors.ENDC}")

    def log_handler(self, record):
        """Handle log records"""
        timestamp = self.format_timestamp()
        self.log_buffer.append((timestamp, record.levelname, record.getMessage()))

    def setup_logging(self):
        """Setup logging handler"""
        class DebugHandler(logging.Handler):
            def __init__(self, debugger):
                super().__init__()
                self.debugger = debugger

            def emit(self, record):
                self.debugger.log_handler(record)

        # Add handler to root logger
        handler = DebugHandler(self)
        handler.setLevel(logging.DEBUG)
        logging.root.addHandler(handler)

    def simulate_activity(self):
        """Simulate activity for demonstration"""
        import logging
        logger = logging.getLogger(__name__)

        while self.running:
            # Log sample messages
            logger.info(f"System check at {datetime.now().strftime('%H:%M:%S')}")

            # Simulate database query
            try:
                from apps.devices.models import Device
                Device.objects.count()

                timestamp = self.format_timestamp()
                self.query_buffer.append((timestamp, "SELECT COUNT(*) FROM devices_device"))

            except:
                pass

            time.sleep(10)

    def run(self):
        """Run the live debugger"""
        self.running = True
        self.setup_logging()

        # Start activity simulator in background
        activity_thread = threading.Thread(target=self.simulate_activity, daemon=True)
        activity_thread.start()

        try:
            while self.running:
                self.refresh_display()
                time.sleep(2)  # Refresh every 2 seconds

        except KeyboardInterrupt:
            self.running = False
            print(f"\n\n{Colors.YELLOW}Live debugging stopped{Colors.ENDC}")

    def run_interactive(self):
        """Run interactive debugging mode"""
        self.print_header()

        print(f"{Colors.BOLD}Interactive Debug Mode{Colors.ENDC}\n")
        print("Available commands:")
        print(f"  {Colors.GREEN}1{Colors.ENDC} - Show database info")
        print(f"  {Colors.GREEN}2{Colors.ENDC} - Show cache info")
        print(f"  {Colors.GREEN}3{Colors.ENDC} - Show Celery info")
        print(f"  {Colors.GREEN}4{Colors.ENDC} - Show system stats")
        print(f"  {Colors.GREEN}5{Colors.ENDC} - Test API endpoints")
        print(f"  {Colors.GREEN}6{Colors.ENDC} - Show recent queries")
        print(f"  {Colors.GREEN}7{Colors.ENDC} - Live monitor mode")
        print(f"  {Colors.GREEN}q{Colors.ENDC} - Quit")

        while True:
            choice = input(f"\n{Colors.CYAN}Enter command: {Colors.ENDC}").strip()

            if choice == '1':
                self.get_database_info()
            elif choice == '2':
                self.get_cache_info()
            elif choice == '3':
                self.get_celery_info()
            elif choice == '4':
                self.get_system_stats()
            elif choice == '5':
                self.test_api_endpoints()
            elif choice == '6':
                self.monitor_database_queries()
            elif choice == '7':
                self.run()
            elif choice.lower() == 'q':
                print(f"{Colors.YELLOW}Exiting...{Colors.ENDC}")
                break
            else:
                print(f"{Colors.RED}Invalid choice{Colors.ENDC}")

    def test_api_endpoints(self):
        """Test API endpoints"""
        print(f"\n{Colors.BLUE}{Colors.BOLD}Testing API Endpoints:{Colors.ENDC}\n")

        from django.test import Client

        client = Client()
        endpoints = [
            ('GET', '/api/v1/health/'),
            ('GET', '/admin/'),
        ]

        for method, path in endpoints:
            try:
                if method == 'GET':
                    response = client.get(path)
                else:
                    response = client.post(path)

                color = Colors.GREEN if response.status_code < 400 else Colors.RED
                print(f"  {method} {path} → {color}{response.status_code}{Colors.ENDC}")

                timestamp = self.format_timestamp()
                self.api_buffer.append((timestamp, method, path, response.status_code))

            except Exception as e:
                print(f"  {method} {path} → {Colors.RED}Error: {str(e)}{Colors.ENDC}")

        print()


def main():
    """Main entry point"""
    debugger = LiveDebugger()

    print(f"{Colors.BOLD}{Colors.BLUE}")
    print("=" * 60)
    print("  MY SPY - LIVE DEBUGGING")
    print("=" * 60)
    print(f"{Colors.ENDC}\n")

    print("Choose mode:")
    print(f"  {Colors.GREEN}1{Colors.ENDC} - Live monitor (auto-refresh)")
    print(f"  {Colors.GREEN}2{Colors.ENDC} - Interactive mode")

    choice = input(f"\n{Colors.CYAN}Enter choice (1 or 2): {Colors.ENDC}").strip()

    if choice == '1':
        debugger.run()
    elif choice == '2':
        debugger.run_interactive()
    else:
        print(f"{Colors.RED}Invalid choice{Colors.ENDC}")
        sys.exit(1)


if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print(f"\n\n{Colors.YELLOW}Debugging interrupted{Colors.ENDC}")
        sys.exit(0)
    except Exception as e:
        print(f"\n{Colors.RED}Fatal error: {str(e)}{Colors.ENDC}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
