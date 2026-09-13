package com.transandina.flotilla.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Payload para insertar un nuevo registro (coincide con la tabla `kilometraje`). */
@Serializable
data class RegistroKilometraje(
    @SerialName("vehiculo_id") val vehiculoId: String,
    @SerialName("registrado_por") val registradoPor: String,
    val fecha: String, // formato ISO "yyyy-MM-dd"
    val km: Double
)

/** Fila leída desde la tabla `kilometraje`, usada para la gráfica histórica. */
@Serializable
data class KilometrajeHistorico(
    val id: String,
    @SerialName("vehiculo_id") val vehiculoId: String,
    val fecha: String,
    val km: Double
)
