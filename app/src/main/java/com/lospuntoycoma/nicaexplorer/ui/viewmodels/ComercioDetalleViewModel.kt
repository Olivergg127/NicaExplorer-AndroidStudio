package com.lospuntoycoma.nicaexplorer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lospuntoycoma.nicaexplorer.data.ApiRepository
import com.lospuntoycoma.nicaexplorer.data.FirebaseRepository
import com.lospuntoycoma.nicaexplorer.model.Comercio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la pantalla de detalle de un comercio.
 * isLoading: carga en curso.
 * comercio: comercio obtenido de Firestore (null si aún no se cargó o no existe).
 * error: mensaje de error si la lectura falló (null si todo está bien).
 */
data class ComercioDetalleUiState(
    val isLoading: Boolean = true,
    val comercio: Comercio? = null,
    val error: String? = null
)

/**
 * ViewModel para gestionar el estado del detalle de un comercio.
 */
class ComercioDetalleViewModel(
    private val comercioId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComercioDetalleUiState())
    val uiState: StateFlow<ComercioDetalleUiState> = _uiState.asStateFlow()

    init {
        loadComercio()
    }

    fun loadComercio() {
        viewModelScope.launch {
            _uiState.value = ComercioDetalleUiState(isLoading = true)
            val result = ApiRepository.getComercio(comercioId)
            result.onSuccess { comercio ->
                _uiState.value = ComercioDetalleUiState(
                    isLoading = false,
                    comercio = comercio
                )
            }.onFailure { _ ->
                // Respaldo: el propietario puede abrir su comercio aunque aún
                // no esté publicado o la API no responda.
                val fallback = FirebaseRepository.getComercioById(comercioId)
                fallback.onSuccess { comercio ->
                    _uiState.value = ComercioDetalleUiState(
                        isLoading = false,
                        comercio = comercio
                    )
                }.onFailure {
                    _uiState.value = ComercioDetalleUiState(
                        isLoading = false,
                        error = "No se pudo cargar el comercio. Revisa tu conexión e intenta de nuevo."
                    )
                }
            }
        }
    }
}
