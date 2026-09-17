package com.transandina.flotilla.domain

import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.TipoMantenimiento
import java.time.LocalDate

/** Filtros del reporte de mantenimientos (Figma `102:198`). Null = todos. */
data class FiltrosReporte(
    val vehiculoId: String? = null,
    val tipo: TipoMantenimiento? = null,
    val taller: String? = null,
    val desde: LocalDate? = null,
    val hasta: LocalDate? = null
)

/**
 * Aplica los filtros y ordena del más reciente al más antiguo. Las fechas son
 * inclusivas. Si hay filtro de fecha, los registros con fecha ilegible quedan
 * fuera; el taller se compara sin distinguir mayúsculas ni espacios.
 */
fun filtrarMantenimientos(
    mantenimientos: List<Mantenimiento>,
    filtros: FiltrosReporte
): List<Mantenimiento> {
    val tallerBuscado = filtros.taller?.trim()?.lowercase()

    return mantenimientos
        .filter { filtros.vehiculoId == null || it.vehiculoId == filtros.vehiculoId }
        .filter { filtros.tipo == null || it.tipo == filtros.tipo }
        .filter { tallerBuscado == null || it.taller?.trim()?.lowercase() == tallerBuscado }
        .filter { m ->
            if (filtros.desde == null && filtros.hasta == null) return@filter true
            val fecha = parsearFechaIso(m.fecha) ?: return@filter false
            (filtros.desde == null || !fecha.isBefore(filtros.desde)) &&
                (filtros.hasta == null || !fecha.isAfter(filtros.hasta))
        }
        .sortedByDescending { it.fecha }
}

/** Suma de costos; los mantenimientos sin costo cuentan como cero. */
fun costoTotal(mantenimientos: List<Mantenimiento>): Double =
    mantenimientos.sumOf { it.costo ?: 0.0 }

/** Talleres distintos que aparecen en los registros, para el filtro. */
fun talleresRegistrados(mantenimientos: List<Mantenimiento>): List<String> =
    mantenimientos
        .mapNotNull { it.taller?.trim()?.takeIf(String::isNotEmpty) }
        .distinctBy { it.lowercase() }
        .sortedBy { it.lowercase() }
