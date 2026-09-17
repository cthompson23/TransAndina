package com.transandina.flotilla.ui.reportes

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.TipoMantenimiento
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.domain.formatearColones
import com.transandina.flotilla.domain.formatearFecha
import com.transandina.flotilla.domain.formatearFechaIso
import com.transandina.flotilla.ui.components.BotonConfirmar
import com.transandina.flotilla.ui.components.BotonSecundario
import com.transandina.flotilla.ui.components.CampoFecha
import com.transandina.flotilla.ui.components.CampoSeleccion
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.FilaBotonesFormulario
import com.transandina.flotilla.ui.components.TarjetaMantenimiento
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Reportes (Figma `102:198` → `102:218`). En móvil los filtros van en una
 * columna y el resultado reemplaza la pantalla; "Formato de salida" se quita
 * porque el reporte se ve aquí y se exporta a PDF (docs/ADAPTACION_MOVIL.md §6).
 */
@Composable
fun ReportesScreen(viewModel: ReportesViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val alcance = rememberCoroutineScope()
    var exportando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargar()
    }

    if (uiState.resultado == null) {
        ContenidoFiltros(
            uiState = uiState,
            acciones = AccionesFiltros(
                onVehiculo = viewModel::onVehiculoChange,
                onTipo = viewModel::onTipoChange,
                onTaller = viewModel::onTallerChange,
                onDesde = viewModel::onDesdeChange,
                onHasta = viewModel::onHastaChange,
                onLimpiar = viewModel::limpiarFiltros,
                onGenerar = viewModel::generar
            )
        )
    } else {
        BackHandler(onBack = viewModel::modificarFiltros)
        ContenidoResultado(
            uiState = uiState,
            exportando = exportando,
            onModificarFiltros = viewModel::modificarFiltros,
            onExportar = {
                exportando = true
                alcance.launch {
                    try {
                        val datos = armarDatosPdf(context, uiState)
                        val archivo = withContext(Dispatchers.IO) {
                            ExportadorPdfReporte.generar(
                                context = context,
                                datos = datos,
                                nombreArchivo = "reporte-mantenimientos-${LocalDate.now()}.pdf"
                            )
                        }
                        ExportadorPdfReporte.compartir(context, archivo)
                    } catch (e: Exception) {
                        Toast.makeText(context, R.string.reporte_error_pdf, Toast.LENGTH_LONG).show()
                    } finally {
                        exportando = false
                    }
                }
            }
        )
    }
}

private data class AccionesFiltros(
    val onVehiculo: (String?) -> Unit = {},
    val onTipo: (TipoMantenimiento?) -> Unit = {},
    val onTaller: (String?) -> Unit = {},
    val onDesde: (LocalDate) -> Unit = {},
    val onHasta: (LocalDate) -> Unit = {},
    val onLimpiar: () -> Unit = {},
    val onGenerar: () -> Unit = {}
)

private fun etiquetaVehiculo(vehiculo: Vehiculo) =
    "${vehiculo.placa} · ${vehiculo.marca} ${vehiculo.modelo}"

