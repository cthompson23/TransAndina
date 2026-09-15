package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/** Mensaje centrado para cuando una lista no tiene datos. */
@Composable
fun EstadoVacio(
    mensaje: String,
    modifier: Modifier = Modifier,
    icono: ImageVector = Icons.Filled.Inbox
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = TransAndinaTheme.colores.textoSecundario,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = TransAndinaTheme.colores.textoSecundario,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFD9D9D9)
@Composable
private fun EstadoVacioPreview() {
    TransAndinaFlotillaTheme {
        EstadoVacio(mensaje = "Todavía no hay mantenimientos registrados")
    }
}
