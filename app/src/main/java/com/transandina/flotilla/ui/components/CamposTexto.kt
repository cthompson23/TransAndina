package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.R
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Cómo se dibuja la caja del campo.
 *
 * - [CAJA]: caja blanca con borde, para formularios sobre fondo navy o gris
 *   (Figma `1:404`, `1:370`).
 * - [SUBRAYADO]: solo una línea inferior, para editar datos ya existentes
 *   (Figma `28:263`).
 */
enum class VarianteCampo { CAJA, SUBRAYADO }

/**
 * Campo de texto del Figma: etiqueta arriba y caja blanca debajo.
 *
 * @param forma `MaterialTheme.shapes.small` en auth y [FormaPildora] en los
 *   formularios con sesión iniciada. Se ignora si la variante es [VarianteCampo.SUBRAYADO].
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
    variante: VarianteCampo = VarianteCampo.CAJA,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    habilitado: Boolean = true,
    soloLectura: Boolean = false,
    esError: Boolean = false,
    lineas: Int = 1
) {
    CampoBase(
        etiqueta = etiqueta,
        valor = valor,
        onValorCambia = onValorCambia,
        modifier = modifier,
        marcadorDePosicion = marcadorDePosicion,
        forma = forma,
        colorEtiqueta = colorEtiqueta,
        variante = variante,
        tipoTeclado = tipoTeclado,
        habilitado = habilitado,
        soloLectura = soloLectura,
        esError = esError,
        lineas = lineas
    )
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
    variante: VarianteCampo = VarianteCampo.CAJA,
    habilitado: Boolean = true,
    esError: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }

    CampoBase(
        etiqueta = etiqueta,
        valor = valor,
        onValorCambia = onValorCambia,
        modifier = modifier,
        marcadorDePosicion = marcadorDePosicion,
        forma = forma,
        colorEtiqueta = colorEtiqueta,
        variante = variante,
        tipoTeclado = KeyboardType.Password,
        habilitado = habilitado,
        esError = esError,
        transformacion = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        iconoFinal = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    },
                    contentDescription = if (visible) {
                        stringResource(R.string.ocultar_contrasena)
                    } else {
                        stringResource(R.string.mostrar_contrasena)
                    },
                    tint = TransAndinaTheme.colores.textoSecundario
                )
            }
        }
    )
}

@Composable
private fun CampoBase(
    etiqueta: String,
    valor: String,
    onValorCambia: (String) -> Unit,
    modifier: Modifier,
    marcadorDePosicion: String?,
    forma: Shape,
    colorEtiqueta: Color,
    variante: VarianteCampo,
    tipoTeclado: KeyboardType,
    habilitado: Boolean,
    esError: Boolean,
    soloLectura: Boolean = false,
    lineas: Int = 1,
    transformacion: VisualTransformation = VisualTransformation.None,
    iconoFinal: @Composable (() -> Unit)? = null
) {
    val marcador: @Composable (() -> Unit)? = marcadorDePosicion?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = TransAndinaTheme.colores.placeholder
            )
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = colorEtiqueta
        )
        when (variante) {
            VarianteCampo.CAJA -> OutlinedTextField(
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
                placeholder = marcador,
                visualTransformation = transformacion,
                trailingIcon = iconoFinal,
                keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
                colors = coloresCampo()
            )

            // Sin caja: el valor se ve como texto y solo lo separa una línea,
            // igual que en "Editar datos" (Figma `28:263`).
            VarianteCampo.SUBRAYADO -> TextField(
                value = valor,
                onValueChange = onValorCambia,
                modifier = Modifier.fillMaxWidth(),
                enabled = habilitado,
                readOnly = soloLectura,
                isError = esError,
                singleLine = lineas == 1,
                minLines = lineas,
                shape = RectangleShape,
                textStyle = MaterialTheme.typography.titleMedium,
                placeholder = marcador,
                visualTransformation = transformacion,
                trailingIcon = iconoFinal,
                keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedTextColor = TransAndinaTheme.colores.grisBoton,
                    unfocusedTextColor = TransAndinaTheme.colores.grisBoton,
                    disabledTextColor = TransAndinaTheme.colores.textoSecundario,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = TransAndinaTheme.colores.textoSecundario,
                    disabledIndicatorColor = TransAndinaTheme.colores.placeholder
                )
            )
        }
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
            CampoTexto(
                etiqueta = "Responsable",
                valor = "Carlos Fernández",
                onValorCambia = {},
                forma = FormaPildora
            )
            CampoContrasena(
                etiqueta = "Contraseña",
                valor = "secreta",
                onValorCambia = {}
            )
            CampoTexto(
                etiqueta = "Nombre completo",
                valor = "Carlos Fernández Quesada",
                onValorCambia = {},
                variante = VarianteCampo.SUBRAYADO
            )
        }
    }
}
