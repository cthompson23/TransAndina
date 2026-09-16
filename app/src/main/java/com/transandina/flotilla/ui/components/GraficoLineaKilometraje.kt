package com.transandina.flotilla.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/** Un punto del gráfico: la etiqueta del eje X y el valor acumulado. */
data class PuntoGrafico(val etiqueta: String, val valor: Double)

/**
 * Línea de kilometraje acumulado (Figma `51:217`). Se dibuja con Canvas, sin
 * librerías externas: es una sola serie sin ejes ni cuadrícula, igual que el
 * prototipo.
 */
@Composable
fun GraficoLineaKilometraje(
    puntos: List<PuntoGrafico>,
    modifier: Modifier = Modifier
) {
    val colorLinea = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 8.dp)
        ) {
            if (puntos.isEmpty()) return@Canvas

            val minimo = puntos.minOf { it.valor }
            val maximo = puntos.maxOf { it.valor }
            // Si todas las lecturas son iguales, la línea va por el medio en
            // vez de dividir entre cero.
            val rango = (maximo - minimo).takeIf { it > 0.0 }

            val pasoX = if (puntos.size > 1) size.width / (puntos.size - 1) else 0f
            val grosor = 3.dp.toPx()

            val coordenadas = puntos.mapIndexed { indice, punto ->
                val x = if (puntos.size > 1) pasoX * indice else size.width / 2
                val proporcion = rango?.let { (punto.valor - minimo) / it } ?: 0.5
                // El eje Y crece hacia abajo en Canvas, por eso se invierte.
                val y = size.height - (proporcion.toFloat() * size.height)
                Offset(x, y.coerceIn(grosor, size.height - grosor))
            }

            if (coordenadas.size == 1) {
                drawCircle(color = colorLinea, radius = grosor, center = coordenadas.first())
                return@Canvas
            }

            val trazo = Path().apply {
                moveTo(coordenadas.first().x, coordenadas.first().y)
                coordenadas.drop(1).forEach { lineTo(it.x, it.y) }
            }

            drawPath(
                path = trazo,
                color = colorLinea,
                style = Stroke(width = grosor, cap = StrokeCap.Round)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            puntos.forEach { punto ->
                Text(
                    text = punto.etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = TransAndinaTheme.colores.textoSecundario
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GraficoLineaKilometrajePreview() {
    TransAndinaFlotillaTheme {
        GraficoLineaKilometraje(
            puntos = listOf(
                PuntoGrafico("Mar", 480_000.0),
                PuntoGrafico("Abr", 482_500.0),
                PuntoGrafico("May", 484_100.0),
                PuntoGrafico("Jun", 487_000.0),
                PuntoGrafico("Jul", 489_200.0),
                PuntoGrafico("Ago", 491_000.0),
                PuntoGrafico("Set", 492_400.0)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
