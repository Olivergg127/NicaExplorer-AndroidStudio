package com.lospuntoycoma.nicaexplorer.data

import kotlinx.coroutines.delay

/**
 * Detecta cambios en la base de datos (vía /api/v1/version) y recarga el catálogo
 * sin que el usuario tenga que cerrar y volver a abrir la app.
 *
 * El backend actualiza `meta/catalogo` en cada alta/edición/borrado del contenido,
 * así que la versión cambia y aquí se dispara `SampleData.loadCatalog()`.
 */
object CatalogSync {

    private const val DEFAULT_INTERVAL_MS = 15_000L

    private var lastVersion: String? = null

    private var ciclo = 0

    /** Se ejecuta mientras la pantalla esté activa (LaunchedEffect). */
    suspend fun watch(intervalMs: Long = DEFAULT_INTERVAL_MS) {
        while (true) {
            checkForChanges()

            // Las valoraciones no cambian la versión del catálogo; se refrescan
            // periódicamente (~60 s) para que los "top" se actualicen solos.
            if (ciclo % 4 == 0) {
                ValoracionesRepository.refreshPublicas()
            }
            ciclo++

            delay(intervalMs)
        }
    }

    private suspend fun checkForChanges() {
        val version = ApiRepository.getCatalogVersion() ?: return

        if (version != lastVersion) {
            val isFirstCheck = lastVersion == null
            lastVersion = version
            if (!isFirstCheck) {
                SampleData.loadCatalog()
            }
        }
    }
}
