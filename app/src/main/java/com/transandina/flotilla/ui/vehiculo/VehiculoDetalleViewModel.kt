package com.transandina.flotilla.ui.vehiculo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.KilometrajeHistorico
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.data.repository.KilometrajeRepository
import com.transandina.flotilla.data.repository.MantenimientoRepository
import com.transandina.flotilla.data.repository.UsuarioRepository
import com.transandina.flotilla.data.repository.VehiculoRepository
import com.transandina.flotilla.domain.ProximoMantenimiento
import com.transandina.flotilla.domain.calcularProximosMantenimientos
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VehiculoDetalleUiState(
    val vehiculo: Vehiculo? = null,
    val conductor: Usuario? = null,
    val historialKilometraje: List<KilometrajeHistorico> = emptyList(),
    val mantenimientos: List<Mantenimiento> = emptyList(),
    val proximosMantenimientos: List<ProximoMantenimiento> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

/**
 * Detalle de un vehículo cualquiera, identificado por su id. No busca "el
 * vehículo del conductor": así el encargado puede abrir la misma pantalla
 * para cualquier unidad de la flotilla (Fase 4).
 */
class VehiculoDetalleViewModel(
    private val vehiculoRepository: VehiculoRepository = VehiculoRepository(),
    private val kilometrajeRepository: KilometrajeRepository = KilometrajeRepository(),
    private val mantenimientoRepository: MantenimientoRepository = MantenimientoRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculoDetalleUiState())
    val uiState: StateFlow<VehiculoDetalleUiState> = _uiState

    fun cargar(vehiculoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val vehiculo = vehiculoRepository.obtenerVehiculoPorId(vehiculoId)
                if (vehiculo == null) {
                    _uiState.update { it.copy(vehiculo = null, cargando = false) }
                    return@launch
                }

                // Lo que sigue depende de permisos distintos (RLS): si una
                // lectura falla, la pantalla igual muestra el resto de los datos.
                coroutineScope {
                    val historial = async {
                        runCatching { kilometrajeRepository.obtenerHistorial(vehiculoId) }
                            .getOrDefault(emptyList())
                    }
                    val mantenimientos = async {
                        runCatching { mantenimientoRepository.obtenerPorVehiculo(vehiculoId) }
                            .getOrDefault(emptyList())
                    }
                    val frecuencias = async {
                        runCatching { mantenimientoRepository.obtenerFrecuencias() }
                            .getOrDefault(emptyList())
                    }
                    // El conductor solo puede leer su propia fila; el encargado, todas.
                    val conductor = async {
                        vehiculo.conductorId?.let { id ->
                            runCatching { usuarioRepository.obtenerPerfil(id) }.getOrNull()
                        }
                    }

                    _uiState.update {
                        it.copy(
                            vehiculo = vehiculo,
                            conductor = conductor.await(),
                            historialKilometraje = historial.await(),
                            mantenimientos = mantenimientos.await(),
                            proximosMantenimientos = calcularProximosMantenimientos(
                                tipoVehiculo = vehiculo.tipo,
                                kmActual = vehiculo.kmActual,
                                mantenimientosDelVehiculo = mantenimientos.await(),
                                frecuencias = frecuencias.await()
                            ),
                            cargando = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudo cargar la información del vehículo"
                    )
                }
            }
        }
    }
}
