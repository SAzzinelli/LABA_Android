package com.laba.firenze.domain.model

import kotlin.math.roundToInt

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeCalculator @Inject constructor() {
    
    /**
     * Calcola il voto di presentazione (media aritmetica convertita su 110)
     * Identico alla logica iOS presentationGradeString()
     */
    fun calculatePresentationGrade(exams: List<Esame>): String {
        val grades = exams.mapNotNull { exam ->
            val title = exam.corso.lowercase()
            if (title.contains("attivit")) return@mapNotNull null
            if (title.contains("tesi")) return@mapNotNull null
            parseVoteForThesis(exam.voto)
        }
        
        return if (grades.isEmpty()) {
            "—"
        } else {
            val avg = grades.reduce { acc, grade -> acc + grade }.toDouble() / grades.size
            val present = ((avg / 30.0 * 110.0).roundToInt()).toString()
            present
        }
    }
    
    /**
     * Parsing del voto per tesi (identico a iOS parseVoteForThesis)
     */
    private fun parseVoteForThesis(voto: String?): Int? {
        if (voto == null) return null
        
        val cleanVoto = voto.trim().uppercase()
        
        return when {
            cleanVoto == "30 E LODE" || cleanVoto == "30L" -> 30
            cleanVoto.contains("30") -> 30
            cleanVoto.contains("29") -> 29
            cleanVoto.contains("28") -> 28
            cleanVoto.contains("27") -> 27
            cleanVoto.contains("26") -> 26
            cleanVoto.contains("25") -> 25
            cleanVoto.contains("24") -> 24
            cleanVoto.contains("23") -> 23
            cleanVoto.contains("22") -> 22
            cleanVoto.contains("21") -> 21
            cleanVoto.contains("20") -> 20
            cleanVoto.contains("19") -> 19
            cleanVoto.contains("18") -> 18
            else -> null
        }
    }
    
    /**
     * Calcola la media degli esami (identico a iOS logic)
     */
    fun calculateAverage(exams: List<Esame>): Double {
        val validGrades = exams.mapNotNull { exam ->
            parseVoteForThesis(exam.voto)
        }
        
        return if (validGrades.isEmpty()) {
            0.0
        } else {
            validGrades.sum().toDouble() / validGrades.size
        }
    }
    
    /**
     * Calcola il numero di esami superati
     */
    fun countPassedExams(exams: List<Esame>): Int {
        return exams.count { exam ->
            val voto = exam.voto?.trim() ?: ""
            voto.isNotEmpty() && voto != "0"
        }
    }
    
    /**
     * Calcola i CFU totali
     */
    fun calculateTotalCFU(exams: List<Esame>): Int {
        return exams.sumOf { exam ->
            exam.cfa?.toIntOrNull() ?: 0
        }
    }
}
