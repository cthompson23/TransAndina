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
fun formatearKilometros(km: Double): String = agruparMiles(km)

/** Igual que [formatearKilometros] pero con la unidad: "492 400 km". */
fun formatearKilometrosConUnidad(km: Double): String = "${formatearKilometros(km)} km"

/** Montos en colones sin decimales, como en el Figma: "₡ 45 000". */
fun formatearColones(monto: Double): String = "₡ ${agruparMiles(monto)}"

/**
 * Lee un monto escrito a mano: "₡ 45 000", "45000,50", "45.000" o "1.234,5".
 * La coma es decimal (costumbre en Costa Rica); un punto seguido de tres
 * dígitos se toma como separador de miles. Devuelve null si no es un número
 * o es negativo.
 */
fun interpretarMonto(texto: String): Double? {
    val limpio = texto.filterNot { it.isWhitespace() || it == '₡' }
    if (limpio.isEmpty()) return null

    val normalizado = when {
        ',' in limpio && '.' in limpio ->
            if (limpio.lastIndexOf(',') > limpio.lastIndexOf('.')) {
                limpio.replace(".", "").replace(',', '.')
            } else {
                limpio.replace(",", "")
            }
        ',' in limpio ->
            if (limpio.count { it == ',' } == 1) limpio.replace(',', '.') else limpio.replace(",", "")
        '.' in limpio ->
            if (limpio.count { it == '.' } > 1 || limpio.substringAfterLast('.').length == 3) {
                limpio.replace(".", "")
            } else {
                limpio
            }
        else -> limpio
    }
    return normalizado.toDoubleOrNull()?.takeIf { it >= 0 }
}

/** Kilómetros escritos a mano ("492 400", "492.400"): solo cuentan los dígitos. */
fun interpretarKilometros(texto: String): Double? =
    texto.filter(Char::isDigit).takeIf { it.isNotEmpty() }?.toDoubleOrNull()

/** Redondea a entero y separa los miles con un espacio: 492400.0 → "492 400". */
private fun agruparMiles(valor: Double): String {
    val entero = valor.roundToLong()
    val signo = if (entero < 0) "-" else ""
    val digitos = abs(entero).toString()

    val conSeparador = digitos
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()

    return signo + conSeparador
}

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
