package com.transandina.flotilla.ui.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.TipoMantenimiento
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.data.repository.MantenimientoRepository
import com.transandina.flotilla.data.repository.VehiculoRepository
import com.transandina.flotilla.domain.FiltrosReporte
import com.transandina.flotilla.domain.costoTotal
import com.transandina.flotilla.domain.filtrarMantenimientos
import com.transandina.flotilla.domain.talleresRegistrados
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Por defecto: del 1 de enero del año en curso hasta hoy. */
fun filtrosIniciales(hoy: LocalDate = LocalDate.now()) =
    FiltrosReporte(desde = hoy.withDayOfYear(1), hasta = hoy)

data class ReportesUiState(
    val vehiculos: List<Vehiculo> = emptyList(),
    val mantenimientos: List<Mantenimiento> = emptyList(),
    val talleres: List<String> = emptyList(),
    val filtros: FiltrosReporte = filtrosIniciales(),
    /** Null mientras se ven los filtros; la lista filtrada al generar el reporte. */
    val resultado: List<Mantenimiento>? = null,
    val cargando: Boolean = false,
    val error: String? = null
) {
    val costoTotal: Double get() = resultado?.let(::costoTotal) ?: 0.0

    val placasPorVehiculo: Map<String, String> get() = vehiculos.associate { it.id to it.placa }

    val vehiculoFiltrado: Vehiculo? get() = vehiculos.find { it.id == filtros.vehiculoId }
}

/**
 * Reporte de mantenimientos (Figma `102:198` y `102:218`). Se descarga todo
 * una vez y se filtra en el teléfono con `domain/Reportes.kt`.
 */
class ReportesViewModel(
    private val vehiculoRepository: VehiculoRepository = VehiculoRepository(),
    private val mantenimientoRepository: MantenimientoRepository = MantenimientoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportesUiState())
    val uiState: StateFlow<ReportesUiState> = _uiState

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                coroutineScope {
                    val vehiculos = async { vehiculoRepository.obtenerFlotilla() }
                    val mantenimientos = async { mantenimientoRepository.obtenerTodos() }
                    _uiState.update {
                        it.copy(
                            vehiculos = vehiculos.await(),
                            mantenimientos = mantenimientos.await(),
                            talleres = talleresRegistrados(mantenimientos.await()),
                            cargando = false
                        )
                    }
                }
                // Si ya había un reporte en pantalla, se recalcula con los datos nuevos.
                if (_uiState.value.resultado != null) generar()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudieron cargar los mantenimientos. Revisa tu conexión.")
                }
            }
        }
    }

    fun onVehiculoChange(vehiculoId: String?) = actualizarFiltros { it.copy(vehiculoId = vehiculoId) }
    fun onTipoChange(tipo: TipoMantenimiento?) = actualizarFiltros { it.copy(tipo = tipo) }
    fun onTallerChange(taller: String?) = actualizarFiltros { it.copy(taller = taller) }
    fun onDesdeChange(fecha: LocalDate) = actualizarFiltros { it.copy(desde = fecha) }
    fun onHastaChange(fecha: LocalDate) = actualizarFiltros { it.copy(hasta = fecha) }

    fun limpiarFiltros() = _uiState.update { it.copy(filtros = filtrosIniciales(), error = null) }

    fun generar() {
        val filtros = _uiState.value.filtros
        if (filtros.desde != null && filtros.hasta != null && filtros.desde.isAfter(filtros.hasta)) {
            _uiState.update { it.copy(error = "La fecha inicial no puede ser posterior a la final") }
            return
        }
        _uiState.update {
            it.copy(resultado = filtrarMantenimientos(it.mantenimientos, filtros), error = null)
        }
    }

    fun modificarFiltros() = _uiState.update { it.copy(resultado = null) }

    private fun actualizarFiltros(cambio: (FiltrosReporte) -> FiltrosReporte) =
        _uiState.update { it.copy(filtros = cambio(it.filtros), error = null) }
}
