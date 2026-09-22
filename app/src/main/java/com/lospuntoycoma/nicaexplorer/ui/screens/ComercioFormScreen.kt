package com.lospuntoycoma.nicaexplorer.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.data.ImageUploader
import com.lospuntoycoma.nicaexplorer.model.Comercio
import com.lospuntoycoma.nicaexplorer.model.RedesSociales
import com.lospuntoycoma.nicaexplorer.ui.components.NicaButton
import com.lospuntoycoma.nicaexplorer.ui.components.NicaTopBar
import com.lospuntoycoma.nicaexplorer.ui.theme.nicaAppBackgroundBrush
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.ComercioFormViewModel
import kotlinx.coroutines.launch

/**
 * Alta y edición de un comercio propio. Al crear, el comercio queda pendiente
 * de aprobación; el propietario no puede marcarlo como aprobado.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ComercioFormScreen(
    comercioId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val viewModel: ComercioFormViewModel = viewModel(
        key = "comercio_form_${comercioId ?: "nuevo"}",
        initializer = { ComercioFormViewModel(comercioId) }
    )
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val editando = !comercioId.isNullOrBlank()

    var nombre by remember { mutableStateOf("") }
    var categoriaPadre by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var cityId by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var tieneWhatsapp by remember { mutableStateOf(false) }
    var horario by remember { mutableStateOf("") }
    var diasAtencion by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var redes by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var servicios by remember { mutableStateOf("") }
    var productos by remember { mutableStateOf("") }
    var infoAdicional by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf("") }
    var logoUrl by remember { mutableStateOf("") }
    var galeria by remember { mutableStateOf<List<String>>(emptyList()) }
    var activo by remember { mutableStateOf(false) }

    var subiendoImagen by remember { mutableStateOf(false) }
    var errorLocal by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.comercioExistente) {
        uiState.comercioExistente?.let { c ->
            nombre = c.nombre
            categoriaPadre = c.categoriaPadre
            categoria = c.categoria
            cityId = c.cityId
            ciudad = c.ciudad
            descripcion = c.descripcion
            direccion = c.direccion
            latitud = c.latitud.takeIf { it != 0.0 }?.toString().orEmpty()
            longitud = c.longitud.takeIf { it != 0.0 }?.toString().orEmpty()
            telefono = c.telefono
            whatsapp = c.whatsapp
            tieneWhatsapp = c.tieneWhatsapp
            horario = c.horario
            diasAtencion = c.diasAtencion
            correo = c.correo
            redes = c.redesSociales
                .mapNotNull { RedesSociales.parsear(it) }
                .associate { it.first.clave to it.second }
            servicios = c.servicios.joinToString("\n")
            productos = c.productos.joinToString("\n")
            infoAdicional = c.infoAdicional
            imagenUrl = c.imagenUrl
            logoUrl = c.logoUrl
            galeria = c.galeria
            activo = c.activo
        }
    }

    LaunchedEffect(uiState.guardadoOk) {
        if (uiState.guardadoOk) onSaved()
    }

    val categorias = uiState.categorias
    val categoriasPadre = remember(categorias) {
        categorias.filter { it.categoriaPadre.isBlank() }.map { it.nombre }
    }
    val subcategorias = remember(categorias, categoriaPadre) {
        categorias.filter { it.categoriaPadre.equals(categoriaPadre, ignoreCase = true) }.map { it.nombre }
    }

    val subirImagen: (android.net.Uri, (String) -> Unit) -> Unit = { uri, onUrl ->
        scope.launch {
            subiendoImagen = true
            errorLocal = null
            ImageUploader.subir(context, uri)
                .onSuccess { onUrl(it) }
                .onFailure { errorLocal = it.localizedMessage ?: "No se pudo subir la imagen." }
            subiendoImagen = false
        }
    }

    val portadaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { subirImagen(it) { url -> imagenUrl = url } }
    }
    val logoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { subirImagen(it) { url -> logoUrl = url } }
    }
    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { subirImagen(it) { url -> galeria = galeria + url } }
    }

    Scaffold(
        topBar = {
            NicaTopBar(
                title = if (editando) "Editar comercio" else "Nuevo comercio",
                onBack = onBack
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .background(nicaAppBackgroundBrush())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (!editando) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Tu comercio se enviará a revisión. Un administrador lo aprobará antes de que sea visible; después podrás activarlo para que aparezca en el mapa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            FormTextField(nombre, { nombre = it }, "Nombre del comercio", requerido = true)

            DropdownSelector(
                label = "Categoría superior",
                value = categoriaPadre,
                options = categoriasPadre,
                onSelect = {
                    categoriaPadre = it
                    categoria = ""
                }
            )

            DropdownSelector(
                label = "Subcategoría",
                value = categoria,
                options = subcategorias,
                onSelect = { categoria = it },
                enabled = categoriaPadre.isNotBlank()
            )

            DropdownSelector(
                label = "Ciudad",
                value = ciudad,
                options = SampleData.cities.map { it.name },
                onSelect = { seleccion ->
                    val city = SampleData.cities.firstOrNull { it.name == seleccion }
                    ciudad = seleccion
                    cityId = city?.id.orEmpty()
                }
            )

            FormTextField(direccion, { direccion = it }, "Dirección")
            FormTextField(descripcion, { descripcion = it }, "Descripción", minLines = 3)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = latitud,
                    onValueChange = { latitud = it },
                    label = { Text("Latitud") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = longitud,
                    onValueChange = { longitud = it },
                    label = { Text("Longitud") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            FormTextField(telefono, { telefono = it }, "Teléfono", keyboardType = KeyboardType.Phone)
            FormTextField(whatsapp, { whatsapp = it }, "WhatsApp", keyboardType = KeyboardType.Phone)

            SwitchRow("Tiene WhatsApp", tieneWhatsapp) { tieneWhatsapp = it }

            FormTextField(horario, { horario = it }, "Horario (ej. 8:00 a.m. - 6:00 p.m.)")
            FormTextField(diasAtencion, { diasAtencion = it }, "Días de atención")
            FormTextField(
                value = correo,
                onValueChange = { correo = it },
                label = "Correo de contacto",
                keyboardType = KeyboardType.Email
            )

            SeccionTitulo("Redes sociales")
            Text(
                text = "Elige una o varias redes y escribe el enlace o tu usuario.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RedesSociales.disponibles.forEach { red ->
                    val seleccionada = redes.containsKey(red.clave)
                    FilterChip(
                        selected = seleccionada,
                        onClick = {
                            redes = if (seleccionada) {
                                redes - red.clave
                            } else {
                                redes + (red.clave to "")
                            }
                        },
                        label = { Text(red.nombre) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            RedesSociales.disponibles
                .filter { redes.containsKey(it.clave) }
                .forEach { red ->
                    FormTextField(
                        value = redes[red.clave].orEmpty(),
                        onValueChange = { valor -> redes = redes + (red.clave to valor) },
                        label = "Enlace o usuario de ${red.nombre}"
                    )
                }

            FormTextField(servicios, { servicios = it }, "Servicios (uno por línea)", minLines = 3)
            FormTextField(productos, { productos = it }, "Productos (uno por línea)", minLines = 3)
            FormTextField(infoAdicional, { infoAdicional = it }, "Información adicional", minLines = 2)

            SeccionTitulo("Imágenes")

            ImagenUnica(
                etiqueta = "Portada",
                url = imagenUrl,
                subiendo = subiendoImagen,
                onElegir = { portadaLauncher.launch("image/*") },
                onQuitar = { imagenUrl = "" }
            )
            ImagenUnica(
                etiqueta = "Logo",
                url = logoUrl,
                subiendo = subiendoImagen,
                onElegir = { logoLauncher.launch("image/*") },
                onQuitar = { logoUrl = "" }
            )

            Text(
                text = "Galería de fotos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            galeria.forEach { url ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    IconButton(onClick = { galeria = galeria - url }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            OutlinedButton(
                onClick = { galeriaLauncher.launch("image/*") },
                enabled = !subiendoImagen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar foto")
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (subiendoImagen) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Subiendo imagen...", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (editando) {
                SwitchRow("Comercio activo (visible en el mapa si está aprobado)", activo) { activo = it }
                Spacer(modifier = Modifier.height(8.dp))
            }

            (errorLocal ?: uiState.error)?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            NicaButton(
                text = if (uiState.saving) "Guardando..." else "Guardar comercio",
                enabled = !uiState.saving && !subiendoImagen,
                onClick = {
                    if (nombre.isBlank()) {
                        errorLocal = "El nombre del comercio es obligatorio."
                        return@NicaButton
                    }
                    errorLocal = null
                    viewModel.guardar(
                        Comercio(
                            id = comercioId.orEmpty(),
                            nombre = nombre.trim(),
                            categoria = categoria.trim(),
                            categoriaPadre = categoriaPadre.trim(),
                            descripcion = descripcion.trim(),
                            ciudad = ciudad.trim(),
                            cityId = cityId.trim(),
                            direccion = direccion.trim(),
                            horario = horario.trim(),
                            diasAtencion = diasAtencion.trim(),
                            imagenUrl = imagenUrl.trim(),
                            logoUrl = logoUrl.trim(),
                            galeria = galeria,
                            latitud = latitud.trim().toDoubleOrNull() ?: 0.0,
                            longitud = longitud.trim().toDoubleOrNull() ?: 0.0,
                            telefono = telefono.trim(),
                            whatsapp = whatsapp.trim(),
                            tieneWhatsapp = tieneWhatsapp,
                            correo = correo.trim(),
                            redesSociales = redes.mapNotNull { (clave, valor) ->
                                RedesSociales.entrada(clave, valor)
                            },
                            servicios = servicios.split("\n").map { it.trim() }.filter { it.isNotBlank() },
                            productos = productos.split("\n").map { it.trim() }.filter { it.isNotBlank() },
                            infoAdicional = infoAdicional.trim(),
                            activo = activo
                        )
                    )
                }
            )

            if (uiState.saving) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SeccionTitulo(texto: String) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    requerido: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(if (requerido) "$label *" else label) },
        singleLine = minLines == 1,
        minLines = minLines,
        maxLines = if (minLines == 1) 1 else minLines + 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (value.isBlank()) label else "$label: $value",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ImagenUnica(
    etiqueta: String,
    url: String,
    subiendo: Boolean,
    onElegir: () -> Unit,
    onQuitar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            if (url.isNotBlank()) {
                AsyncImage(
                    model = url,
                    contentDescription = etiqueta,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(72.dp)
                )
            } else {
                Icon(
                    Icons.Filled.AddPhotoAlternate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(onClick = onElegir, enabled = !subiendo) {
                Text(if (url.isBlank()) "Elegir imagen" else "Cambiar")
            }
        }
        if (url.isNotBlank()) {
            IconButton(onClick = onQuitar) {
                Icon(Icons.Filled.Close, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
