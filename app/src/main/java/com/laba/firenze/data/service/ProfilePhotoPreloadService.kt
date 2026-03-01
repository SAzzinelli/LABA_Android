package com.laba.firenze.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Precarga le foto profilo all'avvio.
 * Chiamare dopo login; non blocca l'UI.
 * Allineato a iOS ProfilePhotoPreloadService.
 * (Senza minigiochi: precarica solo foto utente corrente)
 */
object ProfilePhotoPreloadService {

    fun preloadAllIfNeeded(currentUserPhotoURL: String?) {
        CoroutineScope(Dispatchers.Default).launch {
            val urlsToPreload = mutableListOf<String>()
            currentUserPhotoURL?.takeIf { it.isNotBlank() }?.let { urlsToPreload.add(it) }
            val unique = urlsToPreload.distinct()
            if (unique.isNotEmpty()) {
                ProfilePhotoImageCache.preload(unique)
            }
        }
    }
}
