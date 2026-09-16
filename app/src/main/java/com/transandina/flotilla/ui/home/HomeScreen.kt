package com.transandina.flotilla.ui.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.ui.components.ChipEstado
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.NivelEstado
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme
import com.transandina.flotilla.ui.vehiculo.EstadoDocumento
import com.transandina.flotilla.ui.vehiculo.calcularEstadoDocumento

/**
 * Punto de entrada único de la pestaña "Inicio". El contenido real
 * depende del rol: cada uno necesita ver algo distinto apenas entra
 * a la app, así que despachamos aquí en vez de tener 3 rutas separadas.
 */
@Composable
fun HomeScreen(rol: RolUsuario) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Encabezado fijo de marca, igual en todas las pestañas de Inicio
        // (Figma `1:1225`).
        TransAndinaTopBar(titulo = "TransAndina")
        Box(modifier = Modifier.weight(1f)) {
            when (rol) {
                RolUsuario.conductor -> HomeConductorContent()
                RolUsuario.mecanico -> HomeMecanicoContent()
                RolUsuario.encargado -> HomeEncargadoContent()
            }
        }
    }
}

@Composable
private fun HomeConductorContent(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                uiState.cargando && uiState.vehiculo == null -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                }
                uiState.vehiculo == null -> {
                    EstadoVacio(
                        mensaje = uiState.error ?: "No tienes un vehículo asignado todavía"
                    )
                }
                else -> {
                    TarjetaVehiculoAsignado(vehiculo = uiState.vehiculo!!)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ve a la pestaña Vehículo para ver el detalle de tus documentos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TransAndinaTheme.colores.textoSecundario
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta del vehículo asignado: etiqueta, placa destacada y una línea de
 * detalle, con el chip de estado a la derecha (Figma `28:407`).
 */
@Composable
private fun TarjetaVehiculoAsignado(vehiculo: Vehiculo) {
    val estados = listOf(
        calcularEstadoDocumento(vehiculo.fechaMarchamo),
        calcularEstadoDocumento(vehiculo.fechaRevisionTecnica),
        calcularEstadoDocumento(vehiculo.fechaSeguro)
    )
    val estadoGeneral = when {
        estados.any { it == EstadoDocumento.VENCIDO } -> EstadoDocumento.VENCIDO
        estados.any { it == EstadoDocumento.PROXIMO } -> EstadoDocumento.PROXIMO
        estados.all { it == EstadoDocumento.AL_DIA } -> EstadoDocumento.AL_DIA
        else -> EstadoDocumento.SIN_DATO
    }

    TarjetaTransAndina {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${vehiculo.marca} ${vehiculo.modelo} · ${vehiculo.kmActual} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = TransAndinaTheme.colores.textoTerciario
                )
                Text(
                    text = "Año: ${vehiculo.anio} · Tipo: ${vehiculo.tipo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TransAndinaTheme.colores.textoTerciario
                )
            }
            ChipEstadoDocumento(estado = estadoGeneral)
        }
    }
}

/**
 * Placeholder: el mecánico no tiene "un" vehículo asignado, trabaja
 * sobre cualquiera de la flotilla. Cuando construyamos el módulo de
 * mantenimiento, aquí va la lista de vehículos para elegir sobre cuál
 * registrar un servicio.
 */
@Composable
private fun HomeMecanicoContent() {
    PlaceholderContent(
        icono = Icons.Filled.Build,
        titulo = "Vehículos de la flotilla",
        mensaje = "Próximamente: listado de vehículos para registrar un mantenimiento sobre cualquiera de ellos."
    )
}

/**
 * Placeholder: panel general del encargado de flota (semáforo de todos
 * los vehículos, según el módulo de gestión de flotilla del enunciado).
 */
@Composable
private fun HomeEncargadoContent() {
    PlaceholderContent(
        icono = Icons.Filled.Dashboard,
        titulo = "Panel de flotilla",
        mensaje = "Próximamente: estado general de todos los vehículos con indicadores de semáforo."
    )
}

@Composable
private fun PlaceholderContent(
    icono: ImageVector,
    titulo: String,
    mensaje: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
            EstadoVacio(mensaje = mensaje, icono = icono)
        }
    }
}

/** Traduce el estado de los documentos al chip del sistema de diseño. */
@Composable
private fun ChipEstadoDocumento(estado: EstadoDocumento) {
    val (texto, nivel) = when (estado) {
        EstadoDocumento.AL_DIA -> "Al día" to NivelEstado.OK
        EstadoDocumento.PROXIMO -> "Próximo" to NivelEstado.AVISO
        EstadoDocumento.VENCIDO -> "Atrasado" to NivelEstado.CRITICO
        EstadoDocumento.SIN_DATO -> "Sin datos" to NivelEstado.INFO
    }
    ChipEstado(texto = texto, nivel = nivel)
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun HomeConductorPreview() {
    TransAndinaFlotillaTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TransAndinaTopBar(titulo = "TransAndina")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                TarjetaVehiculoAsignado(
                    vehiculo = Vehiculo(
                        id = "1",
                        placa = "SCD-3421",
                        marca = "Nissan",
                        modelo = "Frontier",
                        anio = 2021,
                        tipo = "Liviano",
                        kmActual = 492_400.0
                    )
                )
            }
        }
    }
}
