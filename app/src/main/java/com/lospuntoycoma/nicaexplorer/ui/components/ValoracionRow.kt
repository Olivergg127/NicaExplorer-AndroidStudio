package com.lospuntoycoma.nicaexplorer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.lospuntoycoma.nicaexplorer.data.FirebaseRepository
import com.lospuntoycoma.nicaexplorer.data.ValoracionesRepository
import com.lospuntoycoma.nicaexplorer.ui.theme.GoldAccent
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Fila de valoración con 5 estrellas: muestra el promedio y permite al usuario
 * calificar (1..5). El objetivo se identifica por [tipo] y [refId].
 *
 * El promedio se toma de la caché pública (sirve también para visitantes); las
 * estrellas se actualizan solas cuando alguien valora.
 */
@Composable
fun ValoracionRow(
    tipo: String,
    refId: String,
    cityId: String,
    modifier: Modifier = Modifier
) {
    val uid = remember { FirebaseRepository.getCurrentUser()?.uid.orEmpty() }
    val scope = rememberCoroutineScope()

    val publicas by ValoracionesRepository.publicas.collectAsState()
    val promedioTotal = remember(publicas, tipo, refId) {
        ValoracionesRepository.resumenPublico(tipo, refId)
    }
    val promedio = promedioTotal.first
    val total = promedioTotal.second

    var miValoracion by remember(refId) { mutableStateOf<Int?>(null) }

    LaunchedEffect(tipo, refId, uid) {
        if (uid.isBlank() || refId.isBlank()) return@LaunchedEffect
        miValoracion = ValoracionesRepository.miValoracion(tipo, refId, uid)
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        (1..5).forEach { indice ->
            val llena = indice <= (miValoracion ?: promedio.roundToInt())
            Icon(
                imageVector = if (llena) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Valorar con $indice estrellas",
                tint = if (llena) GoldAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                modifier = Modifier
                    .size(26.dp)
                    .clickable(enabled = uid.isNotBlank()) {
                        scope.launch {
                            if (ValoracionesRepository.guardar(tipo, refId, cityId, uid, indice)) {
                                miValoracion = indice
                            }
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (total > 0) {
                String.format(Locale.getDefault(), "%.1f (%d)", promedio, total)
            } else {
                "Sin valoraciones"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
