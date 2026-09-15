package com.transandina.flotilla.ui.theme

import androidx.compose.ui.graphics.Color

// Tokens de color del Figma "Prototipo" (ver docs/ADAPTACION_MOVIL.md §2).
// Este es el único archivo donde se declaran colores: las pantallas los usan
// a través de MaterialTheme.colorScheme o de TransAndinaTheme.colores.

/** Encabezados, fondo de auth, texto principal, pestaña activa. */
val Navy = Color(0xFF0C2340)

/** Botón primario de auth ("Ingresar", "Enviar", "Crear cuenta") y "Editar datos". */
val Naranja = Color(0xFFC45C26)

/** Solo el logotipo "TransAndina". */
val NaranjaLogo = Color(0xFFAA500F)

/** Botones de confirmar ("Guardar", "Registrar"). */
val Verde = Color(0xFF287721)

/** Botón secundario ("Cancelar"). */
val GrisBoton = Color(0xFF4A4A4A)

/** Acción destructiva ("Cerrar sesión"). */
val Rojo = Color(0xFFAE2525)

/** Fondo de las pantallas con sesión iniciada. */
val Fondo = Color(0xFFD9D9D9)

/** Tarjetas, inputs y barra inferior. */
val Superficie = Color(0xFFFFFFFF)

/** Filas / ítems dentro de una tarjeta. */
val SuperficieSuave = Color(0xFFF7FAFC)

/** Etiquetas y subtítulos. */
val TextoSecundario = Color(0xFF737D8C)

/** Líneas de detalle en tarjetas. */
val TextoTerciario = Color(0xFF6B6B6B)

/**
 * Texto de ayuda en inputs. El Figma usa la variable
 * `Colors/Primary/Dark grey/600` (#9EA1A8), no el #999999 de la tabla.
 */
val Placeholder = Color(0xFF9EA1A8)

/** "Vigente", "Al día", "Activo". */
val EstadoOk = Color(0xFF1C703D)

/** "Por vencer", "Próximo", "Suspendido". */
val EstadoAviso = Color(0xFFB8780D)

/** "Vencido", "Atrasado", "Crítica", "Desactivado". */
val EstadoCritico = Color(0xFFAD2626)

/** "Informativa". */
val EstadoInfo = Navy

/** Opacidad del fondo de los chips de estado sobre su color pleno. */
const val ALFA_FONDO_ESTADO = 0.15f

/** Los chips informativos van un poco más suaves que el resto. */
const val ALFA_FONDO_ESTADO_INFO = 0.10f
