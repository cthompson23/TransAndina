package com.transandina.flotilla.domain

import com.transandina.flotilla.data.model.FrecuenciaMantenimiento
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.TipoMantenimiento
import com.transandina.flotilla.data.model.Vehiculo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class EstadoMantenimientoTest {

    private val hoy = LocalDate.of(2026, 9, 17)

    private val aceite = FrecuenciaMantenimiento(
        id = "f1",
        tipoVehiculo = "liviano",
        categoria = "Cambio de aceite",
        kmFrecuencia = 5_000.0,
        diasFrecuencia = 180
    )

    private val frenosPesado = FrecuenciaMantenimiento(
        id = "f2",
        tipoVehiculo = "pesado",
        categoria = "Frenos",
        kmFrecuencia = 15_000.0,
        diasFrecuencia = 180
    )

    private fun mantenimiento(
        categoria: String = "Cambio de aceite",
        fecha: LocalDate,
        km: Double,
        vehiculoId: String = "v1"
    ) = Mantenimiento(
        id = "$categoria-$fecha",
        vehiculoId = vehiculoId,
        registradoPor = "u1",
        tipo = TipoMantenimiento.preventivo,
        categoria = categoria,
        fecha = fecha.toString(),
        km = km
    )

    private fun vehiculo(
        kmActual: Double,
        fechaMarchamo: String? = null
    ) = Vehiculo(
        id = "v1",
        placa = "SCD-3421",
        marca = "Nissan",
        modelo = "Frontier",
        anio = 2021,
        tipo = "liviano",
        kmActual = kmActual,
        fechaMarchamo = fechaMarchamo
    )

    @Test
    fun `sin mantenimientos registrados no hay proximo que calcular`() {
        val resultado = calcularProximosMantenimientos(
            "liviano", 10_000.0, emptyList(), listOf(aceite), hoy
        )
        assertTrue(resultado.isEmpty())
    }

    @Test
    fun `lejos del limite de km y de fecha esta al dia`() {
        val resultado = calcularProximosMantenimientos(
            "liviano",
            kmActual = 11_000.0,
            mantenimientosDelVehiculo = listOf(mantenimiento(fecha = hoy.minusDays(30), km = 10_000.0)),
            frecuencias = listOf(aceite),
            hoy = hoy
        ).single()

        assertEquals(EstadoMantenimiento.AL_DIA, resultado.estado)
        assertEquals(15_000.0, resultado.kmObjetivo!!, 0.0)
        assertEquals(4_000.0, resultado.kmRestantes!!, 0.0)
        assertEquals(150L, resultado.diasRestantes)
    }

    @Test
    fun `faltando mil km o menos esta proximo`() {
        val resultado = calcularProximosMantenimientos(
            "liviano",
            kmActual = 14_200.0,
            mantenimientosDelVehiculo = listOf(mantenimiento(fecha = hoy.minusDays(30), km = 10_000.0)),
            frecuencias = listOf(aceite),
            hoy = hoy
        ).single()

        assertEquals(EstadoMantenimiento.PROXIMO, resultado.estado)
    }

    @Test
    fun `al pasar el km objetivo esta atrasado`() {
        val resultado = calcularProximosMantenimientos(
            "liviano",
            kmActual = 15_000.0,
            mantenimientosDelVehiculo = listOf(mantenimiento(fecha = hoy.minusDays(30), km = 10_000.0)),
            frecuencias = listOf(aceite),
            hoy = hoy
        ).single()

        assertEquals(EstadoMantenimiento.ATRASADO, resultado.estado)
    }

    @Test
    fun `al pasar la fecha objetivo esta atrasado aunque sobren km`() {
        val resultado = calcularProximosMantenimientos(
            "liviano",
            kmActual = 10_500.0,
            mantenimientosDelVehiculo = listOf(mantenimiento(fecha = hoy.minusDays(181), km = 10_000.0)),
            frecuencias = listOf(aceite),
            hoy = hoy
        ).single()

        assertEquals(EstadoMantenimiento.ATRASADO, resultado.estado)
        assertEquals(-1L, resultado.diasRestantes)
    }

    @Test
    fun `cuenta desde el mantenimiento mas reciente de la categoria`() {
        val resultado = calcularProximosMantenimientos(
            "liviano",
            kmActual = 16_000.0,
            mantenimientosDelVehiculo = listOf(
                mantenimiento(fecha = hoy.minusDays(200), km = 5_000.0),
                mantenimiento(fecha = hoy.minusDays(10), km = 15_500.0)
            ),
            frecuencias = listOf(aceite),
            hoy = hoy
        ).single()

        assertEquals(EstadoMantenimiento.AL_DIA, resultado.estado)
        assertEquals(20_500.0, resultado.kmObjetivo!!, 0.0)
    }

    @Test
    fun `ignora las frecuencias de otro tipo de vehiculo`() {
        val resultado = calcularProximosMantenimientos(
            "liviano",
            kmActual = 100_000.0,
            mantenimientosDelVehiculo = listOf(mantenimiento(categoria = "Frenos", fecha = hoy.minusDays(400), km = 0.0)),
            frecuencias = listOf(frenosPesado),
            hoy = hoy
        )
        assertTrue(resultado.isEmpty())
    }

    @Test
    fun `el estado general toma el peor entre documentos y mantenimiento`() {
        val resumen = resumirVehiculo(
            vehiculo = vehiculo(kmActual = 11_000.0, fechaMarchamo = hoy.minusDays(1).toString()),
            nombreConductor = "Carlos",
            mantenimientosDelVehiculo = listOf(mantenimiento(fecha = hoy.minusDays(30), km = 10_000.0)),
            frecuencias = listOf(aceite),
            hoy = hoy
        )

        assertEquals(EstadoMantenimiento.AL_DIA, resumen.estadoMantenimiento)
        assertTrue(resumen.tieneDocumentosPendientes)
        assertEquals(EstadoMantenimiento.ATRASADO, resumen.estadoGeneral)
    }

    @Test
    fun `un vehiculo sin fechas ni mantenimientos esta al dia`() {
        val resumen = resumirVehiculo(
            vehiculo = vehiculo(kmActual = 0.0),
            nombreConductor = null,
            mantenimientosDelVehiculo = emptyList(),
            frecuencias = listOf(aceite),
            hoy = hoy
        )

        assertFalse(resumen.tieneDocumentosPendientes)
        assertEquals(EstadoMantenimiento.AL_DIA, resumen.estadoGeneral)
        assertEquals(null, resumen.ultimoMantenimiento)
    }
}
