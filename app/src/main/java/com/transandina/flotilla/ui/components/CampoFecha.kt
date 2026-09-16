package com.transandina.flotilla.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/**
 * Campo de fecha con el `DatePicker` de Material 3. Muestra dd/mm/aaaa
 * y entrega la fecha como [LocalDate] (Figma `49:161`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    etiqueta: String,
    fecha: LocalDate?,
    onFechaCambia: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    marcadorDePosicion: String = "dd/mm/aaaa",
    forma: Shape = MaterialTheme.shapes.small,
    colorEtiqueta: Color = MaterialTheme.colorScheme.onBackground,
    habilitado: Boolean = true,
    fechaMaxima: LocalDate? = null
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    // Con `fechaMaxima` el calendario deja en gris todo lo posterior, por
    // ejemplo para que no se registre un recorrido con fecha futura.
    val topeMillis = fechaMaxima
        ?.plusDays(1)
        ?.atStartOfDay(ZoneOffset.UTC)
        ?.toInstant()
        ?.toEpochMilli()
    val seleccionables = remember(topeMillis) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                topeMillis == null || utcTimeMillis < topeMillis

            override fun isSelectableYear(year: Int): Boolean =
                fechaMaxima == null || year <= fechaMaxima.year
        }
    }
    val estadoPicker = rememberDatePickerState(
        initialSelectedDateMillis = fecha
            ?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli(),
        selectableDates = seleccionables
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = colorEtiqueta
        )
        Box {
            OutlinedTextField(
                value = fecha?.format(FORMATO_FECHA) ?: "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
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
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = TransAndinaTheme.colores.textoSecundario
                    )
                },
                colors = coloresCampo()
            )
            // El campo va deshabilitado para que no se abra el teclado: el
            // toque lo recoge esta capa y abre el calendario.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(enabled = habilitado) { mostrarDialogo = true }
            )
        }
    }

    if (mostrarDialogo) {
        DatePickerDialog(
            onDismissRequest = { mostrarDialogo = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estadoPicker.selectedDateMillis?.let { millis ->
                            onFechaCambia(
                                Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            )
                        }
                        mostrarDialogo = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = estadoPicker)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CampoFechaPreview() {
    TransAndinaFlotillaTheme {
        CampoFecha(
            etiqueta = "Fecha",
            fecha = LocalDate.of(2026, 8, 25),
            onFechaCambia = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
