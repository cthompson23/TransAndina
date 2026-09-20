package com.transandina.flotilla.ui.flotilla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.repository.FlotillaRepository
import com.transandina.flotilla.domain.EstadoMantenimiento
import com.transandina.flotilla.domain.ResumenVehiculo
import com.transandina.flotilla.domain.resumirVehiculo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Chips de filtro de la lista; null en [estado] significa "Todos". */
enum class FiltroFlotilla(val estado: EstadoMantenimiento?) {
    TODOS(null),
    ATRASADO(EstadoMantenimiento.ATRASADO),
    PROXIMO(EstadoMantenimiento.PROXIMO),
    AL_DIA(EstadoMantenimiento.AL_DIA)
}

data class FlotillaUiState(
    val resumenes: List<ResumenVehiculo> = emptyList(),
    val busqueda: String = "",
    val filtro: FiltroFlotilla = FiltroFlotilla.TODOS,
    val cargando: Boolean = false,
    val error: String? = null
) {
    val vehiculosActivos: Int
        get() = resumenes.count { it.vehiculo.activo }

    val mantenimientosPendientes: Int
        get() = resumenes.count { it.estadoMantenimiento != EstadoMantenimiento.AL_DIA }

    val documentosPendientes: Int
        get() = resumenes.count { it.tieneDocumentosPendientes }

    fun conteo(filtro: FiltroFlotilla): Int =
        resumenes.count { filtro.estado == null || it.estadoGeneral == filtro.estado }

    /** Lo que se ve en la lista: filtro por estado y búsqueda por placa, vehículo o conductor. */
    val visibles: List<ResumenVehiculo>
        get() {
            val texto = busqueda.trim().lowercase()
            return resumenes
                .filter { filtro.estado == null || it.estadoGeneral == filtro.estado }
                .filter { resumen ->
                    texto.isEmpty() || listOfNotNull(
                        resumen.vehiculo.placa,
                        resumen.vehiculo.marca,
                        resumen.vehiculo.modelo,
                        resumen.nombreConductor
                    ).any { it.lowercase().contains(texto) }
                }
        }
}

/**
 * Estado general de la flotilla para el encargado. El semáforo de cada
 * vehículo lo calcula `domain/EstadoMantenimiento.kt`.
 */
class FlotillaViewModel(
    private val flotillaRepository: FlotillaRepository = FlotillaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlotillaUiState())
    val uiState: StateFlow<FlotillaUiState> = _uiState

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val datos = flotillaRepository.cargar()
                val nombres = datos.usuarios.associate { it.id to it.nombreCompleto } +
                    datos.personas.associate { it.id to it.nombreCompleto }
                val mantenimientosPorVehiculo = datos.mantenimientos.groupBy { it.vehiculoId }
                val resumenes = datos.vehiculos.map { vehiculo ->
                    resumirVehiculo(
                        vehiculo = vehiculo,
                        nombreConductor = vehiculo.conductorId?.let { nombres[it] },
                        mantenimientosDelVehiculo = mantenimientosPorVehiculo[vehiculo.id].orEmpty(),
                        frecuencias = datos.frecuencias
                    )
                }
                _uiState.update { it.copy(resumenes = resumenes, cargando = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudo cargar la flotilla. Revisa tu conexión.")
                }
            }
        }
    }

    fun onBusquedaChange(valor: String) = _uiState.update { it.copy(busqueda = valor) }

    fun onFiltroChange(filtro: FiltroFlotilla) = _uiState.update { it.copy(filtro = filtro) }
}
