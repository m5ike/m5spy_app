"""
User Models
@author Michael KOJDL
@version 1.0.0
"""
from django.contrib.auth.models import AbstractUser
from django.db import models
import secrets


class User(AbstractUser):
    """Custom User Model with roles and API key"""

    class Role(models.TextChoices):
        ADMIN = 'ADMIN', 'Administrator'
        USER = 'USER', 'User'
        GUEST = 'GUEST', 'Guest'

    role = models.CharField(max_length=10, choices=Role.choices, default=Role.USER)
    phone = models.CharField(max_length=20, blank=True)
    address = models.TextField(blank=True)
    api_key = models.CharField(max_length=64, unique=True, blank=True, editable=False)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'users'
        verbose_name = 'User'
        verbose_name_plural = 'Users'

    def save(self, *args, **kwargs):
        if not self.api_key:
            self.api_key = secrets.token_urlsafe(32)
        super().save(*args, **kwargs)

    @property
    def is_admin(self):
        return self.role == self.Role.ADMIN
