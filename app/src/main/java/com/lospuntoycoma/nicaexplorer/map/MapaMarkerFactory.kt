package com.lospuntoycoma.nicaexplorer.map

import android.content.Context
import androidx.annotation.DrawableRes
import com.lospuntoycoma.nicaexplorer.R
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory

/**
 * Punto único para resolver el icono de cada tipo de ubicación.
 * Por ahora todas las categorías usan el marcador común de NicaExplorer;
 * las categorías futuras podrán devolver recursos distintos sin tocar MapaScreen.
 */
object MapaMarkerFactory {
    fun comercioIcon(context: Context, categoria: String): Icon =
        IconFactory.getInstance(context).fromResource(markerResource(categoria))

    @DrawableRes
    private fun markerResource(categoria: String): Int = when {
        categoria.contains("restaurante", ignoreCase = true) -> R.drawable.ic_map_marker_comercio
        categoria.contains("cafeter", ignoreCase = true) -> R.drawable.ic_map_marker_comercio
        categoria.contains("hotel", ignoreCase = true) -> R.drawable.ic_map_marker_comercio
        categoria.contains("tienda", ignoreCase = true) -> R.drawable.ic_map_marker_comercio
        else -> R.drawable.ic_map_marker_comercio
    }
}
