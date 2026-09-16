package com.transandina.flotilla.ui.vehiculo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.KilometrajeHistorico
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.data.repository.KilometrajeRepository
import com.transandina.flotilla.data.repository.VehiculoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VehiculoDetalleUiState(
    val vehiculo: Vehiculo? = null,
    val historialKilometraje: List<KilometrajeHistorico> = emptyList(),
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
    private val kilometrajeRepository: KilometrajeRepository = KilometrajeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculoDetalleUiState())
    val uiState: StateFlow<VehiculoDetalleUiState> = _uiState

    fun cargar(vehiculoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val vehiculo = vehiculoRepository.obtenerVehiculoPorId(vehiculoId)
                // El historial solo lo puede leer quien tenga permiso sobre el
                // vehículo (política kilometraje_select); si falla, la pantalla
                // igual muestra el resto de los datos.
                val historial = kilometrajeRepository.obtenerHistorial(vehiculoId)
                _uiState.update {
                    it.copy(
                        vehiculo = vehiculo,
                        historialKilometraje = historial,
                        cargando = false
                    )
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
