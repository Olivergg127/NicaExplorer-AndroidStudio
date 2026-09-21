package com.lospuntoycoma.nicaexplorer.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Texto recortado a [maxLines] con un control "Ver más" / "Ver menos".
 *
 * El control solo aparece cuando el texto realmente se desborda, por lo que el
 * contenido corto se muestra completo sin agregar un botón innecesario.
 */
@Composable
fun TextoExpandible(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 4,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign? = null
) {
    var expanded by remember(text) { mutableStateOf(false) }
    var hasOverflow by remember(text) { mutableStateOf(false) }

    Column(modifier = modifier.animateContentSize()) {
        Text(
            text = text,
            style = style,
            color = color,
            textAlign = textAlign,
            maxLines = if (expanded) Int.MAX_VALUE else maxLines,
            onTextLayout = { result ->
                if (!expanded) {
                    hasOverflow = result.hasVisualOverflow
                }
            }
        )

        if (hasOverflow) {
            TextButton(
                onClick = { expanded = !expanded },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = if (expanded) "Ver menos" else "Ver más",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
