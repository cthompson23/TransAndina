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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.data.model.RolUsuario

// Misma paleta que LoginScreen / HomeScreen / PerfilScreen — pendiente
// moverla a un Color.kt compartido (ui/theme/Color.kt) para no repetirla.
private val PageBackground = Color(0xFF13263F)
private val OrangeAccent = Color(0xFFBB6B2E)
private val FieldBackground = Color(0xFFFFFFFF)
private val FieldPlaceholder = Color(0xFF8A8A8A)
private val TextPrimary = Color(0xFFFFFFFF)
private val ErrorColor = Color(0xFFC62828)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text = "TransAndina",
                fontFamily = FontFamily.Serif,
                fontSize = 26.sp,
                color = OrangeAccent
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Crear cuenta",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            CampoTexto(
                label = "Nombre completo",
                value = uiState.nombreCompleto,
                onValueChange = viewModel::onNombreChange
            )
            Spacer(modifier = Modifier.height(12.dp))

            CampoTexto(
                label = "Cédula",
                value = uiState.cedula,
                onValueChange = viewModel::onCedulaChange,
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(12.dp))

            CampoTexto(
                label = "Correo electrónico",
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                keyboardType = KeyboardType.Email
            )
            Spacer(modifier = Modifier.height(12.dp))

            CampoTexto(
                label = "Teléfono",
                value = uiState.telefono,
                onValueChange = viewModel::onTelefonoChange,
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Tipo de cuenta", color = TextPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            SelectorRol(rolSeleccionado = uiState.rol, onRolChange = viewModel::onRolChange)

            if (uiState.rol == RolUsuario.conductor) {
                Spacer(modifier = Modifier.height(12.dp))
                CampoTexto(
                    label = "Número de licencia de conducir",
                    value = uiState.licenciaConducir,
                    onValueChange = viewModel::onLicenciaChange
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            CampoTexto(
                label = "Contraseña",
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(12.dp))

            CampoTexto(
                label = "Confirmar contraseña",
                value = uiState.confirmarPassword,
                onValueChange = viewModel::onConfirmarPasswordChange,
                visualTransformation = PasswordVisualTransformation()
            )

            uiState.error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = ErrorColor, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::registrar,
                enabled = !uiState.cargando,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeAccent,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = onIrALogin, modifier = Modifier.fillMaxWidth()) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = TextPrimary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CampoTexto(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = TextPrimary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FieldBackground,
                unfocusedContainerColor = FieldBackground,
                disabledContainerColor = FieldBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = OrangeAccent
            ),
            modifier = Modifier.fillMaxWidth()
        )
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
        listOf(RolUsuario.conductor to "Conductor", RolUsuario.mecanico to "Mecánico").forEach { (rol, etiqueta) ->
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
                        color = if (seleccionado) OrangeAccent else FieldBackground,
                        shape = RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = etiqueta,
                    color = if (seleccionado) Color.White else FieldPlaceholder,
                    fontSize = 14.sp,
                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}