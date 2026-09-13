package com.transandina.flotilla.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val autenticado: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun iniciarSesion() {
        val estadoActual = _uiState.value
        if (estadoActual.email.isBlank() || estadoActual.password.isBlank()) {
            _uiState.update { it.copy(error = "Completa correo y contraseña") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                authRepository.iniciarSesion(estadoActual.email, estadoActual.password)
                _uiState.update { it.copy(cargando = false, autenticado = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "Credenciales inválidas o sin conexión")
                }
            }
        }
    }
}
