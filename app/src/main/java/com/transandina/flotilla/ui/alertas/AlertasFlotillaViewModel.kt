package com.transandina.flotilla.ui.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.repository.FlotillaRepository
import com.transandina.flotilla.data.repository.ReasignacionRepository
import com.transandina.flotilla.domain.AlertaFlotilla
import com.transandina.flotilla.domain.DIAS_REASIGNACION_INFORMATIVA
import com.transandina.flotilla.domain.NivelAlerta
import com.transandina.flotilla.domain.aFechaIso
import com.transandina.flotilla.domain.calcularAlertasFlotilla
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AlertasFlotillaUiState(
    val alertas: List<AlertaFlotilla> = emptyList(),
    /** Null = "Todas". */
    val filtro: NivelAlerta? = null,
    val cargando: Boolean = false,
    val error: String? = null
) {
    fun conteo(nivel: NivelAlerta?): Int =
        alertas.count { nivel == null || it.nivel == nivel }

    val visibles: List<AlertaFlotilla>
        get() = alertas.filter { filtro == null || it.nivel == filtro }
}

/**
 * Panel de alertas del encargado (Figma `102:238`). Las alertas se calculan
 * en `domain/AlertasFlotilla.kt`; no se leen de la tabla `alertas`.
 */
class AlertasFlotillaViewModel(
    private val flotillaRepository: FlotillaRepository = FlotillaRepository(),
    private val reasignacionRepository: ReasignacionRepository = ReasignacionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertasFlotillaUiState())
    val uiState: StateFlow<AlertasFlotillaUiState> = _uiState

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val desde = aFechaIso(LocalDate.now().minusDays(DIAS_REASIGNACION_INFORMATIVA))
                val alertas = coroutineScope {
                    val datos = async { flotillaRepository.cargar() }
                    val reasignaciones = async { reasignacionRepository.obtenerDesde(desde) }
                    val flotilla = datos.await()
                    calcularAlertasFlotilla(
                        vehiculos = flotilla.vehiculos,
                        mantenimientos = flotilla.mantenimientos,
                        frecuencias = flotilla.frecuencias,
                        reasignaciones = reasignaciones.await(),
                        nombresPorUsuario = flotilla.usuarios.associate { it.id to it.nombreCompleto }
                    )
                }
                _uiState.update { it.copy(alertas = alertas, cargando = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudieron cargar las alertas. Revisa tu conexión.")
                }
            }
        }
    }

    fun onFiltroChange(nivel: NivelAlerta?) = _uiState.update { it.copy(filtro = nivel) }
}
