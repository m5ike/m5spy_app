"""
Location Monitoring Models
@author Michael KOJDL
@version 1.0.0
"""
from django.db import models
from apps.devices.models import Device


class Location(models.Model):
    """GPS Location Model"""

    device = models.ForeignKey(Device, on_delete=models.CASCADE, related_name='locations')
    latitude = models.FloatField()
    longitude = models.FloatField()
    accuracy = models.FloatField(null=True, blank=True, help_text='Accuracy in meters')
    altitude = models.FloatField(null=True, blank=True, help_text='Altitude in meters')
    speed = models.FloatField(null=True, blank=True, help_text='Speed in m/s')
    address = models.CharField(max_length=255, blank=True)
    timestamp = models.DateTimeField(db_index=True)
    received_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'locations'
        verbose_name = 'Location'
        verbose_name_plural = 'Locations'
        ordering = ['-timestamp']
        indexes = [
            models.Index(fields=['device', '-timestamp']),
        ]

    def __str__(self):
        return f"{self.device} - {self.latitude}, {self.longitude} - {self.timestamp}"
