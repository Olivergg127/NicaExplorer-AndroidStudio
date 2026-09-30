package com.lospuntoycoma.nicaexplorer.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lospuntoycoma.nicaexplorer.ui.theme.GoldAccent
import java.util.Locale

/**
 * Puntuación compacta (estrella + promedio + nº de reseñas) para tarjetas.
 * No dibuja nada si el objetivo aún no tiene valoraciones.
 */
@Composable
fun RatingStars(
    promedio: Double,
    total: Int,
    modifier: Modifier = Modifier
) {
    if (total <= 0) return

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = GoldAccent,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = String.format(Locale.getDefault(), "%.1f", promedio),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = "($total)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
