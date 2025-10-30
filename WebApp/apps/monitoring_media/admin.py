from django.contrib import admin
from .models import MediaFile


@admin.register(MediaFile)
class MediaFileAdmin(admin.ModelAdmin):
    list_display = ['file_name', 'media_type', 'file_size_display', 'device', 'timestamp']
    list_filter = ['media_type', 'device', 'timestamp']
    search_fields = ['file_name', 'file_path', 'device__uuid']
    readonly_fields = ['created_at', 'file_size_mb', 'duration_seconds']
    date_hierarchy = 'timestamp'

    def file_size_display(self, obj):
        return f"{obj.file_size_mb} MB"
    file_size_display.short_description = 'File Size'

    fieldsets = (
        ('File Information', {
            'fields': ('file_name', 'file_path', 'media_type', 'mime_type')
        }),
        ('Metadata', {
            'fields': ('file_size', 'file_size_mb', 'width', 'height', 'duration', 'duration_seconds')
        }),
        ('Device & Timestamp', {
            'fields': ('device', 'timestamp', 'created_at')
        }),
    )
