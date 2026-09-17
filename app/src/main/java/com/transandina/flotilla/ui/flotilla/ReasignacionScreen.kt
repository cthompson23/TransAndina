package com.transandina.flotilla.ui.flotilla

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.ui.components.CampoFecha
import com.transandina.flotilla.ui.components.CampoSeleccion
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.components.DialogoConfirmacion
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.FilaBotonesFormulario
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme
import java.time.LocalDate

/**
 * Reasignar conductor (Figma `102:318`): se entra desde el detalle del
 * vehículo, así que vehículo y conductor actual son de solo lectura. Debajo,
 * las reasignaciones recientes de ese vehículo.
 */
@Composable
fun ReasignacionScreen(
    vehiculoId: String,
    viewModel: ReasignacionViewModel = viewModel(),
    onAtras: () -> Unit = {},
    onReasignado: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vehiculoId) {
        viewModel.cargar(vehiculoId)
    }

    LaunchedEffect(uiState.reasignado) {
        if (uiState.reasignado) onReasignado()
    }

    ContenidoReasignacion(
        uiState = uiState,
        onSeleccionarConductor = viewModel::onSeleccionarConductor,
        onQuitarConductor = viewModel::onQuitarConductor,
        onFechaCambia = viewModel::onFechaChange,
        onMotivoCambia = viewModel::onMotivoChange,
        onConfirmar = viewModel::confirmar,
        onCancelar = onAtras
    )
}

@Composable
private fun ContenidoReasignacion(
    uiState: ReasignacionUiState,
    onSeleccionarConductor: (Usuario) -> Unit,
    onQuitarConductor: () -> Unit,
    onFechaCambia: (LocalDate) -> Unit,
    onMotivoCambia: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    var pedirConfirmacion by rememberSaveable { mutableStateOf(false) }
    val vehiculo = uiState.vehiculo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = stringResource(R.string.reasignar_titulo),
            subtitulo = vehiculo?.placa,
            onAtras = onCancelar
        )

        when {
            vehiculo == null && uiState.cargando -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            vehiculo == null -> Box(modifier = Modifier.padding(16.dp)) {
                EstadoVacio(
                    mensaje = uiState.error ?: stringResource(R.string.detalle_no_encontrado)
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.reasignar_aviso),
                    style = MaterialTheme.typography.bodySmall,
                    color = TransAndinaTheme.colores.textoSecundario
                )

                CampoTexto(
                    etiqueta = stringResource(R.string.registro_km_vehiculo),
                    valor = "${vehiculo.placa} · ${vehiculo.marca} ${vehiculo.modelo}",
                    onValorCambia = {},
                    forma = FormaPildora,
                    habilitado = false,
                    soloLectura = true
                )
                CampoTexto(
                    etiqueta = stringResource(R.string.reasignar_conductor_actual),
                    valor = uiState.conductorActual?.nombreCompleto
                        ?: stringResource(R.string.sin_conductor),
                    onValorCambia = {},
                    forma = FormaPildora,
                    habilitado = false,
                    soloLectura = true
                )

                SelectorConductor(
                    uiState = uiState,
                    tieneConductor = vehiculo.conductorId != null,
                    onSeleccionarConductor = onSeleccionarConductor,
                    onQuitarConductor = onQuitarConductor
                )

                CampoFecha(
                    etiqueta = stringResource(R.string.reasignar_fecha),
                    fecha = uiState.fechaEfectiva,
                    onFechaCambia = onFechaCambia,
                    forma = FormaPildora,
                    habilitado = !uiState.guardando
                )
                CampoTexto(
                    etiqueta = stringResource(R.string.reasignar_motivo),
                    valor = uiState.motivo,
                    onValorCambia = onMotivoCambia,
                    marcadorDePosicion = stringResource(R.string.reasignar_motivo_marcador),
                    habilitado = !uiState.guardando,
                    lineas = 3
                )

                uiState.error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                FilaBotonesFormulario(
                    textoAccion = stringResource(R.string.reasignar_confirmar),
                    onAccion = { pedirConfirmacion = true },
                    onCancelar = onCancelar,
                    textoCancelar = stringResource(R.string.cancelar),
                    accionHabilitada = uiState.haySeleccion,
                    cargando = uiState.guardando
                )

                Spacer(modifier = Modifier.height(8.dp))

                HistorialReasignaciones(uiState.historial)
            }
        }
    }

    if (pedirConfirmacion && vehiculo != null) {
        DialogoConfirmacion(
            titulo = stringResource(R.string.reasignar_dialogo_titulo),
            mensaje = uiState.seleccionado
                ?.let { stringResource(R.string.reasignar_dialogo_mensaje, vehiculo.placa, it.nombreCompleto) }
                ?: stringResource(R.string.reasignar_dialogo_quitar, vehiculo.placa),
            textoConfirmar = stringResource(R.string.reasignar_dialogo_confirmar),
            onConfirmar = {
                pedirConfirmacion = false
                onConfirmar()
            },
            onCancelar = { pedirConfirmacion = false }
        )
    }
}

