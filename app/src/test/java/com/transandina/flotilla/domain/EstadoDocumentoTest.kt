package com.transandina.flotilla.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * La función compara contra la fecha de hoy, así que los casos se construyen
 * relativos a hoy en vez de con fechas fijas.
 */
class EstadoDocumentoTest {

    private fun isoDentroDe(dias: Long): String = LocalDate.now().plusDays(dias).toString()

    @Test
    fun `una fecha de ayer esta vencida`() {
        assertEquals(EstadoDocumento.VENCIDO, calcularEstadoDocumento(isoDentroDe(-1)))
    }

    @Test
    fun `una fecha de hace un ano esta vencida`() {
        assertEquals(EstadoDocumento.VENCIDO, calcularEstadoDocumento(isoDentroDe(-365)))
    }

    @Test
    fun `una fecha dentro de 10 dias esta proxima`() {
        assertEquals(EstadoDocumento.PROXIMO, calcularEstadoDocumento(isoDentroDe(10)))
    }

    @Test
    fun `hoy cuenta como proximo, no como vencido`() {
        assertEquals(EstadoDocumento.PROXIMO, calcularEstadoDocumento(LocalDate.now().toString()))
    }

    @Test
    fun `el dia 15 ya esta al dia`() {
        // El corte es estricto: fecha < hoy + 15 días es "próximo".
        assertEquals(EstadoDocumento.AL_DIA, calcularEstadoDocumento(isoDentroDe(15)))
    }

    @Test
    fun `una fecha lejana esta al dia`() {
        assertEquals(EstadoDocumento.AL_DIA, calcularEstadoDocumento(isoDentroDe(200)))
    }

    @Test
    fun `sin fecha no hay dato`() {
        assertEquals(EstadoDocumento.SIN_DATO, calcularEstadoDocumento(null))
    }

    @Test
    fun `una fecha invalida no revienta y queda sin dato`() {
        assertEquals(EstadoDocumento.SIN_DATO, calcularEstadoDocumento("25/08/2026"))
        assertEquals(EstadoDocumento.SIN_DATO, calcularEstadoDocumento("2026-13-45"))
        assertEquals(EstadoDocumento.SIN_DATO, calcularEstadoDocumento(""))
    }
}
