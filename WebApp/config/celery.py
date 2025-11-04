"""
Celery Configuration
@author Michael KOJDL
@version 2.0.0
"""

import os
from celery import Celery
from celery.schedules import crontab

# Set the default Django settings module for the 'celery' program.
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings.production')

app = Celery('myspy')

# Using a string here means the worker doesn't have to serialize
# the configuration object to child processes.
app.config_from_object('django.conf:settings', namespace='CELERY')

# Load task modules from all registered Django apps.
app.autodiscover_tasks()

# Celery Beat schedule (periodic tasks)
app.conf.beat_schedule = {
    # Example: Clean old data every day at midnight
    'cleanup-old-data': {
        'task': 'apps.monitoring_sms.tasks.cleanup_old_messages',
        'schedule': crontab(hour=0, minute=0),
    },
    # Example: Generate daily reports
    'generate-daily-reports': {
        'task': 'apps.api.tasks.generate_daily_reports',
        'schedule': crontab(hour=1, minute=0),
    },
}

@app.task(bind=True, ignore_result=True)
def debug_task(self):
    """Debug task for testing Celery"""
    print(f'Request: {self.request!r}')
