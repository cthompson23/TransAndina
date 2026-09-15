package com.transandina.flotilla.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
 * Barra de pestañas tipo segmento sobre fondo blanco: la activa va en navy
 * con texto blanco (Figma `28:418`, `87:135`). Si no caben, se desplaza.
 */
@Composable
fun PestanasSegmentadas(
    pestanas: List<String>,
    indiceSeleccionado: Int,
    onSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        pestanas.forEachIndexed { indice, pestana ->
            val activa = indice == indiceSeleccionado
            Text(
                text = pestana,
                style = MaterialTheme.typography.labelMedium,
                color = if (activa) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier
                    .background(
                        color = if (activa) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = MaterialTheme.shapes.small
                    )
                    .clickable { onSeleccionar(indice) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFD9D9D9)
@Composable
private fun PestanasSegmentadasPreview() {
    TransAndinaFlotillaTheme {
        PestanasSegmentadas(
            pestanas = listOf("Información", "Historial", "Kilometraje", "Documentos"),
            indiceSeleccionado = 0,
            onSeleccionar = {},
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}
