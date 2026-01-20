package com.laba.firenze.domain.model

/**
 * Achievement Category
 * Conforme a iOS AchievementModels.swift
 */
enum class AchievementCategory(
    val id: String,
    val displayName: String,
    val iconName: String,
    val colorHex: Long
) {
    FIRST_STEPS("first_steps", "Primi Passi", "star.fill", 0xFF007AFF), // Blue
    EXAMS("exams", "Esami", "graduationcap.fill", 0xFFAF52DE), // Purple
    PERFORMANCE("performance", "Performance", "flame.fill", 0xFFFF3B30), // Red
    SEMINARS("seminars", "Seminari", "calendar.badge.clock", 0xFFFF9500), // Orange
    CFA("cfa", "Crediti", "chart.bar.fill", 0xFF34C759), // Green
    APP_USAGE("app_usage", "Utilizzo App", "iphone", 0xFF5AC8FA), // Cyan
    EASTER_EGGS("easter_eggs", "Easter Eggs", "sparkles", 0xFFFF2D55), // Pink
    META("meta", "Meta", "crown.fill", 0xFFFFCC00), // Yellow
    MILESTONES("milestones", "Traguardi", "trophy.fill", 0xFFFFCC00); // Yellow
    
    companion object {
        fun fromId(id: String): AchievementCategory = values().find { it.id == id } ?: FIRST_STEPS
    }
}

/**
 * Achievement Rarity
 * Conforme a iOS AchievementRarity
 */
enum class AchievementRarity(
    val id: String,
    val displayName: String,
    val emoji: String,
    val colorHex: Long
) {
    COMMON("common", "Comune", "🥉", 0xFF8E8E93), // Gray
    RARE("rare", "Raro", "🥈", 0xFF007AFF), // Blue
    EPIC("epic", "Epico", "🥇", 0xFFAF52DE), // Purple
    LEGENDARY("legendary", "Leggendario", "💎", 0xFFFF9500); // Orange
    
    companion object {
        fun fromPoints(points: Int): AchievementRarity {
            return when {
                points < 25 -> COMMON
                points < 75 -> RARE
                points < 150 -> EPIC
                else -> LEGENDARY
            }
        }
    }
}

/**
 * Achievement ID
 * Tutti gli achievement definiti in iOS AchievementID
 */
enum class AchievementID(val rawValue: String) {
    // Primi Passi
    FIRST_LOGIN("first_login"),
    FIRST_DATA("first_data"),
    
    // Esami
    FIRST_18("first_18"),
    FIRST_30("first_30"),
    FIRST_LODE("first_lode"),
    FIRST_EXAM_BOOKED("first_exam_booked"),
    READY_TO_GRADUATE("ready_to_graduate"),
    YEAR_1_COMPLETE("year_1_complete"),
    YEAR_2_COMPLETE("year_2_complete"),
    YEAR_3_COMPLETE("year_3_complete"),
    
    // Performance
    STREAK_PERFECT("streak_perfect"),
    PERFEZIONISTA("perfezionista"),
    PUNTEGGIO_PIENO("punteggio_pieno"),
    MARATONETA("maratoneta"),
    
    // Seminari
    FIRST_SEMINAR("first_seminar"),
    TWO_SEMINARS("two_seminars"),
    THREE_SEMINARS("three_seminars"),
    FIVE_SEMINARS("five_seminars"),
    NETWORKING("networking"),
    
    // CFA
    CFA_25("cfa_25"),
    CFA_HALF("cfa_half"),
    CFA_75("cfa_75"),
    CFA_COMPLETE("cfa_complete"),
    CFA_COLLECTOR("cfa_collector"),
    
    // App Usage
    PUNTUALE("puntuale"),
    SEMPRE_AGGIORNATO("sempre_aggiornato"),
    GUFO_NOTTURNO("gufo_notturno"),
    MATTINIERO("mattiniero"),
    DIPENDENTE("dipendente"),
    REFRESH_MANIAC("refresh_maniac"),
    ESPLORATORE("esploratore"),
    STUDIOSO("studioso"),
    INFORMATO("informato"),
    CURIOSO("curioso"),
    
