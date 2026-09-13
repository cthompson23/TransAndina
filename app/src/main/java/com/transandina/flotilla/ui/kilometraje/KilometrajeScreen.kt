package com.transandina.flotilla.ui.kilometraje

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun KilometrajeScreen(
    viewModel: KilometrajeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Colores de TransAndina
    val azulOscuro = Color(0xFF0D2948)
    val naranja = Color(0xFFD26320)
    val blanco = Color(0xFFFFFFFF)
    val grisClaro = Color(0xFFD0D3D8)
    val grisTexto = Color(0xFF4A4A4A)
    val grisPlaceholder = Color(0xFF9DA3AC)
    val grisFondo = Color(0xFFF5F6F7)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(blanco)
    ) {

        // =====================================================
        // HEADER
        // =====================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(155.dp)
                .background(azulOscuro)
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {
                    // Acción para regresar
                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = blanco,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "Registrar kilometraje",
                color = blanco,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 28.dp)
            )
        }

        // =====================================================
        // CONTENIDO
        // =====================================================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(blanco)
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            // ----------------------------------------
            // CARGANDO VEHÍCULO
            // ----------------------------------------

            if (uiState.vehiculo == null && uiState.cargando) {
                CircularProgressIndicator(
                    color = naranja,
                    modifier = Modifier.size(40.dp)
                )
                return@Column
            }

            // ----------------------------------------
            // VEHÍCULO
            // ----------------------------------------

            val vehiculo = uiState.vehiculo

            if (vehiculo == null) {
                Text(
                    text = uiState.error ?: "No tienes un vehículo asignado",
                    color = grisTexto,
                    fontSize = 15.sp
                )
                return@Column
            }

            // ----------------------------------------
            // INFORMACIÓN DEL VEHÍCULO
            // ----------------------------------------

            Text(
                text = "${vehiculo.marca} ${vehiculo.modelo}",
                color = azulOscuro,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Placa: ${vehiculo.placa}",
                color = grisTexto,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ----------------------------------------
            // KILOMETRAJE ACTUAL
            // ----------------------------------------

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = grisFondo,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Kilometraje actual",
                    color = grisTexto,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "${vehiculo.kmActual} km",
                    color = azulOscuro,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ----------------------------------------
            // NUEVO KILOMETRAJE
            // ----------------------------------------

            Text(
                text = "Nuevo kilometraje",
                color = azulOscuro,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 7.dp)
            )

            OutlinedTextField(
                value = uiState.kmIngresado,
                onValueChange = viewModel::onKmChange,

                placeholder = {
                    Text(
                        text = "Ingrese el kilometraje",
                        color = grisPlaceholder,
                        fontSize = 12.sp
                    )
                },

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),

                singleLine = true,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),

                shape = RoundedCornerShape(10.dp),

                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = blanco,
                    unfocusedContainerColor = blanco,
                    focusedBorderColor = naranja,
                    unfocusedBorderColor = grisClaro,
                    focusedTextColor = grisTexto,
                    unfocusedTextColor = grisTexto
                )
            )

            // ----------------------------------------
            // ERROR
            // ----------------------------------------

            uiState.error?.let {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = it,
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp
                )
            }

            // ----------------------------------------
            // MENSAJE DE ÉXITO
            // ----------------------------------------

            if (uiState.exito) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Kilometraje actualizado correctamente",
                    color = Color(0xFF388E3C),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ----------------------------------------
            // BOTÓN GUARDAR
            // ----------------------------------------

            Button(
                onClick = viewModel::registrarKilometraje,
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
                        modifier = Modifier.size(20.dp),
                        color = blanco,
                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text = "Guardar",
                        color = blanco,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}