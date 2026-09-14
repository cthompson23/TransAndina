package com.transandina.flotilla.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import com.transandina.flotilla.data.model.RolUsuario

sealed class BottomNavItem(val ruta: String, val etiqueta: String, val icono: ImageVector) {
    object Inicio : BottomNavItem("inicio", "", Icons.Filled.Home)
    object Vehiculo : BottomNavItem("vehiculo", "", Icons.Filled.DirectionsCar)
    object Mantenimiento : BottomNavItem("mantenimiento", "", Icons.Filled.Build)
    object Notificaciones : BottomNavItem("notificaciones", "", Icons.Filled.Notifications)
    object Perfil : BottomNavItem("perfil", "", Icons.Filled.Person)
    object Estado : BottomNavItem("estado", "", Icons.Filled.Dashboard)
    object Historial : BottomNavItem("historial", "", Icons.Filled.History)
    object Reportes : BottomNavItem("resportes", "", Icons.Filled.Assessment)
    object Usuarios : BottomNavItem("usuarios", "", Icons.Filled.People)
    object Reasignacion : BottomNavItem("reasignacion", "", Icons.Filled.SwapHoriz)
}

/**
 * Cada rol ve un subconjunto distinto de pestañas. Las rutas siguen
 * todas registradas en el NavHost (ver MainActivity) — esto solo
 * controla qué aparece en la barra inferior, no qué existe.
 *
 * - conductor: tiene un vehículo propio asignado, así que ve su detalle.
 * - mecánico: no tiene "un" vehículo, trabaja sobre cualquiera, así que
 *   no tiene sentido la pestaña Vehículo individual.
 * - encargado: gestiona la flotilla completa, no registra mantenimientos
 *   él mismo, así que tampoco ve esa pestaña (por ahora).
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
        BottomNavItem.Historial,
        BottomNavItem.Reportes,
        BottomNavItem.Usuarios,
        BottomNavItem.Reasignacion,
        BottomNavItem.Perfil
    )
}
