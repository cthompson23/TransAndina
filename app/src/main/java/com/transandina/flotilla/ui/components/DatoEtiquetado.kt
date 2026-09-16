package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Etiqueta gris arriba y valor abajo, para pantallas de detalle
 * (Figma `28:163`, `28:418`).
 */
@Composable
fun DatoEtiquetado(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    colorEtiqueta: Color = TransAndinaTheme.colores.textoSecundario,
    colorValor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = colorEtiqueta
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.titleMedium,
            color = colorValor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DatoEtiquetadoPreview() {
    TransAndinaFlotillaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DatoEtiquetado(etiqueta = "Placa", valor = "SCD-3421")
            DatoEtiquetado(etiqueta = "Capacidad", valor = "1,1 toneladas")
        }
    }
}
