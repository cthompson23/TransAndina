package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Desplegable con el mismo aspecto que [CampoTexto] (Figma `1:1113`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoSeleccion(
    etiqueta: String,
    seleccion: String?,
    opciones: List<String>,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier,
    marcadorDePosicion: String = "Selecciona una opción",
    forma: Shape = MaterialTheme.shapes.small,
    colorEtiqueta: Color = MaterialTheme.colorScheme.onBackground,
    habilitado: Boolean = true
) {
    var desplegado by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = colorEtiqueta
        )
        ExposedDropdownMenuBox(
            expanded = desplegado,
            onExpandedChange = { if (habilitado) desplegado = !desplegado }
        ) {
            OutlinedTextField(
                value = seleccion ?: "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                readOnly = true,
                enabled = habilitado,
                singleLine = true,
                shape = forma,
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = {
                    Text(
                        text = marcadorDePosicion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TransAndinaTheme.colores.placeholder
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegado) },
                colors = coloresCampo()
            )
            ExposedDropdownMenu(
                expanded = desplegado,
                onDismissRequest = { desplegado = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = {
                            Text(text = opcion, style = MaterialTheme.typography.bodyLarge)
                        },
                        onClick = {
                            onSeleccionar(opcion)
                            desplegado = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CampoSeleccionPreview() {
    TransAndinaFlotillaTheme {
        CampoSeleccion(
            etiqueta = "Tipo",
            seleccion = "Preventivo",
            opciones = listOf("Preventivo", "Correctivo"),
            onSeleccionar = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
