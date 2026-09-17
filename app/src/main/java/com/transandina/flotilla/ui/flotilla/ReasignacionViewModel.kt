package com.transandina.flotilla.ui.flotilla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.EstadoCuenta
import com.transandina.flotilla.data.model.ReasignarConductorParams
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.data.repository.ReasignacionRepository
import com.transandina.flotilla.data.repository.UsuarioRepository
import com.transandina.flotilla.data.repository.VehiculoRepository
import com.transandina.flotilla.domain.aFechaIso
import com.transandina.flotilla.domain.formatearFechaIso
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Una fila de "Reasignaciones recientes", con los nombres ya resueltos. */
data class FilaReasignacion(
    val anterior: String?,
    val nuevo: String?,
    val fecha: String,
    val motivo: String?
)

data class ReasignacionUiState(
    val vehiculo: Vehiculo? = null,
    val conductorActual: Usuario? = null,
    /** Conductores activos sin vehículo. */
    val disponibles: List<Usuario> = emptyList(),
    /** Null = sin elegir. [quitarConductor] = dejar el vehículo sin conductor. */
    val seleccionado: Usuario? = null,
    val quitarConductor: Boolean = false,
    val fechaEfectiva: LocalDate = LocalDate.now(),
    val motivo: String = "",
    val historial: List<FilaReasignacion> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val reasignado: Boolean = false
) {
    val haySeleccion: Boolean get() = seleccionado != null || quitarConductor
}

class ReasignacionViewModel(
    private val vehiculoRepository: VehiculoRepository = VehiculoRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository(),
    private val reasignacionRepository: ReasignacionRepository = ReasignacionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReasignacionUiState())
    val uiState: StateFlow<ReasignacionUiState> = _uiState

    fun cargar(vehiculoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                coroutineScope {
                    val vehiculo = async { vehiculoRepository.obtenerVehiculoPorId(vehiculoId) }
                    val flotilla = async { vehiculoRepository.obtenerFlotilla() }
                    val usuarios = async { usuarioRepository.obtenerUsuarios() }
                    val historial = async { reasignacionRepository.obtenerPorVehiculo(vehiculoId) }

                    val nombres = usuarios.await().associate { it.id to it.nombreCompleto }
                    val conDueno = flotilla.await()
                        .filter { it.activo }
                        .mapNotNull { it.conductorId }
                        .toSet()
                    val actual = vehiculo.await()

                    _uiState.update {
                        it.copy(
                            vehiculo = actual,
                            conductorActual = usuarios.await().find { u -> u.id == actual?.conductorId },
                            disponibles = usuarios.await().filter { u ->
                                u.rol == RolUsuario.conductor &&
                                    u.estado == EstadoCuenta.activo &&
                                    u.id !in conDueno
                            },
                            historial = historial.await().map { r ->
                                FilaReasignacion(
                                    anterior = r.conductorAnteriorId?.let { id -> nombres[id] },
                                    nuevo = r.conductorNuevoId?.let { id -> nombres[id] },
                                    fecha = formatearFechaIso(r.fechaEfectiva),
                                    motivo = r.motivo
                                )
                            },
                            cargando = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudo cargar la información del vehículo")
                }
            }
        }
    }

    fun onSeleccionarConductor(usuario: Usuario) =
        _uiState.update { it.copy(seleccionado = usuario, quitarConductor = false, error = null) }

    fun onQuitarConductor() =
        _uiState.update { it.copy(seleccionado = null, quitarConductor = true, error = null) }

    fun onFechaChange(fecha: LocalDate) = _uiState.update { it.copy(fechaEfectiva = fecha, error = null) }

    fun onMotivoChange(valor: String) = _uiState.update { it.copy(motivo = valor, error = null) }

    /** La pantalla ya pidió confirmación antes de llamar aquí. */
    fun confirmar() {
        val s = _uiState.value
        val vehiculo = s.vehiculo ?: return
        if (!s.haySeleccion) {
            _uiState.update { it.copy(error = "Selecciona el nuevo conductor") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                vehiculoRepository.reasignarConductor(
                    ReasignarConductorParams(
                        vehiculoId = vehiculo.id,
                        conductorNuevoId = s.seleccionado?.id,
                        fechaEfectiva = aFechaIso(s.fechaEfectiva),
                        motivo = s.motivo.trim().ifBlank { null }
                    )
                )
                _uiState.update { it.copy(guardando = false, reasignado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = mensajeDeError(e)) }
            }
        }
    }

    /** Traduce los `raise exception` de la función `reasignar_conductor`. */
    private fun mensajeDeError(e: Exception): String {
        val detalle = e.message.orEmpty()
        return when {
            detalle.contains("ya tiene un vehículo asignado") -> "Ese conductor ya tiene un vehículo asignado"
            detalle.contains("cuenta activa") -> "El conductor elegido no está activo"
            detalle.contains("ya está asignado") -> "El vehículo ya está asignado a ese conductor"
            detalle.contains("Solo el encargado") -> "Solo el encargado de flota puede reasignar vehículos"
            else -> "No se pudo reasignar. Intenta de nuevo"
        }
    }
}
