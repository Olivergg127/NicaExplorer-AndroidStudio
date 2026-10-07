package com.lospuntoycoma.nicaexplorer.ui.viewmodels

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lospuntoycoma.nicaexplorer.R
import com.lospuntoycoma.nicaexplorer.data.SolicitudComercioRepository
import com.lospuntoycoma.nicaexplorer.model.SolicitudComercio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SolicitudComercioUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val validationErrors: Map<String, Int> = emptyMap(),
    @StringRes val errorMessage: Int? = null
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
                        errorMessage = R.string.error_enviar_solicitud
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

    private fun validate(solicitud: SolicitudComercio): Map<String, Int> {
        val errors = mutableMapOf<String, Int>()

        fun requireText(field: String, value: String, maxLength: Int) {
            when {
                value.isBlank() -> errors[field] = R.string.validacion_obligatorio
                value.trim().length > maxLength -> {
                    errors[field] = R.string.validacion_max_caracteres
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
            errors["redesSociales"] = R.string.validacion_max_caracteres
        }

        if (solicitud.correoResponsable.isNotBlank() &&
            !Patterns.EMAIL_ADDRESS.matcher(solicitud.correoResponsable.trim()).matches()
        ) {
            errors["correoResponsable"] = R.string.validacion_correo
        }

        if (solicitud.telefono.isNotBlank() && !isReasonablePhone(solicitud.telefono)) {
            errors["telefono"] = R.string.validacion_telefono
        }

        if (solicitud.whatsapp.isNotBlank() && !isReasonablePhone(solicitud.whatsapp)) {
            errors["whatsapp"] = R.string.validacion_whatsapp
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
