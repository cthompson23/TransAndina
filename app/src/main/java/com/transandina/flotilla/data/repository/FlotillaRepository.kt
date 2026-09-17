package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.FrecuenciaMantenimiento
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.data.model.Vehiculo
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** Todo lo que necesitan las pantallas de flotilla y de alertas del encargado. */
data class DatosFlotilla(
    val vehiculos: List<Vehiculo>,
    val usuarios: List<Usuario>,
    val mantenimientos: List<Mantenimiento>,
    val frecuencias: List<FrecuenciaMantenimiento>
)

/**
 * Junta las lecturas de varias tablas en paralelo. No agrega lógica: los
 * cálculos de estado viven en `domain/`.
 */
class FlotillaRepository(
    private val vehiculoRepository: VehiculoRepository = VehiculoRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository(),
    private val mantenimientoRepository: MantenimientoRepository = MantenimientoRepository()
) {

    suspend fun cargar(): DatosFlotilla = coroutineScope {
        val vehiculos = async { vehiculoRepository.obtenerFlotilla() }
        val usuarios = async { usuarioRepository.obtenerUsuarios() }
        val mantenimientos = async { mantenimientoRepository.obtenerTodos() }
        val frecuencias = async { mantenimientoRepository.obtenerFrecuencias() }
        DatosFlotilla(
            vehiculos = vehiculos.await(),
            usuarios = usuarios.await(),
            mantenimientos = mantenimientos.await(),
            frecuencias = frecuencias.await()
        )
    }
}
