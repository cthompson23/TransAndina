package com.transandina.flotilla.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToLong

/** Formato de fecha que se muestra en pantalla. */
private val FORMATO_UI: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Formato que entiende Postgres para las columnas `date`. */
private val FORMATO_ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

/**
 * Kilómetros con separador de miles y espacio, como en el Figma: "492 400".
 * Se redondea a entero porque `km_actual` es numeric pero siempre se captura
 * sin decimales.
 */
fun formatearKilometros(km: Double): String {
    val entero = km.roundToLong()
    val signo = if (entero < 0) "-" else ""
    val digitos = abs(entero).toString()

    val conSeparador = digitos
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()

    return signo + conSeparador
}

/** Igual que [formatearKilometros] pero con la unidad: "492 400 km". */
fun formatearKilometrosConUnidad(km: Double): String = "${formatearKilometros(km)} km"

/**
 * Pasa una fecha ISO de Supabase al formato de pantalla: "2026-08-25" →
 * "25/08/2026". Si la cadena no es una fecha válida, se devuelve tal cual
 * para no esconder el dato.
 */
fun formatearFechaIso(fechaIso: String?): String {
    if (fechaIso.isNullOrBlank()) return ""
    return try {
        LocalDate.parse(fechaIso, FORMATO_ISO).format(FORMATO_UI)
    } catch (e: Exception) {
        fechaIso
    }
}

/** Formato de pantalla de una fecha ya interpretada: "25/08/2026". */
fun formatearFecha(fecha: LocalDate): String = fecha.format(FORMATO_UI)

/** Fecha en el formato que espera Supabase: "2026-08-25". */
fun aFechaIso(fecha: LocalDate): String = fecha.format(FORMATO_ISO)

/** Interpreta una fecha ISO; devuelve null si no se puede. */
fun parsearFechaIso(fechaIso: String?): LocalDate? {
    if (fechaIso.isNullOrBlank()) return null
    return try {
        LocalDate.parse(fechaIso, FORMATO_ISO)
    } catch (e: Exception) {
        null
    }
}
