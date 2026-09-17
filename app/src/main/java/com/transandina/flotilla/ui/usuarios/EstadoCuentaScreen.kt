package com.transandina.flotilla.ui.usuarios

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.EstadoCuenta
import com.transandina.flotilla.ui.components.DialogoConfirmacion
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.FilaBotonesFormulario
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.components.colorDeNivel
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Control de estado de cuenta (Figma `102:298`): los radio buttons del
 * escritorio pasan a tarjetas seleccionables de ancho completo, y
 * "Desactivado" pide confirmación (docs/ADAPTACION_MOVIL.md §4 y §6).
 */
@Composable
fun EstadoCuentaScreen(
    usuarioId: String,
    viewModel: EstadoCuentaViewModel = viewModel(),
    onAtras: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(usuarioId) {
        viewModel.cargar(usuarioId)
    }

    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) onAtras()
    }

    ContenidoEstadoCuenta(
        uiState = uiState,
        onSeleccionar = viewModel::onSeleccionar,
        onGuardar = viewModel::guardar,
        onCancelar = onAtras
    )
}

@Composable
private fun ContenidoEstadoCuenta(
    uiState: EstadoCuentaUiState,
    onSeleccionar: (EstadoCuenta) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    var pedirConfirmacion by rememberSaveable { mutableStateOf(false) }
    val usuario = uiState.usuario

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = stringResource(R.string.estado_cuenta_titulo),
            onAtras = onCancelar
        )

        when {
            usuario == null && uiState.cargando -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            usuario == null -> Box(modifier = Modifier.padding(16.dp)) {
                EstadoVacio(mensaje = uiState.error ?: stringResource(R.string.estado_cuenta_no_encontrada))
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaTransAndina {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = usuario.nombreCompleto,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${etiquetaRol(usuario.rol)} · ${usuario.cedula}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TransAndinaTheme.colores.textoSecundario
                            )
                            Text(
                                text = usuario.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = TransAndinaTheme.colores.textoSecundario
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ChipEstadoCuenta(usuario.estado)
                            Text(
                                text = stringResource(R.string.estado_cuenta_actual),
                                style = MaterialTheme.typography.labelSmall,
                                color = TransAndinaTheme.colores.textoSecundario
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.estado_cuenta_cambiar),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = when {
                        uiState.esCuentaPropia -> stringResource(R.string.estado_cuenta_propia)
                        usuario.estado == EstadoCuenta.desactivado ->
                            stringResource(R.string.estado_cuenta_desactivada_final)
                        else -> stringResource(R.string.estado_cuenta_ayuda)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TransAndinaTheme.colores.textoSecundario
                )

                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EstadoCuenta.entries.forEach { estado ->
                        OpcionEstado(
                            estado = estado,
                            seleccionada = uiState.seleccion == estado,
                            habilitada = !uiState.bloqueada && !uiState.guardando,
                            onSeleccionar = { onSeleccionar(estado) }
                        )
                    }
                }

                uiState.error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                FilaBotonesFormulario(
                    textoAccion = stringResource(R.string.guardar_cambios),
                    onAccion = {
                        if (uiState.seleccion == EstadoCuenta.desactivado) {
                            pedirConfirmacion = true
                        } else {
                            onGuardar()
                        }
                    },
                    onCancelar = onCancelar,
                    textoCancelar = stringResource(R.string.cancelar),
                    accionHabilitada = uiState.hayCambios && !uiState.bloqueada,
                    cargando = uiState.guardando
                )
            }
        }
    }

    if (pedirConfirmacion && usuario != null) {
        DialogoConfirmacion(
            titulo = stringResource(R.string.estado_cuenta_dialogo_titulo),
            mensaje = stringResource(R.string.estado_cuenta_dialogo_mensaje, usuario.nombreCompleto),
            textoConfirmar = stringResource(R.string.estado_cuenta_dialogo_confirmar),
            destructiva = true,
            onConfirmar = {
                pedirConfirmacion = false
                onGuardar()
            },
            onCancelar = { pedirConfirmacion = false }
        )
    }
}

/** Tarjeta seleccionable con radio button, borde navy si está elegida (Figma `108:457`). */
@Composable
private fun OpcionEstado(
    estado: EstadoCuenta,
    seleccionada: Boolean,
    habilitada: Boolean,
    onSeleccionar: () -> Unit
) {
    val colorEstado = colorDeNivel(nivelEstadoCuenta(estado))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = seleccionada,
                enabled = habilitada,
                role = Role.RadioButton,
                onClick = onSeleccionar
            ),
        shape = MaterialTheme.shapes.small,
        color = if (seleccionada) {
            TransAndinaTheme.colores.superficieSuave
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (seleccionada) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, TransAndinaTheme.colores.placeholder)
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = seleccionada,
                onClick = null,
                enabled = habilitada,
                colors = RadioButtonDefaults.colors(
                    selectedColor = colorEstado,
                    unselectedColor = TransAndinaTheme.colores.placeholder
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = etiquetaEstadoCuenta(estado),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (estado) {
                        EstadoCuenta.activo -> stringResource(R.string.estado_cuenta_activo_detalle)
                        EstadoCuenta.suspendido -> stringResource(R.string.estado_cuenta_suspendido_detalle)
                        EstadoCuenta.desactivado -> stringResource(R.string.estado_cuenta_desactivado_detalle)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TransAndinaTheme.colores.textoSecundario
                )
            }
        }
    }
}

@Preview(name = "Estado de cuenta", showBackground = true, heightDp = 800)
@Composable
private fun EstadoCuentaPreview() {
    val usuario = usuariosDeMuestra[2]
    TransAndinaFlotillaTheme {
        ContenidoEstadoCuenta(
            uiState = EstadoCuentaUiState(usuario = usuario, seleccion = EstadoCuenta.activo),
            onSeleccionar = {},
            onGuardar = {},
            onCancelar = {}
        )
    }
}

@Preview(name = "Estado de cuenta · desactivada", showBackground = true, heightDp = 800)
@Composable
private fun EstadoCuentaDesactivadaPreview() {
    val usuario = usuariosDeMuestra[3]
    TransAndinaFlotillaTheme {
        ContenidoEstadoCuenta(
            uiState = EstadoCuentaUiState(usuario = usuario, seleccion = usuario.estado),
            onSeleccionar = {},
            onGuardar = {},
            onCancelar = {}
        )
    }
}
