package com.lospuntoycoma.nicaexplorer.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.lospuntoycoma.nicaexplorer.R

enum class Afluencia(
    @StringRes val displayNameRes: Int,
    val quietnessPriority: Int
) {
    BAJA(displayNameRes = R.string.afluencia_baja, quietnessPriority = 0),
    MODERADA(displayNameRes = R.string.afluencia_moderada, quietnessPriority = 1),
    ALTA(displayNameRes = R.string.afluencia_alta, quietnessPriority = 2)
}

data class Place(
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
    val imagenUrl: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val consejosResponsables: List<String> = emptyList()
)
