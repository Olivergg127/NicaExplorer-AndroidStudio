package com.lospuntoycoma.nicaexplorer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lospuntoycoma.nicaexplorer.data.ApiRepository
import com.lospuntoycoma.nicaexplorer.data.FirebaseRepository
import com.lospuntoycoma.nicaexplorer.model.CategoriaComercio
import com.lospuntoycoma.nicaexplorer.model.Comercio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ComercioFormUiState(
    val isLoading: Boolean = true,
    val saving: Boolean = false,
    val comercioExistente: Comercio? = null,
    val categorias: List<CategoriaComercio> = emptyList(),
    val error: String? = null,
    val guardadoOk: Boolean = false
)

/**
 * Gestiona la carga y el guardado de un comercio propio. Si `comercioId` es
 * null se trata de una creación (nace pendiente de aprobación).
 */
class ComercioFormViewModel(
    private val comercioId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComercioFormUiState())
    val uiState: StateFlow<ComercioFormUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            val categorias = runCatching { ApiRepository.getCategoriasComercios() }
                .getOrDefault(emptyList())

            var existente: Comercio? = null
            var error: String? = null

            if (!comercioId.isNullOrBlank()) {
                FirebaseRepository.getComercioById(comercioId)
                    .onSuccess { comercio ->
                        // Solo el propietario puede editar su comercio.
                        val uid = FirebaseRepository.getCurrentUser()?.uid
                        if (comercio.propietarioUid.isNotBlank() && comercio.propietarioUid != uid) {
                            error = "Este comercio no pertenece a tu cuenta."
                        } else {
                            existente = comercio
                        }
                    }
                    .onFailure { error = "No se pudo cargar el comercio." }
            }

            _uiState.value = ComercioFormUiState(
                isLoading = false,
                comercioExistente = existente,
                categorias = categorias,
                error = error
            )
        }
    }

    fun guardar(comercio: Comercio) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true, error = null)

            val result = if (comercioId.isNullOrBlank()) {
                FirebaseRepository.crearComercio(comercio).map { }
            } else {
                FirebaseRepository.actualizarComercio(comercio.copy(id = comercioId))
            }

            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(saving = false, guardadoOk = true)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        saving = false,
                        error = it.localizedMessage ?: "No se pudo guardar el comercio."
                    )
                }
        }
    }
}
