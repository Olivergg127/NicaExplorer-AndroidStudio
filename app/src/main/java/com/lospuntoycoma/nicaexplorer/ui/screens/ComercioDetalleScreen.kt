package com.lospuntoycoma.nicaexplorer.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.lospuntoycoma.nicaexplorer.R
import com.lospuntoycoma.nicaexplorer.model.Comercio
import com.lospuntoycoma.nicaexplorer.model.RedesSociales
import com.lospuntoycoma.nicaexplorer.model.TipoValoracion
import com.lospuntoycoma.nicaexplorer.ui.components.ComercioCover
import com.lospuntoycoma.nicaexplorer.ui.components.ValoracionRow
import com.lospuntoycoma.nicaexplorer.ui.components.EmptyState
import com.lospuntoycoma.nicaexplorer.ui.components.NicaButton
import com.lospuntoycoma.nicaexplorer.ui.components.NicaTopBar
import com.lospuntoycoma.nicaexplorer.ui.components.TextoExpandible
import com.lospuntoycoma.nicaexplorer.ui.theme.GreenPrimary
import com.lospuntoycoma.nicaexplorer.ui.theme.GreenSurface
import com.lospuntoycoma.nicaexplorer.ui.theme.SuccessGreen
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.ComercioDetalleViewModel
import com.lospuntoycoma.nicaexplorer.util.TelefonoUtils
import kotlinx.coroutines.launch

