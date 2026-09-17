package com.transandina.flotilla.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.People
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.RolUsuario

/**
 * Ítem de la barra inferior. La etiqueta no se dibuja (el Figma `1:1225` solo
 * muestra íconos), pero se usa como descripción para los lectores de pantalla.
 */
sealed class BottomNavItem(
    val ruta: String,
    @StringRes val etiqueta: Int,
    val icono: ImageVector
) {
    object Inicio : BottomNavItem("inicio", R.string.nav_inicio, Icons.Filled.Home)
    object Vehiculo : BottomNavItem("vehiculo", R.string.nav_vehiculo, Icons.Filled.DirectionsCar)
    object Mantenimiento : BottomNavItem("mantenimiento", R.string.nav_mantenimiento, Icons.Filled.Build)
    object Notificaciones : BottomNavItem("notificaciones", R.string.nav_notificaciones, Icons.Filled.Notifications)
    object Perfil : BottomNavItem("perfil", R.string.nav_perfil, Icons.Filled.Person)

    // Encargado (docs/ADAPTACION_MOVIL.md §5). "Historial por vehículo" y
    // "Reasignación" ya no son pestañas: se entra desde el detalle del vehículo.
    object Estado : BottomNavItem("estado", R.string.nav_flotilla, Icons.Filled.Dashboard)
    object AlertasFlotilla : BottomNavItem("alertas-flotilla", R.string.nav_alertas, Icons.Filled.NotificationImportant)
    object Reportes : BottomNavItem("reportes", R.string.nav_reportes, Icons.Filled.Assessment)
    object Usuarios : BottomNavItem("usuarios", R.string.nav_usuarios, Icons.Filled.People)
}

/**
 * Cada rol ve un subconjunto distinto de pestañas. Las rutas siguen
 * todas registradas en el NavHost (ver MainActivity) — esto solo
 * controla qué aparece en la barra inferior, no qué existe.
 *
 * - conductor: tiene un vehículo propio asignado, así que ve su detalle.
 * - mecánico: no tiene "un" vehículo, trabaja sobre cualquiera, así que
 *   no tiene sentido la pestaña Vehículo individual.
 * - encargado: gestiona la flotilla completa y no registra mantenimientos
 *   él mismo: Flotilla · Alertas · Reportes · Usuarios · Perfil.
 */
fun itemsParaRol(rol: RolUsuario): List<BottomNavItem> = when (rol) {
    RolUsuario.conductor -> listOf(
        BottomNavItem.Inicio,
        BottomNavItem.Vehiculo,
        BottomNavItem.Mantenimiento,
        BottomNavItem.Notificaciones,
        BottomNavItem.Perfil
    )
    RolUsuario.mecanico -> listOf(
        BottomNavItem.Inicio,
        BottomNavItem.Mantenimiento,
        BottomNavItem.Notificaciones,
        BottomNavItem.Perfil
    )
    RolUsuario.encargado -> listOf(
        BottomNavItem.Estado,
        BottomNavItem.AlertasFlotilla,
        BottomNavItem.Reportes,
        BottomNavItem.Usuarios,
        BottomNavItem.Perfil
    )
}
