package com.lospuntoycoma.nicaexplorer.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lospuntoycoma.nicaexplorer.R
import com.lospuntoycoma.nicaexplorer.map.MapaConfig
import com.lospuntoycoma.nicaexplorer.map.MapaMarkerFactory
import com.lospuntoycoma.nicaexplorer.model.Comercio
import com.lospuntoycoma.nicaexplorer.ui.theme.nicaAppBackgroundBrush
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.MapaViewModel
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val MAPLIBRE_TAG = "NicaExplorerMap"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    onBack: () -> Unit,
    onVerComercio: (String) -> Unit,
    viewModel: MapaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var comercioSeleccionado by remember { mutableStateOf<Comercio?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de NicaExplorer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MapaMapView(
                comercios = uiState.comercios,
                onComercioSelected = { comercioSeleccionado = it },
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = MapaConfig.ATTRIBUTION,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (uiState.isLoading) {
                Surface(
                    modifier = Modifier.align(Alignment.Center),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    shadowElevation = 6.dp
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(22.dp))
                }
            }

            uiState.error?.let { error ->
                AlertDialog(
                    onDismissRequest = onBack,
                    title = { Text("Mapa no disponible") },
                    text = { Text(error) },
                    confirmButton = { TextButton(onClick = onBack) { Text("Cerrar") } }
                )
            }

            if (!uiState.isLoading && uiState.error == null && uiState.comercios.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "No hay comercios con coordenadas para mostrar.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    comercioSeleccionado?.let { comercio ->
        ModalBottomSheet(
            onDismissRequest = { comercioSeleccionado = null },
            modifier = Modifier.background(nicaAppBackgroundBrush())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = comercio.nombre.ifBlank { "Comercio local" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = listOf(comercio.categoria, comercio.ciudad)
                        .filter(String::isNotBlank)
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                comercio.descripcion.takeIf { it.isNotBlank() }?.let { descripcion ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = {
                        comercioSeleccionado = null
                        onVerComercio(comercio.id)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver detalles")
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun MapaMapView(
    comercios: List<Comercio>,
    onComercioSelected: (Comercio) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val markerComercios = remember { mutableMapOf<Long, Comercio>() }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleLoaded by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            // Mantener la superficie de MapLibre encima del contenido Compose
            // evita que el lienzo del mapa quede oculto por la ventana.
            val mapOptions = MapLibreMapOptions().apply {
                renderSurfaceOnTop(true)
            }
            MapView(viewContext, mapOptions).also { createdMapView ->
                Log.e(
                    MAPLIBRE_TAG,
                    "MapView creado: render=${createdMapView.getRenderView()::class.java.name}"
                )
                mapView = createdMapView
                createdMapView.addOnWillStartRenderingMapListener {
                    Log.e(MAPLIBRE_TAG, "MapLibre empezó a renderizar")
                }
                createdMapView.addOnDidFinishRenderingMapListener {
                    Log.e(MAPLIBRE_TAG, "MapLibre terminó de renderizar")
                }
                createdMapView.addOnDidFinishLoadingStyleListener {
                    Log.d(MAPLIBRE_TAG, "Estilo MapLibre cargado con teselas OSM")
                }
                createdMapView.addOnDidFinishLoadingMapListener {
                    Log.d(MAPLIBRE_TAG, "Mapa MapLibre terminó de cargar")
                }
                createdMapView.addOnDidFailLoadingMapListener { errorMessage ->
                    Log.e(MAPLIBRE_TAG, "MapLibre no pudo cargar el mapa: $errorMessage")
                }
            }
        },
        update = { }
    )

    DisposableEffect(lifecycleOwner, mapView) {
        val view = mapView ?: return@DisposableEffect onDispose { }
        var isStarted = false
        var isResumed = false
        var isDestroyed = false

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (!isStarted) {
                        view.onStart()
                        isStarted = true
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!isResumed) {
                        view.onResume()
                        isResumed = true
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    if (isResumed) {
                        view.onPause()
                        isResumed = false
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    if (isStarted) {
                        view.onStop()
                        isStarted = false
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    if (isResumed) view.onPause()
                    if (isStarted) view.onStop()
                    view.onDestroy()
                    isResumed = false
                    isStarted = false
                    isDestroyed = true
                }
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        view.onCreate(null)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            view.onStart()
            isStarted = true
            Log.e(MAPLIBRE_TAG, "MapView onStart ejecutado")
        }
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) || isStarted) {
            view.onResume()
            isResumed = true
            Log.e(MAPLIBRE_TAG, "MapView onResume ejecutado")
        }

        view.post {
            Log.e(
                MAPLIBRE_TAG,
                "Solicitando getMapAsync: size=${view.width}x${view.height}, attached=${view.isAttachedToWindow}"
            )
            view.getMapAsync { map ->
                Log.e(MAPLIBRE_TAG, "MapLibreMap listo; aplicando estilo local")
                mapLibreMap = map
                map.uiSettings.apply {
                    isLogoEnabled = true
                    isAttributionEnabled = true
                    isCompassEnabled = true
                    isRotateGesturesEnabled = false
                }
                map.setOnMarkerClickListener { marker ->
                    markerComercios[marker.id]?.let(onComercioSelected)
                    true
                }
                map.setStyle(Style.Builder().fromJson(MapaConfig.STYLE_JSON)) {
                    Log.e(MAPLIBRE_TAG, "Callback setStyle recibido")
                    styleLoaded = true
                    map.cameraPosition = CameraPosition.Builder()
                        .target(MapaConfig.NICARAGUA)
                        .zoom(MapaConfig.INITIAL_ZOOM)
                        .build()
                }
            }
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (!isDestroyed) {
                if (isResumed) view.onPause()
                if (isStarted) view.onStop()
                view.onDestroy()
            }
        }
    }

    LaunchedEffect(mapLibreMap, styleLoaded, comercios) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!styleLoaded) return@LaunchedEffect

        markerComercios.clear()
        map.clear()
        comercios.forEach { comercio ->
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(comercio.latitud, comercio.longitud))
                    .icon(MapaMarkerFactory.comercioIcon(context, comercio.categoria))
                    .title(comercio.nombre)
            )
            markerComercios[marker.id] = comercio
        }
    }
}
