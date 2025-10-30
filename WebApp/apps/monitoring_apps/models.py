from django.db import models
from django.conf import settings
from apps.devices.models import Device


class InstalledApp(models.Model):
    """Model for tracking installed applications on devices"""

    device = models.ForeignKey(Device, on_delete=models.CASCADE, related_name='installed_apps')
    package_name = models.CharField(max_length=255, db_index=True)
    app_name = models.CharField(max_length=255)
    version_name = models.CharField(max_length=100)
    version_code = models.CharField(max_length=50)
    is_system_app = models.BooleanField(default=False)
    install_time = models.DateTimeField()
    update_time = models.DateTimeField()

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'monitoring_apps'
        ordering = ['-updated_at']
        indexes = [
            models.Index(fields=['device', 'package_name']),
            models.Index(fields=['device', 'is_system_app']),
            models.Index(fields=['-install_time']),
        ]
        unique_together = [['device', 'package_name']]

    def __str__(self):
        return f"{self.app_name} ({self.package_name}) on {self.device.uuid}"
