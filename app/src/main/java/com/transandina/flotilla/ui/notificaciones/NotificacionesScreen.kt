package com.transandina.flotilla.ui.notificaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/** Alertas del conductor (Figma `28:312`). */
@Composable
fun NotificacionesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(titulo = "TransAndina")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            TarjetaTransAndina {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EstadoVacio(
                        mensaje = "No hay notificaciones",
                        icono = Icons.Filled.NotificationsNone
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Aquí aparecerán las alertas y notificaciones relacionadas con tu vehículo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TransAndinaTheme.colores.textoSecundario,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun NotificacionesScreenPreview() {
    TransAndinaFlotillaTheme {
        NotificacionesScreen()
    }
}
