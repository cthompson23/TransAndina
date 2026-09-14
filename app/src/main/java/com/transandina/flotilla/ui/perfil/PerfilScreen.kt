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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.data.model.RolUsuario

// Misma paleta que LoginScreen / HomeScreen / MainActivity — pendiente
// moverla a un Color.kt compartido (ui/theme/Color.kt) para no repetirla.
private val PageBackground = Color(0xFFD9D9D9)
private val NavyBackground = Color(0xFF13263F)
private val OrangeAccent = Color(0xFFBB6B2E)
private val FieldBackground = Color(0xFFFFFFFF)
private val FieldPlaceholder = Color(0xFF8A8A8A)
private val TextPrimary = Color(0xFF13263F)
private val TextMuted = Color(0xFF6B7280)
private val SuccessColor = Color(0xFF2E7D32)
private val ErrorColor = Color(0xFFC62828)

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = viewModel(),
    onSesionCerrada: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TransAndinaTopBar()

        Box(
            modifier = Modifier
                .weight(1f)
                .background(PageBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Perfil",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                when {
                    uiState.cargando && uiState.usuario == null -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = OrangeAccent)
                        }
                    }
                    uiState.usuario == null -> {
                        Text(
                            text = "No se pudo cargar tu perfil",
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    uiState.editando -> {
                        FormularioEdicion(uiState = uiState, viewModel = viewModel)
                    }
                    else -> {
                        VistaPerfil(uiState = uiState, viewModel = viewModel)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { mostrarDialogoCerrarSesion = true }) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = null,
                            tint = ErrorColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Cerrar sesión", color = ErrorColor, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCerrarSesion = false
                        viewModel.cerrarSesion(onSesionCerrada)
                    }
                ) {
                    Text("Cerrar sesión", color = ErrorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Encabezado fijo de marca, igual al de HomeScreen — navy sólido con
 * "TransAndina" centrado, para mantener el mismo lenguaje visual en
 * todas las pestañas.
 */
@Composable
private fun TransAndinaTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyBackground)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "TransAndina",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun VistaPerfil(uiState: PerfilUiState, viewModel: PerfilViewModel) {
    val usuario = uiState.usuario!!

    if (uiState.guardadoExitoso) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = SuccessColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Perfil actualizado correctamente", color = SuccessColor, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CampoSoloLectura(Icons.Filled.Person, "Nombre completo", usuario.nombreCompleto)
        CampoSoloLectura(Icons.Filled.Badge, "Cédula", usuario.cedula)
        CampoSoloLectura(Icons.Filled.Email, "Correo", usuario.email)
        CampoSoloLectura(Icons.Filled.Phone, "Teléfono", usuario.telefono ?: "Sin registrar")
        if (usuario.rol == RolUsuario.conductor) {
            CampoSoloLectura(
                Icons.Filled.DirectionsCar,
                "Licencia de conducir",
                usuario.licenciaConducir ?: "Sin registrar"
            )
        }
        CampoSoloLectura(Icons.Filled.Work, "Rol", usuario.rol.name)
    }

    Spacer(modifier = Modifier.height(20.dp))

    TextButton(
        onClick = viewModel::iniciarEdicion,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Editar perfil", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OrangeAccent)
    }
}

@Composable
private fun FormularioEdicion(uiState: PerfilUiState, viewModel: PerfilViewModel) {
    val usuario = uiState.usuario!!

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CampoEditable("Nombre completo", uiState.nombreCompleto, viewModel::onNombreChange)
        CampoEditable("Cédula", uiState.cedula, viewModel::onCedulaChange)
        CampoEditable("Teléfono", uiState.telefono, viewModel::onTelefonoChange)

        if (usuario.rol == RolUsuario.conductor) {
            CampoEditable("Licencia de conducir", uiState.licenciaConducir, viewModel::onLicenciaChange)
        }

        CampoSoloLectura(Icons.Filled.Email, "Correo (no editable)", usuario.email)
        CampoSoloLectura(Icons.Filled.Work, "Rol (no editable)", usuario.rol.name)

        uiState.error?.let {
            Text(text = it, color = ErrorColor, fontSize = 13.sp)
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = viewModel::cancelarEdicion,
            enabled = !uiState.guardando,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Text("Cancelar")
        }
        Button(
            onClick = viewModel::guardarCambios,
            enabled = !uiState.guardando,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent, contentColor = Color.White),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            if (uiState.guardando) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
            } else {
                Text("Guardar")
            }
        }
    }
}

@Composable
private fun CampoSoloLectura(icono: ImageVector, etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FieldBackground, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = FieldPlaceholder,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = etiqueta, color = TextMuted, fontSize = 12.sp)
            Text(text = valor, color = TextPrimary, fontSize = 15.sp)
        }
    }
}

@Composable
private fun CampoEditable(etiqueta: String, valor: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = etiqueta, color = TextMuted, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = valor,
            onValueChange = onValueChange,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FieldBackground,
                unfocusedContainerColor = FieldBackground,
                disabledContainerColor = FieldBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = OrangeAccent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}