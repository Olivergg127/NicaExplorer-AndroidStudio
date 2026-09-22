package com.lospuntoycoma.nicaexplorer.data

import com.lospuntoycoma.nicaexplorer.model.Afluencia
import com.lospuntoycoma.nicaexplorer.model.CategoriaComercio
import com.lospuntoycoma.nicaexplorer.model.City
import com.lospuntoycoma.nicaexplorer.model.Comercio
import com.lospuntoycoma.nicaexplorer.model.Place
import com.lospuntoycoma.nicaexplorer.model.ReferenciaParadaRuta
import com.lospuntoycoma.nicaexplorer.model.RutaTuristica
import com.lospuntoycoma.nicaexplorer.model.Valoracion
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repositorio de contenido de NicaExplorer. Todo el catálogo (ciudades, lugares,
 * rutas y comercios) se obtiene de la API del backend, que a su vez lee/escribe
 * en la misma base de Firestore que administra el panel.
 */
object ApiRepository {

    private const val DEFAULT_GRADIENT_START = 0xFF4A1A5C
    private const val DEFAULT_GRADIENT_END = 0xFF7B2D8E

    suspend fun getCiudades(): List<City> =
        ApiClient.getItems("/api/v1/ciudades")
            .mapNotNull { (id, document) -> toCity(id, document) }

    /** Versión del catálogo en el backend; cambia cuando hay altas/ediciones/borrados. */
    suspend fun getCatalogVersion(): String? = runCatching {
        ApiClient.getObject("/api/v1/version").optString("version").takeIf { it.isNotBlank() }
    }.getOrNull()

    suspend fun getLugares(): List<Place> =
        ApiClient.getItems("/api/v1/lugares")
            .mapNotNull { (id, document) -> toPlace(id, document) }

    suspend fun getRutas(): List<RutaTuristica> =
        ApiClient.getItems("/api/v1/rutas")
            .mapNotNull { (id, document) -> toRuta(id, document) }

    suspend fun getComercios(): Result<List<Comercio>> = runCatching {
        ApiClient.getItems("/api/v1/comercios")
            .mapNotNull { (id, document) -> toComercio(id, document) }
            .filter { it.visiblePublicamente }
    }

    suspend fun getComercio(id: String): Result<Comercio> = runCatching {
        val json = ApiClient.getObject("/api/v1/comercios/$id")
        val item = json.optJSONObject("data") ?: error("Comercio no encontrado")
        val document = item.optJSONObject("data") ?: item
        toComercio(item.optString("id", id), document) ?: error("Comercio no encontrado")
    }

    suspend fun getCategoriasComercios(): List<CategoriaComercio> =
        ApiClient.getItems("/api/v1/categorias_comercios")
            .mapNotNull { (id, document) -> toCategoriaComercio(id, document) }

    /** Valoraciones públicas (sin uid) para el ranking de recomendados. */
    suspend fun getValoraciones(): List<Valoracion> =
        ApiClient.getItems("/api/v1/valoraciones")
            .mapNotNull { (_, document) ->
                val tipo = document.optString("tipo", "").trim()
                val refId = document.optString("refId", "").trim()
                if (tipo.isBlank() || refId.isBlank()) {
                    null
                } else {
                    Valoracion(
                        tipo = tipo,
                        refId = refId,
                        cityId = document.optString("cityId", "").trim(),
                        uid = "",
                        estrellas = document.optInt("estrellas", 0)
                    )
                }
            }

    // ---- Mapeo JSON -> modelos ----

    private fun toCity(id: String, d: JSONObject): City? {
        if (!d.optBoolean("activo", true)) return null
        return City(
            id = id,
            name = d.optString("nombre", d.optString("name", id)),
            description = d.optString("descripcion", d.optString("description", "")),
            // El campo en Firestore sigue siendo "monumentCount"; en la app es placeCount.
            placeCount = d.optInt("monumentCount", 0),
            gradientStart = d.optLong("gradientStart", DEFAULT_GRADIENT_START),
            gradientEnd = d.optLong("gradientEnd", DEFAULT_GRADIENT_END),
            imagenUrl = d.optString("imagenUrl").takeIf { it.isNotBlank() },
            galeria = d.optJSONArray("galeria").toStringList(),
            lema = d.optString("lema", ""),
            historia = d.optString("historia", ""),
            departamento = d.optString("departamento", ""),
            latitud = d.optNullableDouble("latitud"),
            longitud = d.optNullableDouble("longitud"),
            orden = d.optInt("orden", 0),
            activo = true
        )
    }

