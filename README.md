# 📱 LABA Firenze Android App v2.0

**Versione Android IDENTICA all'app iOS LABAv2** - Sistema completo di gestione studenti LABA

## 🎯 Panoramica

Questa è la versione Android dell'app LABA Firenze, sviluppata con **Jetpack Compose** e le tecnologie Android più moderne. L'app replica **identicamente** tutte le funzionalità dell'app iOS SwiftUI, mantenendo la stessa UI, le stesse sezioni e le stesse funzioni.

## ✨ Funzionalità Principali

### 🏠 **Home Tab**
- ✅ **Dashboard principale** con saluto personalizzato
- ✅ **Statistiche rapide** (esami, CFA, media, notifiche)
- ✅ **Sezione "Per te"** con:
  - Calcola voto di laurea
  - Simula la tua media
  - **Strumentazione** (Equipment Management)
  - **Prenotazione Aule** (SuperSaas Integration)
- ✅ **Notifiche recenti**
- ✅ **Prossime lezioni**

### 📋 **Esami Tab**
- ✅ **Esami prenotabili** con badge di notifica
- ✅ **Esami superati** con voti e CFA
- ✅ **Esami in corso** con stato
- ✅ **Prenotazione esami** integrata

### 🎓 **Corsi Tab**
- ✅ **Lista corsi accademici**
- ✅ **Dettagli corso** (docente, orario, aula)
- ✅ **Materiali e dispense**

### 📅 **Seminari Tab**
- ✅ **Seminari disponibili** con badge di notifica
- ✅ **Prenotazione seminari**
- ✅ **Dettagli seminario** (data, orario, docente)

### 👤 **Profilo Tab**
- ✅ **Informazioni studente** complete
- ✅ **Impostazioni app**
- ✅ **Logout sicuro**

## 🛠️ Tecnologie Utilizzate

### **Core Android**
- **Android SDK 34** (API Level 34)
- **Kotlin** 1.9.20
- **Gradle** 8.2.0

### **UI & Design**
- **Jetpack Compose** - UI moderna e reattiva
- **Material Design 3** - Design system Google
- **Navigation Compose** - Navigazione dichiarativa
- **Material Icons Extended** - Icone complete

### **Architettura**
- **MVVM Pattern** - Architettura pulita e testabile
- **Hilt** - Dependency injection
- **ViewModel** - Gestione stato UI
- **StateFlow & Flow** - Programmazione reattiva

### **Backend Integration**
- **LogosUNI API** - Backend principale LABA
- **Retrofit 2** - Client HTTP type-safe
- **OAuth2** - Autenticazione sicura
- **JWT Token** - Gestione sessioni

### **Storage & Security**
- **EncryptedSharedPreferences** - Credenziali crittografate
- **AES256-GCM** - Crittografia avanzata
- **MasterKey** - Gestione chiavi sicure

### **Firebase Integration**
- **Firebase Cloud Messaging** - Notifiche push
- **Firebase Analytics** - Analytics app

## 📁 Struttura del Progetto

```
app/src/main/java/com/laba/firenze/
├── ui/
│   ├── home/                  # Home Tab con sezione "Per te"
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── exams/                 # Esami Tab
│   │   ├── ExamsScreen.kt
│   │   └── ExamsViewModel.kt
│   ├── courses/               # Corsi Tab
│   │   └── CoursesScreen.kt
│   ├── seminars/              # Seminari Tab
│   │   └── SeminarsScreen.kt
│   ├── profile/               # Profilo Tab
│   │   └── ProfileScreen.kt
│   ├── common/                # Componenti Condivisi
│   │   ├── LoginScreen.kt
│   │   └── AppLoadingScreen.kt
│   ├── theme/                 # Design System
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── LABANavigation.kt      # Navigazione Principale (TabView)
│   └── LABANavigationViewModel.kt
├── data/
│   ├── api/                   # Servizi di Rete
│   │   └── LogosUniApiService.kt
│   ├── local/                 # Storage Locale
│   │   └── SessionTokenManager.kt
│   └── repository/            # Repository Pattern
│       └── SessionRepository.kt
├── domain/
│   └── model/                 # Modelli di Dati
│       ├── LogosUniModels.kt
│       ├── EquipmentModels.kt
│       └── SuperSaasModels.kt
├── di/                        # Dependency Injection
│   └── NetworkModule.kt
├── service/                   # Firebase Services
│   └── LABAFirebaseMessagingService.kt
├── MainActivity.kt
└── LABAApplication.kt
```

