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
    @SerialName("conductor_id") val conductorId: String? = null,
    val activo: Boolean = true
)
