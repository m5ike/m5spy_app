from django.contrib import admin
from .models import SmsMessage

@admin.register(SmsMessage)
class SmsMessageAdmin(admin.ModelAdmin):
    list_display = ['device', 'message_type', 'address', 'timestamp', 'received_at']
    list_filter = ['message_type', 'device']
    search_fields = ['address', 'body']
    readonly_fields = ['received_at']