/**
 * Pantalla de detalle de un comercio/restaurante.
 * Muestra toda la información y permite contactar por WhatsApp,
 * copiar el teléfono y abrir la ubicación en Google Maps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComercioDetalleScreen(
    comercioId: String,
    onBack: () -> Unit
) {
    val viewModel: ComercioDetalleViewModel = viewModel(
        initializer = { ComercioDetalleViewModel(comercioId) }
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var showMapsDialog by remember { mutableStateOf(false) }
    var lightboxIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            NicaTopBar(
                title = uiState.comercio?.nombre ?: stringResource(R.string.comerciodet_comercio),
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    EmptyState(
                        icon = Icons.Filled.Refresh,
                        message = uiState.error?.let { stringResource(it) } ?: "",
                        buttonText = stringResource(R.string.comerciodet_reintentar),
                        onButtonClick = { viewModel.loadComercio() }
                    )
                }
            }

            else -> {
                val comercio = uiState.comercio
                if (comercio == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        EmptyState(
                            icon = Icons.Filled.Storefront,
                            message = stringResource(R.string.comerciodet_no_encontrado),
                            buttonText = stringResource(R.string.comerciodet_reintentar),
                            onButtonClick = { viewModel.loadComercio() }
                        )
                    }
                } else {
                    val msgNumeroCopiado = stringResource(R.string.comerciodet_numero_copiado)
                    val msgSinAppWhatsapp = stringResource(R.string.comerciodet_sin_app_whatsapp)
                    val msgSinAppCorreo = stringResource(R.string.comerciodet_sin_app_correo)
                    val msgNoAbrirMaps = stringResource(R.string.comerciodet_no_abrir_maps)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        ComercioCover(
                            comercio = comercio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        )

                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = comercio.nombre,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = comercio.categoria,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = comercio.ciudad,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            ValoracionRow(
                                tipo = TipoValoracion.COMERCIO,
                                refId = comercio.id,
                                cityId = comercio.cityId
                            )

                            if (comercio.tieneWhatsapp) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GreenSurface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Chat,
                                            contentDescription = null,
                                            tint = GreenPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.comerciodet_whatsapp_disponible),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GreenPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            ComercioInfoRow(
                                icon = Icons.Filled.AccessTime,
                                label = stringResource(R.string.comerciodet_horario),
                                value = comercio.horario
                            )
                            ComercioInfoRow(
                                icon = Icons.Filled.Place,
                                label = stringResource(R.string.comerciodet_direccion),
                                value = comercio.direccion
                            )

                            if (comercio.descripcion.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.comerciodet_descripcion),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextoExpandible(
                                    text = comercio.descripcion,
                                    maxLines = 4,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                            }

                            if (comercio.galeria.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = stringResource(R.string.comerciodet_fotos),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(comercio.galeria) { url ->
                                        AsyncImage(
                                            model = url,
                                            contentDescription = stringResource(R.string.comerciodet_ampliar_foto),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(140.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable {
                                                    lightboxIndex = comercio.galeria.indexOf(url)
                                                }
                                        )
                                    }
                                }
                            }

                            if (comercio.diasAtencion.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                ComercioInfoRow(
                                    icon = Icons.Filled.AccessTime,
                                    label = stringResource(R.string.comerciodet_dias_atencion),
                                    value = comercio.diasAtencion
                                )
                            }

                            ListaTexto(stringResource(R.string.comerciodet_servicios), comercio.servicios)
                            ListaTexto(stringResource(R.string.comerciodet_productos), comercio.productos)

                            val redesValidas = comercio.redesSociales
                                .mapNotNull { RedesSociales.parsear(it) }
                            if (redesValidas.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.comerciodet_redes_sociales),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    redesValidas.forEach { (red, valor) ->
                                        val mensajeError = stringResource(R.string.comerciodet_no_abrir_red, red.nombre)
                                        Image(
                                            painter = painterResource(id = red.icono),
                                            contentDescription = red.nombre,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    openSocial(
                                                        context,
                                                        RedesSociales.urlDe("${red.clave}|$valor")
                                                    ) {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(mensajeError)
                                                        }
                                                    }
                                                }
                                        )
                                    }
                                }
                            }

                            if (comercio.correo.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                ComercioInfoRow(
                                    icon = Icons.Filled.Email,
                                    label = stringResource(R.string.comerciodet_correo_contacto),
                                    value = comercio.correo
                                )
                            }

                            if (comercio.infoAdicional.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.comerciodet_info_adicional),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextoExpandible(
                                    text = comercio.infoAdicional,
                                    maxLines = 4,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                            }

                            if (!comercio.visiblePublicamente) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (!comercio.aprobado) {
                                            stringResource(R.string.comerciodet_pendiente_aprobacion)
                                        } else {
                                            stringResource(R.string.comerciodet_inactivo)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            if (comercio.telefono.isNotBlank()) {
                                NicaButton(
                                    text = stringResource(R.string.comerciodet_copiar_telefono),
                                    onClick = {
                                        clipboard.setText(AnnotatedString(comercio.telefono))
                                        scope.launch {
                                            snackbarHostState.showSnackbar(msgNumeroCopiado)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (comercio.tieneWhatsapp && comercio.whatsapp.isNotBlank()) {
                                NicaButton(
                                    text = stringResource(R.string.comerciodet_contactar_whatsapp),
                                    onClick = {
                                        openWhatsApp(context, comercio) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(msgSinAppWhatsapp)
                                            }
                                        }
                                    },
                                    gradient = Brush.horizontalGradient(
                                        colors = listOf(GreenPrimary, SuccessGreen)
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (comercio.correo.isNotBlank()) {
                                NicaButton(
                                    text = stringResource(R.string.comerciodet_enviar_correo),
                                    onClick = {
                                        openCorreo(context, comercio.nombre, comercio.correo) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(msgSinAppCorreo)
                                            }
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (comercio.latitud != 0.0 && comercio.longitud != 0.0) {
                                NicaButton(
                                    text = stringResource(R.string.comerciodet_ver_maps),
                                    onClick = { showMapsDialog = true }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                    if (showMapsDialog) {
                        AlertDialog(
                            onDismissRequest = { showMapsDialog = false },
                            title = { Text(stringResource(R.string.comerciodet_abrir_maps_titulo)) },
                            text = {
                                Text(stringResource(R.string.comerciodet_abrir_maps_pregunta, comercio.nombre))
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showMapsDialog = false
                                        openGoogleMaps(context, comercio) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(msgNoAbrirMaps)
                                            }
                                        }
                                    }
                                ) {
                                    Text(stringResource(R.string.comerciodet_abrir_maps))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showMapsDialog = false }) {
                                    Text(stringResource(R.string.comerciodet_cancelar))
                                }
                            }
                        )
                    }

                    val indiceActual = lightboxIndex
                    if (indiceActual != null && indiceActual in comercio.galeria.indices) {
                        GaleriaLightbox(
                            urls = comercio.galeria,
                            initialIndex = indiceActual,
                            onClose = { lightboxIndex = null }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visor a pantalla completa de la galería. Permite deslizar (swipe) entre las fotos.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GaleriaLightbox(
    urls: List<String>,
    initialIndex: Int,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val pagerState = rememberPagerState(initialPage = initialIndex) { urls.size }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = urls[page],
                    contentDescription = stringResource(R.string.comerciodet_foto_de, page + 1, urls.size),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.comerciodet_cerrar),
                    tint = Color.White
                )
            }

            if (urls.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${urls.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ListaTexto(titulo: String, items: List<String>) {
    if (items.isEmpty()) return

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(6.dp))
    items.forEach { item ->
        Text(
            text = "• $item",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }
}

@Composable
private fun ComercioInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    if (value.isBlank()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

private fun openWhatsApp(context: Context, comercio: Comercio, onError: () -> Unit) {
    val number = TelefonoUtils.buildWhatsAppNumber(comercio.whatsapp)
    if (number.isBlank()) {
        onError()
        return
    }
    try {
        val directIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("whatsapp://send?phone=$number")
        )
        if (directIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(directIntent)
            return
        }

        val fallbackIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://wa.me/$number")
        )
        if (fallbackIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(fallbackIntent)
        } else {
            onError()
        }
    } catch (e: Exception) {
        onError()
    }
}

private fun openGoogleMaps(context: Context, comercio: Comercio, onError: () -> Unit) {
    val label = Uri.encode(comercio.nombre)
    val geoUri = Uri.parse(
        "geo:0,0?q=${comercio.latitud},${comercio.longitud}($label)"
    )
    val gmmIntent = Intent(Intent.ACTION_VIEW, geoUri)
        .setPackage("com.google.android.apps.maps")
    try {
        if (gmmIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(gmmIntent)
        } else {
            val chooser = Intent.createChooser(
                Intent(Intent.ACTION_VIEW, geoUri),
                "Abrir ubicación"
            )
            context.startActivity(chooser)
        }
    } catch (e: Exception) {
        onError()
    }
}

private fun openSocial(context: Context, url: String, onError: () -> Unit) {
    if (url.isBlank()) {
        onError()
        return
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        onError()
    }
}

private fun openCorreo(context: Context, comercio: String, correo: String, onError: () -> Unit) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$correo")
            putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre $comercio")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        onError()
    }
}
