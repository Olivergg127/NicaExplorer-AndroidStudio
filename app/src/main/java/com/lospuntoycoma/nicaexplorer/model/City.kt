package com.lospuntoycoma.nicaexplorer.model

import androidx.compose.ui.graphics.vector.ImageVector

data class City(
    val id: String,
    val name: String,
    val description: String,
    val monumentCount: Int,
    val gradientStart: Long,
    val gradientEnd: Long,
    val icon: ImageVector? = null
)
