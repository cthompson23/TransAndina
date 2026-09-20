package com.transandina.flotilla.ui.flotilla

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.TIPOS_VEHICULO
import com.transandina.flotilla.ui.components.BotonDestructivo
import com.transandina.flotilla.ui.components.BotonSecundario
import com.transandina.flotilla.ui.components.CampoFecha
import com.transandina.flotilla.ui.components.CampoSeleccion
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.components.ChipEstado
import com.transandina.flotilla.ui.components.DialogoConfirmacion
import com.transandina.flotilla.ui.components.FilaBotonesFormulario
import com.transandina.flotilla.ui.components.NivelEstado
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme
import java.time.LocalDate

/**
 * Registrar vehículo (Figma `49:118`) y, con [vehiculoId], editarlo. Además de
 * los datos del Figma pide las fechas de vencimiento de los documentos,
 * porque es el encargado quien las carga (docs/ADAPTACION_MOVIL.md §7).
 */
@Composable
fun VehiculoFormularioScreen(
    vehiculoId: String? = null,
    viewModel: VehiculoFormularioViewModel = viewModel(),
    onAtras: () -> Unit = {},
    onGuardado: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vehiculoId) {
        viewModel.iniciar(vehiculoId)
    }

    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) onGuardado()
    }

    ContenidoVehiculoFormulario(
        uiState = uiState,
        acciones = AccionesVehiculoFormulario(
            onPlaca = viewModel::onPlacaChange,
            onTipo = viewModel::onTipoChange,
            onAnio = viewModel::onAnioChange,
            onMarca = viewModel::onMarcaChange,
            onModelo = viewModel::onModeloChange,
            onCapacidad = viewModel::onCapacidadChange,
            onKmInicial = viewModel::onKmInicialChange,
            onMarchamo = viewModel::onFechaMarchamoChange,
            onRevision = viewModel::onFechaRevisionChange,
            onSeguro = viewModel::onFechaSeguroChange,
            onPermiso = viewModel::onFechaPermisoChange,
            onConductor = viewModel::onConductorChange,
            onMecanico = viewModel::onMecanicoChange,
            onGuardar = viewModel::guardar,
            onCambiarActivo = viewModel::cambiarActivo,
            onCancelar = onAtras
        )
    )
}

/** Agrupa los callbacks para no pasar doce parámetros sueltos. */
private data class AccionesVehiculoFormulario(
    val onPlaca: (String) -> Unit = {},
    val onTipo: (String) -> Unit = {},
    val onAnio: (String) -> Unit = {},
    val onMarca: (String) -> Unit = {},
    val onModelo: (String) -> Unit = {},
    val onCapacidad: (String) -> Unit = {},
    val onKmInicial: (String) -> Unit = {},
    val onMarchamo: (LocalDate) -> Unit = {},
    val onRevision: (LocalDate) -> Unit = {},
    val onSeguro: (LocalDate) -> Unit = {},
    val onPermiso: (LocalDate) -> Unit = {},
    val onConductor: (String?) -> Unit = {},
    val onMecanico: (String?) -> Unit = {},
    val onGuardar: () -> Unit = {},
    val onCambiarActivo: (Boolean) -> Unit = {},
    val onCancelar: () -> Unit = {}
)

@Composable
private fun etiquetaTipoVehiculo(tipo: String): String = when (tipo) {
    "liviano" -> stringResource(R.string.tipo_vehiculo_liviano)
    "pesado" -> stringResource(R.string.tipo_vehiculo_pesado)
    "especial" -> stringResource(R.string.tipo_vehiculo_especial)
    else -> tipo
}

