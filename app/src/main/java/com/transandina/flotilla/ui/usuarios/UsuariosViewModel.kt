package com.transandina.flotilla.ui.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.data.repository.AuthRepository
import com.transandina.flotilla.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Chips de la lista; null en [rol] significa "Todos". */
enum class FiltroUsuarios(val rol: RolUsuario?) {
    TODOS(null),
    CONDUCTORES(RolUsuario.conductor),
    MECANICOS(RolUsuario.mecanico),
    ENCARGADOS(RolUsuario.encargado)
}

data class UsuariosUiState(
    val usuarios: List<Usuario> = emptyList(),
    val idActual: String? = null,
    val busqueda: String = "",
    val filtro: FiltroUsuarios = FiltroUsuarios.TODOS,
    val cargando: Boolean = false,
    val error: String? = null
) {
    fun conteo(filtro: FiltroUsuarios): Int =
        usuarios.count { filtro.rol == null || it.rol == filtro.rol }

    /** Busca por nombre, correo o cédula; en la cédula ignora guiones y espacios. */
    val visibles: List<Usuario>
        get() {
            val texto = busqueda.trim().lowercase()
            val soloDigitos = texto.filter(Char::isLetterOrDigit)
            return usuarios
                .filter { filtro.rol == null || it.rol == filtro.rol }
                .filter { usuario ->
                    texto.isEmpty() ||
                        usuario.nombreCompleto.lowercase().contains(texto) ||
                        usuario.email.lowercase().contains(texto) ||
                        (soloDigitos.isNotEmpty() &&
                            usuario.cedula.filter(Char::isLetterOrDigit).lowercase().contains(soloDigitos))
                }
        }
}

/** Consulta de cuentas del encargado (Figma `102:258`). */
class UsuariosViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuariosUiState())
    val uiState: StateFlow<UsuariosUiState> = _uiState

    fun cargar() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(cargando = true, error = null, idActual = authRepository.usuarioActualId())
            }
            try {
                val usuarios = usuarioRepository.obtenerUsuarios()
                _uiState.update { it.copy(usuarios = usuarios, cargando = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudieron cargar las cuentas. Revisa tu conexión.")
                }
            }
        }
    }

    fun onBusquedaChange(valor: String) = _uiState.update { it.copy(busqueda = valor) }

    fun onFiltroChange(filtro: FiltroUsuarios) = _uiState.update { it.copy(filtro = filtro) }
}
