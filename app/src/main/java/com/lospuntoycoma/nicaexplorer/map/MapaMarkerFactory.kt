package com.lospuntoycoma.nicaexplorer.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
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
    private const val COLOR_PARADA = 0xFF00A08A.toInt()

    /** Colores de los puntos del mapa principal. */
    const val COLOR_COMERCIO = 0xFF00A08A.toInt()
    const val COLOR_LUGAR = 0xFF4098D7.toInt()

    fun comercioIcon(context: Context, categoria: String): Icon =
        IconFactory.getInstance(context).fromResource(markerResource(categoria))

    /**
     * Marcador circular numerado para las paradas de una ruta.
     * Se dibuja en un bitmap para no depender de un recurso por cada número.
     */
    fun numeroIcon(context: Context, numero: Int): Icon =
        IconFactory.getInstance(context).fromBitmap(numeroBitmap(context, numero))

    /**
     * Marcador circular de color para puntos genéricos (lugares/comercios) del mapa.
     */
    fun puntoIcon(context: Context, color: Int): Icon =
        IconFactory.getInstance(context).fromBitmap(puntoBitmap(context, color))

    private fun puntoBitmap(context: Context, color: Int): Bitmap {
        val size = (34 * context.resources.displayMetrics.density).toInt().coerceAtLeast(34)
        val centra = size / 2f

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val relleno = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        val borde = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = size * 0.09f
        }
        val centro = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            style = Paint.Style.FILL
        }

        val radio = centra - borde.strokeWidth
        canvas.drawCircle(centra, centra, radio, relleno)
        canvas.drawCircle(centra, centra, radio, borde)
        canvas.drawCircle(centra, centra, radio * 0.34f, centro)

        return bitmap
    }

    private fun numeroBitmap(context: Context, numero: Int): Bitmap {
        val size = (30 * context.resources.displayMetrics.density).toInt().coerceAtLeast(30)
        val centra = size / 2f

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val relleno = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_PARADA
            style = Paint.Style.FILL
        }
        val borde = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = size * 0.07f
        }
        // El número de paradas es dinámico: ajustamos el tamaño del texto a los dígitos.
        val factorTexto = when (numero.toString().length) {
            1 -> 0.46f
            2 -> 0.38f
            else -> 0.30f
        }
        val texto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = size * factorTexto
            typeface = Typeface.DEFAULT_BOLD
        }

        val radio = centra - borde.strokeWidth
        canvas.drawCircle(centra, centra, radio, relleno)
        canvas.drawCircle(centra, centra, radio, borde)

        val baseline = centra - (texto.descent() + texto.ascent()) / 2f
        canvas.drawText(numero.toString(), centra, baseline, texto)

        return bitmap
    }

    @DrawableRes
    private fun markerResource(categoria: String): Int = when {
        categoria.contains("restaurante", ignoreCase = true) -> R.drawable.ic_map_marker_comercio
        categoria.contains("cafeter", ignoreCase = true) -> R.drawable.ic_map_marker_comercio
        categoria.contains("hotel", ignoreCase = true) -> R.drawable.ic_map_marker_comercio
        categoria.contains("tienda", ignoreCase = true) -> R.drawable.ic_map_marker_comercio
        else -> R.drawable.ic_map_marker_comercio
    }
}
