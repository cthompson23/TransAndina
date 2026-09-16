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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.R
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Alertas del conductor (Figma `28:312`). El título del Figma dice "Datos
 * personales" por error; el correcto es "Mis alertas".
 *
 * TODO(backend): todavía no hay de dónde sacar las alertas. Se pueden
 *  calcular en el cliente con las fechas de documentos, el kilometraje y
 *  `frecuencias_mantenimiento`, o con una vista en Postgres
 *  (docs/ADAPTACION_MOVIL.md §8). Mientras tanto la pantalla se queda vacía
 *  en vez de mostrar alertas inventadas.
 */
@Composable
fun NotificacionesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(titulo = stringResource(R.string.alertas_titulo))

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
                        mensaje = stringResource(R.string.alertas_vacio),
                        icono = Icons.Filled.NotificationsNone
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.alertas_vacio_detalle),
                        style = MaterialTheme.typography.bodySmall,
                        color = TransAndinaTheme.colores.textoSecundario,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(name = "Mis alertas (sin datos)", showBackground = true, heightDp = 600)
@Composable
private fun NotificacionesScreenPreview() {
    TransAndinaFlotillaTheme {
        NotificacionesScreen()
    }
}
