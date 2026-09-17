package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Botón flotante verde con ícono y texto. Reemplaza los botones de acción de
 * la barra superior del escritorio, como "Registrar administrador"
 * (Figma `102:258`, docs/ADAPTACION_MOVIL.md §4).
 */
@Composable
fun BotonFlotante(
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        containerColor = TransAndinaTheme.colores.verde,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        icon = { Icon(imageVector = icono, contentDescription = null) },
        text = { Text(text = texto, style = MaterialTheme.typography.titleMedium) }
    )
}

@Preview
@Composable
private fun BotonFlotantePreview() {
    TransAndinaFlotillaTheme {
        BotonFlotante(
            texto = "Registrar administrador",
            icono = Icons.Filled.PersonAdd,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
