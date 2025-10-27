package com.laba.firenze.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// MARK: - Thesis Documents Models

@Parcelize
data class ThesisDocumentsResponse(
    val success: Boolean,
    val errors: List<String> = emptyList(),
    val start: Int = 0,
    val count: Int = 0,
    val totalCount: Int = 0,
    val payload: List<ThesisDocumentItem>? = null,
    val errorSummary: String? = null
) : Parcelable

@Parcelize
data class ThesisDocumentItem(
    val descrizione: String,
    val allegatoOid: String
) : Parcelable


