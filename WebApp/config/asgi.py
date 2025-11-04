"""
ASGI config for My Spy project - WebSocket support
@author Michael KOJDL
@version 2.0.0
"""

import os
from django.core.asgi import get_asgi_application
from channels.routing import ProtocolTypeRouter, URLRouter
from channels.auth import AuthMiddlewareStack

# Use production settings by default, can be overridden by environment variable
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings.production')

application = ProtocolTypeRouter({
    "http": get_asgi_application(),
    "websocket": AuthMiddlewareStack(
        URLRouter([
            # WebSocket URL patterns can be added here
        ])
    ),
})
