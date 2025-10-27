package com.laba.firenze.ui.seminars

import android.text.Html
import androidx.core.text.HtmlCompat

data class SeminarDetails(
    val docente: String? = null,
    val dateLines: List<String> = emptyList(),
    val aula: String? = null,
    val allievi: String? = null,
    val cfa: String? = null,
    val assenze: String? = null,
    val completed: Boolean = false,
    val groups: List<SeminarGroup> = emptyList()
)

data class SeminarGroup(
    val label: String,
    val time: String
)

data class AllieviGroup(
    val anno: Int?,
    val corso: String?
)

/**
 * Rimuove tutto ciò che è tra parentesi tonde (anche annidate)
 */
fun stripParentheses(s: String): String {
    val out = StringBuilder()
    var depth = 0
    for (ch in s) {
        when (ch) {
            '(' -> {
                depth++
                continue
            }
            ')' -> {
                if (depth > 0) depth--
                continue
            }
            else -> {
                if (depth == 0) out.append(ch)
            }
        }
    }
    return out.toString().trim()
}

/**
 * Restituisce SOLO il testo tra virgolette
 */
fun seminarTitle(from: String): String {
    val noParens = stripParentheses(from)
    
    // Cerca virgolette dritte
    val quoteStart = noParens.indexOf('"')
    if (quoteStart != -1) {
        val quoteEnd = noParens.indexOf('"', quoteStart + 1)
        if (quoteEnd != -1) {
            return noParens.substring(quoteStart + 1, quoteEnd).trim()
        }
    }
    
    // Fallback: rimuovi prefissi
    val prefixes = listOf("SEMINARIO", "WORKSHOP")
    var title = noParens.trim()
    for (prefix in prefixes) {
        if (title.uppercase().startsWith(prefix)) {
            title = title.drop(prefix.length).trim()
            break
        }
    }
    
    return title
}

/**
 * Converte HTML in testo semplice
 */
