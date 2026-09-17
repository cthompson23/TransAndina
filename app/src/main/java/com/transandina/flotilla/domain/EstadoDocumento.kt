package com.transandina.flotilla.domain

import java.time.LocalDate

/** Semáforo de un documento legal del vehículo. */
enum class EstadoDocumento { AL_DIA, PROXIMO, VENCIDO, SIN_DATO }

/**
 * Calcula el estado de un documento (marchamo, revisión técnica, seguro)
 * comparando su fecha de vencimiento contra hoy. Es una función pura, así que
 * la usan tanto Inicio como el detalle del vehículo.
 *
 * Las fechas llegan de Supabase como `date` en ISO (yyyy-MM-dd); cualquier
 * cosa que no se pueda interpretar cuenta como "sin dato".
 */
fun calcularEstadoDocumento(
    fechaIso: String?,
    hoy: LocalDate = LocalDate.now()
): EstadoDocumento {
    if (fechaIso == null) return EstadoDocumento.SIN_DATO
    return try {
        val fecha = LocalDate.parse(fechaIso)
        when {
            fecha.isBefore(hoy) -> EstadoDocumento.VENCIDO
            fecha.isBefore(hoy.plusDays(15)) -> EstadoDocumento.PROXIMO
            else -> EstadoDocumento.AL_DIA
        }
    } catch (e: Exception) {
        EstadoDocumento.SIN_DATO
    }
}
