package com.lospuntoycoma.nicaexplorer.data

import com.lospuntoycoma.nicaexplorer.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente HTTP mínimo para la API REST del backend NicaExplorer (CodeIgniter 4).
 *
 * La URL base y la API key se inyectan en tiempo de compilación desde
 * `local.properties` (no versionado) como BuildConfig.API_BASE_URL / API_KEY.
 */
object ApiClient {

    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 20_000

    suspend fun getObject(path: String): JSONObject = withContext(Dispatchers.IO) {
        JSONObject(getText(path))
    }

    /**
     * Extrae el arreglo "data" de la respuesta del backend.
     * Formato: { "data": [ { "id": "...", "data": { ... } }, ... ] }
     *
     * @return lista de pares (id, documento)
     */
    suspend fun getItems(path: String): List<Pair<String, JSONObject>> {
        val json = getObject(path)
        val array = json.optJSONArray("data") ?: JSONArray()
        val result = ArrayList<Pair<String, JSONObject>>(array.length())

        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id")
            val document = item.optJSONObject("data") ?: item
            result.add(id to document)
        }

        return result
    }

    private fun getText(path: String): String {
        val base = BuildConfig.API_BASE_URL.trim().trimEnd('/')
        val connection = (URL(base + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            if (BuildConfig.API_KEY.isNotBlank()) {
                setRequestProperty("X-API-KEY", BuildConfig.API_KEY)
            }
        }

        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (code !in 200..299) {
                throw IllegalStateException("API $code en $path")
            }

            body
        } finally {
            connection.disconnect()
        }
    }
}
