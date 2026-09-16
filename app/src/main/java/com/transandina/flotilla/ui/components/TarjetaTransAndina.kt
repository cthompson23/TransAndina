package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme

/**
 * Contenedor blanco de radio 12 dp, con o sin toque (Figma `28:408`).
 * Es la base de todas las tarjetas de la app.
 */
@Composable
fun TarjetaTransAndina(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    relleno: Dp = 16.dp,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val forma = MaterialTheme.shapes.medium
    val color = MaterialTheme.colorScheme.surface

    if (onClick == null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = forma,
            color = color
        ) {
            Column(modifier = Modifier.padding(relleno), content = contenido)
        }
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = forma,
            color = color
        ) {
            Column(modifier = Modifier.padding(relleno), content = contenido)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFD9D9D9)
@Composable
private fun TarjetaTransAndinaPreview() {
    TransAndinaFlotillaTheme {
        TarjetaTransAndina(modifier = Modifier.padding(16.dp)) {
            Text("Mi vehículo", style = MaterialTheme.typography.bodyMedium)
            Text("SCD-3421", style = MaterialTheme.typography.headlineSmall)
        }
    }
}
