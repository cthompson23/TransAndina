package com.transandina.flotilla.ui.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.Alerta
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.data.repository.AlertaRepository
import com.transandina.flotilla.data.repository.AuthRepository
import com.transandina.flotilla.data.repository.MantenimientoRepository
import com.transandina.flotilla.data.repository.VehiculoRepository
import com.transandina.flotilla.domain.AlertaFlotilla
import com.transandina.flotilla.domain.calcularAlertasFlotilla
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificacionesUiState(
    val vehiculo: Vehiculo? = null,
    /** Documentos y mantenimientos: se calculan, no están guardados. */
    val calculadas: List<AlertaFlotilla> = emptyList(),
    /** Avisos de la tabla `alertas`: gerencia, reasignaciones y confirmaciones. */
    val guardadas: List<Alerta> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
) {
    val vacio: Boolean get() = calculadas.isEmpty() && guardadas.isEmpty()
}

/**
 * "Mis alertas" del conductor (Figma `28:312`). Mezcla dos fuentes: lo que se
 * calcula sobre su vehículo (documentos y mantenimientos) y los avisos que sí
 * están guardados en `alertas` (docs/ADAPTACION_MOVIL.md §8).
 */
class NotificacionesViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val vehiculoRepository: VehiculoRepository = VehiculoRepository(),
    private val mantenimientoRepository: MantenimientoRepository = MantenimientoRepository(),
    private val alertaRepository: AlertaRepository = AlertaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacionesUiState())
    val uiState: StateFlow<NotificacionesUiState> = _uiState

    fun cargar() {
        val usuarioId = authRepository.usuarioActualId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                coroutineScope {
                    val vehiculo = async { vehiculoRepository.obtenerVehiculoAsignado(usuarioId) }
                    val guardadas = async { alertaRepository.obtenerMisAlertas(usuarioId) }
                    val frecuencias = async { mantenimientoRepository.obtenerFrecuencias() }

                    val asignado = vehiculo.await()
                    val mantenimientos = asignado
                        ?.let { mantenimientoRepository.obtenerPorVehiculo(it.id) }
                        .orEmpty()

                    _uiState.update {
                        it.copy(
                            vehiculo = asignado,
                            calculadas = calcularAlertasFlotilla(
                                vehiculos = listOfNotNull(asignado),
                                mantenimientos = mantenimientos,
                                frecuencias = frecuencias.await(),
                                // Las reasignaciones le llegan como aviso guardado.
                                reasignaciones = emptyList(),
                                nombresPorUsuario = emptyMap()
                            ),
                            guardadas = guardadas.await(),
                            cargando = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudieron cargar tus alertas. Revisa tu conexión.")
                }
            }
        }
    }
}
