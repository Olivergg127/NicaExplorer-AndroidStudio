package com.lospuntoycoma.nicaexplorer.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lospuntoycoma.nicaexplorer.model.Valoracion
import kotlinx.coroutines.tasks.await

/**
 * Valoraciones por estrellas de ciudades, lugares y comercios.
 * Un documento por usuario y objetivo: `valoraciones/{tipo}_{refId}_{uid}`.
 */
object ValoracionesRepository {
    private val db = FirebaseFirestore.getInstance()

    private fun docId(tipo: String, refId: String, uid: String) = "${tipo}_${refId}_$uid"

    /** Estrellas que el usuario dio a un objetivo (null si no ha valorado). */
    suspend fun miValoracion(tipo: String, refId: String, uid: String): Int? = try {
        val document = db.collection("valoraciones").document(docId(tipo, refId, uid)).get().await()
        if (document.exists()) document.getLong("estrellas")?.toInt() else null
    } catch (e: Exception) {
        null
    }

    /** Guarda (o actualiza) la valoración del usuario. */
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
        true
    } catch (e: Exception) {
        false
    }

    /** Promedio y total de valoraciones de un objetivo. */
    suspend fun resumen(tipo: String, refId: String): Pair<Double, Int> {
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

    /** Valoraciones de una ciudad (para recomendaciones del Home). */
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
