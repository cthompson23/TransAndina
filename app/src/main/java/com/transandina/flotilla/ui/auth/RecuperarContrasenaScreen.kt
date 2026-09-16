package com.transandina.flotilla.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.transandina.flotilla.ui.components.BotonPrimario
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme

/**
 * Envío del correo de recuperación (Figma `1:387`): encabezado navy con
 * flecha atrás, título "Verificar correo" y un solo campo.
 */
@Composable
fun RecuperarContrasenaScreen(
    viewModel: RecuperarContrasenaViewModel = viewModel(),
    onVolverALogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ContenidoRecuperarContrasena(
        uiState = uiState,
        onEmailCambia = viewModel::onEmailChange,
        onEnviarCorreo = viewModel::enviarCorreo,
        onVolverALogin = onVolverALogin
    )
}

@Composable
private fun ContenidoRecuperarContrasena(
    uiState: RecuperarContrasenaUiState,
    onEmailCambia: (String) -> Unit,
    onEnviarCorreo: () -> Unit,
    onVolverALogin: () -> Unit
) {
    val colorEtiqueta = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        TransAndinaTopBar(
            titulo = stringResource(R.string.recuperar_encabezado),
            onAtras = onVolverALogin
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.recuperar_titulo),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.correoEnviado) {
                Text(
                    text = stringResource(R.string.recuperar_enviado),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = onVolverALogin) {
                        Text(
                            text = stringResource(R.string.recuperar_volver),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.recuperar_ayuda),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background
                )

                Spacer(modifier = Modifier.height(24.dp))

                CampoTexto(
                    etiqueta = stringResource(R.string.correo_electronico),
                    valor = uiState.email,
                    onValorCambia = onEmailCambia,
                    marcadorDePosicion = stringResource(R.string.correo_marcador),
                    colorEtiqueta = colorEtiqueta,
                    tipoTeclado = KeyboardType.Email,
                    habilitado = !uiState.cargando,
                    esError = uiState.error != null
                )

                uiState.error?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                BotonPrimario(
                    texto = stringResource(R.string.recuperar_enviar),
                    onClick = onEnviarCorreo,
                    modifier = Modifier.fillMaxWidth(),
                    cargando = uiState.cargando
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = onVolverALogin) {
                        Text(
                            text = stringResource(R.string.recuperar_volver),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Recuperar", showBackground = true, heightDp = 800)
@Composable
private fun RecuperarContrasenaPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRecuperarContrasena(
            uiState = RecuperarContrasenaUiState(email = "carlos@transandina.cr"),
            onEmailCambia = {},
            onEnviarCorreo = {},
            onVolverALogin = {}
        )
    }
}

@Preview(name = "Recuperar cargando", showBackground = true, heightDp = 800)
@Composable
private fun RecuperarCargandoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRecuperarContrasena(
            uiState = RecuperarContrasenaUiState(
                email = "carlos@transandina.cr",
                cargando = true
            ),
            onEmailCambia = {},
            onEnviarCorreo = {},
            onVolverALogin = {}
        )
    }
}

@Preview(name = "Recuperar con error", showBackground = true, heightDp = 800)
@Composable
private fun RecuperarErrorPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRecuperarContrasena(
            uiState = RecuperarContrasenaUiState(
                email = "correo-invalido",
                error = "Escribe un correo válido"
            ),
            onEmailCambia = {},
            onEnviarCorreo = {},
            onVolverALogin = {}
        )
    }
}

@Preview(name = "Recuperar enviado", showBackground = true, heightDp = 800)
@Composable
private fun RecuperarEnviadoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRecuperarContrasena(
            uiState = RecuperarContrasenaUiState(
                email = "carlos@transandina.cr",
                correoEnviado = true
            ),
            onEmailCambia = {},
            onEnviarCorreo = {},
            onVolverALogin = {}
        )
    }
}
