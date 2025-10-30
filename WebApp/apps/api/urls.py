"""
API URLs
@author Michael KOJDL
@version 1.0.0
"""
from django.urls import path
from . import views

app_name = 'api'

urlpatterns = [
    # Device Registration
    path('register/', views.register_device, name='register'),

    # Data Upload Endpoints
    path('sms/', views.upload_sms, name='upload_sms'),
    path('calls/', views.upload_calls, name='upload_calls'),
    path('location/', views.upload_location, name='upload_location'),

    # Device Status
    path('status/', views.device_status, name='device_status'),
]
