package com.lospuntoycoma.nicaexplorer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage

/**
 * Muestra una imagen remota (gestionada desde el backend) cuando hay URL y,
 * si no, el drawable local como respaldo.
 *
 * @return `true` si dibujó alguna imagen; `false` si no había ninguna fuente.
 */
@Composable
fun CoverImage(
    url: String?,
    imageRes: Int?,
    contentDescription: String?,
    modifier: Modifier = Modifier
): Boolean {
    return when {
        !url.isNullOrBlank() -> {
            val fallback = imageRes?.let { painterResource(it) }
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop,
                error = fallback,
                fallback = fallback
            )
            true
        }
        imageRes != null -> {
            Image(
                painter = painterResource(imageRes),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
            true
        }
        else -> false
    }
}
