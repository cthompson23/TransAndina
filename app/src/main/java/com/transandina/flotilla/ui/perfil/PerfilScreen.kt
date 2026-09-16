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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.ui.components.BotonDestructivo
import com.transandina.flotilla.ui.components.BotonPrimario
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.components.DatoEtiquetado
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.FilaBotonesFormulario
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/** Perfil del usuario con sesión iniciada (Figma `28:163`, `28:263`). */
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = viewModel(),
    onSesionCerrada: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TransAndinaTopBar(titulo = "Perfil")

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
                        EstadoVacio(mensaje = "No se pudo cargar tu perfil")
                    }
                    uiState.editando -> {
                        FormularioEdicion(uiState = uiState, viewModel = viewModel)
                    }
                    else -> {
                        VistaPerfil(
                            uiState = uiState,
                            onEditar = viewModel::iniciarEdicion,
                            onCerrarSesion = { mostrarDialogoCerrarSesion = true }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Cerrar sesión", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = "¿Seguro que quieres cerrar sesión?",
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
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text("Cancelar", color = TransAndinaTheme.colores.textoSecundario)
                }
            }
        )
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
                text = "Perfil actualizado correctamente",
                style = MaterialTheme.typography.bodyMedium,
                color = TransAndinaTheme.colores.estadoOk
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // El Figma muestra los datos como etiqueta + valor separados por una
    // línea fina, no como tarjetas (Figma `28:163`).
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DatoConLinea(etiqueta = "Nombre completo", valor = usuario.nombreCompleto)
        DatoConLinea(etiqueta = "Cédula", valor = usuario.cedula)
        DatoConLinea(etiqueta = "Correo", valor = usuario.email)
        DatoConLinea(etiqueta = "Teléfono", valor = usuario.telefono ?: "Sin registrar")
        if (usuario.rol == RolUsuario.conductor) {
            DatoConLinea(
                etiqueta = "Licencia de conducir",
                valor = usuario.licenciaConducir ?: "Sin registrar"
            )
        }
        DatoConLinea(etiqueta = "Rol", valor = usuario.rol.name)
    }

    Spacer(modifier = Modifier.height(32.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BotonPrimario(
            texto = "Editar perfil",
            onClick = onEditar,
            modifier = Modifier.weight(1f)
        )
        BotonDestructivo(
            texto = "Cerrar sesión",
            onClick = onCerrarSesion,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DatoConLinea(etiqueta: String, valor: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DatoEtiquetado(etiqueta = etiqueta, valor = valor)
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = TransAndinaTheme.colores.placeholder)
    }
}

@Composable
private fun FormularioEdicion(uiState: PerfilUiState, viewModel: PerfilViewModel) {
    val usuario = uiState.usuario!!

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CampoTexto(
            etiqueta = "Nombre completo",
            valor = uiState.nombreCompleto,
            onValorCambia = viewModel::onNombreChange,
            forma = FormaPildora
        )
        CampoTexto(
            etiqueta = "Cédula",
            valor = uiState.cedula,
            onValorCambia = viewModel::onCedulaChange,
            forma = FormaPildora
        )
        CampoTexto(
            etiqueta = "Teléfono",
            valor = uiState.telefono,
            onValorCambia = viewModel::onTelefonoChange,
            forma = FormaPildora
        )

        if (usuario.rol == RolUsuario.conductor) {
            CampoTexto(
                etiqueta = "Licencia de conducir",
                valor = uiState.licenciaConducir,
                onValorCambia = viewModel::onLicenciaChange,
                forma = FormaPildora
            )
        }

        DatoConLinea(etiqueta = "Correo (no editable)", valor = usuario.email)
        DatoConLinea(etiqueta = "Rol (no editable)", valor = usuario.rol.name)

        uiState.error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Start
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    FilaBotonesFormulario(
        textoAccion = "Guardar",
        onAccion = viewModel::guardarCambios,
        onCancelar = viewModel::cancelarEdicion,
        cargando = uiState.guardando
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PerfilScreenPreview() {
    TransAndinaFlotillaTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TransAndinaTopBar(titulo = "Perfil")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Column {
                    VistaPerfil(
                        uiState = PerfilUiState(
                            usuario = Usuario(
                                id = "1",
                                nombreCompleto = "Carlos Fernández Quesada",
                                cedula = "1-1111-1111",
                                email = "carlos@transandina.cr",
                                telefono = "8889-9900",
                                licenciaConducir = "B1-123456",
                                rol = RolUsuario.conductor
                            )
                        ),
                        onEditar = {},
                        onCerrarSesion = {}
                    )
                }
            }
        }
    }
}
