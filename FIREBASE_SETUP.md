# Setup Firebase per LABA Android

## Configurazione Firebase

### 1. Scarica il file google-services.json

1. Vai su [Firebase Console](https://console.firebase.google.com/)
2. Seleziona o crea il progetto per l'app LABA Android
3. Vai su Project Settings > Your apps
4. Aggiungi un'app Android con package name: `com.laba.firenze`
5. Scarica il file `google-services.json`
6. Posiziona il file in: `LABA_Android/app/google-services.json`

### 2. Verifica che il plugin sia configurato

Il plugin `google-services` è già configurato in:
- `build.gradle.kts` (project level)
- `app/build.gradle.kts` (app level)

### 3. Build e Run

```bash
cd /Users/simone/Desktop/LABA_Android
./gradlew clean build
./gradlew installDebug
```

## Funzionalità Implementate

### Sistema di Notifiche Completo

✅ **Firebase Cloud Messaging**
- Firebase Messaging Service configurato
- Gestione topic FCM
- Ricezione notifiche push

✅ **NotificationSettingsScreen**
- Toggle globale per abilitare/disabilitare notifiche
- Toggle per categoria:
  - Esami
  - Voti
  - Assenze docenti
  - Comunicazioni da professori
  - Materiali e dispense
  - Seminari
  - Eventi LABA
  - Comunicazioni generali
- Avviso su rischi disattivazione categorie

✅ **Integrazione Profilo**
- Pulsante "Notifiche" nel ProfileScreen
- Navigazione a NotificationSettingsScreen

✅ **Topic FCM Dinamici**
- Sottoscrizione automatica a topic basati su:
  - Corso (es: GD, FO, VI)
  - Anno (1, 2, 3)
  - Stato laureato
  - Categorie notifiche abilitate

### Flusso di Topic

Esempio per uno studente di Grafica Digitale, anno 2:
- `tutti` (sempre abbonato)
- `GD_2_esami` (se categoria esami abilitata)
- `GD_esami` (generale corso)
- `GD_2_voti` (se categoria voti abilitata)
- `GD_voti` (generale corso)
- ...e così via per tutte le categorie abilitate

Se laureato:
- `laureato`
- `laureato_GD`

## Struttura File

```
app/src/main/java/com/laba/firenze/
├── data/
│   ├── NotificationManager.kt          # Gestione FCM topics
│   ├── NotificationCategory.kt          # Enum categorie notifiche
│   ├── TopicManager.kt                 # Logica topic subscription
│   └── firebase/
│       └── LABAFirebaseMessagingService.kt   # Ricezione notifiche
├── ui/
│   ├── notifications/
│   │   ├── NotificationSettingsScreen.kt
│   │   └── viewmodel/
│   │       └── NotificationSettingsViewModel.kt
│   ├── profile/
│   │   └── ProfileScreen.kt            # Con pulsante Notifiche
│   └── LABANavigation.kt                # Con route notifications
└── di/
    └── DataModule.kt                    # Dependency Injection
```

## Test

1. Installa l'app su un dispositivo Android
2. Apri Profile > Notifiche
3. Verifica che tutti i toggle funzionino
4. Disattiva alcune categorie e verifica che i topic siano aggiornati
5. Invia una notifica dal portale FCM per testare la ricezione

## Note

- Il sistema è compatibile con il backend FCM già in uso per iOS
- I topic sono identici tra iOS e Android per compatibilità
- Le preferenze sono salvate in SharedPreferences

