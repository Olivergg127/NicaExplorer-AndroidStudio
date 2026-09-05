package com.lospuntoycoma.nicaexplorer.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.lospuntoycoma.nicaexplorer.data.RutasTuristicasData
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.model.Afluencia
import com.lospuntoycoma.nicaexplorer.model.Comercio
import com.lospuntoycoma.nicaexplorer.model.Monument
import com.lospuntoycoma.nicaexplorer.model.ReferenciaParadaRuta
import com.lospuntoycoma.nicaexplorer.model.RutaTuristica
import com.lospuntoycoma.nicaexplorer.ui.components.ComercioCover
import com.lospuntoycoma.nicaexplorer.ui.components.NicaButton
import com.lospuntoycoma.nicaexplorer.ui.components.NicaTopBar
import com.lospuntoycoma.nicaexplorer.ui.theme.nicaAppBackgroundBrush
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.ComerciosUiState
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.ComerciosViewModel

/**
 * Presenta la ruta predefinida de una ciudad sin GPS, mapa ni navegación.
 * Las referencias de cada parada se resuelven desde las fuentes existentes.
 */
@Composable
fun RutasInteligentesScreen(
    cityId: String,
    onVerLugar: (String) -> Unit,
    onVerComercio: (String) -> Unit,
    onBack: () -> Unit
) {
    val ruta = remember(cityId) { RutasTuristicasData.rutaParaCiudad(cityId) }

    Scaffold(
        topBar = {
            NicaTopBar(
                title = "Rutas inteligentes",
                onBack = onBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(nicaAppBackgroundBrush())
        ) {
            if (ruta == null) {
                RutasProximamente()
            } else {
                RutaDisponible(
                    ruta = ruta,
                    onVerLugar = onVerLugar,
                    onVerComercio = onVerComercio
                )
            }
        }
    }
}

@Composable
private fun RutasProximamente() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Rutas próximamente",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Estamos preparando recorridos con lugares y comercios locales de esta ciudad.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RutaDisponible(
    ruta: RutaTuristica,
    onVerLugar: (String) -> Unit,
    onVerComercio: (String) -> Unit
) {
    // Se crea solo para ciudades con una ruta publicada; León y Managua no consultan Firestore.
    val comerciosViewModel: ComerciosViewModel = viewModel()
    val comerciosState by comerciosViewModel.uiState.collectAsState()
    val monumentosPorId = remember {
        SampleData.allMonuments.associateBy { monumento -> monumento.id }
    }
    val nombreCiudad = remember(ruta.cityId) {
        SampleData.cities
            .firstOrNull { ciudad -> ciudad.id.equals(ruta.cityId, ignoreCase = true) }
            ?.name
            .orEmpty()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            EncabezadoRuta(ruta = ruta)
        }

        item {
            InteligenciaRuta(objetivos = ruta.objetivos)
        }

        item {
            Text(
                text = "Paradas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        itemsIndexed(
            items = ruta.paradas,
            key = { index, referencia -> "${referencia.referenciaId}-$index" }
        ) { index, referencia ->
            val numeroParada = index + 1

            when (referencia) {
                is ReferenciaParadaRuta.Monumento -> {
                    val monumento = monumentosPorId[referencia.monumentId]
                        ?.takeIf { candidate ->
                            candidate.cityId.equals(ruta.cityId, ignoreCase = true)
                        }

                    if (monumento != null) {
                        ParadaMonumento(
                            numero = numeroParada,
                            monumento = monumento,
                            onVerLugar = { onVerLugar(monumento.id) }
                        )
                    } else {
                        ParadaLugarNoDisponible(numero = numeroParada)
                    }
                }

                is ReferenciaParadaRuta.ComercioLocal -> {
                    ParadaComercioDesdeFirestore(
                        numero = numeroParada,
                        comercioId = referencia.comercioId,
                        cityId = ruta.cityId,
                        nombreCiudad = nombreCiudad,
                        uiState = comerciosState,
                        onVerComercio = onVerComercio,
                        onReintentar = comerciosViewModel::loadComercios
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun EncabezadoRuta(ruta: RutaTuristica) {
    Column {
        Text(
            text = ruta.nombre,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = ruta.descripcion,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.76f)
        )
        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DatoRuta(
                icono = Icons.Filled.LocationOn,
                valor = ruta.numeroParadas.toString(),
                etiqueta = "paradas",
                modifier = Modifier.weight(1f)
            )
            DatoRuta(
                icono = Icons.Filled.AccessTime,
                valor = ruta.duracionEstimada,
                etiqueta = "duración estimada",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = ruta.notaDuracion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
            )
        }
    }
}

@Composable
private fun DatoRuta(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(9.dp))
            Column {
                Text(
                    text = valor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InteligenciaRuta(objetivos: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Eco,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Cómo funciona esta ruta inteligente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            objetivos.forEachIndexed { index, objetivo ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = objetivo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                    )
                }
                if (index < objetivos.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ParadaMonumento(
    numero: Int,
    monumento: Monument,
    onVerLugar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(monumento.gradientStart),
                                Color(monumento.gradientEnd)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val imageRes = monumento.imageRes
                if (imageRes != null) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = monumento.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.12f))
                    )
                } else {
                    Icon(
                        imageVector = monumento.icon ?: Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.55f),
                        modifier = Modifier.size(58.dp)
                    )
                }
                NumeroParada(numero = numero, modifier = Modifier.align(Alignment.TopStart))
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    TipoParada(texto = "Monumento")
                    Spacer(modifier = Modifier.height(8.dp))
                    AfluenciaEstimada(afluencia = monumento.afluencia)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = monumento.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = monumento.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
                Spacer(modifier = Modifier.height(14.dp))
                NicaButton(
                    text = "Ver lugar",
                    onClick = onVerLugar
                )
            }
        }
    }
}

