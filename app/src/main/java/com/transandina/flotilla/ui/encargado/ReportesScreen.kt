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
fun ReportesScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "Reportes", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Próximamente: costos totales de mantenimiento filtrables por periodo.", style = MaterialTheme.typography.bodyLarge)
    }
}
