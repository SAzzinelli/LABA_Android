# Riepilogo implementazioni – allineamento Android a iOS

## ✅ Completato

### 1. Cache e preload foto profilo (ImgBB)
- **`ProfilePhotoImageCache.kt`** – cache in-memory per foto profilo (max 150 entry)
- **`ProfilePhotoPreloadService.kt`** – preload foto utente corrente all’avvio
- **`ProfileScreen.kt`** – `ProfilePhotoFromURL` usa la cache prima del caricamento
- **`SessionRepository.kt`** – chiama `ProfilePhotoPreloadService.preloadAllIfNeeded()` dopo il login

### 2. Tutorial allineato a iOS
- **`TutorialScreen.kt`** – pagine allineate a iOS:
  - Pagina 0: "La tua carriera. In ordine." + valueLine
  - Pagine 1–6: Voti, Esami, Strumenti, Seminari, Personalizza, Traguardi
  - Pagina 7: **Foto profilo** – card con picker per impostare la foto (ImgBB)
  - Pagina 8: **Prova le novità** – toggle Beta (orario, traguardi, minigiochi)
  - Pagina 9: Chiusura
- **`TutorialProfilePhotoSetup`** – card per upload foto dal tutorial
- **`TutorialBetaFeatures`** – switch per laba.timetable.enabled, laba.achievements.enabled, laba.minigames.enabled
- **`ProfileScreen.kt`** – passa `profileViewModel` al tutorial per la pagina foto

### 3. Migrazione API (già fatta in precedenza)
- URL prod: `api/api` (allineato a iOS)
- URL test: `api-test/api`

### 4. Profilo: Funzionalità e Servizi – allineamento a iOS
- **`ServiziScreen.kt`**: titolo "Funzionalità e Servizi", etichetta toggle "Esami prenotati" (senza "in home")
- Toggle: Orari, Traguardi, Esami prenotati, Wi‑Fi, Server Studenti, Guida stampa (con alert Beta dove previsto)

### 5. Esami in Home solo nei periodi d'esame
- **`HomeViewModel.kt`**: `examSessionWindows()` con date fisse (es. 16–20 feb 2026, 22–30 giu 2026, 1–10 set 2026), `isTodayInExamSession()`
- **`HomeScreen.kt`**: sezione Esami prenotati visibile solo se `isTodayInExamSession()` (come iOS)

### 6. Gruppo disattivato come iOS
- **`LabaConfig.kt`**: `USE_GROUP_FILTER = false` (allineato a `LABA_USE_GROUP_FILTER` iOS)
- **`ProfileScreen.kt`**: voce "Il tuo gruppo" nascosta quando `USE_GROUP_FILTER` è false
- **`HomeViewModel.kt`**: filtro gruppo nelle lezioni applicato solo se `USE_GROUP_FILTER` è true

---

## ✅ Completato (continuazione)

### 7. SuperSaas API
**Implementato su Android**:
- `SuperSaasApi.kt`, `SuperSaasAuthInterceptor.kt`, `SuperSaasRepository.kt`
- Login con checksum MD5, persistenza token/user, slot per schedule_id e data
- `PrenotazioneAuleScreen` – login (email LABA), elenco aule, "Vedi slot", calendario prenotazioni, WebView fallback

### 8. Attrezzature / Equipment
**Implementato su Android**:
- `GestionaleModels.kt`, estensioni `GestionaleApi.kt` e `GestionaleRepository.kt`
- Dashboard Strumentazione: richieste, prestiti attivi, catalogo attrezzature, segnalazioni

### 9. Schermate Esami, Corsi e dettagli (da iOS)
**Implementato su Android**:
- **`Pill.kt`**: componente pillole (anno, CFA, status, alert) come iOS
- **`ExamsScreen.kt`**: `ExamGradeBadge` circolare (28, 30L, ID, —), layout lista come iOS
- **`CoursesScreen.kt`**: sezioni Workshop/Tesi abilitate, Pill per anno/CFA, filtri ricerca (voto, idoneità)
- **`ExamDetailScreen.kt`**: banner voto compatto (30L, ID), propedeuticità da campo `propedeutico` API
- **`CourseDetailScreen.kt`**: già allineato (propedeutico, mail docente)
- **`CoursesViewModel.kt`**: filtri include workshop/tesi, ricerca per voto numerico e idoneità

### 10. YearRecap – COMPLETATO
**Stato iOS**: `YearRecapView` – schermata stile “Spotify Wrapped” con pagine per esami, media, CFA, lodi, achievement.

**Implementato**:
- `YearRecapScreen.kt` con `HorizontalPager`
- `YearRecapViewModel` – dati da `SessionRepository`, `AchievementManager`, `GradeCalculator`
- Pulsante “Il tuo anno in LABA” in `AchievementsScreen`
- Route `year-recap` in `LABANavigation`

---

## File creati/modificati (sessione corrente)

| File | Azione |
|------|--------|
| `ProfilePhotoImageCache.kt` | Creato |
| `ProfilePhotoPreloadService.kt` | Creato |
| `ProfileScreen.kt` | Modificato (cache, import) |
| `SessionRepository.kt` | Modificato (preload) |
| `TutorialScreen.kt` | Modificato (pagine, foto, beta) |
| `ProfileScreen.kt` | Modificato (profileViewModel al tutorial) |
| `LabaConfig.kt` | Creato (USE_GROUP_FILTER) |
| `ServiziScreen.kt` | Modificato (titolo, etichetta esami) |
| `HomeViewModel.kt` | Modificato (exam session windows, group filter) |
| `HomeScreen.kt` | Modificato (esami solo in sessione) |
| `ProfileScreen.kt` | Modificato (gruppo condizionale) |
| `SuperSaasApi.kt`, `SuperSaasRepository.kt` | Creati |
| `PrenotazioneAuleScreen.kt`, `PrenotazioneAuleViewModel.kt` | Modificati (SuperSaas nativo) |
| `GestionaleApi.kt`, `GestionaleRepository.kt`, `StrumentazioneScreen.kt` | Estesi (Equipment) |

---

## Note
- **SuperSaas**: `SUPERSAAS_API_KEY` in `gradle.properties` o `local.properties` (fallback in BuildConfig).
- **SuperSaas** richiede MD5 per il checksum: `java.security.MessageDigest.getInstance("MD5")`.
- **laba.minigames.enabled** – aggiunto in `TutorialBetaFeatures`; va usato dove servono i minigiochi.
- **ProfilePhotoSupabaseService** – non implementato (usato su iOS per LABArola/Battaglia; minigiochi esclusi).
