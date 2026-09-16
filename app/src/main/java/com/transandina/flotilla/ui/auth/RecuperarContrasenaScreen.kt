package com.transandina.flotilla.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.ui.components.BotonPrimario
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.theme.EstiloLogotipo
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/** Envío del correo de recuperación (Figma `1:387`). */
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TransAndina",
                style = EstiloLogotipo,
                color = TransAndinaTheme.colores.naranjaLogo
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recuperar contraseña",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.correoEnviado) {
                Text(
                    text = "Te enviamos un correo con un enlace para restablecer tu contraseña. " +
                        "Ábrelo desde este mismo dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.background
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onVolverALogin) {
                    Text(
                        text = "Volver a iniciar sesión",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                Text(
                    text = "Ingresa tu correo y te mandamos un enlace para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.background
                )
                Spacer(modifier = Modifier.height(24.dp))

                CampoTexto(
                    etiqueta = "Correo electrónico",
                    valor = uiState.email,
                    onValorCambia = onEmailCambia,
                    colorEtiqueta = colorEtiqueta,
                    tipoTeclado = KeyboardType.Email
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
                    texto = "Enviar enlace",
                    onClick = onEnviarCorreo,
                    modifier = Modifier.fillMaxWidth(),
                    cargando = uiState.cargando
                )

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onVolverALogin) {
                    Text(
                        text = "Volver a iniciar sesión",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun RecuperarContrasenaScreenPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRecuperarContrasena(
            uiState = RecuperarContrasenaUiState(email = "carlos@transandina.cr"),
            onEmailCambia = {},
            onEnviarCorreo = {},
            onVolverALogin = {}
        )
    }
}