    // Easter Eggs
    FORTUNATO("fortunato"),
    ARCOBALENO("arcobaleno"),
    COMPLEANNO("compleanno"),
    NATALE("natale"),
    ESTATE_AGOSTO("estate_agosto"),
    ESTATE_GIUGNO("estate_giugno"),
    UNICORNO("unicorno"),
    HALLOWEEN("halloween"),
    
    // Meta
    COLLEZIONISTA("collezionista"),
    MAESTRO("maestro"),
    LEGGENDA("leggenda"),
    CACCIATORE("cacciatore"),
    MILIONARIO("milionario"),
    
    // Traguardi
    GRADUATED("graduated");
}

/**
 * Achievement Model
 * Completo come iOS Achievement
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String, // SF Symbol name or emoji
    val category: AchievementCategory,
    val points: Int,
    var isUnlocked: Boolean = false,
    var unlockedDate: Long? = null, // Timestamp
    var progress: Int = 0,
    val maxProgress: Int = 1,
    val hint: String? = null,
    var eventDate: Long? = null // Data dell'evento (es. data esame) se disponibile
) {
    val progressPercentage: Double
        get() = if (maxProgress > 0) {
            (progress.toDouble() / maxProgress.toDouble()).coerceIn(0.0, 1.0)
        } else {
            0.0
        }
    
    val isCompleted: Boolean
        get() = progress >= maxProgress
    
    val rarity: AchievementRarity
        get() = AchievementRarity.fromPoints(points)
}

/**
 * Achievement Factory
 * Crea achievement dalla definizione iOS
 */
