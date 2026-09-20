package com.transandina.flotilla.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vehiculo(
    val id: String,
    val placa: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val tipo: String,
    val capacidad: Double? = null,
    @SerialName("km_actual") val kmActual: Double,
    @SerialName("fecha_marchamo") val fechaMarchamo: String? = null,
    @SerialName("fecha_revision_tecnica") val fechaRevisionTecnica: String? = null,
    @SerialName("fecha_seguro") val fechaSeguro: String? = null,
    @SerialName("fecha_permiso_carga") val fechaPermisoCarga: String? = null,
    @SerialName("conductor_id") val conductorId: String? = null,
    @SerialName("mecanico_id") val mecanicoId: String? = null,
    val activo: Boolean = true
)

/**
 * Datos que el encargado llena al registrar o editar un vehículo (Figma
 * `49:118`). Sin valores por defecto a propósito: el serializador de
 * Supabase omite los campos que tienen su valor por defecto, y aquí un null
 * tiene que llegar como null para poder borrar una fecha.
 */
@Serializable
data class VehiculoPayload(
    val placa: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val tipo: String,
    val capacidad: Double?,
    @SerialName("fecha_marchamo") val fechaMarchamo: String?,
    @SerialName("fecha_revision_tecnica") val fechaRevisionTecnica: String?,
    @SerialName("fecha_seguro") val fechaSeguro: String?,
    @SerialName("fecha_permiso_carga") val fechaPermisoCarga: String?,
    @SerialName("mecanico_id") val mecanicoId: String?
)

/**
 * Id y nombre de una persona, que es todo lo que devuelven las funciones
 * `mecanicos_disponibles()` y `personas_de_mis_vehiculos()`. Existen porque
 * ni el conductor ni el mecánico pueden leer la tabla `usuarios` completa.
 */
@Serializable
data class PersonaResumen(
    val id: String,
    @SerialName("nombre_completo") val nombreCompleto: String
)

/** Parámetros de la función `asignar_mecanico`. */
@Serializable
data class AsignarMecanicoParams(
    @SerialName("p_vehiculo_id") val vehiculoId: String,
    @SerialName("p_mecanico_id") val mecanicoId: String?
)

/** Valores del enum `tipo_vehiculo`. */
val TIPOS_VEHICULO = listOf("liviano", "pesado", "especial")
