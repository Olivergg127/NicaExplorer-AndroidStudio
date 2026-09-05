package com.lospuntoycoma.nicaexplorer.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.lospuntoycoma.nicaexplorer.model.SolicitudComercio
import kotlinx.coroutines.tasks.await

/**
 * Guarda solicitudes de negocios sin permitir que la interfaz controle
 * estado, fecha o propietario del documento.
 */
object SolicitudComercioRepository {
    private const val COLLECTION = "solicitudes_comercios"
    private const val ESTADO_PENDIENTE = "pendiente"

    private val db = FirebaseFirestore.getInstance()

    suspend fun enviarSolicitud(solicitud: SolicitudComercio): Result<String> {
        return runCatching {
            val userId = FirebaseRepository.getCurrentUser()?.uid
                ?: error("Debes iniciar sesión para enviar una solicitud.")

            val payload = hashMapOf<String, Any>(
                "nombreNegocio" to solicitud.nombreNegocio.trim(),
                "ciudad" to solicitud.ciudad.trim(),
                "cityId" to solicitud.cityId.trim(),
                "categoria" to solicitud.categoria.trim(),
                "direccion" to solicitud.direccion.trim(),
                "descripcion" to solicitud.descripcion.trim(),
                "telefono" to solicitud.telefono.trim(),
                "whatsapp" to solicitud.whatsapp.trim(),
                "horario" to solicitud.horario.trim(),
                "nombreResponsable" to solicitud.nombreResponsable.trim(),
                "correoResponsable" to solicitud.correoResponsable.trim(),
                "redesSociales" to solicitud.redesSociales.trim(),
                "estado" to ESTADO_PENDIENTE,
                "fechaSolicitud" to FieldValue.serverTimestamp(),
                "userId" to userId
            )

            db.collection(COLLECTION).add(payload).await().id
        }
    }
}
