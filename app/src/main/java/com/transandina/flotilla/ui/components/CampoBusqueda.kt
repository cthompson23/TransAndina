package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.R
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Buscador de las listas del encargado (Figma `102:258`): caja blanca en
 * forma de píldora con lupa y botón para borrar.
 */
@Composable
fun CampoBusqueda(
    valor: String,
    onValorCambia: (String) -> Unit,
    marcadorDePosicion: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = FormaPildora,
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = {
            Text(
                text = marcadorDePosicion,
                style = MaterialTheme.typography.bodyMedium,
                color = TransAndinaTheme.colores.placeholder
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = TransAndinaTheme.colores.textoSecundario
            )
        },
        trailingIcon = if (valor.isNotEmpty()) {
            {
                IconButton(onClick = { onValorCambia("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.busqueda_borrar),
                        tint = TransAndinaTheme.colores.textoSecundario
                    )
                }
            }
        } else {
            null
        },
        colors = coloresCampo()
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFD9D9D9)
@Composable
private fun CampoBusquedaPreview() {
    TransAndinaFlotillaTheme {
        CampoBusqueda(
            valor = "",
            onValorCambia = {},
            marcadorDePosicion = "Nombre, cédula o correo",
            modifier = Modifier.padding(16.dp)
        )
    }
}
