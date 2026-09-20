package com.transandina.flotilla.ui.vehiculo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.transandina.flotilla.data.model.PersonaResumen
import com.transandina.flotilla.ui.components.CampoSeleccion
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.FilaBotonesFormulario
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Elegir el mecánico responsable de un vehículo. Entran aquí el encargado y
 * el conductor del vehículo, desde la pestaña Información de su detalle.
 */
@Composable
fun AsignarMecanicoScreen(
    vehiculoId: String,
    viewModel: AsignarMecanicoViewModel = viewModel(),
    onAtras: () -> Unit = {},
    onGuardado: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vehiculoId) {
        viewModel.cargar(vehiculoId)
    }

    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) onGuardado()
    }

    ContenidoAsignarMecanico(
        uiState = uiState,
        onSeleccionar = viewModel::onSeleccionar,
        onGuardar = viewModel::guardar,
        onCancelar = onAtras
    )
}

@Composable
private fun ContenidoAsignarMecanico(
    uiState: AsignarMecanicoUiState,
    onSeleccionar: (String?) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    val editable = !uiState.guardando && !uiState.cargando

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = stringResource(R.string.asignar_mecanico_titulo),
            subtitulo = uiState.vehiculo?.let { "${it.placa} · ${it.marca} ${it.modelo}" },
            onAtras = onCancelar
        )

        when {
            uiState.cargando -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            uiState.vehiculo == null -> Box(modifier = Modifier.padding(16.dp)) {
                EstadoVacio(
                    mensaje = uiState.error ?: stringResource(R.string.detalle_no_encontrado)
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.asignar_mecanico_actual,
                        uiState.nombreDe(uiState.vehiculo.mecanicoId)
                            ?: stringResource(R.string.sin_mecanico)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                val sinMecanico = stringResource(R.string.sin_mecanico)
                CampoSeleccion(
                    etiqueta = stringResource(R.string.detalle_mecanico_asignado),
                    seleccion = uiState.nombreDe(uiState.seleccionadoId) ?: sinMecanico,
                    opciones = listOf(sinMecanico) + uiState.mecanicos.map { it.nombreCompleto },
                    onSeleccionar = { nombre ->
                        onSeleccionar(uiState.mecanicos.find { it.nombreCompleto == nombre }?.id)
                    },
                    marcadorDePosicion = sinMecanico,
                    forma = FormaPildora,
                    habilitado = editable
                )

                Text(
                    text = if (uiState.mecanicos.isEmpty()) {
                        stringResource(R.string.asignar_mecanico_vacio)
                    } else {
                        stringResource(R.string.asignar_mecanico_ayuda)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TransAndinaTheme.colores.textoSecundario
                )

                uiState.error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                FilaBotonesFormulario(
                    textoAccion = stringResource(R.string.guardar),
                    onAccion = onGuardar,
                    onCancelar = onCancelar,
                    textoCancelar = stringResource(R.string.cancelar),
                    accionHabilitada = editable,
                    cargando = uiState.guardando
                )
            }
        }
    }
}

private val mecanicosDeMuestra = listOf(
    PersonaResumen(id = "m1", nombreCompleto = "Marco Ureña Rojas"),
    PersonaResumen(id = "m2", nombreCompleto = "Sofía Calderón Mora")
)

@Preview(name = "Asignar mecánico", showBackground = true, heightDp = 500)
@Composable
private fun AsignarMecanicoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoAsignarMecanico(
            uiState = AsignarMecanicoUiState(
                vehiculo = vehiculoDeMuestra,
                mecanicos = mecanicosDeMuestra,
                seleccionadoId = "m1"
            ),
            onSeleccionar = {},
            onGuardar = {},
            onCancelar = {}
        )
    }
}

@Preview(name = "Asignar mecánico · sin mecánicos", showBackground = true, heightDp = 500)
@Composable
private fun AsignarMecanicoVacioPreview() {
    TransAndinaFlotillaTheme {
        ContenidoAsignarMecanico(
            uiState = AsignarMecanicoUiState(vehiculo = vehiculoDeMuestra),
            onSeleccionar = {},
            onGuardar = {},
            onCancelar = {}
        )
    }
}
