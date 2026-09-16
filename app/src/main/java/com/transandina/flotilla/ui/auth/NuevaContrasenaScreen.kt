package com.transandina.flotilla.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.ui.components.BotonPrimario
import com.transandina.flotilla.ui.components.CampoContrasena
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme

/**
 * Pantalla a la que se llega desde el correo de recuperación (Figma `1:352`):
 * fondo navy, encabezado "Resetear la contraseña" y los dos campos con ojo.
 */
@Composable
fun NuevaContrasenaScreen(
    viewModel: NuevaContrasenaViewModel = viewModel(),
    onContrasenaActualizada: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.actualizada) {
        if (uiState.actualizada) onContrasenaActualizada()
    }

    ContenidoNuevaContrasena(
        uiState = uiState,
        onPasswordCambia = viewModel::onPasswordChange,
        onConfirmarPasswordCambia = viewModel::onConfirmarPasswordChange,
        onGuardar = viewModel::guardarNuevaContrasena
    )
}

@Composable
private fun ContenidoNuevaContrasena(
    uiState: NuevaContrasenaUiState,
    onPasswordCambia: (String) -> Unit,
    onConfirmarPasswordCambia: (String) -> Unit,
    onGuardar: () -> Unit
) {
    val colorEtiqueta = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // No lleva flecha atrás: se llega por el enlace del correo, no
        // navegando desde otra pantalla.
        TransAndinaTopBar(titulo = stringResource(R.string.nueva_contrasena_encabezado))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            CampoContrasena(
                etiqueta = stringResource(R.string.nueva_contrasena_nueva),
                valor = uiState.password,
                onValorCambia = onPasswordCambia,
                colorEtiqueta = colorEtiqueta,
                habilitado = !uiState.cargando,
                esError = uiState.error != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoContrasena(
                etiqueta = stringResource(R.string.nueva_contrasena_confirmar),
                valor = uiState.confirmarPassword,
                onValorCambia = onConfirmarPasswordCambia,
                colorEtiqueta = colorEtiqueta,
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
                texto = stringResource(R.string.nueva_contrasena_guardar),
                onClick = onGuardar,
                modifier = Modifier.fillMaxWidth(),
                cargando = uiState.cargando
            )
        }
    }
}

@Preview(name = "Nueva contraseña", showBackground = true, heightDp = 800)
@Composable
private fun NuevaContrasenaPreview() {
    TransAndinaFlotillaTheme {
        ContenidoNuevaContrasena(
            uiState = NuevaContrasenaUiState(
                password = "secreta",
                confirmarPassword = "secreta"
            ),
            onPasswordCambia = {},
            onConfirmarPasswordCambia = {},
            onGuardar = {}
        )
    }
}

@Preview(name = "Nueva contraseña cargando", showBackground = true, heightDp = 800)
@Composable
private fun NuevaContrasenaCargandoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoNuevaContrasena(
            uiState = NuevaContrasenaUiState(password = "secreta", cargando = true),
            onPasswordCambia = {},
            onConfirmarPasswordCambia = {},
            onGuardar = {}
        )
    }
}

@Preview(name = "Nueva contraseña con error", showBackground = true, heightDp = 800)
@Composable
private fun NuevaContrasenaErrorPreview() {
    TransAndinaFlotillaTheme {
        ContenidoNuevaContrasena(
            uiState = NuevaContrasenaUiState(
                password = "secreta",
                confirmarPassword = "otra",
                error = "Las contraseñas no coinciden"
            ),
            onPasswordCambia = {},
            onConfirmarPasswordCambia = {},
            onGuardar = {}
        )
    }
}
