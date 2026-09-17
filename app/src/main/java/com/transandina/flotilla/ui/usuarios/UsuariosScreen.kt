package com.transandina.flotilla.ui.usuarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.EstadoCuenta
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.ui.components.BotonFlotante
import com.transandina.flotilla.ui.components.CampoBusqueda
import com.transandina.flotilla.ui.components.ChipsFiltro
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme

/**
 * Consulta de usuarios (Figma `102:258`): buscador, chips por rol y la tabla
 * convertida en tarjetas. Tocar una cuenta abre su estado; el botón flotante
 * reemplaza "Registrar administrador" de la barra superior.
 */
@Composable
fun UsuariosScreen(
    viewModel: UsuariosViewModel = viewModel(),
    onAbrirUsuario: (usuarioId: String) -> Unit = {},
    onRegistrarAdministrador: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Se recarga al volver, para mostrar los cambios de estado recién guardados.
    LaunchedEffect(Unit) {
        viewModel.cargar()
    }

    ContenidoUsuarios(
        uiState = uiState,
        onRecargar = viewModel::cargar,
        onBusquedaCambia = viewModel::onBusquedaChange,
        onFiltroCambia = viewModel::onFiltroChange,
        onAbrirUsuario = onAbrirUsuario,
        onRegistrarAdministrador = onRegistrarAdministrador
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoUsuarios(
    uiState: UsuariosUiState,
    onRecargar: () -> Unit,
    onBusquedaCambia: (String) -> Unit,
    onFiltroCambia: (FiltroUsuarios) -> Unit,
    onAbrirUsuario: (String) -> Unit,
    onRegistrarAdministrador: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TransAndinaTopBar(titulo = stringResource(R.string.usuarios_titulo))

            PullToRefreshBox(
                isRefreshing = uiState.cargando && uiState.usuarios.isNotEmpty(),
                onRefresh = onRecargar,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        CampoBusqueda(
                            valor = uiState.busqueda,
                            onValorCambia = onBusquedaCambia,
                            marcadorDePosicion = stringResource(R.string.usuarios_buscar)
                        )
                    }
                    item {
                        ChipsFiltro(
                            opciones = FiltroUsuarios.entries.map { filtro ->
                                "${etiquetaFiltro(filtro)} (${uiState.conteo(filtro)})"
                            },
                            indiceSeleccionado = uiState.filtro.ordinal,
                            onSeleccionar = { onFiltroCambia(FiltroUsuarios.entries[it]) }
                        )
                    }

                    val visibles = uiState.visibles
                    when {
                        uiState.cargando && uiState.usuarios.isEmpty() -> item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        uiState.error != null && uiState.usuarios.isEmpty() -> item {
                            EstadoVacio(mensaje = uiState.error)
                        }

                        visibles.isEmpty() -> item {
                            EstadoVacio(mensaje = stringResource(R.string.usuarios_sin_coincidencias))
                        }

                        else -> items(visibles, key = { it.id }) { usuario ->
                            TarjetaUsuario(
                                usuario = usuario,
                                esUsuarioActual = usuario.id == uiState.idActual,
                                onClick = { onAbrirUsuario(usuario.id) }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }

        BotonFlotante(
            texto = stringResource(R.string.usuarios_registrar_admin),
            icono = Icons.Filled.PersonAdd,
            onClick = onRegistrarAdministrador,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun etiquetaFiltro(filtro: FiltroUsuarios): String = when (filtro) {
    FiltroUsuarios.TODOS -> stringResource(R.string.filtro_todos)
    FiltroUsuarios.CONDUCTORES -> stringResource(R.string.usuarios_filtro_conductores)
    FiltroUsuarios.MECANICOS -> stringResource(R.string.usuarios_filtro_mecanicos)
    FiltroUsuarios.ENCARGADOS -> stringResource(R.string.usuarios_filtro_encargados)
}

@Composable
private fun TarjetaUsuario(
    usuario: Usuario,
    esUsuarioActual: Boolean,
    onClick: () -> Unit
) {
    TarjetaTransAndina(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (esUsuarioActual) {
                    stringResource(R.string.usuarios_tu, usuario.nombreCompleto)
                } else {
                    usuario.nombreCompleto
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            ChipEstadoCuenta(usuario.estado)
        }
        Text(
            text = "${etiquetaRol(usuario.rol)} · ${usuario.cedula}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = usuario.email,
            style = MaterialTheme.typography.bodySmall,
            color = TransAndinaTheme.colores.textoSecundario,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

internal val usuariosDeMuestra = listOf(
    Usuario("1", "Carlos Fernández Quesada", "1-1111-1111", "carlitosfeque@gmail.com", rol = RolUsuario.conductor),
    Usuario("2", "Marco Ureña Rojas", "1-0987-0456", "murena@transandina.cr", rol = RolUsuario.mecanico),
    Usuario("3", "Luis Solano Pérez", "3-0456-0789", "lsolano@transandina.cr", rol = RolUsuario.conductor, estado = EstadoCuenta.suspendido),
    Usuario("4", "Diego Ramírez Vega", "4-0111-0332", "dramirez@transandina.cr", rol = RolUsuario.conductor, estado = EstadoCuenta.desactivado),
    Usuario("5", "Carlos Rojas Méndez", "1-0555-0666", "crojas@transandina.cr", rol = RolUsuario.encargado)
)

@Preview(name = "Usuarios", showBackground = true, heightDp = 800)
@Composable
private fun UsuariosPreview() {
    TransAndinaFlotillaTheme {
        ContenidoUsuarios(
            uiState = UsuariosUiState(usuarios = usuariosDeMuestra, idActual = "5"),
            onRecargar = {},
            onBusquedaCambia = {},
            onFiltroCambia = {},
            onAbrirUsuario = {},
            onRegistrarAdministrador = {}
        )
    }
}
