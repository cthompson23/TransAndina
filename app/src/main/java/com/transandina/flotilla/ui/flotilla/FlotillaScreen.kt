package com.transandina.flotilla.ui.flotilla

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.TipoMantenimiento
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.domain.EstadoMantenimiento
import com.transandina.flotilla.domain.ResumenVehiculo
import com.transandina.flotilla.domain.formatearFechaIso
import com.transandina.flotilla.domain.formatearKilometrosConUnidad
import com.transandina.flotilla.ui.components.BotonFlotante
import com.transandina.flotilla.ui.components.CampoBusqueda
import com.transandina.flotilla.ui.components.ChipEstado
import com.transandina.flotilla.ui.components.ChipEstadoMantenimiento
import com.transandina.flotilla.ui.components.ChipsFiltro
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.NivelEstado
import com.transandina.flotilla.ui.components.TarjetaKpi
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Estado de la flotilla del encargado (Figma `102:158`, adaptado a móvil):
 * KPIs compactos, buscador, chips de estado y la tabla convertida en tarjetas.
 * Tocar un vehículo abre su detalle.
 */
@Composable
fun FlotillaScreen(
    viewModel: FlotillaViewModel = viewModel(),
    onAbrirVehiculo: (vehiculoId: String) -> Unit = {},
    onRegistrarVehiculo: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Se recarga cada vez que se vuelve a la pestaña, para reflejar lo que se
    // haya cambiado en el detalle o en el formulario.
    LaunchedEffect(Unit) {
        viewModel.cargar()
    }

    ContenidoFlotilla(
        uiState = uiState,
        onRecargar = viewModel::cargar,
        onBusquedaCambia = viewModel::onBusquedaChange,
        onFiltroCambia = viewModel::onFiltroChange,
        onAbrirVehiculo = onAbrirVehiculo,
        onRegistrarVehiculo = onRegistrarVehiculo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoFlotilla(
    uiState: FlotillaUiState,
    onRecargar: () -> Unit,
    onBusquedaCambia: (String) -> Unit,
    onFiltroCambia: (FiltroFlotilla) -> Unit,
    onAbrirVehiculo: (String) -> Unit,
    onRegistrarVehiculo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TransAndinaTopBar(titulo = stringResource(R.string.flotilla_titulo))

            PullToRefreshBox(
                isRefreshing = uiState.cargando && uiState.resumenes.isNotEmpty(),
                onRefresh = onRecargar,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        FilaKpis(uiState)
                    }
                    item {
                        CampoBusqueda(
                            valor = uiState.busqueda,
                            onValorCambia = onBusquedaCambia,
                            marcadorDePosicion = stringResource(R.string.flotilla_buscar)
                        )
                    }
                    item {
                        ChipsFiltro(
                            opciones = FiltroFlotilla.entries.map { filtro ->
                                "${etiquetaFiltro(filtro)} (${uiState.conteo(filtro)})"
                            },
                            indiceSeleccionado = uiState.filtro.ordinal,
                            onSeleccionar = { onFiltroCambia(FiltroFlotilla.entries[it]) }
                        )
                    }

                    val visibles = uiState.visibles
                    when {
                        uiState.cargando && uiState.resumenes.isEmpty() -> item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        uiState.error != null && uiState.resumenes.isEmpty() -> item {
                            EstadoVacio(mensaje = uiState.error)
                        }

                        visibles.isEmpty() -> item {
                            EstadoVacio(
                                mensaje = if (uiState.resumenes.isEmpty()) {
                                    stringResource(R.string.flotilla_vacia)
                                } else {
                                    stringResource(R.string.flotilla_sin_coincidencias)
                                }
                            )
                        }

                        else -> items(visibles, key = { it.vehiculo.id }) { resumen ->
                            TarjetaVehiculoFlotilla(
                                resumen = resumen,
                                onClick = { onAbrirVehiculo(resumen.vehiculo.id) }
                            )
                        }
                    }

                    // Espacio para que el botón flotante no tape la última tarjeta.
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }

        BotonFlotante(
            texto = stringResource(R.string.flotilla_registrar_vehiculo),
            icono = Icons.Filled.Add,
            onClick = onRegistrarVehiculo,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun FilaKpis(uiState: FlotillaUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TarjetaKpi(
            etiqueta = stringResource(R.string.flotilla_kpi_activos),
            valor = uiState.vehiculosActivos.toString(),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        TarjetaKpi(
            etiqueta = stringResource(R.string.flotilla_kpi_mantenimientos),
            valor = uiState.mantenimientosPendientes.toString(),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        TarjetaKpi(
            etiqueta = stringResource(R.string.flotilla_kpi_documentos),
            valor = uiState.documentosPendientes.toString(),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun etiquetaFiltro(filtro: FiltroFlotilla): String = when (filtro) {
    FiltroFlotilla.TODOS -> stringResource(R.string.filtro_todos)
    FiltroFlotilla.ATRASADO -> stringResource(R.string.estado_atrasado)
    FiltroFlotilla.PROXIMO -> stringResource(R.string.estado_proximo)
    FiltroFlotilla.AL_DIA -> stringResource(R.string.estado_al_dia)
}

/** Fila de la tabla del escritorio convertida en tarjeta (docs/ADAPTACION_MOVIL.md §4). */
@Composable
private fun TarjetaVehiculoFlotilla(
    resumen: ResumenVehiculo,
    onClick: () -> Unit
) {
    val vehiculo = resumen.vehiculo

    TarjetaTransAndina(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = vehiculo.placa,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (vehiculo.activo) {
                ChipEstadoMantenimiento(resumen.estadoGeneral)
            } else {
                ChipEstado(
                    texto = stringResource(R.string.flotilla_inactivo),
                    nivel = NivelEstado.NEUTRO
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.flotilla_vehiculo_conductor,
                "${vehiculo.marca} ${vehiculo.modelo}",
                resumen.nombreConductor ?: stringResource(R.string.sin_conductor)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = resumen.ultimoMantenimiento
                ?.let {
                    stringResource(
                        R.string.flotilla_ultimo_mantenimiento,
                        formatearFechaIso(it.fecha),
                        formatearKilometrosConUnidad(vehiculo.kmActual)
                    )
                }
                ?: stringResource(
                    R.string.flotilla_sin_mantenimientos,
                    formatearKilometrosConUnidad(vehiculo.kmActual)
                ),
            style = MaterialTheme.typography.bodySmall,
            color = TransAndinaTheme.colores.textoSecundario
        )
    }
}

private fun resumenDeMuestra(
    id: String,
    placa: String,
    marca: String,
    modelo: String,
    conductor: String?,
    estado: EstadoMantenimiento,
    km: Double,
    fechaUltimo: String?
) = ResumenVehiculo(
    vehiculo = Vehiculo(
        id = id,
        placa = placa,
        marca = marca,
        modelo = modelo,
        anio = 2021,
        tipo = "liviano",
        kmActual = km
    ),
    nombreConductor = conductor,
    ultimoMantenimiento = fechaUltimo?.let {
        Mantenimiento(
            id = "m$id",
            vehiculoId = id,
            registradoPor = "u1",
            tipo = TipoMantenimiento.preventivo,
            categoria = "Cambio de aceite",
            fecha = it,
            km = km
        )
    },
    estadoMantenimiento = estado,
    tieneDocumentosPendientes = estado == EstadoMantenimiento.ATRASADO,
    estadoGeneral = estado
)

private val flotillaDeMuestra = FlotillaUiState(
    resumenes = listOf(
        resumenDeMuestra("1", "SCD-3421", "Nissan", "Frontier", "Carlos Fernández", EstadoMantenimiento.ATRASADO, 492_400.0, "2026-08-25"),
        resumenDeMuestra("2", "BQR-8890", "Toyota", "Hilux", "María Vargas", EstadoMantenimiento.AL_DIA, 310_250.0, "2026-08-12"),
        resumenDeMuestra("3", "TRA-1204", "Hino", "300", "Luis Solano", EstadoMantenimiento.PROXIMO, 688_900.0, "2026-08-02"),
        resumenDeMuestra("4", "CRC-7745", "Isuzu", "NPR", null, EstadoMantenimiento.AL_DIA, 145_600.0, null)
    )
)

@Preview(name = "Flotilla", showBackground = true, heightDp = 800)
@Composable
private fun FlotillaPreview() {
    TransAndinaFlotillaTheme {
        ContenidoFlotilla(
            uiState = flotillaDeMuestra,
            onRecargar = {},
            onBusquedaCambia = {},
            onFiltroCambia = {},
            onAbrirVehiculo = {},
            onRegistrarVehiculo = {}
        )
    }
}

@Preview(name = "Flotilla · cargando", showBackground = true, heightDp = 500)
@Composable
private fun FlotillaCargandoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoFlotilla(
            uiState = FlotillaUiState(cargando = true),
            onRecargar = {},
            onBusquedaCambia = {},
            onFiltroCambia = {},
            onAbrirVehiculo = {},
            onRegistrarVehiculo = {}
        )
    }
}
