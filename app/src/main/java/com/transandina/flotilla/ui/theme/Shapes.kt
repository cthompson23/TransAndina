package com.transandina.flotilla.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Formas del Figma (docs/ADAPTACION_MOVIL.md §2):
// botones e inputs de auth 8 dp, tarjetas 12 dp, contenedores grandes 16 dp.
val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

/** Inputs de formularios con sesión iniciada y chips de estado: píldora. */
val FormaPildora = RoundedCornerShape(percent = 50)
