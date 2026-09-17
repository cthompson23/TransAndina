package com.transandina.flotilla.ui.alertas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.ui.components.CampoSeleccion
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.components.FilaBotonesFormulario
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme
import com.transandina.flotilla.ui.usuarios.etiquetaRol

/**
 * Enviar un aviso de gerencia. No tiene frame propio en el Figma: sigue el
 * patrón de formulario de `49:161` (campos en columna, botones abajo).
 */
@Composable
fun EnviarAvisoScreen(
    viewModel: EnviarAvisoViewModel = viewModel(),
    onAtras: () -> Unit = {},
    onEnviado: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.enviado) {
        if (uiState.enviado) onEnviado()
    }

    ContenidoEnviarAviso(
        uiState = uiState,
        onDestinoCambia = viewModel::onDestinoChange,
        onMensajeCambia = viewModel::onMensajeChange,
        onEnviar = viewModel::enviar,
        onCancelar = onAtras
    )
}

@Composable
private fun ContenidoEnviarAviso(
    uiState: EnviarAvisoUiState,
    onDestinoCambia: (DestinoAviso) -> Unit,
    onMensajeCambia: (String) -> Unit,
    onEnviar: () -> Unit,
    onCancelar: () -> Unit
) {
    val textoConductores = stringResource(R.string.aviso_todos_conductores)
    val textoTodos = stringResource(R.string.aviso_todos_usuarios)
    val etiquetasUsuarios = uiState.destinatarios.map { etiquetaDestinatario(it) }
    val opciones = listOf(textoConductores, textoTodos) + etiquetasUsuarios

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = stringResource(R.string.aviso_titulo),
            onAtras = onCancelar
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.aviso_ayuda),
                style = MaterialTheme.typography.bodySmall,
                color = TransAndinaTheme.colores.textoSecundario
            )

            CampoSeleccion(
                etiqueta = stringResource(R.string.aviso_destino),
                seleccion = when (val destino = uiState.destino) {
                    null -> null
                    DestinoAviso.TodosLosConductores -> textoConductores
                    DestinoAviso.TodosLosUsuarios -> textoTodos
                    is DestinoAviso.UnUsuario -> etiquetaDestinatario(destino.usuario)
                },
                opciones = opciones,
                onSeleccionar = { elegida ->
                    when (elegida) {
                        textoConductores -> onDestinoCambia(DestinoAviso.TodosLosConductores)
                        textoTodos -> onDestinoCambia(DestinoAviso.TodosLosUsuarios)
                        else -> {
                            val indice = etiquetasUsuarios.indexOf(elegida)
                            if (indice >= 0) {
                                onDestinoCambia(DestinoAviso.UnUsuario(uiState.destinatarios[indice]))
                            }
                        }
                    }
                },
                marcadorDePosicion = stringResource(R.string.aviso_destino_marcador),
                forma = FormaPildora,
                habilitado = !uiState.enviando && !uiState.cargando
            )

            CampoTexto(
                etiqueta = stringResource(R.string.aviso_mensaje),
                valor = uiState.mensaje,
                onValorCambia = onMensajeCambia,
                marcadorDePosicion = stringResource(R.string.aviso_mensaje_marcador),
                habilitado = !uiState.enviando,
                lineas = 5
            )
            Text(
                text = "${uiState.mensaje.length}/$LIMITE_MENSAJE_AVISO",
                style = MaterialTheme.typography.labelSmall,
                color = TransAndinaTheme.colores.textoSecundario,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            uiState.error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            FilaBotonesFormulario(
                textoAccion = stringResource(R.string.aviso_enviar),
                onAccion = onEnviar,
                onCancelar = onCancelar,
                textoCancelar = stringResource(R.string.cancelar),
                cargando = uiState.enviando
            )
        }
    }
}

/** La cédula va al final para que dos personas con el mismo nombre no se confundan. */
@Composable
private fun etiquetaDestinatario(usuario: Usuario): String =
    "${usuario.nombreCompleto} · ${etiquetaRol(usuario.rol)} · ${usuario.cedula}"

@Preview(name = "Enviar aviso", showBackground = true, heightDp = 700)
@Composable
private fun EnviarAvisoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoEnviarAviso(
            uiState = EnviarAvisoUiState(
                destino = DestinoAviso.TodosLosConductores,
                mensaje = "Recuerden actualizar el kilometraje antes del viernes."
            ),
            onDestinoCambia = {},
            onMensajeCambia = {},
            onEnviar = {},
            onCancelar = {}
        )
    }
}
