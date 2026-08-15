package com.lospuntoycoma.nicaexplorer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lospuntoycoma.nicaexplorer.data.FirebaseRepository
import com.lospuntoycoma.nicaexplorer.model.Comercio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la sección "Comercios y restaurantes".
 * isLoading: carga en curso.
 * comercios: lista de comercios activos obtenidos de Firestore.
 * error: mensaje de error si la lectura falló (null si todo está bien).
 */
data class ComerciosUiState(
    val isLoading: Boolean = true,
    val comercios: List<Comercio> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para gestionar el estado de los comercios.
 */
class ComerciosViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ComerciosUiState())
    val uiState: StateFlow<ComerciosUiState> = _uiState.asStateFlow()

    init {
        loadComercios()
    }

    fun loadComercios() {
        viewModelScope.launch {
            _uiState.value = ComerciosUiState(isLoading = true)
            val result = FirebaseRepository.getComerciosActivos()
            result.onSuccess { comercios ->
                _uiState.value = ComerciosUiState(
                    isLoading = false,
                    comercios = comercios
                )
            }.onFailure { _ ->
                _uiState.value = ComerciosUiState(
                    isLoading = false,
                    error = "No se pudieron cargar los comercios. Revisa tu conexión e intenta de nuevo."
                )
            }
        }
    }
}
