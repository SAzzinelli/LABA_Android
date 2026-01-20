package com.laba.firenze.domain.model

import java.util.Date

data class LessonEvent(
    val id: String,
    val corso: String,
    val oidCorso: String?,
    val oidCorsi: List<String>? = null,
    val anno: Int,
    val aula: String? = null,
    val docente: String? = null,
    val start: Date,
    val end: Date,
    val note: String? = null,
    val gruppo: String? = null
)

