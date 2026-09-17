package com.transandina.flotilla.ui.mantenimiento

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transandina.flotilla.R
import com.transandina.flotilla.data.foto.FormatoFotoNoPermitido
import com.transandina.flotilla.data.foto.PreparadorFoto
import com.transandina.flotilla.data.model.FrecuenciaMantenimiento
import com.transandina.flotilla.data.model.TipoMantenimiento
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.domain.ServicioEstimado
import com.transandina.flotilla.domain.formatearFecha
import com.transandina.flotilla.domain.formatearKilometrosConUnidad
import com.transandina.flotilla.ui.components.BotonAdjuntar
import com.transandina.flotilla.ui.components.BotonSecundario
import com.transandina.flotilla.ui.components.CampoFecha
import com.transandina.flotilla.ui.components.CampoSeleccion
import com.transandina.flotilla.ui.components.CampoTexto
import com.transandina.flotilla.ui.components.EstadoVacio
import com.transandina.flotilla.ui.components.FilaBotonesFormulario
import com.transandina.flotilla.ui.components.TarjetaTransAndina
import com.transandina.flotilla.ui.components.TransAndinaTopBar
import com.transandina.flotilla.ui.components.etiquetaTipoMantenimiento
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Registrar mantenimiento (Figma `49:161`). Además de los campos del Figma
 * pide el kilometraje del servicio, el taller y una descripción
 * (docs/ADAPTACION_MOVIL.md §7), y muestra cuándo tocaría el siguiente.
 */
@Composable
fun RegistrarMantenimientoScreen(
    vehiculoId: String,
    viewModel: RegistrarMantenimientoViewModel = viewModel(),
    tokenRecarga: Int = 0,
    onAtras: () -> Unit = {},
    onRegistrarKilometraje: (vehiculoId: String) -> Unit = {},
    onGuardado: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val recursos = LocalResources.current
    val alcance = rememberCoroutineScope()
    var preparandoFotos by remember { mutableStateOf(false) }

    val mensajeFormato = stringResource(R.string.mant_foto_formato_invalido)
    val mensajeFotoFallida = stringResource(R.string.mant_foto_no_se_pudo_leer)

    LaunchedEffect(vehiculoId) {
        viewModel.cargar(vehiculoId)
    }

    // Al volver de registrar el kilometraje, se relee el vehículo para que la
    // nueva lectura valga; lo escrito en el formulario se conserva.
    LaunchedEffect(tokenRecarga) {
        if (tokenRecarga > 0) viewModel.recargarVehiculo()
    }

    LaunchedEffect(uiState.guardado) {
        if (!uiState.guardado) return@LaunchedEffect
        if (uiState.fotosFallidas > 0) {
            Toast.makeText(
                context,
                recursos.getString(R.string.mant_fotos_fallidas, uiState.fotosFallidas),
                Toast.LENGTH_LONG
            ).show()
        }
        onGuardado()
    }

    // El selector del sistema no pide permisos de almacenamiento.
    val selectorFotos = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAXIMO_FOTOS)
    ) { uris ->
        val libres = MAXIMO_FOTOS - viewModel.uiState.value.fotos.size
        if (uris.isEmpty() || libres <= 0) return@rememberLauncherForActivityResult
        preparandoFotos = true
        alcance.launch {
            uris.take(libres).forEachIndexed { indice, uri ->
                try {
                    val preparada = withContext(Dispatchers.IO) { PreparadorFoto.preparar(context, uri) }
                    viewModel.onFotoAgregada(
                        FotoAdjunta(
                            nombre = preparada.nombre
                                ?: recursos.getString(R.string.mant_foto_generica, indice + 1),
                            jpeg = preparada.jpeg,
                            miniatura = preparada.miniatura
                        )
                    )
                } catch (e: FormatoFotoNoPermitido) {
                    viewModel.onErrorFoto(mensajeFormato)
                } catch (e: Exception) {
                    viewModel.onErrorFoto(mensajeFotoFallida)
                }
            }
            preparandoFotos = false
        }
    }

    ContenidoRegistrarMantenimiento(
        uiState = uiState,
        preparandoFotos = preparandoFotos,
        acciones = AccionesMantenimiento(
            onTipo = viewModel::onTipoChange,
            onCategoria = viewModel::onCategoriaChange,
            onFecha = viewModel::onFechaChange,
            onKm = viewModel::onKmChange,
            onTaller = viewModel::onTallerChange,
            onResponsable = viewModel::onResponsableChange,
            onCosto = viewModel::onCostoChange,
            onDescripcion = viewModel::onDescripcionChange,
            onAdjuntar = {
                selectorFotos.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onQuitarFoto = viewModel::onFotoQuitada,
            onRegistrarKilometraje = { uiState.vehiculo?.let { onRegistrarKilometraje(it.id) } },
            onGuardar = viewModel::guardar,
            onCancelar = onAtras
        )
    )
}

