package com.transandina.flotilla.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme

/**
 * Caja blanca con borde navy punteado y "+ texto" (Figma `67:133`,
 * "Adjuntar fotografía").
 */
@Composable
fun BotonAdjuntar(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = if (habilitado) 1f else 0.4f)
    val forma = MaterialTheme.shapes.medium

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(forma)
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                val radio = 12.dp.toPx()
                drawRoundRect(
                    color = color,
                    cornerRadius = CornerRadius(radio, radio),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(8.dp.toPx(), 5.dp.toPx())
                        )
                    )
                )
            }
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFD9D9D9)
@Composable
private fun BotonAdjuntarPreview() {
    TransAndinaFlotillaTheme {
        BotonAdjuntar(
            texto = "Adjuntar fotografía",
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
