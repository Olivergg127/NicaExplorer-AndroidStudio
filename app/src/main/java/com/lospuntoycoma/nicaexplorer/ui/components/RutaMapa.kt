package com.lospuntoycoma.nicaexplorer.ui.components

import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lospuntoycoma.nicaexplorer.map.MapaConfig
import com.lospuntoycoma.nicaexplorer.map.MapaMarkerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.net.HttpURLConnection
import java.net.URL

private const val MAPA_TAG = "NicaExplorerRutaMapa"
private const val COLOR_RUTA = 0xFF00A08A.toInt()

/**
 * Parada de una ruta con coordenadas, lista para pintarse en el mapa.
 *
 * @param itemIndex posición de la tarjeta de la parada en el detalle, para poder
 *  desplazar la lista al pulsar su pin.
 */
data class ParadaMapa(
    val numero: Int,
    val latitud: Double,
    val longitud: Double,
    val titulo: String,
    val itemIndex: Int
)

/**
 * Mapa de la ruta turística: pines numerados por parada y el camino que las une.
 * Al pulsar un pin se notifica la parada para que la pantalla desplace la lista.
 *
 * La cámara se encuadra en las paradas recibidas; sin paradas muestra Nicaragua.
 */
@Composable
fun RutaMapa(
    paradas: List<ParadaMapa>,
    onParadaClick: (ParadaMapa) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val marcadorItems = remember { mutableMapOf<Long, ParadaMapa>() }
    val clickActual = rememberUpdatedState(onParadaClick)

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var estiloCargado by remember { mutableStateOf(false) }

    // El encuadre se hace una sola vez; luego se restaura la última cámara para
    // que el mapa no vuelva a hacer zoom al salir y entrar de la vista.
    var encuadrado by rememberSaveable { mutableStateOf(false) }
    var camaraLat by rememberSaveable { mutableStateOf(Double.NaN) }
    var camaraLng by rememberSaveable { mutableStateOf(Double.NaN) }
    var camaraZoom by rememberSaveable { mutableStateOf(-1.0) }

    // Línea directa entre paradas como respaldo inmediato.
    var camino by remember(paradas) {
        mutableStateOf(paradas.map { LatLng(it.latitud, it.longitud) })
    }

    LaunchedEffect(paradas) {
        val porCarretera = RutaMapaRepository.caminoPorCarretera(paradas)
        if (porCarretera.size >= 2) {
            camino = porCarretera
        }
    }

    Box(
        modifier = modifier.clip(RoundedCornerShape(20.dp))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                MapLibre.getInstance(viewContext)
                // TextureView: se integra en la jerarquía de Compose y se desplaza
                // correctamente dentro de un contenedor con scroll.
                val opciones = MapLibreMapOptions().apply { textureMode(true) }
                MapView(viewContext, opciones).also { creado ->
                    creado.addOnDidFinishLoadingStyleListener {
                        Log.d(MAPA_TAG, "Estilo del mapa de la ruta cargado")
                    }
                    creado.addOnDidFailLoadingMapListener { error ->
                        Log.e(MAPA_TAG, "No se pudo cargar el mapa de la ruta: $error")
                    }
                    // Evita que el scroll de la lista intercepte los gestos del mapa
                    // (arrastrar/zoom) mientras el dedo empieza sobre el mapa.
                    creado.setOnTouchListener { vista, evento ->
                        when (evento.actionMasked) {
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                                vista.parent?.requestDisallowInterceptTouchEvent(true)
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                                vista.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                        false
                    }
                    creado.getMapAsync { map ->
                        mapLibreMap = map
                        map.uiSettings.apply {
                            isAttributionEnabled = true
                            isLogoEnabled = true
                            isCompassEnabled = true
                            isRotateGesturesEnabled = false
                        }
                        // Evita que el encuadre se acerque demasiado cuando las
                        // paradas están muy juntas (máx. ~16.5).
                        map.setMaxZoomPreference(16.5)
                        map.setOnMarkerClickListener { marker ->
                            marcadorItems[marker.id]?.let { clickActual.value(it) }
                            true
                        }
                        map.addOnCameraIdleListener {
                            val camara = map.cameraPosition
                            camara.target?.let { objetivo ->
                                camaraLat = objetivo.latitude
                                camaraLng = objetivo.longitude
                            }
                            camaraZoom = camara.zoom
                        }
                        map.setStyle(Style.Builder().fromJson(MapaConfig.STYLE_JSON)) {
                            estiloCargado = true
                        }
                    }
                }
            },
            update = { }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 2.dp
        ) {
            Text(
                text = MapaConfig.ATTRIBUTION,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    LaunchedEffect(mapLibreMap, estiloCargado, paradas, camino) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!estiloCargado) return@LaunchedEffect

        marcadorItems.clear()
        map.clear()

        if (camino.size >= 2) {
            map.addPolyline(
                PolylineOptions()
                    .addAll(camino)
                    .color(COLOR_RUTA)
                    .width(5f)
            )
        }

        paradas.forEach { parada ->
            val marcador = map.addMarker(
                MarkerOptions()
                    .position(LatLng(parada.latitud, parada.longitud))
                    .icon(MapaMarkerFactory.numeroIcon(context, parada.numero))
                    .title(parada.titulo)
            )
            marcadorItems[marcador.id] = parada
        }

        if (!encuadrado) {
            // Primer encuadre (con animación) solo al cargar la vista.
            when {
                paradas.size == 1 -> {
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(paradas.first().latitud, paradas.first().longitud))
                        .zoom(15.0)
                        .build()
                }
                paradas.size > 1 -> {
                    val bounds = LatLngBounds.Builder().apply {
                        paradas.forEach { include(LatLng(it.latitud, it.longitud)) }
                    }.build()
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 90))
                }
                else -> {
                    map.cameraPosition = CameraPosition.Builder()
                        .target(MapaConfig.NICARAGUA)
                        .zoom(MapaConfig.INITIAL_ZOOM)
                        .build()
                }
            }
            encuadrado = true
        } else if (!camaraLat.isNaN() && camaraZoom > 0.0) {
            // Al volver a la vista se restaura la última cámara sin animación.
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(camaraLat, camaraLng))
                .zoom(camaraZoom)
                .build()
        }
    }
}

