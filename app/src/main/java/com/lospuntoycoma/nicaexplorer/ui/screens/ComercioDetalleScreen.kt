package com.lospuntoycoma.nicaexplorer.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lospuntoycoma.nicaexplorer.model.Comercio
import com.lospuntoycoma.nicaexplorer.ui.components.ComercioCover
import com.lospuntoycoma.nicaexplorer.ui.components.EmptyState
import com.lospuntoycoma.nicaexplorer.ui.components.NicaButton
import com.lospuntoycoma.nicaexplorer.ui.components.NicaTopBar
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

    Scaffold(
        topBar = {
            NicaTopBar(
                title = uiState.comercio?.nombre ?: "Comercio",
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
                        message = uiState.error ?: "",
                        buttonText = "Reintentar",
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
                            message = "No se encontró el comercio.",
                            buttonText = "Reintentar",
                            onButtonClick = { viewModel.loadComercio() }
                        )
                    }
                } else {
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
                                            text = "WhatsApp disponible",
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
                                label = "Horario",
                                value = comercio.horario
                            )
                            ComercioInfoRow(
                                icon = Icons.Filled.Place,
                                label = "Dirección",
                                value = comercio.direccion
                            )

                            if (comercio.descripcion.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Descripción",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = comercio.descripcion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            if (comercio.telefono.isNotBlank()) {
                                NicaButton(
                                    text = "Copiar teléfono",
                                    onClick = {
                                        clipboard.setText(AnnotatedString(comercio.telefono))
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Número copiado")
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (comercio.tieneWhatsapp && comercio.whatsapp.isNotBlank()) {
                                NicaButton(
                                    text = "Contactar por WhatsApp",
                                    onClick = {
                                        openWhatsApp(context, comercio) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "No hay una aplicación compatible para abrir WhatsApp."
                                                )
                                            }
                                        }
                                    },
                                    gradient = Brush.horizontalGradient(
                                        colors = listOf(GreenPrimary, SuccessGreen)
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (comercio.latitud != 0.0 && comercio.longitud != 0.0) {
                                NicaButton(
                                    text = "Ver en Google Maps",
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
                            title = { Text("Abrir en Google Maps") },
                            text = {
                                Text("¿Quieres abrir la ubicación de ${comercio.nombre} en Google Maps?")
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showMapsDialog = false
                                        openGoogleMaps(context, comercio) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "No se pudo abrir Google Maps."
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Text("Abrir Maps")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showMapsDialog = false }) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }
                }
            }
        }
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
