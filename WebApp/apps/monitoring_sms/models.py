"""
SMS Monitoring Models
@author Michael KOJDL
@version 1.0.0
"""
from django.db import models
from apps.devices.models import Device


class SmsMessage(models.Model):
    """SMS Message Model"""

    class MessageType(models.TextChoices):
        INCOMING = 'INCOMING', 'Incoming'
        OUTGOING = 'OUTGOING', 'Outgoing'
        DRAFT = 'DRAFT', 'Draft'

    device = models.ForeignKey(Device, on_delete=models.CASCADE, related_name='sms_messages')
    message_type = models.CharField(max_length=10, choices=MessageType.choices)
    address = models.CharField(max_length=20)  # Phone number
    body = models.TextField()
    timestamp = models.DateTimeField(db_index=True)
    thread_id = models.IntegerField(null=True)
    received_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'sms_messages'
        verbose_name = 'SMS Message'
        verbose_name_plural = 'SMS Messages'
        ordering = ['-timestamp']
        indexes = [
            models.Index(fields=['device', '-timestamp']),
            models.Index(fields=['address', '-timestamp']),
        ]

    def __str__(self):
        return f"{self.message_type} - {self.address} - {self.timestamp}"
