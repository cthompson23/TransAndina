package com.transandina.flotilla.ui.vehiculo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.data.repository.AuthRepository
import com.transandina.flotilla.data.repository.VehiculoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class EstadoDocumento { AL_DIA, PROXIMO, VENCIDO, SIN_DATO }

data class VehiculoUiState(
    val vehiculo: Vehiculo? = null,
    val cargando: Boolean = false,
    val error: String? = null
)

class VehiculoViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val vehiculoRepository: VehiculoRepository = VehiculoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculoUiState())
    val uiState: StateFlow<VehiculoUiState> = _uiState

    init {
        cargarVehiculo()
    }

    fun cargarVehiculo() {
        val conductorId = authRepository.usuarioActualId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val vehiculo = vehiculoRepository.obtenerVehiculoAsignado(conductorId)
                _uiState.update { it.copy(vehiculo = vehiculo, cargando = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudo cargar la información del vehículo")
                }
            }
        }
    }
}

/**
 * Calcula el estado de un documento (marchamo, revisión técnica, seguro)
 * comparando su fecha de vencimiento contra hoy. Vive fuera del ViewModel
 * porque es una función pura, fácil de reutilizar en Home y en el detalle.
 */
fun calcularEstadoDocumento(fechaIso: String?): EstadoDocumento {
    if (fechaIso == null) return EstadoDocumento.SIN_DATO
    return try {
        val fecha = LocalDate.parse(fechaIso)
        val hoy = LocalDate.now()
        when {
            fecha.isBefore(hoy) -> EstadoDocumento.VENCIDO
            fecha.isBefore(hoy.plusDays(15)) -> EstadoDocumento.PROXIMO
            else -> EstadoDocumento.AL_DIA
        }
    } catch (e: Exception) {
        EstadoDocumento.SIN_DATO
    }
}
