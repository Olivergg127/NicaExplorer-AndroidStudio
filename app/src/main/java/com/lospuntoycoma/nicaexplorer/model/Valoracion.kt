package com.lospuntoycoma.nicaexplorer.model

/** Valoración (estrellas) de una ciudad, lugar o comercio. */
data class Valoracion(
    val tipo: String = "",
    val refId: String = "",
    val cityId: String = "",
    val uid: String = "",
    val estrellas: Int = 0
)

/** Tipos de objetivo valorable. */
object TipoValoracion {
    const val CIUDAD = "ciudad"
    const val LUGAR = "lugar"
    const val COMERCIO = "comercio"
}
