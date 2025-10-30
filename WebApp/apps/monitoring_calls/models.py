"""
Calls Monitoring Models
@author Michael KOJDL
@version 1.0.0
"""
from django.db import models
from apps.devices.models import Device


class CallLog(models.Model):
    """Call Log Model"""

    class CallType(models.TextChoices):
        INCOMING = 'INCOMING', 'Incoming'
        OUTGOING = 'OUTGOING', 'Outgoing'
        MISSED = 'MISSED', 'Missed'
        REJECTED = 'REJECTED', 'Rejected'
        BLOCKED = 'BLOCKED', 'Blocked'
        UNKNOWN = 'UNKNOWN', 'Unknown'

    device = models.ForeignKey(Device, on_delete=models.CASCADE, related_name='call_logs')
    call_type = models.CharField(max_length=10, choices=CallType.choices)
    number = models.CharField(max_length=20)
    duration = models.IntegerField(help_text='Duration in seconds')
    contact_name = models.CharField(max_length=100, null=True, blank=True)
    timestamp = models.DateTimeField(db_index=True)
    received_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'call_logs'
        verbose_name = 'Call Log'
        verbose_name_plural = 'Call Logs'
        ordering = ['-timestamp']
        indexes = [
            models.Index(fields=['device', '-timestamp']),
            models.Index(fields=['number', '-timestamp']),
        ]

    def __str__(self):
        return f"{self.call_type} - {self.number} - {self.timestamp}"
