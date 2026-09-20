package com.transandina.flotilla.ui.vehiculo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.PersonaResumen
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.data.repository.VehiculoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AsignarMecanicoUiState(
    val vehiculo: Vehiculo? = null,
    val mecanicos: List<PersonaResumen> = emptyList(),
    /** Null = el vehículo queda sin mecánico responsable. */
    val seleccionadoId: String? = null,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val guardado: Boolean = false
) {
    fun nombreDe(mecanicoId: String?): String? =
        mecanicos.find { it.id == mecanicoId }?.nombreCompleto

    val hayCambio: Boolean get() = seleccionadoId != vehiculo?.mecanicoId
}

/**
 * Asignar el mecánico responsable de un vehículo. La pantalla la usan el
 * encargado y el conductor del vehículo; quién tiene permiso lo decide la
 * función `asignar_mecanico` en Postgres (migración 202609201200).
 */
class AsignarMecanicoViewModel(
    private val vehiculoRepository: VehiculoRepository = VehiculoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AsignarMecanicoUiState())
    val uiState: StateFlow<AsignarMecanicoUiState> = _uiState

    fun cargar(vehiculoId: String) {
        if (_uiState.value.vehiculo?.id == vehiculoId) return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                coroutineScope {
                    val vehiculo = async { vehiculoRepository.obtenerVehiculoPorId(vehiculoId) }
                    val mecanicos = async { vehiculoRepository.obtenerMecanicos() }
                    val encontrado = vehiculo.await()
                    _uiState.update {
                        it.copy(
                            vehiculo = encontrado,
                            mecanicos = mecanicos.await(),
                            seleccionadoId = encontrado?.mecanicoId,
                            cargando = false,
                            error = if (encontrado == null) "No encontramos ese vehículo" else null
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

    fun onSeleccionar(mecanicoId: String?) =
        _uiState.update { it.copy(seleccionadoId = mecanicoId, error = null, guardado = false) }

    fun guardar() {
        val s = _uiState.value
        val vehiculo = s.vehiculo ?: return
        if (!s.hayCambio) {
            _uiState.update { it.copy(guardado = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                vehiculoRepository.asignarMecanico(vehiculo.id, s.seleccionadoId)
                _uiState.update {
                    it.copy(
                        guardando = false,
                        guardado = true,
                        vehiculo = vehiculo.copy(mecanicoId = s.seleccionadoId)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = mensajeDeError(e)) }
            }
        }
    }

    /** Traduce los `raise exception` de la función `asignar_mecanico`. */
    private fun mensajeDeError(e: Exception): String {
        val detalle = e.message.orEmpty()
        return when {
            detalle.contains("Solo el encargado") ->
                "Solo el encargado de flota o el conductor del vehículo pueden cambiar el mecánico"
            detalle.contains("rol mecánico") -> "El mecánico elegido ya no está activo"
            detalle.contains("cuenta no está activa") -> "Tu cuenta no está activa"
            else -> "No se pudo guardar el mecánico. Intenta de nuevo"
        }
    }
}
