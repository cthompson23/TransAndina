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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Misma paleta que LoginScreen / RegistroScreen / NuevaContrasenaScreen /
// PerfilScreen / HomeScreen — pendiente moverla a un Color.kt compartido
// (ui/theme/Color.kt).
private val PageBackground = Color(0xFF13263F)
private val OrangeAccent = Color(0xFFBB6B2E)
private val FieldBackground = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF6B7280)
private val ErrorColor = Color(0xFFC62828)

@Composable
fun RecuperarContrasenaScreen(
    viewModel: RecuperarContrasenaViewModel = viewModel(),
    onVolverALogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
                text = "TransAndina",
                fontFamily = FontFamily.Serif,
                fontSize = 34.sp,
                color = OrangeAccent
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recuperar contraseña",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.correoEnviado) {
                Text(
                    text = "Te enviamos un correo con un enlace para restablecer tu contraseña. " +
                            "Ábrelo desde este mismo dispositivo.",
                    textAlign = TextAlign.Center,
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onVolverALogin) {
                    Text("Volver a iniciar sesión", color = OrangeAccent, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "Ingresa tu correo y te mandamos un enlace para restablecer tu contraseña.",
                    textAlign = TextAlign.Center,
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                CampoTexto(
                    label = "Correo electrónico",
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange
                )

                uiState.error?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = it, color = ErrorColor, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = viewModel::enviarCorreo,
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
                        Text("Enviar enlace", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onVolverALogin) {
                    Text("Volver a iniciar sesión", color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CampoTexto(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = TextPrimary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
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