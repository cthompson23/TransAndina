package com.transandina.flotilla.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginExitoso: () -> Unit,
    onRegistrar: () -> Unit = {},
    onRecuperarContrasena: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var mostrarPassword by remember { mutableStateOf(false) }

    // Colores de la pantalla
    val azulOscuro = Color(0xFF0D2948)
    val naranja = Color(0xFFD26320)
    val blanco = Color(0xFFFFFFFF)
    val grisClaro = Color(0xFFD0D3D8)
    val grisPlaceholder = Color(0xFF9DA3AC)

    LaunchedEffect(uiState.autenticado) {
        if (uiState.autenticado) {
            onLoginExitoso()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(azulOscuro)
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ------------------------------------------------
        // LOGO / NOMBRE
        // ------------------------------------------------
        Spacer(modifier = Modifier.height(115.dp))

        Text(
            text = "TransAndina",
            color = naranja,
            fontSize = 40.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(62.dp))

        // ------------------------------------------------
        // TÍTULO
        // ------------------------------------------------
        Text(
            text = "Iniciar sesión",
            color = blanco,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(62.dp))

        // ------------------------------------------------
        // CORREO
        // ------------------------------------------------
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Correo electrónico",
                color = grisClaro,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 7.dp)
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = {
                    Text(
                        text = "ejemplo@correo.com",
                        color = grisPlaceholder,
                        fontSize = 12.sp
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = blanco,
                    unfocusedContainerColor = blanco,
                    focusedBorderColor = naranja,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.DarkGray,
                    unfocusedTextColor = Color.DarkGray
                )
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // ------------------------------------------------
        // CONTRASEÑA
        // ------------------------------------------------
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Contraseña",
                color = grisClaro,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 7.dp)
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = {
                    Text(
                        text = "Escribir la contraseña",
                        color = grisPlaceholder,
                        fontSize = 12.sp
                    )
                },
                singleLine = true,
                visualTransformation = if (mostrarPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            mostrarPassword = !mostrarPassword
                        }
                    ) {
                        Icon(
                            imageVector = if (mostrarPassword) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = "Mostrar contraseña",
                            tint = Color.DarkGray
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = blanco,
                    unfocusedContainerColor = blanco,
                    focusedBorderColor = naranja,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.DarkGray,
                    unfocusedTextColor = Color.DarkGray
                )
            )
        }

        // ------------------------------------------------
        // RECUPERAR CONTRASEÑA
        // ------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Recuperar contraseña",
                color = grisClaro,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    onRecuperarContrasena()
                }
            )
        }

        Spacer(modifier = Modifier.height(42.dp))

        // ------------------------------------------------
        // ERROR
        // ------------------------------------------------
        uiState.error?.let {
            Text(
                text = it,
                color = Color(0xFFFF8A80),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        Button(
            onClick = viewModel::iniciarSesion,
            enabled = !uiState.cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = naranja,
                disabledContainerColor = naranja.copy(alpha = 0.6f)
            )
        ) {
            if (uiState.cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = blanco,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Ingresar",
                    color = blanco,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 60.dp)
        ) {
            Text(
                text = "¿No tienes una cuenta?",
                color = blanco,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onRegistrar,
                modifier = Modifier.height(38.dp),
                shape = RoundedCornerShape(9.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 13.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = naranja
                )
            ) {
                Text(
                    text = "Regístrate aquí",
                    color = blanco,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}