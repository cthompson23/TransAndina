package com.transandina.flotilla.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.TipoMantenimiento
import com.transandina.flotilla.domain.formatearColones
import com.transandina.flotilla.domain.formatearFechaIso
import com.transandina.flotilla.domain.formatearKilometrosConUnidad
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

@Composable
fun etiquetaTipoMantenimiento(tipo: TipoMantenimiento): String = when (tipo) {
    TipoMantenimiento.preventivo -> stringResource(R.string.mantenimiento_preventivo)
    TipoMantenimiento.correctivo -> stringResource(R.string.mantenimiento_correctivo)
}

/**
 * Un mantenimiento como tarjeta (Figma `51:125`, `102:178`, `102:218`):
 * categoría y costo arriba, tipo y taller, y abajo fecha, km y responsable.
 * Con [placa] se antepone la unidad, para los reportes de toda la flotilla.
 * Con [onClick] la tarjeta se vuelve tocable; lo usa el encargado para
 * corregir o eliminar el registro.
 */
@Composable
fun TarjetaMantenimiento(
    mantenimiento: Mantenimiento,
    modifier: Modifier = Modifier,
    placa: String? = null,
    fotos: List<String> = emptyList(),
    onVerFoto: (String) -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    TarjetaTransAndina(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = if (placa != null) {
                    "$placa · ${mantenimiento.categoria}"
                } else {
                    mantenimiento.categoria
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = mantenimiento.costo?.let(::formatearColones)
                    ?: stringResource(R.string.mantenimiento_sin_costo),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = listOfNotNull(
                etiquetaTipoMantenimiento(mantenimiento.tipo),
                mantenimiento.taller?.takeIf { it.isNotBlank() }
            ).joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = listOfNotNull(
                formatearFechaIso(mantenimiento.fecha),
                formatearKilometrosConUnidad(mantenimiento.km),
                mantenimiento.responsable?.takeIf { it.isNotBlank() }
            ).joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = TransAndinaTheme.colores.textoSecundario
        )

        if (fotos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fotos.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = stringResource(R.string.mant_foto_evidencia),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(TransAndinaTheme.colores.superficieSuave)
                            .clickable { onVerFoto(url) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFD9D9D9)
@Composable
private fun TarjetaMantenimientoPreview() {
    val mantenimiento = Mantenimiento(
        id = "1",
        vehiculoId = "v1",
        registradoPor = "u1",
        tipo = TipoMantenimiento.preventivo,
        categoria = "Cambio de aceite",
        fecha = "2026-08-25",
        km = 492_400.0,
        responsable = "Marco Ureña",
        costo = 45_000.0,
        taller = "Taller Central"
    )
    TransAndinaFlotillaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaMantenimiento(mantenimiento)
            TarjetaMantenimiento(
                mantenimiento.copy(tipo = TipoMantenimiento.correctivo, costo = null, taller = null),
                placa = "SCD-3421"
            )
        }
    }
}
