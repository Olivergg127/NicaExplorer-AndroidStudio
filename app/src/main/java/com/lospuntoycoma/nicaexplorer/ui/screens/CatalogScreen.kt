package com.lospuntoycoma.nicaexplorer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import com.lospuntoycoma.nicaexplorer.ui.components.NicaRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.lospuntoycoma.nicaexplorer.data.FirebaseRepository
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.data.UserPreferences
import com.lospuntoycoma.nicaexplorer.model.Afluencia
import com.lospuntoycoma.nicaexplorer.model.City
import com.lospuntoycoma.nicaexplorer.model.Comercio
import com.lospuntoycoma.nicaexplorer.model.Place
import com.lospuntoycoma.nicaexplorer.model.TipoValoracion
import com.lospuntoycoma.nicaexplorer.ui.components.CoverImage
import com.lospuntoycoma.nicaexplorer.ui.components.ValoracionRow
import com.lospuntoycoma.nicaexplorer.ui.components.ComercioCover
import com.lospuntoycoma.nicaexplorer.ui.components.NicaButton
import com.lospuntoycoma.nicaexplorer.ui.components.NicaTopBar
import com.lospuntoycoma.nicaexplorer.ui.components.TextoExpandible
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.ComerciosViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    cityId: String,
    initialPlaceId: String? = null,
    onVerEn3dClick: (String) -> Unit,
    onAssistantClick: (String) -> Unit,
    onComercioClick: (String) -> Unit,
    onVerSubcategoriasComercio: (String) -> Unit,
    onRutasInteligentesClick: () -> Unit,
    onBack: () -> Unit
) {
    val places = SampleData.placesByCity[cityId] ?: emptyList()
    val scope = rememberCoroutineScope()
    val uid = FirebaseRepository.getCurrentUser()?.uid ?: ""
    val savedIds by UserPreferences.savedPlacesFlow(uid).collectAsState(initial = emptySet())
    val comerciosViewModel: ComerciosViewModel = viewModel()
    val comerciosState by comerciosViewModel.uiState.collectAsState()
    val initialIndex = remember(initialPlaceId) {
        val idx = places.indexOfFirst { it.id == initialPlaceId }
        if (idx < 0) 0 else idx
    }
    var currentIndex by remember { mutableIntStateOf(initialIndex) }
    val scrollState = rememberScrollState()

    val city = SampleData.cities.firstOrNull { it.id == cityId }
    val cityName = city?.name ?: "Ciudad"

    val comerciosCiudad = comerciosState.comercios.filter {
        it.ciudad.trim().equals(cityId.trim(), ignoreCase = true) ||
            it.ciudad.trim().equals(cityName.trim(), ignoreCase = true)
    }

    // Categorías superiores presentes en los comercios de la ciudad, ordenadas por el catálogo.
    val categoriasPadreCiudad = remember(comerciosCiudad, comerciosState.categorias) {
        val presentes = comerciosCiudad
            .map { it.categoriaPadre.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        val ordenRaiz = comerciosState.categorias
            .filter { it.categoriaPadre.isBlank() }
            .associate { it.nombre.trim() to it.orden }
        presentes.sortedWith(compareBy({ ordenRaiz[it] ?: Int.MAX_VALUE }, { it }))
    }

    // Por defecto "Restaurantes y comida" si existe; si no, la primera categoría superior.
    var padreSeleccionado by remember(categoriasPadreCiudad) {
        mutableStateOf(
            categoriasPadreCiudad.firstOrNull { it.equals("Restaurantes y comida", ignoreCase = true) }
                ?: categoriasPadreCiudad.firstOrNull()
        )
    }

    val comerciosPadre = remember(comerciosCiudad, padreSeleccionado) {
        padreSeleccionado?.let { padre ->
            comerciosCiudad.filter { it.categoriaPadre.trim().equals(padre, ignoreCase = true) }
        } ?: emptyList()
    }

    if (places.isEmpty()) {
        Scaffold(
            topBar = {
                NicaTopBar(
                    title = cityName,
                    onBack = onBack,
                    showTitleText = false,
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Search, contentDescription = "Buscar")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay lugars disponibles",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    val place = places[currentIndex]
    val quieterAlternative = if (place.afluencia == Afluencia.ALTA) {
        places
            .asSequence()
            .filter { candidate ->
                candidate.id != place.id &&
                    candidate.cityId == place.cityId &&
                    candidate.afluencia != Afluencia.ALTA
            }
            .minByOrNull { it.afluencia.quietnessPriority }
    } else {
        null
    }

    val isSaved = place.id in savedIds

    LaunchedEffect(place.id) {
        // Cambiar de lugar (flechas del carrusel) no debe saltar al tope.
        UserPreferences.recordExploration(uid, place.id)
    }

    Scaffold(
        topBar = {
            NicaTopBar(
                title = cityName,
                onBack = onBack,
                showTitleText = false,
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                UserPreferences.toggleSavedPlace(uid, place.id)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark
                            else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isSaved) "Quitar de guardados"
                            else "Guardar lugar",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    }
                }
            )
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Estructura de la vista de ciudad: portada + carrusel + información.
            if (city != null) {
                CiudadHero(city = city)
                CiudadInfoCard(city = city)

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Valora esta ciudad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ValoracionRow(
                        tipo = TipoValoracion.CIUDAD,
                        refId = city.id,
                        cityId = city.id
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(place.gradientStart),
                                Color(place.gradientEnd)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val hasImage = CoverImage(
                    url = place.imagenUrl,
                    imageRes = place.imageRes,
                    contentDescription = place.name,
                    modifier = Modifier.fillMaxSize()
                )
                if (hasImage) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.18f))
                    )
                } else {
                    place.icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            currentIndex = if (currentIndex > 0) currentIndex - 1 else places.size - 1
                        },
                        enabled = places.size > 1
                    ) {
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = "Anterior",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = {
                            currentIndex = if (currentIndex < places.size - 1) currentIndex + 1 else 0
                        },
                        enabled = places.size > 1
                    ) {
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = "Siguiente",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${currentIndex + 1} de ${places.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = place.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                ValoracionRow(
                    tipo = TipoValoracion.LUGAR,
                    refId = place.id,
                    cityId = place.cityId
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = place.city,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = place.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextoExpandible(
                    text = place.description,
                    maxLines = 4,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                AfluenciaCard(
                    afluencia = place.afluencia,
                    quieterAlternative = quieterAlternative,
                    onAlternativeClick = { alternative ->
                        val alternativeIndex = places.indexOfFirst { it.id == alternative.id }
                        if (alternativeIndex >= 0) {
                            currentIndex = alternativeIndex
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // El botón 3D solo aparece si el lugar tiene modelo asignado.
                if (place.modeloUnity.isNotBlank()) {
                    NicaButton(
                        text = "Ver en 3D",
                        onClick = { onVerEn3dClick(place.id) },
                        gradient = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                NicaButton(
                    text = "Hablar con el asistente IA",
                    onClick = { onAssistantClick(place.id) },
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )

                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    text = "Información adicional",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow(
                            icon = Icons.Filled.CalendarMonth,
                            label = "Año de construcción",
                            value = place.yearBuilt
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(
                            icon = Icons.Filled.Info,
                            label = "Historia",
                            value = place.history,
                            expandible = true
                        )
                    }
                }

                if (place.consejosResponsables.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    var consejosExpandidos by remember(place.id) { mutableStateOf(false) }

                    Card(
                        onClick = { consejosExpandidos = !consejosExpandidos },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Eco,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Turismo responsable",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Contribuye a preservar este lugar",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                TextButton(onClick = { consejosExpandidos = !consejosExpandidos }) {
                                    Text(
                                        text = if (consejosExpandidos) "Ocultar consejos" else "Ver consejos",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }

                            AnimatedVisibility(visible = consejosExpandidos) {
                                Column(modifier = Modifier.padding(top = 4.dp)) {
                                    place.consejosResponsables.forEach { consejo ->
                                        Row(
                                            modifier = Modifier.padding(bottom = 8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = consejo,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                RutasInteligentesAccessCard(
                    cityId = cityId,
                    onClick = onRutasInteligentesClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    text = "Comercios recomendados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (categoriasPadreCiudad.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Nube de categorías superiores.
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoriasPadreCiudad.forEach { padre ->
                            FilterChip(
                                selected = padre == padreSeleccionado,
                                onClick = { padreSeleccionado = padre },
                                label = { Text(padre) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val comerciosVisibles = remember(comerciosPadre) { comerciosPadre.take(6) }

                    if (comerciosVisibles.isEmpty()) {
                        Text(
                            text = "No hay comercios de esta categoría en la ciudad.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = comerciosVisibles,
                                key = { it.id }
                            ) { comercio ->
                                ComercioMiniCard(
                                    comercio = comercio,
                                    onClick = { onComercioClick(comercio.id) }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { padreSeleccionado?.let(onVerSubcategoriasComercio) },
                        enabled = padreSeleccionado != null
                    ) {
                        Text(text = "Ver todos")
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
            }
        }
    }
}

/**
 * Cabecera de la ciudad: portada/carrusel de imágenes (galeria) con el nombre y el lema.
 * Si la ciudad no tiene imágenes remotas, usa el drawable local o el degradado.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CiudadHero(city: City) {
    val pages = remember(city.id, city.galeria, city.imagenUrl) {
        city.galeria.ifEmpty { listOfNotNull(city.imagenUrl) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        if (pages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(city.gradientStart), Color(city.gradientEnd))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = city.icon ?: Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(72.dp)
                )
            }
        } else {
            val pagerState = rememberPagerState { pages.size }
            val localRes = city.imageRes

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = pages[page],
                    contentDescription = city.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = localRes?.let { painterResource(it) },
                    fallback = localRes?.let { painterResource(it) }
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 10.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = city.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            if (city.lema.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = city.lema,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Información de la ciudad: departamento, descripción e historia (expandible).
 */
@Composable
private fun CiudadInfoCard(city: City) {
    if (city.description.isBlank() && city.historia.isBlank() && city.departamento.isBlank()) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        if (city.departamento.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = city.departamento,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (city.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            TextoExpandible(
                text = city.description,
                maxLines = 4,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
            )
        }

        if (city.historia.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            TextoExpandible(
                text = city.historia,
                maxLines = 4,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RutasInteligentesAccessCard(
    cityId: String,
    onClick: () -> Unit
) {
    val ruta = remember(cityId) {
        SampleData.rutasDeCiudad(cityId).firstOrNull()
    }
    val containerColor = if (ruta != null) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Eco,
                contentDescription = null,
                tint = if (ruta != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (ruta != null) "Rutas Creativas" else "Rutas próximamente",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ruta?.let {
                        "${it.nombre} · ${it.numeroParadas} paradas · ${it.duracionEstimada}"
                    } ?: "Estamos preparando recorridos con lugares y comercios locales de esta ciudad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = if (ruta != null) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (ruta != null) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Abrir rutas creativas",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (ruta != null) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            content = { content() }
        )
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            content = { content() }
        )
    }
}

@Composable
private fun AfluenciaCard(
    afluencia: Afluencia,
    quieterAlternative: Place?,
    onAlternativeClick: (Place) -> Unit
) {
    val levelColor = when (afluencia) {
        Afluencia.BAJA -> Color(0xFF2E7D32)
        Afluencia.MODERADA -> Color(0xFFF9A825)
        Afluencia.ALTA -> Color(0xFFC62828)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Afluencia estimada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = afluencia.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = levelColor,
                    modifier = Modifier
                        .background(
                            color = levelColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(3) { segment ->
                    val isActive = segment <= afluencia.quietnessPriority
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) levelColor
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Nivel orientativo para ayudar a distribuir las visitas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
            )

            if (afluencia == Afluencia.ALTA && quieterAlternative != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¿Buscas un lugar más tranquilo?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { onAlternativeClick(quieterAlternative) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = quieterAlternative.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Afluencia estimada ${quieterAlternative.afluencia.displayName.lowercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Abrir ${quieterAlternative.name}",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    expandible: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (expandible) {
                TextoExpandible(
                    text = value,
                    maxLines = 4,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

/**
 * Tarjeta compacta del carrusel "Descubre negocios locales".
 * Muestra solo imagen, nombre y categoría. Al tocarla abre el detalle.
 */
@Composable
fun ComercioMiniCard(
    comercio: Comercio,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            ComercioCover(
                comercio = comercio,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = comercio.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = comercio.categoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
