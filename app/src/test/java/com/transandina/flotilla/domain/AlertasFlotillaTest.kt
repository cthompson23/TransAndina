package com.transandina.flotilla.domain

import com.transandina.flotilla.data.model.FrecuenciaMantenimiento
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.Reasignacion
import com.transandina.flotilla.data.model.TipoMantenimiento
import com.transandina.flotilla.data.model.Vehiculo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AlertasFlotillaTest {

    private val hoy = LocalDate.of(2026, 9, 17)

    private fun vehiculo(
        id: String,
        kmActual: Double = 0.0,
        fechaSeguro: LocalDate? = null,
        fechaPermisoCarga: LocalDate? = null,
        activo: Boolean = true
    ) = Vehiculo(
        id = id,
        placa = "PLA-$id",
        marca = "Nissan",
        modelo = "Frontier",
        anio = 2021,
        tipo = "liviano",
        kmActual = kmActual,
        fechaSeguro = fechaSeguro?.toString(),
        fechaPermisoCarga = fechaPermisoCarga?.toString(),
        activo = activo
    )

    private val aceite = FrecuenciaMantenimiento(
        id = "f1",
        tipoVehiculo = "liviano",
        categoria = "Cambio de aceite",
        kmFrecuencia = 5_000.0,
        diasFrecuencia = null
    )

    private fun alertas(
        vehiculos: List<Vehiculo>,
        mantenimientos: List<Mantenimiento> = emptyList(),
        reasignaciones: List<Reasignacion> = emptyList()
    ) = calcularAlertasFlotilla(
        vehiculos = vehiculos,
        mantenimientos = mantenimientos,
        frecuencias = listOf(aceite),
        reasignaciones = reasignaciones,
        nombresPorUsuario = mapOf("c2" to "Sofía Blanco"),
        hoy = hoy
    )

    @Test
    fun `un documento vencido es critico`() {
        val resultado = alertas(listOf(vehiculo("1", fechaPermisoCarga = hoy.minusDays(3)))).single()

        assertEquals(NivelAlerta.CRITICA, resultado.nivel)
        val motivo = resultado.motivo as MotivoAlerta.DocumentoVencido
        assertEquals(TipoDocumento.PERMISO_CARGA, motivo.documento)
    }

    @Test
    fun `un documento que vence en una semana o menos es critico`() {
        val resultado = alertas(listOf(vehiculo("1", fechaSeguro = hoy.plusDays(7)))).single()

        assertEquals(NivelAlerta.CRITICA, resultado.nivel)
        assertTrue(resultado.motivo is MotivoAlerta.DocumentoPorVencer)
    }

    @Test
    fun `un documento que vence en diez dias es proximo`() {
        val resultado = alertas(listOf(vehiculo("1", fechaSeguro = hoy.plusDays(10)))).single()

        assertEquals(NivelAlerta.PROXIMA, resultado.nivel)
    }

    @Test
    fun `un documento lejano no genera alerta`() {
        assertTrue(alertas(listOf(vehiculo("1", fechaSeguro = hoy.plusDays(90)))).isEmpty())
    }

    @Test
    fun `los vehiculos inactivos no generan alertas`() {
        val inactivo = vehiculo("1", fechaSeguro = hoy.minusDays(1), activo = false)
        assertTrue(alertas(listOf(inactivo)).isEmpty())
    }

    @Test
    fun `un mantenimiento atrasado es critico`() {
        val resultado = alertas(
            vehiculos = listOf(vehiculo("1", kmActual = 16_000.0)),
            mantenimientos = listOf(
                Mantenimiento(
                    id = "m1",
                    vehiculoId = "1",
                    registradoPor = "u1",
                    tipo = TipoMantenimiento.preventivo,
                    categoria = "Cambio de aceite",
                    fecha = hoy.minusDays(60).toString(),
                    km = 10_000.0
                )
            )
        ).single()

        assertEquals(NivelAlerta.CRITICA, resultado.nivel)
        assertTrue(resultado.motivo is MotivoAlerta.MantenimientoAtrasado)
    }

    @Test
    fun `las reasignaciones recientes son informativas y las viejas no aparecen`() {
        fun reasignacion(id: String, fecha: LocalDate) = Reasignacion(
            id = id,
            vehiculoId = "1",
            conductorAnteriorId = "c1",
            conductorNuevoId = "c2",
            fechaEfectiva = fecha.toString(),
            creadaEn = "${fecha}T10:00:00+00:00"
        )

        val resultado = alertas(
            vehiculos = listOf(vehiculo("1")),
            reasignaciones = listOf(
                reasignacion("r1", hoy.minusDays(5)),
                reasignacion("r2", hoy.minusDays(45))
            )
        ).single()

        assertEquals(NivelAlerta.INFORMATIVA, resultado.nivel)
        val motivo = resultado.motivo as MotivoAlerta.ConductorReasignado
        assertEquals("Sofía Blanco", motivo.nombreConductorNuevo)
    }

    @Test
    fun `se ordenan por urgencia y dentro del nivel por fecha`() {
        val resultado = alertas(
            listOf(
                vehiculo("a", fechaSeguro = hoy.plusDays(10)),
                vehiculo("b", fechaSeguro = hoy.minusDays(1)),
                vehiculo("c", fechaSeguro = hoy.minusDays(20))
            )
        )

        assertEquals(listOf("c", "b", "a"), resultado.map { it.vehiculo.id })
        assertEquals(
            listOf(NivelAlerta.CRITICA, NivelAlerta.CRITICA, NivelAlerta.PROXIMA),
            resultado.map { it.nivel }
        )
    }
}
