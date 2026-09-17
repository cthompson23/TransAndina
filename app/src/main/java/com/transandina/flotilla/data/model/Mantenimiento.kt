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
 * Lo que se inserta al registrar un mantenimiento (Figma `49:161`). Sin
 * valores por defecto para que los null viajen explícitos (el serializador de
 * Supabase omite los campos que tienen su valor por defecto).
 */
@Serializable
data class NuevoMantenimientoPayload(
    @SerialName("vehiculo_id") val vehiculoId: String,
    @SerialName("registrado_por") val registradoPor: String,
    val tipo: TipoMantenimiento,
    val categoria: String,
    val fecha: String,
    val km: Double,
    val responsable: String?,
    val descripcion: String?,
    val costo: Double,
    val taller: String
)

/** Fila de `mantenimiento_fotos` leída de la base. */
@Serializable
data class FotoMantenimiento(
    val id: String,
    @SerialName("mantenimiento_id") val mantenimientoId: String,
    @SerialName("storage_path") val rutaArchivo: String
)

/** Fila de `mantenimiento_fotos`: apunta a un archivo del bucket `mantenimientos`. */
@Serializable
data class NuevaFotoMantenimientoPayload(
    @SerialName("mantenimiento_id") val mantenimientoId: String,
    @SerialName("storage_path") val rutaArchivo: String
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