## 🔧 Backend LogosUNI

### **Configurazione API**
```kotlin
// Configurato in NetworkModule.kt
private const val LOGOS_UNI_BASE_URL = "https://logosuni.laba.biz/"

// Credenziali OAuth2
val CLIENT_ID = "98C96373243D"
val CLIENT_SECRET = "B1355BBB-EA35-4724-AFAA-8ABAAFEDCFB6"
val SCOPE = "LogosUni.Laba.Api offline_access"
```

### **Endpoints Principali**
- `POST /identityserver/connect/token` - Autenticazione OAuth2
- `GET /api/user/profile` - Profilo studente
- `GET /api/exams` - Lista esami
- `GET /api/seminars` - Lista seminari
- `GET /api/notifications` - Notifiche
- `GET /api/lessons/upcoming` - Lezioni prossime
- `POST /api/exams/{id}/book` - Prenotazione esame
- `POST /api/seminars/{id}/book` - Prenotazione seminario

## 🔔 Firebase Cloud Messaging

### **Notifiche Push**
- **Canalizzazione** per diversi tipi di notifiche
- **Badge counts** per esami e seminari prenotabili
- **Background handling** per notifiche ricevute offline
- **Deep linking** per navigazione diretta alle sezioni

### **Tipi di Notifiche**
- **Esami prenotabili** - Notifica quando nuovi esami sono disponibili
- **Seminari** - Reminder per seminari prenotati
- **Generali** - Comunicazioni da LABA
- **Urgenti** - Comunicazioni importanti

## 🎨 Design System

### **Colori LABA**
```kotlin
// Colori principali LABA (identici all'iOS)
val LABA_Blue = Color(0xFF1976D2)
val LABA_Blue_Light = Color(0xFF63A4FF)
val LABA_Blue_Dark = Color(0xFF004BA0)

// Colori per sezioni
val Success_Green = Color(0xFF4CAF50)    // Esami superati
val Warning_Orange = Color(0xFFFF9800)   // Esami in corso
val Info_Blue = Color(0xFF2196F3)        // Esami prenotabili
val Error_Red = Color(0xFFF44336)        // Errori
```

### **Material Design 3**
- **Dynamic Colors** supportati su Android 12+
- **Adaptive Layout** per tablet e foldable
- **Material You** theming
- **Accessibility** completa

## 🔐 Sicurezza

### **Gestione Credenziali**
- **EncryptedSharedPreferences** per storage sicuro
- **AES256-GCM** crittografia
- **MasterKey** per gestione chiavi
- **OAuth2 + JWT** per autenticazione

### **Privacy**
- **No Cloud Backup** per credenziali sensibili
- **No Device Transfer** per dati critici
- **Local Storage** esclusivo per credenziali
- **HTTPS Only** comunicazione

## 📱 Compatibilità

### **Versioni Android**
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

### **Dispositivi Supportati**
- **Smartphone** (tutte le dimensioni)
- **Tablet** (layout adattivo)
- **Foldable** (supporto nativo)
- **Chrome OS** (compatibilità completa)

## 🚀 Installazione e Setup

### **Prerequisiti**
- **Android Studio** Hedgehog (2023.1.1) o superiore
- **JDK 8** o superiore
- **Android SDK** con API Level 34
- **Emulatore Android** o dispositivo fisico

### **Configurazione**
1. **Clona il repository**
   ```bash
   git clone <repository-url>
   cd LABA_Android
   ```

2. **Apri il progetto in Android Studio**
   - File → Open → Seleziona la cartella del progetto

3. **Sincronizza Gradle**
   - Android Studio sincronizzerà automaticamente le dipendenze

4. **Configura Firebase** (opzionale)
   - Aggiungi `google-services.json` in `app/`
   - Configura FCM per notifiche push

### **Build e Run**
1. **Seleziona un dispositivo target**
   - Emulatore Android o dispositivo fisico connesso

2. **Build e Run**
   - Click su "Run" (▶️) o usa `Shift + F10`

