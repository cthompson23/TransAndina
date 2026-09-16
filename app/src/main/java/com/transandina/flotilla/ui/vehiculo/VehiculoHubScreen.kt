package com.transandina.flotilla.ui.vehiculo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.TarjetaOpcionMenu
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme

/**
 * Hub del vehículo del conductor (Figma `70:133`): una lista de opciones que
 * llevan al detalle o a registrar kilometraje.
 *
 * "Registrar vehículo" no aparece: es del encargado (Fase 4). "Registrar
 * mantenimiento" tampoco, hasta que exista la pantalla (Parte B).
 */
@Composable
fun VehiculoHubScreen(
    viewModel: VehiculoViewModel = viewModel(),
    onAbrirDetalle: (vehiculoId: String, pestana: PestanaVehiculo) -> Unit = { _, _ -> },
    onRegistrarKilometraje: (vehiculoId: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    ContenidoHub(
        uiState = uiState,
        onAbrirDetalle = onAbrirDetalle,
        onRegistrarKilometraje = onRegistrarKilometraje
    )
}

@Composable
private fun ContenidoHub(
    uiState: VehiculoUiState,
    onAbrirDetalle: (String, PestanaVehiculo) -> Unit,
    onRegistrarKilometraje: (String) -> Unit
) {
    val vehiculo = uiState.vehiculo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = stringResource(R.string.vehiculo_titulo),
            subtitulo = vehiculo?.let { "${it.placa} · ${it.marca} ${it.modelo}" }
        )

        when {
            uiState.cargando && vehiculo == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            vehiculo == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    EstadoVacio(
                        mensaje = uiState.error
                            ?: stringResource(R.string.vehiculo_sin_asignar)
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaOpcionMenu(
                        titulo = stringResource(R.string.vehiculo_opcion_informacion),
                        descripcion = stringResource(R.string.vehiculo_opcion_informacion_detalle),
                        onClick = { onAbrirDetalle(vehiculo.id, PestanaVehiculo.INFORMACION) }
                    )
                    TarjetaOpcionMenu(
                        titulo = stringResource(R.string.vehiculo_opcion_historial),
                        descripcion = stringResource(R.string.vehiculo_opcion_historial_detalle),
                        onClick = { onAbrirDetalle(vehiculo.id, PestanaVehiculo.HISTORIAL) }
                    )
                    TarjetaOpcionMenu(
                        titulo = stringResource(R.string.vehiculo_opcion_registrar_km),
                        descripcion = stringResource(R.string.vehiculo_opcion_registrar_km_detalle),
                        onClick = { onRegistrarKilometraje(vehiculo.id) }
                    )
                    TarjetaOpcionMenu(
                        titulo = stringResource(R.string.vehiculo_opcion_kilometraje),
                        descripcion = stringResource(R.string.vehiculo_opcion_kilometraje_detalle),
                        onClick = { onAbrirDetalle(vehiculo.id, PestanaVehiculo.KILOMETRAJE) }
                    )
                    TarjetaOpcionMenu(
                        titulo = stringResource(R.string.vehiculo_opcion_documentos),
                        descripcion = stringResource(R.string.vehiculo_opcion_documentos_detalle),
                        onClick = { onAbrirDetalle(vehiculo.id, PestanaVehiculo.DOCUMENTOS) }
                    )
                }
            }
        }
    }
}

internal val vehiculoDeMuestra = Vehiculo(
    id = "1",
    placa = "SCD-3421",
    marca = "Nissan",
    modelo = "Frontier",
    anio = 2021,
    tipo = "liviano",
    capacidad = 1.1,
    kmActual = 492_400.0,
    fechaMarchamo = "2026-12-31",
    fechaRevisionTecnica = "2026-09-15",
    fechaSeguro = null
)

@Preview(name = "Hub con vehículo", showBackground = true, heightDp = 700)
@Composable
private fun VehiculoHubPreview() {
    TransAndinaFlotillaTheme {
        ContenidoHub(
            uiState = VehiculoUiState(vehiculo = vehiculoDeMuestra),
            onAbrirDetalle = { _, _ -> },
            onRegistrarKilometraje = {}
        )
    }
}

@Preview(name = "Hub cargando", showBackground = true, heightDp = 400)
@Composable
private fun VehiculoHubCargandoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoHub(
            uiState = VehiculoUiState(cargando = true),
            onAbrirDetalle = { _, _ -> },
            onRegistrarKilometraje = {}
        )
    }
}

@Preview(name = "Hub sin vehículo", showBackground = true, heightDp = 400)
@Composable
private fun VehiculoHubSinVehiculoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoHub(
            uiState = VehiculoUiState(),
            onAbrirDetalle = { _, _ -> },
            onRegistrarKilometraje = {}
        )
    }
}

@Preview(name = "Hub con error", showBackground = true, heightDp = 400)
@Composable
private fun VehiculoHubErrorPreview() {
    TransAndinaFlotillaTheme {
        ContenidoHub(
            uiState = VehiculoUiState(error = "No se pudo cargar la información del vehículo"),
            onAbrirDetalle = { _, _ -> },
            onRegistrarKilometraje = {}
        )
    }
}
