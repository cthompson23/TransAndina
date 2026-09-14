package com.transandina.flotilla.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.data.repository.AuthRepository
import com.transandina.flotilla.data.repository.VehiculoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val vehiculo: Vehiculo? = null,
    val cargando: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val vehiculoRepository: VehiculoRepository = VehiculoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

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
                    it.copy(cargando = false, error = "No se pudo cargar el vehículo")
                }
            }
        }
    }
}
