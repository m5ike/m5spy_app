from django.contrib import admin
from .models import Screenshot


@admin.register(Screenshot)
class ScreenshotAdmin(admin.ModelAdmin):
    list_display = ['file_name', 'resolution', 'file_size_display', 'device', 'timestamp']
    list_filter = ['device', 'timestamp']
    search_fields = ['file_name', 'file_path', 'device__uuid']
    readonly_fields = ['created_at', 'file_size_mb', 'resolution']
    date_hierarchy = 'timestamp'

    def file_size_display(self, obj):
        return f"{obj.file_size_mb} MB"
    file_size_display.short_description = 'File Size'

    fieldsets = (
        ('File Information', {
            'fields': ('file_name', 'file_path')
        }),
        ('Metadata', {
            'fields': ('file_size', 'file_size_mb', 'width', 'height', 'resolution')
        }),
        ('Device & Timestamp', {
            'fields': ('device', 'timestamp', 'created_at')
        }),
    )
