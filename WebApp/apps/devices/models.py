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
