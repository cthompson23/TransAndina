package com.transandina.flotilla.ui.kilometraje

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.KilometrajeHistorico
import com.transandina.flotilla.data.model.RegistroKilometraje
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.data.repository.AuthRepository
import com.transandina.flotilla.data.repository.KilometrajeRepository
import com.transandina.flotilla.data.repository.VehiculoRepository
import com.transandina.flotilla.domain.aFechaIso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class KilometrajeUiState(
    val vehiculo: Vehiculo? = null,
    val ultimoRegistro: KilometrajeHistorico? = null,
    val kmIngresado: String = "",
    val fecha: LocalDate = LocalDate.now(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val exito: Boolean = false
)

class KilometrajeViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val kilometrajeRepository: KilometrajeRepository = KilometrajeRepository(),
    private val vehiculoRepository: VehiculoRepository = VehiculoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(KilometrajeUiState())
    val uiState: StateFlow<KilometrajeUiState> = _uiState

    /**
     * Carga el vehículo sobre el que se va a registrar. Si no se pasa
     * [vehiculoId] se usa el vehículo asignado al conductor con sesión
     * iniciada, que es como se entra desde el hub.
     */
    fun cargar(vehiculoId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val vehiculo = if (vehiculoId != null) {
                    vehiculoRepository.obtenerVehiculoPorId(vehiculoId)
                } else {
                    val conductorId = authRepository.usuarioActualId()
                    conductorId?.let { vehiculoRepository.obtenerVehiculoAsignado(it) }
                }
                val ultimo = vehiculo?.let {
                    kilometrajeRepository.obtenerHistorial(it.id).lastOrNull()
                }
                _uiState.update {
                    it.copy(vehiculo = vehiculo, ultimoRegistro = ultimo, cargando = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudo cargar el vehículo asignado")
                }
            }
        }
    }

    fun onKmChange(value: String) {
        _uiState.update { it.copy(kmIngresado = value, error = null, exito = false) }
    }

    fun onFechaChange(fecha: LocalDate) {
        _uiState.update { it.copy(fecha = fecha, error = null, exito = false) }
    }

    fun registrarKilometraje() {
        val estadoActual = _uiState.value
        val vehiculo = estadoActual.vehiculo
        val conductorId = authRepository.usuarioActualId()
        val kmNuevo = estadoActual.kmIngresado.trim().toDoubleOrNull()

        if (vehiculo == null || conductorId == null) {
            _uiState.update { it.copy(error = "No tienes un vehículo asignado") }
            return
        }
        // Única validación local: que sea un número. Que el kilometraje sea
        // mayor al anterior lo decide el trigger trg_validar_km, y su mensaje
        // se traduce abajo en lugar de repetir la regla aquí.
        if (kmNuevo == null) {
            _uiState.update { it.copy(error = "Ingresa un kilometraje válido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                kilometrajeRepository.registrarKilometraje(
                    RegistroKilometraje(
                        vehiculoId = vehiculo.id,
                        registradoPor = conductorId,
                        fecha = aFechaIso(estadoActual.fecha),
                        km = kmNuevo
                    )
                )
                // El trigger trg_actualizar_km ya dejó vehiculos.km_actual en
                // este valor; reflejarlo aquí evita una lectura extra.
                _uiState.update {
                    it.copy(
                        guardando = false,
                        exito = true,
                        kmIngresado = "",
                        vehiculo = vehiculo.copy(kmActual = kmNuevo)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = mensajeDeError(e, vehiculo)) }
            }
        }
    }

    /**
     * Traduce el error que devuelve Postgres a algo que se pueda leer en
     * pantalla. El trigger `validar_km_incremental` compara contra
     * `vehiculos.km_actual`, sin mirar la fecha del registro.
     */
    private fun mensajeDeError(e: Exception, vehiculo: Vehiculo): String {
        val detalle = e.message.orEmpty()
        return when {
            detalle.contains("mayor al último registrado", ignoreCase = true) ||
                detalle.contains("validar_km_incremental", ignoreCase = true) ->
                "El kilometraje debe ser mayor al último registrado " +
                    "(${vehiculo.kmActual.toLong()} km)"

            detalle.contains("row-level security", ignoreCase = true) ||
                detalle.contains("policy", ignoreCase = true) ->
                "Solo el conductor asignado puede registrar el kilometraje de este vehículo"

            else -> "No se pudo registrar. Intenta de nuevo"
        }
    }
}
