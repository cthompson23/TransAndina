package com.transandina.flotilla.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class RolUsuario {
    conductor, mecanico, encargado
}

/** Coincide con el enum `estado_cuenta` de la base (migración 202609171900). */
enum class EstadoCuenta {
    activo, suspendido, desactivado
}

@Serializable
data class Usuario(
    val id: String,
    @SerialName("nombre_completo") val nombreCompleto: String,
    val cedula: String,
    val email: String,
    val telefono: String? = null,
    @SerialName("licencia_conducir") val licenciaConducir: String? = null,
    val rol: RolUsuario,
    val activo: Boolean = true,
    val estado: EstadoCuenta = EstadoCuenta.activo
)
