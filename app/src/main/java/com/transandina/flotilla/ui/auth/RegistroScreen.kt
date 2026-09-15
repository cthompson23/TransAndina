package com.transandina.flotilla.ui.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.ui.components.BotonPrimario
import com.transandina.flotilla.ui.components.CampoContrasena
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.theme.EstiloLogotipo
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/** Registro de un usuario nuevo (Figma `1:370`). */
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel = viewModel(),
    onRegistroExitoso: (RolUsuario) -> Unit,
    onIrALogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) onRegistroExitoso(uiState.rol)
    }

    ContenidoRegistro(
        uiState = uiState,
        onNombreCambia = viewModel::onNombreChange,
        onCedulaCambia = viewModel::onCedulaChange,
        onEmailCambia = viewModel::onEmailChange,
        onTelefonoCambia = viewModel::onTelefonoChange,
        onRolCambia = viewModel::onRolChange,
        onLicenciaCambia = viewModel::onLicenciaChange,
        onPasswordCambia = viewModel::onPasswordChange,
        onConfirmarPasswordCambia = viewModel::onConfirmarPasswordChange,
        onRegistrar = viewModel::registrar,
        onIrALogin = onIrALogin
    )
}

@Composable
private fun ContenidoRegistro(
    uiState: RegistroUiState,
    onNombreCambia: (String) -> Unit,
    onCedulaCambia: (String) -> Unit,
    onEmailCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onRolCambia: (RolUsuario) -> Unit,
    onLicenciaCambia: (String) -> Unit,
    onPasswordCambia: (String) -> Unit,
    onConfirmarPasswordCambia: (String) -> Unit,
    onRegistrar: () -> Unit,
    onIrALogin: () -> Unit
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "TransAndina",
                style = EstiloLogotipo,
                color = TransAndinaTheme.colores.naranjaLogo
            )
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            CampoTexto(
                etiqueta = "Nombre completo",
                valor = uiState.nombreCompleto,
                onValorCambia = onNombreCambia,
                colorEtiqueta = colorEtiqueta
            )
            CampoTexto(
                etiqueta = "Cédula",
                valor = uiState.cedula,
                onValorCambia = onCedulaCambia,
                colorEtiqueta = colorEtiqueta,
                tipoTeclado = KeyboardType.Number
            )
            CampoTexto(
                etiqueta = "Correo electrónico",
                valor = uiState.email,
                onValorCambia = onEmailCambia,
                colorEtiqueta = colorEtiqueta,
                tipoTeclado = KeyboardType.Email
            )
            CampoTexto(
                etiqueta = "Teléfono",
                valor = uiState.telefono,
                onValorCambia = onTelefonoCambia,
                colorEtiqueta = colorEtiqueta,
                tipoTeclado = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tipo de cuenta",
                style = MaterialTheme.typography.bodyMedium,
                color = colorEtiqueta
            )
            SelectorRol(rolSeleccionado = uiState.rol, onRolChange = onRolCambia)

            if (uiState.rol == RolUsuario.conductor) {
                CampoTexto(
                    etiqueta = "Número de licencia de conducir",
                    valor = uiState.licenciaConducir,
                    onValorCambia = onLicenciaCambia,
                    colorEtiqueta = colorEtiqueta
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            CampoContrasena(
                etiqueta = "Contraseña",
                valor = uiState.password,
                onValorCambia = onPasswordCambia,
                colorEtiqueta = colorEtiqueta
            )
            CampoContrasena(
                etiqueta = "Confirmar contraseña",
                valor = uiState.confirmarPassword,
                onValorCambia = onConfirmarPasswordCambia,
                colorEtiqueta = colorEtiqueta
            )

            uiState.error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            BotonPrimario(
                texto = "Crear cuenta",
                onClick = onRegistrar,
                modifier = Modifier.fillMaxWidth(),
                cargando = uiState.cargando
            )

            TextButton(onClick = onIrALogin, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "¿Ya tienes cuenta? Inicia sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * A propósito NO incluye la opción "encargado" — esa cuenta se crea
 * manualmente en Supabase, nunca por auto-registro (ver política RLS
 * usuarios_insert_self).
 */
@Composable
private fun SelectorRol(rolSeleccionado: RolUsuario, onRolChange: (RolUsuario) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            RolUsuario.conductor to "Conductor",
            RolUsuario.mecanico to "Mecánico"
        ).forEach { (rol, etiqueta) ->
            val seleccionado = rolSeleccionado == rol
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .selectable(
                        selected = seleccionado,
                        onClick = { onRolChange(rol) }
                    )
                    .background(
                        color = if (seleccionado) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = FormaPildora
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (seleccionado) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        TransAndinaTheme.colores.textoSecundario
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun RegistroScreenPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRegistro(
            uiState = RegistroUiState(
                nombreCompleto = "Carlos Fernández",
                cedula = "1-1111-1111",
                rol = RolUsuario.conductor
            ),
            onNombreCambia = {},
            onCedulaCambia = {},
            onEmailCambia = {},
            onTelefonoCambia = {},
            onRolCambia = {},
            onLicenciaCambia = {},
            onPasswordCambia = {},
            onConfirmarPasswordCambia = {},
            onRegistrar = {},
            onIrALogin = {}
        )
    }
}
