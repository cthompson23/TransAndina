package com.transandina.flotilla.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Fila de filtros de selección única ("Todos · Atrasado · Próximo · Al día").
 * Reemplaza los filtros en grilla del escritorio (docs/ADAPTACION_MOVIL.md §4).
 * Si no caben, se desplaza de lado.
 */
@Composable
fun ChipsFiltro(
    opciones: List<String>,
    indiceSeleccionado: Int,
    onSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEachIndexed { indice, opcion ->
            val activo = indice == indiceSeleccionado
            FilterChip(
                selected = activo,
                onClick = { onSeleccionar(indice) },
                label = {
                    Text(text = opcion, style = MaterialTheme.typography.labelMedium)
                },
                shape = FormaPildora,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.primary,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = if (activo) {
                    null
                } else {
                    BorderStroke(1.dp, TransAndinaTheme.colores.placeholder)
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFD9D9D9)
@Composable
private fun ChipsFiltroPreview() {
    TransAndinaFlotillaTheme {
        ChipsFiltro(
            opciones = listOf("Todos", "Atrasado (2)", "Próximo (3)", "Al día (19)"),
            indiceSeleccionado = 1,
            onSeleccionar = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