private data class AccionesMantenimiento(
    val onTipo: (TipoMantenimiento) -> Unit = {},
    val onCategoria: (String) -> Unit = {},
    val onFecha: (LocalDate) -> Unit = {},
    val onKm: (String) -> Unit = {},
    val onTaller: (String) -> Unit = {},
    val onResponsable: (String) -> Unit = {},
    val onCosto: (String) -> Unit = {},
    val onDescripcion: (String) -> Unit = {},
    val onAdjuntar: () -> Unit = {},
    val onQuitarFoto: (String) -> Unit = {},
    val onRegistrarKilometraje: () -> Unit = {},
    val onGuardar: () -> Unit = {},
    val onCancelar: () -> Unit = {}
)

@Composable
private fun ContenidoRegistrarMantenimiento(
    uiState: RegistrarMantenimientoUiState,
    preparandoFotos: Boolean,
    acciones: AccionesMantenimiento
) {
    val vehiculo = uiState.vehiculo
    val editable = !uiState.guardando

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TransAndinaTopBar(
            titulo = stringResource(R.string.mant_titulo),
            subtitulo = vehiculo?.let { "${it.placa} · ${it.marca} ${it.modelo}" },
            onAtras = acciones.onCancelar
        )

        when {
            vehiculo == null && uiState.cargando -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            vehiculo == null -> Box(modifier = Modifier.padding(16.dp)) {
                EstadoVacio(mensaje = uiState.error ?: stringResource(R.string.detalle_no_encontrado))
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val preventivo = etiquetaTipoMantenimiento(TipoMantenimiento.preventivo)
                val correctivo = etiquetaTipoMantenimiento(TipoMantenimiento.correctivo)

                CampoSeleccion(
                    etiqueta = stringResource(R.string.vehiculo_tipo),
                    seleccion = uiState.tipo?.let { etiquetaTipoMantenimiento(it) },
                    opciones = listOf(preventivo, correctivo),
                    onSeleccionar = { elegida ->
                        acciones.onTipo(
                            if (elegida == preventivo) TipoMantenimiento.preventivo else TipoMantenimiento.correctivo
                        )
                    },
                    marcadorDePosicion = stringResource(R.string.mant_tipo_marcador),
                    forma = FormaPildora,
                    habilitado = editable
                )
                CampoSeleccion(
                    etiqueta = stringResource(R.string.reporte_col_categoria),
                    seleccion = uiState.categoria,
                    opciones = uiState.categorias,
                    onSeleccionar = acciones.onCategoria,
                    marcadorDePosicion = stringResource(R.string.mant_categoria_marcador),
                    forma = FormaPildora,
                    habilitado = editable && uiState.categorias.isNotEmpty()
                )
                if (uiState.categorias.isEmpty() && !uiState.cargando) {
                    Text(
                        text = stringResource(R.string.mant_sin_categorias),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                CampoFecha(
                    etiqueta = stringResource(R.string.reporte_col_fecha),
                    fecha = uiState.fecha,
                    onFechaCambia = acciones.onFecha,
                    forma = FormaPildora,
                    habilitado = editable,
                    fechaMaxima = LocalDate.now()
                )
                CampoTexto(
                    etiqueta = stringResource(R.string.mant_km),
                    valor = uiState.km,
                    onValorCambia = acciones.onKm,
                    marcadorDePosicion = stringResource(R.string.registro_km_nuevo_marcador),
                    forma = FormaPildora,
                    tipoTeclado = KeyboardType.Number,
                    habilitado = editable
                )
                if (uiState.kmMayorAlDelVehiculo) {
                    // El odómetro solo sube: primero se registra la lectura y
                    // después el servicio (migración 202609172200).
                    Text(
                        text = stringResource(
                            R.string.mant_km_mayor,
                            formatearKilometrosConUnidad(vehiculo.kmActual)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TransAndinaTheme.colores.estadoAviso
                    )
                    BotonSecundario(
                        texto = stringResource(R.string.vehiculo_opcion_registrar_km),
                        onClick = acciones.onRegistrarKilometraje,
                        modifier = Modifier.fillMaxWidth(),
                        habilitado = editable
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.mant_km_ayuda,
                            formatearKilometrosConUnidad(vehiculo.kmActual)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = TransAndinaTheme.colores.textoSecundario
                    )
                }
                CampoTexto(
                    etiqueta = stringResource(R.string.reporte_taller),
                    valor = uiState.taller,
                    onValorCambia = acciones.onTaller,
                    marcadorDePosicion = stringResource(R.string.mant_taller_marcador),
                    forma = FormaPildora,
                    habilitado = editable
                )
                CampoTexto(
                    etiqueta = stringResource(R.string.reporte_col_responsable),
                    valor = uiState.responsable,
                    onValorCambia = acciones.onResponsable,
                    marcadorDePosicion = stringResource(R.string.mant_responsable_marcador),
                    forma = FormaPildora,
                    habilitado = editable
                )
                CampoTexto(
                    etiqueta = stringResource(R.string.reporte_col_costo),
                    valor = uiState.costo,
                    onValorCambia = acciones.onCosto,
                    marcadorDePosicion = stringResource(R.string.mant_costo_marcador),
                    forma = FormaPildora,
                    tipoTeclado = KeyboardType.Decimal,
                    habilitado = editable
                )
                CampoTexto(
                    etiqueta = stringResource(R.string.mant_descripcion),
                    valor = uiState.descripcion,
                    onValorCambia = acciones.onDescripcion,
                    marcadorDePosicion = stringResource(R.string.mant_descripcion_marcador),
                    habilitado = editable,
                    lineas = 3
                )

                SeccionFotos(
                    fotos = uiState.fotos,
                    puedeAdjuntar = uiState.puedeAdjuntar && editable && !preparandoFotos,
                    preparando = preparandoFotos,
                    onAdjuntar = acciones.onAdjuntar,
                    onQuitar = acciones.onQuitarFoto
                )

                TarjetaEstimado(
                    categoria = uiState.categoria,
                    estimado = uiState.estimado
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
                    textoAccion = stringResource(R.string.guardar),
                    onAccion = acciones.onGuardar,
                    onCancelar = acciones.onCancelar,
                    textoCancelar = stringResource(R.string.cancelar),
                    accionHabilitada = !preparandoFotos,
                    cargando = uiState.guardando
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/** "Evidencia fotográfica" (Figma `67:133`): botón punteado y las fotos elegidas. */
@Composable
private fun SeccionFotos(
    fotos: List<FotoAdjunta>,
    puedeAdjuntar: Boolean,
    preparando: Boolean,
    onAdjuntar: () -> Unit,
    onQuitar: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.mant_fotos),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        BotonAdjuntar(
            texto = stringResource(R.string.mant_adjuntar),
            onClick = onAdjuntar,
            habilitado = puedeAdjuntar
        )
        Text(
            text = stringResource(R.string.mant_fotos_ayuda, fotos.size, MAXIMO_FOTOS),
            style = MaterialTheme.typography.labelSmall,
            color = TransAndinaTheme.colores.textoSecundario,
            modifier = Modifier.fillMaxWidth()
        )
        if (preparando) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterHorizontally),
                strokeWidth = 2.dp
            )
        }
        fotos.forEach { foto ->
            FilaFoto(
                nombre = foto.nombre,
                pesoBytes = foto.jpeg.size,
                miniatura = foto.miniatura,
                onQuitar = { onQuitar(foto.id) }
            )
        }
    }
}

@Composable
private fun FilaFoto(
    nombre: String,
    pesoBytes: Int,
    miniatura: Bitmap,
    onQuitar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = miniatura.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.mant_foto_peso, (pesoBytes + 1023) / 1024),
                style = MaterialTheme.typography.bodySmall,
                color = TransAndinaTheme.colores.textoSecundario
            )
        }
        IconButton(onClick = onQuitar) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.mant_quitar_foto),
                tint = TransAndinaTheme.colores.textoSecundario
            )
        }
    }
}

