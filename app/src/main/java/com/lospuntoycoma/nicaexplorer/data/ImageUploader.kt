package com.lospuntoycoma.nicaexplorer.data

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.lospuntoycoma.nicaexplorer.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Sube una imagen al almacén del backend (GitHub/Storage) usando el endpoint
 * `/api/v1/upload`, que exige la API key y un ID token de Firebase válido.
 */
object ImageUploader {

    private const val CONNECT_TIMEOUT_MS = 20_000
    private const val READ_TIMEOUT_MS = 60_000

    suspend fun subir(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val user = FirebaseAuth.getInstance().currentUser
                ?: return@withContext Result.failure(Exception("Debes iniciar sesión para subir imágenes."))

            val token = user.getIdToken(true).await().token
                ?: return@withContext Result.failure(Exception("No se pudo validar la sesión."))

            val resolver = context.contentResolver
            val mime = resolver.getType(uri) ?: "image/jpeg"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(Exception("No se pudo leer la imagen."))

            if (bytes.size > 5 * 1024 * 1024) {
                return@withContext Result.failure(Exception("La imagen supera el máximo de 5 MB."))
            }

            val boundary = "----NicaExplorer${System.currentTimeMillis()}"
            val extension = when {
                mime.contains("png") -> "png"
                mime.contains("webp") -> "webp"
                mime.contains("gif") -> "gif"
                else -> "jpg"
            }
            val filename = "upload_${System.currentTimeMillis()}.$extension"
            val base = BuildConfig.API_BASE_URL.trim().trimEnd('/')

            val connection = (URL("$base/api/v1/upload").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("X-API-KEY", BuildConfig.API_KEY)
                setRequestProperty("Authorization", "Bearer $token")
            }

            try {
                connection.outputStream.use { output ->
                    output.write("--$boundary\r\n".toByteArray())
                    output.write(
                        "Content-Disposition: form-data; name=\"image\"; filename=\"$filename\"\r\n"
                            .toByteArray()
                    )
                    output.write("Content-Type: $mime\r\n\r\n".toByteArray())
                    output.write(bytes)
                    output.write("\r\n--$boundary--\r\n".toByteArray())
                }

                val code = connection.responseCode
                val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader()?.use { it.readText() }.orEmpty()

                if (code in 200..299) {
                    val url = JSONObject(body).optString("url")
                    if (url.isBlank()) {
                        Result.failure(Exception("El servidor no devolvió la URL de la imagen."))
                    } else {
                        Result.success(url)
                    }
                } else {
                    val message = runCatching { JSONObject(body).optString("message") }.getOrNull()
                    Result.failure(Exception(message?.takeIf { it.isNotBlank() } ?: "Error al subir la imagen ($code)."))
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
