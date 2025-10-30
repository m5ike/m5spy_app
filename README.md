# My Spy - Projektová Dokumentace

**Autor:** Michael KOJDL
**Verze:** 1.0.0
**Poslední revize:** 2025-10-30
**Repository:**
- SSH: `git@github.com:m5ike/m5spy_app.git`
- HTTPS: `https://github.com/m5ike/m5spy_app.git`

---

## Obsah

1. [Úvod](#úvod)
2. [Účel a cíle projektu](#účel-a-cíle-projektu)
3. [Architektura projektu](#architektura-projektu)
4. [Použité technologie](#použité-technologie)
5. [Funkční specifikace](#funkční-specifikace)
6. [Bezpečnostní aspekty](#bezpečnostní-aspekty)
7. [Instalace a nasazení](#instalace-a-nasazení)
8. [Struktura projektu](#struktura-projektu)
9. [Dokumentace komponent](#dokumentace-komponent)
10. [API Dokumentace](#api-dokumentace)
11. [Rizika a právní aspekty](#rizika-a-právní-aspekty)

---

## Úvod

Aplikace "My Spy" je komplexní monitorovací platforma navržená pro zabezpečení digitální bezpečnosti a rodičovskou kontrolu. Systém se skládá ze tří hlavních komponent:

- **Mobilní aplikace (Android)** - Klientská aplikace pro monitorování
- **Webová aplikace** - Serverová část s administračním rozhraním
- **Serverová infrastruktura** - Konfigurace a skripty pro provoz

---

## Účel a cíle projektu

Aplikace "My Spy" je navržena jako platforma pro:

### Hlavní cíle

* ✅ **Monitorování a řízení** internetové aktivity, používání počítače a mobilu
* ✅ **Zajištění vyšší úrovně** digitální bezpečnosti a informovanosti
* ✅ **Transparentní reportování** s detailními statistikami a grafy
* ✅ **Bezpečná komunikace** mezi správcem a monitorovaným uživatelem (např. rodič-dítě)
* ✅ **Maximální uživatelská přizpůsobitelnost** - možnost vypnout či zapnout jednotlivé monitorovací moduly
* ✅ **Modulární architektura** - snadné přidávání a odebírání funkčních modulů

### Klíčové vlastnosti

- **Transparentnost**: Všechny funkce lze vypnout/zapnout
- **Bezpečnost**: End-to-end šifrování komunikace
- **Flexibilita**: Modulární design umožňující rozšiřování
- **Škálovatelnost**: Podpora více zařízení z jednoho účtu

---

## Architektura projektu

### Přehled architektury

```
┌─────────────────────────────────────────────────────────────┐
│                     My Spy Ecosystem                         │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐         ┌──────────────┐                  │
│  │   Android    │◄───────►│  WebSocket   │                  │
│  │  Application │  WSS    │    Server    │                  │
│  │              │         │              │                  │
│  │  (Stealth)   │◄───────►│  API Server  │                  │
│  └──────────────┘  HTTPS  └──────┬───────┘                  │
│                                   │                          │
│                           ┌───────▼────────┐                 │
│  ┌──────────────┐         │   Web App      │                 │
│  │   Browser    │◄───────►│   (Django)     │                 │
│  │   Client     │  HTTPS  │                │                 │
│  └──────────────┘         └────────┬───────┘                 │
│                                    │                          │
│                           ┌────────▼────────┐                 │
│                           │   PostgreSQL    │                 │
│                           │   Redis/Memcache│                 │
│                           │   RabbitMQ      │                 │
│                           └─────────────────┘                 │
└─────────────────────────────────────────────────────────────┘
```

### Komunikační flow

1. **Registrace zařízení**: Android app → API Server → Database
2. **Autentizace**: UUID + API Key validace
3. **Data streaming**: WebSocket (WSS) pro real-time data
4. **API volání**: REST API (HTTPS) pro operace CRUD
5. **Web interface**: Django framework s Tailwind CSS

---

## Použité technologie

### Mobilní aplikace (Android)

- **IDE**: Android Studio 2025.1.4
- **Min SDK**: Android 12 (API 31+)
- **Jazyk**: Kotlin / Java
- **Knihovny**:
  - OkHttp3 - HTTP/WebSocket komunikace
  - Room Database - Lokální úložiště
  - WorkManager - Background tasks
  - Retrofit - API komunikace
  - Encryption - AES-256, RSA

### Webová aplikace

- **Backend**: Python 3.13
- **Framework**: Django (latest stable)
- **Premium template**: Rocket Django PRO
- **Frontend**:
  - Tailwind CSS
  - Flowbite
  - ApexCharts
  - React Integration
- **Features**:
  - ✅ Dynamic Tables - build server-side dataTables for any model
  - ✅ Dynamic API - expose secure APIs on top of DRF
  - ✅ Charts via ApexJS
  - ✅ React Integration
  - ✅ Django CLI Package
  - ✅ Session-based Authentication, Password recovery
  - ✅ Celery (async tasks)
  - ✅ Docker support

### Serverová infrastruktura

- **OS**: Debian 12.12 nebo Docker
- **Web server**: Nginx / Apache2
- **Database**: PostgreSQL / MySQL / SQLite
- **Cache**: Redis + Memcached
- **Message Queue**: RabbitMQ
- **Task Queue**: Celery
- **WebSocket**: Django Channels / WebSocket Secure Server
- **Virtual Environment**: Conda

---

## Funkční specifikace

### Mobilní aplikace (Android)

#### 1. Monitoring SMS a hovorů
- Zaznamenávání přijatých a odeslaných SMS zpráv
- Uskutečněné a přijaté hovory
- Délka hovorů
- Časové razítko všech komunikací

#### 2. Sledování pohybu uživatele
- Geolokace pomocí GPS
- Historie pohybu s časovým razítkem
- KML export pro vizualizaci trasy
- Výpočet rychlosti a vzdálenosti

#### 3. Monitoring používaných aplikací
- Přehled spuštěných aplikací
- Čas strávený v jednotlivých aplikacích
- Statistiky používání
- Možnost blokování aplikací

#### 4. Komunikační modul "My Spy Chat"
- End-to-end šifrovaná komunikace
- Komunikace mezi správcem a monitorovaným uživatelem
- Push notifikace
- Historie konverzací

#### 5. Ochrana před odinstalací
- Device Admin API
- Skrytí z app drawer
- Ochrana proti factory reset
- Self-defense mechanismy

#### 6. Monitoring internetové aktivity
- Sledování navštívených URL
- Timestamp návštěv
- Blokování specifických domén
- SSL/TLS traffic analysis (pokud root)

#### 7. Monitoring spuštěných programů
- Seznam aktivních procesů
- Foreground/background detection
- Statistiky využití CPU/RAM
- Možnost killování procesů

#### 8. Keylogger
- Záznam stisků kláves
- Context-aware logging (aplikace, okno)
- Možnost vypnutí/zapnutí
- Ochrana heslem pro přístup k datům

#### 9. Záznam obrazovky / Screenshoty
- **Režimy**:
  - Periodický (interval v sekundách)
  - Rozvrh (časové okno)
  - On-demand (z web aplikace)
  - Event-based (při spuštění vybrané aplikace)
- **Možnosti**:
  - Počet obrázků a delay
  - Vykreslení tap/click míst
  - Komprese a kvalita

#### 10. Video a Audio záznam
- **Video z kamery**:
  - Přední/zadní kamera
  - Rozlišení a kvalita
  - Se zvukem / bez zvuku
  - Maximální délka souboru
- **Screen recording**:
  - Záznam obrazovky
  - Vykreslení touch událostí
  - Délka záznamu
- **Audio z mikrofonu**:
  - Maximální délka souboru
  - Kvalita záznamu
  - Plán nahrávání
- **Pravidla spuštění**:
  - Zamčený telefon
  - Display OFF
  - Kdykoli

#### 11. Remote File Browser
- **WebDAV over WebSocket**
- **Operace**:
  - Copy, Move, Rename, Delete
  - New File, New Directory
  - chown, chmod
  - lsattr, chattr
  - Download, Upload
- **Filesystem**:
  - Internal storage
  - SD card
  - Všechna dostupná úložiště

#### 12. Vzdálené bezpečnostní funkce

##### 12.1 Kompletní samo-vymazání
- Smazání aplikace
- Smazání všech dat a nastavení
- Smazání logů a dočasných souborů
- Vymazání stop o instalaci

##### 12.2 Black Display
- Černá obrazovka
- Znemožnění interakce
- Simulace zamrznutí

##### 12.3 Frozen Display
- Zamrznutí na aktuálním obrazu
- Zablokování touch událostí

##### 12.4 WiFi konfigurace
- Zapnout/vypnout
- Rozvrh zapnutí/vypnutí
- Seznam známých sítí
- Detaily sítí včetně hesel
- Přidání/úprava/odebrání sítě

##### 12.5 Bluetooth konfigurace
- Zapnout/vypnout dle rozvrhu
- Seznam známých zařízení
- Přidání/odebrání párování

##### 12.6 Statistiky a diagnostika
- Statistiky všech network interfaces
- Síťové protokoly
- Diagnostika senzorů
- Akcelerometr, gyroskop
- Světelný senzor, proximity
- Hlukový sensor
- Všechny dostupné systémové informace

#### 13. Stealth mode
- Aplikace neviditelná pro uživatele
- Není na ploše
- Není v seznamu aplikací
- Maskování jako systémová komponenta
- Možnost vypnutí pouze z Web aplikace

#### 14. Registrace a komunikace

##### 14.1 První spuštění - registerNewDevice
**Request (POST /api/v1/register)**:
```json
{
  "manufacturer": "Samsung",
  "model": "Galaxy S23",
  "serial": "ABC123XYZ",
  "os": "Android",
  "os_version": "13",
  "imei": "123456789012345",
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response**:
```json
{
  "result": "ok",
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "api_key": "A1B2C3D4E5F6G7H8I9J0",
  "timestamp": "2025-10-30T12:34:56Z",
  "version": "1.0.0"
}
```

**Chyby**:
- Duplicitní serial: `HTTP 409 Conflict`
- Duplicitní UUID: `HTTP 409 Conflict`
- Duplicitní IMEI: `HTTP 409 Conflict`

##### 14.2 Komunikační protokoly
- **API format**: `application/json`
- **API endpoint**: `https://api.myspy.fir.ma`
- **WebSocket endpoint**: `wss://wss.myspy.fir.ma`
- **Autentizace**: UUID + API Key v každém requestu

---

### Webová aplikace - Server

#### 1. Uživatelské role

##### 1.1 Administrator
- Neomezený přístup ke všem modulům
- Správa uživatelů
- Správa všech registrací
- Systémová nastavení

##### 1.2 User
- Přístup k vlastnímu profilu
- Správa vlastních registrací zařízení
- Přístup k nesystémovým modulům
- Možnost sdílení registrací

##### 1.3 Guest
- Pouze přihlášení
- Ukázková data
- Read-only přístup

#### 2. Správa uživatelů
- **Profil**:
  - Jméno, příjmení
  - Adresa, email, telefon
  - Role
  - Přihlašovací údaje (login, heslo)
  - User API Key
- **Logy**:
  - Datum a čas přidání
  - Historie přihlášení
  - Audit log aktivit

#### 3. Správa registrací zařízení
- Seznam registrovaných zařízení
- Editace údajů (UUID, API Key)
- Přiřazování k uživatelům
- Sdílení registrace mezi více uživateli
- Dashboard pro každé zařízení

#### 4. Dashboard registrace
- **Editovatelná plocha**:
  - Přidávání řádků
  - Rozdělení na sloupce
  - Drag & drop widgety
  - Uložení layoutu per-user
- **Widgety**: (viz níže pro jednotlivé moduly)

#### 5. Monitoring internetové aktivity

##### Widgety:
1. **Vyhledávání**
   - Filter podle URL
   - Filter podle času
   - Full-text search

2. **Časová osa**
   - Zobrazení: den / týden / měsíc / vlastní datum
   - Timestamp
   - Seznam navštívených stránek
   - Detail po kliknutí
   - Quick actions: přidat k blokovaným/sledovaným

3. **Seznamy**
   - Blokované stránky
   - Sledované stránky
   - Ostatní
   - Editace a odebrání ze seznamu

4. **Detail stránky**
   - Historie návštěv
   - Celkový čas na stránce
   - Screenshot historie (pokud dostupné)

#### 6. Monitoring spuštěných aplikací

##### Widgety:
1. **Vyhledávání**
   - Podle názvu
   - Podle cesty
   - Podle času spuštění

2. **Časová osa**
   - Den / týden / měsíc
   - Seznam aplikací s timeline
   - Detail po kliknutí

3. **Seznamy**
   - Blokované aplikace
   - Sledované aplikace
   - Statistiky používání

#### 7. Keylogger / Mouse / Tap monitoring

##### Widgety:
1. **Vyhledávání**
   - Podle času
   - Podle názvu aplikace
   - Podle názvu okna
   - Full-text v zaznamenaném textu

2. **Časová osa**
   - Timeline záznamu
   - Grouped by application
   - Detail po kliknutí

3. **Detail view**
   - Rekonstrukce textu
   - Metadata (aplikace, okno, čas)
   - Export do TXT

#### 8. Řízení času zařízení
- Nastavení maximální denní doby
- Plánování dostupnosti
- Time-out notifikace
- Statistiky dodržování limitů

#### 9. SMS a hovory

##### Widgety:
1. **Vyhledávání**
   - Podle času
   - Podle příjemce/odesílatele
   - Podle trvání (hovory)
   - Full-text v obsahu SMS

2. **Časová osa**
   - Timeline hovorů a zpráv
   - Detail po kliknutí

3. **Detail**
   - Pro konkrétní číslo
   - Historie komunikace
   - Statistiky

4. **Operace**
   - Editace call logu
   - Mazání záznamů
   - Editace/přidání/mazání SMS
   - Odeslání SMS
   - Uskutečnění hovoru
   - USSD kódy

##### Hooks a pravidla

**Eventy**:
- SMS přijaté
- SMS odeslané
- SMS uložené
- Příchozí hovor
- Odchozí hovor

**Filtry (regex)**:
- SMS: odesílatel, příjemce, text
- Hovory: volaný, volající

**Akce**:
1. **notify** - Odeslání notifikace
   - SMS (Nexmo API)
   - Email
   - WebApp log

2. **block** - Zablokování
   - Hovor: odmítnutí + busy tone
   - SMS: zabránění uložení/příjmu

3. **accept** - Normální chování

4. **silent** - Přijmout bez notifikace

5. **masquerade** - Maskování/úprava
   - SMS: změna odesílatele, příjemce, textu, času
   - Hovor: změna volaného/volajícího, času

#### 10. Sledování GPS polohy

##### Widgety:
1. **Seznam změn lokací**
   - Poslední den / týden / měsíc
   - Adresa + timestamp
   - Filter podle místa

2. **Mapa**
   - Vizualizace trasy
   - Timeline overlay
   - KML export
   - Výpočet rychlosti
   - Značky významných míst

#### 11. Video/Screenshot/Audio záznamy

##### Widgety:
1. **Vyhledávání**
   - Podle času
   - Podle aplikace
   - Podle typu (video/screenshot/audio)

2. **Časová osa médií**
   - Timeline zobrazení
   - Thumbnail preview
   - Filter: IMG / VID / AUDIO / ALL
   - Order by timestamp

3. **Media player modal**
   - Po kliknutí otevře modal
   - Video/audio player
   - Image viewer
   - Download option
   - Delete option

#### 12. Vzdálené bezpečnostní funkce

##### Widget - Control Panel:
- **Speed buttons**:
  - Self-destruct
  - Black Display ON/OFF
  - Frozen Display ON/OFF
  - WiFi ON/OFF
  - Bluetooth ON/OFF
- **Stavové okno**:
  - Real-time status
  - Výsledek operace
  - Error handling

#### 13. Remote File Browser

##### Widget - File Manager:

**Typy zobrazení**:
1. **Details view**
   - Název, typ, upraveno, vytvořeno
   - Velikost, vlastník, práva
   - Cesta

2. **Grid view**
   - Velké ikony typů souborů
   - Název pod ikonou
   - Thumbnail preview (obrázky)

3. **Carousel view**
   - Carousel souborů
   - Velké okno pro detail/náhled
   - Quick actions

**Operace**:
- Context menu (pravé tlačítko / long tap):
  - Delete (s potvrzením)
  - Rename
  - Copy
  - Move
  - Download (multi-files → ZIP)
  - Preview (single file only)
  - Details (single file only)
  - chown, chmod
  - lsattr, chattr

- Keyboard shortcuts:
  - SHIFT - multi-select
  - CTRL (CMD) - multi-select

**Main Menu - Files**:
- New File
- New Directory
- Upload
- Refresh

---

## Bezpečnostní aspekty

### 1. Šifrování dat

#### 1.1 Data at Rest
- **Database**: AES-256 encryption
- **File storage**: Encrypted filesystem nebo per-file encryption
- **API Keys**: Bcrypt hash
- **Passwords**: Argon2id

#### 1.2 Data in Transit
- **API komunikace**: TLS 1.3 minimum
- **WebSocket**: WSS (WebSocket Secure)
- **Certificate pinning**: Android app

### 2. Autentizace a autorizace

#### 2.1 Registrace zařízení
- UUID validace
- IMEI/Serial number verification
- Rate limiting na registraci
- Whitelist/blacklist

#### 2.2 API Autentizace
- UUID + API Key kombinace
- JWT tokens pro web aplikaci
- Refresh token rotation
- Token expiry: 1 hodina (access), 7 dní (refresh)

#### 2.3 Web aplikace
- Session-based authentication
- CSRF protection
- XSS protection
- SQL injection prevention (ORM)
- Rate limiting

### 3. Self-Defense mechanismy

#### 3.1 Android aplikace
- Obfuscation (ProGuard/R8)
- Root detection
- Debugger detection
- Emulator detection
- Repackaging detection
- Integrity checks

#### 3.2 Ochrana před odinstalací
- Device Admin API
- Accessibility Service (fallback)
- Warning před uninstall
- Remote lock možnost

### 4. Audit a logging

- Všechny API volání logované
- User actions v web app
- Failed authentication attempts
- Access logs
- Retention policy: 90 dní

### 5. GDPR Compliance

- **Consent**: Explicitní souhlas monitorovaného
- **Right to access**: Export všech dat
- **Right to deletion**: Kompletní mazání dat
- **Data minimization**: Pouze nezbytná data
- **Purpose limitation**: Pouze pro deklarované účely

---

## Instalace a nasazení

### Požadavky

#### Server
- **OS**: Debian 12.12 / Ubuntu 22.04 LTS / Docker
- **RAM**: minimum 4GB, doporučeno 8GB
- **CPU**: 2+ cores
- **Disk**: 50GB+ SSD
- **Network**: Veřejná IP adresa, domény

#### Domény
- `api.myspy.fir.ma` → API server
- `wss.myspy.fir.ma` → WebSocket server
- `app.myspy.fir.ma` → Web aplikace

### Instalace (viz Server/README.md pro detaily)

```bash
# 1. Stažení instalačního scriptu
wget https://github.com/m5ike/m5spy_app/raw/main/Server/install.sh

# 2. Spuštění instalace
sudo bash install.sh

# 3. Konfigurace
sudo m5spy-config

# 4. Start služeb
sudo systemctl start m5spy

# 5. Kontrola stavu
sudo systemctl status m5spy
```

---

## Struktura projektu

```
m5spy_app/
├── README.md                          # Tento soubor
├── LICENSE                            # Licence projektu
├── .gitignore                         # Git ignore pravidla
│
├── MobilApp/                          # Android aplikace
│   ├── README.md                      # Dokumentace Android app
│   ├── MobilniAplikaceAndroid.sourcew # Step-by-step cookbook
│   ├── app/                           # Android Studio project
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/myspy/
│   │   │   │   │   ├── modules/       # Moduly
│   │   │   │   │   ├── services/      # Services
│   │   │   │   │   ├── receivers/     # Broadcast receivers
│   │   │   │   │   ├── api/           # API komunikace
│   │   │   │   │   ├── security/      # Bezpečnost
│   │   │   │   │   └── utils/         # Utility
│   │   │   │   ├── res/               # Resources
│   │   │   │   └── AndroidManifest.xml
│   │   │   └── test/                  # Unit testy
│   │   ├── build.gradle               # Gradle config
│   │   └── proguard-rules.pro         # ProGuard/R8
│   └── docs/                          # Dodatečná dokumentace
│
├── WebApp/                            # Django webová aplikace
│   ├── README.md                      # Dokumentace Web app
│   ├── WebovaAplikace.sourcew         # Step-by-step cookbook
│   ├── manage.py                      # Django manage
│   ├── requirements.txt               # Python dependencies
│   ├── config/                        # Konfigurace
│   │   ├── settings/
│   │   │   ├── base.py
│   │   │   ├── development.py
│   │   │   ├── production.py
│   │   │   └── testing.py
│   │   ├── urls.py
│   │   ├── wsgi.py
│   │   └── asgi.py
│   ├── apps/                          # Django aplikace
│   │   ├── users/                     # User management
│   │   ├── devices/                   # Device registration
│   │   ├── monitoring/                # Monitoring moduly
│   │   │   ├── sms/
│   │   │   ├── calls/
│   │   │   ├── location/
│   │   │   ├── apps/
│   │   │   ├── internet/
│   │   │   ├── keylogger/
│   │   │   ├── media/
│   │   │   └── files/
│   │   ├── api/                       # REST API
│   │   ├── websocket/                 # WebSocket handlers
│   │   ├── dashboard/                 # Dashboard & widgets
│   │   └── hooks/                     # Event hooks
│   ├── static/                        # Static files
│   ├── media/                         # Uploaded media
│   ├── templates/                     # Django templates
│   ├── locale/                        # Translations
│   └── tests/                         # Tests
│
├── Server/                            # Server konfigurace
│   ├── README.md                      # Dokumentace serveru
│   ├── install.sh                     # Instalační script
│   ├── uninstall.sh                   # Odinstalační script
│   ├── m5spy-config                   # Konfigurační utility
│   ├── systemd/                       # Systemd service files
│   │   ├── m5spy.service
│   │   ├── m5spy-api.service
│   │   ├── m5spy-wss.service
│   │   ├── m5spy-celery.service
│   │   └── m5spy-celerybeat.service
│   ├── nginx/                         # Nginx konfigurace
│   │   ├── api.myspy.fir.ma.conf
│   │   ├── wss.myspy.fir.ma.conf
│   │   └── app.myspy.fir.ma.conf
│   ├── apache2/                       # Apache2 konfigurace (alternativa)
│   ├── redis/                         # Redis konfigurace
│   │   └── redis.conf
│   ├── memcached/                     # Memcached konfigurace
│   │   └── memcached.conf
│   ├── rabbitmq/                      # RabbitMQ konfigurace
│   │   └── rabbitmq.conf
│   ├── postgresql/                    # PostgreSQL konfigurace
│   │   └── pg_hba.conf
│   ├── docker/                        # Docker konfigurace
│   │   ├── Dockerfile
│   │   ├── docker-compose.yml
│   │   └── .env.example
│   └── scripts/                       # Helper skripty
│       ├── backup.sh
│       ├── restore.sh
│       └── update.sh
│
└── docs/                              # Projektová dokumentace
    ├── API.md                         # API dokumentace
    ├── SECURITY.md                    # Bezpečnostní dokumentace
    ├── MODULES.md                     # Dokumentace modulů
    ├── ARCHITECTURE.md                # Architektura
    ├── DEPLOYMENT.md                  # Nasazení
    ├── LEGAL.md                       # Právní aspekty
    └── CHANGELOG.md                   # Historie změn
```

---

## Dokumentace komponent

### Mobilní aplikace
→ Viz [MobilApp/README.md](MobilApp/README.md)
→ Step-by-step: [MobilApp/MobilniAplikaceAndroid.sourcew](MobilApp/MobilniAplikaceAndroid.sourcew)

### Webová aplikace
→ Viz [WebApp/README.md](WebApp/README.md)
→ Step-by-step: [WebApp/WebovaAplikace.sourcew](WebApp/WebovaAplikace.sourcew)

### Server
→ Viz [Server/README.md](Server/README.md)

---

## API Dokumentace

→ Kompletní API dokumentace: [docs/API.md](docs/API.md)

### Základní endpoints

#### Autentizace
```
POST   /api/v1/register          # Registrace zařízení
POST   /api/v1/auth/login        # Login (web app)
POST   /api/v1/auth/refresh      # Refresh token
POST   /api/v1/auth/logout       # Logout
```

#### Monitoring
```
POST   /api/v1/sms               # Upload SMS data
POST   /api/v1/calls             # Upload call logs
POST   /api/v1/location          # Upload GPS data
POST   /api/v1/apps              # Upload app usage
POST   /api/v1/internet          # Upload browser history
POST   /api/v1/keylogger         # Upload keylogger data
POST   /api/v1/media             # Upload media (screenshots, videos)
```

#### Remote Control
```
POST   /api/v1/control/command   # Odeslat příkaz na zařízení
GET    /api/v1/control/status    # Status zařízení
```

#### WebSocket
```
WSS    wss://wss.myspy.fir.ma/device/{uuid}      # Device connection
WSS    wss://wss.myspy.fir.ma/dashboard/{uuid}   # Dashboard real-time
```

---

## Rizika a právní aspekty

### ⚠️ DŮLEŽITÉ UPOZORNĚNÍ

Tato aplikace je určena **výhradně** pro:

1. **Rodičovskou kontrolu** - S vědomím a souhlasem dítěte
2. **Firemní monitoring** - S písemným souhlasem zaměstnance
3. **Vlastní zařízení** - Monitoring vlastních zařízení

### Právní požadavky

#### GDPR Compliance
- ✅ Informovaný souhlas monitorované osoby
- ✅ Transparentní informace o zpracování dat
- ✅ Možnost odvolání souhlasu
- ✅ Právo na přístup k datům
- ✅ Právo na výmaz dat
- ✅ Data minimization
- ✅ Purpose limitation
- ✅ Storage limitation (max 90 dní)

#### Národní legislativa
- Dodržení zákonů ČR o ochraně osobních údajů
- Dodržení zákonů o elektronických komunikacích
- Dodržení pracovněprávních předpisů

### Rizika

1. **Právní rizika**
   - Použití bez souhlasu = trestný čin
   - Porušení GDPR = vysoké pokuty
   - Porušení pracovního práva

2. **Bezpečnostní rizika**
   - Kompromitace API keys
   - Man-in-the-middle útoky
   - Reverse engineering aplikace
   - Data breach

3. **Etická rizika**
   - Porušení soukromí
   - Zneužití dat
   - Psychologický dopad na monitorované

### Doporučení

1. **Vždy získat písemný souhlas**
2. **Transparentně informovat** o rozsahu monitoringu
3. **Umožnit deaktivaci** invazivních funkcí
4. **Pravidelné audity** bezpečnosti
5. **Školení** uživatelů
6. **Právní konzultace** před nasazením

---

## Licencování

**Proprietary Software**
© 2025 Michael KOJDL
All rights reserved.

Toto software je proprietární a je chráněno autorským právem. Neautorizované kopírování, distribuce nebo modifikace jsou zakázány.

---

## Podpora

Pro technickou podporu kontaktujte:
- **Email**: support@myspy.fir.ma
- **Issues**: https://github.com/m5ike/m5spy_app/issues

---

## Changelog

### Version 1.0.0 (2025-10-30)
- ✨ Iniciální verze dokumentace
- ✨ Definice architektury
- ✨ Specifikace všech modulů
- ✨ API dokumentace
- ✨ Bezpečnostní protokoly

---

**Poslední aktualizace**: 2025-10-30
**Verze dokumentace**: 1.0.0
