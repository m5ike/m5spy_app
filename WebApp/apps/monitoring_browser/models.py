from django.db import models
from django.conf import settings
from apps.devices.models import Device


class BrowserHistory(models.Model):
    """Model for tracking browser history"""

    device = models.ForeignKey(Device, on_delete=models.CASCADE, related_name='browser_history')
    url = models.URLField(max_length=2048)
    title = models.CharField(max_length=500, null=True, blank=True)
    visit_count = models.IntegerField(default=1)
    timestamp = models.DateTimeField(db_index=True)

    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'monitoring_browser_history'
        ordering = ['-timestamp']
        indexes = [
            models.Index(fields=['device', '-timestamp']),
            models.Index(fields=['url']),
        ]
        verbose_name = 'Browser History Entry'
        verbose_name_plural = 'Browser History'

    def __str__(self):
        return f"{self.url[:50]} on {self.device.uuid} at {self.timestamp}"
