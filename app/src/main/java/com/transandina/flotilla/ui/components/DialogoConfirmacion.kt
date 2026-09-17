package com.transandina.flotilla.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.transandina.flotilla.R
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Confirmación antes de una acción importante (reasignar, desactivar una
 * cuenta). Si [destructiva] es true, el botón de confirmar va en rojo.
 */
@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    destructiva: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(text = titulo, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(text = mensaje, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(
                    text = textoConfirmar,
                    fontWeight = FontWeight.Bold,
                    color = if (destructiva) {
                        MaterialTheme.colorScheme.error
                    } else {
                        TransAndinaTheme.colores.verde
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(
                    text = stringResource(R.string.cancelar),
                    color = TransAndinaTheme.colores.grisBoton
                )
            }
        }
    )
}

@Preview
@Composable
private fun DialogoConfirmacionPreview() {
    TransAndinaFlotillaTheme {
        DialogoConfirmacion(
            titulo = "¿Desactivar la cuenta?",
            mensaje = "Es permanente y libera el vehículo asignado.",
            textoConfirmar = "Desactivar",
            onConfirmar = {},
            onCancelar = {},
            destructiva = true
        )
    }
}
