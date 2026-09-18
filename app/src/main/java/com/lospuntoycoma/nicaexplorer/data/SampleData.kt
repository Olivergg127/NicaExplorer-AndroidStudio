package com.lospuntoycoma.nicaexplorer.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lospuntoycoma.nicaexplorer.model.City
import com.lospuntoycoma.nicaexplorer.model.Place
import com.lospuntoycoma.nicaexplorer.model.RutaTuristica

/**
 * Catálogo dinámico de NicaExplorer.
 *
 * Toda la información (ciudades, lugares, rutas, imágenes y modelos 3D) se obtiene
 * de la API del backend (que administra Firestore). No hay datos hardcodeados: si la
 * API no responde, las listas quedan vacías.
 */
object SampleData {

    var cities: List<City> by mutableStateOf(emptyList())

    var placesByCity: Map<String, List<Place>> by mutableStateOf(emptyMap())

    var rutasByCity: Map<String, List<RutaTuristica>> by mutableStateOf(emptyMap())

    /** Hay una recarga del catálogo en curso (pull-to-refresh). */
    var isRefreshing: Boolean by mutableStateOf(false)
        private set

    val allPlaces: List<Place>
        get() = placesByCity.values.flatten()

    val recommendedPlaces: List<Place>
        get() = allPlaces.take(5)

    fun rutasDeCiudad(cityId: String): List<RutaTuristica> =
        rutasByCity[cityId.trim().lowercase()].orEmpty()

    /** Recarga el catálogo mostrando el indicador de pull-to-refresh. */
    suspend fun refresh() {
        if (isRefreshing) return
        isRefreshing = true
        try {
            loadCatalog()
        } finally {
            isRefreshing = false
        }
    }

    /** Carga todo el catálogo desde la API del backend. */
    suspend fun loadCatalog(): Boolean {
        return try {
            val loadedPlaces = ApiRepository.getLugares().groupBy { it.cityId }

            cities = ApiRepository.getCiudades()
                .map { city ->
                    if (loadedPlaces.containsKey(city.id)) {
                        city.copy(placeCount = loadedPlaces.getValue(city.id).size)
                    } else {
                        city
                    }
                }
                .sortedWith(compareBy({ if (it.orden == 0) Int.MAX_VALUE else it.orden }, { it.name }))

            placesByCity = loadedPlaces
            rutasByCity = ApiRepository.getRutas().groupBy { it.cityId }
            true
        } catch (_: Exception) {
            false
        }
    }
}
