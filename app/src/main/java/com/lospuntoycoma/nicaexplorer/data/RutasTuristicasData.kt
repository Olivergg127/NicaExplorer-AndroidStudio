package com.lospuntoycoma.nicaexplorer.data

import com.lospuntoycoma.nicaexplorer.model.ReferenciaParadaRuta
import com.lospuntoycoma.nicaexplorer.model.RutaTuristica

/**
 * Rutas disponibles en el prototipo.
 *
 * Solo Juigalpa tiene una ruta definida. Para cualquier otra ciudad se devuelve
 * null, de modo que la interfaz pueda mostrar el estado "Rutas próximamente" sin
 * inventar recorridos ni paradas.
 */
object RutasTuristicasData {

    val rutasPorCiudad: Map<String, RutaTuristica> = mapOf(
        "juigalpa" to RutaTuristica(
            id = "ruta_cultural_juigalpa",
            cityId = "juigalpa",
            nombre = "Ruta cultural de Juigalpa",
            descripcion = "Un recorrido por el patrimonio de Juigalpa que combina lugares con distinta afluencia estimada y una parada en un negocio local.",
            duracionEstimada = "2–3 horas",
            notaDuracion = "Duración orientativa del prototipo; no usa GPS ni información en tiempo real.",
            objetivos = listOf(
                "Distribuir las visitas entre lugares con distinta afluencia estimada.",
                "Promover el patrimonio local de Juigalpa.",
                "Conectar a los turistas con negocios locales de Juigalpa.",
                "Fomentar prácticas de turismo responsable."
            ),
            paradas = listOf(
                ReferenciaParadaRuta.Monumento("homenaje_madre_juigalpina"),
                ReferenciaParadaRuta.Monumento("estatua_museo_juigalpa"),
                ReferenciaParadaRuta.Monumento("toro_chontaleno"),
                ReferenciaParadaRuta.ComercioLocal("Restaurantes-1")
            )
        )
    )

    fun rutaParaCiudad(cityId: String): RutaTuristica? =
        rutasPorCiudad[cityId.trim().lowercase()]
}
