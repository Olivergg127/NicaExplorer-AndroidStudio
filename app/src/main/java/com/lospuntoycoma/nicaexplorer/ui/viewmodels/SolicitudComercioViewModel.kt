package com.lospuntoycoma.nicaexplorer.ui.viewmodels

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lospuntoycoma.nicaexplorer.data.SolicitudComercioRepository
import com.lospuntoycoma.nicaexplorer.model.SolicitudComercio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SolicitudComercioUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap(),
    val errorMessage: String? = null
)

class SolicitudComercioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SolicitudComercioUiState())
    val uiState: StateFlow<SolicitudComercioUiState> = _uiState.asStateFlow()

    fun enviarSolicitud(solicitud: SolicitudComercio) {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isSuccess) return

        val validationErrors = validate(solicitud)
        if (validationErrors.isNotEmpty()) {
            _uiState.value = currentState.copy(
                validationErrors = validationErrors,
                errorMessage = null
            )
            return
        }

        // Se actualiza antes de lanzar la corrutina para bloquear toques repetidos.
        _uiState.value = SolicitudComercioUiState(isLoading = true)

        viewModelScope.launch {
            SolicitudComercioRepository.enviarSolicitud(solicitud).fold(
                onSuccess = {
                    _uiState.value = SolicitudComercioUiState(isSuccess = true)
                },
                onFailure = {
                    _uiState.value = SolicitudComercioUiState(
                        errorMessage = "No se pudo enviar la solicitud. Inténtalo nuevamente más tarde."
                    )
                }
            )
        }
    }

    fun clearValidationError(field: String) {
        val currentState = _uiState.value
        if (field !in currentState.validationErrors && currentState.errorMessage == null) return

        _uiState.value = currentState.copy(
            validationErrors = currentState.validationErrors - field,
            errorMessage = null
        )
    }

    private fun validate(solicitud: SolicitudComercio): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        fun requireText(field: String, value: String, maxLength: Int) {
            when {
                value.isBlank() -> errors[field] = "Este campo es obligatorio"
                value.trim().length > maxLength -> {
                    errors[field] = "Máximo $maxLength caracteres"
                }
            }
        }

        requireText("nombreNegocio", solicitud.nombreNegocio, 120)
        requireText("ciudad", solicitud.ciudad, 80)
        requireText("cityId", solicitud.cityId, 80)
        requireText("categoria", solicitud.categoria, 80)
        requireText("direccion", solicitud.direccion, 240)
        requireText("descripcion", solicitud.descripcion, 2000)
        requireText("telefono", solicitud.telefono, 25)
        requireText("whatsapp", solicitud.whatsapp, 25)
        requireText("horario", solicitud.horario, 240)
        requireText("nombreResponsable", solicitud.nombreResponsable, 120)
        requireText("correoResponsable", solicitud.correoResponsable, 254)

        if (solicitud.redesSociales.trim().length > 500) {
            errors["redesSociales"] = "Máximo 500 caracteres"
        }

        if (solicitud.correoResponsable.isNotBlank() &&
            !Patterns.EMAIL_ADDRESS.matcher(solicitud.correoResponsable.trim()).matches()
        ) {
            errors["correoResponsable"] = "Ingresa un correo válido"
        }

        if (solicitud.telefono.isNotBlank() && !isReasonablePhone(solicitud.telefono)) {
            errors["telefono"] = "Ingresa un teléfono válido de 7 a 15 dígitos"
        }

        if (solicitud.whatsapp.isNotBlank() && !isReasonablePhone(solicitud.whatsapp)) {
            errors["whatsapp"] = "Ingresa un WhatsApp válido de 7 a 15 dígitos"
        }

        return errors
    }

    private fun isReasonablePhone(value: String): Boolean {
        val trimmed = value.trim()
        val digits = trimmed.count(Char::isDigit)
        return trimmed.length <= 25 &&
            digits in 7..15 &&
            trimmed.matches(Regex("^[+0-9() .-]+$"))
    }
}
