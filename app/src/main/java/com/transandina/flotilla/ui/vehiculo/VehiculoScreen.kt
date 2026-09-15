package com.transandina.flotilla.ui.vehiculo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.ui.components.ChipEstado
import com.transandina.flotilla.ui.components.DatoEtiquetado
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.NivelEstado
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/** Detalle del vehículo del conductor (Figma `28:418` y `87:135`). */
@Composable
fun VehiculoScreen(
    viewModel: VehiculoViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = "Mi vehículo",
            subtitulo = uiState.vehiculo?.let { "${it.placa} · ${it.marca} ${it.modelo}" },
            onAtras = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when {
                uiState.cargando && uiState.vehiculo == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                uiState.vehiculo == null -> {
                    TarjetaTransAndina {
                        EstadoVacio(
                            mensaje = uiState.error ?: "No tienes un vehículo asignado"
                        )
                    }
                }

                else -> {
                    val vehiculo = uiState.vehiculo!!

                    TarjetaInformacion(vehiculo = vehiculo)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Documentos legales",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TarjetaTransAndina(relleno = 0.dp) {
                        FilaDocumento("Marchamo", vehiculo.fechaMarchamo)
                        HorizontalDivider(color = MaterialTheme.colorScheme.background)
                        FilaDocumento("Revisión técnica", vehiculo.fechaRevisionTecnica)
                        HorizontalDivider(color = MaterialTheme.colorScheme.background)
                        FilaDocumento("Seguro", vehiculo.fechaSeguro)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaInformacion(vehiculo: Vehiculo) {
    TarjetaTransAndina {
        Text(
            text = "Mi vehículo",
            style = MaterialTheme.typography.bodyMedium,
            color = TransAndinaTheme.colores.textoSecundario
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = vehiculo.placa,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DatoEtiquetado(etiqueta = "Marca", valor = vehiculo.marca)
            DatoEtiquetado(etiqueta = "Modelo", valor = vehiculo.modelo)
            DatoEtiquetado(etiqueta = "Año", valor = vehiculo.anio.toString())
            DatoEtiquetado(etiqueta = "Tipo", valor = vehiculo.tipo)
            vehiculo.capacidad?.let {
                DatoEtiquetado(etiqueta = "Capacidad", valor = it.toString())
            }
            DatoEtiquetado(
                etiqueta = "Kilometraje actual",
                valor = "${vehiculo.kmActual} km"
            )
        }
    }
}

@Composable
private fun FilaDocumento(
    nombre: String,
    fechaIso: String?
) {
    val estado = calcularEstadoDocumento(fechaIso)
    val (texto, nivel) = when (estado) {
        EstadoDocumento.AL_DIA -> "Al día" to NivelEstado.OK
        EstadoDocumento.PROXIMO -> "Próximo" to NivelEstado.AVISO
        EstadoDocumento.VENCIDO -> "Vencido" to NivelEstado.CRITICO
        EstadoDocumento.SIN_DATO -> "Sin fecha" to NivelEstado.INFO
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            fechaIso?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = TransAndinaTheme.colores.textoSecundario
                )
            }
        }
        ChipEstado(texto = texto, nivel = nivel)
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun VehiculoScreenPreview() {
    TransAndinaFlotillaTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TransAndinaTopBar(
                titulo = "Mi vehículo",
                subtitulo = "SCD-3421 · Nissan Frontier",
                onAtras = {}
            )
            Column(modifier = Modifier.padding(16.dp)) {
                TarjetaInformacion(
                    vehiculo = Vehiculo(
                        id = "1",
                        placa = "SCD-3421",
                        marca = "Nissan",
                        modelo = "Frontier",
                        anio = 2021,
                        tipo = "Liviano",
                        capacidad = 1.1,
                        kmActual = 492_400.0,
                        fechaMarchamo = "2026-12-31"
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                TarjetaTransAndina(relleno = 0.dp) {
                    FilaDocumento("Marchamo", "2026-12-31")
                    HorizontalDivider(color = MaterialTheme.colorScheme.background)
                    FilaDocumento("Revisión técnica", "2026-09-15")
                    HorizontalDivider(color = MaterialTheme.colorScheme.background)
                    FilaDocumento("Seguro", null)
                }
            }
        }
    }
}