/** Cuándo tocaría el siguiente servicio de esta categoría si se guarda así. */
@Composable
private fun TarjetaEstimado(categoria: String?, estimado: ServicioEstimado?) {
    if (categoria == null) return
    TarjetaTransAndina {
        Text(
            text = stringResource(R.string.mant_estimado_titulo),
            style = MaterialTheme.typography.bodyMedium,
            color = TransAndinaTheme.colores.textoSecundario
        )
        Spacer(modifier = Modifier.height(4.dp))
        val km = estimado?.kmObjetivo?.let(::formatearKilometrosConUnidad)
        val fecha = estimado?.fechaObjetivo?.let(::formatearFecha)
        Text(
            text = when {
                km != null && fecha != null ->
                    stringResource(R.string.kilometraje_proximo_km_o_fecha, km, fecha)
                km != null -> stringResource(R.string.kilometraje_proximo_km, km)
                fecha != null -> stringResource(R.string.kilometraje_proximo_fecha, fecha)
                else -> stringResource(R.string.mant_estimado_sin_frecuencia)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private val vehiculoDeMuestra = Vehiculo(
    id = "1",
    placa = "SCD-3421",
    marca = "Nissan",
    modelo = "Frontier",
    anio = 2021,
    tipo = "liviano",
    kmActual = 492_400.0
)

@Preview(name = "Mantenimiento · km mayor al del vehículo", showBackground = true, heightDp = 900)
@Composable
private fun RegistrarMantenimientoKmMayorPreview() {
    TransAndinaFlotillaTheme {
        ContenidoRegistrarMantenimiento(
            uiState = RegistrarMantenimientoUiState(
                vehiculo = vehiculoDeMuestra,
                tipo = TipoMantenimiento.correctivo,
                categoria = "Frenos",
                km = "495000",
                taller = "Taller Norte"
            ),
            preparandoFotos = false,
            acciones = AccionesMantenimiento()
        )
    }
}

@Preview(name = "Registrar mantenimiento", showBackground = true, heightDp = 1300)
@Composable
private fun RegistrarMantenimientoPreview() {
    val miniatura = createBitmap(48, 48)
    TransAndinaFlotillaTheme {
        ContenidoRegistrarMantenimiento(
            uiState = RegistrarMantenimientoUiState(
                vehiculo = vehiculoDeMuestra,
                frecuencias = listOf(
                    FrecuenciaMantenimiento("f1", "liviano", "Cambio de aceite", 5_000.0, 180),
                    FrecuenciaMantenimiento("f2", "liviano", "Otro", null, null)
                ),
                tipo = TipoMantenimiento.preventivo,
                categoria = "Cambio de aceite",
                fecha = LocalDate.of(2026, 9, 17),
                km = "492400",
                taller = "Taller Central",
                costo = "45 000",
                fotos = listOf(FotoAdjunta("IMG_2031.jpg", ByteArray(310_000), miniatura))
            ),
            preparandoFotos = false,
            acciones = AccionesMantenimiento()
        )
    }
}
