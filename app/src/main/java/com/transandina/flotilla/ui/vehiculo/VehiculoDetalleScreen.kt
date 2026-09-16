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
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.domain.EstadoDocumento
import com.transandina.flotilla.domain.calcularEstadoDocumento
import com.transandina.flotilla.domain.formatearFechaIso
import com.transandina.flotilla.domain.formatearKilometrosConUnidad
import com.transandina.flotilla.domain.parsearFechaIso
import com.transandina.flotilla.ui.components.BotonPrimario
import com.transandina.flotilla.ui.components.ChipEstado
import com.transandina.flotilla.ui.components.DatoEtiquetado
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
 * @param accionesExtra se dibuja al final de la pestaña Información. Por ahora
 *   va vacío; en la Fase 4 ahí entra "Reasignar conductor".
 */
@Composable
fun VehiculoDetalleScreen(
    vehiculoId: String,
    modifier: Modifier = Modifier,
    pestanaInicial: PestanaVehiculo = PestanaVehiculo.INFORMACION,
    viewModel: VehiculoDetalleViewModel = viewModel(),
    onAtras: () -> Unit = {},
    onRegistrarKilometraje: (vehiculoId: String) -> Unit = {},
    tokenRecarga: Int = 0,
    accionesExtra: @Composable ColumnScope.(Vehiculo) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vehiculoId, tokenRecarga) {
        viewModel.cargar(vehiculoId)
    }

    ContenidoDetalle(
        uiState = uiState,
        pestanaInicial = pestanaInicial,
        modifier = modifier,
        onAtras = onAtras,
        onRegistrarKilometraje = onRegistrarKilometraje,
        accionesExtra = accionesExtra
    )
}

@Composable
private fun ContenidoDetalle(
    uiState: VehiculoDetalleUiState,
    pestanaInicial: PestanaVehiculo,
    modifier: Modifier = Modifier,
    onAtras: () -> Unit = {},
    onRegistrarKilometraje: (String) -> Unit = {},
    accionesExtra: @Composable ColumnScope.(Vehiculo) -> Unit = {}
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
                PestanaVehiculo.INFORMACION -> stringResource(R.string.vehiculo_titulo)
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
                            accionesExtra = accionesExtra
                        )

                        PestanaVehiculo.HISTORIAL -> PestanaHistorial()

                        PestanaVehiculo.KILOMETRAJE -> PestanaKilometraje(
                            vehiculo = vehiculo,
                            historial = uiState.historialKilometraje,
                            onRegistrarKilometraje = { onRegistrarKilometraje(vehiculo.id) }
                        )

                        PestanaVehiculo.DOCUMENTOS -> PestanaDocumentos(vehiculo = vehiculo)
                    }
                }
            }
        }
    }
}

/** Datos de solo lectura (Figma `28:418`). Sin botón de editar. */
@Composable
private fun ColumnScope.PestanaInformacion(
    vehiculo: Vehiculo,
    accionesExtra: @Composable ColumnScope.(Vehiculo) -> Unit
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

    accionesExtra(vehiculo)
}

/** Se llena en la Parte B, cuando exista el repositorio de mantenimientos. */
@Composable
private fun PestanaHistorial() {
    EstadoVacio(mensaje = stringResource(R.string.historial_vacio))
}

/** Kilometraje actual, gráfico por mes y acceso a registrar (Figma `51:217`). */
@Composable
private fun ColumnScope.PestanaKilometraje(
    vehiculo: Vehiculo,
    historial: List<KilometrajeHistorico>,
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

    // TODO(backend): la tarjeta "Próximo mantenimiento preventivo" del Figma
    //  (51:217) necesita cruzar `frecuencias_mantenimiento` (km_frecuencia /
    //  dias_frecuencia por tipo de vehículo y categoría) con el ÚLTIMO
    //  mantenimiento de esa categoría, que vive en la tabla `mantenimientos`.
    //  Esa tabla se conecta en la Parte B; hasta entonces no se muestra nada,
    //  para no inventar una fecha estimada.

    Spacer(modifier = Modifier.height(16.dp))

    BotonPrimario(
        texto = stringResource(R.string.vehiculo_opcion_registrar_km),
        onClick = onRegistrarKilometraje,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Una tarjeta por cada fecha registrada en `vehiculos` (Figma `87:135`).
 *
 * TODO(backend): el Figma muestra además "Permiso de carga" y un enlace
 *  "Ver documento ›". Ninguno existe hoy: los documentos son tres columnas
 *  `date` en `vehiculos`, no una entidad con archivos adjuntos
 *  (docs/ADAPTACION_MOVIL.md §8).
 */
@Composable
private fun ColumnScope.PestanaDocumentos(vehiculo: Vehiculo) {
    val documentos = listOf(
        stringResource(R.string.documentos_marchamo) to vehiculo.fechaMarchamo,
        stringResource(R.string.documentos_revision) to vehiculo.fechaRevisionTecnica,
        stringResource(R.string.documentos_seguro) to vehiculo.fechaSeguro
    ).filter { (_, fecha) -> fecha != null }

    if (documentos.isEmpty()) {
        EstadoVacio(mensaje = stringResource(R.string.documentos_vacio))
        return
    }

    documentos.forEach { (nombre, fechaIso) ->
        TarjetaDocumento(nombre = nombre, fechaIso = fechaIso)
        Spacer(modifier = Modifier.height(12.dp))
    }

    Text(
        text = stringResource(R.string.documentos_pie),
        style = MaterialTheme.typography.labelSmall,
        color = TransAndinaTheme.colores.textoSecundario
    )
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

@Composable
private fun DetalleDePrueba(
    uiState: VehiculoDetalleUiState,
    pestana: PestanaVehiculo
) {
    TransAndinaFlotillaTheme {
        ContenidoDetalle(uiState = uiState, pestanaInicial = pestana, onAtras = {})
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
            historialKilometraje = historialDeMuestra
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
