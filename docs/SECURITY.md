# My Spy - Bezpečnostní Dokumentace

**Autor:** Michael KOJDL
**Verze:** 1.0.0
**Poslední revize:** 2025-10-30

---

## Obsah

1. [Bezpečnostní přehled](#bezpečnostní-přehled)
2. [Šifrování](#šifrování)
3. [Autentizace a autorizace](#autentizace-a-autorizace)
4. [Network Security](#network-security)
5. [Data Protection](#data-protection)
6. [Android Security](#android-security)
7. [Web Application Security](#web-application-security)
8. [API Security](#api-security)
9. [GDPR Compliance](#gdpr-compliance)
10. [Security Best Practices](#security-best-practices)

---

## Bezpečnostní přehled

My Spy aplikace implementuje vícevrstv security model:

```
┌─────────────────────────────────────────────┐
│         Security Layers                      │
├─────────────────────────────────────────────┤
│  1. Transport Layer Security (TLS 1.3)      │
│  2. Certificate Pinning                      │
│  3. API Authentication (UUID + API Key)      │
│  4. End-to-End Encryption                   │
│  5. Data-at-Rest Encryption                 │
│  6. Code Obfuscation                        │
│  7. Anti-Tamper Mechanisms                  │
│  8. GDPR Compliance                         │
└─────────────────────────────────────────────┘
```

---

## Šifrování

### 1. Transport Layer Security (TLS)

**Konfigurace:**
- Minimální verze: TLS 1.3
- Cipher suites: pouze silné (AES-256-GCM)
- Perfect Forward Secrecy (PFS)
- HSTS (HTTP Strict Transport Security)

**Implementace (Nginx):**
```nginx
ssl_protocols TLSv1.3;
ssl_ciphers 'ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384';
ssl_prefer_server_ciphers on;
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
```

### 2. Certificate Pinning

**Android implementace:**
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.myspy.fir.ma", "sha256/AAAAAAA...")
    .add("wss.myspy.fir.ma", "sha256/BBBBBBB...")
    .build()

val okHttpClient = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

**Aktualizace pinů:**
- Piny se mění při změně SSL certifikátu
- Implementovat backup pins
- Monitoring expirací certifikátů

### 3. Data-at-Rest Encryption

#### Android

**EncryptedSharedPreferences:**
```kotlin
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

**File Encryption:**
```kotlin
// AES-256 encryption pro lokální soubory
val cipher = Cipher.getInstance("AES/GCM/NoPadding")
cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)
val encrypted = cipher.doFinal(plaintext)
```

#### Server (Django)

**Database Encryption:**
```python
# Field-level encryption
from django_cryptography.fields import encrypt

class SensitiveData(models.Model):
    encrypted_field = encrypt(models.CharField(max_length=255))
```

**Password Hashing:**
```python
# settings.py
PASSWORD_HASHERS = [
    'django.contrib.auth.hashers.Argon2PasswordHasher',
    'django.contrib.auth.hashers.PBKDF2PasswordHasher',
]
```

### 4. End-to-End Encryption (E2EE)

**Pro My Spy Chat:**
```kotlin
// Asymetrické šifrování (RSA-4096)
val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
keyPairGenerator.initialize(4096)
val keyPair = keyPairGenerator.generateKeyPair()

// Symetrické šifrování zpráv (AES-256)
val symmetricKey = KeyGenerator.getInstance("AES").apply {
    init(256)
}.generateKey()
```

---

## Autentizace a autorizace

### Device Authentication

**Mechanismus:**
1. Zařízení generuje UUID při první instalaci
2. Registrace na serveru → získání API Key
3. Každý request obsahuje UUID + API Key v headers
4. Server validuje kombinaci

**Security measures:**
- API Key: 20+ znaků, kryptograficky bezpečný
- Rate limiting na registraci (5/hour)
- Blacklisting kompromitovaných zařízení
- Regular rotation doporučena každých 90 dní

### User Authentication (Web)

**Multi-layer approach:**
```python
# 1. Session-based authentication
SESSION_COOKIE_SECURE = True
SESSION_COOKIE_HTTPONLY = True
SESSION_COOKIE_SAMESITE = 'Strict'
SESSION_COOKIE_AGE = 86400  # 24 hours

# 2. Token authentication (API)
REST_FRAMEWORK = {
    'DEFAULT_AUTHENTICATION_CLASSES': [
        'rest_framework.authentication.TokenAuthentication',
    ]
}

# 3. JWT pro long-lived sessions
SIMPLE_JWT = {
    'ACCESS_TOKEN_LIFETIME': timedelta(hours=1),
    'REFRESH_TOKEN_LIFETIME': timedelta(days=7),
    'ROTATE_REFRESH_TOKENS': True,
}
```

### Role-Based Access Control (RBAC)

```python
class Permissions:
    ADMIN_FULL_ACCESS = ['*']

    USER_PERMISSIONS = [
        'view_own_devices',
        'manage_own_devices',
        'view_monitoring_data',
        'send_commands',
    ]

    GUEST_PERMISSIONS = [
        'view_demo_data',
    ]
```

---

## Network Security

### 1. Firewall Rules

```bash
# UFW Configuration
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp    # SSH (změnit port!)
sudo ufw allow 80/tcp    # HTTP (redirect to HTTPS)
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

### 2. DDoS Protection

**Rate Limiting (Nginx):**
```nginx
limit_req_zone $binary_remote_addr zone=api:10m rate=100r/s;
limit_req_zone $binary_remote_addr zone=login:10m rate=5r/m;

location /api/ {
    limit_req zone=api burst=20 nodelay;
}

location /auth/login/ {
    limit_req zone=login burst=2 nodelay;
}
```

**Django Rate Limiting:**
```python
# settings.py
REST_FRAMEWORK = {
    'DEFAULT_THROTTLE_CLASSES': [
        'rest_framework.throttling.AnonRateThrottle',
        'rest_framework.throttling.UserRateThrottle'
    ],
    'DEFAULT_THROTTLE_RATES': {
        'anon': '100/hour',
        'user': '1000/hour',
        'device': '5000/hour'
    }
}
```

### 3. SQL Injection Prevention

**Django ORM:**
```python
# BEZPEČNÉ - používat vždy ORM
Device.objects.filter(uuid=user_input)

# NEBEZPEČNÉ - nikdy!
cursor.execute(f"SELECT * FROM devices WHERE uuid='{user_input}'")

# Pokud raw SQL je nutný - parametrizované queries
cursor.execute("SELECT * FROM devices WHERE uuid = %s", [user_input])
```

### 4. XSS Protection

**Headers:**
```python
# settings.py
SECURE_BROWSER_XSS_FILTER = True
SECURE_CONTENT_TYPE_NOSNIFF = True

# CSP (Content Security Policy)
CSP_DEFAULT_SRC = ("'self'",)
CSP_SCRIPT_SRC = ("'self'", "'unsafe-inline'", "cdn.example.com")
CSP_STYLE_SRC = ("'self'", "'unsafe-inline'")
```

**Template Escaping:**
```django
{# Auto-escaped by default #}
{{ user_input }}

{# Manually mark as safe only when necessary #}
{{ trusted_html|safe }}
```

### 5. CSRF Protection

```python
# settings.py
CSRF_COOKIE_SECURE = True
CSRF_COOKIE_HTTPONLY = True
CSRF_COOKIE_SAMESITE = 'Strict'
CSRF_USE_SESSIONS = True
```

---

## Data Protection

### Data Minimization

Sbírat pouze nezbytná data:
- ✅ SMS/Calls: Jen metadata (ne kompletní obsah bez souhlasu)
- ✅ Location: Pouze když je opravdu potřeba
- ✅ Keylogger: OFF by default, vyžaduje explicitní souhlas

### Data Retention

```python
# Automatické mazání starých dat
RETENTION_POLICY = {
    'sms_messages': 90,      # days
    'call_logs': 90,
    'locations': 365,
    'screenshots': 30,
    'videos': 30,
    'keylogger': 7,         # krátká retence!
}
```

### Data Anonymization

```python
def anonymize_phone_number(phone):
    """Anonymizace telefonního čísla"""
    return f"+***{phone[-4:]}"

def pseudonymize_data(data):
    """Pseudonymizace citlivých dat"""
    return hashlib.sha256(data.encode()).hexdigest()
```

### Secure Deletion

```python
def secure_delete_file(filepath):
    """Bezpečné smazání souboru"""
    # 1. Přepsat random daty (3x)
    with open(filepath, 'ba+') as f:
        length = f.tell()
        for _ in range(3):
            f.seek(0)
            f.write(os.urandom(length))

    # 2. Smazat soubor
    os.remove(filepath)
```

---

## Android Security

### 1. Code Obfuscation

**ProGuard/R8 konfigurace:**
```proguard
# Základní obfuskace
-repackageclasses 'com.android.systemcore.o'
-allowaccessmodification
-overloadaggressively
-dontskipnonpubliclibraryclasses

# String encryption
-obfuscationdictionary dictionary.txt
-packageobfuscationdictionary dictionary.txt
-classobfuscationdictionary dictionary.txt

# Remove logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
```

### 2. Root Detection

```kotlin
object RootDetector {
    fun isRooted(): Boolean {
        return checkBuildTags() ||
               checkSuperuserApk() ||
               checkSuBinary() ||
               checkRWPaths() ||
               checkDangerousProps()
    }

    private fun checkSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        return paths.any { File(it).exists() }
    }
}
```

### 3. Debugger Detection

```kotlin
object AntiDebug {
    fun isDebuggable(): Boolean {
        return Debug.isDebuggerConnected() ||
               checkDebugBuildConfig() ||
               checkTracerPid()
    }

    private fun checkTracerPid(): Boolean {
        try {
            val status = File("/proc/self/status").readText()
            val tracerPid = status.lines()
                .find { it.startsWith("TracerPid:") }
                ?.substringAfter(":")?.trim()?.toInt() ?: 0
            return tracerPid != 0
        } catch (e: Exception) {
            return false
        }
    }
}
```

### 4. Emulator Detection

```kotlin
object EmulatorDetector {
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.contains("vbox") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.PRODUCT.contains("sdk"))
    }
}
```

### 5. Certificate Validation

```kotlin
// Validace podpisu APK
fun validateAppSignature(context: Context): Boolean {
    try {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNATURES
        )
        val signatures = packageInfo.signatures
        val expectedSignature = "YOUR_EXPECTED_SIGNATURE_HASH"

        return signatures.any {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(it.toByteArray())
            hash.toHex() == expectedSignature
        }
    } catch (e: Exception) {
        return false
    }
}
```

---

## Web Application Security

### Security Headers

```python
# settings.py - Production
SECURE_SSL_REDIRECT = True
SECURE_HSTS_SECONDS = 31536000
SECURE_HSTS_INCLUDE_SUBDOMAINS = True
SECURE_HSTS_PRELOAD = True

SECURE_CONTENT_TYPE_NOSNIFF = True
SECURE_BROWSER_XSS_FILTER = True

X_FRAME_OPTIONS = 'DENY'

# CSP
CSP_DEFAULT_SRC = ("'self'",)
CSP_SCRIPT_SRC = ("'self'", "'unsafe-inline'")
CSP_STYLE_SRC = ("'self'", "'unsafe-inline'")
CSP_IMG_SRC = ("'self'", "data:", "https:")
CSP_FONT_SRC = ("'self'", "data:")
```

### Input Validation

```python
from django.core.validators import RegexValidator

class DeviceSerializer(serializers.ModelSerializer):
    # Validace UUID
    uuid = serializers.CharField(
        validators=[
            RegexValidator(
                regex=r'^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$',
                message='Invalid UUID format'
            )
        ]
    )

    # Validace phone number
    phone = serializers.CharField(
        validators=[
            RegexValidator(
                regex=r'^\+?1?\d{9,15}$',
                message='Invalid phone number'
            )
        ]
    )
```

### Authentication Hardening

```python
# Password policy
AUTH_PASSWORD_VALIDATORS = [
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
        'OPTIONS': {'min_length': 12}
    },
    {
        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',
    },
]

# Account lockout
AXES_FAILURE_LIMIT = 5
AXES_COOLOFF_TIME = timedelta(minutes=30)
AXES_LOCK_OUT_BY_COMBINATION_USER_AND_IP = True
```

---

## API Security

### API Key Management

```python
# Generování bezpečných API keys
import secrets

def generate_api_key(length=32):
    return secrets.token_urlsafe(length)

# Hashing API keys v databázi
import hashlib

def hash_api_key(api_key):
    return hashlib.sha256(api_key.encode()).hexdigest()
```

### Request Signing

```python
# HMAC signing pro kritické operace
import hmac

def sign_request(payload, secret):
    return hmac.new(
        secret.encode(),
        payload.encode(),
        hashlib.sha256
    ).hexdigest()

def verify_signature(payload, signature, secret):
    expected = sign_request(payload, secret)
    return hmac.compare_digest(expected, signature)
```

---

## GDPR Compliance

### Data Subject Rights

```python
# Právo na přístup k datům
def export_user_data(user):
    """Export všech dat uživatele"""
    data = {
        'profile': UserSerializer(user).data,
        'devices': DeviceSerializer(user.owned_devices.all(), many=True).data,
        'sms': SmsSerializer(SmsMessage.objects.filter(device__owner=user), many=True).data,
        # ... další data
    }
    return data

# Právo na výmaz
def delete_user_data(user):
    """Kompletní výmaz všech dat uživatele"""
    # Soft delete nebo hard delete podle požadavků
    user.owned_devices.all().delete()
    user.is_active = False
    user.email = f"deleted_{user.id}@deleted.com"
    user.save()
```

### Consent Management

```python
class ConsentModel(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    consent_type = models.CharField(max_length=50)
    given_at = models.DateTimeField(auto_now_add=True)
    withdrawn_at = models.DateTimeField(null=True, blank=True)
    ip_address = models.GenericIPAddressField()
    user_agent = models.TextField()

    def is_valid(self):
        return self.withdrawn_at is None
```

---

## Security Best Practices

### Development

- ✅ Nikdy necommitovat secrets do Git
- ✅ Používat environment variables (.env)
- ✅ Regular dependency updates
- ✅ Code review všech změn
- ✅ Static code analysis (SonarQube, Bandit)

### Deployment

- ✅ Používat pouze HTTPS
- ✅ Firewall konfigurace
- ✅ Regular security patches
- ✅ Monitoring a alerting
- ✅ Regular penetration testing

### Operations

- ✅ Regular backups (encrypted)
- ✅ Audit logging
- ✅ Incident response plan
- ✅ Security training pro tým

---

## Security Checklist

### Pre-deployment

- [ ] All secrets v environment variables
- [ ] SSL certifikáty platné
- [ ] Database credentials změněny z default
- [ ] Debug mode OFF
- [ ] Firewall nakonfigurován
- [ ] Rate limiting aktivní
- [ ] Security headers nastaveny
- [ ] GDPR consent flow implementován
- [ ] Penetration test proveden

### Post-deployment

- [ ] Monitoring nastaven
- [ ] Alerting funkční
- [ ] Backup automatizace běží
- [ ] Log rotation nakonfigurována
- [ ] Security audit naplánován

---

## Incident Response

### V případě security incidentu:

1. **Containment** - Izolovat kompromitovaný systém
2. **Assessment** - Určit rozsah incidentu
3. **Notification** - Informovat affected users (GDPR 72h)
4. **Remediation** - Opravit vulnerability
5. **Recovery** - Obnovit služby
6. **Post-mortem** - Analýza a lessons learned

---

**Autor:** Michael KOJDL
**Poslední aktualizace:** 2025-10-30
**Verze:** 1.0.0
