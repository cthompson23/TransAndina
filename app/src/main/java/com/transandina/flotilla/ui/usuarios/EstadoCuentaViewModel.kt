package com.transandina.flotilla.ui.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.EstadoCuenta
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.data.repository.AuthRepository
import com.transandina.flotilla.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoCuentaUiState(
    val usuario: Usuario? = null,
    val seleccion: EstadoCuenta? = null,
    val esCuentaPropia: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val guardado: Boolean = false
) {
    /** Una cuenta desactivada no se puede reactivar, y nadie cambia su propio estado. */
    val bloqueada: Boolean
        get() = esCuentaPropia || usuario?.estado == EstadoCuenta.desactivado

    val hayCambios: Boolean
        get() = usuario != null && seleccion != null && seleccion != usuario.estado
}

/** Control de estado de cuenta (Figma `102:298`). */
class EstadoCuentaViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadoCuentaUiState())
    val uiState: StateFlow<EstadoCuentaUiState> = _uiState

    fun cargar(usuarioId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val usuario = usuarioRepository.obtenerPerfil(usuarioId)
                _uiState.update {
                    it.copy(
                        usuario = usuario,
                        seleccion = usuario?.estado,
                        esCuentaPropia = usuarioId == authRepository.usuarioActualId(),
                        cargando = false,
                        error = if (usuario == null) "No encontramos esa cuenta" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = "No se pudo cargar la cuenta") }
            }
        }
    }

    fun onSeleccionar(estado: EstadoCuenta) {
        if (_uiState.value.bloqueada) return
        _uiState.update { it.copy(seleccion = estado, error = null) }
    }

    /** Si se desactiva, la pantalla ya pidió confirmación antes de llamar aquí. */
    fun guardar() {
        val s = _uiState.value
        val usuario = s.usuario ?: return
        val nuevo = s.seleccion ?: return
        if (!s.hayCambios || s.bloqueada) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                usuarioRepository.cambiarEstado(usuario.id, nuevo)
                _uiState.update {
                    it.copy(guardando = false, guardado = true, usuario = usuario.copy(estado = nuevo))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = mensajeDeError(e)) }
            }
        }
    }

    /** Traduce los `raise exception` del trigger `proteger_campos_privilegiados_usuario`. */
    private fun mensajeDeError(e: Exception): String {
        val detalle = e.message.orEmpty()
        return when {
            detalle.contains("no se puede reactivar") -> "Una cuenta desactivada no se puede reactivar"
            detalle.contains("propia cuenta") -> "No puedes cambiar el estado de tu propia cuenta"
            detalle.contains("No tienes permiso") -> "Solo el encargado de flota puede cambiar el estado de una cuenta"
            else -> "No se pudo guardar el cambio. Intenta de nuevo"
        }
    }
}
