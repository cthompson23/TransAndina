package com.transandina.flotilla.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Coincide con el enum `tipo_mantenimiento` de la base. */
enum class TipoMantenimiento {
    preventivo, correctivo
}

/** Fila de la tabla `mantenimientos`. */
@Serializable
data class Mantenimiento(
    val id: String,
    @SerialName("vehiculo_id") val vehiculoId: String,
    @SerialName("registrado_por") val registradoPor: String,
    val tipo: TipoMantenimiento,
    val categoria: String,
    val fecha: String, // formato ISO "yyyy-MM-dd"
    val km: Double,
    val responsable: String? = null,
    val descripcion: String? = null,
    val costo: Double? = null,
    val taller: String? = null
)

/**
 * Fila de `frecuencias_mantenimiento`: cada cuánto toca una categoría según
 * el tipo de vehículo. También sirve de catálogo de categorías.
 */
@Serializable
data class FrecuenciaMantenimiento(
    val id: String,
    @SerialName("tipo_vehiculo") val tipoVehiculo: String,
    val categoria: String,
    @SerialName("km_frecuencia") val kmFrecuencia: Double? = null,
    @SerialName("dias_frecuencia") val diasFrecuencia: Int? = null
)
