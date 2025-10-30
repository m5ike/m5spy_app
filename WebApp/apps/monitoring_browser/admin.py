from django.contrib import admin
from .models import BrowserHistory


@admin.register(BrowserHistory)
class BrowserHistoryAdmin(admin.ModelAdmin):
    list_display = ['url_short', 'title_short', 'visit_count', 'device', 'timestamp']
    list_filter = ['device', 'timestamp']
    search_fields = ['url', 'title', 'device__uuid']
    readonly_fields = ['created_at']
    date_hierarchy = 'timestamp'

    def url_short(self, obj):
        return obj.url[:80] + '...' if len(obj.url) > 80 else obj.url
    url_short.short_description = 'URL'

    def title_short(self, obj):
        if not obj.title:
            return '-'
        return obj.title[:50] + '...' if len(obj.title) > 50 else obj.title
    title_short.short_description = 'Title'

    fieldsets = (
        ('Browser Information', {
            'fields': ('url', 'title', 'visit_count')
        }),
        ('Device & Timestamp', {
            'fields': ('device', 'timestamp', 'created_at')
        }),
    )
