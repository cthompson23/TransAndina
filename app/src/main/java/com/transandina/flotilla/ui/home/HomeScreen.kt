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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.domain.EstadoDocumento
import com.transandina.flotilla.domain.calcularEstadoDocumento
import com.transandina.flotilla.domain.formatearKilometrosConUnidad
import com.transandina.flotilla.ui.components.ChipEstado
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.NivelEstado
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

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
        TransAndinaTopBar(titulo = stringResource(R.string.marca))
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
    ContenidoConductor(uiState = uiState)
}

@Composable
private fun ContenidoConductor(uiState: HomeUiState) {
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
                        mensaje = uiState.error ?: stringResource(R.string.inicio_sin_vehiculo)
                    )
                }
                else -> {
                    TarjetaVehiculoAsignado(vehiculo = uiState.vehiculo!!)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.inicio_ver_documentos),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TransAndinaTheme.colores.textoSecundario
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta del vehículo asignado (Figma `28:407`): etiqueta, placa destacada,
 * una línea de detalle y el chip de estado a la derecha.
 */
@Composable
private fun TarjetaVehiculoAsignado(vehiculo: Vehiculo) {
    val estado = estadoGeneralDocumentos(vehiculo)

    TarjetaTransAndina {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.inicio_mi_vehiculo),
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
                    text = "${vehiculo.marca} ${vehiculo.modelo} · ${formatearKilometrosConUnidad(vehiculo.kmActual)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TransAndinaTheme.colores.textoTerciario
                )
            }
            // El chip solo aparece cuando hay fechas con las que calcular el
            // estado. Sin ellas no se muestra nada, en vez de inventar un
            // "Al día" que no sabemos si es cierto.
            // TODO(backend): las alertas reales deberían venir de una vista en
            //  Postgres que cruce documentos, kilometraje y frecuencias de
            //  mantenimiento (docs/ADAPTACION_MOVIL.md §8).
            when (estado) {
                EstadoDocumento.AL_DIA -> ChipEstado(
                    texto = stringResource(R.string.estado_al_dia),
                    nivel = NivelEstado.OK
                )
                EstadoDocumento.PROXIMO -> ChipEstado(
                    texto = stringResource(R.string.estado_proximo),
                    nivel = NivelEstado.AVISO
                )
                EstadoDocumento.VENCIDO -> ChipEstado(
                    texto = stringResource(R.string.estado_atrasado),
                    nivel = NivelEstado.CRITICO
                )
                EstadoDocumento.SIN_DATO -> Unit
            }
        }
    }
}

/**
 * El peor estado de los tres documentos. Devuelve `SIN_DATO` si ninguno tiene
 * fecha registrada, para que la tarjeta no muestre chip.
 */
private fun estadoGeneralDocumentos(vehiculo: Vehiculo): EstadoDocumento {
    val estados = listOf(
        calcularEstadoDocumento(vehiculo.fechaMarchamo),
        calcularEstadoDocumento(vehiculo.fechaRevisionTecnica),
        calcularEstadoDocumento(vehiculo.fechaSeguro)
    ).filterNot { it == EstadoDocumento.SIN_DATO }

    return when {
        estados.isEmpty() -> EstadoDocumento.SIN_DATO
        estados.any { it == EstadoDocumento.VENCIDO } -> EstadoDocumento.VENCIDO
        estados.any { it == EstadoDocumento.PROXIMO } -> EstadoDocumento.PROXIMO
        else -> EstadoDocumento.AL_DIA
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
        titulo = stringResource(R.string.inicio_mecanico_titulo),
        mensaje = stringResource(R.string.inicio_mecanico_mensaje)
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
        titulo = stringResource(R.string.inicio_encargado_titulo),
        mensaje = stringResource(R.string.inicio_encargado_mensaje)
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

private val vehiculoDeMuestra = Vehiculo(
    id = "1",
    placa = "SCD-3421",
    marca = "Nissan",
    modelo = "Frontier",
    anio = 2021,
    tipo = "Liviano",
    capacidad = 1.1,
    kmActual = 492_400.0
)

@Preview(name = "Inicio con documentos", showBackground = true, heightDp = 600)
@Composable
private fun HomeConDocumentosPreview() {
    TransAndinaFlotillaTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TransAndinaTopBar(titulo = stringResource(R.string.marca))
            ContenidoConductor(
                uiState = HomeUiState(
                    vehiculo = vehiculoDeMuestra.copy(fechaMarchamo = "2020-01-01")
                )
            )
        }
    }
}

@Preview(name = "Inicio sin fechas (sin chip)", showBackground = true, heightDp = 600)
@Composable
private fun HomeSinFechasPreview() {
    TransAndinaFlotillaTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TransAndinaTopBar(titulo = stringResource(R.string.marca))
            ContenidoConductor(uiState = HomeUiState(vehiculo = vehiculoDeMuestra))
        }
    }
}

@Preview(name = "Inicio cargando", showBackground = true, heightDp = 600)
@Composable
private fun HomeCargandoPreview() {
    TransAndinaFlotillaTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TransAndinaTopBar(titulo = stringResource(R.string.marca))
            ContenidoConductor(uiState = HomeUiState(cargando = true))
        }
    }
}

@Preview(name = "Inicio sin vehículo", showBackground = true, heightDp = 600)
@Composable
private fun HomeSinVehiculoPreview() {
    TransAndinaFlotillaTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TransAndinaTopBar(titulo = stringResource(R.string.marca))
            ContenidoConductor(uiState = HomeUiState())
        }
    }
}

@Preview(name = "Inicio con error", showBackground = true, heightDp = 600)
@Composable
private fun HomeErrorPreview() {
    TransAndinaFlotillaTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TransAndinaTopBar(titulo = stringResource(R.string.marca))
            ContenidoConductor(
                uiState = HomeUiState(error = "No se pudo cargar tu vehículo")
            )
        }
    }
}

@Preview(name = "Inicio mecánico", showBackground = true, heightDp = 600)
@Composable
private fun HomeMecanicoPreview() {
    TransAndinaFlotillaTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TransAndinaTopBar(titulo = stringResource(R.string.marca))
            HomeMecanicoContent()
        }
    }
}
