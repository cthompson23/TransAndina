package com.transandina.flotilla.domain

import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.TipoMantenimiento
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ReportesTest {

    private fun mantenimiento(
        id: String,
        fecha: String,
        vehiculoId: String = "v1",
        tipo: TipoMantenimiento = TipoMantenimiento.preventivo,
        taller: String? = null,
        costo: Double? = null
    ) = Mantenimiento(
        id = id,
        vehiculoId = vehiculoId,
        registradoPor = "u1",
        tipo = tipo,
        categoria = "Cambio de aceite",
        fecha = fecha,
        km = 1_000.0,
        taller = taller,
        costo = costo
    )

    private val registros = listOf(
        mantenimiento("1", "2026-01-10", taller = "Taller Central", costo = 45_000.0),
        mantenimiento("2", "2026-03-05", vehiculoId = "v2", tipo = TipoMantenimiento.correctivo, taller = "taller norte ", costo = 320_000.0),
        mantenimiento("3", "2026-08-25", taller = "Taller Norte"),
        mantenimiento("4", "fecha-mala", costo = 10.0)
    )

    @Test
    fun `sin filtros devuelve todo del mas reciente al mas antiguo`() {
        val ids = filtrarMantenimientos(registros, FiltrosReporte()).map { it.id }
        assertEquals(listOf("4", "3", "2", "1"), ids)
    }

    @Test
    fun `el rango de fechas es inclusivo y descarta fechas ilegibles`() {
        val filtros = FiltrosReporte(
            desde = LocalDate.of(2026, 1, 10),
            hasta = LocalDate.of(2026, 3, 5)
        )
        val ids = filtrarMantenimientos(registros, filtros).map { it.id }
        assertEquals(listOf("2", "1"), ids)
    }

    @Test
    fun `filtra por vehiculo y por tipo`() {
        assertEquals(
            listOf("2"),
            filtrarMantenimientos(registros, FiltrosReporte(vehiculoId = "v2")).map { it.id }
        )
        assertEquals(
            listOf("2"),
            filtrarMantenimientos(registros, FiltrosReporte(tipo = TipoMantenimiento.correctivo)).map { it.id }
        )
    }

    @Test
    fun `el taller se compara sin mayusculas ni espacios`() {
        val ids = filtrarMantenimientos(registros, FiltrosReporte(taller = "Taller Norte")).map { it.id }
        assertEquals(listOf("3", "2"), ids)
    }

    @Test
    fun `el costo total ignora los registros sin costo`() {
        assertEquals(365_010.0, costoTotal(registros), 0.0)
    }

    @Test
    fun `los talleres se listan una sola vez y ordenados`() {
        assertEquals(listOf("Taller Central", "taller norte"), talleresRegistrados(registros))
    }

    @Test
    fun `los colones se agrupan de a tres con espacio`() {
        assertEquals("₡ 4 812 500", formatearColones(4_812_500.0))
        assertEquals("₡ 0", formatearColones(0.0))
    }
}
