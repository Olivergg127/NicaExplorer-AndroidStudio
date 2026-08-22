package com.lospuntoycoma.nicaexplorer.model

import androidx.compose.ui.graphics.vector.ImageVector

data class Monument(
    val id: String,
    val name: String,
    val city: String,
    val cityId: String,
    val category: String,
    val description: String,
    val history: String,
    val yearBuilt: String,
    val modeloUnity: String = "",
    val gradientStart: Long,
    val gradientEnd: Long,
    val icon: ImageVector? = null,
    val consejosResponsables: List<String> = emptyList()
)
