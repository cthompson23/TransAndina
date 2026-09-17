package com.transandina.flotilla.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Alerta con franja de color a la izquierda, título, detalle y chip de nivel
 * (Figma `102:238`). Toda la tarjeta es tocable si se pasa [onClick].
 */
@Composable
fun TarjetaAlerta(
    titulo: String,
    detalle: String,
    textoNivel: String,
    nivel: NivelEstado,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    TarjetaTransAndina(modifier = modifier, onClick = onClick, relleno = 12.dp) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(colorDeNivel(nivel), RoundedCornerShape(2.dp))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TransAndinaTheme.colores.textoSecundario
                )
            }
            ChipEstado(texto = textoNivel, nivel = nivel)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFD9D9D9)
@Composable
private fun TarjetaAlertaPreview() {
    TransAndinaFlotillaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaAlerta(
                titulo = "Permiso de carga vencido",
                detalle = "SCD-3421 · Nissan Frontier · venció el 10/08/2026",
                textoNivel = "Crítica",
                nivel = NivelEstado.CRITICO,
                onClick = {}
            )
            TarjetaAlerta(
                titulo = "Mantenimiento preventivo próximo",
                detalle = "SJO-2291 · Cambio de aceite · faltan 800 km",
                textoNivel = "Próxima",
                nivel = NivelEstado.AVISO
            )
        }
    }
}
