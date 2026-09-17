package com.transandina.flotilla.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.ALFA_FONDO_ESTADO
import com.transandina.flotilla.ui.theme.ALFA_FONDO_ESTADO_INFO
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/** Nivel de un estado: define el color del chip. */
enum class NivelEstado {
    /** "Vigente", "Al día", "Activo". */
    OK,

    /** "Por vencer", "Próximo", "Suspendido". */
    AVISO,

    /** "Vencido", "Atrasado", "Crítica", "Desactivado". */
    CRITICO,

    /** "Informativa". */
    INFO,

    /** "Desactivado": ya no hay nada que atender. */
    NEUTRO
}

/** Color pleno de cada nivel; el fondo del chip es este mismo con transparencia. */
@Composable
fun colorDeNivel(nivel: NivelEstado): Color {
    val colores = TransAndinaTheme.colores
    return when (nivel) {
        NivelEstado.OK -> colores.estadoOk
        NivelEstado.AVISO -> colores.estadoAviso
        NivelEstado.CRITICO -> colores.estadoCritico
        NivelEstado.INFO -> colores.estadoInfo
        NivelEstado.NEUTRO -> colores.estadoNeutro
    }
}

/**
 * Chip de estado: fondo del color al 15 % y texto Bold 12 del color pleno
 * (Figma `28:413`, `87:135`).
 */
@Composable
fun ChipEstado(
    texto: String,
    nivel: NivelEstado,
    modifier: Modifier = Modifier
) {
    val color = colorDeNivel(nivel)
    val alfaFondo = if (nivel == NivelEstado.INFO) {
        ALFA_FONDO_ESTADO_INFO
    } else {
        ALFA_FONDO_ESTADO
    }

    Row(
        modifier = modifier
            .background(color = color.copy(alpha = alfaFondo), shape = FormaPildora)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChipEstadoPreview() {
    TransAndinaFlotillaTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChipEstado(texto = "Al día", nivel = NivelEstado.OK)
            ChipEstado(texto = "Próximo", nivel = NivelEstado.AVISO)
            ChipEstado(texto = "Atrasado", nivel = NivelEstado.CRITICO)
            ChipEstado(texto = "Informativa", nivel = NivelEstado.INFO)
        }
    }
}
