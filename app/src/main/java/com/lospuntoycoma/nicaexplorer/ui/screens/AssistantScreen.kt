package com.lospuntoycoma.nicaexplorer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lospuntoycoma.nicaexplorer.R
import com.lospuntoycoma.nicaexplorer.data.ApiRepository
import com.lospuntoycoma.nicaexplorer.data.GeminiRepository
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.data.UserPreferences
import com.lospuntoycoma.nicaexplorer.model.Place
import com.lospuntoycoma.nicaexplorer.ui.components.EstadoMascota
import com.lospuntoycoma.nicaexplorer.ui.components.MascotaFlotante
import com.lospuntoycoma.nicaexplorer.ui.components.NicaTopBar
import com.lospuntoycoma.nicaexplorer.ui.theme.nicaAppBackgroundBrush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    placeContext: Place? = null,
    onBack: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var isLoading by remember { mutableStateOf(false) }
    var respondiendo by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val localPlaces = remember { SampleData.allPlaces }
    val mascotaActiva by UserPreferences.mascotaFlow().collectAsState(initial = true)
    val mensajeErrorConexion = stringResource(R.string.assistant_error_conexion)

    val citySuggestion = placeContext?.city
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { stringResource(R.string.assistant_que_puedo_visitar_en, it) }
        ?: stringResource(R.string.assistant_que_puedo_visitar)

    val quickQuestions = listOf(
        citySuggestion,
        stringResource(R.string.assistant_cuentame_sobre_este_lugar),
        stringResource(R.string.assistant_como_funciona_ar)
    )

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || isLoading) return

        keyboardController?.hide()
        messages.add(ChatMessage(trimmed, isUser = true))
        message = ""
        isLoading = true

        scope.launch {
            // Obtenemos los comercios dinámicamente para pasarlos como contexto a Itzae
            val comerciosResult = ApiRepository.getComercios()
            val comercios = comerciosResult.getOrDefault(emptyList())

            val response = GeminiRepository.generateContent(
                prompt = trimmed,
                comercios = comercios,
                place = placeContext,
                places = localPlaces
            )
            messages.add(
                ChatMessage(
                    response ?: mensajeErrorConexion,
                    isUser = false
                )
            )
            isLoading = false
            // La mascota "responde" (rebota/habla) unos segundos tras contestar.
            respondiendo = true
            delay(1800)
            respondiendo = false
        }
    }

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            val lastIndex = messages.lastIndex + if (isLoading) 1 else 0
            listState.animateScrollToItem(lastIndex)
        }
    }

    Scaffold(
        topBar = {
            NicaTopBar(
                title = "Itzae",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch { UserPreferences.setMascotaEnabled(!mascotaActiva) }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SmartToy,
                            contentDescription = if (mascotaActiva) stringResource(R.string.assistant_ocultar_mascota) else stringResource(R.string.assistant_mostrar_mascota),
                            tint = if (mascotaActiva) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .background(nicaAppBackgroundBrush())
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = com.lospuntoycoma.nicaexplorer.R.drawable.logo_asistente),
                                    contentDescription = "Itzae",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.assistant_saludo),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.assistant_que_deseas_conocer),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                                placeContext?.let { place ->
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = stringResource(R.string.assistant_consultando_sobre, place.name),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.assistant_preguntas_rapidas),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        quickQuestions.forEach { question ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        message = question
                                        sendMessage(question)
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Text(
                                    text = question,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(messages) { msg ->
                        ChatBubble(message = msg)
                    }
                    if (isLoading) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text(stringResource(R.string.assistant_escribe_mensaje)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = { sendMessage(message) }
                    )
                )

                IconButton(
                    onClick = {
                        sendMessage(message)
                    },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.assistant_enviar),
                        tint = if (message.isNotBlank()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }

            if (mascotaActiva) {
                val estadoMascota = when {
                    isLoading -> EstadoMascota.PENSANDO
                    respondiendo -> EstadoMascota.RESPONDIENDO
                    else -> EstadoMascota.IDLE
                }
                MascotaFlotante(
                    imageRes = com.lospuntoycoma.nicaexplorer.R.drawable.mascota_itzae,
                    estado = estadoMascota,
                    onCerrar = { scope.launch { UserPreferences.setMascotaEnabled(false) } }
                )
            }
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@Composable
private fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (message.isUser)
                Arrangement.End
            else
                Arrangement.Start
        ) {
            if (!message.isUser) {
                AssistantAvatar()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        color = if (message.isUser)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isUser) 16.dp else 4.dp,
                            bottomEnd = if (message.isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) Color.White
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun AssistantAvatar() {
    Icon(
        painter = painterResource(id = com.lospuntoycoma.nicaexplorer.R.drawable.logo_asistente),
        contentDescription = "Itzae",
        tint = Color.Unspecified,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
    )
}

@Composable
private fun TypingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            AssistantAvatar()
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.assistant_pensando),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
