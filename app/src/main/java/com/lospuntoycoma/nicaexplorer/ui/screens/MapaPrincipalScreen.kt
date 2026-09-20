package com.lospuntoycoma.nicaexplorer.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.data.UbicacionHelper
import com.lospuntoycoma.nicaexplorer.map.MapaConfig
import com.lospuntoycoma.nicaexplorer.map.MapaMarkerFactory
import com.lospuntoycoma.nicaexplorer.model.City
import com.lospuntoycoma.nicaexplorer.ui.components.NicaTopBar
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.MapaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/** Tipo de punto que se muestra en el mapa principal. */
enum class TipoMapa { TODOS, LUGARES, COMERCIOS }

/** Punto del mapa principal. */
data class MapaPin(
    val id: String,
    val tipo: TipoMapa,
    val lat: Double,
    val lng: Double,
    val titulo: String,
    val cityId: String? = null
)

/**
 * Mapa principal: pines de lugares y comercios, selector de ciudad y filtro por tipo.
 * Detecta la ciudad actual con el GPS del sistema (si hay permiso).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaPrincipalScreen(
    onBack: () -> Unit,
    onVerLugar: (cityId: String, placeId: String) -> Unit,
    onVerComercio: (comercioId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapaViewModel: MapaViewModel = viewModel()
    val uiState by mapaViewModel.uiState.collectAsState()

    val ciudades = SampleData.cities
    val lugares = SampleData.allPlaces

    var ciudadSeleccionada by remember { mutableStateOf<City?>(null) }
    var tipoSeleccionado by remember { mutableStateOf(TipoMapa.TODOS) }
    var permisoUbicacion by remember { mutableStateOf(UbicacionHelper.tienePermiso(context)) }

    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val concedido = resultado.values.any { it }
        permisoUbicacion = concedido
        if (concedido) {
            ubicarCiudad(
                context = context,
                scope = scope,
                ciudades = ciudades,
                onCiudad = { ciudad ->
                    ciudadSeleccionada = ciudad
                    tipoSeleccionado = TipoMapa.TODOS
                },
                onError = { mensaje -> Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show() }
            )
        } else {
            Toast.makeText(context, "Sin permiso de ubicación.", Toast.LENGTH_SHORT).show()
        }
    }

    val ciudadFiltro = ciudadSeleccionada

    val pins = remember(uiState.comercios, lugares, ciudadFiltro, tipoSeleccionado) {
        buildList {
            if (tipoSeleccionado == TipoMapa.TODOS || tipoSeleccionado == TipoMapa.LUGARES) {
                lugares
                    .filter { it.latitud != null && it.longitud != null }
                    .filter { ciudadFiltro == null || it.cityId.equals(ciudadFiltro.id, ignoreCase = true) }
                    .forEach { lugar ->
                        add(
                            MapaPin(
                                id = lugar.id,
                                tipo = TipoMapa.LUGARES,
                                lat = lugar.latitud!!,
                                lng = lugar.longitud!!,
                                titulo = lugar.name,
                                cityId = lugar.cityId
                            )
                        )
                    }
            }

            if (tipoSeleccionado == TipoMapa.TODOS || tipoSeleccionado == TipoMapa.COMERCIOS) {
                uiState.comercios
                    .filter {
                        ciudadFiltro == null ||
                            it.ciudad.trim().equals(ciudadFiltro.id, ignoreCase = true) ||
                            it.ciudad.trim().equals(ciudadFiltro.name, ignoreCase = true)
                    }
                    .forEach { comercio ->
                        add(
                            MapaPin(
                                id = comercio.id,
                                tipo = TipoMapa.COMERCIOS,
                                lat = comercio.latitud,
                                lng = comercio.longitud,
                                titulo = comercio.nombre
                            )
                        )
                    }
            }
        }
    }

    Scaffold(
        topBar = { NicaTopBar(title = "Mapa", onBack = onBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MapaLibrePrincipal(
                pins = pins,
                centro = ciudadFiltro?.let { ciudad ->
                    val lat = ciudad.latitud
                    val lng = ciudad.longitud
                    if (lat != null && lng != null) LatLng(lat, lng) else null
                },
                onPinClick = { pin ->
                    when (pin.tipo) {
                        TipoMapa.LUGARES -> pin.cityId?.let { onVerLugar(it, pin.id) }
                        TipoMapa.COMERCIOS -> onVerComercio(pin.id)
                        TipoMapa.TODOS -> Unit
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            PanelFiltrosMapa(
                ciudades = ciudades,
                ciudadSeleccionada = ciudadSeleccionada,
                tipoSeleccionado = tipoSeleccionado,
                onCiudad = { ciudadSeleccionada = it },
                onTipo = { tipoSeleccionado = it },
                onMiUbicacion = {
                    if (permisoUbicacion) {
                        ubicarCiudad(
                            context = context,
                            scope = scope,
                            ciudades = ciudades,
                            onCiudad = { ciudad ->
                                ciudadSeleccionada = ciudad
                                tipoSeleccionado = TipoMapa.TODOS
                            },
                            onError = { mensaje -> Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show() }
                        )
                    } else {
                        permisoLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                },
                onInicio = {
                    ciudadSeleccionada = null
                    tipoSeleccionado = TipoMapa.TODOS
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun PanelFiltrosMapa(
    ciudades: List<City>,
    ciudadSeleccionada: City?,
    tipoSeleccionado: TipoMapa,
    onCiudad: (City?) -> Unit,
    onTipo: (TipoMapa) -> Unit,
    onMiUbicacion: () -> Unit,
    onInicio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Explora en el mapa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onInicio) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Inicio")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item {
                    FilterChip(
                        selected = ciudadSeleccionada == null,
                        onClick = { onCiudad(null) },
                        label = { Text("Todas") }
                    )
                }
                items(ciudades, key = { it.id }) { ciudad ->
                    FilterChip(
                        selected = ciudadSeleccionada?.id == ciudad.id,
                        onClick = { onCiudad(ciudad) },
                        label = { Text(ciudad.name) }
                    )
                }
                item {
                    AssistChip(
                        onClick = onMiUbicacion,
                        label = { Text("Mi ubicación") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = tipoSeleccionado == TipoMapa.TODOS,
                        onClick = { onTipo(TipoMapa.TODOS) },
                        label = { Text("Todos") }
                    )
                }
                item {
                    FilterChip(
                        selected = tipoSeleccionado == TipoMapa.LUGARES,
                        onClick = { onTipo(TipoMapa.LUGARES) },
                        label = { Text("Lugares") }
                    )
                }
                item {
                    FilterChip(
                        selected = tipoSeleccionado == TipoMapa.COMERCIOS,
                        onClick = { onTipo(TipoMapa.COMERCIOS) },
                        label = { Text("Comercios") }
                    )
                }
            }
        }
    }
}

@Composable
private fun MapaLibrePrincipal(
    pins: List<MapaPin>,
    centro: LatLng?,
    onPinClick: (MapaPin) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clickActual = rememberUpdatedState(onPinClick)
    val pinItems = remember { mutableMapOf<Long, MapaPin>() }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var estiloCargado by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                MapLibre.getInstance(viewContext)
                MapView(viewContext, MapLibreMapOptions().apply { textureMode(true) }).also { creado ->
                    creado.addOnDidFinishLoadingStyleListener {
                        estiloCargado = true
                    }
                    creado.getMapAsync { map ->
                        mapLibreMap = map
                        map.uiSettings.apply {
                            isAttributionEnabled = true
                            isLogoEnabled = true
                            isCompassEnabled = true
                            isRotateGesturesEnabled = false
                        }
                        map.setMaxZoomPreference(17.0)
                        map.setOnMarkerClickListener { marker ->
                            pinItems[marker.id]?.let { clickActual.value(it) }
                            true
                        }
                        map.setStyle(Style.Builder().fromJson(MapaConfig.STYLE_JSON))
                    }
                }
            },
            update = { }
        )
    }

    LaunchedEffect(mapLibreMap, estiloCargado, pins, centro) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!estiloCargado) return@LaunchedEffect

        pinItems.clear()
        map.clear()

        pins.forEach { pin ->
            val color = if (pin.tipo == TipoMapa.COMERCIOS) {
                MapaMarkerFactory.COLOR_COMERCIO
            } else {
                MapaMarkerFactory.COLOR_LUGAR
            }
            val marcador = map.addMarker(
                MarkerOptions()
                    .position(LatLng(pin.lat, pin.lng))
                    .icon(MapaMarkerFactory.puntoIcon(context, color))
                    .title(pin.titulo)
            )
            pinItems[marcador.id] = pin
        }

        when {
            centro != null -> map.animateCamera(CameraUpdateFactory.newLatLngZoom(centro, 13.0))

            pins.size > 1 -> {
                val bounds = LatLngBounds.Builder().apply {
                    pins.forEach { include(LatLng(it.lat, it.lng)) }
                }.build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 90))
            }

            pins.size == 1 -> map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(pins.first().lat, pins.first().lng), 14.0)
            )

            else -> map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(MapaConfig.NICARAGUA, MapaConfig.INITIAL_ZOOM)
            )
        }
    }
}

private fun ubicarCiudad(
    context: Context,
    scope: CoroutineScope,
    ciudades: List<City>,
    onCiudad: (City) -> Unit,
    onError: (String) -> Unit
) {
    scope.launch {
        val ubicacion = UbicacionHelper.ultimaUbicacion(context)
        if (ubicacion == null) {
            onError("No se pudo obtener tu ubicación. Revisa el GPS e intenta de nuevo.")
            return@launch
        }

        val ciudad = UbicacionHelper.ciudadMasCercana(ubicacion.latitude, ubicacion.longitude, ciudades)
        if (ciudad != null) {
            onCiudad(ciudad)
        } else {
            onError("No estás cerca de una ciudad registrada.")
        }
    }
}
