package com.lospuntoycoma.nicaexplorer.map

import org.maplibre.android.geometry.LatLng

/**
 * Configuración central del mapa para poder cambiar de proveedor o estilo
 * sin repartir URLs por las pantallas.
 */
object MapaConfig {
    // Estilo local mínimo: solo depende de las teselas raster públicas de OSM.
    // Mantener la URL aquí permite cambiar de proveedor sin tocar la pantalla.
    const val OSM_TILE_URL = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"

    val STYLE_JSON: String
        get() = """
            {
              "version": 8,
              "name": "NicaExplorer OSM",
              "sources": {
                "osm": {
                  "type": "raster",
                  "tiles": ["$OSM_TILE_URL"],
                  "tileSize": 256,
                  "maxzoom": 19,
                  "attribution": "© OpenStreetMap contributors"
                }
              },
              "layers": [
                {
                  "id": "osm",
                  "type": "raster",
                  "source": "osm"
                }
              ]
            }
        """.trimIndent()

    const val ATTRIBUTION = "© OpenStreetMap contributors · MapLibre"

    // Vista inicial temporal de todo el país; el enfoque en Juigalpa se
    // incorporará cuando comencemos la fase de comercios y marcadores.
    val NICARAGUA = LatLng(12.8654, -85.2072)
    const val INITIAL_ZOOM = 6.3
}
