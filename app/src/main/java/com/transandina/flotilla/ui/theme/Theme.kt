package com.transandina.flotilla.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// La app es solo tema claro y sin dynamic color: los colores de marca
// (navy y naranja) no pueden cambiar según el fondo de pantalla del teléfono.
private val EsquemaClaro = lightColorScheme(
    primary = Navy,
    onPrimary = Superficie,
    primaryContainer = Navy,
    onPrimaryContainer = Superficie,
    secondary = Naranja,
    onSecondary = Superficie,
    secondaryContainer = Naranja,
    onSecondaryContainer = Superficie,
    tertiary = Verde,
    onTertiary = Superficie,
    background = Fondo,
    onBackground = Navy,
    surface = Superficie,
    onSurface = Navy,
    surfaceVariant = SuperficieSuave,
    onSurfaceVariant = TextoSecundario,
    outline = Placeholder,
    outlineVariant = Placeholder,
    error = Rojo,
    onError = Superficie,
    errorContainer = Rojo,
    onErrorContainer = Superficie
)

/**
 * Colores del Figma que Material 3 no cubre con su esquema estándar.
 * Se usan como `TransAndinaTheme.colores.verde`, etc.
 */
@Immutable
data class ColoresTransAndina(
    val naranjaLogo: Color,
    val verde: Color,
    val grisBoton: Color,
    val rojo: Color,
    val superficieSuave: Color,
    val textoSecundario: Color,
    val textoTerciario: Color,
    val placeholder: Color,
    val estadoOk: Color,
    val estadoAviso: Color,
    val estadoCritico: Color,
    val estadoInfo: Color
)

private val ColoresClaros = ColoresTransAndina(
    naranjaLogo = NaranjaLogo,
    verde = Verde,
    grisBoton = GrisBoton,
    rojo = Rojo,
    superficieSuave = SuperficieSuave,
    textoSecundario = TextoSecundario,
    textoTerciario = TextoTerciario,
    placeholder = Placeholder,
    estadoOk = EstadoOk,
    estadoAviso = EstadoAviso,
    estadoCritico = EstadoCritico,
    estadoInfo = EstadoInfo
)

private val LocalColoresTransAndina = staticCompositionLocalOf { ColoresClaros }

/** Acceso a los colores extendidos dentro de cualquier composable del tema. */
object TransAndinaTheme {
    val colores: ColoresTransAndina
        @Composable
        @ReadOnlyComposable
        get() = LocalColoresTransAndina.current
}

@Composable
fun TransAndinaFlotillaTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalColoresTransAndina provides ColoresClaros) {
        MaterialTheme(
            colorScheme = EsquemaClaro,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
