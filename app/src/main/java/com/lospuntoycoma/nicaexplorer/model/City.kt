package com.lospuntoycoma.nicaexplorer.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Ciudad del catálogo.
 *
 * Estructura (plantilla: Juigalpa):
 *  - identidad: [name], [lema], [description], [historia], [departamento]
 *  - imágenes: [imagenUrl] (portada) y [galeria] (carrusel)
 *  - ubicación/orden: [latitud], [longitud], [orden], [activo]
 *  - respaldo local: [imageRes] / [imageKey] (drawable de la app)
 */
data class City(
    val id: String,
    val name: String,
    val description: String,
    val placeCount: Int,
    val gradientStart: Long,
    val gradientEnd: Long,
    val icon: ImageVector? = null,
    val imageRes: Int? = null,
    val imageKey: String? = null,
    val imagenUrl: String? = null,
    val galeria: List<String> = emptyList(),
    val lema: String = "",
    val historia: String = "",
    val departamento: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val orden: Int = 0,
    val activo: Boolean = true
)
