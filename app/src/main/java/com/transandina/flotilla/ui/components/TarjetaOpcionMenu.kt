package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Fila del hub de vehículo: título, descripción y chevron (Figma `70:133`).
 */
@Composable
fun TarjetaOpcionMenu(
    titulo: String,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaTransAndina(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TransAndinaTheme.colores.textoSecundario
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TransAndinaTheme.colores.textoSecundario
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFD9D9D9)
@Composable
private fun TarjetaOpcionMenuPreview() {
    TransAndinaFlotillaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaOpcionMenu(
                titulo = "Información del vehículo",
                descripcion = "Placa, tipo, marca, modelo y capacidad",
                onClick = {}
            )
            TarjetaOpcionMenu(
                titulo = "Registrar mantenimiento",
                descripcion = "Preventivo o correctivo, con evidencia",
                onClick = {}
            )
        }
    }
}
