package com.transandina.flotilla.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.transandina.flotilla.R

// Escala tipográfica del Figma (docs/ADAPTACION_MOVIL.md §2).
// Roboto es la fuente del sistema en Android, así que FontFamily.Default ya es
// Roboto y no hace falta empaquetarla. La única fuente propia es la del
// logotipo.

/** Rosarivo (licencia OFL), usada únicamente en el logotipo "TransAndina". */
val Rosarivo = FontFamily(Font(R.font.rosarivo_regular, FontWeight.Normal))

/** Logotipo del login: Rosarivo Regular 48. */
val EstiloLogotipo = TextStyle(
    fontFamily = Rosarivo,
    fontWeight = FontWeight.Normal,
    fontSize = 48.sp,
    lineHeight = 62.sp
)

val Typography = Typography(
    // Números grandes (KPIs, kilometraje actual).
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp
    ),
    // Placa destacada.
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    // Título del encabezado de pantalla.
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    // Título de tarjeta.
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // Texto de inputs y botones.
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // Texto general.
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp
    ),
    // Detalles.
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    // Chips de estado y pestañas.
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Fechas y notas al pie.
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)
