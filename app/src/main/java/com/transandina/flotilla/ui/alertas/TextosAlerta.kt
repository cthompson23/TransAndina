package com.transandina.flotilla.ui.alertas

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.Alerta
import com.transandina.flotilla.data.model.TipoAlerta
import com.transandina.flotilla.domain.AlertaFlotilla
import com.transandina.flotilla.domain.MotivoAlerta
import com.transandina.flotilla.domain.NivelAlerta
import com.transandina.flotilla.domain.TipoDocumento
import com.transandina.flotilla.domain.UMBRAL_KM_PROXIMO
import com.transandina.flotilla.domain.formatearFecha
import com.transandina.flotilla.domain.formatearKilometrosConUnidad
import com.transandina.flotilla.ui.components.NivelEstado
import com.transandina.flotilla.ui.vehiculo.PestanaVehiculo
import kotlin.math.abs

// Textos de las alertas, compartidos por el panel del encargado
// (Figma `102:238`) y "Mis alertas" del conductor (Figma `28:312`).

@Composable
internal fun etiquetaNivel(nivel: NivelAlerta): String = when (nivel) {
    NivelAlerta.CRITICA -> stringResource(R.string.alerta_nivel_critica)
    NivelAlerta.PROXIMA -> stringResource(R.string.alerta_nivel_proxima)
    NivelAlerta.INFORMATIVA -> stringResource(R.string.alerta_nivel_informativa)
}

internal fun nivelVisual(nivel: NivelAlerta): NivelEstado = when (nivel) {
    NivelAlerta.CRITICA -> NivelEstado.CRITICO
    NivelAlerta.PROXIMA -> NivelEstado.AVISO
    NivelAlerta.INFORMATIVA -> NivelEstado.INFO
}

/** A qué pestaña del vehículo lleva cada alerta al tocarla. */
internal fun pestanaDeAlerta(motivo: MotivoAlerta): PestanaVehiculo = when (motivo) {
    is MotivoAlerta.DocumentoVencido, is MotivoAlerta.DocumentoPorVencer -> PestanaVehiculo.DOCUMENTOS
    is MotivoAlerta.MantenimientoAtrasado, is MotivoAlerta.MantenimientoProximo -> PestanaVehiculo.HISTORIAL
    is MotivoAlerta.ConductorReasignado -> PestanaVehiculo.INFORMACION
}

@Composable
internal fun tituloAlerta(motivo: MotivoAlerta): String = when (motivo) {
    is MotivoAlerta.DocumentoVencido -> when (motivo.documento) {
        TipoDocumento.MARCHAMO -> stringResource(R.string.alerta_vencido_marchamo)
        TipoDocumento.REVISION_TECNICA -> stringResource(R.string.alerta_vencido_revision)
        TipoDocumento.SEGURO -> stringResource(R.string.alerta_vencido_seguro)
        TipoDocumento.PERMISO_CARGA -> stringResource(R.string.alerta_vencido_permiso)
    }
    is MotivoAlerta.DocumentoPorVencer -> when (motivo.documento) {
        TipoDocumento.MARCHAMO -> stringResource(R.string.alerta_por_vencer_marchamo)
        TipoDocumento.REVISION_TECNICA -> stringResource(R.string.alerta_por_vencer_revision)
        TipoDocumento.SEGURO -> stringResource(R.string.alerta_por_vencer_seguro)
        TipoDocumento.PERMISO_CARGA -> stringResource(R.string.alerta_por_vencer_permiso)
    }
    is MotivoAlerta.MantenimientoAtrasado -> stringResource(R.string.alerta_mantenimiento_atrasado)
    is MotivoAlerta.MantenimientoProximo -> stringResource(R.string.alerta_mantenimiento_proximo)
    is MotivoAlerta.ConductorReasignado -> stringResource(R.string.alerta_reasignacion)
}

@Composable
internal fun detalleAlerta(alerta: AlertaFlotilla): String {
    val vehiculo = alerta.vehiculo
    val nombreVehiculo = "${vehiculo.marca} ${vehiculo.modelo}"
    return when (val motivo = alerta.motivo) {
        is MotivoAlerta.DocumentoVencido -> stringResource(
            R.string.alerta_detalle_vencio,
            vehiculo.placa,
            nombreVehiculo,
            formatearFecha(motivo.fecha)
        )

        is MotivoAlerta.DocumentoPorVencer -> stringResource(
            R.string.alerta_detalle_vence,
            vehiculo.placa,
            nombreVehiculo,
            formatearFecha(motivo.fecha)
        )

        is MotivoAlerta.MantenimientoAtrasado -> {
            val proximo = motivo.proximo
            val kmPasados = proximo.kmRestantes?.takeIf { it <= 0 }
            val fecha = proximo.fechaObjetivo
            when {
                fecha != null && (proximo.diasRestantes ?: 0) < 0 -> stringResource(
                    R.string.alerta_detalle_atrasado_fecha,
                    vehiculo.placa,
                    proximo.categoria,
                    formatearFecha(fecha)
                )
                kmPasados != null -> stringResource(
                    R.string.alerta_detalle_atrasado_km,
                    vehiculo.placa,
                    proximo.categoria,
                    formatearKilometrosConUnidad(abs(kmPasados))
                )
                else -> "${vehiculo.placa} · ${proximo.categoria}"
            }
        }

        is MotivoAlerta.MantenimientoProximo -> {
            val proximo = motivo.proximo
            val kmRestantes = proximo.kmRestantes
            val fecha = proximo.fechaObjetivo
            when {
                kmRestantes != null && kmRestantes <= UMBRAL_KM_PROXIMO -> stringResource(
                    R.string.alerta_detalle_proximo_km,
                    vehiculo.placa,
                    proximo.categoria,
                    formatearKilometrosConUnidad(kmRestantes)
                )
                fecha != null -> stringResource(
                    R.string.alerta_detalle_proximo_fecha,
                    vehiculo.placa,
                    proximo.categoria,
                    formatearFecha(fecha)
                )
                else -> "${vehiculo.placa} · ${proximo.categoria}"
            }
        }

        is MotivoAlerta.ConductorReasignado -> motivo.nombreConductorNuevo
            ?.let {
                stringResource(
                    R.string.alerta_detalle_reasignado,
                    vehiculo.placa,
                    it,
                    formatearFecha(motivo.fecha)
                )
            }
            ?: stringResource(
                R.string.alerta_detalle_sin_conductor,
                vehiculo.placa,
                formatearFecha(motivo.fecha)
            )
    }
}

/** Título de un aviso guardado en la tabla `alertas`. */
@Composable
internal fun tituloAlertaGuardada(alerta: Alerta): String = when (alerta.tipo) {
    TipoAlerta.gerencia -> stringResource(R.string.alerta_guardada_gerencia)
    TipoAlerta.reasignacion -> stringResource(R.string.alerta_reasignacion)
    TipoAlerta.mantenimiento_confirmado -> stringResource(R.string.alerta_guardada_mantenimiento)
    TipoAlerta.mantenimiento_proximo -> stringResource(R.string.alerta_mantenimiento_proximo)
    TipoAlerta.documento_vencimiento -> stringResource(R.string.alerta_guardada_documento)
}
