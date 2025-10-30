from django.contrib import admin
from .models import CallLog

@admin.register(CallLog)
class CallLogAdmin(admin.ModelAdmin):
    list_display = ['device', 'call_type', 'number', 'duration', 'timestamp']
    list_filter = ['call_type', 'device']
    search_fields = ['number']
