package com.transandina.flotilla.ui.perfil

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.ui.components.BotonDestructivo
import com.transandina.flotilla.ui.components.BotonPrimario
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.components.DatoEtiquetado
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.FilaBotonesFormulario
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.components.VarianteCampo
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Perfil del usuario con sesión iniciada. En modo lectura sigue `28:163`
 * y al editar cambia al encabezado y los botones de `28:263`.
 */
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = viewModel(),
    onSesionCerrada: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    ContenidoPerfil(
        uiState = uiState,
        onEditar = viewModel::iniciarEdicion,
        onCancelarEdicion = viewModel::cancelarEdicion,
        onGuardar = viewModel::guardarCambios,
        onNombreCambia = viewModel::onNombreChange,
        onCedulaCambia = viewModel::onCedulaChange,
        onTelefonoCambia = viewModel::onTelefonoChange,
        onLicenciaCambia = viewModel::onLicenciaChange,
        onCerrarSesion = { mostrarDialogoCerrarSesion = true }
    )

    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.perfil_cerrar_sesion),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.perfil_cerrar_sesion_pregunta),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCerrarSesion = false
                        viewModel.cerrarSesion(onSesionCerrada)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.perfil_cerrar_sesion),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text(
                        text = stringResource(R.string.cancelar),
                        color = TransAndinaTheme.colores.textoSecundario
                    )
                }
            }
        )
    }
}