@Composable
private fun ContenidoFiltros(
    uiState: ReportesUiState,
    acciones: AccionesFiltros
) {
    val filtros = uiState.filtros
    val todosVehiculos = stringResource(R.string.reporte_todos_vehiculos)
    val ambosTipos = stringResource(R.string.reporte_ambos_tipos)
    val todosTalleres = stringResource(R.string.reporte_todos_talleres)
    val preventivo = stringResource(R.string.mantenimiento_preventivo)
    val correctivo = stringResource(R.string.mantenimiento_correctivo)
    val etiquetasVehiculos = uiState.vehiculos.map(::etiquetaVehiculo)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(titulo = stringResource(R.string.reporte_titulo))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.reporte_filtros),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            CampoSeleccion(
                etiqueta = stringResource(R.string.registro_km_vehiculo),
                seleccion = uiState.vehiculoFiltrado?.let(::etiquetaVehiculo) ?: todosVehiculos,
                opciones = listOf(todosVehiculos) + etiquetasVehiculos,
                onSeleccionar = { elegida ->
                    val indice = etiquetasVehiculos.indexOf(elegida)
                    acciones.onVehiculo(uiState.vehiculos.getOrNull(indice)?.id)
                },
                forma = FormaPildora,
                habilitado = !uiState.cargando
            )
            CampoSeleccion(
                etiqueta = stringResource(R.string.reporte_tipo),
                seleccion = when (filtros.tipo) {
                    null -> ambosTipos
                    TipoMantenimiento.preventivo -> preventivo
                    TipoMantenimiento.correctivo -> correctivo
                },
                opciones = listOf(ambosTipos, preventivo, correctivo),
                onSeleccionar = { elegida ->
                    acciones.onTipo(
                        when (elegida) {
                            preventivo -> TipoMantenimiento.preventivo
                            correctivo -> TipoMantenimiento.correctivo
                            else -> null
                        }
                    )
                },
                forma = FormaPildora
            )
            CampoSeleccion(
                etiqueta = stringResource(R.string.reporte_taller),
                seleccion = filtros.taller ?: todosTalleres,
                opciones = listOf(todosTalleres) + uiState.talleres,
                onSeleccionar = { elegida ->
                    acciones.onTaller(elegida.takeIf { it != todosTalleres })
                },
                forma = FormaPildora,
                habilitado = !uiState.cargando
            )
            CampoFecha(
                etiqueta = stringResource(R.string.reporte_desde),
                fecha = filtros.desde,
                onFechaCambia = acciones.onDesde,
                forma = FormaPildora
            )
            CampoFecha(
                etiqueta = stringResource(R.string.reporte_hasta),
                fecha = filtros.hasta,
                onFechaCambia = acciones.onHasta,
                forma = FormaPildora
            )

            uiState.error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            FilaBotonesFormulario(
                textoAccion = stringResource(R.string.reporte_generar),
                onAccion = acciones.onGenerar,
                onCancelar = acciones.onLimpiar,
                textoCancelar = stringResource(R.string.reporte_limpiar),
                cargando = uiState.cargando
            )

            Text(
                text = stringResource(R.string.reporte_ayuda),
                style = MaterialTheme.typography.bodySmall,
                color = TransAndinaTheme.colores.textoSecundario
            )
        }
    }
}

@Composable
private fun ContenidoResultado(
    uiState: ReportesUiState,
    exportando: Boolean,
    onModificarFiltros: () -> Unit,
    onExportar: () -> Unit
) {
    val resultado = uiState.resultado.orEmpty()
    val placas = uiState.placasPorVehiculo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = stringResource(R.string.reporte_generado_titulo),
            onAtras = onModificarFiltros
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TarjetaTransAndina {
                    Text(
                        text = subtituloReporte(uiState),
                        style = MaterialTheme.typography.bodySmall,
                        color = TransAndinaTheme.colores.textoSecundario
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.reporte_cantidad, resultado.size),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.reporte_costo_total, formatearColones(uiState.costoTotal)),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BotonSecundario(
                            texto = stringResource(R.string.reporte_modificar),
                            onClick = onModificarFiltros,
                            modifier = Modifier.weight(1f)
                        )
                        BotonConfirmar(
                            texto = stringResource(R.string.reporte_exportar),
                            onClick = onExportar,
                            modifier = Modifier.weight(1f),
                            habilitado = resultado.isNotEmpty(),
                            cargando = exportando
                        )
                    }
                }
            }

            if (uiState.cargando && resultado.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (resultado.isEmpty()) {
                item { EstadoVacio(mensaje = stringResource(R.string.reporte_sin_resultados)) }
            } else {
                items(resultado, key = { it.id }) { mantenimiento ->
                    TarjetaMantenimiento(
                        mantenimiento = mantenimiento,
                        placa = placas[mantenimiento.vehiculoId]
                    )
                }
            }
        }
    }
}

/** "01/01/2026 al 29/08/2026 · Todos los vehículos · Preventivo" */
@Composable
private fun subtituloReporte(uiState: ReportesUiState): String {
    val filtros = uiState.filtros
    val partes = mutableListOf<String>()
    partes += stringResource(
        R.string.reporte_rango,
        filtros.desde?.let(::formatearFecha) ?: "—",
        filtros.hasta?.let(::formatearFecha) ?: "—"
    )
    partes += uiState.vehiculoFiltrado?.placa ?: stringResource(R.string.reporte_todos_vehiculos)
    when (filtros.tipo) {
        TipoMantenimiento.preventivo -> partes += stringResource(R.string.mantenimiento_preventivo)
        TipoMantenimiento.correctivo -> partes += stringResource(R.string.mantenimiento_correctivo)
        null -> Unit
    }
    filtros.taller?.let { partes += it }
    return partes.joinToString(" · ")
}

