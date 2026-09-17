package com.transandina.flotilla.ui.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.EstadoCuenta
import com.transandina.flotilla.data.model.NuevaAlertaPayload
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.TipoAlerta
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.data.repository.AlertaRepository
import com.transandina.flotilla.data.repository.AuthRepository
import com.transandina.flotilla.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A quién va el aviso. */
sealed interface DestinoAviso {
    data object TodosLosConductores : DestinoAviso
    data object TodosLosUsuarios : DestinoAviso
    data class UnUsuario(val usuario: Usuario) : DestinoAviso
}

const val LIMITE_MENSAJE_AVISO = 500

data class EnviarAvisoUiState(
    /** Cuentas activas que pueden recibir el aviso, sin incluir a quien lo envía. */
    val destinatarios: List<Usuario> = emptyList(),
    val destino: DestinoAviso? = null,
    val mensaje: String = "",
    val cargando: Boolean = false,
    val enviando: Boolean = false,
    val error: String? = null,
    val enviado: Boolean = false
)

/**
 * Aviso de gerencia: una fila por destinatario en `alertas` con tipo
 * `gerencia` (política `alertas_insert`). Los ve el conductor en "Mis alertas".
 */
class EnviarAvisoViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository(),
    private val alertaRepository: AlertaRepository = AlertaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnviarAvisoUiState())
    val uiState: StateFlow<EnviarAvisoUiState> = _uiState

    init {
        cargarDestinatarios()
    }

    private fun cargarDestinatarios() {
        val idActual = authRepository.usuarioActualId()
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val activos = usuarioRepository.obtenerUsuarios()
                    .filter { it.estado == EstadoCuenta.activo && it.id != idActual }
                _uiState.update { it.copy(destinatarios = activos, cargando = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = "No se pudieron cargar los destinatarios") }
            }
        }
    }

    fun onDestinoChange(destino: DestinoAviso) = _uiState.update { it.copy(destino = destino, error = null) }

    fun onMensajeChange(valor: String) =
        _uiState.update { it.copy(mensaje = valor.take(LIMITE_MENSAJE_AVISO), error = null) }

    fun enviar() {
        val s = _uiState.value
        val mensaje = s.mensaje.trim()
        val usuarios = when (val destino = s.destino) {
            null -> {
                _uiState.update { it.copy(error = "Elige a quién va el aviso") }
                return
            }
            DestinoAviso.TodosLosConductores -> s.destinatarios.filter { it.rol == RolUsuario.conductor }
            DestinoAviso.TodosLosUsuarios -> s.destinatarios
            is DestinoAviso.UnUsuario -> listOf(destino.usuario)
        }
        if (mensaje.isEmpty()) {
            _uiState.update { it.copy(error = "Escribe el mensaje del aviso") }
            return
        }
        if (usuarios.isEmpty()) {
            _uiState.update { it.copy(error = "No hay destinatarios activos para ese grupo") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(enviando = true, error = null) }
            try {
                alertaRepository.enviarAvisos(
                    usuarios.map {
                        NuevaAlertaPayload(tipo = TipoAlerta.gerencia, usuarioId = it.id, mensaje = mensaje)
                    }
                )
                _uiState.update { it.copy(enviando = false, enviado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(enviando = false, error = "No se pudo enviar el aviso. Intenta de nuevo") }
            }
        }
    }
}