/**
 * Obtiene el trazado por carretera de una secuencia de paradas usando OSRM.
 * Si el servicio no responde, se usan líneas rectas (la pantalla ya dibuja el respaldo).
 */
private object RutaMapaRepository {
    suspend fun caminoPorCarretera(paradas: List<ParadaMapa>): List<LatLng> =
        withContext(Dispatchers.IO) {
            if (paradas.size < 2) return@withContext emptyList()

            val coordenadas = paradas.joinToString(";") { "${it.longitud},${it.latitud}" }
            val url = "https://router.project-osrm.org/route/v1/driving/$coordenadas" +
                "?overview=full&geometries=geojson"

            runCatching {
                val conexion = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8_000
                    readTimeout = 8_000
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "NicaExplorer/1.0 (Android)")
                }

                val codigo = conexion.responseCode
                val cuerpo = (if (codigo in 200..299) conexion.inputStream else conexion.errorStream)
                    ?.bufferedReader()?.use { it.readText() }.orEmpty()
                conexion.disconnect()

                val puntos = JSONObject(cuerpo)
                    .optJSONArray("routes")?.optJSONObject(0)
                    ?.optJSONObject("geometry")?.optJSONArray("coordinates")

                if (codigo !in 200..299 || puntos == null || puntos.length() == 0) {
                    Log.w(MAPA_TAG, "OSRM sin geometría (HTTP $codigo); se usan líneas rectas")
                    return@runCatching emptyList<LatLng>()
                }

                (0 until puntos.length()).map { indice ->
                    val punto = puntos.getJSONArray(indice)
                    LatLng(punto.getDouble(1), punto.getDouble(0))
                }
            }.onFailure { error ->
                Log.w(MAPA_TAG, "OSRM no disponible (${error.message}); se usan líneas rectas")
            }.getOrDefault(emptyList())
        }
}
