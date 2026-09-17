package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.R
import com.transandina.flotilla.domain.EstadoMantenimiento
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme

/** Chip "Al día" / "Próximo" / "Atrasado" del semáforo de mantenimiento (Figma `102:158`). */
@Composable
fun ChipEstadoMantenimiento(estado: EstadoMantenimiento, modifier: Modifier = Modifier) {
    when (estado) {
        EstadoMantenimiento.AL_DIA -> ChipEstado(
            texto = stringResource(R.string.estado_al_dia),
            nivel = NivelEstado.OK,
            modifier = modifier
        )
        EstadoMantenimiento.PROXIMO -> ChipEstado(
            texto = stringResource(R.string.estado_proximo),
            nivel = NivelEstado.AVISO,
            modifier = modifier
        )
        EstadoMantenimiento.ATRASADO -> ChipEstado(
            texto = stringResource(R.string.estado_atrasado),
            nivel = NivelEstado.CRITICO,
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChipEstadoMantenimientoPreview() {
    TransAndinaFlotillaTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EstadoMantenimiento.entries.forEach { ChipEstadoMantenimiento(it) }
        }
    }
}
