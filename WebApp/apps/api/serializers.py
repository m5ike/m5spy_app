"""
API Serializers
@author Michael KOJDL
@version 1.0.0
"""
from rest_framework import serializers
from apps.devices.models import Device
from apps.monitoring_sms.models import SmsMessage
from apps.monitoring_calls.models import CallLog
from apps.monitoring_location.models import Location


class RegisterDeviceSerializer(serializers.Serializer):
    """Device Registration Serializer"""
    manufacturer = serializers.CharField(max_length=100)
    model = serializers.CharField(max_length=100)
    serial = serializers.CharField(max_length=100)
    os = serializers.CharField(max_length=50)
    os_version = serializers.CharField(max_length=50)
    imei = serializers.CharField(max_length=20)
    uuid = serializers.CharField(max_length=36)

    def validate(self, data):
        # Check for duplicates
        if Device.objects.filter(serial=data['serial']).exists():
            raise serializers.ValidationError({'serial': 'Device with this serial already exists'})
        if Device.objects.filter(imei=data['imei']).exists():
            raise serializers.ValidationError({'imei': 'Device with this IMEI already exists'})
        if Device.objects.filter(uuid=data['uuid']).exists():
            raise serializers.ValidationError({'uuid': 'Device with this UUID already exists'})
        return data

    def create(self, validated_data):
        from apps.users.models import User
        # Assign to admin user (id=1) or first user
        admin_user = User.objects.filter(role=User.Role.ADMIN).first() or User.objects.first()

        device = Device.objects.create(
            owner=admin_user,
            api_key=Device.generate_api_key(),
            **validated_data
        )
        return device


class SmsMessageSerializer(serializers.ModelSerializer):
    """SMS Message Serializer"""

    class Meta:
        model = SmsMessage
        fields = ['message_type', 'address', 'body', 'timestamp', 'thread_id']

    def create(self, validated_data):
        # Add device from request
        device = self.context['request'].device
        return SmsMessage.objects.create(device=device, **validated_data)


class CallLogSerializer(serializers.ModelSerializer):
    """Call Log Serializer"""

    class Meta:
        model = CallLog
        fields = ['call_type', 'number', 'duration', 'timestamp']

    def create(self, validated_data):
        device = self.context['request'].device
        return CallLog.objects.create(device=device, **validated_data)


class LocationSerializer(serializers.ModelSerializer):
    """Location Serializer"""

    class Meta:
        model = Location
        fields = ['latitude', 'longitude', 'accuracy', 'altitude', 'speed', 'address', 'timestamp']

    def create(self, validated_data):
        device = self.context['request'].device
        return Location.objects.create(device=device, **validated_data)
