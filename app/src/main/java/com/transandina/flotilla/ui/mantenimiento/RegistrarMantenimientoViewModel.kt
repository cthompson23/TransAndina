package com.transandina.flotilla.ui.mantenimiento

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transandina.flotilla.data.model.FrecuenciaMantenimiento
import com.transandina.flotilla.data.model.NuevoMantenimientoPayload
import com.transandina.flotilla.data.model.TipoMantenimiento
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.data.repository.AuthRepository
import com.transandina.flotilla.data.repository.MantenimientoRepository
import com.transandina.flotilla.data.repository.UsuarioRepository
import com.transandina.flotilla.data.repository.VehiculoRepository
import com.transandina.flotilla.domain.ServicioEstimado
import com.transandina.flotilla.domain.aFechaIso
import com.transandina.flotilla.domain.estimarSiguienteServicio
import com.transandina.flotilla.domain.formatearKilometrosConUnidad
import com.transandina.flotilla.domain.interpretarKilometros
import com.transandina.flotilla.domain.interpretarMonto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

/** Tope de fotos por mantenimiento (decisión de producto, migración 202609152200). */
const val MAXIMO_FOTOS = 3

/** Categoría sin frecuencia, para lo que no encaja en las demás. Va al final. */
private const val CATEGORIA_OTRO = "Otro"

/** Foto ya preparada en el teléfono, pendiente de subir al guardar. */
class FotoAdjunta(
    val nombre: String,
    val jpeg: ByteArray,
    val miniatura: Bitmap,
    val id: String = UUID.randomUUID().toString()
)

data class RegistrarMantenimientoUiState(
    val vehiculo: Vehiculo? = null,
    /** Vehículos sobre los que esta persona puede registrar. El mecánico suele tener varios. */
    val vehiculosDisponibles: List<Vehiculo> = emptyList(),
    val frecuencias: List<FrecuenciaMantenimiento> = emptyList(),
    val tipo: TipoMantenimiento? = null,
    val categoria: String? = null,
    val fecha: LocalDate = LocalDate.now(),
    val km: String = "",
    val taller: String = "",
    val responsable: String = "",
    val costo: String = "",
    val descripcion: String = "",
    val fotos: List<FotoAdjunta> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val guardado: Boolean = false,
    /** Cuántas fotos no se pudieron subir aunque el mantenimiento sí se guardó. */
    val fotosFallidas: Int = 0
) {
    /** Categorías del catálogo para el tipo de este vehículo, con "Otro" al final. */
    val categorias: List<String>
        get() = frecuencias
            .filter { it.tipoVehiculo == vehiculo?.tipo }
            .map { it.categoria }
            .distinct()
            .sortedWith(compareBy({ it == CATEGORIA_OTRO }, { it }))

    val puedeAdjuntar: Boolean get() = fotos.size < MAXIMO_FOTOS

    /** Con más de uno hay que elegir sobre cuál se registra. */
    val hayQueElegirVehiculo: Boolean get() = vehiculosDisponibles.size > 1

    /**
     * El servicio se hizo con más kilómetros de los que tiene registrados el
     * vehículo: primero hay que registrar la lectura del odómetro.
     */
    val kmMayorAlDelVehiculo: Boolean
        get() {
            val ingresado = interpretarKilometros(km) ?: return false
            return ingresado > (vehiculo?.kmActual ?: return false)
        }

    /** Siguiente servicio si se guarda lo que hay en el formulario; null si no aplica. */
    val estimado: ServicioEstimado?
        get() {
            val frecuencia = frecuencias.find {
                it.tipoVehiculo == vehiculo?.tipo && it.categoria == categoria
            } ?: return null
            val kmServicio = interpretarKilometros(km) ?: return null
            return estimarSiguienteServicio(frecuencia, fecha, kmServicio)
        }
}

/**
 * Registro de un mantenimiento sobre un vehículo (Figma `49:161`). La misma
 * pantalla sirve para los tres roles: el encargado entra desde el detalle de
 * cualquier vehículo, el conductor la tiene como pestaña sobre su vehículo
 * asignado y el mecánico elige entre los que tiene a cargo.
 */
class RegistrarMantenimientoViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val vehiculoRepository: VehiculoRepository = VehiculoRepository(),
    private val mantenimientoRepository: MantenimientoRepository = MantenimientoRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrarMantenimientoUiState())
    val uiState: StateFlow<RegistrarMantenimientoUiState> = _uiState

    /**
     * Vuelve a leer el vehículo sin tocar lo que la persona ya escribió. Se
     * usa al volver de registrar el kilometraje.
     */
    fun recargarVehiculo() {
        val vehiculoId = _uiState.value.vehiculo?.id ?: return
        viewModelScope.launch {
            val actualizado = runCatching { vehiculoRepository.obtenerVehiculoPorId(vehiculoId) }
                .getOrNull() ?: return@launch
            _uiState.update { estado ->
                estado.copy(
                    vehiculo = actualizado,
                    vehiculosDisponibles = estado.vehiculosDisponibles.map { v ->
                        if (v.id == actualizado.id) actualizado else v
                    },
                    error = null
                )
            }
        }
    }

    /**
     * Carga los vehículos sobre los que se puede registrar. Con [vehiculoId]
     * es uno solo (se entra desde el detalle); sin él, los propios de quien
     * tiene la sesión: el asignado como conductor y los que tiene a cargo
     * como mecánico. Si queda uno solo, se selecciona sin preguntar.
     *
     * @param esMecanico rellena el responsable con su propio nombre, que es
     *   lo que la rúbrica llama "taller o mecánico responsable".
     */
    fun cargar(vehiculoId: String?, esMecanico: Boolean = false) {
        val actual = _uiState.value
        val yaCargado = actual.vehiculo != null || actual.vehiculosDisponibles.isNotEmpty()
        if (yaCargado && (vehiculoId == null || actual.vehiculo?.id == vehiculoId)) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val usuarioId = authRepository.usuarioActualId()
                coroutineScope {
                    val disponibles = async {
                        if (vehiculoId != null) {
                            listOfNotNull(vehiculoRepository.obtenerVehiculoPorId(vehiculoId))
                        } else if (usuarioId != null) {
                            // Un conductor no tiene vehículos a cargo y un
                            // mecánico no tiene vehículo asignado: cada
                            // consulta devuelve vacío para el otro rol.
                            val asignado = runCatching {
                                vehiculoRepository.obtenerVehiculoAsignado(usuarioId)
                            }.getOrNull()
                            val aCargo = runCatching {
                                vehiculoRepository.obtenerVehiculosDeMecanico(usuarioId)
                            }.getOrDefault(emptyList())
                            (listOfNotNull(asignado) + aCargo).distinctBy { v -> v.id }
                        } else {
                            emptyList()
                        }
                    }
                    val frecuencias = async { mantenimientoRepository.obtenerFrecuencias() }
                    val responsable = async {
                        if (!esMecanico || usuarioId == null) {
                            ""
                        } else {
                            runCatching { usuarioRepository.obtenerPerfil(usuarioId) }
                                .getOrNull()?.nombreCompleto.orEmpty()
                        }
                    }

                    val lista = disponibles.await()
                    val elegido = lista.singleOrNull()
                    _uiState.update {
                        it.copy(
                            vehiculo = elegido,
                            vehiculosDisponibles = lista,
                            frecuencias = frecuencias.await(),
                            responsable = responsable.await().ifBlank { it.responsable },
                            // Se propone el km actual: es lo más común al registrar el mismo día.
                            km = elegido?.kmActual?.toLong()?.toString().orEmpty(),
                            cargando = false,
                            error = when {
                                lista.isNotEmpty() -> null
                                vehiculoId != null -> "No encontramos ese vehículo"
                                else -> null // Sin vehículos: lo explica la pantalla
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = "No se pudo cargar el vehículo") }
            }
        }
    }

    /** Cambia el vehículo elegido y propone su kilometraje actual. */
    fun onVehiculoChange(vehiculoId: String) = editar { estado ->
        val elegido = estado.vehiculosDisponibles.find { it.id == vehiculoId } ?: return@editar estado
        estado.copy(vehiculo = elegido, km = elegido.kmActual.toLong().toString())
    }

    // Cualquier edición limpia el error y el aviso de "guardado" anterior.
    private fun editar(cambio: (RegistrarMantenimientoUiState) -> RegistrarMantenimientoUiState) =
        _uiState.update { cambio(it).copy(error = null, guardado = false) }

    fun onTipoChange(tipo: TipoMantenimiento) = editar { it.copy(tipo = tipo) }
    fun onCategoriaChange(valor: String) = editar { it.copy(categoria = valor) }
    fun onFechaChange(fecha: LocalDate) = editar { it.copy(fecha = fecha) }
    fun onKmChange(valor: String) = editar { it.copy(km = valor) }
    fun onTallerChange(valor: String) = editar { it.copy(taller = valor) }
    fun onResponsableChange(valor: String) = editar { it.copy(responsable = valor) }
    fun onCostoChange(valor: String) = editar { it.copy(costo = valor) }
    fun onDescripcionChange(valor: String) = editar { it.copy(descripcion = valor) }

    fun onFotoAgregada(foto: FotoAdjunta) = editar {
        if (it.fotos.size >= MAXIMO_FOTOS) it else it.copy(fotos = it.fotos + foto)
    }

    fun onFotoQuitada(id: String) = editar { it.copy(fotos = it.fotos.filterNot { f -> f.id == id }) }

    fun onErrorFoto(mensaje: String) = _uiState.update { it.copy(error = mensaje) }

    /**
     * Deja el formulario en blanco sin salir de la pantalla. Lo usa la pestaña
     * del conductor, que no tiene a dónde volver después de guardar.
     *
     * @param conservarAviso mantiene el mensaje de "registrado correctamente".
     */
    fun limpiarFormulario(conservarAviso: Boolean = false) = _uiState.update {
        it.copy(
            tipo = null,
            categoria = null,
            fecha = LocalDate.now(),
            km = it.vehiculo?.kmActual?.toLong()?.toString().orEmpty(),
            taller = "",
            responsable = "",
            costo = "",
            descripcion = "",
            fotos = emptyList(),
            error = null,
            guardado = conservarAviso,
            fotosFallidas = if (conservarAviso) it.fotosFallidas else 0
        )
    }

    fun guardar() {
        val s = _uiState.value
        val vehiculo = s.vehiculo ?: return
        val usuarioId = authRepository.usuarioActualId()
        val km = interpretarKilometros(s.km)
        val costo = interpretarMonto(s.costo)

        val error = when {
            usuarioId == null -> "Tu sesión expiró. Vuelve a iniciar sesión"
            s.tipo == null -> "Selecciona el tipo de mantenimiento"
            s.categoria == null -> "Selecciona la categoría"
            km == null -> "Ingresa el kilometraje del servicio"
            // El odómetro solo sube: un servicio no puede estar "adelante" de
            // la última lectura del vehículo. La pantalla ofrece registrarla.
            km > vehiculo.kmActual ->
                "El servicio tiene más kilómetros que la última lectura del vehículo " +
                    "(${formatearKilometrosConUnidad(vehiculo.kmActual)}). " +
                    "Registra primero el kilometraje."
            s.taller.isBlank() -> "Indica el taller o lugar del servicio"
            costo == null -> "Ingresa un costo válido (puede ser 0)"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            val registrado = try {
                mantenimientoRepository.registrar(
                    NuevoMantenimientoPayload(
                        vehiculoId = vehiculo.id,
                        registradoPor = usuarioId!!,
                        tipo = s.tipo!!,
                        categoria = s.categoria!!,
                        fecha = aFechaIso(s.fecha),
                        km = km!!,
                        responsable = s.responsable.trim().ifBlank { null },
                        descripcion = s.descripcion.trim().ifBlank { null },
                        costo = costo!!,
                        taller = s.taller.trim()
                    )
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = mensajeDeError(e)) }
                return@launch
            }

            // El mantenimiento ya quedó guardado: si una foto falla, se avisa
            // pero no se pierde el registro.
            var fallidas = 0
            s.fotos.forEach { foto ->
                try {
                    mantenimientoRepository.subirFoto(vehiculo.id, registrado.id, foto.jpeg)
                } catch (e: Exception) {
                    fallidas++
                }
            }
            _uiState.update { it.copy(guardando = false, guardado = true, fotosFallidas = fallidas) }
        }
    }

    private fun mensajeDeError(e: Exception): String {
        val detalle = e.message.orEmpty()
        return when {
            detalle.contains("row-level security", ignoreCase = true) ->
                "No tienes permiso para registrar mantenimientos en este vehículo"
            else -> "No se pudo guardar el mantenimiento. Intenta de nuevo"
        }
    }
}