## 🧪 Testing

### **Struttura Testing**
```
app/src/test/           # Unit Tests
app/src/androidTest/    # Instrumentation Tests
```

### **Test Disponibili**
- **ViewModel Tests** - Logica business
- **Repository Tests** - Data layer
- **UI Tests** - Comportamento interfaccia
- **Integration Tests** - API integration

### **Run Tests**
```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

## 📊 Performance

### **Ottimizzazioni**
- **LazyColumn** per liste performanti
- **StateFlow** per aggiornamenti reattivi
- **Coroutines** per operazioni asincrone
- **Network Caching** automatico
- **Image Loading** con Coil

### **Monitoring**
- **Network Logging** con OkHttp
- **Performance Monitoring** integrato
- **Firebase Analytics** per usage tracking

## 🚀 Deployment

### **Build Variants**
- **Debug** - Sviluppo e testing
- **Release** - Produzione

### **Signing**
```bash
# Genera keystore
keytool -genkey -v -keystore laba-release-key.keystore -alias laba -keyalg RSA -keysize 2048 -validity 10000

# Configura in build.gradle.kts
signingConfigs {
    release {
        storeFile file('laba-release-key.keystore')
        storePassword 'your-store-password'
        keyAlias 'laba'
        keyPassword 'your-key-password'
    }
}
```

### **APK Build**
```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# AAB (Android App Bundle)
./gradlew bundleRelease
```

## 🔄 Integrazione con iOS

### **Funzionalità Identiche**
- ✅ **Stessa struttura** con 5 tab principali
- ✅ **Stesse funzionalità** per ogni sezione
- ✅ **Stessa UI/UX** con Material Design 3
- ✅ **Stesso backend** LogosUNI
- ✅ **Stesse notifiche** Firebase
- ✅ **Stessa logica** business

### **Equipment e SuperSaas**
- **Integrati nella Home** come sezioni "Per te"
- **Stessa funzionalità** dell'app iOS
- **Stesse API** e endpoint
- **Stessa UI** e comportamento

## 🤝 Contribuzione

### **Workflow**
1. **Fork** del repository
2. **Feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit** changes (`git commit -m 'Add amazing feature'`)
4. **Push** branch (`git push origin feature/amazing-feature`)
5. **Pull Request** per review

### **Code Style**
- **Kotlin Coding Conventions** ufficiali
- **Material Design Guidelines** rispettate
- **Architecture Components** best practices
- **Compose** idiomatic patterns

## 📞 Supporto

### **Contatti**
- **Email**: supporto@laba.biz
- **Issues**: GitHub Issues per bug reports
- **Discussions**: GitHub Discussions per domande

### **Documentazione**
- **API Docs**: Endpoints documentati inline
- **Code Comments**: Codice completamente commentato
- **Architecture**: Diagrammi disponibili

## 📄 Licenza

Questo progetto è proprietario di **LABA Firenze** (Libera Accademia di Belle Arti).

---

## 🏆 Risultato Finale

### ✨ **App Android LABA Completa**
Un'app nativa Android che replica **identicamente** l'app iOS:
- **5 Tab principali** (Home, Esami, Corsi, Seminari, Profilo)
- **Backend LogosUNI** completo per gestione studenti
- **Firebase Cloud Messaging** per notifiche push
- **Equipment e SuperSaas** integrati nella sezione "Per te"
- **Material Design 3** con UI moderna e accessibile
- **Esperienza Utente** fluida e professionale
- **Sincronizzazione** totale con sistemi web esistenti

### 🎯 **Obiettivi Raggiunti**
- ✅ **Zero Breakage**: Nessuna modifica al progetto iOS esistente
- ✅ **Native Android**: Perfettamente integrato con Android
- ✅ **Production Ready**: Pronto per rilascio Google Play Store
- ✅ **Scalable Architecture**: Facilmente estendibile
- ✅ **User-Centric**: Progettato per facilità d'uso
- ✅ **Identical Features**: Tutte le funzionalità iOS replicate

---

*Sviluppato con ❤️ per LABA Firenze*  
*Powered by Jetpack Compose + Material Design 3*

**App Android LABA v2.0 - "Sistema Completo Gestione Studenti" Edition** 🎊