    private fun toPlace(id: String, d: JSONObject): Place? {
        if (!d.optBoolean("activo", true)) return null
        val cityId = d.optString("cityId").ifBlank { return null }
        val afluencia = d.optString("afluencia").uppercase()
            .let { value -> runCatching { Afluencia.valueOf(value) }.getOrNull() }
            ?: Afluencia.MODERADA

        return Place(
            id = id,
            name = d.optString("nombre", d.optString("name", id)),
            city = d.optString("ciudad", d.optString("city", "")),
            cityId = cityId,
            category = d.optString("categoria", d.optString("category", "")),
            afluencia = afluencia,
            description = d.optString("descripcion", ""),
            history = d.optString("historia", d.optString("history", "")),
            yearBuilt = d.optString("anioConstruccion", d.optString("yearBuilt", "")),
            modeloUnity = d.optString("modeloUnity", ""),
            gradientStart = d.optLong("gradientStart", DEFAULT_GRADIENT_START),
            gradientEnd = d.optLong("gradientEnd", DEFAULT_GRADIENT_END),
            imagenUrl = d.optString("imagenUrl").takeIf { it.isNotBlank() },
            latitud = d.optNullableDouble("latitud"),
            longitud = d.optNullableDouble("longitud"),
            consejosResponsables = d.optJSONArray("consejosResponsables").toStringList()
        )
    }

    private fun toRuta(id: String, d: JSONObject): RutaTuristica? {
        if (!d.optBoolean("activo", true)) return null
        val cityId = d.optString("cityId").trim().lowercase().ifBlank { return null }

        return RutaTuristica(
            id = id,
            cityId = cityId,
            nombre = d.optString("nombre", ""),
            descripcion = d.optString("descripcion", ""),
            duracionEstimada = d.optString("duracionEstimada", ""),
            notaDuracion = d.optString("notaDuracion", ""),
            objetivos = d.optJSONArray("objetivos").toStringList(),
            paradas = d.optJSONArray("paradas").toStringList().mapNotNull(::parseReferenciaParada),
            imagenUrl = d.optString("imagenUrl").takeIf { it.isNotBlank() },
            orden = d.optInt("orden", 0),
            activo = true
        )
    }

    private fun toComercio(id: String, d: JSONObject): Comercio? = Comercio(
        id = id,
        nombre = d.optString("nombre", ""),
        categoria = d.optString("categoria", ""),
        categoriaPadre = d.optString("categoriaPadre", ""),
        descripcion = d.optString("descripcion", ""),
        ciudad = d.optString("ciudad", ""),
        cityId = d.optString("cityId", ""),
        direccion = d.optString("direccion", ""),
        horario = d.optString("horario", ""),
        diasAtencion = d.optString("diasAtencion", ""),
        imagenUrl = d.optString("imagenUrl").ifBlank { d.optString("imagenurl") },
        logoUrl = d.optString("logoUrl", ""),
        galeria = d.optJSONArray("galeria").toStringList(),
        latitud = d.optDouble("latitud", 0.0),
        longitud = d.optDouble("longitud", 0.0),
        telefono = d.optString("telefono", ""),
        whatsapp = d.optString("whatsapp", ""),
        tieneWhatsapp = d.optBoolean("tieneWhatsapp", false),
        correo = d.optString("correo", ""),
        redesSociales = d.toStringListFlexible("redesSociales"),
        servicios = d.optJSONArray("servicios").toStringList(),
        productos = d.optJSONArray("productos").toStringList(),
        infoAdicional = d.optString("infoAdicional", ""),
        activo = d.optBoolean("activo", false),
        // Los documentos antiguos no tienen "aprobado": se consideran aprobados.
        aprobado = if (d.has("aprobado")) d.optBoolean("aprobado", true) else true,
        propietarioUid = d.optString("propietarioUid", "")
    )

    private fun toCategoriaComercio(id: String, d: JSONObject): CategoriaComercio? {
        if (!d.optBoolean("activo", true)) return null
        val nombre = d.optString("nombre", "").trim()
        if (nombre.isBlank()) return null

        return CategoriaComercio(
            id = id,
            nombre = nombre,
            categoriaPadre = d.optString("categoriaPadre", "").trim(),
            orden = d.optInt("orden", 0),
            activo = true
        )
    }

    private fun parseReferenciaParada(value: String): ReferenciaParadaRuta? {
        val trimmed = value.trim()
        return when {
            trimmed.startsWith("lugar:", ignoreCase = true) ||
                trimmed.startsWith("monumento:", ignoreCase = true) -> // "monumento:" es legado
                ReferenciaParadaRuta.Lugar(trimmed.substringAfter(':').trim())
            trimmed.startsWith("comercio:", ignoreCase = true) ->
                ReferenciaParadaRuta.ComercioLocal(trimmed.substringAfter(':').trim())
            else -> null
        }
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index -> optString(index).takeIf { it.isNotBlank() } }
    }

    /**
     * Acepta tanto una lista como un string (documentos legados que guardaban
     * `redesSociales` como texto libre).
     */
    private fun JSONObject.toStringListFlexible(key: String): List<String> {
        optJSONArray(key)?.let { return it.toStringList() }
        val texto = optString(key, "").trim()
        return if (texto.isBlank()) emptyList() else listOf(texto)
    }

    private fun JSONObject.optNullableDouble(key: String): Double? =
        if (has(key) && !isNull(key)) optDouble(key) else null
}
