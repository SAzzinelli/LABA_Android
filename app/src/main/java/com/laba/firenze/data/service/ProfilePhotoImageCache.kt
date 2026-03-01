package com.laba.firenze.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Cache in-memory delle foto profilo (ImgBB).
 * Mantiene le immagini per tutta la durata della sessione app,
 * evitando reload continui in Profilo e altre schermate.
 * Allineato a iOS ProfilePhotoImageCache.
 */
object ProfilePhotoImageCache {

    private val lock = ReentrantLock()
    private val cache = mutableMapOf<String, ByteArray>()
    private val keyOrder = mutableListOf<String>()
    private const val maxEntries = 150

    /** Restituisce i dati immagine dalla cache se presenti. */
    fun imageDataFor(url: String): ByteArray? {
        lock.withLock {
            return cache[url]
        }
    }

    /** Salva i dati immagine in cache. */
    fun setImageData(data: ByteArray, forUrl: String) {
        lock.withLock {
            if (cache[forUrl] == null) {
                keyOrder.add(forUrl)
                if (keyOrder.size > maxEntries && keyOrder.isNotEmpty()) {
                    val oldest = keyOrder.removeFirst()
                    cache.remove(oldest)
                }
            }
            cache[forUrl] = data
        }
    }

    /** Carica l'immagine da URL, la salva in cache e restituisce i dati. Ritorna null se fallisce. */
    suspend fun loadAndCache(url: String): ByteArray? = withContext(Dispatchers.IO) {
        if (imageDataFor(url) != null) return@withContext imageDataFor(url)
        try {
            val conn = URL(url).openConnection()
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            val data = conn.getInputStream().use { it.readBytes() }
            if (data.size >= 15_000) {
                setImageData(data, url)
                data
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /** Precarica più URL in parallelo (ignora errori, salva solo successi). */
    suspend fun preload(urls: List<String>) = withContext(Dispatchers.IO) {
        urls.forEach { url ->
            try {
                loadAndCache(url)
            } catch (_: Exception) { }
        }
    }
}
