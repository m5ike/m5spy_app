from django.db import models
from django.conf import settings
from apps.devices.models import Device


class MediaFile(models.Model):
    """Model for tracking media files (photos, videos, audio)"""

    MEDIA_TYPE_CHOICES = [
        ('PHOTO', 'Photo'),
        ('VIDEO', 'Video'),
        ('AUDIO', 'Audio'),
    ]

    device = models.ForeignKey(Device, on_delete=models.CASCADE, related_name='media_files')
    media_type = models.CharField(max_length=10, choices=MEDIA_TYPE_CHOICES, db_index=True)
    file_path = models.CharField(max_length=500)
    file_name = models.CharField(max_length=255)
    file_size = models.BigIntegerField()  # in bytes
    mime_type = models.CharField(max_length=100, null=True, blank=True)
    width = models.IntegerField(null=True, blank=True)
    height = models.IntegerField(null=True, blank=True)
    duration = models.BigIntegerField(null=True, blank=True)  # in milliseconds for video/audio
    timestamp = models.DateTimeField(db_index=True)

    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'monitoring_media'
        ordering = ['-timestamp']
        indexes = [
            models.Index(fields=['device', 'media_type', '-timestamp']),
            models.Index(fields=['file_name']),
        ]
        verbose_name = 'Media File'
        verbose_name_plural = 'Media Files'

    def __str__(self):
        return f"{self.media_type}: {self.file_name} on {self.device.uuid}"

    @property
    def file_size_mb(self):
        """Return file size in megabytes"""
        return round(self.file_size / (1024 * 1024), 2)

    @property
    def duration_seconds(self):
        """Return duration in seconds"""
        if self.duration:
            return round(self.duration / 1000, 2)
        return None
