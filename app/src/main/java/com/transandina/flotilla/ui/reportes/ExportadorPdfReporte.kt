package com.transandina.flotilla.ui.reportes

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.transandina.flotilla.R
import com.transandina.flotilla.ui.theme.Navy
import com.transandina.flotilla.ui.theme.Superficie
import com.transandina.flotilla.ui.theme.SuperficieSuave
import com.transandina.flotilla.ui.theme.TextoSecundario
import java.io.File

/** Contenido ya traducido del reporte; el exportador solo lo dibuja. */
data class DatosReportePdf(
    val titulo: String,
    val subtitulo: String,
    val resumen: String,
    val encabezados: List<String>,
    val filas: List<List<String>>,
    val pie: String
)

/**
 * Dibuja el reporte en un PDF tamaño carta con `android.graphics.pdf` y lo
 * comparte con el selector del sistema (docs/ADAPTACION_MOVIL.md §6).
 * No usa librerías externas.
 */
object ExportadorPdfReporte {

    private const val ANCHO = 612 // carta, en puntos
    private const val ALTO = 792
    private const val MARGEN = 36f
    private const val ALTO_FILA = 20f

    /** Ancho de cada columna; suman el ancho útil (540). La última va alineada a la derecha. */
    private val ANCHOS_COLUMNAS = floatArrayOf(62f, 64f, 118f, 66f, 90f, 80f, 60f)

    /** Debe coincidir con `android:authorities` en AndroidManifest.xml. */
    private fun autoridad(context: Context) = "${context.packageName}.archivos"

    /** Escribe el PDF en la caché de la app. Hacerlo fuera del hilo principal. */
    fun generar(context: Context, datos: DatosReportePdf, nombreArchivo: String): File {
        val documento = PdfDocument()
        try {
            val pincel = Pincel()
            var numeroPagina = 0
            var pagina: PdfDocument.Page? = null
            var y = 0f

            fun cerrarPagina() {
                pagina?.let { actual ->
                    actual.canvas.drawText(
                        "${datos.pie} $numeroPagina",
                        MARGEN,
                        ALTO - MARGEN / 2,
                        pincel.pie
                    )
                    documento.finishPage(actual)
                }
            }

            fun nuevaPagina() {
                cerrarPagina()
                numeroPagina++
                val info = PdfDocument.PageInfo.Builder(ANCHO, ALTO, numeroPagina).create()
                val nueva = documento.startPage(info)
                pagina = nueva
                val lienzo = nueva.canvas

                // Encabezado navy con la marca, como el TopBar de la app.
                lienzo.drawRect(0f, 0f, ANCHO.toFloat(), 56f, pincel.fondoMarca)
                lienzo.drawText(
                    context.getString(R.string.marca),
                    MARGEN,
                    36f,
                    pincel.marca
                )
                y = 84f
                if (numeroPagina == 1) {
                    lienzo.drawText(datos.titulo, MARGEN, y, pincel.titulo)
                    y += 18f
                    lienzo.drawText(datos.subtitulo, MARGEN, y, pincel.secundario)
                    y += 18f
                    lienzo.drawText(datos.resumen, MARGEN, y, pincel.resumen)
                    y += 24f
                }

                // Encabezado de la tabla en cada página.
                lienzo.drawRect(MARGEN, y, ANCHO - MARGEN, y + ALTO_FILA, pincel.fondoFila)
                dibujarFila(lienzo, datos.encabezados, y, pincel.encabezado)
                y += ALTO_FILA + 4f
            }

            nuevaPagina()
            datos.filas.forEach { fila ->
                if (y + ALTO_FILA > ALTO - MARGEN) nuevaPagina()
                pagina?.canvas?.let { dibujarFila(it, fila, y, pincel.celda) }
                y += ALTO_FILA
            }
            cerrarPagina()

            val carpeta = File(context.cacheDir, "reportes").apply { mkdirs() }
            val archivo = File(carpeta, nombreArchivo)
            archivo.outputStream().use { documento.writeTo(it) }
            return archivo
        } finally {
            documento.close()
        }
    }

    /** Abre el selector del sistema para mandar el PDF por correo, WhatsApp, Drive, etc. */
    fun compartir(context: Context, archivo: File) {
        val uri = FileProvider.getUriForFile(context, autoridad(context), archivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.reporte_compartir))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun dibujarFila(
        lienzo: android.graphics.Canvas,
        celdas: List<String>,
        yFila: Float,
        pincel: Paint
    ) {
        val base = yFila + ALTO_FILA - 6f
        var x = MARGEN + 4f
        celdas.forEachIndexed { indice, texto ->
            val ancho = ANCHOS_COLUMNAS.getOrElse(indice) { 60f } - 6f
            val recortado = recortar(texto, ancho, pincel)
            val esUltima = indice == ANCHOS_COLUMNAS.lastIndex
            val xTexto = if (esUltima) x + ancho - pincel.measureText(recortado) else x
            lienzo.drawText(recortado, xTexto, base, pincel)
            x += ANCHOS_COLUMNAS.getOrElse(indice) { 60f }
        }
    }

    /** Corta el texto con "…" si no cabe en la columna. */
    private fun recortar(texto: String, ancho: Float, pincel: Paint): String {
        if (pincel.measureText(texto) <= ancho) return texto
        val cabe = pincel.breakText(texto, true, ancho - pincel.measureText("…"), null)
        return texto.take(cabe.coerceAtLeast(0)) + "…"
    }

    /** Estilos del PDF con los colores del tema (ui/theme/Color.kt). */
    private class Pincel {
        private fun base(tamano: Float, color: Int, negrita: Boolean = false) = Paint().apply {
            isAntiAlias = true
            textSize = tamano
            this.color = color
            typeface = if (negrita) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

        val fondoMarca = Paint().apply { color = Navy.toArgb() }
        val fondoFila = Paint().apply { color = SuperficieSuave.toArgb() }
        val marca = base(20f, Superficie.toArgb(), negrita = true)
        val titulo = base(16f, Navy.toArgb(), negrita = true)
        val secundario = base(10f, TextoSecundario.toArgb())
        val resumen = base(12f, Navy.toArgb(), negrita = true)
        val encabezado = base(9f, TextoSecundario.toArgb(), negrita = true)
        val celda = base(9f, Navy.toArgb())
        val pie = base(8f, TextoSecundario.toArgb())
    }
}
