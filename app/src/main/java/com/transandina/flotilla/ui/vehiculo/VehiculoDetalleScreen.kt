package com.transandina.flotilla.ui.vehiculo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.KilometrajeHistorico
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.TipoMantenimiento
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.domain.EstadoDocumento
import com.transandina.flotilla.domain.EstadoMantenimiento
import com.transandina.flotilla.domain.ProximoMantenimiento
import com.transandina.flotilla.domain.TipoDocumento
import com.transandina.flotilla.domain.calcularEstadoDocumento
import com.transandina.flotilla.domain.fechasDocumentos
import com.transandina.flotilla.domain.formatearFecha
import com.transandina.flotilla.domain.formatearFechaIso
import com.transandina.flotilla.domain.formatearKilometrosConUnidad
import com.transandina.flotilla.domain.parsearFechaIso
import com.transandina.flotilla.ui.components.BotonPrimario
import com.transandina.flotilla.ui.components.BotonSecundario
import com.transandina.flotilla.ui.components.ChipEstado
import com.transandina.flotilla.ui.components.ChipEstadoMantenimiento
import com.transandina.flotilla.ui.components.ChipsFiltro
import com.transandina.flotilla.ui.components.DatoEtiquetado
import com.transandina.flotilla.ui.components.TarjetaMantenimiento
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.GraficoLineaKilometraje
import com.transandina.flotilla.ui.components.NivelEstado
import com.transandina.flotilla.ui.components.PestanasSegmentadas
import com.transandina.flotilla.ui.components.PuntoGrafico
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme
import java.time.format.TextStyle
import java.util.Locale

/** Las cuatro pestañas del detalle, en el orden del Figma. */
enum class PestanaVehiculo { INFORMACION, HISTORIAL, KILOMETRAJE, DOCUMENTOS }

/**
 * Detalle del vehículo con pestañas (Figma `28:418`, `51:125`, `51:217` y
 * `87:135`). Recibe el id por la ruta para que sirva tanto al conductor con su
 * unidad como al encargado con cualquier vehículo de la flotilla.
 *
 * @param esEncargado muestra el conductor asignado y las acciones del
 *   encargado (reasignar, editar datos y documentos), y oculta "Registrar
 *   kilometraje", que solo puede hacer el conductor (Figma `102:178`).
 */
@Composable
fun VehiculoDetalleScreen(
    vehiculoId: String,
    modifier: Modifier = Modifier,
    pestanaInicial: PestanaVehiculo = PestanaVehiculo.INFORMACION,
    viewModel: VehiculoDetalleViewModel = viewModel(),
    esEncargado: Boolean = false,
    onAtras: () -> Unit = {},
    onRegistrarKilometraje: (vehiculoId: String) -> Unit = {},
    onRegistrarMantenimiento: (vehiculoId: String) -> Unit = {},
    onReasignarConductor: (vehiculoId: String) -> Unit = {},
    onEditarVehiculo: (vehiculoId: String) -> Unit = {},
    tokenRecarga: Int = 0
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vehiculoId, tokenRecarga) {
        viewModel.cargar(vehiculoId)
    }

    ContenidoDetalle(
        uiState = uiState,
        pestanaInicial = pestanaInicial,
        modifier = modifier,
        esEncargado = esEncargado,
        onAtras = onAtras,
        onRegistrarKilometraje = onRegistrarKilometraje,
        onRegistrarMantenimiento = onRegistrarMantenimiento,
        onReasignarConductor = onReasignarConductor,
        onEditarVehiculo = onEditarVehiculo
    )
}

