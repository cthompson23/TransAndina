package com.transandina.flotilla.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InterpretarMontoTest {

    private fun monto(texto: String): Double = interpretarMonto(texto)!!

    @Test
    fun `acepta el simbolo de colones y espacios`() {
        assertEquals(45_000.0, monto("₡ 45 000"), 0.0)
        assertEquals(45_000.0, monto("45000"), 0.0)
    }

    @Test
    fun `la coma es decimal`() {
        assertEquals(45_000.5, monto("45000,50"), 0.0)
        assertEquals(0.0, monto("₡ 0,00"), 0.0)
    }

    @Test
    fun `el punto con tres digitos es separador de miles`() {
        assertEquals(45_000.0, monto("45.000"), 0.0)
        assertEquals(1_250_000.0, monto("1.250.000"), 0.0)
        assertEquals(1_234.5, monto("1.234,5"), 0.0)
    }

    @Test
    fun `el punto con otra cantidad de digitos es decimal`() {
        assertEquals(45.5, monto("45.5"), 0.0)
    }

    @Test
    fun `texto vacio, letras o negativos no son montos`() {
        assertNull(interpretarMonto(""))
        assertNull(interpretarMonto("   "))
        assertNull(interpretarMonto("abc"))
        assertNull(interpretarMonto("-500"))
    }

    @Test
    fun `los kilometros solo toman los digitos`() {
        assertEquals(492_400.0, interpretarKilometros("492 400")!!, 0.0)
        assertEquals(492_400.0, interpretarKilometros("492.400")!!, 0.0)
        assertNull(interpretarKilometros("km"))
    }
}
