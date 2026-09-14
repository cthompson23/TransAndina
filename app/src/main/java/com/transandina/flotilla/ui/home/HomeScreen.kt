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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.ui.vehiculo.EstadoDocumento
import com.transandina.flotilla.ui.vehiculo.calcularEstadoDocumento

// Misma paleta que LoginScreen / PerfilScreen / MainActivity — pendiente
// moverla a un Color.kt compartido (ui/theme/Color.kt).
private val PageBackground = Color(0xFFD9D9D9)
private val NavyTopBar = Color(0xFF13263F)
private val OrangeAccent = Color(0xFFBB6B2E)
private val CardBackground = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF13263F)
private val MutedText = Color(0xFF8A8A8A)
private val SubtleTextColor = Color(0xFF5A6472)

/**
 * Punto de entrada único de la pestaña "Inicio". El contenido real
 * depende del rol: cada uno necesita ver algo distinto apenas entra
 * a la app, así que despachamos aquí en vez de tener 3 rutas separadas.
 */
@Composable
fun HomeScreen(rol: RolUsuario) {
    Column(modifier = Modifier.fillMaxSize()) {
        TransAndinaTopBar()
        Box(modifier = Modifier.weight(1f)) {
            when (rol) {
                RolUsuario.conductor -> HomeConductorContent()
                RolUsuario.mecanico -> HomeMecanicoContent()
                RolUsuario.encargado -> HomeEncargadoContent()
            }
        }
    }
}

/**
 * Encabezado fijo de marca, igual en todas las pestañas de Inicio
 * (el mismo navy que la barra inferior, para "encerrar" el contenido
 * gris entre las dos barras).
 */
@Composable
private fun TransAndinaTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyTopBar)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "TransAndina",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
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
            .background(PageBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Mi vehículo",
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                color = OrangeAccent
            )
            Spacer(modifier = Modifier.height(20.dp))

            when {
                uiState.cargando && uiState.vehiculo == null -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrangeAccent)
                    }
                }
                uiState.vehiculo == null -> {
                    Text(
                        text = uiState.error ?: "No tienes un vehículo asignado todavía",
                        color = TextPrimary
                    )
                }
                else -> {
                    val vehiculo = uiState.vehiculo!!
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBackground, RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${vehiculo.marca} ${vehiculo.modelo}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                EstadoBadge(estado = estadoGeneral)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Placa: ${vehiculo.placa}", color = MutedText)
                            Text(text = "Año: ${vehiculo.anio} · Tipo: ${vehiculo.tipo}", color = MutedText)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Kilometraje actual",
                                fontSize = 13.sp,
                                color = MutedText
                            )
                            Text(
                                text = "${vehiculo.kmActual} km",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangeAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ve a la pestaña Vehículo para ver el detalle de tus documentos.",
                        fontSize = 13.sp,
                        color = SubtleTextColor
                    )
                }
            }
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
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    mensaje: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(
                text = titulo,
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                color = OrangeAccent
            )
            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = mensaje,
                    color = SubtleTextColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EstadoBadge(estado: EstadoDocumento) {
    val (texto, color) = when (estado) {
        EstadoDocumento.AL_DIA -> "Al día" to Color(0xFF2E7D32)
        EstadoDocumento.PROXIMO -> "Próximo" to Color(0xFFF9A825)
        EstadoDocumento.VENCIDO -> "Atrasado" to Color(0xFFC62828)
        EstadoDocumento.SIN_DATO -> "Sin datos" to Color.Gray
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = texto, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}