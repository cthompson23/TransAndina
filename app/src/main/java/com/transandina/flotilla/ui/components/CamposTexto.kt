package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Campo de texto del Figma: etiqueta arriba, caja blanca con borde fino.
 *
 * @param forma `MaterialTheme.shapes.small` en las pantallas de auth y
 *   `FormaPildora` en los formularios con sesión iniciada.
 * @param colorEtiqueta se pasa claro cuando el fondo de la pantalla es navy.
 */
@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onValorCambia: (String) -> Unit,
    modifier: Modifier = Modifier,
    marcadorDePosicion: String? = null,
    forma: Shape = MaterialTheme.shapes.small,
    colorEtiqueta: Color = MaterialTheme.colorScheme.onBackground,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    habilitado: Boolean = true,
    soloLectura: Boolean = false,
    esError: Boolean = false,
    lineas: Int = 1
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = colorEtiqueta
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onValorCambia,
            modifier = Modifier.fillMaxWidth(),
            enabled = habilitado,
            readOnly = soloLectura,
            isError = esError,
            singleLine = lineas == 1,
            minLines = lineas,
            shape = forma,
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = marcadorDePosicion?.let {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TransAndinaTheme.colores.placeholder
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
            colors = coloresCampo()
        )
    }
}

/** Igual que [CampoTexto], con el ojo para mostrar u ocultar la contraseña. */
@Composable
fun CampoContrasena(
    etiqueta: String,
    valor: String,
    onValorCambia: (String) -> Unit,
    modifier: Modifier = Modifier,
    marcadorDePosicion: String? = null,
    forma: Shape = MaterialTheme.shapes.small,
    colorEtiqueta: Color = MaterialTheme.colorScheme.onBackground,
    habilitado: Boolean = true,
    esError: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = colorEtiqueta
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onValorCambia,
            modifier = Modifier.fillMaxWidth(),
            enabled = habilitado,
            isError = esError,
            singleLine = true,
            shape = forma,
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = marcadorDePosicion?.let {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TransAndinaTheme.colores.placeholder
                    )
                }
            },
            visualTransformation = if (visible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = if (visible) {
                            "Ocultar contraseña"
                        } else {
                            "Mostrar contraseña"
                        },
                        tint = TransAndinaTheme.colores.textoSecundario
                    )
                }
            },
            colors = coloresCampo()
        )
    }
}

/** Caja blanca con borde gris, igual en reposo y con foco (Figma `1:412`). */
@Composable
internal fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    errorContainerColor = MaterialTheme.colorScheme.surface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = TransAndinaTheme.colores.textoSecundario,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = TransAndinaTheme.colores.placeholder,
    disabledBorderColor = TransAndinaTheme.colores.placeholder
)

@Preview(showBackground = true)
@Composable
private fun CamposTextoPreview() {
    TransAndinaFlotillaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoTexto(
                etiqueta = "Correo electrónico",
                valor = "",
                onValorCambia = {},
                marcadorDePosicion = "ejemplo@correo.com"
            )
            CampoContrasena(
                etiqueta = "Contraseña",
                valor = "secreta",
                onValorCambia = {}
            )
            Text(
                text = "Con sesión iniciada los campos van en forma de píldora",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
