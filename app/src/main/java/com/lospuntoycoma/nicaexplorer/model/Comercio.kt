package com.lospuntoycoma.nicaexplorer.model

/**
 * Representa un comercio o restaurante registrado en Firestore (colección "comercios").
 * Todos los campos tienen valores por defecto seguros para no provocar crashes
 * si algún documento está incompleto.
 */
data class Comercio(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val descripcion: String = "",
    val ciudad: String = "",
    val direccion: String = "",
    val horario: String = "",
    val imagenUrl: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val telefono: String = "",
    val whatsapp: String = "",
    val tieneWhatsapp: Boolean = false,
    val activo: Boolean = false
)
