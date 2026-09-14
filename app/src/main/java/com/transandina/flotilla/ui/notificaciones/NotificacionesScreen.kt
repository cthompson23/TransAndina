package com.transandina.flotilla.ui.notificaciones

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Colores TransAndina
private val AzulTransAndina = Color(0xFF13263F)
private val GrisFondo = Color(0xFFD9D9D9)
private val Blanco = Color(0xFFFFFFFF)
private val TextoSecundario = Color(0xFF8A8A8A)

@Composable
fun NotificacionesScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisFondo)
    ) {

        // =========================
        // HEADER
        // =========================

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(AzulTransAndina),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "TransAndina",
                color = Blanco,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // =========================
        // CONTENIDO
        // =========================

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            contentAlignment = Alignment.TopCenter
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Blanco
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 28.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // Ícono
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(
                                color = GrisFondo,
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notificaciones",
                            tint = AzulTransAndina,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "No hay notificaciones",
                        color = AzulTransAndina,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Aquí aparecerán las alertas y notificaciones relacionadas con tu vehículo.",
                        color = TextoSecundario,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}