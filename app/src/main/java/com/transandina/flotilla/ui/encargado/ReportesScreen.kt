package com.transandina.flotilla.ui.encargado

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme

/** Pantalla del encargado pendiente de construir (Figma `102:198`). */
@Composable
fun ReportesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(titulo = "Reportes")
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            EstadoVacio(mensaje = "Próximamente: costos totales de mantenimiento filtrables por periodo.")
        }
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun ReportesScreenPreview() {
    TransAndinaFlotillaTheme {
        ReportesScreen()
    }
}
