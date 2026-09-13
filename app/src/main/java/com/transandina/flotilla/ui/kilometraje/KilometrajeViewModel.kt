package com.transandina.flotilla.ui.kilometraje

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.RegistroKilometraje
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.data.repository.AuthRepository
import com.transandina.flotilla.data.repository.KilometrajeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class KilometrajeUiState(
    val vehiculo: Vehiculo? = null,
    val kmIngresado: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val exito: Boolean = false
)

class KilometrajeViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val kilometrajeRepository: KilometrajeRepository = KilometrajeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(KilometrajeUiState())
    val uiState: StateFlow<KilometrajeUiState> = _uiState

    init {
        cargarVehiculoAsignado()
    }

    private fun cargarVehiculoAsignado() {
        val conductorId = authRepository.usuarioActualId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                val vehiculo = kilometrajeRepository.obtenerVehiculoAsignado(conductorId)
                _uiState.update { it.copy(vehiculo = vehiculo, cargando = false) }
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

    fun registrarKilometraje() {
        val estadoActual = _uiState.value
        val vehiculo = estadoActual.vehiculo
        val conductorId = authRepository.usuarioActualId()
        val kmNuevo = estadoActual.kmIngresado.toDoubleOrNull()

        if (vehiculo == null || conductorId == null) {
            _uiState.update { it.copy(error = "No tienes un vehículo asignado") }
            return
        }
        if (kmNuevo == null) {
            _uiState.update { it.copy(error = "Ingresa un kilometraje válido") }
            return
        }
        if (kmNuevo <= vehiculo.kmActual) {
            _uiState.update {
                it.copy(error = "El kilometraje debe ser mayor a ${vehiculo.kmActual}")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                kilometrajeRepository.registrarKilometraje(
                    RegistroKilometraje(
                        vehiculoId = vehiculo.id,
                        registradoPor = conductorId,
                        fecha = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                        km = kmNuevo
                    )
                )
                // Actualizamos el estado local en optimista; el trigger en la
                // base de datos ya hizo lo mismo en `vehiculos.km_actual`.
                _uiState.update {
                    it.copy(
                        cargando = false,
                        exito = true,
                        kmIngresado = "",
                        vehiculo = vehiculo.copy(kmActual = kmNuevo)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudo registrar. Intenta de nuevo")
                }
            }
        }
    }
}
