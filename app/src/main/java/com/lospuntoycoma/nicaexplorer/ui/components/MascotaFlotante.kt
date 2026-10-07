package com.lospuntoycoma.nicaexplorer.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lospuntoycoma.nicaexplorer.R
import kotlin.math.roundToInt

/**
 * Estado de ánimo de la mascota; cambia el tipo de animación.
 */
enum class EstadoMascota {
    /** Reposo: respira, flota y se balancea suave. */
    IDLE,

    /** Itzae está pensando: se mueve rápido y nervioso. */
    PENSANDO,

    /** Itzae responde: rebota y "habla" (squash/stretch rápido). */
    RESPONDIENDO
}

/**
 * Mascota flotante de Itzae para el chat.
 *
 * - Se puede **arrastrar** con el dedo por toda la pantalla (acotada a los bordes).
 * - Tiene **animaciones procedurales** según [estado] (no hay fotogramas en el arte):
 *   - [EstadoMascota.IDLE]: respiración lenta + flote + vaivén.
 *   - [EstadoMascota.PENSANDO]: oscilación rápida (nerviosa) con más vaivén.
 *   - [EstadoMascota.RESPONDIENDO]: rebote y "habla" con squash/stretch rápido.
 * - `onCerrar` (opcional) muestra una "x" para ocultarla.
 *
 * El sprite es pixel art (64x64), por eso se dibuja con [FilterQuality.None] para que
 * se vea nítido al escalar.
 */
@Composable
fun MascotaFlotante(
    imageRes: Int,
    modifier: Modifier = Modifier,
    tamano: Dp = 110.dp,
    estado: EstadoMascota = EstadoMascota.IDLE,
    onCerrar: (() -> Unit)? = null
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val anchoMax = constraints.maxWidth.toFloat()
        val altoMax = constraints.maxHeight.toFloat()
        val mascotaPx = with(LocalDensity.current) { tamano.toPx() }

        // Posición base (la que cambia el arrastre).
        var posX by remember { mutableStateOf((anchoMax - mascotaPx - 24f).coerceAtLeast(0f)) }
        var posY by remember { mutableStateOf((altoMax - mascotaPx - 160f).coerceAtLeast(0f)) }

        // Parámetros de animación según el estado.
        val respiraAmp: Float
        val respiraDur: Int
        val floteAmp: Float
        val floteDur: Int
        val vaivenAmp: Float
        val vaivenDur: Int
        when (estado) {
            EstadoMascota.IDLE -> {
                // Respiración lenta y marcada + flote + vaivén suave.
                respiraAmp = 0.07f; respiraDur = 1900
                floteAmp = -16f; floteDur = 1900
                vaivenAmp = 4f; vaivenDur = 2400
            }
            EstadoMascota.PENSANDO -> {
                // Movimiento nervioso y rápido (más vaivén) mientras "piensa".
                respiraAmp = 0.03f; respiraDur = 480
                floteAmp = -8f; floteDur = 440
                vaivenAmp = 11f; vaivenDur = 420
            }
            EstadoMascota.RESPONDIENDO -> {
                // Rebote y "habla" (squash/stretch rápido).
                respiraAmp = 0.09f; respiraDur = 210
                floteAmp = -22f; floteDur = 200
                vaivenAmp = 3f; vaivenDur = 300
            }
        }

        val transicion = rememberInfiniteTransition(label = "mascota")
        val respira by transicion.animateFloat(
            initialValue = 1f - respiraAmp,
            targetValue = 1f + respiraAmp,
            animationSpec = infiniteRepeatable(tween(durationMillis = respiraDur), RepeatMode.Reverse),
            label = "respira"
        )
        val flote by transicion.animateFloat(
            initialValue = 0f,
            targetValue = floteAmp,
            animationSpec = infiniteRepeatable(tween(durationMillis = floteDur), RepeatMode.Reverse),
            label = "flote"
        )
        val vaiven by transicion.animateFloat(
            initialValue = -vaivenAmp,
            targetValue = vaivenAmp,
            animationSpec = infiniteRepeatable(tween(durationMillis = vaivenDur), RepeatMode.Reverse),
            label = "vaiven"
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(posX.roundToInt(), (posY + flote).roundToInt()) }
                .size(tamano)
                .graphicsLayer {
                    rotationZ = vaiven
                    // Squash/stretch conservando volumen (X inverso a Y).
                    scaleY = respira
                    scaleX = 2f - respira
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        posX = (posX + drag.x).coerceIn(0f, (anchoMax - mascotaPx).coerceAtLeast(0f))
                        posY = (posY + drag.y).coerceIn(0f, (altoMax - mascotaPx).coerceAtLeast(0f))
                    }
                }
        ) {
            Image(
                bitmap = ImageBitmap.imageResource(id = imageRes),
                contentDescription = stringResource(R.string.comp_mascota_descripcion),
                filterQuality = FilterQuality.None,
                modifier = Modifier.fillMaxSize()
            )

            if (estado == EstadoMascota.PENSANDO) {
                BurbujaPensando(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-22).dp)
                )
            }

            if (onCerrar != null) {
                IconButton(
                    onClick = onCerrar,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.comp_mascota_ocultar),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/** Burbuja de "pensando" con tres puntos que se iluminan en secuencia. */
@Composable
private fun BurbujaPensando(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "pensando")
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { i ->
            val alpha by transicion.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, delayMillis = i * 180),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "punto$i"
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .graphicsLayer { this.alpha = alpha }
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}