fun plainText(from: String?): String {
    if (from == null) return ""
    return HtmlCompat.fromHtml(from, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
}

/**
 * Parsa i dettagli del seminario dall'HTML
 */
fun parseSeminarDetails(html: String?, esito: String?): SeminarDetails {
    val text = plainText(html).replace("\u00a0", " ")
    val lines = text.split("\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    
    fun extract(after: String): String? {
        return lines.firstOrNull { 
            it.lowercase().startsWith(after.lowercase()) 
        }?.drop(after.length)?.trim()
    }
    
    val docente = extract("Docente:") ?: extract("Docenti:")
    val aula = extract("Aula:") ?: extract("Aula :")
    val allievi = extract("Allievi:") ?: extract("Allievi :")
    
    // Estrai CFA
    val cfaLine = lines.firstOrNull { it.contains("CFA", ignoreCase = true) }
    val cfa = cfaLine?.let { line ->
        val regex = Regex("(?i)(?:N°\\s*)?(\\d{1,2})\\s*CFA|CFA\\s*(\\d{1,2})")
        val matchResult = regex.find(line)
        matchResult?.groupValues?.drop(1)?.firstOrNull { it.isNotEmpty() }
    }
    
    // Estrai assenze
    val assenze = lines.firstOrNull { it.contains("assenz", ignoreCase = true) }
    
    // Check se completato
    val completed = esito?.lowercase()?.let { e ->
        e.contains("complet") || e.contains("approv") || e.contains("valid")
    } ?: false
    
    return SeminarDetails(
        docente = docente?.let { cleanTeacherList(it) },
        dateLines = extractDateLines(lines),
        aula = aula,
        allievi = allievi,
        cfa = cfa,
        assenze = assenze?.let { extractAssenzaSentence(it) },
        completed = completed,
        groups = extractGroups(lines)
    )
}

/**
 * Pulisce i nomi dei docenti rimuovendo prefissi
 */
fun cleanTeacherList(raw: String): String {
    val parts = raw.split("/").map { it.trim() }
    val regex = Regex("(?i)^(prof\\.?\\s*(ssa|ss|sso)?|avv\\.?|dott\\.?|dr\\.?|ing\\.?|arch\\.?|maestro|maestra)\\s+")
    
    fun cleanOne(s: String): String {
        return regex.replace(s, "").trim().replaceFirstChar { it.uppercase() }
    }
    
    return parts.map { cleanOne(it) }.joinToString(" / ")
}

/**
 * Estrae le date dai seminari
 */
private fun extractDateLines(lines: List<String>): List<String> {
    val itMonths = listOf("gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno", 
                         "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre")
    
    fun containsMonthName(s: String): Boolean {
        val lower = s.lowercase()
        return itMonths.any { lower.contains(it) }
    }
    
    val dateStartIndex = lines.indexOfFirst { 
        it.lowercase().startsWith("date:") || it.lowercase().startsWith("orario") 
    }
    
    if (dateStartIndex == -1) return emptyList()
    
    val collected = mutableListOf<String>()
    for (i in dateStartIndex until minOf(lines.size, dateStartIndex + 12)) {
        val line = lines[i]
            .replace("Date:", "", ignoreCase = true)
            .replace("Orario:", "", ignoreCase = true)
            .trim()
        
        val lower = line.lowercase()
        if (lower.startsWith("allievi") || lower.startsWith("aula") || lower.contains("cfa")) {
            break
        }
        
        if (containsMonthName(line)) {
            collected.add(line)
        }
    }
    
    return collected
}

/**
 * Estrae gruppi e orari
 */
private fun extractGroups(lines: List<String>): List<SeminarGroup> {
    val groups = mutableListOf<SeminarGroup>()
    
    for (line in lines) {
        val lower = line.lowercase()
        if (lower.contains("gruppo")) {
            val timeRegex = Regex("\\b\\d{1,2}[:.:]\\d{2}\\b(\\s?[–\\u2013\\u2014-]\\s?\\d{1,2}[:.:]\\d{2}\\b)?")
            val time = timeRegex.find(line)?.value ?: ""
            
            val groupRegex = Regex("(?i)gruppo\\s*([A-Za-z0-9]+)")
            val groupMatch = groupRegex.find(line)
            if (groupMatch != null) {
                val label = groupMatch.groupValues[1].uppercase()
                groups.add(SeminarGroup(label = label, time = time))
            }
        }
    }
    
    return groups
}

/**
 * Estrae solo la frase sulle assenze
 */
private fun extractAssenzaSentence(s: String): String {
    val regex = Regex("(?i)[^.]*assenz[^.]*")
    return regex.find(s)?.value?.trim() ?: s
}

/**
 * Converte la stringa "Allievi" in gruppi
 */
fun allieviGroups(from: String?): List<AllieviGroup> {
    if (from.isNullOrEmpty()) return emptyList()
    
    val parts = from.split("+", ",", ";")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    
    val groups = mutableListOf<AllieviGroup>()
    
    for (part in parts) {
        var year: Int? = null
        val yearRegex = Regex("(\\d)°")
        val yearMatch = yearRegex.find(part)
        if (yearMatch != null) {
            year = yearMatch.groupValues[1].toIntOrNull()
        }
        
        val hints = mapOf(
            "GD" to "Graphic Design & Multimedia",
            "Graphic Design" to "Graphic Design & Multimedia", 
            "Design" to "Design",
            "Fotografia" to "Fotografia",
            "Fashion" to "Fashion Design",
            "Pittura" to "Pittura",
            "Regia" to "Regia e Videomaking",
            "Cinema" to "Cinema e Audiovisivi",
            "Interior" to "Interior Design"
        )
        
        var course: String? = null
        for ((key, value) in hints) {
            if (part.contains(key, ignoreCase = true)) {
                course = value
                break
            }
        }
        
        groups.add(AllieviGroup(anno = year, corso = course))
    }
    
    return groups
}

/**
 * Formatta il titolo
 */
fun prettifyTitle(title: String): String {
    return title.replace("_", " ")
}