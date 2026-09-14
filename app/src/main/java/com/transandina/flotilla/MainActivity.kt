package com.transandina.flotilla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.ui.auth.LoginScreen
import com.transandina.flotilla.ui.encargado.EstadoScreen
import com.transandina.flotilla.ui.encargado.HistorialScreen
import com.transandina.flotilla.ui.encargado.ReasignacionScreen
import com.transandina.flotilla.ui.encargado.ReportesScreen
import com.transandina.flotilla.ui.encargado.UsuariosScreen
import com.transandina.flotilla.ui.home.HomeScreen
import com.transandina.flotilla.ui.mantenimiento.MantenimientoScreen
import com.transandina.flotilla.ui.navigation.BottomNavItem
import com.transandina.flotilla.ui.navigation.itemsParaRol
import com.transandina.flotilla.ui.notificaciones.NotificacionesScreen
import com.transandina.flotilla.ui.perfil.PerfilScreen
import com.transandina.flotilla.ui.vehiculo.VehiculoScreen

private const val RUTA_LOGIN = "login"

// Misma paleta que LoginScreen / PerfilScreen / HomeScreen — pendiente
// moverla a un Color.kt compartido (ui/theme/Color.kt) para no repetirla.
// El fondo de las pantallas es gris claro; la barra inferior se mantiene
// en navy, igual que el encabezado "TransAndina" de Home y Perfil.
private val PageBackground = Color(0xFFD9D9D9)
private val NavyBarra = Color(0xFF0F2135)
private val OrangeAccent = Color(0xFFBB6B2E)
private val IconoInactivo = Color(0xFF7C8CA3)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Deja que el contenido dibuje detrás de la barra de estado y la
        // de gestos (en vez de que Android les reserve un espacio blanco
        // aparte) y fuerza íconos claros, porque nuestro fondo siempre es
        // oscuro sin importar el tema del sistema.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

/** A qué ruta cae cada rol justo después de iniciar sesión. */
private fun rutaInicialParaRol(rol: RolUsuario): String = when (rol) {
    RolUsuario.conductor -> BottomNavItem.Inicio.ruta
    RolUsuario.mecanico -> BottomNavItem.Inicio.ruta
    RolUsuario.encargado -> BottomNavItem.Estado.ruta
}

/**
 * Un solo NavHost para toda la app. El rol del usuario se guarda aquí
 * (se conoce recién al hacer login) y determina qué ítems muestra la
 * barra inferior y a qué pantalla se entra primero.
 */
@Composable
private fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    var rolActual by remember { mutableStateOf<RolUsuario?>(null) }
    val mostrarBarraInferior = rutaActual != RUTA_LOGIN && rolActual != null
    val itemsMenu = rolActual?.let(::itemsParaRol) ?: emptyList()

    Scaffold(
        containerColor = PageBackground,
        bottomBar = {
            if (mostrarBarraInferior) {
                NavigationBar(
                    containerColor = NavyBarra,
                    tonalElevation = 0.dp
                ) {
                    itemsMenu.forEach { item ->
                        val seleccionado = rutaActual == item.ruta
                        NavigationBarItem(
                            selected = seleccionado,
                            onClick = {
                                navController.navigate(item.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(imageVector = item.icono, contentDescription = item.etiqueta)
                            },
                            label = if (item.etiqueta.isNotBlank()) {
                                { Text(item.etiqueta) }
                            } else {
                                null
                            },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = OrangeAccent,
                                indicatorColor = OrangeAccent,
                                unselectedIconColor = IconoInactivo,
                                unselectedTextColor = IconoInactivo
                            )
                        )
                    }
                }
            }
        }
    ) { paddingInterno ->
        NavHost(
            navController = navController,
            startDestination = RUTA_LOGIN,
            modifier = Modifier.padding(paddingInterno)
        ) {
            composable(RUTA_LOGIN) {
                LoginScreen(
                    onLoginExitoso = { rol ->
                        rolActual = rol
                        navController.navigate(rutaInicialParaRol(rol)) {
                            popUpTo(RUTA_LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            composable(BottomNavItem.Inicio.ruta) {
                rolActual?.let { HomeScreen(rol = it) }
            }
            composable(BottomNavItem.Vehiculo.ruta) { VehiculoScreen() }
            composable(BottomNavItem.Mantenimiento.ruta) { MantenimientoScreen() }
            composable(BottomNavItem.Notificaciones.ruta) { NotificacionesScreen() }
            composable(BottomNavItem.Perfil.ruta) {
                PerfilScreen(
                    onSesionCerrada = {
                        rolActual = null
                        navController.navigate(RUTA_LOGIN) {
                            popUpTo(0)
                        }
                    }
                )
            }

            // Exclusivas del encargado de flota
            composable(BottomNavItem.Estado.ruta) { EstadoScreen() }
            composable(BottomNavItem.Historial.ruta) { HistorialScreen() }
            composable(BottomNavItem.Reportes.ruta) { ReportesScreen() }
            composable(BottomNavItem.Usuarios.ruta) { UsuariosScreen() }
            composable(BottomNavItem.Reasignacion.ruta) { ReasignacionScreen() }
        }
    }
}