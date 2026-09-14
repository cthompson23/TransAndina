package com.transandina.flotilla.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.data.repository.ActualizarPerfilPayload
import com.transandina.flotilla.data.repository.AuthRepository
import com.transandina.flotilla.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val usuario: Usuario? = null,
    val cargando: Boolean = false,
    val editando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val guardadoExitoso: Boolean = false,
    // Campos en edición (borrador, separados del usuario ya guardado
    // para poder cancelar sin perder lo que había antes de editar).
    val nombreCompleto: String = "",
    val cedula: String = "",
    val telefono: String = "",
    val licenciaConducir: String = ""
)

class PerfilViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        val usuarioId = authRepository.usuarioActualId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val usuario = usuarioRepository.obtenerPerfil(usuarioId)
            _uiState.update { it.copy(usuario = usuario, cargando = false) }
        }
    }

    fun iniciarEdicion() {
        val usuario = _uiState.value.usuario ?: return
        _uiState.update {
            it.copy(
                editando = true,
                error = null,
                guardadoExitoso = false,
                nombreCompleto = usuario.nombreCompleto,
                cedula = usuario.cedula,
                telefono = usuario.telefono ?: "",
                licenciaConducir = usuario.licenciaConducir ?: ""
            )
        }
    }

    fun cancelarEdicion() {
        _uiState.update { it.copy(editando = false, error = null) }
    }

    fun onNombreChange(value: String) = _uiState.update { it.copy(nombreCompleto = value, error = null) }
    fun onCedulaChange(value: String) = _uiState.update { it.copy(cedula = value, error = null) }
    fun onTelefonoChange(value: String) = _uiState.update { it.copy(telefono = value, error = null) }
    fun onLicenciaChange(value: String) = _uiState.update { it.copy(licenciaConducir = value, error = null) }

    fun guardarCambios() {
        val estadoActual = _uiState.value
        val usuarioActual = estadoActual.usuario ?: return

        if (estadoActual.nombreCompleto.isBlank() || estadoActual.cedula.isBlank()) {
            _uiState.update { it.copy(error = "Nombre y cédula no pueden estar vacíos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                val payload = ActualizarPerfilPayload(
                    nombreCompleto = estadoActual.nombreCompleto.trim(),
                    cedula = estadoActual.cedula.trim(),
                    telefono = estadoActual.telefono.trim().ifBlank { null },
                    licenciaConducir = estadoActual.licenciaConducir.trim().ifBlank { null }
                )
                usuarioRepository.actualizarPerfil(usuarioActual.id, payload)

                _uiState.update {
                    it.copy(
                        guardando = false,
                        editando = false,
                        guardadoExitoso = true,
                        usuario = usuarioActual.copy(
                            nombreCompleto = payload.nombreCompleto,
                            cedula = payload.cedula,
                            telefono = payload.telefono,
                            licenciaConducir = payload.licenciaConducir
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(guardando = false, error = "No se pudo guardar. Intenta de nuevo")
                }
            }
        }
    }

    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            authRepository.cerrarSesion()
            alTerminar()
        }
    }
}
