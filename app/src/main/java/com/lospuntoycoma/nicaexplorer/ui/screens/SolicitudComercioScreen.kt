package com.lospuntoycoma.nicaexplorer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lospuntoycoma.nicaexplorer.model.SolicitudComercio
import com.lospuntoycoma.nicaexplorer.ui.components.NicaButton
import com.lospuntoycoma.nicaexplorer.ui.components.NicaTopBar
import com.lospuntoycoma.nicaexplorer.ui.theme.nicaAppBackgroundBrush
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.SolicitudComercioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudComercioScreen(
    cityId: String,
    ciudad: String,
    onBack: () -> Unit
) {
    val viewModel: SolicitudComercioViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var nombreNegocio by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var whatsapp by rememberSaveable { mutableStateOf("") }
    var horario by rememberSaveable { mutableStateOf("") }
    var nombreResponsable by rememberSaveable { mutableStateOf("") }
    var correoResponsable by rememberSaveable { mutableStateOf("") }
    var redesSociales by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            NicaTopBar(
                title = "Solicitud de comercio",
                onBack = onBack
            )
        }
    ) { padding ->
        if (uiState.isSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(nicaAppBackgroundBrush())
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Solicitud enviada",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nuestro equipo revisará la información antes de publicar el negocio en NicaExplorer.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    NicaButton(
                        text = "Volver a comercios",
                        onClick = onBack
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .background(nicaAppBackgroundBrush())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Solicita aparecer en NicaExplorer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Completa la información para que nuestro equipo pueda revisar tu negocio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                SolicitudTextField(
                    value = nombreNegocio,
                    onValueChange = {
                        nombreNegocio = it
                        viewModel.clearValidationError("nombreNegocio")
                    },
                    label = "Nombre del negocio",
                    error = uiState.validationErrors["nombreNegocio"]
                )

                SolicitudTextField(
                    value = ciudad,
                    onValueChange = {},
                    label = "Ciudad",
                    error = uiState.validationErrors["ciudad"],
                    readOnly = true
                )

                SolicitudTextField(
                    value = categoria,
                    onValueChange = {
                        categoria = it
                        viewModel.clearValidationError("categoria")
                    },
                    label = "Categoría",
                    error = uiState.validationErrors["categoria"]
                )

                SolicitudTextField(
                    value = direccion,
                    onValueChange = {
                        direccion = it
                        viewModel.clearValidationError("direccion")
                    },
                    label = "Dirección",
                    error = uiState.validationErrors["direccion"]
                )

                SolicitudTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                        viewModel.clearValidationError("descripcion")
                    },
                    label = "Descripción",
                    error = uiState.validationErrors["descripcion"],
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5
                )

                SolicitudTextField(
                    value = telefono,
                    onValueChange = {
                        telefono = it
                        viewModel.clearValidationError("telefono")
                    },
                    label = "Teléfono",
                    error = uiState.validationErrors["telefono"],
                    keyboardType = KeyboardType.Phone
                )

                SolicitudTextField(
                    value = whatsapp,
                    onValueChange = {
                        whatsapp = it
                        viewModel.clearValidationError("whatsapp")
                    },
                    label = "WhatsApp",
                    error = uiState.validationErrors["whatsapp"],
                    keyboardType = KeyboardType.Phone
                )

                SolicitudTextField(
                    value = horario,
                    onValueChange = {
                        horario = it
                        viewModel.clearValidationError("horario")
                    },
                    label = "Horario",
                    error = uiState.validationErrors["horario"]
                )

                SolicitudTextField(
                    value = nombreResponsable,
                    onValueChange = {
                        nombreResponsable = it
                        viewModel.clearValidationError("nombreResponsable")
                    },
                    label = "Nombre del responsable",
                    error = uiState.validationErrors["nombreResponsable"]
                )

                SolicitudTextField(
                    value = correoResponsable,
                    onValueChange = {
                        correoResponsable = it
                        viewModel.clearValidationError("correoResponsable")
                    },
                    label = "Correo del responsable",
                    error = uiState.validationErrors["correoResponsable"],
                    keyboardType = KeyboardType.Email
                )

                SolicitudTextField(
                    value = redesSociales,
                    onValueChange = {
                        redesSociales = it
                        viewModel.clearValidationError("redesSociales")
                    },
                    label = "Redes sociales (opcional)",
                    error = uiState.validationErrors["redesSociales"],
                    imeAction = ImeAction.Done
                )

                Spacer(modifier = Modifier.height(12.dp))

                NicaButton(
                    text = if (uiState.isLoading) "Enviando solicitud..." else "Enviar solicitud",
                    enabled = !uiState.isLoading,
                    onClick = {
                        viewModel.enviarSolicitud(
                            SolicitudComercio(
                                nombreNegocio = nombreNegocio,
                                ciudad = ciudad,
                                cityId = cityId,
                                categoria = categoria,
                                direccion = direccion,
                                descripcion = descripcion,
                                telefono = telefono,
                                whatsapp = whatsapp,
                                horario = horario,
                                nombreResponsable = nombreResponsable,
                                correoResponsable = correoResponsable,
                                redesSociales = redesSociales
                            )
                        )
                    }
                )

                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(14.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                uiState.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SolicitudTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 4
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = if (error != null) {
            {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            null
        },
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            errorBorderColor = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    )
}
