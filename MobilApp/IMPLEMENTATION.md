# My Spy - Android Aplikace - Implementační Průvodce

**Stav:** Základní struktura vytvořena
**Autor:** Michael KOJDL
**Verze:** 1.0.0
**Datum:** 2025-10-30

---

## ✅ Co je hotovo

- ✅ Základní adresářová struktura
- ✅ Gradle build konfigurace (project & app level)
- ✅ Gradle properties
- ✅ Settings gradle
- ✅ AndroidManifest.xml s oprávněními
- ✅ Application class (App.kt)
- ✅ Adresáře pro všechny moduly

---

## 📋 Co zbývá implementovat

Postupujte podle souboru **`MobilniAplikaceAndroid.sourcew`** pro dokončení implementace.

### 1. Core Komponenty (Priorita: VYSOKÁ)

**Lokace:** `app/src/main/java/com/myspy/android/`

#### 1.1 Constants & Config
- [ ] `core/constants/ApiConstants.kt` - API konstanty
- [ ] `core/constants/DbConstants.kt` - Database konstanty
- [ ] `core/constants/AppConstants.kt` - App konstanty
- [ ] `app/config/AppConfig.kt` - Aplikační konfigurace

#### 1.2 Data Layer - Local
- [ ] `data/local/prefs/PrefsManager.kt` - Encrypted SharedPreferences
- [ ] `data/local/db/AppDatabase.kt` - Room Database
- [ ] `data/local/db/entities/` - Všechny entity (SMS, Calls, Location, atd.)
- [ ] `data/local/db/dao/` - Všechny DAO interfaces

#### 1.3 Data Layer - Remote
- [ ] `data/remote/api/ApiService.kt` - Retrofit interface
- [ ] `data/remote/api/ApiClient.kt` - API client setup
- [ ] `data/remote/websocket/WebSocketClient.kt` - WebSocket handler
- [ ] `data/remote/models/` - Data transfer objects

#### 1.4 Repositories
- [ ] `data/repository/DeviceRepository.kt`
- [ ] `data/repository/SmsRepository.kt`
- [ ] `data/repository/CallsRepository.kt`
- [ ] `data/repository/LocationRepository.kt`
- [ ] Další repositories pro každý modul

### 2. Dependency Injection (Priorita: VYSOKÁ)

**Lokace:** `app/di/`

- [ ] `AppModule.kt` - Application-level dependencies
- [ ] `DatabaseModule.kt` - Room database providers
- [ ] `NetworkModule.kt` - Retrofit, OkHttp setup
- [ ] `RepositoryModule.kt` - Repository bindings

### 3. Services (Priorita: VYSOKÁ)

**Lokace:** `services/`

- [ ] `MainService.kt` - Hlavní Foreground Service
- [ ] `SyncService.kt` - Synchronizační service
- [ ] `workers/UploadWorker.kt` - WorkManager worker pro upload
- [ ] `workers/SyncWorker.kt` - Periodická synchronizace

### 4. Moduly - Monitoring (Priorita: STŘEDNÍ)

**Lokace:** `modules/`

#### 4.1 Base Module
- [ ] `base/BaseModule.kt` - Abstract base class pro všechny moduly
- [ ] `base/ModuleManager.kt` - Správce modulů
- [ ] `base/ModuleConfig.kt` - Konfigurace modulů

#### 4.2 SMS Module
- [ ] `sms/SmsModule.kt` - SMS monitoring implementace
- [ ] `sms/SmsCollector.kt` - Collector SMS dat
- [ ] `sms/SmsUploader.kt` - Upload SMS na server

#### 4.3 Calls Module
- [ ] `calls/CallsModule.kt`
- [ ] `calls/CallsCollector.kt`
- [ ] `calls/CallsUploader.kt`

#### 4.4 Location Module
- [ ] `location/LocationModule.kt`
- [ ] `location/LocationTracker.kt`
- [ ] `location/GeocodingService.kt`

#### 4.5 Apps Module
- [ ] `apps/AppsModule.kt`
- [ ] `apps/AppUsageCollector.kt`

#### 4.6 Internet Module
- [ ] `internet/InternetModule.kt`
- [ ] `internet/BrowserHistoryCollector.kt`

