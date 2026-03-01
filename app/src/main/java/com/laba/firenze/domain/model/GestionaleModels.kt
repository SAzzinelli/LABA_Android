package com.laba.firenze.domain.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Modelli Service LABA (Gestionale attrezzatura) - identici a iOS EquipmentModels. */

private val dateFormats = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
    "yyyy-MM-dd"
)

private fun parseDate(s: String): Date? {
    val raw = s.take(10)
    for (fmt in dateFormats) {
        try {
            SimpleDateFormat(fmt, Locale("it")).parse(s)?.let { return it }
        } catch (_: Exception) {}
    }
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale("it")).parse(raw)
    } catch (_: Exception) { null }
}

internal fun formatLoanDateRange(uscita: String, rientro: String): String {
    val start = parseDate(uscita)
    val end = parseDate(rientro)
    return if (start != null && end != null) {
        val fmt = SimpleDateFormat("dd/MM/yyyy", Locale("it"))
        "Dal ${fmt.format(start)} al ${fmt.format(end)}"
    } else "Dal $uscita al $rientro"
}

internal fun daysBetweenTodayAnd(dateStr: String): Int {
    val end = parseDate(dateStr) ?: return 0
    val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
    val target = Calendar.getInstance().apply { time = end; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
    return ((target.time - today.time) / (24 * 60 * 60 * 1000)).toInt()
}

internal fun formatRequestShortDateRange(dal: String, al: String): String {
    val start = parseDate(dal)
    val end = parseDate(al)
    return if (start != null && end != null) {
        val fmt = SimpleDateFormat("d/M", Locale("it"))
        "${fmt.format(start)} - ${fmt.format(end)}"
    } else "$dal - $al"
}

data class UserEquipment(
    val id: Int,
    val nome: String,
    val categoria_madre: String? = null,
    val categoria_id: Int? = null,
    val posizione: String? = null,
    val note: String? = null,
    val immagine_url: String? = null,
    val categoria_nome: String? = null,
    val unita_disponibili: Int,
    val stato_effettivo: String,
    val tipo_prestito: String? = null
) {
    val isAvailable: Boolean get() = stato_effettivo == "disponibile" && unita_disponibili > 0
    val availabilityText: String get() = if (unita_disponibili == 1) "1 disponibile" else "$unita_disponibili disponibili"
    val categoryDisplay: String
        get() = categoria_nome?.split(" - ")?.lastOrNull() ?: categoria_madre ?: "Nessuna categoria"
}

data class EquipmentRequest(
    val id: Int,
    val inventario_id: Int,
    val utente_id: Int,
    val stato: String,
    val dal: String,
    val al: String,
    val motivo: String? = null,
    val note: String? = null,
    val tipo_utilizzo: String? = null,
    val unit_id: Int? = null,
    val oggetto_nome: String? = null,
    val articolo_nome: String? = null,
    val prestito_stato: String? = null,
    val prestito_id: Int? = null,
    val created_at: String,
    val updated_at: String? = null
) {
    val statusDisplay: String
        get() = when (stato.lowercase()) {
            "pending", "in_attesa", "in attesa" -> "In Attesa"
            "approved", "approvata" -> "Approvata"
            "rejected", "rifiutata" -> "Rifiutata"
            else -> stato.replaceFirstChar { it.uppercase() }
        }
    val equipmentName: String get() = oggetto_nome ?: articolo_nome ?: "Attrezzatura"
    val shortDateRange: String get() = formatRequestShortDateRange(dal, al)
}

data class CreateEquipmentRequest(
    val inventario_id: Int,
    val dal: String,
    val al: String,
    val motivo: String? = null,
    val note: String? = null,
    val unit_id: Int? = null,
    val tipo_utilizzo: String? = null
)

data class EquipmentLoan(
    val id: Int,
    val inventario_id: Int,
    val chi: String,
    val data_uscita: String,
    val data_rientro: String,
    val stato: String,
    val note: String? = null,
    val articolo_nome: String? = null,
    val unita: List<String>? = null,
    val categoria_madre: String? = null,
    val categoria_figlia: String? = null,
    val penalty_applied: Boolean? = null,
    val penalty_reason: String? = null
) {
    val equipmentName: String get() = articolo_nome ?: "Attrezzatura"
    val unitsDisplay: String get() = unita?.joinToString(", ") ?: ""
    val fullEquipmentName: String get() = if (unitsDisplay.isEmpty()) equipmentName else "$equipmentName - $unitsDisplay"
    val dateRange: String get() = formatLoanDateRange(data_uscita, data_rientro)
    val daysRemaining: Int get() = daysBetweenTodayAnd(data_rientro)
    val isExpiringSoon: Boolean get() = daysRemaining in 0..2
    val isExpired: Boolean get() = daysRemaining < 0
}

data class EquipmentReport(
    val id: Int,
    val prestito_id: Int? = null,
    val inventario_id: Int,
    val unit_id: Int? = null,
    val tipo: String,
    val urgenza: String,
    val messaggio: String,
    val stato: String,
    val inventario_nome: String? = null,
    val codice_univoco: String? = null,
    val created_at: String
) {
    val typeDisplay: String
        get() = when (tipo) {
            "problema" -> "Problema Tecnico"
            "danno" -> "Danno"
            "perdita" -> "Perdita"
            else -> tipo.replaceFirstChar { it.uppercase() }
        }
}

data class CreateEquipmentReport(
    val prestito_id: Int? = null,
    val inventario_id: Int,
    val unit_id: Int? = null,
    val tipo: String,
    val urgenza: String,
    val messaggio: String
)
