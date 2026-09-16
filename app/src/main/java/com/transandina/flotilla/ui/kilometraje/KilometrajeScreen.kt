package com.transandina.flotilla.ui.kilometraje

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.ui.components.BotonConfirmar
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/** Registro del recorrido del vehículo (Figma `51:174`). */
@Composable
fun KilometrajeScreen(
    viewModel: KilometrajeViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = "Registrar kilometraje",
            subtitulo = uiState.vehiculo?.let { "${it.placa} · ${it.marca} ${it.modelo}" },
            onAtras = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val vehiculo = uiState.vehiculo

            if (vehiculo == null && uiState.cargando) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                return@Column
            }

            if (vehiculo == null) {
                EstadoVacio(mensaje = uiState.error ?: "No tienes un vehículo asignado")
                return@Column
            }

            TarjetaTransAndina {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Kilometraje actual",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TransAndinaTheme.colores.textoSecundario
                    )
                    Text(
                        text = "${vehiculo.kmActual} km",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CampoTexto(
                etiqueta = "Nuevo kilometraje",
                valor = uiState.kmIngresado,
                onValorCambia = viewModel::onKmChange,
                marcadorDePosicion = "Ingrese el kilometraje",
                forma = FormaPildora,
                tipoTeclado = KeyboardType.Number
            )

            uiState.error?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (uiState.exito) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Kilometraje actualizado correctamente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TransAndinaTheme.colores.estadoOk
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            BotonConfirmar(
                texto = "Guardar",
                onClick = viewModel::registrarKilometraje,
                modifier = Modifier.fillMaxWidth(),
                cargando = uiState.cargando
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun KilometrajeScreenPreview() {
    TransAndinaFlotillaTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TransAndinaTopBar(
                titulo = "Registrar kilometraje",
                subtitulo = "SCD-3421 · Nissan Frontier",
                onAtras = {}
            )
            Column(modifier = Modifier.padding(16.dp)) {
                TarjetaTransAndina {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Kilometraje actual",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TransAndinaTheme.colores.textoSecundario
                        )
                        Text(
                            text = "492400.0 km",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                CampoTexto(
                    etiqueta = "Nuevo kilometraje",
                    valor = "",
                    onValorCambia = {},
                    marcadorDePosicion = "Ingrese el kilometraje",
                    forma = FormaPildora,
                    tipoTeclado = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(24.dp))
                BotonConfirmar("Guardar", {}, Modifier.fillMaxWidth())
            }
        }
    }
}
