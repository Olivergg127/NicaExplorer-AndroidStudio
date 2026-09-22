package com.lospuntoycoma.nicaexplorer.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import com.lospuntoycoma.nicaexplorer.ui.components.NicaRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lospuntoycoma.nicaexplorer.R
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.data.UbicacionHelper
import com.lospuntoycoma.nicaexplorer.data.ValoracionesRepository
import com.lospuntoycoma.nicaexplorer.model.City
import com.lospuntoycoma.nicaexplorer.model.Comercio
import com.lospuntoycoma.nicaexplorer.model.Place
import com.lospuntoycoma.nicaexplorer.model.Valoracion
import com.lospuntoycoma.nicaexplorer.ui.components.CoverImage
import com.lospuntoycoma.nicaexplorer.ui.components.PlaceCard
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.ComerciosViewModel
import com.lospuntoycoma.nicaexplorer.ui.theme.nicaAppBackgroundBrush
import com.lospuntoycoma.nicaexplorer.ui.theme.nicaBottomNavBarBrush
import com.lospuntoycoma.nicaexplorer.ui.theme.GradientEnd
import com.lospuntoycoma.nicaexplorer.ui.theme.GradientStart
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    isLoggedIn: Boolean,
    onCityClick: () -> Unit,
    onCityCardClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onAssistantClick: () -> Unit,
    onMapClick: () -> Unit,
    onAdminPanelClick: () -> Unit,
    onSavedPlacesClick: () -> Unit,
    onMisComerciosClick: () -> Unit,
    onLoginClick: () -> Unit,
    onPlaceClick: (cityId: String, placeId: String) -> Unit,
    onComercioClick: (comercioId: String) -> Unit,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedNavItem by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    val userProfile by userViewModel.userProfile.collectAsState()
    val canAccessAdminPanel = userProfile?.rol == com.lospuntoycoma.nicaexplorer.model.UserRole.ADMIN ||
        userProfile?.rol == com.lospuntoycoma.nicaexplorer.model.UserRole.AUDITOR
    val canManageComercios = userProfile?.rol == com.lospuntoycoma.nicaexplorer.model.UserRole.COMERCIO ||
        userProfile?.rol == com.lospuntoycoma.nicaexplorer.model.UserRole.ADMIN

    // Recomendaciones según la ubicación actual (GPS) y valoraciones.
    val context = LocalContext.current
    val comerciosViewModel: ComerciosViewModel = viewModel()
    val comerciosState by comerciosViewModel.uiState.collectAsState()

    var ciudadActual by remember { mutableStateOf<City?>(null) }
    val valoraciones by ValoracionesRepository.publicas.collectAsState()
    var permisoUbicacion by remember { mutableStateOf(UbicacionHelper.tienePermiso(context)) }

    LaunchedEffect(Unit) {
        if (ValoracionesRepository.publicas.value.isEmpty()) {
            ValoracionesRepository.refreshPublicas()
        }
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        permisoUbicacion = resultado.values.any { it }
    }

    LaunchedEffect(Unit) {
        if (!permisoUbicacion) {
            permisoLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(permisoUbicacion, SampleData.cities) {
        if (!permisoUbicacion) return@LaunchedEffect

        val ubicacion = UbicacionHelper.ultimaUbicacion(context)
        if (ubicacion != null) {
            val ciudad = UbicacionHelper.ciudadMasCercana(ubicacion.latitude, ubicacion.longitude, SampleData.cities)
            if (ciudad != null) {
                ciudadActual = ciudad
            }
        }
    }

    val promedios = remember(valoraciones) {
        valoraciones.groupBy { "${it.tipo}|${it.refId}" }
            .mapValues { (_, lista) -> lista.map { it.estrellas }.average() }
    }
    val promedioDe: (String, String) -> Double = { tipo, refId -> promedios["$tipo|$refId"] ?: 0.0 }

    val lugaresRecomendados = remember(ciudadActual, valoraciones) {
        val ciudad = ciudadActual
        val lugares = if (ciudad != null) {
            SampleData.placesByCity[ciudad.id].orEmpty()
        } else {
            SampleData.recommendedPlaces
        }
        lugares
            .sortedWith(compareByDescending<Place> { promedioDe("lugar", it.id) }.thenBy { it.name })
            .take(5)
    }

    val comerciosRecomendados = remember(ciudadActual, comerciosState.comercios, valoraciones) {
        val ciudad = ciudadActual ?: return@remember emptyList()
        comerciosState.comercios
            .filter {
                it.ciudad.trim().equals(ciudad.id, ignoreCase = true) ||
                    it.ciudad.trim().equals(ciudad.name, ignoreCase = true)
            }
            .sortedWith(compareByDescending<Comercio> { promedioDe("comercio", it.id) }.thenBy { it.nombre })
            .take(10)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.nicaexplorer_isotipo),
                            contentDescription = "Logo de NicaExplorer",
                            modifier = Modifier.size(68.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "NicaExplorer",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Explore, contentDescription = null) },
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onProfileClick()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                    label = { Text("Lugares guardados") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSavedPlacesClick()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                if (canManageComercios) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
                        label = { Text("Mis comercios") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onMisComerciosClick()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Assistant, contentDescription = null) },
                    label = { Text("Asistente IA") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onAssistantClick()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Map, contentDescription = null) },
                    label = { Text("Mapa") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onMapClick()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                if (canAccessAdminPanel) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.AdminPanelSettings, contentDescription = null) },
                        label = { Text("Panel de Admin") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAdminPanelClick()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text(if (isLoggedIn) "Cerrar sesión" else "Iniciar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (isLoggedIn) onLogout() else onLoginClick()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(nicaAppBackgroundBrush())
        ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = com.lospuntoycoma.nicaexplorer.R.drawable.nicaexplorer_isotipo),
                                contentDescription = "NicaExplorer",
                                tint = androidx.compose.ui.graphics.Color.Unspecified,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "NicaExplorer",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
                        }
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Filled.Person, contentDescription = "Perfil")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.background(nicaBottomNavBarBrush()),
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Explore, contentDescription = null) },
                        label = { Text("Inicio") },
                        selected = selectedNavItem == 0,
                        onClick = { selectedNavItem = 0 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Explore, contentDescription = null) },
                        label = { Text("Explorar") },
                        selected = selectedNavItem == 1,
                        onClick = {
                            selectedNavItem = 1
                            onCityClick()
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Assistant, contentDescription = null) },
                        label = { Text("Asistente IA") },
                        selected = selectedNavItem == 2,
                        onClick = {
                            selectedNavItem = 2
                            onAssistantClick()
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        label = { Text("Perfil") },
                        selected = selectedNavItem == 3,
                        onClick = {
                            selectedNavItem = 3
                            onProfileClick()
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { padding ->
            NicaRefreshBox(
                isRefreshing = SampleData.isRefreshing,
                onRefresh = { scope.launch { SampleData.refresh() } },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isLoggedIn && !userProfile?.nombre.isNullOrBlank()) {
                        "Hola, ${userProfile?.nombre}"
                    } else {
                        "Hola, explorador"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "¿Qué deseas descubrir hoy?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        Image(
                            painter = painterResource(
                                id = com.lospuntoycoma.nicaexplorer.R.drawable.fondotarjeta
                            ),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.22f),
                                            Color.Black.copy(alpha = 0.72f)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Descubre Nicaragua con NicaExplorer",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Explora ciudades, conoce su historia y cultura, descubre lugars en 3D y encuentra experiencias y negocios locales en un solo lugar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explora por ciudad",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Ver todas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onCityClick() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    items(SampleData.cities) { city ->
                        Card(
                            modifier = Modifier
                                .width(140.dp)
                                .height(100.dp)
                                .clickable { onCityCardClick(city.id) },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(city.gradientStart),
                                                Color(city.gradientEnd)
                                            )
                                        )
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                val hasImage = CoverImage(
                                    url = city.imagenUrl,
                                    imageRes = city.imageRes,
                                    contentDescription = city.name,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (hasImage) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.08f),
                                                        Color.Black.copy(alpha = 0.58f)
                                                    )
                                                )
                                            )
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (!hasImage) {
                                        Icon(
                                            imageVector = Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    Text(
                                        text = city.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                val sufijoCiudad = ciudadActual?.let { " en ${it.name}" } ?: ""

                Text(
                    text = "Lugares recomendados$sufijoCiudad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                ) {
                    items(lugaresRecomendados) { place ->
                        PlaceCard(
                            place = place,
                            onClick = { onPlaceClick(place.cityId, place.id) }
                        )
                    }
                }

                if (comerciosRecomendados.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Comercios recomendados$sufijoCiudad",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                    ) {
                        items(comerciosRecomendados, key = { it.id }) { comercio ->
                            ComercioMiniCard(
                                comercio = comercio,
                                onClick = { onComercioClick(comercio.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
            }
        }
        }
    }
}
