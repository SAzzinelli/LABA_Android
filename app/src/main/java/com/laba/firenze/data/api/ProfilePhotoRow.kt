package com.laba.firenze.data.api

/**
 * Riga dalla tabella user_profile_photos su Supabase (allineato a iOS).
 */
data class ProfilePhotoRow(
    val imgbb_url: String,
    val delete_url: String?
)
