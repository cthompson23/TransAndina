package com.transandina.flotilla.ui.kilometraje

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.KilometrajeHistorico
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.domain.formatearFechaIso
import com.transandina.flotilla.domain.formatearKilometrosConUnidad
import com.transandina.flotilla.ui.components.CampoFecha
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.FilaBotonesFormulario
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme
import java.time.LocalDate

/**
 * Registro del recorrido (Figma `51:174`): vehículo de solo lectura, el nuevo
 * kilometraje, la fecha y los botones anclados abajo.
 *
 * La regla de "mayor al último registrado" la aplica el trigger
 * `trg_validar_km` en Postgres; aquí solo se traduce su error.
 */
@Composable
fun KilometrajeScreen(
    vehiculoId: String? = null,
    viewModel: KilometrajeViewModel = viewModel(),
    onAtras: () -> Unit = {},
    onRegistroExitoso: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vehiculoId) {
        viewModel.cargar(vehiculoId)
    }

    LaunchedEffect(uiState.exito) {
        if (uiState.exito) onRegistroExitoso()
    }

    ContenidoRegistroKilometraje(
        uiState = uiState,
        onKmCambia = viewModel::onKmChange,
        onFechaCambia = viewModel::onFechaChange,
        onGuardar = viewModel::registrarKilometraje,
        onCancelar = onAtras
    )
}

@Composable
private fun ContenidoRegistroKilometraje(
    uiState: KilometrajeUiState,
    onKmCambia: (String) -> Unit,
    onFechaCambia: (LocalDate) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = stringResource(R.string.registro_km_titulo),
            onAtras = onCancelar
        )

        val vehiculo = uiState.vehiculo

        when {
            vehiculo == null && uiState.cargando -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(40.dp)
                    )
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
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CampoTexto(
                        etiqueta = stringResource(R.string.registro_km_vehiculo),
                        valor = vehiculo.placa,
                        onValorCambia = {},
                        forma = FormaPildora,
                        habilitado = false,
                        soloLectura = true
                    )

                    CampoTexto(
                        etiqueta = stringResource(R.string.registro_km_nuevo),
                        valor = uiState.kmIngresado,
                        onValorCambia = onKmCambia,
                        marcadorDePosicion = stringResource(R.string.registro_km_nuevo_marcador),
                        forma = FormaPildora,
                        tipoTeclado = KeyboardType.Number,
                        habilitado = !uiState.guardando,
                        esError = uiState.error != null
                    )

                    CampoFecha(
                        etiqueta = stringResource(R.string.registro_km_fecha),
                        fecha = uiState.fecha,
                        onFechaCambia = onFechaCambia,
                        forma = FormaPildora,
                        habilitado = !uiState.guardando,
                        fechaMaxima = LocalDate.now()
                    )

                    TextoUltimoRegistro(
                        vehiculo = vehiculo,
                        ultimoRegistro = uiState.ultimoRegistro
                    )

                    uiState.error?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (uiState.exito) {
                        Text(
                            text = stringResource(R.string.registro_km_exito),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TransAndinaTheme.colores.estadoOk
                        )
                    }

                    // Los botones van anclados abajo, como en el Figma.
                    Spacer(modifier = Modifier.weight(1f))

                    FilaBotonesFormulario(
                        textoAccion = stringResource(R.string.guardar),
                        onAccion = onGuardar,
                        onCancelar = onCancelar,
                        textoCancelar = stringResource(R.string.cancelar),
                        cargando = uiState.guardando
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun TextoUltimoRegistro(
    vehiculo: Vehiculo,
    ultimoRegistro: KilometrajeHistorico?
) {
    // Si todavía no hay filas en `kilometraje`, se muestra el km_actual del
    // vehículo, que es lo único que hay.
    val km = formatearKilometrosConUnidad(ultimoRegistro?.km ?: vehiculo.kmActual)
    val fecha = ultimoRegistro?.let { formatearFechaIso(it.fecha) }

    Text(
        text = if (fecha.isNullOrBlank()) {
            stringResource(R.string.registro_km_ultimo_sin_fecha, km)
        } else {
            stringResource(R.string.registro_km_ultimo, km, fecha)
        },
        style = MaterialTheme.typography.labelSmall,
        color = TransAndinaTheme.colores.textoSecundario
    )
}

private val vehiculoDeMuestra = Vehiculo(
    id = "1",
    placa = "SCD-3421",
    marca = "Nissan",
    modelo = "Frontier",
    anio = 2021,
    tipo = "liviano",
    capacidad = 1.1,
    kmActual = 492_400.0
)

@Preview(name = "Registrar km", showBackground = true, heightDp = 750)
@Composable
private fun RegistrarKilometrajePreview() {
    TransAndinaFlotillaTheme {
        ContenidoRegistroKilometraje(
            uiState = KilometrajeUiState(
                vehiculo = vehiculoDeMuestra,
                ultimoRegistro = KilometrajeHistorico("1", "1", "2026-08-25", 492_400.0)
            ),
            onKmCambia = {},
            onFechaCambia = {},
            onGuardar = {},
            onCancelar = {}
        )
    }
}

@Preview(name = "Registrar km · guardando", showBackground = true, heightDp = 750)
@Composable
private fun RegistrarKilometrajeGuardandoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRegistroKilometraje(
            uiState = KilometrajeUiState(
                vehiculo = vehiculoDeMuestra,
                kmIngresado = "493000",
                guardando = true
            ),
            onKmCambia = {},
            onFechaCambia = {},
            onGuardar = {},
            onCancelar = {}
        )
    }
}

@Preview(name = "Registrar km · error del trigger", showBackground = true, heightDp = 750)
@Composable
private fun RegistrarKilometrajeErrorPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRegistroKilometraje(
            uiState = KilometrajeUiState(
                vehiculo = vehiculoDeMuestra,
                kmIngresado = "1000",
                error = "El kilometraje debe ser mayor al último registrado (492400 km)"
            ),
            onKmCambia = {},
            onFechaCambia = {},
            onGuardar = {},
            onCancelar = {}
        )
    }
}

@Preview(name = "Registrar km · cargando", showBackground = true, heightDp = 400)
@Composable
private fun RegistrarKilometrajeCargandoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRegistroKilometraje(
            uiState = KilometrajeUiState(cargando = true),
            onKmCambia = {},
            onFechaCambia = {},
            onGuardar = {},
            onCancelar = {}
        )
    }
}

@Preview(name = "Registrar km · sin vehículo", showBackground = true, heightDp = 400)
@Composable
private fun RegistrarKilometrajeSinVehiculoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRegistroKilometraje(
            uiState = KilometrajeUiState(),
            onKmCambia = {},
            onFechaCambia = {},
            onGuardar = {},
            onCancelar = {}
        )
    }
}
