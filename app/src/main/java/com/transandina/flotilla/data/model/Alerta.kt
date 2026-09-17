package com.transandina.flotilla.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Coincide con el enum `tipo_alerta` de la base. */
enum class TipoAlerta {
    mantenimiento_proximo,
    documento_vencimiento,
    mantenimiento_confirmado,
    reasignacion,
    gerencia
}

/** Aviso que el encargado le manda a un usuario (política `alertas_insert`). */
@Serializable
data class NuevaAlertaPayload(
    val tipo: TipoAlerta,
    @SerialName("usuario_id") val usuarioId: String,
    val mensaje: String
)
