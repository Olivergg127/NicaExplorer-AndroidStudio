package com.lospuntoycoma.nicaexplorer.model

/**
 * Referencia a una parada cuyos datos se resuelven desde las fuentes existentes.
 * Así la ruta no duplica información de [Monument] ni de [Comercio].
 */
sealed interface ReferenciaParadaRuta {
    val referenciaId: String

    data class Monumento(
        val monumentId: String
    ) : ReferenciaParadaRuta {
        override val referenciaId: String = monumentId
    }

    data class ComercioLocal(
        val comercioId: String
    ) : ReferenciaParadaRuta {
        override val referenciaId: String = comercioId
    }
}

/**
 * Definición local de una ruta turística predefinida.
 *
 * Las distancias no forman parte del modelo porque esta primera versión no usa
 * GPS, mapas ni navegación en tiempo real.
 */
data class RutaTuristica(
    val id: String,
    val cityId: String,
    val nombre: String,
    val descripcion: String,
    val duracionEstimada: String,
    val notaDuracion: String,
    val objetivos: List<String>,
    val paradas: List<ReferenciaParadaRuta>
) {
    val numeroParadas: Int
        get() = paradas.size
}
