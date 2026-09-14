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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// Colores de TransAndina
private val AzulTransAndina = Color(0xFF13263F)
private val GrisFondo = Color(0xFFD9D9D9)
private val Blanco = Color(0xFFFFFFFF)
private val TextoPrincipal = Color(0xFF0C2340)
private val TextoSecundario = Color(0xFF8A8A8A)

@Composable
fun VehiculoScreen(
    viewModel: VehiculoViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisFondo)
    ) {

        // =========================
        // HEADER
        // =========================

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(AzulTransAndina),
            contentAlignment = Alignment.Center
        ) {

            // Botón volver
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Blanco
                )
            }

            Text(
                text = "TransAndina",
                color = Blanco,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // =========================
        // CONTENIDO
        // =========================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 18.dp,
                    vertical = 24.dp
                )
        ) {

            when {

                // Cargando
                uiState.cargando && uiState.vehiculo == null -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AzulTransAndina
                        )
                    }
                }

                // Sin vehículo
                uiState.vehiculo == null -> {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Blanco
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        )
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = AzulTransAndina,
                                modifier = Modifier.size(40.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = uiState.error
                                    ?: "No tienes un vehículo asignado",
                                color = TextoPrincipal,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Vehículo encontrado
                else -> {

                    val vehiculo = uiState.vehiculo!!

                    // =========================
                    // TARJETA DEL VEHÍCULO
                    // =========================

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Blanco
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // Icono
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(
                                            color = GrisFondo,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = AzulTransAndina,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.size(12.dp)
                                )

                                Column {
                                    Text(
                                        text = "Mi vehículo",
                                        color = TextoSecundario,
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Spacer(
                                        modifier = Modifier.height(2.dp)
                                    )

                                    Text(
                                        text = "${vehiculo.marca} ${vehiculo.modelo}",
                                        color = TextoPrincipal,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "Placa: ${vehiculo.placa}",
                                        color = TextoSecundario,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier.height(18.dp)
                            )

                            DatoVehiculo(
                                titulo = "Año",
                                valor = vehiculo.anio.toString()
                            )

                            DatoVehiculo(
                                titulo = "Tipo",
                                valor = vehiculo.tipo
                            )

                            vehiculo.capacidad?.let {
                                DatoVehiculo(
                                    titulo = "Capacidad",
                                    valor = it.toString()
                                )
                            }

                            DatoVehiculo(
                                titulo = "Kilometraje actual",
                                valor = "${vehiculo.kmActual} km"
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    // =========================
                    // DOCUMENTOS
                    // =========================

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = AzulTransAndina,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(
                            modifier = Modifier.size(8.dp)
                        )

                        Text(
                            text = "Documentos legales",
                            color = TextoPrincipal,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Blanco
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(
                                horizontal = 18.dp,
                                vertical = 4.dp
                            )
                        ) {

                            FilaDocumento(
                                nombre = "Marchamo",
                                fechaIso = vehiculo.fechaMarchamo
                            )

                            FilaDocumento(
                                nombre = "Revisión técnica",
                                fechaIso = vehiculo.fechaRevisionTecnica
                            )

                            FilaDocumento(
                                nombre = "Seguro",
                                fechaIso = vehiculo.fechaSeguro
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatoVehiculo(
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = titulo,
            color = TextoSecundario,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = valor,
            color = TextoPrincipal,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FilaDocumento(
    nombre: String,
    fechaIso: String?
) {
    val estado = calcularEstadoDocumento(fechaIso)

    val (texto, color) = when (estado) {

        EstadoDocumento.AL_DIA ->
            "Al día" to Color(0xFF4D8B5A)

        EstadoDocumento.PROXIMO ->
            "Próximo" to Color(0xFFC28A45)

        EstadoDocumento.VENCIDO ->
            "Vencido" to Color(0xFFC76969)

        EstadoDocumento.SIN_DATO ->
            "Sin fecha" to TextoSecundario
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = nombre,
                color = TextoPrincipal,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            fechaIso?.let {

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = it,
                    color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Box(
            modifier = Modifier
                .background(
                    color = color.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(
                    horizontal = 9.dp,
                    vertical = 5.dp
                )
        ) {

            Text(
                text = texto,
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}