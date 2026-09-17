package com.transandina.flotilla.domain

import com.transandina.flotilla.data.model.FrecuenciaMantenimiento
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.Reasignacion
import com.transandina.flotilla.data.model.Vehiculo
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Urgencia de una alerta; el orden del enum es el orden en pantalla. */
enum class NivelAlerta { CRITICA, PROXIMA, INFORMATIVA }

/** Un documento que vence en estos días o menos ya cuenta como crítico. */
const val DIAS_DOCUMENTO_CRITICO = 7L

/** Cuánto tiempo sigue visible una reasignación como alerta informativa. */
const val DIAS_REASIGNACION_INFORMATIVA = 30L

/** Por qué existe la alerta. La pantalla arma el texto a partir de esto. */
sealed interface MotivoAlerta {
    data class DocumentoVencido(val documento: TipoDocumento, val fecha: LocalDate) : MotivoAlerta
    data class DocumentoPorVencer(val documento: TipoDocumento, val fecha: LocalDate) : MotivoAlerta
    data class MantenimientoAtrasado(val proximo: ProximoMantenimiento) : MotivoAlerta
    data class MantenimientoProximo(val proximo: ProximoMantenimiento) : MotivoAlerta
    data class ConductorReasignado(val nombreConductorNuevo: String?, val fecha: LocalDate) : MotivoAlerta
}

data class AlertaFlotilla(
    val nivel: NivelAlerta,
    val motivo: MotivoAlerta,
    val vehiculo: Vehiculo,
    /** Desempate dentro del mismo nivel: menor va primero. */
    val prioridad: Long
)

/**
 * Alertas de toda la flotilla, calculadas en el momento a partir de las
 * fechas de documentos, el kilometraje y las frecuencias de mantenimiento
 * (docs/ADAPTACION_MOVIL.md §8). No se guardan en la base.
 *
 * - Crítica: documento vencido o que vence en [DIAS_DOCUMENTO_CRITICO] días o
 *   menos; mantenimiento atrasado.
 * - Próxima: documento por vencer; mantenimiento próximo.
 * - Informativa: reasignaciones de los últimos [DIAS_REASIGNACION_INFORMATIVA] días.
 *
 * Solo se consideran los vehículos activos.
 */
fun calcularAlertasFlotilla(
    vehiculos: List<Vehiculo>,
    mantenimientos: List<Mantenimiento>,
    frecuencias: List<FrecuenciaMantenimiento>,
    reasignaciones: List<Reasignacion>,
    nombresPorUsuario: Map<String, String>,
    hoy: LocalDate = LocalDate.now()
): List<AlertaFlotilla> {
    val activos = vehiculos.filter { it.activo }
    val mantenimientosPorVehiculo = mantenimientos.groupBy { it.vehiculoId }
    val alertas = mutableListOf<AlertaFlotilla>()

    activos.forEach { vehiculo ->
        vehiculo.fechasDocumentos().forEach { (documento, fechaIso) ->
            val fecha = parsearFechaIso(fechaIso) ?: return@forEach
            val dias = ChronoUnit.DAYS.between(hoy, fecha)
            when (calcularEstadoDocumento(fechaIso, hoy)) {
                EstadoDocumento.VENCIDO -> alertas += AlertaFlotilla(
                    nivel = NivelAlerta.CRITICA,
                    motivo = MotivoAlerta.DocumentoVencido(documento, fecha),
                    vehiculo = vehiculo,
                    prioridad = dias
                )

                EstadoDocumento.PROXIMO -> alertas += AlertaFlotilla(
                    nivel = if (dias <= DIAS_DOCUMENTO_CRITICO) NivelAlerta.CRITICA else NivelAlerta.PROXIMA,
                    motivo = MotivoAlerta.DocumentoPorVencer(documento, fecha),
                    vehiculo = vehiculo,
                    prioridad = dias
                )

                else -> Unit
            }
        }

        calcularProximosMantenimientos(
            tipoVehiculo = vehiculo.tipo,
            kmActual = vehiculo.kmActual,
            mantenimientosDelVehiculo = mantenimientosPorVehiculo[vehiculo.id].orEmpty(),
            frecuencias = frecuencias,
            hoy = hoy
        ).forEach { proximo ->
            // Días si hay fecha; si no, cada 100 km cuentan como un día.
            val prioridad = proximo.diasRestantes
                ?: proximo.kmRestantes?.let { (it / 100).toLong() }
                ?: 0L
            when (proximo.estado) {
                EstadoMantenimiento.ATRASADO -> alertas += AlertaFlotilla(
                    nivel = NivelAlerta.CRITICA,
                    motivo = MotivoAlerta.MantenimientoAtrasado(proximo),
                    vehiculo = vehiculo,
                    prioridad = prioridad
                )

                EstadoMantenimiento.PROXIMO -> alertas += AlertaFlotilla(
                    nivel = NivelAlerta.PROXIMA,
                    motivo = MotivoAlerta.MantenimientoProximo(proximo),
                    vehiculo = vehiculo,
                    prioridad = prioridad
                )

                EstadoMantenimiento.AL_DIA -> Unit
            }
        }
    }

    val vehiculosPorId = activos.associateBy { it.id }
    val limiteInformativas = hoy.minusDays(DIAS_REASIGNACION_INFORMATIVA)
    reasignaciones.forEach { reasignacion ->
        val vehiculo = vehiculosPorId[reasignacion.vehiculoId] ?: return@forEach
        val fecha = parsearFechaIso(reasignacion.fechaEfectiva) ?: return@forEach
        if (fecha.isBefore(limiteInformativas)) return@forEach
        alertas += AlertaFlotilla(
            nivel = NivelAlerta.INFORMATIVA,
            motivo = MotivoAlerta.ConductorReasignado(
                nombreConductorNuevo = reasignacion.conductorNuevoId?.let { nombresPorUsuario[it] },
                fecha = fecha
            ),
            vehiculo = vehiculo,
            // Las más recientes primero.
            prioridad = -fecha.toEpochDay()
        )
    }

    return alertas.sortedWith(compareBy({ it.nivel }, { it.prioridad }))
}
