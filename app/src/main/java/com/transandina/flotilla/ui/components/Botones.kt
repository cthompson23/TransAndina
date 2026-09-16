package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/** Alto de todos los botones de la app (docs/ADAPTACION_MOVIL.md §2). */
private val ALTO_BOTON = 52.dp

/**
 * Botón de la acción principal: naranja. Es la base de los demás botones,
 * que solo cambian de color.
 */
@Composable
fun BotonPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false
) {
    BotonBase(
        texto = texto,
        onClick = onClick,
        colorFondo = MaterialTheme.colorScheme.secondary,
        modifier = modifier,
        habilitado = habilitado,
        cargando = cargando
    )
}

/** Botón de confirmar ("Guardar", "Registrar"): verde. */
@Composable
fun BotonConfirmar(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false
) {
    BotonBase(
        texto = texto,
        onClick = onClick,
        colorFondo = TransAndinaTheme.colores.verde,
        modifier = modifier,
        habilitado = habilitado,
        cargando = cargando
    )
}

/** Botón secundario ("Cancelar"): gris. */
@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    BotonBase(
        texto = texto,
        onClick = onClick,
        colorFondo = TransAndinaTheme.colores.grisBoton,
        modifier = modifier,
        habilitado = habilitado
    )
}

/** Acción destructiva ("Cerrar sesión"): rojo. */
@Composable
fun BotonDestructivo(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false
) {
    BotonBase(
        texto = texto,
        onClick = onClick,
        colorFondo = MaterialTheme.colorScheme.error,
        modifier = modifier,
        habilitado = habilitado,
        cargando = cargando
    )
}

@Composable
private fun BotonBase(
    texto: String,
    onClick: () -> Unit,
    colorFondo: Color,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = ALTO_BOTON),
        enabled = habilitado && !cargando,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorFondo,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = colorFondo.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.heightIn(min = 20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = texto, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BotonesPreview() {
    TransAndinaFlotillaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BotonPrimario("Ingresar", {}, Modifier.fillMaxWidth())
            BotonConfirmar("Guardar", {}, Modifier.fillMaxWidth())
            BotonSecundario("Cancelar", {}, Modifier.fillMaxWidth())
            BotonDestructivo("Cerrar sesión", {}, Modifier.fillMaxWidth())
            BotonPrimario("Ingresando", {}, Modifier.fillMaxWidth(), cargando = true)
        }
    }
}
