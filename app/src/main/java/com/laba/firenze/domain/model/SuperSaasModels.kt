package com.laba.firenze.domain.model

/** Modelli Prenotazione Aule (SuperSaas) - identici a iOS SuperSaasModels. */

// Room models
data class SuperSaasRoom(
    val id: String,
    val numericId: Int,
    val name: String,
    val description: String?,
    val category: RoomCategory
)

enum class RoomCategory(val displayName: String) {
    PHOTOGRAPHY("Fotografia"),
    DESIGN("Design"),
    FASHION_DESIGN("Fashion Design"),
    PRINTING("3D & Rendering"),
    LIBRARY("Biblioteca"),
    CINEMA("Cinema e Audiovisivi"),
    DARKROOM("Camera Oscura")
}

/** Elenco aule disponibili (identico a iOS SuperSaasRoom.availableRooms). */
object SuperSaasRooms {
    val list: List<SuperSaasRoom> = listOf(
        SuperSaasRoom("CAMERA_OSCURA", 519294, "Camera Oscura", null, RoomCategory.DARKROOM),
        SuperSaasRoom("SALA_POSA_1", 190289, "Photo LAB 1", null, RoomCategory.PHOTOGRAPHY),
        SuperSaasRoom("SALA_POSA_2", 190222, "Photo LAB 2", null, RoomCategory.PHOTOGRAPHY),
        SuperSaasRoom("SCANNER_FOTOGRAFIA", 621475, "Scanner", null, RoomCategory.PHOTOGRAPHY),
        SuperSaasRoom("LABORATORIO_DESIGN", 548608, "Design LAB", null, RoomCategory.DESIGN),
        SuperSaasRoom("OPEN_LAB_FASHION", 397143, "Open LAB Fashion", null, RoomCategory.FASHION_DESIGN),
        SuperSaasRoom("SERVIZI_STREAMING", 702367, "Servizi Streaming", null, RoomCategory.CINEMA),
        SuperSaasRoom("MOVIE_HALL", 817857, "Movie Hall", null, RoomCategory.CINEMA)
    )
}

// Slot models
data class SuperSaasAvailabilitySlot(
    val start: String,
    val finish: String,
    val available: Boolean,
    val bookedBy: String? = null,
    val bookedByEmail: String? = null
) {
    val id: String get() = start + finish
    val timeRange: String get() = formatTimeRange(start, finish)
}

// User models
data class SuperSaasUser(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    val matricola: String? = null,
    val created_on: String? = null
)

data class SuperSaasLoginResponse(
    val user: SuperSaasUser,
    val token: String
)

// Appointment models
data class SuperSaasAppointment(
    val id: Int,
    val schedule_id: Int,
    val user_id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val start: String,
    val finish: String,
    val description: String? = null,
    val status: String? = null,
    val created_on: String? = null,
    val updated_on: String? = null
) {
    val formattedTimeRange: String get() = formatTimeRange(start, finish)
}

data class CreateSuperSaasAppointment(
    val schedule_id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val matricola: String,
    val start: String,
    val finish: String,
    val description: String? = null
)

// API response models
data class SuperSaasUserResponse(
    val id: Int,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val field_1: String? = null,
    val created_on: String? = null,
    val role: Int? = null
)

data class SuperSaasAvailabilityAPIResponse(
    val slots: List<SuperSaasAvailabilitySlotResponse>
)

data class SuperSaasAvailabilitySlotResponse(
    val start: String? = null,
    val finish: String? = null,
    val bookings: List<SuperSaasBookingInSlot>? = null
)

data class SuperSaasBookingInSlot(
    val name: String? = null,
    val email: String? = null,
    val full_name: String? = null
)

data class SuperSaasBookingItem(
    val id: Int? = null,
    val start: String? = null,
    val finish: String? = null,
    val resource_id: Int? = null,
    val user_id: Int? = null,
    val full_name: String? = null,
    val created_by: String? = null,
    val phone: String? = null,
    val res_name: String? = null,
    val deleted: Boolean? = null,
    val created_on: String? = null
)

// Date parser helper
object SuperSaasDateParser {
    private val formatters = listOf(
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getDefault() },
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getDefault() },
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getDefault() }
    )

    fun parseDate(dateString: String): java.util.Date? {
        for (formatter in formatters) {
            try {
                return formatter.parse(dateString)
            } catch (_: Exception) { }
        }
        return null
    }
}

private fun formatTimeRange(start: String, finish: String): String {
    val startDate = SuperSaasDateParser.parseDate(start)
    val finishDate = SuperSaasDateParser.parseDate(finish)
    if (startDate == null || finishDate == null) return "Orario non disponibile"
    val fmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale("it", "IT"))
    return "${fmt.format(startDate)} - ${fmt.format(finishDate)}"
}
