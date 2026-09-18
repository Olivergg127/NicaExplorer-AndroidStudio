package com.lospuntoycoma.nicaexplorer.ui.screens

import android.os.Bundle
import android.util.Log
import android.app.Activity
import android.view.ViewGroup
import com.lospuntoycoma.nicaexplorer.map.MapaConfig
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.camera.CameraPosition

private const val MAPA_ACTIVITY_TAG = "NicaExplorerMapActivity"

/** Pantalla nativa mínima para aislar el MapView del ciclo de vida de Compose. */
class MapaActivity : Activity() {
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)

        mapView = MapView(this, MapLibreMapOptions())
        setContentView(
            mapView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        mapView.onCreate(savedInstanceState)
        mapView.addOnDidFinishLoadingStyleListener {
            Log.e(MAPA_ACTIVITY_TAG, "Estilo cargado")
        }
        mapView.addOnDidFailLoadingMapListener { error ->
            Log.e(MAPA_ACTIVITY_TAG, "Error cargando mapa: $error")
        }
        mapView.getMapAsync { map ->
            Log.e(MAPA_ACTIVITY_TAG, "MapLibreMap listo en actividad nativa")
            map.setStyle(Style.Builder().fromJson(MapaConfig.STYLE_JSON)) {
                map.cameraPosition = CameraPosition.Builder()
                    .target(MapaConfig.NICARAGUA)
                    .zoom(MapaConfig.INITIAL_ZOOM)
                    .build()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (::mapView.isInitialized) mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (::mapView.isInitialized) mapView.onResume()
    }

    override fun onPause() {
        if (::mapView.isInitialized) mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        if (::mapView.isInitialized) mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (::mapView.isInitialized) mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::mapView.isInitialized) mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (::mapView.isInitialized) mapView.onDestroy()
        super.onDestroy()
    }
}
