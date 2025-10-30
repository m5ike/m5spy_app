from django.db import models
from django.conf import settings
from apps.devices.models import Device


class Screenshot(models.Model):
    """Model for tracking screenshots"""

    device = models.ForeignKey(Device, on_delete=models.CASCADE, related_name='screenshots')
    file_path = models.CharField(max_length=500)
    file_name = models.CharField(max_length=255)
    file_size = models.BigIntegerField()  # in bytes
    width = models.IntegerField()
    height = models.IntegerField()
    timestamp = models.DateTimeField(db_index=True)

    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'monitoring_screenshots'
        ordering = ['-timestamp']
        indexes = [
            models.Index(fields=['device', '-timestamp']),
        ]
        verbose_name = 'Screenshot'
        verbose_name_plural = 'Screenshots'

    def __str__(self):
        return f"{self.file_name} on {self.device.uuid} at {self.timestamp}"

    @property
    def file_size_mb(self):
        """Return file size in megabytes"""
        return round(self.file_size / (1024 * 1024), 2)

    @property
    def resolution(self):
        """Return resolution as string"""
        return f"{self.width}x{self.height}"
