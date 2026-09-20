package com.lospuntoycoma.nicaexplorer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.ui.components.ComercioCard
import com.lospuntoycoma.nicaexplorer.ui.components.EmptyState
import com.lospuntoycoma.nicaexplorer.ui.components.NicaTopBar
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.ComerciosViewModel

/**
 * Subcategorías de una categoría superior de comercios (p. ej. "Restaurantes y comida"),
 * para la ciudad actual. Al tocar una se abren todos sus comercios.
 */
@Composable
fun ComerciosSubcategoriasScreen(
    cityId: String,
    padre: String,
    onSubcategoriaClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: ComerciosViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val cityName = SampleData.cities.firstOrNull { it.id == cityId }?.name ?: cityId

    val subcategorias = remember(uiState.comercios, padre, cityId, cityName) {
        uiState.comercios
            .filter { comercio ->
                (comercio.ciudad.trim().equals(cityId, ignoreCase = true) ||
                    comercio.ciudad.trim().equals(cityName, ignoreCase = true)) &&
                    comercio.categoriaPadre.trim().equals(padre, ignoreCase = true) &&
                    comercio.categoria.isNotBlank()
            }
            .groupingBy { it.categoria.trim() }
            .eachCount()
            .toList()
            .sortedBy { it.first.lowercase() }
    }

    Scaffold(
        topBar = { NicaTopBar(title = padre, onBack = onBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                subcategorias.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.Storefront,
                        message = "Aún no hay comercios de \"$padre\" en esta ciudad.",
                        buttonText = "Reintentar",
                        onButtonClick = { viewModel.loadComercios() }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(subcategorias, key = { it.first }) { (subcategoria, total) ->
                            SubcategoriaCard(
                                nombre = subcategoria,
                                total = total,
                                onClick = { onSubcategoriaClick(subcategoria) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubcategoriaCard(
    nombre: String,
    total: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Storefront,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (total == 1) "1 comercio" else "$total comercios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Todos los comercios de una subcategoría en la ciudad.
 */
@Composable
fun ComerciosSubcategoriaScreen(
    cityId: String,
    subcategoria: String,
    onComercioClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: ComerciosViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val cityName = SampleData.cities.firstOrNull { it.id == cityId }?.name ?: cityId

    val comercios = remember(uiState.comercios, subcategoria, cityId, cityName) {
        uiState.comercios.filter { comercio ->
            (comercio.ciudad.trim().equals(cityId, ignoreCase = true) ||
                comercio.ciudad.trim().equals(cityName, ignoreCase = true)) &&
                comercio.categoria.trim().equals(subcategoria, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = { NicaTopBar(title = subcategoria, onBack = onBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                comercios.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.Storefront,
                        message = "Aún no hay comercios de \"$subcategoria\" en esta ciudad.",
                        buttonText = "Reintentar",
                        onButtonClick = { viewModel.loadComercios() }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(comercios, key = { it.id }) { comercio ->
                            ComercioCard(
                                comercio = comercio,
                                onClick = { onComercioClick(comercio.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
