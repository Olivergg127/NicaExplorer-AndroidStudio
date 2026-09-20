package com.lospuntoycoma.nicaexplorer.model

/**
 * Categoría de comercio administrada desde el panel (colección `categorias_comercios`).
 */
data class CategoriaComercio(
    val id: String = "",
    val nombre: String = "",
    val categoriaPadre: String = "",
    val orden: Int = 0,
    val activo: Boolean = true
)
