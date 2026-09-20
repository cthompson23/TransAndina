package com.transandina.flotilla.ui.notificaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.Alerta
import com.transandina.flotilla.data.model.TipoAlerta
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.domain.AlertaFlotilla
import com.transandina.flotilla.domain.MotivoAlerta
import com.transandina.flotilla.domain.NivelAlerta
import com.transandina.flotilla.domain.TipoDocumento
import com.transandina.flotilla.domain.formatearFechaIso
import com.transandina.flotilla.ui.alertas.detalleAlerta
import com.transandina.flotilla.ui.alertas.etiquetaNivel
import com.transandina.flotilla.ui.alertas.nivelVisual
import com.transandina.flotilla.ui.alertas.pestanaDeAlerta
import com.transandina.flotilla.ui.alertas.tituloAlerta
import com.transandina.flotilla.ui.alertas.tituloAlertaGuardada
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.NivelEstado
import com.transandina.flotilla.ui.components.TarjetaAlerta
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme
import com.transandina.flotilla.ui.vehiculo.PestanaVehiculo
import java.time.LocalDate

/**
 * Alertas del conductor (Figma `28:312`). El título del Figma dice "Datos
 * personales" por error; el correcto es "Mis alertas".
 *
 * Arriba van las alertas de su vehículo, ordenadas por urgencia, y debajo los
 * avisos que le llegaron (gerencia, reasignaciones, mantenimientos
 * registrados). Tocar una alerta abre la pestaña correspondiente del vehículo.
 */
@Composable
fun NotificacionesScreen(
    viewModel: NotificacionesViewModel = viewModel(),
    onAbrirVehiculo: (vehiculoId: String, pestana: PestanaVehiculo) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargar()
    }

    ContenidoNotificaciones(
        uiState = uiState,
        onRecargar = viewModel::cargar,
        onAbrirVehiculo = onAbrirVehiculo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoNotificaciones(
    uiState: NotificacionesUiState,
    onRecargar: () -> Unit,
    onAbrirVehiculo: (String, PestanaVehiculo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(titulo = stringResource(R.string.alertas_titulo))

        PullToRefreshBox(
            isRefreshing = uiState.cargando && !uiState.vacio,
            onRefresh = onRecargar,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    uiState.cargando && uiState.vacio -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    uiState.error != null && uiState.vacio -> item {
                        EstadoVacio(mensaje = uiState.error)
                    }

                    uiState.vacio -> item { SinAlertas() }

                    else -> {
                        // Una misma unidad puede tener varias alertas: la clave
                        // combina posición y vehículo.
                        itemsIndexed(
                            uiState.calculadas,
                            key = { indice, alerta -> "calc-$indice-${alerta.vehiculo.id}" }
                        ) { _, alerta ->
                            TarjetaAlerta(
                                titulo = tituloAlerta(alerta.motivo),
                                detalle = detalleAlerta(alerta),
                                textoNivel = etiquetaNivel(alerta.nivel),
                                nivel = nivelVisual(alerta.nivel),
                                onClick = {
                                    onAbrirVehiculo(alerta.vehiculo.id, pestanaDeAlerta(alerta.motivo))
                                }
                            )
                        }

                        items(uiState.guardadas, key = { it.id }) { aviso ->
                            TarjetaAlerta(
                                titulo = tituloAlertaGuardada(aviso),
                                detalle = aviso.mensaje,
                                textoNivel = stringResource(R.string.alerta_nivel_informativa),
                                nivel = NivelEstado.INFO,
                                fecha = formatearFechaIso(aviso.creadaEn.take(10)),
                                onClick = aviso.vehiculoId
                                    ?.takeIf { uiState.puedeAbrir(it) }
                                    ?.let { id -> { onAbrirVehiculo(id, PestanaVehiculo.INFORMACION) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SinAlertas() {
    TarjetaTransAndina {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EstadoVacio(
                mensaje = stringResource(R.string.alertas_vacio),
                icono = Icons.Filled.NotificationsNone
            )
            Text(
                text = stringResource(R.string.alertas_vacio_detalle),
                style = MaterialTheme.typography.bodySmall,
                color = TransAndinaTheme.colores.textoSecundario,
                textAlign = TextAlign.Center
            )
        }
    }
}

private val vehiculoDeMuestra = Vehiculo(
    id = "1",
    placa = "SCD-3421",
    marca = "Nissan",
    modelo = "Frontier",
    anio = 2021,
    tipo = "liviano",
    kmActual = 492_400.0
)

@Preview(name = "Mis alertas", showBackground = true, heightDp = 700)
@Composable
private fun NotificacionesPreview() {
    val hoy = LocalDate.of(2026, 9, 17)
    TransAndinaFlotillaTheme {
        ContenidoNotificaciones(
            uiState = NotificacionesUiState(
                vehiculos = listOf(vehiculoDeMuestra),
                calculadas = listOf(
                    AlertaFlotilla(
                        NivelAlerta.CRITICA,
                        MotivoAlerta.DocumentoVencido(TipoDocumento.REVISION_TECNICA, hoy.minusDays(4)),
                        vehiculoDeMuestra,
                        -4
                    )
                ),
                guardadas = listOf(
                    Alerta(
                        id = "a1",
                        tipo = TipoAlerta.gerencia,
                        usuarioId = "c1",
                        mensaje = "Recuerden actualizar el kilometraje antes del viernes.",
                        creadaEn = "2026-09-15T10:00:00+00:00"
                    ),
                    Alerta(
                        id = "a2",
                        tipo = TipoAlerta.reasignacion,
                        vehiculoId = "1",
                        usuarioId = "c1",
                        mensaje = "Se te asignó el vehículo con placa SCD-3421",
                        creadaEn = "2026-09-10T08:30:00+00:00"
                    )
                )
            ),
            onRecargar = {},
            onAbrirVehiculo = { _, _ -> }
        )
    }
}

@Preview(name = "Mis alertas · sin datos", showBackground = true, heightDp = 500)
@Composable
private fun NotificacionesVaciasPreview() {
    TransAndinaFlotillaTheme {
        ContenidoNotificaciones(
            uiState = NotificacionesUiState(vehiculos = listOf(vehiculoDeMuestra)),
            onRecargar = {},
            onAbrirVehiculo = { _, _ -> }
        )
    }
}
