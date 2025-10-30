"""
API Authentication
@author Michael KOJDL
@version 1.0.0
"""
from rest_framework import authentication, exceptions
from apps.devices.models import Device


class DeviceAuthentication(authentication.BaseAuthentication):
    """
    Device authentication using UUID and API Key headers
    """

    def authenticate(self, request):
        uuid = request.META.get('HTTP_X_DEVICE_UUID')
        api_key = request.META.get('HTTP_X_API_KEY')

        if not uuid or not api_key:
            return None

        try:
            device = Device.objects.select_related('owner').get(
                uuid=uuid,
                api_key=api_key,
                is_active=True
            )
            # Store device in request for later use
            request.device = device
            # Return user and auth object
            return (device.owner, device)
        except Device.DoesNotExist:
            raise exceptions.AuthenticationFailed('Invalid device credentials')