/** Traduce el resultado a filas de texto para el PDF. */
private fun armarDatosPdf(context: Context, uiState: ReportesUiState): DatosReportePdf {
    val filtros = uiState.filtros
    val placas = uiState.placasPorVehiculo
    val resultado = uiState.resultado.orEmpty()
    val subtitulo = buildList {
        add(
            context.getString(
                R.string.reporte_rango,
                filtros.desde?.let(::formatearFecha) ?: "—",
                filtros.hasta?.let(::formatearFecha) ?: "—"
            )
        )
        add(uiState.vehiculoFiltrado?.let(::etiquetaVehiculo) ?: context.getString(R.string.reporte_todos_vehiculos))
        filtros.tipo?.let { add(context.getString(etiquetaTipoRes(it))) }
        filtros.taller?.let { add(it) }
    }.joinToString(" · ")

    return DatosReportePdf(
        titulo = context.getString(R.string.reporte_pdf_titulo),
        subtitulo = subtitulo,
        resumen = context.getString(R.string.reporte_cantidad, resultado.size) + " · " +
            context.getString(R.string.reporte_costo_total, formatearColones(uiState.costoTotal)),
        encabezados = listOf(
            context.getString(R.string.reporte_col_fecha),
            context.getString(R.string.vehiculo_placa),
            context.getString(R.string.reporte_col_categoria),
            context.getString(R.string.vehiculo_tipo),
            context.getString(R.string.reporte_taller),
            context.getString(R.string.reporte_col_responsable),
            context.getString(R.string.reporte_col_costo)
        ),
        filas = resultado.map { m ->
            listOf(
                formatearFechaIso(m.fecha),
                placas[m.vehiculoId].orEmpty(),
                m.categoria,
                context.getString(etiquetaTipoRes(m.tipo)),
                m.taller.orEmpty(),
                m.responsable.orEmpty(),
                m.costo?.let(::formatearColones).orEmpty()
            )
        },
        pie = context.getString(R.string.reporte_pdf_pie, formatearFecha(LocalDate.now()))
    )
}

private fun etiquetaTipoRes(tipo: TipoMantenimiento): Int = when (tipo) {
    TipoMantenimiento.preventivo -> R.string.mantenimiento_preventivo
    TipoMantenimiento.correctivo -> R.string.mantenimiento_correctivo
}

private val vehiculosDeMuestra = listOf(
    Vehiculo("v1", "SCD-3421", "Nissan", "Frontier", 2021, "liviano", kmActual = 492_400.0),
    Vehiculo("v2", "BQR-8890", "Toyota", "Hilux", 2020, "liviano", kmActual = 310_250.0)
)

private val mantenimientosDeMuestra = listOf(
    Mantenimiento(
        id = "m1", vehiculoId = "v1", registradoPor = "u1",
        tipo = TipoMantenimiento.preventivo, categoria = "Cambio de aceite",
        fecha = "2026-08-25", km = 492_000.0, responsable = "Marco Ureña",
        costo = 45_000.0, taller = "Taller Central"
    ),
    Mantenimiento(
        id = "m2", vehiculoId = "v2", registradoPor = "u1",
        tipo = TipoMantenimiento.correctivo, categoria = "Frenos",
        fecha = "2026-08-18", km = 310_000.0, responsable = "Ana Castro",
        costo = 112_000.0, taller = "Taller Norte"
    )
)

@Preview(name = "Reportes · filtros", showBackground = true, heightDp = 800)
@Composable
private fun ReportesFiltrosPreview() {
    TransAndinaFlotillaTheme {
        ContenidoFiltros(
            uiState = ReportesUiState(
                vehiculos = vehiculosDeMuestra,
                talleres = listOf("Taller Central", "Taller Norte"),
                filtros = filtrosIniciales(LocalDate.of(2026, 8, 29))
            ),
            acciones = AccionesFiltros()
        )
    }
}

@Preview(name = "Reportes · resultado", showBackground = true, heightDp = 800)
@Composable
private fun ReportesResultadoPreview() {
    TransAndinaFlotillaTheme {
        ContenidoResultado(
            uiState = ReportesUiState(
                vehiculos = vehiculosDeMuestra,
                mantenimientos = mantenimientosDeMuestra,
                filtros = filtrosIniciales(LocalDate.of(2026, 8, 29)),
                resultado = mantenimientosDeMuestra
            ),
            exportando = false,
            onModificarFiltros = {},
            onExportar = {}
        )
    }
}
