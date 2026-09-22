package com.lospuntoycoma.nicaexplorer.model

/**
 * Representa un comercio o restaurante registrado en Firestore (colección "comercios").
 * Todos los campos tienen valores por defecto seguros para no provocar crashes
 * si algún documento está incompleto.
 *
 * Visibilidad pública: `activo && aprobado`. Los comercios creados por un
 * usuario COMERCIO nacen con `aprobado = false` hasta que un administrador los
 * aprueba; el propietario puede activarlos/desactivarlos.
 */
data class Comercio(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val categoriaPadre: String = "",
    val descripcion: String = "",
    val ciudad: String = "",
    val cityId: String = "",
    val direccion: String = "",
    val horario: String = "",
    val diasAtencion: String = "",
    val imagenUrl: String = "",
    val logoUrl: String = "",
    val galeria: List<String> = emptyList(),
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val telefono: String = "",
    val whatsapp: String = "",
    val tieneWhatsapp: Boolean = false,
    val correo: String = "",
    /** Entradas en formato "red|valor"; ver [RedesSociales]. */
    val redesSociales: List<String> = emptyList(),
    val servicios: List<String> = emptyList(),
    val productos: List<String> = emptyList(),
    val infoAdicional: String = "",
    val activo: Boolean = false,
    val aprobado: Boolean = true,
    val propietarioUid: String = ""
) {
    /** Visible para el público: aprobado por el admin y activo por el dueño. */
    val visiblePublicamente: Boolean
        get() = activo && aprobado
}
