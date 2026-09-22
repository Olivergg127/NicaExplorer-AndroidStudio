package com.lospuntoycoma.nicaexplorer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lospuntoycoma.nicaexplorer.data.FirebaseRepository
import com.lospuntoycoma.nicaexplorer.model.Comercio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MisComerciosUiState(
    val isLoading: Boolean = true,
    val comercios: List<Comercio> = emptyList(),
    val error: String? = null,
    val deletingId: String? = null
)

/**
 * Estado de "Mis comercios": lista los comercios del usuario autenticado
 * (incluye pendientes e inactivos) y permite eliminarlos.
 */
class MisComerciosViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MisComerciosUiState())
    val uiState: StateFlow<MisComerciosUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        val uid = FirebaseRepository.getCurrentUser()?.uid
        if (uid == null) {
            _uiState.value = MisComerciosUiState(
                isLoading = false,
                error = "Inicia sesión para administrar tus comercios."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            FirebaseRepository.getComerciosDeUsuario(uid)
                .onSuccess { comercios ->
                    _uiState.value = MisComerciosUiState(
                        isLoading = false,
                        comercios = comercios.sortedBy { it.nombre.lowercase() }
                    )
                }
                .onFailure {
                    _uiState.value = MisComerciosUiState(
                        isLoading = false,
                        error = "No se pudieron cargar tus comercios. Revisa tu conexión."
                    )
                }
        }
    }

    fun eliminar(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(deletingId = id)
            FirebaseRepository.eliminarComercio(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        deletingId = null,
                        comercios = _uiState.value.comercios.filterNot { it.id == id }
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        deletingId = null,
                        error = "No se pudo eliminar el comercio."
                    )
                }
        }
    }
}