@Composable
private fun ContenidoDetalle(
    uiState: VehiculoDetalleUiState,
    pestanaInicial: PestanaVehiculo,
    modifier: Modifier = Modifier,
    esEncargado: Boolean = false,
    onAtras: () -> Unit = {},
    onRegistrarKilometraje: (String) -> Unit = {},
    onRegistrarMantenimiento: (String) -> Unit = {},
    onReasignarConductor: (String) -> Unit = {},
    onEditarVehiculo: (String) -> Unit = {}
) {
    var pestana by rememberSaveable { mutableStateOf(pestanaInicial) }
    val vehiculo = uiState.vehiculo

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = when (pestana) {
                PestanaVehiculo.INFORMACION -> if (esEncargado) {
                    stringResource(R.string.detalle_titulo_encargado)
                } else {
                    stringResource(R.string.vehiculo_titulo)
                }
                PestanaVehiculo.HISTORIAL -> stringResource(R.string.detalle_titulo_historial)
                PestanaVehiculo.KILOMETRAJE -> stringResource(R.string.vehiculo_opcion_kilometraje)
                PestanaVehiculo.DOCUMENTOS -> stringResource(R.string.vehiculo_opcion_documentos)
            },
            subtitulo = vehiculo?.let { "${it.placa} · ${it.marca} ${it.modelo}" },
            onAtras = onAtras
        )

        PestanasSegmentadas(
            pestanas = listOf(
                stringResource(R.string.detalle_pestana_informacion),
                stringResource(R.string.detalle_pestana_historial),
                stringResource(R.string.detalle_pestana_kilometraje),
                stringResource(R.string.detalle_pestana_documentos)
            ),
            indiceSeleccionado = pestana.ordinal,
            onSeleccionar = { indice -> pestana = PestanaVehiculo.entries[indice] }
        )

        when {
            uiState.cargando && vehiculo == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            vehiculo == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    EstadoVacio(
                        mensaje = uiState.error ?: stringResource(R.string.detalle_no_encontrado)
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    when (pestana) {
                        PestanaVehiculo.INFORMACION -> PestanaInformacion(
                            vehiculo = vehiculo,
                            conductor = uiState.conductor,
                            esEncargado = esEncargado,
                            onReasignarConductor = { onReasignarConductor(vehiculo.id) },
                            onEditarVehiculo = { onEditarVehiculo(vehiculo.id) }
                        )

                        // Por ahora solo el encargado registra desde aquí; el
                        // formulario del conductor se conecta más adelante.
                        PestanaVehiculo.HISTORIAL -> PestanaHistorial(
                            mantenimientos = uiState.mantenimientos,
                            puedeRegistrar = esEncargado,
                            onRegistrar = { onRegistrarMantenimiento(vehiculo.id) }
                        )

                        PestanaVehiculo.KILOMETRAJE -> PestanaKilometraje(
                            vehiculo = vehiculo,
                            historial = uiState.historialKilometraje,
                            proximos = uiState.proximosMantenimientos,
                            puedeRegistrar = !esEncargado,
                            onRegistrarKilometraje = { onRegistrarKilometraje(vehiculo.id) }
                        )

                        PestanaVehiculo.DOCUMENTOS -> PestanaDocumentos(
                            vehiculo = vehiculo,
                            esEncargado = esEncargado,
                            onEditarDocumentos = { onEditarVehiculo(vehiculo.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Datos de solo lectura (Figma `28:418`). El encargado ve además el conductor
 * asignado, el kilometraje y sus acciones (Figma `102:178`).
 */
@Composable
private fun ColumnScope.PestanaInformacion(
    vehiculo: Vehiculo,
    conductor: Usuario?,
    esEncargado: Boolean,
    onReasignarConductor: () -> Unit,
    onEditarVehiculo: () -> Unit
) {
    TarjetaTransAndina {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DatoEtiquetado(
                etiqueta = stringResource(R.string.vehiculo_placa),
                valor = vehiculo.placa
            )
            DatoEtiquetado(
                etiqueta = stringResource(R.string.vehiculo_tipo),
                valor = vehiculo.tipo.replaceFirstChar { it.uppercase() }
            )
            DatoEtiquetado(
                etiqueta = stringResource(R.string.vehiculo_anio),
                valor = vehiculo.anio.toString()
            )
            DatoEtiquetado(
                etiqueta = stringResource(R.string.vehiculo_marca),
                valor = vehiculo.marca
            )
            DatoEtiquetado(
                etiqueta = stringResource(R.string.vehiculo_modelo),
                valor = vehiculo.modelo
            )
            DatoEtiquetado(
                etiqueta = stringResource(R.string.vehiculo_capacidad),
                valor = vehiculo.capacidad
                    ?.let { stringResource(R.string.vehiculo_capacidad_toneladas, it.toString()) }
                    ?: stringResource(R.string.vehiculo_sin_capacidad)
            )
        }
    }

    if (!esEncargado) return

    Spacer(modifier = Modifier.height(12.dp))

    TarjetaTransAndina {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DatoEtiquetado(
                etiqueta = stringResource(R.string.detalle_conductor_asignado),
                valor = when {
                    conductor != null -> conductor.nombreCompleto
                    vehiculo.conductorId != null -> stringResource(R.string.detalle_conductor_no_disponible)
                    else -> stringResource(R.string.sin_conductor)
                }
            )
            DatoEtiquetado(
                etiqueta = stringResource(R.string.kilometraje_actual),
                valor = formatearKilometrosConUnidad(vehiculo.kmActual)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    BotonPrimario(
        texto = if (vehiculo.conductorId == null) {
            stringResource(R.string.detalle_asignar_conductor)
        } else {
            stringResource(R.string.detalle_reasignar_conductor)
        },
        onClick = onReasignarConductor,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    BotonSecundario(
        texto = stringResource(R.string.detalle_editar_vehiculo),
        onClick = onEditarVehiculo,
        modifier = Modifier.fillMaxWidth()
    )
}

/** Historial con filtro por tipo (Figma `51:125`, `102:178`). */
@Composable
private fun ColumnScope.PestanaHistorial(
    mantenimientos: List<Mantenimiento>,
    puedeRegistrar: Boolean,
    onRegistrar: () -> Unit
) {
    var filtro by rememberSaveable { mutableStateOf<TipoMantenimiento?>(null) }
    val opciones = listOf(null, TipoMantenimiento.preventivo, TipoMantenimiento.correctivo)

    if (puedeRegistrar) {
        BotonPrimario(
            texto = stringResource(R.string.mant_titulo),
            onClick = onRegistrar,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    ChipsFiltro(
        opciones = listOf(
            stringResource(R.string.filtro_todos),
            stringResource(R.string.mantenimiento_preventivo),
            stringResource(R.string.mantenimiento_correctivo)
        ),
        indiceSeleccionado = opciones.indexOf(filtro),
        onSeleccionar = { filtro = opciones[it] }
    )

    Spacer(modifier = Modifier.height(12.dp))

    val visibles = mantenimientos.filter { filtro == null || it.tipo == filtro }
    if (visibles.isEmpty()) {
        EstadoVacio(mensaje = stringResource(R.string.historial_vacio))
        return
    }

    visibles.forEach { mantenimiento ->
        TarjetaMantenimiento(mantenimiento = mantenimiento)
        Spacer(modifier = Modifier.height(12.dp))
    }
}

/** Kilometraje actual, gráfico por mes y acceso a registrar (Figma `51:217`). */
@Composable
private fun ColumnScope.PestanaKilometraje(
    vehiculo: Vehiculo,
    historial: List<KilometrajeHistorico>,
    proximos: List<ProximoMantenimiento>,
    puedeRegistrar: Boolean,
    onRegistrarKilometraje: () -> Unit
) {
    TarjetaTransAndina {
        Text(
            text = stringResource(R.string.kilometraje_actual),
            style = MaterialTheme.typography.bodyMedium,
            color = TransAndinaTheme.colores.textoSecundario
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatearKilometrosConUnidad(vehiculo.kmActual),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    TarjetaTransAndina {
        Text(
            text = stringResource(R.string.kilometraje_acumulado),
            style = MaterialTheme.typography.bodyMedium,
            color = TransAndinaTheme.colores.textoSecundario
        )
        val puntos = remember(historial) { agruparPorMes(historial) }
        if (puntos.isEmpty()) {
            EstadoVacio(mensaje = stringResource(R.string.kilometraje_sin_historial))
        } else {
            GraficoLineaKilometraje(puntos = puntos)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    TarjetaProximoMantenimiento(proximos)

    if (!puedeRegistrar) return

    Spacer(modifier = Modifier.height(16.dp))

    BotonPrimario(
        texto = stringResource(R.string.vehiculo_opcion_registrar_km),
        onClick = onRegistrarKilometraje,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * "Próximo mantenimiento preventivo" (Figma `51:217`): una fila por categoría,
 * calculada con `frecuencias_mantenimiento` y el último servicio registrado.
 */
@Composable
private fun TarjetaProximoMantenimiento(proximos: List<ProximoMantenimiento>) {
    TarjetaTransAndina {
        Text(
            text = stringResource(R.string.kilometraje_proximo_titulo),
            style = MaterialTheme.typography.bodyMedium,
            color = TransAndinaTheme.colores.textoSecundario
        )
        if (proximos.isEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.kilometraje_proximo_sin_datos),
                style = MaterialTheme.typography.bodySmall,
                color = TransAndinaTheme.colores.textoSecundario
            )
        }
        proximos
            .sortedByDescending { it.estado }
            .forEach { proximo ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = proximo.categoria,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = textoProximo(proximo),
                            style = MaterialTheme.typography.bodySmall,
                            color = TransAndinaTheme.colores.textoSecundario
                        )
                    }
                    ChipEstadoMantenimiento(proximo.estado)
                }
            }
    }
}

@Composable
private fun textoProximo(proximo: ProximoMantenimiento): String {
    val km = proximo.kmObjetivo?.let(::formatearKilometrosConUnidad)
    val fecha = proximo.fechaObjetivo?.let(::formatearFecha)
    return when {
        km != null && fecha != null -> stringResource(R.string.kilometraje_proximo_km_o_fecha, km, fecha)
        km != null -> stringResource(R.string.kilometraje_proximo_km, km)
        fecha != null -> stringResource(R.string.kilometraje_proximo_fecha, fecha)
        else -> ""
    }
}

/**
 * Una tarjeta por cada fecha registrada en `vehiculos` (Figma `87:135`).
 * El conductor solo ve las que tienen fecha; el encargado ve las cuatro, para
 * notar las que faltan, y puede editarlas.
 *
 * El enlace "Ver documento ›" del Figma no se implementa: los documentos son
 * fechas, no archivos adjuntos (docs/ADAPTACION_MOVIL.md §8).
 */
@Composable
private fun ColumnScope.PestanaDocumentos(
    vehiculo: Vehiculo,
    esEncargado: Boolean,
    onEditarDocumentos: () -> Unit
) {
    val documentos = vehiculo.fechasDocumentos()
        .filter { (_, fecha) -> esEncargado || fecha != null }

    if (documentos.isEmpty()) {
        EstadoVacio(mensaje = stringResource(R.string.documentos_vacio))
        return
    }

    documentos.forEach { (documento, fechaIso) ->
        TarjetaDocumento(nombre = nombreDocumento(documento), fechaIso = fechaIso)
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (esEncargado) {
        Spacer(modifier = Modifier.height(4.dp))
        BotonPrimario(
            texto = stringResource(R.string.documentos_editar),
            onClick = onEditarDocumentos,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Text(
            text = stringResource(R.string.documentos_pie),
            style = MaterialTheme.typography.labelSmall,
            color = TransAndinaTheme.colores.textoSecundario
        )
    }
}

@Composable
internal fun nombreDocumento(documento: TipoDocumento): String = when (documento) {
    TipoDocumento.MARCHAMO -> stringResource(R.string.documentos_marchamo)
    TipoDocumento.REVISION_TECNICA -> stringResource(R.string.documentos_revision)
    TipoDocumento.SEGURO -> stringResource(R.string.documentos_seguro)
    TipoDocumento.PERMISO_CARGA -> stringResource(R.string.documentos_permiso_carga)
}

@Composable
private fun TarjetaDocumento(nombre: String, fechaIso: String?) {
    val estado = calcularEstadoDocumento(fechaIso)
    val fechaLegible = formatearFechaIso(fechaIso)

    TarjetaTransAndina {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (estado) {
                        EstadoDocumento.VENCIDO ->
                            stringResource(R.string.documentos_vencio, fechaLegible)
                        EstadoDocumento.SIN_DATO ->
                            stringResource(R.string.documentos_sin_fecha)
                        else -> stringResource(R.string.documentos_vence, fechaLegible)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TransAndinaTheme.colores.textoSecundario
                )
            }
            when (estado) {
                EstadoDocumento.AL_DIA -> ChipEstado(
                    texto = stringResource(R.string.estado_vigente),
                    nivel = NivelEstado.OK
                )
                EstadoDocumento.PROXIMO -> ChipEstado(
                    texto = stringResource(R.string.estado_por_vencer),
                    nivel = NivelEstado.AVISO
                )
                EstadoDocumento.VENCIDO -> ChipEstado(
                    texto = stringResource(R.string.estado_vencido),
                    nivel = NivelEstado.CRITICO
                )
                EstadoDocumento.SIN_DATO -> ChipEstado(
                    texto = stringResource(R.string.estado_sin_fecha),
                    nivel = NivelEstado.INFO
                )
            }
        }
    }
}

/**
 * Deja una lectura por mes (la más alta, que es la más reciente del mes porque
 * el odómetro solo sube) y la etiqueta con el nombre corto del mes.
 */
internal fun agruparPorMes(historial: List<KilometrajeHistorico>): List<PuntoGrafico> {
    return historial
        .mapNotNull { registro -> parsearFechaIso(registro.fecha)?.let { it to registro.km } }
        .groupBy { (fecha, _) -> fecha.year to fecha.monthValue }
        .toSortedMap(compareBy({ it.first }, { it.second }))
        .map { (anioMes, lecturas) ->
            val mes = java.time.Month.of(anioMes.second)
                .getDisplayName(TextStyle.SHORT, Locale("es"))
                .replaceFirstChar { it.uppercase() }
                .removeSuffix(".")
            PuntoGrafico(etiqueta = mes, valor = lecturas.maxOf { (_, km) -> km })
        }
}

private val historialDeMuestra = listOf(
    KilometrajeHistorico("1", "1", "2026-03-04", 480_000.0),
    KilometrajeHistorico("2", "1", "2026-04-08", 482_500.0),
    KilometrajeHistorico("3", "1", "2026-05-11", 484_100.0),
    KilometrajeHistorico("4", "1", "2026-06-09", 487_000.0),
    KilometrajeHistorico("5", "1", "2026-07-13", 489_200.0),
    KilometrajeHistorico("6", "1", "2026-08-10", 491_000.0),
    KilometrajeHistorico("7", "1", "2026-09-07", 492_400.0)
)

private val mantenimientosDeMuestra = listOf(
    Mantenimiento(
        id = "m1",
        vehiculoId = "1",
        registradoPor = "u1",
        tipo = TipoMantenimiento.preventivo,
        categoria = "Cambio de aceite",
        fecha = "2026-08-25",
        km = 492_000.0,
        responsable = "Marco Ureña",
        costo = 45_000.0,
        taller = "Taller Central"
    ),
    Mantenimiento(
        id = "m2",
        vehiculoId = "1",
        registradoPor = "u1",
        tipo = TipoMantenimiento.correctivo,
        categoria = "Llantas",
        fecha = "2026-07-02",
        km = 488_500.0,
        costo = 320_000.0,
        taller = "Taller Norte"
    )
)

private val proximosDeMuestra = listOf(
    ProximoMantenimiento(
        categoria = "Cambio de aceite",
        ultimaFecha = java.time.LocalDate.of(2026, 8, 25),
        kmObjetivo = 497_000.0,
        fechaObjetivo = java.time.LocalDate.of(2027, 2, 21),
        kmRestantes = 4_600.0,
        diasRestantes = 157,
        estado = EstadoMantenimiento.AL_DIA
    ),
    ProximoMantenimiento(
        categoria = "Llantas",
        ultimaFecha = java.time.LocalDate.of(2026, 7, 2),
        kmObjetivo = 498_500.0,
        fechaObjetivo = java.time.LocalDate.of(2026, 9, 29),
        kmRestantes = 6_100.0,
        diasRestantes = 12,
        estado = EstadoMantenimiento.PROXIMO
    )
)

private val conductorDeMuestra = Usuario(
    id = "c1",
    nombreCompleto = "Carlos Fernández Quesada",
    cedula = "111111111",
    email = "carlos@transandina.cr",
    rol = RolUsuario.conductor
)

@Composable
private fun DetalleDePrueba(
    uiState: VehiculoDetalleUiState,
    pestana: PestanaVehiculo,
    esEncargado: Boolean = false
) {
    TransAndinaFlotillaTheme {
        ContenidoDetalle(
            uiState = uiState,
            pestanaInicial = pestana,
            esEncargado = esEncargado,
            onAtras = {}
        )
    }
}

@Preview(name = "Detalle · Información", showBackground = true, heightDp = 800)
@Composable
private fun DetalleInformacionPreview() {
    DetalleDePrueba(
        VehiculoDetalleUiState(vehiculo = vehiculoDeMuestra),
        PestanaVehiculo.INFORMACION
    )
}

@Preview(name = "Detalle · Información (encargado)", showBackground = true, heightDp = 900)
@Composable
private fun DetalleInformacionEncargadoPreview() {
    DetalleDePrueba(
        VehiculoDetalleUiState(
            vehiculo = vehiculoDeMuestra.copy(conductorId = "c1"),
            conductor = conductorDeMuestra
        ),
        PestanaVehiculo.INFORMACION,
        esEncargado = true
    )
}

@Preview(name = "Detalle · Historial", showBackground = true, heightDp = 600)
@Composable
private fun DetalleHistorialConDatosPreview() {
    DetalleDePrueba(
        VehiculoDetalleUiState(
            vehiculo = vehiculoDeMuestra,
            mantenimientos = mantenimientosDeMuestra
        ),
        PestanaVehiculo.HISTORIAL
    )
}

@Preview(name = "Detalle · Documentos (encargado)", showBackground = true, heightDp = 800)
@Composable
private fun DetalleDocumentosEncargadoPreview() {
    DetalleDePrueba(
        VehiculoDetalleUiState(vehiculo = vehiculoDeMuestra),
        PestanaVehiculo.DOCUMENTOS,
        esEncargado = true
    )
}

@Preview(name = "Detalle · Historial vacío", showBackground = true, heightDp = 500)
@Composable
private fun DetalleHistorialPreview() {
    DetalleDePrueba(
        VehiculoDetalleUiState(vehiculo = vehiculoDeMuestra),
        PestanaVehiculo.HISTORIAL
    )
}

@Preview(name = "Detalle · Kilometraje", showBackground = true, heightDp = 700)
@Composable
private fun DetalleKilometrajePreview() {
    DetalleDePrueba(
        VehiculoDetalleUiState(
            vehiculo = vehiculoDeMuestra,
            historialKilometraje = historialDeMuestra,
            proximosMantenimientos = proximosDeMuestra
        ),
        PestanaVehiculo.KILOMETRAJE
    )
}

@Preview(name = "Detalle · Kilometraje sin historial", showBackground = true, heightDp = 700)
@Composable
private fun DetalleKilometrajeVacioPreview() {
    DetalleDePrueba(
        VehiculoDetalleUiState(vehiculo = vehiculoDeMuestra),
        PestanaVehiculo.KILOMETRAJE
    )
}

@Preview(name = "Detalle · Documentos", showBackground = true, heightDp = 700)
@Composable
private fun DetalleDocumentosPreview() {
    DetalleDePrueba(
        VehiculoDetalleUiState(vehiculo = vehiculoDeMuestra),
        PestanaVehiculo.DOCUMENTOS
    )
}

@Preview(name = "Detalle · Documentos sin fechas", showBackground = true, heightDp = 500)
@Composable
private fun DetalleDocumentosVaciosPreview() {
    DetalleDePrueba(
        VehiculoDetalleUiState(
            vehiculo = vehiculoDeMuestra.copy(
                fechaMarchamo = null,
                fechaRevisionTecnica = null,
                fechaSeguro = null
            )
        ),
        PestanaVehiculo.DOCUMENTOS
    )
}

@Preview(name = "Detalle · cargando", showBackground = true, heightDp = 400)
@Composable
private fun DetalleCargandoPreview() {
    DetalleDePrueba(
        VehiculoDetalleUiState(cargando = true),
        PestanaVehiculo.INFORMACION
    )
}

@Preview(name = "Detalle · con error", showBackground = true, heightDp = 400)
@Composable
private fun DetalleErrorPreview() {
    DetalleDePrueba(
        VehiculoDetalleUiState(error = "No se pudo cargar la información del vehículo"),
        PestanaVehiculo.INFORMACION
    )
}
