package com.transandina.flotilla.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.domain.esCedulaValida
import com.transandina.flotilla.domain.esCorreoValido
import com.transandina.flotilla.domain.esTelefonoValido
import com.transandina.flotilla.data.repository.AuthRepository
import com.transandina.flotilla.data.repository.NuevoUsuarioPayload
import com.transandina.flotilla.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUiState(
    val nombreCompleto: String = "",
    val cedula: String = "",
    val email: String = "",
    val telefono: String = "",
    val licenciaConducir: String = "",
    val rol: RolUsuario = RolUsuario.conductor,
    val password: String = "",
    val confirmarPassword: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val registroExitoso: Boolean = false
)

class RegistroViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState

    fun onNombreChange(value: String) = _uiState.update { it.copy(nombreCompleto = value, error = null) }
    fun onCedulaChange(value: String) = _uiState.update { it.copy(cedula = value, error = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onTelefonoChange(value: String) = _uiState.update { it.copy(telefono = value, error = null) }
    fun onLicenciaChange(value: String) = _uiState.update { it.copy(licenciaConducir = value, error = null) }
    fun onRolChange(value: RolUsuario) = _uiState.update { it.copy(rol = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onConfirmarPasswordChange(value: String) =
        _uiState.update { it.copy(confirmarPassword = value, error = null) }

    fun registrar() {
        val s = _uiState.value

        val error = validar(s)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val email = s.email.trim()
                // Si un intento anterior creó la cuenta de Auth pero falló al
                // guardar el perfil, la sesión quedó abierta con ese mismo
                // correo. Registrarla de nuevo la rechazaría por duplicada,
                // así que se reaprovecha y solo se crea el perfil.
                val sesionDelMismoCorreo = authRepository.usuarioActualId()
                    ?.takeIf { authRepository.usuarioActualEmail().equals(email, ignoreCase = true) }

                if (sesionDelMismoCorreo == null) {
                    authRepository.registrarUsuario(email, s.password)
                }

                val usuarioId = authRepository.usuarioActualId()
                if (usuarioId == null) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = "Cuenta creada. Revisa tu correo para confirmarla y luego inicia sesión."
                        )
                    }
                    return@launch
                }

                usuarioRepository.crearPerfil(
                    NuevoUsuarioPayload(
                        id = usuarioId,
                        nombreCompleto = s.nombreCompleto.trim(),
                        cedula = s.cedula.trim(),
                        email = s.email.trim(),
                        telefono = s.telefono.trim().ifBlank { null },
                        licenciaConducir = if (s.rol == RolUsuario.conductor) {
                            s.licenciaConducir.trim().ifBlank { null }
                        } else {
                            null
                        },
                        rol = s.rol
                    )
                )

                _uiState.update { it.copy(cargando = false, registroExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = mensajeDeError(e)) }
            }
        }
    }

    /**
     * Traduce lo que devuelve Supabase. Antes todo caía en un mismo mensaje
     * genérico y no había forma de saber si el problema era el correo
     * repetido, la cédula repetida o la conexión.
     */
    private fun mensajeDeError(e: Exception): String {
        val detalle = e.message.orEmpty()
        return when {
            detalle.contains("usuarios_cedula_key") ->
                "Ya existe una cuenta con esa cédula"
            detalle.contains("usuarios_email_key") ->
                "Ya existe una cuenta con ese correo"
            detalle.contains("already registered", ignoreCase = true) ||
                detalle.contains("already exists", ignoreCase = true) ||
                detalle.contains("user_already", ignoreCase = true) ->
                "Ya existe una cuenta con ese correo. Inicia sesión o usa otro."
            detalle.contains("rate limit", ignoreCase = true) ||
                detalle.contains("too many", ignoreCase = true) ->
                "Supabase está limitando los registros. Espera un minuto y vuelve a intentar."
            detalle.contains("password", ignoreCase = true) ->
                "La contraseña no cumple los requisitos de seguridad"
            detalle.contains("email", ignoreCase = true) &&
                detalle.contains("invalid", ignoreCase = true) ->
                "Supabase rechazó ese correo. Prueba con otro dominio."
            detalle.contains("row-level security", ignoreCase = true) ->
                "La cuenta se creó, pero no se pudo guardar el perfil. Contacta al encargado de flota."
            else -> "No se pudo crear la cuenta. Verifica los datos o tu conexión."
        }
    }

    private fun validar(s: RegistroUiState): String? {
        if (s.nombreCompleto.isBlank() || s.cedula.isBlank() || s.email.isBlank() || s.telefono.isBlank()) {
            return "Completa todos los campos obligatorios"
        }
        if (!esCorreoValido(s.email)) {
            return "Escribe un correo válido, como nombre@correo.com"
        }
        if (!esCedulaValida(s.cedula)) {
            return "La cédula debe tener entre 9 y 12 dígitos, sin guiones ni espacios"
        }
        if (!esTelefonoValido(s.telefono)) {
            return "El teléfono debe tener al menos 8 dígitos"
        }
        if (s.rol == RolUsuario.conductor && s.licenciaConducir.isBlank()) {
            return "La licencia de conducir es obligatoria para conductores"
        }
        if (s.password.length < 6) {
            return "La contraseña debe tener al menos 6 caracteres"
        }
        if (s.password != s.confirmarPassword) {
            return "Las contraseñas no coinciden"
        }
        return null
    }
}
