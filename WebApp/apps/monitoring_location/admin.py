from django.contrib import admin
from .models import Location

@admin.register(Location)
class LocationAdmin(admin.ModelAdmin):
    list_display = ['device', 'latitude', 'longitude', 'timestamp', 'address']
    list_filter = ['device']
    search_fields = ['address']
