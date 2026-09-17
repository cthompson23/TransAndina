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

/**
 * Fila de `alertas`: los avisos que sí se guardan (reasignación, mantenimiento
 * registrado y mensajes de la gerencia). Las alertas de documentos y de
 * mantenimiento próximo NO están aquí: se calculan en `domain/`
 * (docs/ADAPTACION_MOVIL.md §8).
 */
@Serializable
data class Alerta(
    val id: String,
    val tipo: TipoAlerta,
    @SerialName("vehiculo_id") val vehiculoId: String? = null,
    @SerialName("usuario_id") val usuarioId: String? = null,
    val mensaje: String,
    val leida: Boolean = false,
    @SerialName("created_at") val creadaEn: String
)

/** Aviso que el encargado le manda a un usuario (política `alertas_insert`). */
@Serializable
data class NuevaAlertaPayload(
    val tipo: TipoAlerta,
    @SerialName("usuario_id") val usuarioId: String,
    val mensaje: String
)
