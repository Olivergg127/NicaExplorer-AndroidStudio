package com.lospuntoycoma.nicaexplorer.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.data.UbicacionHelper
import com.lospuntoycoma.nicaexplorer.map.MapaConfig
import com.lospuntoycoma.nicaexplorer.map.MapaMarkerFactory
import com.lospuntoycoma.nicaexplorer.model.City
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.MapaViewModel
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
    val cityId: String? = null,
    val categoriaPadre: String = "",
    val color: Int = MapaMarkerFactory.COLOR_LUGAR
)

/** Categoría mostrada como tarjeta en la parte superior del mapa. */
private data class CategoriaMapa(
    val nombre: String,
    val etiqueta: String,
    val total: Int,
    val icono: ImageVector,
    val color: Color
)

/**
 * Mapa principal con el diseño "Descubre a tu alrededor":
 * encabezado con ciudad, buscador, rejilla de categorías y el mapa debajo.
 */
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
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var consulta by remember { mutableStateOf("") }
    var ubicacionUsuario by remember { mutableStateOf<LatLng?>(null) }
    var permisoUbicacion by remember { mutableStateOf(UbicacionHelper.tienePermiso(context)) }
    var mapaLibre by remember { mutableStateOf<MapLibreMap?>(null) }
    var menuCiudad by remember { mutableStateOf(false) }
    var menuFiltro by remember { mutableStateOf(false) }

    fun detectarUbicacion(centrar: Boolean) {
        scope.launch {
            val ubicacion = UbicacionHelper.ultimaUbicacion(context)
            if (ubicacion == null) {
                Toast.makeText(context, "No se pudo obtener tu ubicación. Revisa el GPS.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val punto = LatLng(ubicacion.latitude, ubicacion.longitude)
            ubicacionUsuario = punto

            if (centrar) {
                mapaLibre?.animateCamera(CameraUpdateFactory.newLatLngZoom(punto, 15.0))
            }

            val ciudad = UbicacionHelper.ciudadMasCercana(ubicacion.latitude, ubicacion.longitude, ciudades)
            if (ciudad != null) {
                ciudadSeleccionada = ciudad
            }
        }
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val concedido = resultado.values.any { it }
        permisoUbicacion = concedido
        if (concedido) {
            detectarUbicacion(centrar = true)
        } else {
            Toast.makeText(context, "Sin permiso de ubicación.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!permisoUbicacion) {
            permisoLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            detectarUbicacion(centrar = false)
        }
    }

    // Categorías reales tomadas de los comercios cargados (por categoría superior).
    val categorias = remember(uiState.comercios) {
        uiState.comercios
            .map { it.categoriaPadre.ifBlank { it.categoria }.trim() }
            .filter { it.isNotEmpty() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(8)
            .map { (nombre, total) ->
                val (icono, color) = categoriaVisual(nombre)
                CategoriaMapa(nombre, etiquetaCategoria(nombre), total, icono, color)
            }
    }

    val ciudadFiltro = ciudadSeleccionada
    val pins = remember(
        uiState.comercios,
        lugares,
        ciudadFiltro,
        tipoSeleccionado,
        categoriaSeleccionada,
        consulta
    ) {
        val mostrarLugares = categoriaSeleccionada == null &&
            (tipoSeleccionado == TipoMapa.TODOS || tipoSeleccionado == TipoMapa.LUGARES)
        val mostrarComercios = categoriaSeleccionada != null ||
            tipoSeleccionado == TipoMapa.TODOS || tipoSeleccionado == TipoMapa.COMERCIOS

        buildList {
            if (mostrarLugares) {
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

            if (mostrarComercios) {
                uiState.comercios
                    .filter {
                        ciudadFiltro == null ||
                            it.ciudad.trim().equals(ciudadFiltro.id, ignoreCase = true) ||
                            it.ciudad.trim().equals(ciudadFiltro.name, ignoreCase = true) ||
                            it.cityId.trim().equals(ciudadFiltro.id, ignoreCase = true)
                    }
                    .filter {
                        categoriaSeleccionada == null ||
                            it.categoriaPadre.equals(categoriaSeleccionada, ignoreCase = true)
                    }
                    .forEach { comercio ->
                        add(
                            MapaPin(
                                id = comercio.id,
                                tipo = TipoMapa.COMERCIOS,
                                lat = comercio.latitud,
                                lng = comercio.longitud,
                                titulo = comercio.nombre,
                                categoriaPadre = comercio.categoriaPadre,
                                color = categoriaVisual(
                                    comercio.categoriaPadre.ifBlank { comercio.categoria }
                                ).second.toArgb()
                            )
                        )
                    }
            }
        }.let { lista ->
            val texto = consulta.trim()
            if (texto.isEmpty()) lista else lista.filter { it.titulo.contains(texto, ignoreCase = true) }
        }
    }

    val centro = ciudadFiltro?.let { ciudad ->
        val lat = ciudad.latitud
        val lng = ciudad.longitud
        if (lat != null && lng != null) LatLng(lat, lng) else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        EncabezadoMapa(
            ciudad = ciudadFiltro,
            ciudades = ciudades,
            menuAbierto = menuCiudad,
            onMenuAbierto = { menuCiudad = it },
            onCiudad = { ciudadSeleccionada = it },
            onBack = onBack
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Descubre\na tu alrededor",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Explora los mejores locales y comercios de tu ciudad.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            FilaBuscador(
                consulta = consulta,
                onConsulta = { consulta = it },
                tipoSeleccionado = tipoSeleccionado,
                menuAbierto = menuFiltro,
                onMenuAbierto = { menuFiltro = it },
                onTipo = {
                    tipoSeleccionado = it
                    categoriaSeleccionada = null
                }
            )

            if (categorias.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                RejillaCategorias(
                    categorias = categorias,
                    seleccionada = categoriaSeleccionada,
                    onSeleccion = { categoriaSeleccionada = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
        ) {
            MapaLibrePrincipal(
                pins = pins,
                centro = centro,
                ubicacionUsuario = ubicacionUsuario,
                onPinClick = { pin ->
                    when (pin.tipo) {
                        TipoMapa.LUGARES -> pin.cityId?.let { onVerLugar(it, pin.id) }
                        TipoMapa.COMERCIOS -> onVerComercio(pin.id)
                        TipoMapa.TODOS -> Unit
                    }
                },
                onMapReady = { mapaLibre = it },
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BotonMapa(
                    icono = Icons.Filled.Navigation,
                    descripcion = "Mi ubicación",
                    onClick = {
                        if (permisoUbicacion) detectarUbicacion(centrar = true)
                        else permisoLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                )
                BotonMapa(
                    icono = Icons.Filled.Layers,
                    descripcion = "Ver todo",
                    onClick = {
                        val map = mapaLibre ?: return@BotonMapa
                        if (centro != null) {
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(centro, 13.0))
                        } else if (pins.size > 1) {
                            val bounds = LatLngBounds.Builder().apply {
                                pins.forEach { include(LatLng(it.lat, it.lng)) }
                            }.build()
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 90))
                        } else {
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(MapaConfig.NICARAGUA, MapaConfig.INITIAL_ZOOM)
                            )
                        }
                    }
                )
            }

            BotonMapa(
                icono = Icons.Filled.MyLocation,
                descripcion = "Centrar en mi ubicación",
                size = 54.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = {
                    if (permisoUbicacion) detectarUbicacion(centrar = true)
                    else permisoLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun EncabezadoMapa(
    ciudad: City?,
    ciudades: List<City>,
    menuAbierto: Boolean,
    onMenuAbierto: (Boolean) -> Unit,
    onCiudad: (City?) -> Unit,
    onBack: () -> Unit
) {
    val etiqueta = ciudad?.let { listOf(it.name, it.departamento).filter { p -> p.isNotBlank() }.joinToString(", ") }
        ?: "Todas las ciudades"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButtonMapa(
            onClick = onBack,
            icono = Icons.AutoMirrored.Filled.ArrowBack,
            descripcion = "Volver",
            tint = MaterialTheme.colorScheme.onBackground
        )

        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onMenuAbierto(true) }
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Elegir ciudad",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(expanded = menuAbierto, onDismissRequest = { onMenuAbierto(false) }) {
                DropdownMenuItem(
                    text = { Text("Todas las ciudades") },
                    onClick = {
                        onCiudad(null)
                        onMenuAbierto(false)
                    }
                )
                ciudades.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name) },
                        onClick = {
                            onCiudad(item)
                            onMenuAbierto(false)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButtonMapa(
            onClick = { },
            icono = Icons.Filled.Notifications,
            descripcion = "Notificaciones",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun IconButtonMapa(
    onClick: () -> Unit,
    icono: ImageVector,
    descripcion: String,
    tint: Color
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = descripcion, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun FilaBuscador(
    consulta: String,
    onConsulta: (String) -> Unit,
    tipoSeleccionado: TipoMapa,
    menuAbierto: Boolean,
    onMenuAbierto: (Boolean) -> Unit,
    onTipo: (TipoMapa) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (consulta.isEmpty()) {
                        Text(
                            text = "Buscar restaurantes, farmacias...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = consulta,
                        onValueChange = onConsulta,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Box {
            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onMenuAbierto(true) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Filtros",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            DropdownMenu(expanded = menuAbierto, onDismissRequest = { onMenuAbierto(false) }) {
                DropdownMenuItem(
                    text = { Text("Todo") },
                    trailingIcon = { if (tipoSeleccionado == TipoMapa.TODOS) Text("•") },
                    onClick = {
                        onTipo(TipoMapa.TODOS)
                        onMenuAbierto(false)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Lugares") },
                    trailingIcon = { if (tipoSeleccionado == TipoMapa.LUGARES) Text("•") },
                    onClick = {
                        onTipo(TipoMapa.LUGARES)
                        onMenuAbierto(false)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Comercios") },
                    trailingIcon = { if (tipoSeleccionado == TipoMapa.COMERCIOS) Text("•") },
                    onClick = {
                        onTipo(TipoMapa.COMERCIOS)
                        onMenuAbierto(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun RejillaCategorias(
    categorias: List<CategoriaMapa>,
    seleccionada: String?,
    onSeleccion: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        categorias.chunked(4).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                fila.forEach { categoria ->
                    val activa = seleccionada.equals(categoria.nombre, ignoreCase = true)
                    TarjetaCategoria(
                        categoria = categoria,
                        activa = activa,
                        modifier = Modifier.weight(1f),
                        onClick = { onSeleccion(if (activa) null else categoria.nombre) }
                    )
                }
                repeat(4 - fila.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TarjetaCategoria(
    categoria: CategoriaMapa,
    activa: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    val fondo = if (activa) categoria.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .height(90.dp)
            .clip(forma)
            .background(fondo)
            .then(if (activa) Modifier.border(1.5.dp, categoria.color, forma) else Modifier)
            .clickable { onClick() }
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(categoria.color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoria.icono,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = categoria.etiqueta,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium,
            color = if (activa) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 12.sp
        )
    }
}

@Composable
private fun BotonMapa(
    icono: ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp
) {
    Surface(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable { onClick() },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icono,
                contentDescription = descripcion,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(size * 0.44f)
            )
        }
    }
}

@Composable
private fun MapaLibrePrincipal(
    pins: List<MapaPin>,
    centro: LatLng?,
    ubicacionUsuario: LatLng?,
    onPinClick: (MapaPin) -> Unit,
    onMapReady: (MapLibreMap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clickActual = rememberUpdatedState(onPinClick)
    val readyActual = rememberUpdatedState(onMapReady)
    val pinItems = remember { mutableMapOf<Long, MapaPin>() }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var estiloCargado by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { viewContext ->
            MapLibre.getInstance(viewContext)
            MapView(viewContext, MapLibreMapOptions().apply { textureMode(true) }).also { creado ->
                creado.addOnDidFinishLoadingStyleListener {
                    estiloCargado = true
                }
                creado.getMapAsync { map ->
                    mapLibreMap = map
                    readyActual.value(map)
                    map.uiSettings.apply {
                        isAttributionEnabled = true
                        isLogoEnabled = false
                        isCompassEnabled = false
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

    LaunchedEffect(mapLibreMap, estiloCargado, pins, ubicacionUsuario) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!estiloCargado) return@LaunchedEffect

        pinItems.clear()
        map.clear()

        ubicacionUsuario?.let { punto ->
            map.addMarker(
                MarkerOptions()
                    .position(punto)
                    .icon(MapaMarkerFactory.ubicacionIcon(context))
            )
        }

        pins.forEach { pin ->
            val marcador = map.addMarker(
                MarkerOptions()
                    .position(LatLng(pin.lat, pin.lng))
                    .icon(MapaMarkerFactory.puntoIcon(context, pin.color))
                    .title(pin.titulo)
            )
            pinItems[marcador.id] = pin
        }
    }

    LaunchedEffect(mapLibreMap, estiloCargado, centro, pins) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!estiloCargado) return@LaunchedEffect

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

/** Icono y color de cada categoría según su nombre. */
private fun categoriaVisual(nombre: String): Pair<ImageVector, Color> {
    val n = nombre.lowercase()
    return when {
        n.contains("restaurante") || n.contains("comida") || n.contains("pizza") ||
            n.contains("bar") -> Icons.Filled.Restaurant to Color(0xFFF0703C)

        n.contains("cafeter") || n.contains("cafe") -> Icons.Filled.LocalCafe to Color(0xFF6D4C41)

        n.contains("super") || n.contains("mercado") -> Icons.Filled.ShoppingCart to Color(0xFF16A34A)

        n.contains("farmac") || n.contains("salud") || n.contains("medic") ->
            Icons.Filled.LocalPharmacy to Color(0xFFE53935)

        n.contains("hotel") || n.contains("hosped") || n.contains("alojam") ->
            Icons.Filled.Hotel to Color(0xFF2196F3)

        n.contains("gas") || n.contains("combustible") || n.contains("transport") ->
            Icons.Filled.LocalGasStation to Color(0xFF7E57C2)

        n.contains("tienda") || n.contains("comercio") || n.contains("local") ||
            n.contains("artesan") -> Icons.Filled.ShoppingBag to Color(0xFFE91E63)

        n.contains("servicio") -> Icons.Filled.Build to Color(0xFF37474F)

        n.contains("naturaleza") || n.contains("aventura") || n.contains("parque") ->
            Icons.Filled.Park to Color(0xFF2E7D32)

        n.contains("entreten") || n.contains("diversion") || n.contains("cultura") ->
            Icons.Filled.LocalActivity to Color(0xFF8E24AA)

        else -> Icons.Filled.Storefront to Color(0xFF00897B)
    }
}

/** Etiqueta corta para las tarjetas de categoría. */
private fun etiquetaCategoria(nombre: String): String {
    val n = nombre.lowercase()
    return when {
        n.contains("restaurante") || n.contains("comida") -> "Restaurantes"
        n.contains("hosped") || n.contains("hotel") || n.contains("alojam") -> "Hoteles"
        n.contains("comercio") || n.contains("tienda") || n.contains("local") -> "Tiendas"
        n.contains("naturaleza") -> "Naturaleza"
        n.contains("entreten") -> "Entretenimiento"
        n.contains("transport") -> "Transporte"
        n.contains("servicio") -> "Servicios"
        else -> nombre
    }
}
