package com.transandina.flotilla.ui.flotilla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.EstadoCuenta
import com.transandina.flotilla.data.model.PersonaResumen
import com.transandina.flotilla.data.model.ReasignarConductorParams
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.TIPOS_VEHICULO
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.data.model.VehiculoPayload
import com.transandina.flotilla.data.repository.UsuarioRepository
import com.transandina.flotilla.data.repository.VehiculoRepository
import com.transandina.flotilla.domain.aFechaIso
import com.transandina.flotilla.domain.parsearFechaIso
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val activo: Boolean = true,
    /** Conductores activos que pueden tomar este vehículo. */
    val conductores: List<Usuario> = emptyList(),
    /** Mecánicos activos, de `mecanicos_disponibles()`. */
    val mecanicos: List<PersonaResumen> = emptyList(),
    val conductorId: String? = null,
    val mecanicoId: String? = null,
    /** El conductor que tenía al abrir: si cambia, se llama a la reasignación. */
    val conductorOriginalId: String? = null,
    /** Con conductor asignado, darlo de baja también lo libera. */
    val tieneConductor: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val guardado: Boolean = false
) {
    val esEdicion: Boolean get() = vehiculoId != null

    val nombreConductor: String?
        get() = conductores.find { it.id == conductorId }?.nombreCompleto

    val nombreMecanico: String?
        get() = mecanicos.find { it.id == mecanicoId }?.nombreCompleto
}

/** Registrar un vehículo nuevo o editar uno existente (solo encargado). */
class VehiculoFormularioViewModel(
    private val vehiculoRepository: VehiculoRepository = VehiculoRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculoFormularioUiState())
    val uiState: StateFlow<VehiculoFormularioUiState> = _uiState

    /**
     * Carga los catálogos (conductores libres y mecánicos activos) y, con
     * [vehiculoId], los datos del vehículo para editarlos. Solo carga una vez.
     */
    fun iniciar(vehiculoId: String?) {
        val actual = _uiState.value
        val yaCargado = actual.conductores.isNotEmpty() || actual.mecanicos.isNotEmpty()
        if (yaCargado && (vehiculoId == null || actual.vehiculoId == vehiculoId)) return

        viewModelScope.launch {
            _uiState.update { it.copy(vehiculoId = vehiculoId, cargando = true, error = null) }
            try {
                coroutineScope {
                    val usuarios = async { usuarioRepository.obtenerUsuarios() }
                    val flotilla = async { vehiculoRepository.obtenerFlotilla() }
                    val mecanicos = async {
                        runCatching { vehiculoRepository.obtenerMecanicos() }.getOrDefault(emptyList())
                    }

                    // Un conductor solo puede tener un vehículo activo, así que
                    // la lista deja fuera a los que ya tienen otro.
                    val ocupados = flotilla.await()
                        .filter { it.activo && it.id != vehiculoId }
                        .mapNotNull { it.conductorId }
                        .toSet()
                    val conductores = usuarios.await().filter {
                        it.rol == RolUsuario.conductor &&
                            it.estado == EstadoCuenta.activo &&
                            it.id !in ocupados
                    }

                    _uiState.update {
                        it.copy(conductores = conductores, mecanicos = mecanicos.await())
                    }
                }

                if (vehiculoId == null) {
                    _uiState.update { it.copy(cargando = false) }
                    return@launch
                }

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
                        activo = vehiculo.activo,
                        conductorId = vehiculo.conductorId,
                        conductorOriginalId = vehiculo.conductorId,
                        mecanicoId = vehiculo.mecanicoId,
                        tieneConductor = vehiculo.conductorId != null,
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
    fun onConductorChange(id: String?) = _uiState.update { it.copy(conductorId = id, error = null) }
    fun onMecanicoChange(id: String?) = _uiState.update { it.copy(mecanicoId = id, error = null) }

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
            fechaPermisoCarga = s.fechaPermisoCarga?.let(::aFechaIso),
            mecanicoId = s.mecanicoId
        )

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                // El conductor no va en el payload: pasa por la función
                // `reasignar_conductor`, que valida y guarda el historial.
                val id = if (s.vehiculoId == null) {
                    vehiculoRepository.registrarVehiculo(datos).id
                } else {
                    vehiculoRepository.actualizarVehiculo(s.vehiculoId, datos)
                    s.vehiculoId
                }
                if (s.conductorId != s.conductorOriginalId) {
                    vehiculoRepository.reasignarConductor(
                        ReasignarConductorParams(
                            vehiculoId = id,
                            conductorNuevoId = s.conductorId,
                            fechaEfectiva = aFechaIso(LocalDate.now()),
                            motivo = if (s.vehiculoId == null) {
                                "Asignado al registrar el vehículo"
                            } else {
                                "Cambio desde la edición del vehículo"
                            }
                        )
                    )
                }
                _uiState.update {
                    it.copy(
                        guardando = false,
                        guardado = true,
                        vehiculoId = id,
                        conductorOriginalId = s.conductorId,
                        tieneConductor = s.conductorId != null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = mensajeDeError(e)) }
            }
        }
    }

    /**
     * Da de baja el vehículo o lo reactiva. Al darlo de baja se libera al
     * conductor con el mismo RPC de reasignación, para que quede en el
     * historial por qué se quedó sin conductor.
     */
    fun cambiarActivo(activo: Boolean) {
        val s = _uiState.value
        val vehiculoId = s.vehiculoId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                if (!activo && s.tieneConductor) {
                    vehiculoRepository.reasignarConductor(
                        ReasignarConductorParams(
                            vehiculoId = vehiculoId,
                            conductorNuevoId = null,
                            fechaEfectiva = aFechaIso(LocalDate.now()),
                            motivo = "Vehículo dado de baja"
                        )
                    )
                }
                vehiculoRepository.cambiarActivo(vehiculoId, activo)
                _uiState.update {
                    it.copy(guardando = false, activo = activo, tieneConductor = false, guardado = true)
                }
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
            detalle.contains("ya tiene un vehículo asignado") ->
                "Ese conductor ya tiene otro vehículo asignado"
            detalle.contains("cuenta activa") -> "El conductor elegido no está activo"
            detalle.contains("rol mecánico") -> "El mecánico elegido no está activo"
            detalle.contains("row-level security", ignoreCase = true) ->
                "Solo el encargado de flota puede registrar o editar vehículos"
            else -> "No se pudo guardar. Intenta de nuevo"
        }
    }

    /** 1.0 → "1", 1.5 → "1.5": sin ".0" innecesario. */
    private fun capacidadComoTexto(valor: Double): String =
        if (valor % 1.0 == 0.0) valor.toLong().toString() else valor.toString()
}