@Composable
private fun ParadaComercioDesdeFirestore(
    numero: Int,
    comercioId: String,
    cityId: String,
    nombreCiudad: String,
    uiState: ComerciosUiState,
    onVerComercio: (String) -> Unit,
    onReintentar: () -> Unit
) {
    when {
        uiState.isLoading -> {
            ParadaComercioCargando(numero = numero)
        }

        uiState.error != null -> {
            ParadaComercioNoDisponible(
                numero = numero,
                detalle = uiState.error,
                onReintentar = onReintentar
            )
        }

        else -> {
            val comercio = uiState.comercios.firstOrNull { candidate ->
                candidate.id == comercioId &&
                    candidate.activo &&
                    candidate.perteneceA(cityId = cityId, nombreCiudad = nombreCiudad)
            }

            if (comercio != null) {
                ParadaComercio(
                    numero = numero,
                    comercio = comercio,
                    onVerComercio = { onVerComercio(comercio.id) }
                )
            } else {
                ParadaComercioNoDisponible(
                    numero = numero,
                    detalle = "El comercio previsto para esta ruta no está disponible actualmente."
                )
            }
        }
    }
}

private fun Comercio.perteneceA(cityId: String, nombreCiudad: String): Boolean {
    val ciudadNormalizada = ciudad.trim()
    return ciudadNormalizada.equals(cityId.trim(), ignoreCase = true) ||
        nombreCiudad.isNotBlank() &&
        ciudadNormalizada.equals(nombreCiudad.trim(), ignoreCase = true)
}

@Composable
private fun ParadaComercio(
    numero: Int,
    comercio: Comercio,
    onVerComercio: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                ComercioCover(
                    comercio = comercio,
                    modifier = Modifier.fillMaxSize()
                )
                NumeroParada(numero = numero, modifier = Modifier.align(Alignment.TopStart))
            }

            Column(modifier = Modifier.padding(16.dp)) {
                TipoParada(texto = "Comercio")
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = comercio.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (comercio.categoria.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = comercio.categoria,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                NicaButton(
                    text = "Ver comercio",
                    onClick = onVerComercio
                )
            }
        }
    }
}

@Composable
private fun ParadaComercioCargando(numero: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(34.dp))
                NumeroParada(numero = numero, modifier = Modifier.align(Alignment.TopStart))
            }
            Column(modifier = Modifier.padding(16.dp)) {
                TipoParada(texto = "Comercio")
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Cargando comercio…",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Consultando la información del comercio local.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
            }
        }
    }
}

@Composable
private fun ParadaComercioNoDisponible(
    numero: Int,
    detalle: String,
    onReintentar: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier.size(52.dp)
                )
                NumeroParada(numero = numero, modifier = Modifier.align(Alignment.TopStart))
            }
            Column(modifier = Modifier.padding(16.dp)) {
                TipoParada(texto = "Comercio")
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Comercio no disponible",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
                if (onReintentar != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onReintentar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Reintentar")
                    }
                }
            }
        }
    }
}

@Composable
private fun ParadaLugarNoDisponible(numero: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NumeroParada(numero = numero, compacto = true)
                Spacer(modifier = Modifier.width(10.dp))
                TipoParada(texto = "Monumento")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Lugar no disponible",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NumeroParada(
    numero: Int,
    modifier: Modifier = Modifier,
    compacto: Boolean = false
) {
    Surface(
        modifier = modifier.padding(if (compacto) 0.dp else 12.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier = Modifier.size(if (compacto) 32.dp else 38.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = numero.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun TipoParada(texto: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun AfluenciaEstimada(afluencia: Afluencia) {
    val color = when (afluencia) {
        Afluencia.BAJA -> Color(0xFF2E7D32)
        Afluencia.MODERADA -> Color(0xFFF9A825)
        Afluencia.ALTA -> Color(0xFFC62828)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = "Afluencia estimada: ${afluencia.displayName}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
