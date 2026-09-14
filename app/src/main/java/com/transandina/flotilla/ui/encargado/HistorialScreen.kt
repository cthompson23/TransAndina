package com.transandina.flotilla.ui.encargado

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HistorialScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "Historial de mantenimientos", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Próximamente: historial completo filtrable por vehículo, tipo o rango de fechas.", style = MaterialTheme.typography.bodyLarge)
    }
}
