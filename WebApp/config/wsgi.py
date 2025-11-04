"""
WSGI config for My Spy project
@author Michael KOJDL
@version 2.0.0
"""

import os
from django.core.wsgi import get_wsgi_application

# Use production settings by default, can be overridden by environment variable
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings.production')

application = get_wsgi_application()
