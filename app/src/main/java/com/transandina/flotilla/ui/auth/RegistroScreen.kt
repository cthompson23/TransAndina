package com.transandina.flotilla.ui.auth

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
import androidx.compose.material3.TextButton
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
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.ui.components.BotonPrimario
import com.transandina.flotilla.ui.components.CampoContrasena
import com.transandina.flotilla.ui.components.CampoSeleccion
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.components.TransAndinaTopBar

/**
 * Registro de un usuario nuevo (Figma `1:370`). El orden de los campos es el
 * del Figma; se conservan dos que el prototipo no dibuja pero la lógica sí
 * necesita: licencia de conducir (solo conductores) y confirmar contraseña.
 */
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
    val habilitado = !uiState.cargando

    // El auto-registro solo ofrece conductor y mecánico: la cuenta de
    // encargado la crea el equipo en Supabase (docs/ADAPTACION_MOVIL.md §7).
    val etiquetaConductor = stringResource(R.string.registro_rol_conductor)
    val etiquetaMecanico = stringResource(R.string.registro_rol_mecanico)
    val rolesOfrecidos = listOf(etiquetaConductor, etiquetaMecanico)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        TransAndinaTopBar(
            titulo = stringResource(R.string.registro_encabezado),
            onAtras = onIrALogin
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoSeleccion(
                etiqueta = stringResource(R.string.registro_rol),
                seleccion = when (uiState.rol) {
                    RolUsuario.conductor -> etiquetaConductor
                    RolUsuario.mecanico -> etiquetaMecanico
                    RolUsuario.encargado -> null
                },
                opciones = rolesOfrecidos,
                onSeleccionar = { opcion ->
                    onRolCambia(
                        if (opcion == etiquetaMecanico) {
                            RolUsuario.mecanico
                        } else {
                            RolUsuario.conductor
                        }
                    )
                },
                marcadorDePosicion = stringResource(R.string.registro_rol_marcador),
                colorEtiqueta = colorEtiqueta,
                habilitado = habilitado
            )

            CampoTexto(
                etiqueta = stringResource(R.string.correo_electronico),
                valor = uiState.email,
                onValorCambia = onEmailCambia,
                marcadorDePosicion = stringResource(R.string.correo_marcador),
                colorEtiqueta = colorEtiqueta,
                tipoTeclado = KeyboardType.Email,
                habilitado = habilitado
            )

            CampoTexto(
                etiqueta = stringResource(R.string.nombre_completo),
                valor = uiState.nombreCompleto,
                onValorCambia = onNombreCambia,
                marcadorDePosicion = stringResource(R.string.registro_nombre_marcador),
                colorEtiqueta = colorEtiqueta,
                habilitado = habilitado
            )

            CampoTexto(
                etiqueta = stringResource(R.string.cedula),
                valor = uiState.cedula,
                onValorCambia = onCedulaCambia,
                marcadorDePosicion = stringResource(R.string.registro_cedula_marcador),
                colorEtiqueta = colorEtiqueta,
                tipoTeclado = KeyboardType.Number,
                habilitado = habilitado
            )

            CampoTexto(
                etiqueta = stringResource(R.string.telefono),
                valor = uiState.telefono,
                onValorCambia = onTelefonoCambia,
                marcadorDePosicion = stringResource(R.string.registro_telefono_marcador),
                colorEtiqueta = colorEtiqueta,
                tipoTeclado = KeyboardType.Phone,
                habilitado = habilitado
            )

            // No está en el Figma, pero el modelo lo pide para los conductores.
            if (uiState.rol == RolUsuario.conductor) {
                CampoTexto(
                    etiqueta = stringResource(R.string.registro_licencia),
                    valor = uiState.licenciaConducir,
                    onValorCambia = onLicenciaCambia,
                    marcadorDePosicion = stringResource(R.string.registro_licencia_marcador),
                    colorEtiqueta = colorEtiqueta,
                    habilitado = habilitado
                )
            }

            CampoContrasena(
                etiqueta = stringResource(R.string.contrasena),
                valor = uiState.password,
                onValorCambia = onPasswordCambia,
                marcadorDePosicion = stringResource(R.string.registro_contrasena_marcador),
                colorEtiqueta = colorEtiqueta,
                habilitado = habilitado,
                esError = uiState.error != null
            )

            // Tampoco está en el Figma: la validación de RegistroViewModel la
            // necesita para confirmar que no hubo un error de tecleo.
            CampoContrasena(
                etiqueta = stringResource(R.string.registro_confirmar_contrasena),
                valor = uiState.confirmarPassword,
                onValorCambia = onConfirmarPasswordCambia,
                colorEtiqueta = colorEtiqueta,
                habilitado = habilitado,
                esError = uiState.error != null
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
                texto = stringResource(R.string.registro_crear_cuenta),
                onClick = onRegistrar,
                modifier = Modifier.fillMaxWidth(),
                cargando = uiState.cargando
            )

            TextButton(onClick = onIrALogin, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.registro_ya_tienes_cuenta),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Preview(name = "Registro conductor", showBackground = true, heightDp = 1100)
@Composable
private fun RegistroConductorPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRegistro(
            uiState = RegistroUiState(
                nombreCompleto = "Carlos Fernández",
                cedula = "111111111",
                email = "carlos@transandina.cr",
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

@Preview(name = "Registro mecánico", showBackground = true, heightDp = 1100)
@Composable
private fun RegistroMecanicoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRegistro(
            uiState = RegistroUiState(rol = RolUsuario.mecanico),
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

@Preview(name = "Registro cargando", showBackground = true, heightDp = 1100)
@Composable
private fun RegistroCargandoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRegistro(
            uiState = RegistroUiState(
                nombreCompleto = "Carlos Fernández",
                rol = RolUsuario.conductor,
                cargando = true
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

@Preview(name = "Registro con error", showBackground = true, heightDp = 1100)
@Composable
private fun RegistroErrorPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRegistro(
            uiState = RegistroUiState(
                nombreCompleto = "Carlos Fernández",
                rol = RolUsuario.conductor,
                error = "Las contraseñas no coinciden"
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
