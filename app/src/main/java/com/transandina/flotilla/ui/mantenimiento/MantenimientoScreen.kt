package com.transandina.flotilla.ui.mantenimiento

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

/** Mantenimientos, pendiente de construir (Figma `49:161`, `51:125`). */
@Composable
fun MantenimientoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(titulo = "Mantenimiento")
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            EstadoVacio(mensaje = "Próximamente: registro y bitácora de mantenimientos")
        }
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun MantenimientoScreenPreview() {
    TransAndinaFlotillaTheme {
        MantenimientoScreen()
    }
}
