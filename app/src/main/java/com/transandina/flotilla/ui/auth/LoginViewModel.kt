package com.transandina.flotilla.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.EstadoCuenta
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.repository.AuthRepository
import com.transandina.flotilla.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val autenticado: Boolean = false,
    val rol: RolUsuario? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
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

                // Necesitamos el rol ANTES de navegar, para saber qué
                // pantalla y qué menú mostrarle a este usuario.
                val usuarioId = authRepository.usuarioActualId()
                val perfil = usuarioId?.let { usuarioRepository.obtenerPerfil(it) }

                if (perfil == null) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = "Tu cuenta no tiene un perfil registrado. Contacta al encargado de flota."
                        )
                    }
                    return@launch
                }

                // Supabase Auth deja entrar a una cuenta suspendida (la
                // contraseña es válida); la base ya no le devuelve datos, así
                // que se cierra la sesión y se explica el motivo.
                if (perfil.estado != EstadoCuenta.activo) {
                    authRepository.cerrarSesion()
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = if (perfil.estado == EstadoCuenta.suspendido) {
                                "Tu cuenta está suspendida. Contacta al encargado de flota."
                            } else {
                                "Tu cuenta fue desactivada. Contacta al encargado de flota."
                            }
                        )
                    }
                    return@launch
                }

                _uiState.update { it.copy(cargando = false, autenticado = true, rol = perfil.rol) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "Credenciales inválidas o sin conexión")
                }
            }
        }
    }
}
