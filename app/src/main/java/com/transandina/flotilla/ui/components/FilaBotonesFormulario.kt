package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme

/**
 * "Cancelar" + acción principal, lado a lado y del mismo ancho
 * (Figma `49:161`). Se ancla al final del formulario.
 */
@Composable
fun FilaBotonesFormulario(
    textoAccion: String,
    onAccion: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
    textoCancelar: String = "Cancelar",
    accionHabilitada: Boolean = true,
    cargando: Boolean = false,
    accionEsConfirmar: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BotonSecundario(
            texto = textoCancelar,
            onClick = onCancelar,
            modifier = Modifier.weight(1f),
            habilitado = !cargando
        )
        if (accionEsConfirmar) {
            BotonConfirmar(
                texto = textoAccion,
                onClick = onAccion,
                modifier = Modifier.weight(1f),
                habilitado = accionHabilitada,
                cargando = cargando
            )
        } else {
            BotonPrimario(
                texto = textoAccion,
                onClick = onAccion,
                modifier = Modifier.weight(1f),
                habilitado = accionHabilitada,
                cargando = cargando
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilaBotonesFormularioPreview() {
    TransAndinaFlotillaTheme {
        FilaBotonesFormulario(
            textoAccion = "Guardar",
            onAccion = {},
            onCancelar = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
