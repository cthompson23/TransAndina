package com.transandina.flotilla.ui.alertas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.domain.AlertaFlotilla
import com.transandina.flotilla.domain.EstadoMantenimiento
import com.transandina.flotilla.domain.MotivoAlerta
import com.transandina.flotilla.domain.NivelAlerta
import com.transandina.flotilla.domain.ProximoMantenimiento
import com.transandina.flotilla.domain.TipoDocumento
import com.transandina.flotilla.domain.UMBRAL_KM_PROXIMO
import com.transandina.flotilla.domain.formatearFecha
import com.transandina.flotilla.domain.formatearKilometrosConUnidad
import com.transandina.flotilla.ui.components.BotonFlotante
import com.transandina.flotilla.ui.components.ChipsFiltro
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.NivelEstado
import com.transandina.flotilla.ui.components.TarjetaAlerta
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import java.time.LocalDate
import kotlin.math.abs

/** Orden de los chips: "Todas" primero (docs/ADAPTACION_MOVIL.md §6). */
private val FILTROS: List<NivelAlerta?> =
    listOf(null, NivelAlerta.CRITICA, NivelAlerta.PROXIMA, NivelAlerta.INFORMATIVA)

/**
 * Alertas de la flotilla (Figma `102:238`): los KPIs del escritorio se vuelven
 * chips con conteo y la lista va ordenada por urgencia. Tocar una alerta abre
 * el vehículo; el botón flotante envía un aviso de gerencia.
 */
@Composable
fun AlertasFlotillaScreen(
    viewModel: AlertasFlotillaViewModel = viewModel(),
    onAbrirVehiculo: (vehiculoId: String) -> Unit = {},
    onEnviarAviso: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargar()
    }

    ContenidoAlertasFlotilla(
        uiState = uiState,
        onRecargar = viewModel::cargar,
        onFiltroCambia = viewModel::onFiltroChange,
        onAbrirVehiculo = onAbrirVehiculo,
        onEnviarAviso = onEnviarAviso
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoAlertasFlotilla(
    uiState: AlertasFlotillaUiState,
    onRecargar: () -> Unit,
    onFiltroCambia: (NivelAlerta?) -> Unit,
    onAbrirVehiculo: (String) -> Unit,
    onEnviarAviso: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TransAndinaTopBar(titulo = stringResource(R.string.alertas_flotilla_titulo))

            PullToRefreshBox(
                isRefreshing = uiState.cargando && uiState.alertas.isNotEmpty(),
                onRefresh = onRecargar,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ChipsFiltro(
                            opciones = FILTROS.map { nivel ->
                                "${etiquetaFiltro(nivel)} (${uiState.conteo(nivel)})"
                            },
                            indiceSeleccionado = FILTROS.indexOf(uiState.filtro),
                            onSeleccionar = { onFiltroCambia(FILTROS[it]) }
                        )
                    }

                    val visibles = uiState.visibles
                    when {
                        uiState.cargando && uiState.alertas.isEmpty() -> item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        uiState.error != null && uiState.alertas.isEmpty() -> item {
                            EstadoVacio(mensaje = uiState.error)
                        }

                        visibles.isEmpty() -> item {
                            EstadoVacio(
                                mensaje = stringResource(R.string.alertas_flotilla_vacio),
                                icono = Icons.Filled.NotificationsNone
                            )
                        }

                        // Una misma unidad puede tener varias alertas: la clave
                        // combina posición y vehículo.
                        else -> itemsIndexed(
                            visibles,
                            key = { indice, alerta -> "$indice-${alerta.vehiculo.id}" }
                        ) { _, alerta ->
                            TarjetaAlerta(
                                titulo = tituloAlerta(alerta.motivo),
                                detalle = detalleAlerta(alerta),
                                textoNivel = etiquetaNivel(alerta.nivel),
                                nivel = nivelVisual(alerta.nivel),
                                onClick = { onAbrirVehiculo(alerta.vehiculo.id) }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }

        BotonFlotante(
            texto = stringResource(R.string.aviso_boton),
            icono = Icons.AutoMirrored.Filled.Send,
            onClick = onEnviarAviso,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun etiquetaFiltro(nivel: NivelAlerta?): String = when (nivel) {
    null -> stringResource(R.string.filtro_todas)
    NivelAlerta.CRITICA -> stringResource(R.string.alertas_filtro_criticas)
    NivelAlerta.PROXIMA -> stringResource(R.string.alertas_filtro_proximas)
    NivelAlerta.INFORMATIVA -> stringResource(R.string.alertas_filtro_informativas)
}

@Composable
private fun etiquetaNivel(nivel: NivelAlerta): String = when (nivel) {
    NivelAlerta.CRITICA -> stringResource(R.string.alerta_nivel_critica)
    NivelAlerta.PROXIMA -> stringResource(R.string.alerta_nivel_proxima)
    NivelAlerta.INFORMATIVA -> stringResource(R.string.alerta_nivel_informativa)
}

private fun nivelVisual(nivel: NivelAlerta): NivelEstado = when (nivel) {
    NivelAlerta.CRITICA -> NivelEstado.CRITICO
    NivelAlerta.PROXIMA -> NivelEstado.AVISO
    NivelAlerta.INFORMATIVA -> NivelEstado.INFO
}

@Composable
private fun tituloAlerta(motivo: MotivoAlerta): String = when (motivo) {
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
private fun detalleAlerta(alerta: AlertaFlotilla): String {
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

@Preview(name = "Alertas de la flotilla", showBackground = true, heightDp = 900)
@Composable
private fun AlertasFlotillaPreview() {
    val hoy = LocalDate.of(2026, 9, 17)
    fun vehiculo(id: String, placa: String, marca: String, modelo: String) = Vehiculo(
        id = id, placa = placa, marca = marca, modelo = modelo,
        anio = 2021, tipo = "liviano", kmActual = 100_000.0
    )
    val scd = vehiculo("1", "SCD-3421", "Nissan", "Frontier")
    val sjo = vehiculo("2", "SJO-2291", "Mitsubishi", "Fuso")
    val gte = vehiculo("3", "GTE-5518", "Nissan", "NV350")
    val alertas = listOf(
        AlertaFlotilla(
            NivelAlerta.CRITICA,
            MotivoAlerta.DocumentoVencido(TipoDocumento.PERMISO_CARGA, hoy.minusDays(38)),
            scd,
            -38
        ),
        AlertaFlotilla(
            NivelAlerta.PROXIMA,
            MotivoAlerta.MantenimientoProximo(
                ProximoMantenimiento(
                    categoria = "Cambio de aceite",
                    ultimaFecha = hoy.minusDays(60),
                    kmObjetivo = 100_800.0,
                    fechaObjetivo = hoy.plusDays(120),
                    kmRestantes = 800.0,
                    diasRestantes = 120,
                    estado = EstadoMantenimiento.PROXIMO
                )
            ),
            sjo,
            120
        ),
        AlertaFlotilla(
            NivelAlerta.INFORMATIVA,
            MotivoAlerta.ConductorReasignado("Sofía Blanco", hoy.minusDays(3)),
            gte,
            0
        )
    )
    TransAndinaFlotillaTheme {
        ContenidoAlertasFlotilla(
            uiState = AlertasFlotillaUiState(alertas = alertas),
            onRecargar = {},
            onFiltroCambia = {},
            onAbrirVehiculo = {},
            onEnviarAviso = {}
        )
    }
}
