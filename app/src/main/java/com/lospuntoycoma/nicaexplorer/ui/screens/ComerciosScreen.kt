package com.lospuntoycoma.nicaexplorer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.ui.components.ComercioCard
import com.lospuntoycoma.nicaexplorer.ui.components.EmptyState
import com.lospuntoycoma.nicaexplorer.ui.components.NicaButton
import com.lospuntoycoma.nicaexplorer.ui.components.NicaTopBar
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.ComerciosViewModel

/**
 * Pantalla que muestra la lista de comercios y restaurantes activos
 * obtenidos desde Firestore (colección "comercios", activo == true).
 * Si cityFilter no es null, muestra solo los comercios de esa ciudad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComerciosScreen(
    cityFilter: String? = null,
    onComercioClick: (String) -> Unit,
    onSolicitarAparicion: (String) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: ComerciosViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val selectedCity = cityFilter?.let { filtro ->
        SampleData.cities.firstOrNull { it.id.equals(filtro, ignoreCase = true) }
    }
    val comerciosVisibles = selectedCity?.let { city ->
        uiState.comercios.filter { comercio ->
            comercio.ciudad.trim().equals(city.id, ignoreCase = true) ||
                comercio.ciudad.trim().equals(city.name, ignoreCase = true)
        }
    } ?: cityFilter?.let { filtro ->
        uiState.comercios.filter {
            it.ciudad.trim().equals(filtro.trim(), ignoreCase = true)
        }
    } ?: uiState.comercios

    Scaffold(
        topBar = {
            NicaTopBar(
                title = "Comercios y restaurantes",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            selectedCity?.let { city ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "¿Tienes un negocio en ${city.name}?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NicaButton(
                            text = "Solicita aparecer en NicaExplorer",
                            onClick = { onSolicitarAparicion(city.id) }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.error != null -> {
                        val errorMessage = uiState.error ?: ""
                        Box(modifier = Modifier.fillMaxSize()) {
                            EmptyState(
                                icon = Icons.Filled.Refresh,
                                message = errorMessage,
                                buttonText = "Reintentar",
                                onButtonClick = { viewModel.loadComercios() }
                            )
                        }
                    }

                    comerciosVisibles.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            EmptyState(
                                icon = Icons.Filled.Storefront,
                                message = if (cityFilter != null) {
                                    "Aún no hay comercios disponibles en esta ciudad."
                                } else {
                                    "Aún no hay comercios disponibles."
                                },
                                buttonText = "Reintentar",
                                onButtonClick = { viewModel.loadComercios() }
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                top = 0.dp,
                                end = 20.dp,
                                bottom = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = comerciosVisibles,
                                key = { it.id }
                            ) { comercio ->
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
}
