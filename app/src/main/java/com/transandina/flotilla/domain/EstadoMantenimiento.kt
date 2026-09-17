package com.transandina.flotilla.domain

import com.transandina.flotilla.data.model.FrecuenciaMantenimiento
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.Vehiculo
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Semáforo de mantenimiento de un vehículo (Figma `102:158`). */
enum class EstadoMantenimiento { AL_DIA, PROXIMO, ATRASADO }

/** Faltando esto o menos, el servicio cuenta como "próximo". */
const val UMBRAL_KM_PROXIMO = 1_000.0
const val UMBRAL_DIAS_PROXIMO = 15L

/**
 * Cuándo toca el siguiente servicio de una categoría. Toca por kilometraje o
 * por fecha, lo que ocurra primero; cualquiera de los dos puede faltar si la
 * frecuencia no lo define.
 *
 * @property kmRestantes negativo si ya se pasó.
 * @property diasRestantes negativo si ya se pasó.
 */
data class ProximoMantenimiento(
    val categoria: String,
    val ultimaFecha: LocalDate,
    val kmObjetivo: Double?,
    val fechaObjetivo: LocalDate?,
    val kmRestantes: Double?,
    val diasRestantes: Long?,
    val estado: EstadoMantenimiento
)

/**
 * Calcula el próximo servicio de cada categoría con frecuencia definida para
 * el tipo del vehículo. Solo se consideran las categorías que ya tienen al
 * menos un mantenimiento registrado: sin un punto de partida no hay forma de
 * saber cuándo toca, y marcar todo como atrasado sería engañoso.
 *
 * Cualquier mantenimiento de la categoría (preventivo o correctivo) reinicia
 * la cuenta.
 */
fun calcularProximosMantenimientos(
    tipoVehiculo: String,
    kmActual: Double,
    mantenimientosDelVehiculo: List<Mantenimiento>,
    frecuencias: List<FrecuenciaMantenimiento>,
    hoy: LocalDate = LocalDate.now()
): List<ProximoMantenimiento> {
    val ultimoPorCategoria = mantenimientosDelVehiculo
        .mapNotNull { m -> parsearFechaIso(m.fecha)?.let { fecha -> m to fecha } }
        .groupBy { (m, _) -> m.categoria }
        .mapValues { (_, lista) -> lista.maxBy { (_, fecha) -> fecha } }

    return frecuencias
        .filter { it.tipoVehiculo == tipoVehiculo }
        .filter { it.kmFrecuencia != null || it.diasFrecuencia != null }
        .mapNotNull { frecuencia ->
            val (ultimo, ultimaFecha) = ultimoPorCategoria[frecuencia.categoria]
                ?: return@mapNotNull null

            val kmObjetivo = frecuencia.kmFrecuencia?.let { ultimo.km + it }
            val fechaObjetivo = frecuencia.diasFrecuencia?.let { ultimaFecha.plusDays(it.toLong()) }
            val kmRestantes = kmObjetivo?.let { it - kmActual }
            val diasRestantes = fechaObjetivo?.let { ChronoUnit.DAYS.between(hoy, it) }

            val estado = when {
                (kmRestantes != null && kmRestantes <= 0) ||
                    (diasRestantes != null && diasRestantes < 0) -> EstadoMantenimiento.ATRASADO

                (kmRestantes != null && kmRestantes <= UMBRAL_KM_PROXIMO) ||
                    (diasRestantes != null && diasRestantes <= UMBRAL_DIAS_PROXIMO) ->
                    EstadoMantenimiento.PROXIMO

                else -> EstadoMantenimiento.AL_DIA
            }

            ProximoMantenimiento(
                categoria = frecuencia.categoria,
                ultimaFecha = ultimaFecha,
                kmObjetivo = kmObjetivo,
                fechaObjetivo = fechaObjetivo,
                kmRestantes = kmRestantes,
                diasRestantes = diasRestantes,
                estado = estado
            )
        }
}

/** Los cuatro documentos legales que se guardan como fechas en `vehiculos`. */
enum class TipoDocumento { MARCHAMO, REVISION_TECNICA, SEGURO, PERMISO_CARGA }

/** Fecha de vencimiento (ISO o null) de cada documento, en el orden del Figma. */
fun Vehiculo.fechasDocumentos(): List<Pair<TipoDocumento, String?>> = listOf(
    TipoDocumento.MARCHAMO to fechaMarchamo,
    TipoDocumento.REVISION_TECNICA to fechaRevisionTecnica,
    TipoDocumento.SEGURO to fechaSeguro,
    TipoDocumento.PERMISO_CARGA to fechaPermisoCarga
)

/** Resumen de un vehículo para la lista de flotilla. */
data class ResumenVehiculo(
    val vehiculo: Vehiculo,
    val nombreConductor: String?,
    val ultimoMantenimiento: Mantenimiento?,
    val estadoMantenimiento: EstadoMantenimiento,
    val tieneDocumentosPendientes: Boolean,
    /** El peor entre mantenimiento y documentos: es el chip de la tarjeta. */
    val estadoGeneral: EstadoMantenimiento
)

fun resumirVehiculo(
    vehiculo: Vehiculo,
    nombreConductor: String?,
    mantenimientosDelVehiculo: List<Mantenimiento>,
    frecuencias: List<FrecuenciaMantenimiento>,
    hoy: LocalDate = LocalDate.now()
): ResumenVehiculo {
    val proximos = calcularProximosMantenimientos(
        tipoVehiculo = vehiculo.tipo,
        kmActual = vehiculo.kmActual,
        mantenimientosDelVehiculo = mantenimientosDelVehiculo,
        frecuencias = frecuencias,
        hoy = hoy
    )
    val estadoMantenimiento = proximos.maxOfOrNull { it.estado } ?: EstadoMantenimiento.AL_DIA

    val estadosDocumentos = vehiculo.fechasDocumentos()
        .map { (_, fecha) -> calcularEstadoDocumento(fecha, hoy) }
    val estadoDocumentos = when {
        EstadoDocumento.VENCIDO in estadosDocumentos -> EstadoMantenimiento.ATRASADO
        EstadoDocumento.PROXIMO in estadosDocumentos -> EstadoMantenimiento.PROXIMO
        else -> EstadoMantenimiento.AL_DIA
    }

    return ResumenVehiculo(
        vehiculo = vehiculo,
        nombreConductor = nombreConductor,
        ultimoMantenimiento = mantenimientosDelVehiculo.maxByOrNull { it.fecha },
        estadoMantenimiento = estadoMantenimiento,
        tieneDocumentosPendientes = estadoDocumentos != EstadoMantenimiento.AL_DIA,
        estadoGeneral = maxOf(estadoMantenimiento, estadoDocumentos)
    )
}
