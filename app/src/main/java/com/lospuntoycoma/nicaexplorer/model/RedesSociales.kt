package com.lospuntoycoma.nicaexplorer.model

import androidx.annotation.DrawableRes
import com.lospuntoycoma.nicaexplorer.R

/**
 * Catálogo de redes sociales que un comercio puede publicar y utilidades para
 * guardar/leer el formato `"red|valor"` (p. ej. `"facebook|https://facebook.com/x"`).
 */
object RedesSociales {

    data class Red(
        val clave: String,
        val nombre: String,
        val baseUrl: String,
        @DrawableRes val icono: Int
    )

    val disponibles: List<Red> = listOf(
        Red("facebook", "Facebook", "https://facebook.com/", R.drawable.ic_social_facebook),
        Red("x", "X (Twitter)", "https://x.com/", R.drawable.ic_social_x),
        Red("tiktok", "TikTok", "https://www.tiktok.com/@", R.drawable.ic_social_tiktok),
        Red("youtube", "YouTube", "https://youtube.com/@", R.drawable.ic_social_youtube),
        Red("instagram", "Instagram", "https://instagram.com/", R.drawable.ic_social_instagram)
    )

    fun red(clave: String): Red? =
        disponibles.firstOrNull { it.clave == clave.trim().lowercase() }

    fun claveDe(entrada: String): String = entrada.substringBefore('|').trim().lowercase()

    fun valorDe(entrada: String): String = entrada.substringAfter('|', "").trim()

    /** Devuelve la red y el valor de una entrada `"red|valor"`, o null si no es válida. */
    fun parsear(entrada: String): Pair<Red, String>? {
        val red = red(claveDe(entrada)) ?: return null
        val valor = valorDe(entrada)
        if (valor.isBlank()) return null
        return red to valor
    }

    /** URL final a abrir; completa el dominio si el usuario guardó solo su usuario. */
    fun urlDe(entrada: String): String {
        val red = red(claveDe(entrada)) ?: return ""
        val valor = valorDe(entrada)
        if (valor.startsWith("http://") || valor.startsWith("https://")) return valor
        return red.baseUrl + valor.trimStart('@')
    }

    /** Normaliza a `"clave|valor"`; null si faltan datos. */
    fun entrada(clave: String, valor: String): String? {
        val limpio = valor.trim()
        if (clave.isBlank() || limpio.isBlank()) return null
        return "${clave.trim().lowercase()}|$limpio"
    }
}
