package com.transandina.flotilla.ui.flotilla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.TIPOS_VEHICULO
import com.transandina.flotilla.data.model.VehiculoPayload
import com.transandina.flotilla.data.repository.VehiculoRepository
import com.transandina.flotilla.domain.aFechaIso
import com.transandina.flotilla.domain.parsearFechaIso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class VehiculoFormularioUiState(
    /** Null al registrar; el id del vehículo al editar. */
    val vehiculoId: String? = null,
    val placa: String = "",
    val tipo: String? = null,
    val anio: String = "",
    val marca: String = "",
    val modelo: String = "",
    val capacidad: String = "",
    val fechaMarchamo: LocalDate? = null,
    val fechaRevisionTecnica: LocalDate? = null,
    val fechaSeguro: LocalDate? = null,
    val fechaPermisoCarga: LocalDate? = null,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val guardado: Boolean = false
) {
    val esEdicion: Boolean get() = vehiculoId != null
}

/** Registrar un vehículo nuevo o editar uno existente (solo encargado). */
class VehiculoFormularioViewModel(
    private val vehiculoRepository: VehiculoRepository = VehiculoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculoFormularioUiState())
    val uiState: StateFlow<VehiculoFormularioUiState> = _uiState

    /** Con [vehiculoId] carga los datos actuales para editarlos. Solo carga una vez. */
    fun iniciar(vehiculoId: String?) {
        if (vehiculoId == null || _uiState.value.vehiculoId == vehiculoId) return
        viewModelScope.launch {
            _uiState.update { it.copy(vehiculoId = vehiculoId, cargando = true, error = null) }
            try {
                val vehiculo = vehiculoRepository.obtenerVehiculoPorId(vehiculoId)
                if (vehiculo == null) {
                    _uiState.update { it.copy(cargando = false, error = "No encontramos ese vehículo") }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        placa = vehiculo.placa,
                        tipo = vehiculo.tipo,
                        anio = vehiculo.anio.toString(),
                        marca = vehiculo.marca,
                        modelo = vehiculo.modelo,
                        capacidad = vehiculo.capacidad?.let(::capacidadComoTexto).orEmpty(),
                        fechaMarchamo = parsearFechaIso(vehiculo.fechaMarchamo),
                        fechaRevisionTecnica = parsearFechaIso(vehiculo.fechaRevisionTecnica),
                        fechaSeguro = parsearFechaIso(vehiculo.fechaSeguro),
                        fechaPermisoCarga = parsearFechaIso(vehiculo.fechaPermisoCarga),
                        cargando = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudo cargar el vehículo")
                }
            }
        }
    }

    fun onPlacaChange(valor: String) = _uiState.update { it.copy(placa = valor.uppercase(), error = null) }
    fun onTipoChange(valor: String) = _uiState.update { it.copy(tipo = valor, error = null) }
    fun onAnioChange(valor: String) = _uiState.update { it.copy(anio = valor.filter(Char::isDigit).take(4), error = null) }
    fun onMarcaChange(valor: String) = _uiState.update { it.copy(marca = valor, error = null) }
    fun onModeloChange(valor: String) = _uiState.update { it.copy(modelo = valor, error = null) }
    fun onCapacidadChange(valor: String) = _uiState.update { it.copy(capacidad = valor, error = null) }
    fun onFechaMarchamoChange(fecha: LocalDate) = _uiState.update { it.copy(fechaMarchamo = fecha, error = null) }
    fun onFechaRevisionChange(fecha: LocalDate) = _uiState.update { it.copy(fechaRevisionTecnica = fecha, error = null) }
    fun onFechaSeguroChange(fecha: LocalDate) = _uiState.update { it.copy(fechaSeguro = fecha, error = null) }
    fun onFechaPermisoChange(fecha: LocalDate) = _uiState.update { it.copy(fechaPermisoCarga = fecha, error = null) }

    fun guardar() {
        val s = _uiState.value
        val anio = s.anio.toIntOrNull()
        val capacidadTexto = s.capacidad.trim().replace(',', '.')
        val capacidad = capacidadTexto.toDoubleOrNull()
        val anioMaximo = LocalDate.now().year + 1

        val error = when {
            s.placa.isBlank() || s.marca.isBlank() || s.modelo.isBlank() || s.anio.isBlank() ->
                "Completa placa, año, marca y modelo"
            s.tipo == null || s.tipo !in TIPOS_VEHICULO -> "Selecciona el tipo de vehículo"
            anio == null || anio !in 1950..anioMaximo -> "Ingresa un año entre 1950 y $anioMaximo"
            capacidadTexto.isNotEmpty() && (capacidad == null || capacidad < 0) ->
                "La capacidad debe ser un número en toneladas"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        val datos = VehiculoPayload(
            placa = s.placa.trim(),
            marca = s.marca.trim(),
            modelo = s.modelo.trim(),
            anio = anio!!,
            tipo = s.tipo!!,
            capacidad = capacidad,
            fechaMarchamo = s.fechaMarchamo?.let(::aFechaIso),
            fechaRevisionTecnica = s.fechaRevisionTecnica?.let(::aFechaIso),
            fechaSeguro = s.fechaSeguro?.let(::aFechaIso),
            fechaPermisoCarga = s.fechaPermisoCarga?.let(::aFechaIso)
        )

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                if (s.vehiculoId == null) {
                    vehiculoRepository.registrarVehiculo(datos)
                } else {
                    vehiculoRepository.actualizarVehiculo(s.vehiculoId, datos)
                }
                _uiState.update { it.copy(guardando = false, guardado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = mensajeDeError(e)) }
            }
        }
    }

    private fun mensajeDeError(e: Exception): String {
        val detalle = e.message.orEmpty()
        return when {
            detalle.contains("vehiculos_placa_key") || detalle.contains("duplicate key", ignoreCase = true) ->
                "Ya existe un vehículo con esa placa"
            detalle.contains("row-level security", ignoreCase = true) ->
                "Solo el encargado de flota puede registrar o editar vehículos"
            else -> "No se pudo guardar. Intenta de nuevo"
        }
    }

    /** 1.0 → "1", 1.5 → "1.5": sin ".0" innecesario. */
    private fun capacidadComoTexto(valor: Double): String =
        if (valor % 1.0 == 0.0) valor.toLong().toString() else valor.toString()
}
