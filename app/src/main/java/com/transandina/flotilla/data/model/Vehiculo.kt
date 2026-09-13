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
    @SerialName("km_actual") val kmActual: Double,
    @SerialName("conductor_id") val conductorId: String? = null,
    val activo: Boolean = true
)
