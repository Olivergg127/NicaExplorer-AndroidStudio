package com.lospuntoycoma.nicaexplorer.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class Afluencia(
    val displayName: String,
    val quietnessPriority: Int
) {
    BAJA(displayName = "Baja", quietnessPriority = 0),
    MODERADA(displayName = "Moderada", quietnessPriority = 1),
    ALTA(displayName = "Alta", quietnessPriority = 2)
}

data class Monument(
    val id: String,
    val name: String,
    val city: String,
    val cityId: String,
    val category: String,
    val afluencia: Afluencia,
    val description: String,
    val history: String,
    val yearBuilt: String,
    val modeloUnity: String = "",
    val gradientStart: Long,
    val gradientEnd: Long,
    val icon: ImageVector? = null,
    val imageRes: Int? = null,
    val imageKey: String? = null,
    val consejosResponsables: List<String> = emptyList()
)
