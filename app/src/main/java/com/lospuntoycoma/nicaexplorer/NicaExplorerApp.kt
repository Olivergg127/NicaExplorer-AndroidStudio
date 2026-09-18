package com.lospuntoycoma.nicaexplorer

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * Configura Coil con caché en memoria y en disco, de modo que las imágenes ya
 * descargadas se reutilicen al salir y volver a una lista, sin volver a pedirlas
 * al servidor.
 */
class NicaExplorerApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(512L * 1024 * 1024)
                    .build()
            }
            // Conserva las imágenes en caché aunque el servidor no envíe cabeceras de caché.
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
}
