package com.transandina.flotilla.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecuperarContrasenaUiState(
    val email: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val correoEnviado: Boolean = false
)

class RecuperarContrasenaViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarContrasenaUiState())
    val uiState: StateFlow<RecuperarContrasenaUiState> = _uiState

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }

    fun enviarCorreo() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa tu correo") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                authRepository.enviarCorreoRecuperacion(email)
                _uiState.update { it.copy(cargando = false, correoEnviado = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudo enviar el correo. Verifica que esté bien escrito.")
                }
            }
        }
    }
}