#### 4.7 Keylogger Module (⚠️ Vyžaduje souhlas!)
- [ ] `keylogger/KeyloggerModule.kt`
- [ ] `keylogger/KeyloggerService.kt` - AccessibilityService

#### 4.8 Screen Module
- [ ] `screen/ScreenModule.kt`
- [ ] `screen/ScreenCaptureService.kt`
- [ ] `screen/ScreenRecorder.kt`

#### 4.9 Media Module
- [ ] `media/MediaModule.kt`
- [ ] `media/CameraCapture.kt`
- [ ] `media/AudioRecorder.kt`

#### 4.10 Files Module
- [ ] `files/FilesModule.kt`
- [ ] `files/FileSystemProvider.kt`
- [ ] `files/WebDAVHandler.kt`

#### 4.11 Remote Control Module
- [ ] `remote/RemoteModule.kt`
- [ ] `remote/CommandExecutor.kt`
- [ ] `remote/SelfDestructHandler.kt`

### 5. Receivers (Priorita: STŘEDNÍ)

**Lokace:** `receivers/`

- [ ] `BootReceiver.kt` - Boot completed receiver
- [ ] `SmsReceiver.kt` - SMS received receiver
- [ ] `CallReceiver.kt` - Phone state receiver
- [ ] `AdminReceiver.kt` - Device Admin receiver

### 6. Security (Priorita: VYSOKÁ)

**Lokace:** `security/`

- [ ] `encryption/EncryptionManager.kt` - AES encryption
- [ ] `auth/AuthManager.kt` - Authentication handling
- [ ] `antiroot/RootDetector.kt` - Root detection
- [ ] `antitamper/TamperDetector.kt` - Anti-tamper

### 7. UI (Priorita: NÍZKÁ)

**Lokace:** `ui/`

- [ ] `hidden/HiddenActivity.kt` - Skrytá aktivita pro setup
- [ ] `chat/ChatActivity.kt` - My Spy Chat (optional)

### 8. Resources (Priorita: STŘEDNÍ)

**Lokace:** `app/src/main/res/`

- [ ] `values/strings.xml` - String resources
- [ ] `values/colors.xml` - Color resources
- [ ] `values/themes.xml` - Theme definitions
- [ ] `xml/network_security_config.xml` - Network security config
- [ ] `xml/accessibility_service_config.xml` - Accessibility config
- [ ] `xml/device_admin.xml` - Device Admin config

### 9. ProGuard (Priorita: VYSOKÁ pro release)

- [ ] `app/proguard-rules.pro` - Obfuscation rules

### 10. Testing (Priorita: NÍZKÁ)

- [ ] Unit tests pro repositories
- [ ] Unit tests pro modules
- [ ] Instrumentation tests

---

## 🚀 Postup implementace

1. **Fáze 1: Core & Infrastructure**
   - Implementujte Constants, PrefsManager, Database
   - Setup Dependency Injection
   - Implementujte API Client a WebSocket

2. **Fáze 2: Services & Base Module**
   - MainService implementace
   - BaseModule abstrakce
   - ModuleManager

3. **Fáze 3: Moduly postupně**
   - Začněte s SMS a Calls moduly
   - Pak Location a Apps
   - Nakonec složitější moduly (Keylogger, Screen, Media)

4. **Fáze 4: Security & Testing**
   - Security features
   - Testing
   - ProGuard configuration

5. **Fáze 5: Build & Release**
   - Keystore generation
   - Release build
   - Testing na reálném zařízení

---

## 📚 Další zdroje

- **README.md** - Obecná dokumentace Android aplikace
- **MobilniAplikaceAndroid.sourcew** - Detailní step-by-step průvodce
- **docs/API.md** - API dokumentace
- **docs/SECURITY.md** - Bezpečnostní guidelines
- **docs/MODULES.md** - Modulární architektura

---

## ⚠️ Důležité poznámky

1. **Permissions**: Všechna oprávnění musí být requestována runtime
2. **Background limits**: Android 12+ má striktní limity pro background execution
3. **Stealth mode**: Aplikace nesmí mít launcher icon
4. **Security**: Implementujte všechny security features před release
5. **GDPR**: Získejte explicitní souhlas pro monitoring

---

**Autor:** Michael KOJDL
**Poslední aktualizace:** 2025-10-30
