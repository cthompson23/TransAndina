package com.transandina.flotilla.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.ui.navigation.BottomNavItem
import com.transandina.flotilla.ui.navigation.itemsParaRol
import com.transandina.flotilla.ui.theme.EstiloLogotipo
import com.transandina.flotilla.ui.theme.FormaPildora
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.theme.TransAndinaTheme
import java.time.LocalDate

/**
 * Catálogo con todos los componentes juntos, solo para revisarlos de un
 * vistazo desde Android Studio. No se usa en la app.
 */
@Preview(showBackground = true, heightDp = 1800, widthDp = 380)
@Composable
private fun CatalogoComponentesPreview() {
    TransAndinaFlotillaTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                TransAndinaTopBar(titulo = "Catálogo de componentes")
                TransAndinaTopBar(
                    titulo = "Mi vehículo",
                    subtitulo = "SCD-3421 · Nissan Frontier",
                    onAtras = {}
                )

                PestanasSegmentadas(
                    pestanas = listOf("Información", "Historial", "Kilometraje", "Documentos"),
                    indiceSeleccionado = 3,
                    onSeleccionar = {}
                )

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TransAndina",
                        style = EstiloLogotipo,
                        color = TransAndinaTheme.colores.naranjaLogo
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TarjetaKpi("Activos", "24", Modifier.weight(1f))
                        TarjetaKpi("Mant. próximos", "5", Modifier.weight(1f))
                        TarjetaKpi("Docs. por vencer", "3", Modifier.weight(1f))
                    }

                    TarjetaTransAndina {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SCD-3421", style = MaterialTheme.typography.headlineSmall)
                            ChipEstado("Atrasado", NivelEstado.CRITICO)
                        }
                        Text(
                            text = "Nissan Frontier · Carlos Fernández",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TransAndinaTheme.colores.textoTerciario
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipEstado("Vigente", NivelEstado.OK)
                        ChipEstado("Por vencer", NivelEstado.AVISO)
                        ChipEstado("Vencido", NivelEstado.CRITICO)
                        ChipEstado("Informativa", NivelEstado.INFO)
                    }

                    TarjetaOpcionMenu(
                        titulo = "Registrar mantenimiento",
                        descripcion = "Preventivo o correctivo, con evidencia",
                        onClick = {}
                    )

                    TarjetaTransAndina {
                        DatoEtiquetado(etiqueta = "Capacidad", valor = "1,1 toneladas")
                    }

                    CampoTexto(
                        etiqueta = "Correo electrónico",
                        valor = "",
                        onValorCambia = {},
                        marcadorDePosicion = "ejemplo@correo.com"
                    )
                    CampoTexto(
                        etiqueta = "Responsable",
                        valor = "Carlos Fernández",
                        onValorCambia = {},
                        forma = FormaPildora
                    )
                    CampoContrasena(
                        etiqueta = "Contraseña",
                        valor = "secreta",
                        onValorCambia = {}
                    )
                    CampoSeleccion(
                        etiqueta = "Tipo",
                        seleccion = "Preventivo",
                        opciones = listOf("Preventivo", "Correctivo"),
                        onSeleccionar = {}
                    )
                    CampoFecha(
                        etiqueta = "Fecha",
                        fecha = LocalDate.of(2026, 8, 25),
                        onFechaCambia = {}
                    )

                    BotonPrimario("Ingresar", {}, Modifier.fillMaxWidth())
                    BotonConfirmar("Guardar", {}, Modifier.fillMaxWidth())
                    BotonSecundario("Cancelar", {}, Modifier.fillMaxWidth())
                    BotonDestructivo("Cerrar sesión", {}, Modifier.fillMaxWidth())
                    FilaBotonesFormulario(
                        textoAccion = "Guardar",
                        onAccion = {},
                        onCancelar = {}
                    )

                    EstadoVacio(mensaje = "Todavía no hay mantenimientos registrados")
                }

                TransAndinaBottomBar(
                    items = itemsParaRol(RolUsuario.conductor),
                    rutaActual = BottomNavItem.Inicio.ruta,
                    onSeleccionar = {}
                )
            }
        }
    }
}
