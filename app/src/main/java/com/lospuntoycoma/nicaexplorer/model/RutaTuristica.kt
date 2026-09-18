package com.lospuntoycoma.nicaexplorer.model

/**
 * Referencia a una parada cuyos datos se resuelven desde las fuentes existentes.
 * Así la ruta no duplica información de [Place] ni de [Comercio].
 */
sealed interface ReferenciaParadaRuta {
    val referenciaId: String

    data class Lugar(
        val placeId: String
    ) : ReferenciaParadaRuta {
        override val referenciaId: String = placeId
    }

    data class ComercioLocal(
        val comercioId: String
    ) : ReferenciaParadaRuta {
        override val referenciaId: String = comercioId
    }
}

/**
 * Ruta turística de una ciudad.
 *
 * En Firestore vive en la colección `rutas` y sus paradas se guardan como
 * cadenas ("lugar:<id>" o "comercio:<id>"), que se resuelven contra las
 * fuentes existentes para no duplicar información.
 *
 * Esta versión no usa GPS, mapas ni navegación en tiempo real.
 */
data class RutaTuristica(
    val id: String,
    val cityId: String,
    val nombre: String,
    val descripcion: String,
    val duracionEstimada: String,
    val notaDuracion: String,
    val objetivos: List<String>,
    val paradas: List<ReferenciaParadaRuta>,
    val imagenUrl: String? = null,
    val orden: Int = 0,
    val activo: Boolean = true
) {
    val numeroParadas: Int
        get() = paradas.size
}
