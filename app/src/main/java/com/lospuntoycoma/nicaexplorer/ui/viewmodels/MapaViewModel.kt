package com.lospuntoycoma.nicaexplorer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lospuntoycoma.nicaexplorer.data.ApiRepository
import com.lospuntoycoma.nicaexplorer.model.Comercio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapaUiState(
    val isLoading: Boolean = true,
    val comercios: List<Comercio> = emptyList(),
    val error: String? = null
)

/** Carga los comercios activos y deja fuera coordenadas ausentes o imposibles. */
class MapaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MapaUiState())
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    init {
        cargarComercios()
    }

    private fun cargarComercios() {
        viewModelScope.launch {
            val result = ApiRepository.getComercios()
            result.onSuccess { comercios ->
                _uiState.value = MapaUiState(
                    isLoading = false,
                    comercios = comercios.filter { it.tieneCoordenadasValidas() }
                )
            }.onFailure {
                _uiState.value = MapaUiState(
                    isLoading = false,
                    error = "No se pudieron cargar los comercios para el mapa."
                )
            }
        }
    }
}

private fun Comercio.tieneCoordenadasValidas(): Boolean =
    latitud.isFinite() && longitud.isFinite() &&
        latitud in -90.0..90.0 && longitud in -180.0..180.0 &&
        !(latitud == 0.0 && longitud == 0.0)
