package com.transandina.flotilla.ui.usuarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.ui.components.CampoContrasena
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.components.FilaBotonesFormulario
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Registro de administrador (Figma `102:278`): mismo patrón que Registro
 * (`1:370`) pero con sesión iniciada, en una columna y con el rol fijo.
 */
@Composable
fun RegistrarAdministradorScreen(
    viewModel: RegistrarAdministradorViewModel = viewModel(),
    onAtras: () -> Unit = {},
    onRegistrado: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.registrado) {
        if (uiState.registrado) onRegistrado()
    }

    ContenidoRegistrarAdministrador(
        uiState = uiState,
        acciones = AccionesRegistroAdmin(
            onNombre = viewModel::onNombreChange,
            onCedula = viewModel::onCedulaChange,
            onEmail = viewModel::onEmailChange,
            onTelefono = viewModel::onTelefonoChange,
            onPassword = viewModel::onPasswordChange,
            onConfirmar = viewModel::onConfirmarPasswordChange,
            onRegistrar = viewModel::registrar,
            onCancelar = onAtras
        )
    )
}

private data class AccionesRegistroAdmin(
    val onNombre: (String) -> Unit = {},
    val onCedula: (String) -> Unit = {},
    val onEmail: (String) -> Unit = {},
    val onTelefono: (String) -> Unit = {},
    val onPassword: (String) -> Unit = {},
    val onConfirmar: (String) -> Unit = {},
    val onRegistrar: () -> Unit = {},
    val onCancelar: () -> Unit = {}
)

@Composable
private fun ContenidoRegistrarAdministrador(
    uiState: RegistrarAdministradorUiState,
    acciones: AccionesRegistroAdmin
) {
    val editable = !uiState.guardando

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = stringResource(R.string.admin_titulo),
            onAtras = acciones.onCancelar
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.admin_subtitulo),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.admin_ayuda),
                style = MaterialTheme.typography.bodySmall,
                color = TransAndinaTheme.colores.textoSecundario
            )

            CampoTexto(
                etiqueta = stringResource(R.string.nombre_completo),
                valor = uiState.nombreCompleto,
                onValorCambia = acciones.onNombre,
                marcadorDePosicion = stringResource(R.string.admin_nombre_marcador),
                forma = FormaPildora,
                habilitado = editable
            )
            CampoTexto(
                etiqueta = stringResource(R.string.cedula),
                valor = uiState.cedula,
                onValorCambia = acciones.onCedula,
                marcadorDePosicion = stringResource(R.string.registro_cedula_marcador),
                forma = FormaPildora,
                tipoTeclado = KeyboardType.Number,
                habilitado = editable
            )
            CampoTexto(
                etiqueta = stringResource(R.string.correo_electronico),
                valor = uiState.email,
                onValorCambia = acciones.onEmail,
                marcadorDePosicion = stringResource(R.string.admin_correo_marcador),
                forma = FormaPildora,
                tipoTeclado = KeyboardType.Email,
                habilitado = editable
            )
            CampoTexto(
                etiqueta = stringResource(R.string.telefono),
                valor = uiState.telefono,
                onValorCambia = acciones.onTelefono,
                marcadorDePosicion = stringResource(R.string.registro_telefono_marcador),
                forma = FormaPildora,
                tipoTeclado = KeyboardType.Phone,
                habilitado = editable
            )
            CampoContrasena(
                etiqueta = stringResource(R.string.contrasena),
                valor = uiState.password,
                onValorCambia = acciones.onPassword,
                marcadorDePosicion = stringResource(R.string.admin_contrasena_marcador),
                forma = FormaPildora,
                habilitado = editable
            )
            CampoContrasena(
                etiqueta = stringResource(R.string.registro_confirmar_contrasena),
                valor = uiState.confirmarPassword,
                onValorCambia = acciones.onConfirmar,
                marcadorDePosicion = stringResource(R.string.admin_confirmar_marcador),
                forma = FormaPildora,
                habilitado = editable
            )
            CampoTexto(
                etiqueta = stringResource(R.string.admin_rol),
                valor = stringResource(R.string.rol_encargado),
                onValorCambia = {},
                forma = FormaPildora,
                habilitado = false,
                soloLectura = true
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
                textoAccion = stringResource(R.string.registrar),
                onAccion = acciones.onRegistrar,
                onCancelar = acciones.onCancelar,
                textoCancelar = stringResource(R.string.cancelar),
                cargando = uiState.guardando
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(name = "Registrar administrador", showBackground = true, heightDp = 1000)
@Composable
private fun RegistrarAdministradorPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRegistrarAdministrador(
            uiState = RegistrarAdministradorUiState(
                nombreCompleto = "Carlos Rojas Méndez",
                error = "Las contraseñas no coinciden"
            ),
            acciones = AccionesRegistroAdmin()
        )
    }
}
