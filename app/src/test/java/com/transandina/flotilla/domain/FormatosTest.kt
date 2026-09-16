package com.transandina.flotilla.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class FormatosTest {

    @Test
    fun `los kilometros se agrupan de tres en tres con espacio`() {
        assertEquals("492 400", formatearKilometros(492_400.0))
        assertEquals("1 000 000", formatearKilometros(1_000_000.0))
        assertEquals("999", formatearKilometros(999.0))
        assertEquals("1 000", formatearKilometros(1000.0))
        assertEquals("0", formatearKilometros(0.0))
    }

    @Test
    fun `los decimales se redondean`() {
        assertEquals("492 400", formatearKilometros(492_399.6))
        assertEquals("492 399", formatearKilometros(492_399.4))
    }

    @Test
    fun `la unidad va despues del numero`() {
        assertEquals("492 400 km", formatearKilometrosConUnidad(492_400.0))
    }

    @Test
    fun `una fecha iso se muestra en formato dia mes ano`() {
        assertEquals("25/08/2026", formatearFechaIso("2026-08-25"))
        assertEquals("01/01/2026", formatearFechaIso("2026-01-01"))
    }

    @Test
    fun `una fecha que no se entiende se muestra tal cual`() {
        assertEquals("ayer", formatearFechaIso("ayer"))
        assertEquals("", formatearFechaIso(null))
        assertEquals("", formatearFechaIso(""))
    }

    @Test
    fun `hacia supabase la fecha va en iso`() {
        assertEquals("2026-08-25", aFechaIso(LocalDate.of(2026, 8, 25)))
        assertEquals("25/08/2026", formatearFecha(LocalDate.of(2026, 8, 25)))
    }

    @Test
    fun `parsear devuelve null cuando la fecha no sirve`() {
        assertEquals(LocalDate.of(2026, 8, 25), parsearFechaIso("2026-08-25"))
        assertNull(parsearFechaIso("25/08/2026"))
        assertNull(parsearFechaIso(null))
    }
}
