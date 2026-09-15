package com.transandina.flotilla.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NuevaContrasenaUiState(
    val password: String = "",
    val confirmarPassword: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val actualizada: Boolean = false
)

class NuevaContrasenaViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NuevaContrasenaUiState())
    val uiState: StateFlow<NuevaContrasenaUiState> = _uiState

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onConfirmarPasswordChange(value: String) =
        _uiState.update { it.copy(confirmarPassword = value, error = null) }

    fun guardarNuevaContrasena() {
        val s = _uiState.value
        if (s.password.length < 6) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }
        if (s.password != s.confirmarPassword) {
            _uiState.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                authRepository.actualizarContrasena(s.password)
                // Cerramos la sesión de recuperación a propósito: así el
                // usuario entra "limpio" con su contraseña nueva, en vez
                // de quedar en un estado raro con una sesión que la app
                // (rolActual en MainActivity) no reconoce como logueada.
                authRepository.cerrarSesion()
                _uiState.update { it.copy(cargando = false, actualizada = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudo actualizar. El enlace pudo haber expirado, solicita uno nuevo."
                    )
                }
            }
        }
    }
}
