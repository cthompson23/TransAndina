package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Etiqueta corta + número grande, en versión compacta para que entren tres
 * en una fila (Figma `102:158`, adaptado a móvil según §4).
 */
@Composable
fun TarjetaKpi(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    TarjetaTransAndina(modifier = modifier, relleno = 12.dp) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = TransAndinaTheme.colores.textoSecundario
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFD9D9D9)
@Composable
private fun TarjetaKpiPreview() {
    TransAndinaFlotillaTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaKpi(etiqueta = "Activos", valor = "24", modifier = Modifier.weight(1f))
            TarjetaKpi(etiqueta = "Mant. próximos", valor = "5", modifier = Modifier.weight(1f))
            TarjetaKpi(etiqueta = "Docs. por vencer", valor = "3", modifier = Modifier.weight(1f))
        }
    }
}
