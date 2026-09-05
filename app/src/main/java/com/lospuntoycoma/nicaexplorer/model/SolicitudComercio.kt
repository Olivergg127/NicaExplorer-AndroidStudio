package com.lospuntoycoma.nicaexplorer.model

/**
 * Datos proporcionados por un negocio para solicitar su publicación.
 * Los campos controlados por el servidor (estado, fechaSolicitud y userId)
 * se agregan exclusivamente en el repositorio.
 */
data class SolicitudComercio(
    val nombreNegocio: String,
    val ciudad: String,
    val cityId: String,
    val categoria: String,
    val direccion: String,
    val descripcion: String,
    val telefono: String,
    val whatsapp: String,
    val horario: String,
    val nombreResponsable: String,
    val correoResponsable: String,
    val redesSociales: String = ""
)
