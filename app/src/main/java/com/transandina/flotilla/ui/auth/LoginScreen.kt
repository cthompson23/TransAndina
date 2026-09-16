package com.transandina.flotilla.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.ui.components.BotonPrimario
import com.transandina.flotilla.ui.components.CampoContrasena
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.theme.EstiloLogotipo
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/** Pantalla de inicio de sesión (Figma `1:404`). */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginExitoso: (RolUsuario) -> Unit,
    onRecuperarContrasena: () -> Unit = {},
    onRegistrarse: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.autenticado, uiState.rol) {
        val rol = uiState.rol
        if (uiState.autenticado && rol != null) onLoginExitoso(rol)
    }

    ContenidoLogin(
        uiState = uiState,
        onEmailCambia = viewModel::onEmailChange,
        onPasswordCambia = viewModel::onPasswordChange,
        onIniciarSesion = viewModel::iniciarSesion,
        onRecuperarContrasena = onRecuperarContrasena,
        onRegistrarse = onRegistrarse
    )
}

@Composable
private fun ContenidoLogin(
    uiState: LoginUiState,
    onEmailCambia: (String) -> Unit,
    onPasswordCambia: (String) -> Unit,
    onIniciarSesion: () -> Unit,
    onRecuperarContrasena: () -> Unit,
    onRegistrarse: () -> Unit
) {
    // Las pantallas de auth van sobre fondo navy, con las etiquetas en gris
    // claro (Figma `1:404`).
    val colorEtiqueta = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "TransAndina",
                style = EstiloLogotipo,
                color = TransAndinaTheme.colores.naranjaLogo,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            CampoTexto(
                etiqueta = "Correo electrónico",
                valor = uiState.email,
                onValorCambia = onEmailCambia,
                marcadorDePosicion = "ejemplo@correo.com",
                colorEtiqueta = colorEtiqueta,
                tipoTeclado = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoContrasena(
                etiqueta = "Contraseña",
                valor = uiState.password,
                onValorCambia = onPasswordCambia,
                marcadorDePosicion = "Escribir la contraseña",
                colorEtiqueta = colorEtiqueta
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onRecuperarContrasena) {
                    Text(
                        text = "Recuperar contraseña",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }

            uiState.error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            BotonPrimario(
                texto = "Ingresar",
                onClick = onIniciarSesion,
                modifier = Modifier.fillMaxWidth(),
                cargando = uiState.cargando
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¿No tienes una cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background
                )
                Spacer(modifier = Modifier.width(8.dp))
                // El botón de registro es más pequeño que el principal y usa
                // el naranja del logotipo (Figma `1:414`).
                Button(
                    onClick = onRegistrarse,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TransAndinaTheme.colores.naranjaLogo,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Regístrate aquí",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun LoginScreenPreview() {
    TransAndinaFlotillaTheme {
        ContenidoLogin(
            uiState = LoginUiState(email = "carlos@transandina.cr"),
            onEmailCambia = {},
            onPasswordCambia = {},
            onIniciarSesion = {},
            onRecuperarContrasena = {},
            onRegistrarse = {}
        )
    }
}