/** Desplegable con los conductores disponibles; "Sin conductor" va al final si aplica. */
@Composable
private fun SelectorConductor(
    uiState: ReasignacionUiState,
    tieneConductor: Boolean,
    onSeleccionarConductor: (Usuario) -> Unit,
    onQuitarConductor: () -> Unit
) {
    val textoQuitar = stringResource(R.string.reasignar_quitar)
    val etiquetas = uiState.disponibles.map { etiquetaConductor(it) }
    val opciones = if (tieneConductor) etiquetas + textoQuitar else etiquetas

    CampoSeleccion(
        etiqueta = stringResource(R.string.reasignar_nuevo),
        seleccion = when {
            uiState.quitarConductor -> textoQuitar
            else -> uiState.seleccionado?.let(::etiquetaConductor)
        },
        opciones = opciones,
        onSeleccionar = { elegida ->
            val indice = etiquetas.indexOf(elegida)
            if (indice >= 0) onSeleccionarConductor(uiState.disponibles[indice]) else onQuitarConductor()
        },
        marcadorDePosicion = stringResource(R.string.reasignar_nuevo_marcador),
        forma = FormaPildora,
        habilitado = !uiState.guardando && opciones.isNotEmpty()
    )

    if (uiState.disponibles.isEmpty() && !uiState.cargando) {
        Text(
            text = stringResource(R.string.reasignar_sin_disponibles),
            style = MaterialTheme.typography.bodySmall,
            color = TransAndinaTheme.colores.textoSecundario
        )
    }
}

private fun etiquetaConductor(usuario: Usuario) = "${usuario.nombreCompleto} · ${usuario.cedula}"

@Composable
private fun HistorialReasignaciones(historial: List<FilaReasignacion>) {
    TarjetaTransAndina {
        Text(
            text = stringResource(R.string.reasignar_recientes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (historial.isEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.reasignar_recientes_vacio),
                style = MaterialTheme.typography.bodySmall,
                color = TransAndinaTheme.colores.textoSecundario
            )
        }
        val sinConductor = stringResource(R.string.sin_conductor)
        historial.forEach { fila ->
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TransAndinaTheme.colores.superficieSuave, MaterialTheme.shapes.small)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${fila.anterior ?: sinConductor} → ${fila.nuevo ?: sinConductor}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    fila.motivo?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = TransAndinaTheme.colores.textoSecundario
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = fila.fecha,
                    style = MaterialTheme.typography.labelSmall,
                    color = TransAndinaTheme.colores.textoSecundario
                )
            }
        }
    }
}

@Preview(name = "Reasignar conductor", showBackground = true, heightDp = 1000)
@Composable
private fun ReasignacionPreview() {
    val conductor = Usuario(
        id = "c1",
        nombreCompleto = "Carlos Fernández Quesada",
        cedula = "111111111",
        email = "carlos@transandina.cr",
        rol = RolUsuario.conductor
    )
    TransAndinaFlotillaTheme {
        ContenidoReasignacion(
            uiState = ReasignacionUiState(
                vehiculo = Vehiculo(
                    id = "1",
                    placa = "SCD-3421",
                    marca = "Nissan",
                    modelo = "Frontier",
                    anio = 2021,
                    tipo = "liviano",
                    kmActual = 492_400.0,
                    conductorId = "c1"
                ),
                conductorActual = conductor,
                disponibles = listOf(conductor.copy(id = "c2", nombreCompleto = "Sofía Blanco Núñez")),
                historial = listOf(
                    FilaReasignacion("Diego Ramírez", "Carlos Fernández", "22/08/2026", "Cambio de ruta"),
                    FilaReasignacion("Ana Jiménez", null, "05/08/2026", null)
                )
            ),
            onSeleccionarConductor = {},
            onQuitarConductor = {},
            onFechaCambia = {},
            onMotivoCambia = {},
            onConfirmar = {},
            onCancelar = {}
        )
    }
}
