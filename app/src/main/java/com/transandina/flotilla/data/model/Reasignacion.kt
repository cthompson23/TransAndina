package com.transandina.flotilla.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Fila de la tabla `reasignaciones` (historial de cambios de conductor). */
@Serializable
data class Reasignacion(
    val id: String,
    @SerialName("vehiculo_id") val vehiculoId: String,
    @SerialName("conductor_anterior_id") val conductorAnteriorId: String? = null,
    @SerialName("conductor_nuevo_id") val conductorNuevoId: String? = null,
    @SerialName("fecha_efectiva") val fechaEfectiva: String,
    val motivo: String? = null,
    @SerialName("created_at") val creadaEn: String
)

/**
 * Parámetros del RPC `reasignar_conductor`. Sin valores por defecto: PostgREST
 * busca la función por el nombre de TODOS sus parámetros, así que los null
 * tienen que viajar explícitos.
 */
@Serializable
data class ReasignarConductorParams(
    @SerialName("p_vehiculo_id") val vehiculoId: String,
    @SerialName("p_conductor_nuevo_id") val conductorNuevoId: String?,
    @SerialName("p_fecha_efectiva") val fechaEfectiva: String?,
    @SerialName("p_motivo") val motivo: String?
)
