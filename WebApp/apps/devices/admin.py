from django.contrib import admin
from .models import Device

@admin.register(Device)
class DeviceAdmin(admin.ModelAdmin):
    list_display = ['uuid', 'manufacturer', 'model', 'owner', 'is_active', 'last_seen']
    list_filter = ['is_active', 'manufacturer', 'os']
    search_fields = ['uuid', 'manufacturer', 'model', 'serial', 'imei']
    readonly_fields = ['uuid', 'api_key', 'created_at', 'updated_at']
    filter_horizontal = ['shared_with']
