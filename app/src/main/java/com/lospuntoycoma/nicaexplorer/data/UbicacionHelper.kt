package com.lospuntoycoma.nicaexplorer.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.lospuntoycoma.nicaexplorer.model.City
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utilidades de ubicación para detectar la ciudad actual sin dependencias externas.
 * Usa el `LocationManager` del sistema (precisión de kilómetros suficiente para
 * identificar la ciudad más cercana).
 */
object UbicacionHelper {

    /** Radio máximo (km) para considerar que el usuario está en una ciudad conocida. */
    private const val RADIO_CIUDAD_KM = 25.0

    fun tienePermiso(context: Context): Boolean {
        val fina = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val gruesa = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        return fina == PackageManager.PERMISSION_GRANTED || gruesa == PackageManager.PERMISSION_GRANTED
    }

    /** Última ubicación conocida más reciente entre los proveedores disponibles. */
    suspend fun ultimaUbicacion(context: Context): Location? = withContext(Dispatchers.IO) {
        if (!tienePermiso(context)) return@withContext null

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null

        listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).mapNotNull { proveedor ->
            runCatching { manager.getLastKnownLocation(proveedor) }.getOrNull()
        }.maxByOrNull { it.time }
    }

    /**
     * Ciudad registrada más cercana dentro del radio permitido; null si el usuario
     * está lejos de todas las ciudades conocidas.
     */
    fun ciudadMasCercana(
        latitud: Double,
        longitud: Double,
        ciudades: List<City>,
        maxKm: Double = RADIO_CIUDAD_KM
    ): City? {
        val candidatas = ciudades.filter { it.latitud != null && it.longitud != null }
        val masCercana = candidatas.minByOrNull { ciudad ->
            distanciaKm(latitud, longitud, ciudad.latitud!!, ciudad.longitud!!)
        } ?: return null

        val distancia = distanciaKm(latitud, longitud, masCercana.latitud!!, masCercana.longitud!!)

        return if (distancia <= maxKm) masCercana else null
    }

    private fun distanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)

        return 2 * radioTierra * asin(min(1.0, sqrt(a)))
    }
}
