from django.contrib import admin
from .models import InstalledApp


@admin.register(InstalledApp)
class InstalledAppAdmin(admin.ModelAdmin):
    list_display = ['app_name', 'package_name', 'version_name', 'device', 'is_system_app', 'install_time']
    list_filter = ['is_system_app', 'device', 'install_time']
    search_fields = ['app_name', 'package_name', 'device__uuid']
    readonly_fields = ['created_at', 'updated_at']
    date_hierarchy = 'install_time'

    fieldsets = (
        ('App Information', {
            'fields': ('app_name', 'package_name', 'version_name', 'version_code')
        }),
        ('Device', {
            'fields': ('device', 'is_system_app')
        }),
        ('Timestamps', {
            'fields': ('install_time', 'update_time', 'created_at', 'updated_at')
        }),
    )
