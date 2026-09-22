package com.lospuntoycoma.nicaexplorer.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lospuntoycoma.nicaexplorer.model.Valoracion
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Valoraciones por estrellas de ciudades, lugares y comercios.
 * Un documento por usuario y objetivo: `valoraciones/{tipo}_{refId}_{uid}`.
 *
 * Para los rankings públicos se usa la API (colección `valoraciones` sin uid),
 * expuesta como [publicas]; se refresca al iniciar y después de cada valoración
 * para que los "top" se actualicen solos.
 */
object ValoracionesRepository {
    private val db = FirebaseFirestore.getInstance()

    private val _publicas = MutableStateFlow<List<Valoracion>>(emptyList())

    /** Valoraciones públicas en memoria (sin uid) para recomendados. */
    val publicas: StateFlow<List<Valoracion>> = _publicas.asStateFlow()

    private fun docId(tipo: String, refId: String, uid: String) = "${tipo}_${refId}_$uid"

    /** Estrellas que el usuario dio a un objetivo (null si no ha valorado). */
    suspend fun miValoracion(tipo: String, refId: String, uid: String): Int? = try {
        val document = db.collection("valoraciones").document(docId(tipo, refId, uid)).get().await()
        if (document.exists()) document.getLong("estrellas")?.toInt() else null
    } catch (e: Exception) {
        null
    }

    /** Guarda (o actualiza) la valoración del usuario y refresca la caché pública. */
    suspend fun guardar(
        tipo: String,
        refId: String,
        cityId: String,
        uid: String,
        estrellas: Int
    ): Boolean = try {
        db.collection("valoraciones").document(docId(tipo, refId, uid)).set(
            mapOf(
                "tipo" to tipo,
                "refId" to refId,
                "cityId" to cityId,
                "uid" to uid,
                "estrellas" to estrellas,
                "actualizadoEn" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        // Refresca el ranking con la nueva valoración.
        refreshPublicas()
        true
    } catch (e: Exception) {
        false
    }

    /** Promedio y total de valoraciones de un objetivo (lectura directa a Firestore). */
    suspend fun resumen(tipo: String, refId: String): Pair<Double, Int> {
        val publico = resumenPublico(tipo, refId)
        if (publico.second > 0) return publico

        val estrellas = try {
            db.collection("valoraciones").whereEqualTo("refId", refId).get().await()
                .documents.mapNotNull { document ->
                    if ((document.getString("tipo") ?: "") != tipo) {
                        return@mapNotNull null
                    }
                    document.getLong("estrellas")?.toInt()
                }
        } catch (e: Exception) {
            emptyList()
        }

        return if (estrellas.isEmpty()) 0.0 to 0 else estrellas.average() to estrellas.size
    }

    /** Promedio/total desde la caché pública (funciona sin sesión). */
    fun resumenPublico(tipo: String, refId: String): Pair<Double, Int> {
        val estrellas = _publicas.value
            .filter { it.tipo == tipo && it.refId == refId }
            .map { it.estrellas }
        return if (estrellas.isEmpty()) 0.0 to 0 else estrellas.average() to estrellas.size
    }

    /** Valoraciones de una ciudad desde la caché pública. */
    fun deCiudadPublicas(cityId: String): List<Valoracion> =
        _publicas.value.filter { it.cityId.equals(cityId, ignoreCase = true) }

    /** Recarga las valoraciones públicas desde la API. */
    suspend fun refreshPublicas(): List<Valoracion> {
        val lista = try {
            ApiRepository.getValoraciones()
        } catch (e: Exception) {
            emptyList()
        }
        if (lista.isNotEmpty()) {
            _publicas.value = lista
        }
        return _publicas.value
    }

    private fun actualizarCacheLocal(tipo: String, refId: String, cityId: String, estrellas: Int) {
        val actuales = _publicas.value.toMutableList()
        actuales.removeAll { it.tipo == tipo && it.refId == refId }
        actuales.add(Valoracion(tipo = tipo, refId = refId, cityId = cityId, uid = "", estrellas = estrellas))
        _publicas.value = actuales
    }

    /** Valoraciones de una ciudad (compatibilidad; usa Firestore). */
    suspend fun deCiudad(cityId: String): List<Valoracion> = try {
        db.collection("valoraciones").whereEqualTo("cityId", cityId).get().await()
            .documents.mapNotNull { document ->
                val estrellas = document.getLong("estrellas")?.toInt() ?: return@mapNotNull null
                Valoracion(
                    tipo = document.getString("tipo") ?: "",
                    refId = document.getString("refId") ?: "",
                    cityId = document.getString("cityId") ?: "",
                    uid = document.getString("uid") ?: "",
                    estrellas = estrellas
                )
            }
    } catch (e: Exception) {
        emptyList()
    }
}
