package com.laba.firenze.data.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.laba.firenze.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Servizio per upload foto profilo su ImgBB (free tier).
 * Allineato a iOS ProfilePhotoService: compressione, delete, nome "nome.cognome".
 */
object ProfilePhotoService {

    private const val TAG = "ProfilePhoto"

    val isConfigured: Boolean
        get() {
            val key = BuildConfig.IMGBB_API_KEY
            return key.isNotEmpty() && key != "YOUR_IMGBB_API_KEY"
        }

    /**
     * Elimina l'immagine precedente su ImgBB (usa delete_url dalla risposta di upload).
     * Chiamare prima di un nuovo upload per sostituire invece di accumulare.
     */
    suspend fun deleteImage(deleteURL: String) = withContext(Dispatchers.IO) {
        try {
            val url = URL(deleteURL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            val code = conn.responseCode
            Log.d(TAG, "Delete old image: $code")
        } catch (e: Exception) {
            Log.w(TAG, "Delete failed: ${e.message}")
        }
    }

    /**
     * Upload immagine su ImgBB. Restituisce Pair(url, deleteUrl).
     * Comprime a max 512px, JPEG 0.85.
     * @param name Nome personalizzato su ImgBB (es. "nome.cognome") per ritrovare facilmente le foto.
     */
    suspend fun upload(
        imageData: ByteArray,
        maxDimension: Int = 512,
        name: String? = null
    ): Result<Pair<String, String?>> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(Exception("API ImgBB non configurata. Aggiungi IMGBB_API_KEY in gradle.properties o local.properties."))
        }

        try {
            val resized = resizeImageForProfile(imageData, maxDimension)
            val jpeg = resized ?: imageData
            val base64 = Base64.encodeToString(jpeg, Base64.NO_WRAP)

            val keyEnc = URLEncoder.encode(BuildConfig.IMGBB_API_KEY, "UTF-8")
                .replace("+", "%20")
            val imgEnc = URLEncoder.encode(base64, "UTF-8")
                .replace("+", "%20")

            var body = "key=$keyEnc&image=$imgEnc"
            val nameTrimmed = name?.trim()
            if (!nameTrimmed.isNullOrEmpty()) {
                body += "&name=${URLEncoder.encode(nameTrimmed, "UTF-8").replace("+", "%20")}"
            }

            val url = URL("https://api.imgbb.com/1/upload")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.connectTimeout = 30000
            conn.readTimeout = 30000

            conn.outputStream.use { os ->
                os.write(body.toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            val responseBody = if (code in 200..299) {
                conn.inputStream.bufferedReader().readText()
            } else {
                conn.errorStream?.bufferedReader()?.readText() ?: ""
            }

            val json = JSONObject(responseBody)
            val success = json.optBoolean("success", false)
            if (!success) {
                val errMsg = json.optJSONObject("error")?.optString("message") ?: "HTTP $code"
                return@withContext Result.failure(Exception("Upload fallito: $errMsg"))
            }

            val data = json.optJSONObject("data") ?: return@withContext Result.failure(Exception("Risposta non valida"))
            val imageUrl = data.optString("url")
            val deleteUrl = data.optString("delete_url").takeIf { it.isNotEmpty() }

            if (imageUrl.isEmpty()) {
                return@withContext Result.failure(Exception("URL immagine non restituito"))
            }

            Result.success(Pair(imageUrl, deleteUrl))
        } catch (e: Exception) {
            Log.e(TAG, "Upload exception: ${e.message}")
            Result.failure(e)
        }
    }

    private fun resizeImageForProfile(data: ByteArray, maxDimension: Int): ByteArray? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(data, 0, data.size, opts)
        val w = opts.outWidth
        val h = opts.outHeight
        if (w <= 0 || h <= 0) return null

        val maxSide = maxOf(w, h)
        if (maxSide <= maxDimension) return null

        val sampleSize = (maxSide / maxDimension).coerceAtLeast(1)
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, decodeOpts) ?: return null

        val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
        val newW = (bitmap.width * scale).toInt()
        val newH = (bitmap.height * scale).toInt()

        val scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        if (scaled != bitmap) bitmap.recycle()

        val output = java.io.ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, output)
        scaled.recycle()

        return output.toByteArray()
    }
}
