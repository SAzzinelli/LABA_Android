# Confronto LABA iOS vs Android – Funzionalità e migrazione API

Documento di riferimento per allineare l'app Android a iOS. **Priorità iniziale**: migrazione API v3 (test) / v2 (produzione).

---

## 1. Configurazione API – v2 prod / v3 test

### Situazione attuale

| Piattaforma | v2 (Produzione) | v3 (Test) |
|-------------|-----------------|-----------|
| **iOS** | `https://logosuni.laba.biz/api/api` | `https://logosuni.laba.biz/api-test/api` |
| **Android** | `https://logosuni.laba.biz/api/api/` ✅ (allineato) | `https://logosuni.laba.biz/api-test/api/` |

**IdentityServer:**

| Piattaforma | v2 | v3 |
|-------------|-----|-----|
| **iOS** | `https://logosuni.laba.biz/identityserver` | `https://logosuni.laba.biz/identityserver-test` |
| **Android** | `https://logosuni.laba.biz/identityserver/` | `https://logosuni.laba.biz/identityserver-test/` |

### Allineamento completato ✅

- **iOS** e **Android** usano entrambi `api/api` per prod (migrazione completata).

### File modificati (migrazione API) ✅

1. **`NetworkModule.kt`** – URL base API allineato: prod = `api/api`, test = `api-test/api`.
2. ~~**`LogosUniAPIClient.kt`** – fallback Documents v2~~ ✅ Aggiornato a `api/api`.
3. **`LogosUniApiService.kt`** – endpoint notifiche: v2 usa `Notification/GetNotifications`, v3 `Notifications/GetNotifications` (già presenti entrambi).
4. **`setFcmToken`** – iOS usa `PUT Students/SetFCMToken`; Android usa `POST setFcmToken`. Verificare quale sia quello corretto sul backend.

---

## 2. Funzionalità presenti su entrambe le piattaforme

| Funzionalità | iOS | Android |
|--------------|-----|---------|
| Login OAuth2 ROPC + refresh token | ✅ | ✅ |
| Home dashboard, ordine sezioni, confetti | ✅ | ✅ |
| Esami (lista, dettaglio, prenotati) | ✅ | ✅ |
| Calendario lezioni (GitHub Pages / npoint) | ✅ | ✅ |
| Deep link `laba://lesson/{id}` | ✅ | ✅ |
| Corsi (lista, dettaglio) | ✅ | ✅ |
| Seminari (lista, dettaglio) | ✅ | ✅ |
| Notifiche FCM + inbox + impostazioni | ✅ | ✅ |
| Topic FCM per corso/anno | ✅ | ✅ |
| Profilo (anagrafica, tessera, gruppo) | ✅ | ✅ |
| Foto profilo ImgBB | ✅ | ✅ |
| Per Te: Calcolo voto laurea, Simula media | ✅ | ✅ |
| Strumentazione (gestionale attrezzatura) | ✅ | ✅ |
| Prenotazione aule (SuperSaas – link) | ✅ | ✅ |
| Grade trend | ✅ | ✅ |
| Documenti (programmi, dispense, materiali) | ✅ | ✅ |
| Tesi + pergamena | ✅ | ✅ |
| Gamification (achievement, sync Supabase) | ✅ | ✅ |
| FAQ, convenzioni, biblioteca | ✅ | ✅ |
| Guide (Wi‑Fi, server, stampante) | ✅ | ✅ |
| Tema dinamico, animazioni, ordine tab | ✅ | ✅ |
| Debug / Dev options (switch API v2/v3) | ✅ | ✅ |
| Tutorial | ✅ | ✅ |

---

## 3. Funzionalità mancanti su Android (da implementare)

### Alta priorità (back-end già disponibile)

| Funzionalità | Descrizione | Backend |
|--------------|-------------|---------|
| **LABArola** | Gioco parole quotidiano + leaderboard | Supabase (`labarola_scores`, `labarola_user_profiles`) |
| **Battaglia Navale** | Gioco amici multiplayer | Supabase (`battaglia_games`, `battaglia_boards`, `battaglia_moves`, `battaglia_user_profiles`) |

### Media priorità

| Funzionalità | Descrizione | Note |
|--------------|-------------|------|
| **LABACristallo** | Sfera magica con riconoscimento vocale | iOS: Speech framework, microfono |
| **YearRecap** | Riepilogo annuale gamification | `YearRecapView` su iOS |
| **Cache foto profilo** | Preload avatar (Profilo, Amici, LABArola, Battaglia) | `ProfilePhotoPreloadService`, `ProfilePhotoImageCache` su iOS |
| **Wallet/Crediti** | Crediti LABA | `labamobileapp-production.up.railway.app` |

### Bassa priorità / opzionale

| Funzionalità | Note |
|--------------|------|
| **LABAssistant / Maestro** | Assistente AI – verificare stato su iOS |
| **SuperSaas API** | iOS ha `SuperSaasNetworkManager` (login + prenotazioni); Android usa solo link |
| **Watch app** | Solo iOS |

### Dettaglio differenze endpoint / servizi

- **setFcmToken**: iOS `PUT Students/SetFCMToken?FCMToken=`, Android `POST setFcmToken` con body JSON.
- **Documents fallback v2**: Android usa URL hardcoded; va reso dinamico.
- **Supabase**: tabelle `labarola_*` e `battaglia_*` non usate su Android.

---

## 4. Piano di implementazione graduale

### Fase 1 – Migrazione API ✅ (completata)

1. ~~Creare `ApiConfig`~~ – Android usa già `laba.apiVersion` in SharedPreferences (equivalente).
2. ✅ URL prod allineato a `api/api`.
3. ✅ Fallback Documents aggiornato a `api/api`.
4. `setFcmToken`: iOS usa `PUT Students/SetFCMToken`, Android `POST setFcmToken` – verificare con backend se serve allineamento.
5. ✅ Switch v2/v3 in Debug/Dev options attivo.

### Fase 2 – LABArola

1. Integrare Supabase (`labarola_scores`, `labarola_user_profiles`).
2. UI gioco, submit punteggio, leaderboard.
3. Eventuale cache foto profilo per avatar leaderboard.

### Fase 3 – Battaglia Navale

1. Integrare Supabase (`battaglia_*`).
2. Amici, inviti, partite, UI gioco.
3. Push notifications per turni (FCM + funzione Supabase `send-battaglia-push`).

### Fase 4 – Altre funzionalità

- LABACristallo (Speech API Android).
- YearRecap.
- Cache foto profilo.
- Wallet (se necessario).

---

## 5. Riferimenti file chiave

### iOS

- `APIConfig.swift` – URL v2/v3
- `APIClient.swift` – chiamate API
- `LABArolaLeaderboardService.swift`, `BattagliaService.swift`, `BattagliaFriendsService.swift`
- `ProfilePhotoPreloadService`, `ProfilePhotoImageCache`
- `YearRecapView.swift`

### Android

- `NetworkModule.kt` – Retrofit, URL base
- `LogosUniApiService.kt`, `LogosUniAPIClient.kt`
- `SupabaseApi.kt` – solo achievement/stats
- `ProfilePhotoService.kt` – ImgBB (no preload/cache)
