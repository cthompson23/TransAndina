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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.ui.components.BotonPrimario
import com.transandina.flotilla.ui.components.CampoContrasena
import com.transandina.flotilla.ui.theme.EstiloLogotipo
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Pantalla a la que se llega desde el correo de recuperación (Figma `1:352`).
 * A diferencia del resto de auth, su fondo es claro.
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                text = "Nueva contraseña",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(28.dp))

            CampoContrasena(
                etiqueta = "Nueva contraseña",
                valor = uiState.password,
                onValorCambia = onPasswordCambia
            )
            Spacer(modifier = Modifier.height(12.dp))

            CampoContrasena(
                etiqueta = "Confirmar contraseña",
                valor = uiState.confirmarPassword,
                onValorCambia = onConfirmarPasswordCambia
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
                texto = "Guardar nueva contraseña",
                onClick = onGuardar,
                modifier = Modifier.fillMaxWidth(),
                cargando = uiState.cargando
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun NuevaContrasenaScreenPreview() {
    TransAndinaFlotillaTheme {
        ContenidoNuevaContrasena(
            uiState = NuevaContrasenaUiState(password = "secreta"),
            onPasswordCambia = {},
            onConfirmarPasswordCambia = {},
            onGuardar = {}
        )
    }
}