@Composable
private fun ContenidoVehiculoFormulario(
    uiState: VehiculoFormularioUiState,
    acciones: AccionesVehiculoFormulario
) {
    val editable = !uiState.guardando && !uiState.cargando
    val etiquetasTipo = TIPOS_VEHICULO.map { etiquetaTipoVehiculo(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = if (uiState.esEdicion) {
                stringResource(R.string.vehiculo_form_titulo_editar)
            } else {
                stringResource(R.string.vehiculo_form_titulo_nuevo)
            },
            onAtras = acciones.onCancelar
        )

        if (uiState.cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoTexto(
                etiqueta = stringResource(R.string.vehiculo_placa),
                valor = uiState.placa,
                onValorCambia = acciones.onPlaca,
                marcadorDePosicion = stringResource(R.string.vehiculo_form_placa_marcador),
                forma = FormaPildora,
                habilitado = editable
            )
            CampoSeleccion(
                etiqueta = stringResource(R.string.vehiculo_tipo),
                seleccion = uiState.tipo?.let { etiquetaTipoVehiculo(it) },
                opciones = etiquetasTipo,
                onSeleccionar = { etiqueta ->
                    acciones.onTipo(TIPOS_VEHICULO[etiquetasTipo.indexOf(etiqueta)])
                },
                marcadorDePosicion = stringResource(R.string.vehiculo_form_tipo_marcador),
                forma = FormaPildora,
                habilitado = editable
            )
            CampoTexto(
                etiqueta = stringResource(R.string.vehiculo_anio),
                valor = uiState.anio,
                onValorCambia = acciones.onAnio,
                marcadorDePosicion = stringResource(R.string.vehiculo_form_anio_marcador),
                forma = FormaPildora,
                tipoTeclado = KeyboardType.Number,
                habilitado = editable
            )
            CampoTexto(
                etiqueta = stringResource(R.string.vehiculo_marca),
                valor = uiState.marca,
                onValorCambia = acciones.onMarca,
                marcadorDePosicion = stringResource(R.string.vehiculo_form_marca_marcador),
                forma = FormaPildora,
                habilitado = editable
            )
            CampoTexto(
                etiqueta = stringResource(R.string.vehiculo_modelo),
                valor = uiState.modelo,
                onValorCambia = acciones.onModelo,
                marcadorDePosicion = stringResource(R.string.vehiculo_form_modelo_marcador),
                forma = FormaPildora,
                habilitado = editable
            )
            CampoTexto(
                etiqueta = stringResource(R.string.vehiculo_form_capacidad),
                valor = uiState.capacidad,
                onValorCambia = acciones.onCapacidad,
                marcadorDePosicion = stringResource(R.string.vehiculo_form_capacidad_marcador),
                forma = FormaPildora,
                tipoTeclado = KeyboardType.Decimal,
                habilitado = editable
            )
            // Al editar no se pide: el kilometraje solo se mueve registrando
            // lecturas, que es lo que valida el trigger de la base.
            if (!uiState.esEdicion) {
                CampoTexto(
                    etiqueta = stringResource(R.string.kilometraje_actual),
                    valor = uiState.kmInicial,
                    onValorCambia = acciones.onKmInicial,
                    marcadorDePosicion = stringResource(R.string.vehiculo_form_km_marcador),
                    forma = FormaPildora,
                    tipoTeclado = KeyboardType.Number,
                    habilitado = editable
                )
                Text(
                    text = stringResource(R.string.vehiculo_form_km_ayuda),
                    style = MaterialTheme.typography.bodySmall,
                    color = TransAndinaTheme.colores.textoSecundario
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.vehiculo_form_asignacion),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.vehiculo_form_asignacion_ayuda),
                style = MaterialTheme.typography.bodySmall,
                color = TransAndinaTheme.colores.textoSecundario
            )

            val sinConductor = stringResource(R.string.sin_conductor)
            val noDisponible = stringResource(R.string.detalle_conductor_no_disponible)
            CampoSeleccion(
                etiqueta = stringResource(R.string.detalle_conductor_asignado),
                seleccion = uiState.nombreConductor
                    ?: if (uiState.conductorId != null) noDisponible else sinConductor,
                opciones = listOf(sinConductor) + uiState.conductores.map { it.nombreCompleto },
                onSeleccionar = { nombre ->
                    acciones.onConductor(uiState.conductores.find { it.nombreCompleto == nombre }?.id)
                },
                marcadorDePosicion = sinConductor,
                forma = FormaPildora,
                habilitado = editable
            )

            val sinMecanico = stringResource(R.string.sin_mecanico)
            CampoSeleccion(
                etiqueta = stringResource(R.string.detalle_mecanico_asignado),
                seleccion = uiState.nombreMecanico
                    ?: if (uiState.mecanicoId != null) noDisponible else sinMecanico,
                opciones = listOf(sinMecanico) + uiState.mecanicos.map { it.nombreCompleto },
                onSeleccionar = { nombre ->
                    acciones.onMecanico(uiState.mecanicos.find { it.nombreCompleto == nombre }?.id)
                },
                marcadorDePosicion = sinMecanico,
                forma = FormaPildora,
                habilitado = editable
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.vehiculo_form_documentos),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.vehiculo_form_documentos_ayuda),
                style = MaterialTheme.typography.bodySmall,
                color = TransAndinaTheme.colores.textoSecundario
            )

            CampoFecha(
                etiqueta = stringResource(R.string.documentos_marchamo),
                fecha = uiState.fechaMarchamo,
                onFechaCambia = acciones.onMarchamo,
                forma = FormaPildora,
                habilitado = editable
            )
            CampoFecha(
                etiqueta = stringResource(R.string.documentos_revision),
                fecha = uiState.fechaRevisionTecnica,
                onFechaCambia = acciones.onRevision,
                forma = FormaPildora,
                habilitado = editable
            )
            CampoFecha(
                etiqueta = stringResource(R.string.documentos_seguro),
                fecha = uiState.fechaSeguro,
                onFechaCambia = acciones.onSeguro,
                forma = FormaPildora,
                habilitado = editable
            )
            CampoFecha(
                etiqueta = stringResource(R.string.documentos_permiso_carga),
                fecha = uiState.fechaPermisoCarga,
                onFechaCambia = acciones.onPermiso,
                forma = FormaPildora,
                habilitado = editable
            )

            uiState.error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FilaBotonesFormulario(
                textoAccion = if (uiState.esEdicion) {
                    stringResource(R.string.guardar)
                } else {
                    stringResource(R.string.registrar)
                },
                onAccion = acciones.onGuardar,
                onCancelar = acciones.onCancelar,
                textoCancelar = stringResource(R.string.cancelar),
                cargando = uiState.guardando
            )

            if (uiState.esEdicion) {
                Spacer(modifier = Modifier.height(4.dp))
                SeccionBaja(
                    uiState = uiState,
                    habilitado = editable,
                    onCambiarActivo = acciones.onCambiarActivo
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Baja y alta del vehículo. Darlo de baja lo saca de la flotilla activa y de
 * las alertas, pero conserva su historial; por eso no se borra nada.
 */
@Composable
private fun SeccionBaja(
    uiState: VehiculoFormularioUiState,
    habilitado: Boolean,
    onCambiarActivo: (Boolean) -> Unit
) {
    var pedirConfirmacion by rememberSaveable { mutableStateOf(false) }

    TarjetaTransAndina {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.vehiculo_form_estado),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            ChipEstado(
                texto = if (uiState.activo) {
                    stringResource(R.string.vehiculo_form_activo)
                } else {
                    stringResource(R.string.flotilla_inactivo)
                },
                nivel = if (uiState.activo) NivelEstado.OK else NivelEstado.NEUTRO
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (uiState.activo) {
                stringResource(R.string.vehiculo_form_baja_ayuda)
            } else {
                stringResource(R.string.vehiculo_form_alta_ayuda)
            },
            style = MaterialTheme.typography.bodySmall,
            color = TransAndinaTheme.colores.textoSecundario
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.activo) {
            BotonDestructivo(
                texto = stringResource(R.string.vehiculo_form_dar_baja),
                onClick = { pedirConfirmacion = true },
                modifier = Modifier.fillMaxWidth(),
                habilitado = habilitado
            )
        } else {
            BotonSecundario(
                texto = stringResource(R.string.vehiculo_form_reactivar),
                onClick = { onCambiarActivo(true) },
                modifier = Modifier.fillMaxWidth(),
                habilitado = habilitado
            )
        }
    }

    if (pedirConfirmacion) {
        DialogoConfirmacion(
            titulo = stringResource(R.string.vehiculo_form_baja_dialogo_titulo),
            mensaje = if (uiState.tieneConductor) {
                stringResource(R.string.vehiculo_form_baja_dialogo_con_conductor, uiState.placa)
            } else {
                stringResource(R.string.vehiculo_form_baja_dialogo, uiState.placa)
            },
            textoConfirmar = stringResource(R.string.vehiculo_form_dar_baja),
            destructiva = true,
            onConfirmar = {
                pedirConfirmacion = false
                onCambiarActivo(false)
            },
            onCancelar = { pedirConfirmacion = false }
        )
    }
}

@Preview(name = "Registrar vehículo", showBackground = true, heightDp = 1100)
@Composable
private fun VehiculoFormularioNuevoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoVehiculoFormulario(
            uiState = VehiculoFormularioUiState(),
            acciones = AccionesVehiculoFormulario()
        )
    }
}

@Preview(name = "Editar vehículo con error", showBackground = true, heightDp = 1100)
@Composable
private fun VehiculoFormularioEdicionPreview() {
    TransAndinaFlotillaTheme {
        ContenidoVehiculoFormulario(
            uiState = VehiculoFormularioUiState(
                vehiculoId = "1",
                placa = "SCD-3421",
                tipo = "liviano",
                anio = "2021",
                marca = "Nissan",
                modelo = "Frontier",
                capacidad = "1.1",
                fechaMarchamo = LocalDate.of(2026, 12, 31),
                error = "Ya existe un vehículo con esa placa"
            ),
            acciones = AccionesVehiculoFormulario()
        )
    }
}