object AchievementFactory {
    fun createAchievement(id: AchievementID): Achievement {
        return when (id) {
            // Primi Passi
            AchievementID.FIRST_LOGIN -> Achievement(
                id = id.rawValue,
                title = "Benvenuto in LABA! 🎉",
                description = "Hai effettuato il primo accesso all'app",
                icon = "hand.wave.fill",
                category = AchievementCategory.FIRST_STEPS,
                points = 5,
                maxProgress = 1
            )
            
            AchievementID.FIRST_DATA -> Achievement(
                id = id.rawValue,
                title = "Inizia l'avventura",
                description = "Hai caricato i tuoi dati per la prima volta",
                icon = "arrow.down.circle.fill",
                category = AchievementCategory.FIRST_STEPS,
                points = 10,
                maxProgress = 1
            )
            
            // Esami
            AchievementID.FIRST_18 -> Achievement(
                id = id.rawValue,
                title = "Il primo 18 non si scorda mai 😅",
                description = "Hai ottenuto il tuo primo 18",
                icon = "18.circle.fill",
                category = AchievementCategory.EXAMS,
                points = 15,
                maxProgress = 1
            )
            
            AchievementID.FIRST_30 -> Achievement(
                id = id.rawValue,
                title = "Ahh, il primo 30! 🎯",
                description = "Hai ottenuto il tuo primo 30",
                icon = "30.circle.fill",
                category = AchievementCategory.EXAMS,
                points = 50,
                maxProgress = 1
            )
            
            AchievementID.FIRST_LODE -> Achievement(
                id = id.rawValue,
                title = "Eccellenza pura! ⭐️",
                description = "Hai ottenuto la tua prima lode",
                icon = "star.circle.fill",
                category = AchievementCategory.EXAMS,
                points = 100,
                maxProgress = 1
            )
            
            AchievementID.FIRST_EXAM_BOOKED -> Achievement(
                id = id.rawValue,
                title = "Il dado è tratto",
                description = "Hai prenotato il tuo primo esame",
                icon = "checkmark.circle.fill",
                category = AchievementCategory.EXAMS,
                points = 10,
                maxProgress = 1
            )
            
            AchievementID.READY_TO_GRADUATE -> Achievement(
                id = id.rawValue,
                title = "Puoi laurearti! 🎓",
                description = "Hai completato tutti gli esami necessari",
                icon = "flag.checkered.circle.fill",
                category = AchievementCategory.EXAMS,
                points = 200,
                maxProgress = 1
            )
            
            AchievementID.YEAR_1_COMPLETE -> Achievement(
                id = id.rawValue,
                title = "Primo anno in tasca",
                description = "Hai superato tutti gli esami del primo anno",
                icon = "1.circle.fill",
                category = AchievementCategory.EXAMS,
                points = 75,
                maxProgress = 1
            )
            
            AchievementID.YEAR_2_COMPLETE -> Achievement(
                id = id.rawValue,
                title = "Secondo anno dominato",
                description = "Hai superato tutti gli esami del secondo anno",
                icon = "2.circle.fill",
                category = AchievementCategory.EXAMS,
                points = 100,
                maxProgress = 1
            )
            
            AchievementID.YEAR_3_COMPLETE -> Achievement(
                id = id.rawValue,
                title = "Terzo anno conquistato",
                description = "Hai superato tutti gli esami del terzo anno",
                icon = "3.circle.fill",
                category = AchievementCategory.EXAMS,
                points = 150,
                maxProgress = 1
            )
            
            // Seminari
            AchievementID.FIRST_SEMINAR -> Achievement(
                id = id.rawValue,
                title = "Curioso",
                description = "Hai frequentato il tuo primo seminario",
                icon = "person.fill",
                category = AchievementCategory.SEMINARS,
                points = 15,
                maxProgress = 1
            )
            
            AchievementID.TWO_SEMINARS -> Achievement(
                id = id.rawValue,
                title = "Appassionato",
                description = "Hai frequentato 2 seminari",
                icon = "person.2.fill",
                category = AchievementCategory.SEMINARS,
                points = 30,
                maxProgress = 2
            )
            
            AchievementID.THREE_SEMINARS -> Achievement(
                id = id.rawValue,
                title = "Assetato di conoscenza",
                description = "Hai frequentato 3 seminari",
                icon = "person.3.fill",
                category = AchievementCategory.SEMINARS,
                points = 50,
                maxProgress = 3
            )
            
            AchievementID.FIVE_SEMINARS -> Achievement(
                id = id.rawValue,
                title = "Collezionista di seminari",
                description = "Hai frequentato 5 seminari",
                icon = "star.fill",
                category = AchievementCategory.SEMINARS,
                points = 100,
                maxProgress = 5
            )
            
            AchievementID.NETWORKING -> Achievement(
                id = id.rawValue,
                title = "Networking 👥",
                description = "Partecipa a 10 eventi/seminari",
                icon = "person.3.fill",
                category = AchievementCategory.SEMINARS,
                points = 100,
                maxProgress = 10
            )
            
            // Performance
            AchievementID.STREAK_PERFECT -> Achievement(
                id = id.rawValue,
                title = "Streak perfetto 🔥",
                description = "Supera 3 esami consecutivi con voto ≥ 28",
                icon = "flame.fill",
                category = AchievementCategory.PERFORMANCE,
                points = 100,
                maxProgress = 1
            )
            
            AchievementID.PERFEZIONISTA -> Achievement(
                id = id.rawValue,
                title = "Perfezionista 💯",
                description = "Accumula 5 lodi",
                icon = "star.leadinghalf.filled",
                category = AchievementCategory.PERFORMANCE,
                points = 200,
                maxProgress = 5
            )
            
            AchievementID.PUNTEGGIO_PIENO -> Achievement(
                id = id.rawValue,
                title = "Punteggio pieno 🎯",
                description = "Raggiungi una media finale ≥ 28",
                icon = "target",
                category = AchievementCategory.PERFORMANCE,
                points = 150,
                maxProgress = 1
            )
            
            AchievementID.MARATONETA -> Achievement(
                id = id.rawValue,
                title = "Maratoneta 🏃",
                description = "Supera 3 esami in una singola sessione",
                icon = "figure.run",
                category = AchievementCategory.PERFORMANCE,
                points = 100,
                maxProgress = 1
            )
            
            // CFA
            AchievementID.CFA_25 -> Achievement(
                id = id.rawValue,
                title = "25% del percorso 🎯",
                description = "Raggiungi 45 CFA (triennio) o 30 (biennio)",
                icon = "chart.bar.fill",
                category = AchievementCategory.CFA,
                points = 50,
                maxProgress = 1
            )
            
            AchievementID.CFA_HALF -> Achievement(
                id = id.rawValue,
                title = "A metà strada",
                description = "Hai raggiunto il 50% dei CFA necessari",
                icon = "chart.bar.fill",
                category = AchievementCategory.CFA,
                points = 75,
                maxProgress = 1
            )
            
            AchievementID.CFA_75 -> Achievement(
                id = id.rawValue,
                title = "75% del percorso 📈",
                description = "Raggiungi 135 CFA (triennio) o 90 (biennio)",
                icon = "chart.bar.fill",
                category = AchievementCategory.CFA,
                points = 100,
                maxProgress = 1
            )
            
            AchievementID.CFA_COMPLETE -> Achievement(
                id = id.rawValue,
                title = "Tutti i crediti! 💯",
                description = "Hai completato tutti i CFA necessari",
                icon = "chart.bar.fill",
                category = AchievementCategory.CFA,
                points = 150,
                maxProgress = 1
            )
            
            AchievementID.CFA_COLLECTOR -> Achievement(
                id = id.rawValue,
                title = "CFA collector 🎓",
                description = "Accumula più CFA del necessario",
                icon = "plus.circle.fill",
                category = AchievementCategory.CFA,
                points = 75,
                maxProgress = 1
            )
            
            // App Usage
            AchievementID.PUNTUALE -> Achievement(
                id = id.rawValue,
                title = "Puntuale 📆",
                description = "Controlla l'app 7 giorni consecutivi",
                icon = "calendar.circle.fill",
                category = AchievementCategory.APP_USAGE,
                points = 50,
                maxProgress = 7
            )
            
            AchievementID.SEMPRE_AGGIORNATO -> Achievement(
                id = id.rawValue,
                title = "Sempre aggiornato 🔔",
                description = "Leggi tutte le notifiche entro 24h per 1 mese",
                icon = "bell.badge.fill",
                category = AchievementCategory.APP_USAGE,
                points = 75,
                maxProgress = 1
            )
            
            AchievementID.GUFO_NOTTURNO -> Achievement(
                id = id.rawValue,
                title = "Gufo notturno 🌙",
                description = "Accedi all'app dopo mezzanotte 5 volte",
                icon = "moon.stars.fill",
                category = AchievementCategory.APP_USAGE,
                points = 25,
                maxProgress = 5
            )
            
            AchievementID.MATTINIERO -> Achievement(
                id = id.rawValue,
                title = "Mattiniero ☀️",
                description = "Accedi all'app prima delle 7:00 per 10 volte",
                icon = "sunrise.fill",
                category = AchievementCategory.APP_USAGE,
                points = 50,
                maxProgress = 10
            )
            
            AchievementID.DIPENDENTE -> Achievement(
                id = id.rawValue,
                title = "Dipendente 📲",
                description = "Effettua 50 accessi all'app",
                icon = "iphone.circle.fill",
                category = AchievementCategory.APP_USAGE,
                points = 100,
                maxProgress = 50
            )
            
            AchievementID.REFRESH_MANIAC -> Achievement(
                id = id.rawValue,
                title = "Refresh maniac 🔄",
                description = "Aggiorna i dati 100 volte",
                icon = "arrow.clockwise.circle.fill",
                category = AchievementCategory.APP_USAGE,
                points = 75,
                maxProgress = 100
            )
            
            AchievementID.ESPLORATORE -> Achievement(
                id = id.rawValue,
                title = "Esploratore 🗺️",
                description = "Visita tutte le sezioni dell'app.",
                icon = "map.fill",
                category = AchievementCategory.APP_USAGE,
                points = 30,
                maxProgress = 1
            )
            
            AchievementID.STUDIOSO -> Achievement(
                id = id.rawValue,
                title = "Studioso 📚",
                description = "Apri 10 dispense diverse",
                icon = "book.fill",
                category = AchievementCategory.APP_USAGE,
                points = 40,
                maxProgress = 10
            )
            
            AchievementID.INFORMATO -> Achievement(
                id = id.rawValue,
                title = "Informato 📄",
                description = "Leggi tutti i regolamenti",
                icon = "doc.text.fill",
                category = AchievementCategory.APP_USAGE,
                points = 35,
                maxProgress = 1
            )
            
            AchievementID.CURIOSO -> Achievement(
                id = id.rawValue,
                title = "Curioso ❓",
                description = "Visita le FAQ 5 volte",
                icon = "questionmark.circle.fill",
                category = AchievementCategory.APP_USAGE,
                points = 20,
                maxProgress = 5
            )
            
            // Easter Eggs
            AchievementID.FORTUNATO -> Achievement(
                id = id.rawValue,
                title = "Fortunato 🎲",
                description = "Ottieni esattamente 27 (numero perfetto)",
                icon = "dice.fill",
                category = AchievementCategory.EASTER_EGGS,
                points = 27,
                maxProgress = 1
            )
            
            AchievementID.ARCOBALENO -> Achievement(
                id = id.rawValue,
                title = "Arcobaleno 🌈",
                description = "Hai nel libretto tutti i voti da 18 a 30L",
                icon = "rainbow",
                category = AchievementCategory.EASTER_EGGS,
                points = 200,
                maxProgress = 1
            )
            
            AchievementID.COMPLEANNO -> Achievement(
                id = id.rawValue,
                title = "Compleanno studente 🎂",
                description = "Accedi all'app il giorno del tuo compleanno",
                icon = "birthday.cake.fill",
                category = AchievementCategory.EASTER_EGGS,
                points = 50,
                maxProgress = 1
            )
            
            AchievementID.NATALE -> Achievement(
                id = id.rawValue,
                title = "Studente natalizio 🎄",
                description = "Accedi durante le vacanze di Natale",
                icon = "gift.fill",
                category = AchievementCategory.EASTER_EGGS,
                points = 30,
                maxProgress = 1
            )
            
            AchievementID.ESTATE_AGOSTO -> Achievement(
                id = id.rawValue,
                title = "Studente estivo 🏖️",
                description = "Studia durante agosto",
                icon = "beach.umbrella.fill",
                category = AchievementCategory.EASTER_EGGS,
                points = 40,
                maxProgress = 1
            )
            
            AchievementID.ESTATE_GIUGNO -> Achievement(
                id = id.rawValue,
                title = "Sessione estiva 🌸",
                description = "Completa 3 esami nella sessione estiva (Giugno)",
                icon = "sun.max.fill",
                category = AchievementCategory.EASTER_EGGS,
                points = 50,
                maxProgress = 1
            )
            
            AchievementID.UNICORNO -> Achievement(
                id = id.rawValue,
                title = "Primo dell'anno 🦄",
                description = "Accedi a mezzanotte del 1 gennaio",
                icon = "sparkles",
                category = AchievementCategory.EASTER_EGGS,
                points = 100,
                maxProgress = 1
            )
            
            AchievementID.HALLOWEEN -> Achievement(
                id = id.rawValue,
                title = "Halloween 🎃",
                description = "Accedi il 31 ottobre",
                icon = "flame.fill",
                category = AchievementCategory.EASTER_EGGS,
                points = 30,
                maxProgress = 1
            )
            
            // Meta Achievements
            AchievementID.COLLEZIONISTA -> Achievement(
                id = id.rawValue,
                title = "Collezionista 🏅",
                description = "Sblocca 10 achievement",
                icon = "rosette",
                category = AchievementCategory.META,
                points = 50,
                maxProgress = 1
            )
            
            AchievementID.MAESTRO -> Achievement(
                id = id.rawValue,
                title = "Maestro 💎",
                description = "Sblocca 20 achievement",
                icon = "diamond.fill",
                category = AchievementCategory.META,
                points = 100,
                maxProgress = 1
            )
            
            AchievementID.LEGGENDA -> Achievement(
                id = id.rawValue,
                title = "Leggenda 👑",
                description = "Sblocca TUTTI gli achievement",
                icon = "crown.fill",
                category = AchievementCategory.META,
                points = 500,
                maxProgress = 1
            )
            
            AchievementID.CACCIATORE -> Achievement(
                id = id.rawValue,
                title = "Cacciatore 🎯",
                description = "Sblocca 5 achievement in un giorno",
                icon = "scope",
                category = AchievementCategory.META,
                points = 75,
                maxProgress = 1
            )
            
            AchievementID.MILIONARIO -> Achievement(
                id = id.rawValue,
                title = "Milionario ⭐",
                description = "Raggiungi 1000 punti totali",
                icon = "star.fill",
                category = AchievementCategory.META,
                points = 100,
                maxProgress = 1
            )
            
            // Traguardi
            AchievementID.GRADUATED -> Achievement(
                id = id.rawValue,
                title = "LAUREATO! 🎊🎉",
                description = "Ce l'hai fatta! Congratulazioni!",
                icon = "trophy.fill",
                category = AchievementCategory.MILESTONES,
                points = 500,
                maxProgress = 1
            )
        }
    }
}
