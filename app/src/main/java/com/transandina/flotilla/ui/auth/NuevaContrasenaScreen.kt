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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Misma paleta que LoginScreen / RegistroScreen / PerfilScreen / HomeScreen
// — pendiente moverla a un Color.kt compartido (ui/theme/Color.kt).
private val PageBackground = Color(0xFFD9D9D9)
private val OrangeAccent = Color(0xFFBB6B2E)
private val FieldBackground = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF13263F)
private val ErrorColor = Color(0xFFC62828)

@Composable
fun NuevaContrasenaScreen(
    viewModel: NuevaContrasenaViewModel = viewModel(),
    onContrasenaActualizada: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.actualizada) {
        if (uiState.actualizada) onContrasenaActualizada()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Trasandina",
                fontFamily = FontFamily.Serif,
                fontSize = 26.sp,
                color = OrangeAccent
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nueva contraseña",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(28.dp))

            CampoContrasena(
                label = "Nueva contraseña",
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange
            )
            Spacer(modifier = Modifier.height(12.dp))

            CampoContrasena(
                label = "Confirmar contraseña",
                value = uiState.confirmarPassword,
                onValueChange = viewModel::onConfirmarPasswordChange
            )

            uiState.error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = ErrorColor, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::guardarNuevaContrasena,
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
                    Text("Guardar nueva contraseña", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CampoContrasena(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = TextPrimary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
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