package com.laba.firenze.data.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Response del token OAuth2 (identico a iOS)
 * Endpoint: /identityserver/connect/token
 */
@Parcelize
data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Long,
    val refresh_token: String
) : Parcelable
