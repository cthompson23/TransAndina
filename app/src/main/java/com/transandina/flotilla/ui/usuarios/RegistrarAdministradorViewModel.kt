package com.transandina.flotilla.ui.usuarios

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

data class RegistrarAdministradorUiState(
    val nombreCompleto: String = "",
    val cedula: String = "",
    val email: String = "",
    val telefono: String = "",
    val password: String = "",
    val confirmarPassword: String = "",
    val guardando: Boolean = false,
    val error: String? = null,
    val registrado: Boolean = false
)

/**
 * Registro de un encargado de flota por otro encargado (Figma `102:278`).
 *
 * La cuenta de Auth se crea con un cliente temporal para no cerrar la sesión
 * actual, y luego se guarda el perfil con rol encargado (política
 * `usuarios_insert_encargado`, migración 202609171900).
 */
class RegistrarAdministradorViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrarAdministradorUiState())
    val uiState: StateFlow<RegistrarAdministradorUiState> = _uiState

    fun onNombreChange(valor: String) = _uiState.update { it.copy(nombreCompleto = valor, error = null) }
    fun onCedulaChange(valor: String) = _uiState.update { it.copy(cedula = valor, error = null) }
    fun onEmailChange(valor: String) = _uiState.update { it.copy(email = valor, error = null) }
    fun onTelefonoChange(valor: String) = _uiState.update { it.copy(telefono = valor, error = null) }
    fun onPasswordChange(valor: String) = _uiState.update { it.copy(password = valor, error = null) }
    fun onConfirmarPasswordChange(valor: String) =
        _uiState.update { it.copy(confirmarPassword = valor, error = null) }

    fun registrar() {
        val s = _uiState.value
        val error = validar(s)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        val email = s.email.trim().lowercase()
        val cedula = s.cedula.trim()

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                // Se revisa antes de crear la cuenta de Auth: si el perfil
                // fallara después, quedaría una cuenta sin perfil.
                val existentes = usuarioRepository.obtenerUsuarios()
                val duplicado = when {
                    existentes.any { it.email.equals(email, ignoreCase = true) } ->
                        "Ya existe una cuenta con ese correo"
                    existentes.any { it.cedula == cedula } ->
                        "Ya existe una cuenta con esa cédula"
                    else -> null
                }
                if (duplicado != null) {
                    _uiState.update { it.copy(guardando = false, error = duplicado) }
                    return@launch
                }

                val nuevoId = authRepository.crearCuentaParaOtraPersona(email, s.password)
                usuarioRepository.guardarPerfilDeOtraPersona(
                    NuevoUsuarioPayload(
                        id = nuevoId,
                        nombreCompleto = s.nombreCompleto.trim(),
                        cedula = cedula,
                        email = email,
                        telefono = s.telefono.trim().ifBlank { null },
                        licenciaConducir = null,
                        rol = RolUsuario.encargado
                    )
                )
                _uiState.update { it.copy(guardando = false, registrado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = mensajeDeError(e)) }
            }
        }
    }

    private fun validar(s: RegistrarAdministradorUiState): String? = when {
        s.nombreCompleto.isBlank() || s.cedula.isBlank() || s.email.isBlank() || s.telefono.isBlank() ->
            "Completa todos los campos obligatorios"
        !esCorreoValido(s.email) -> "Escribe un correo válido, como nombre@correo.com"
        !esCedulaValida(s.cedula) ->
            "La cédula debe tener entre 9 y 12 dígitos, sin guiones ni espacios"
        !esTelefonoValido(s.telefono) -> "El teléfono debe tener al menos 8 dígitos"
        s.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        s.password != s.confirmarPassword -> "Las contraseñas no coinciden"
        else -> null
    }

    private fun mensajeDeError(e: Exception): String {
        val detalle = e.message.orEmpty()
        return when {
            detalle.contains("already registered", ignoreCase = true) ||
                detalle.contains("already exists", ignoreCase = true) ->
                "Ya existe una cuenta con ese correo"
            detalle.contains("password", ignoreCase = true) ->
                "La contraseña no cumple los requisitos de seguridad"
            detalle.contains("usuarios_cedula_key") -> "Ya existe una cuenta con esa cédula"
            detalle.contains("row-level security", ignoreCase = true) ->
                "Solo un encargado de flota activo puede registrar administradores"
            else -> "No se pudo registrar la cuenta. Verifica los datos o tu conexión"
        }
    }
}