@Composable
private fun ContenidoPerfil(
    uiState: PerfilUiState,
    onEditar: () -> Unit,
    onCancelarEdicion: () -> Unit,
    onGuardar: () -> Unit,
    onNombreCambia: (String) -> Unit,
    onCedulaCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onLicenciaCambia: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TransAndinaTopBar(
            titulo = if (uiState.editando) {
                stringResource(R.string.perfil_editar_titulo)
            } else {
                stringResource(R.string.perfil_titulo)
            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when {
                    uiState.cargando && uiState.usuario == null -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    uiState.usuario == null -> {
                        EstadoVacio(
                            mensaje = uiState.error
                                ?: stringResource(R.string.perfil_error_carga)
                        )
                    }
                    uiState.editando -> {
                        FormularioEdicion(
                            uiState = uiState,
                            onNombreCambia = onNombreCambia,
                            onCedulaCambia = onCedulaCambia,
                            onTelefonoCambia = onTelefonoCambia,
                            onLicenciaCambia = onLicenciaCambia,
                            onCancelar = onCancelarEdicion,
                            onGuardar = onGuardar
                        )
                    }
                    else -> {
                        VistaPerfil(
                            uiState = uiState,
                            onEditar = onEditar,
                            onCerrarSesion = onCerrarSesion
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VistaPerfil(
    uiState: PerfilUiState,
    onEditar: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val usuario = uiState.usuario!!

    if (uiState.guardadoExitoso) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = TransAndinaTheme.colores.estadoOk,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.perfil_actualizado),
                style = MaterialTheme.typography.bodyMedium,
                color = TransAndinaTheme.colores.estadoOk
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // El Figma muestra los datos como etiqueta navy + valor gris separados
    // por una línea fina, no como tarjetas (Figma `28:163`).
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DatoConLinea(
            etiqueta = stringResource(R.string.nombre_completo),
            valor = usuario.nombreCompleto
        )
        DatoConLinea(
            etiqueta = stringResource(R.string.cedula),
            valor = usuario.cedula
        )
        DatoConLinea(
            etiqueta = stringResource(R.string.correo_electronico),
            valor = usuario.email
        )
        DatoConLinea(
            etiqueta = stringResource(R.string.telefono),
            valor = usuario.telefono ?: stringResource(R.string.perfil_sin_registrar)
        )
        if (usuario.rol == RolUsuario.conductor) {
            DatoConLinea(
                etiqueta = stringResource(R.string.perfil_licencia),
                valor = usuario.licenciaConducir
                    ?: stringResource(R.string.perfil_sin_registrar)
            )
        }
        DatoConLinea(etiqueta = stringResource(R.string.perfil_rol), valor = usuario.rol.name)
    }

    Spacer(modifier = Modifier.height(32.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BotonPrimario(
            texto = stringResource(R.string.perfil_editar_datos),
            onClick = onEditar,
            modifier = Modifier.weight(1f)
        )
        BotonDestructivo(
            texto = stringResource(R.string.perfil_cerrar_sesion),
            onClick = onCerrarSesion,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DatoConLinea(etiqueta: String, valor: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DatoEtiquetado(
            etiqueta = etiqueta,
            valor = valor,
            colorEtiqueta = MaterialTheme.colorScheme.onBackground,
            colorValor = TransAndinaTheme.colores.grisBoton
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = TransAndinaTheme.colores.textoSecundario)
    }
}

@Composable
private fun FormularioEdicion(
    uiState: PerfilUiState,
    onNombreCambia: (String) -> Unit,
    onCedulaCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onLicenciaCambia: (String) -> Unit,
    onCancelar: () -> Unit,
    onGuardar: () -> Unit
) {
    val usuario = uiState.usuario!!
    val habilitado = !uiState.guardando

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CampoTexto(
            etiqueta = stringResource(R.string.nombre_completo),
            valor = uiState.nombreCompleto,
            onValorCambia = onNombreCambia,
            variante = VarianteCampo.SUBRAYADO,
            habilitado = habilitado
        )
        CampoTexto(
            etiqueta = stringResource(R.string.cedula),
            valor = uiState.cedula,
            onValorCambia = onCedulaCambia,
            variante = VarianteCampo.SUBRAYADO,
            tipoTeclado = KeyboardType.Number,
            habilitado = habilitado
        )
        CampoTexto(
            etiqueta = stringResource(R.string.telefono),
            valor = uiState.telefono,
            onValorCambia = onTelefonoCambia,
            variante = VarianteCampo.SUBRAYADO,
            tipoTeclado = KeyboardType.Phone,
            habilitado = habilitado
        )

        if (usuario.rol == RolUsuario.conductor) {
            CampoTexto(
                etiqueta = stringResource(R.string.perfil_licencia),
                valor = uiState.licenciaConducir,
                onValorCambia = onLicenciaCambia,
                variante = VarianteCampo.SUBRAYADO,
                habilitado = habilitado
            )
        }

        // El correo y el rol no se editan desde la app: el correo es la
        // identidad en Supabase Auth y el rol lo controlan las políticas RLS.
        DatoConLinea(
            etiqueta = stringResource(R.string.perfil_correo_no_editable),
            valor = usuario.email
        )
        DatoConLinea(
            etiqueta = stringResource(R.string.perfil_rol_no_editable),
            valor = usuario.rol.name
        )

        uiState.error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    FilaBotonesFormulario(
        textoAccion = stringResource(R.string.guardar),
        onAccion = onGuardar,
        onCancelar = onCancelar,
        textoCancelar = stringResource(R.string.cancelar),
        cargando = uiState.guardando
    )
}

private val usuarioDeMuestra = Usuario(
    id = "1",
    nombreCompleto = "Carlos Fernández Quesada",
    cedula = "1-1111-1111",
    email = "carlos@transandina.cr",
    telefono = "8889-9900",
    licenciaConducir = "B1-123456",
    rol = RolUsuario.conductor
)

@Composable
private fun PerfilDePrueba(uiState: PerfilUiState) {
    TransAndinaFlotillaTheme {
        ContenidoPerfil(
            uiState = uiState,
            onEditar = {},
            onCancelarEdicion = {},
            onGuardar = {},
            onNombreCambia = {},
            onCedulaCambia = {},
            onTelefonoCambia = {},
            onLicenciaCambia = {},
            onCerrarSesion = {}
        )
    }
}

@Preview(name = "Perfil", showBackground = true, heightDp = 800)
@Composable
private fun PerfilPreview() {
    PerfilDePrueba(PerfilUiState(usuario = usuarioDeMuestra))
}

@Preview(name = "Perfil editando", showBackground = true, heightDp = 800)
@Composable
private fun PerfilEditandoPreview() {
    PerfilDePrueba(
        PerfilUiState(
            usuario = usuarioDeMuestra,
            editando = true,
            nombreCompleto = usuarioDeMuestra.nombreCompleto,
            cedula = usuarioDeMuestra.cedula,
            telefono = usuarioDeMuestra.telefono.orEmpty(),
            licenciaConducir = usuarioDeMuestra.licenciaConducir.orEmpty()
        )
    )
}

@Preview(name = "Perfil guardando", showBackground = true, heightDp = 800)
@Composable
private fun PerfilGuardandoPreview() {
    PerfilDePrueba(
        PerfilUiState(
            usuario = usuarioDeMuestra,
            editando = true,
            guardando = true,
            nombreCompleto = usuarioDeMuestra.nombreCompleto,
            cedula = usuarioDeMuestra.cedula,
            telefono = usuarioDeMuestra.telefono.orEmpty()
        )
    )
}

@Preview(name = "Perfil cargando", showBackground = true, heightDp = 400)
@Composable
private fun PerfilCargandoPreview() {
    PerfilDePrueba(PerfilUiState(cargando = true))
}

@Preview(name = "Perfil con error", showBackground = true, heightDp = 400)
@Composable
private fun PerfilErrorPreview() {
    PerfilDePrueba(PerfilUiState(error = "No hay conexión con el servidor"))
}

@Preview(name = "Perfil guardado", showBackground = true, heightDp = 800)
@Composable
private fun PerfilGuardadoPreview() {
    PerfilDePrueba(PerfilUiState(usuario = usuarioDeMuestra, guardadoExitoso = true))
}
