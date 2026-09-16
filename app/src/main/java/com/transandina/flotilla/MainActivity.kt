package com.transandina.flotilla

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.di.SupabaseProvider
import com.transandina.flotilla.ui.auth.LoginScreen
import com.transandina.flotilla.ui.auth.NuevaContrasenaScreen
import com.transandina.flotilla.ui.auth.RecuperarContrasenaScreen
import com.transandina.flotilla.ui.auth.RegistroScreen
import com.transandina.flotilla.ui.components.TransAndinaBottomBar
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
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme
import com.transandina.flotilla.ui.kilometraje.KilometrajeScreen
import com.transandina.flotilla.ui.vehiculo.PestanaVehiculo
import com.transandina.flotilla.ui.vehiculo.VehiculoDetalleScreen
import com.transandina.flotilla.ui.vehiculo.VehiculoHubScreen
import io.github.jan.supabase.auth.handleDeeplinks

private const val RUTA_LOGIN = "login"
private const val RUTA_REGISTRO = "registro"
private const val RUTA_RECUPERAR_CONTRASENA = "recuperar-contrasena"
private const val RUTA_NUEVA_CONTRASENA = "nueva-contrasena"
private const val ARG_VEHICULO_ID = "vehiculoId"
private const val ARG_PESTANA = "pestana"
private const val RUTA_VEHICULO_DETALLE = "vehiculo-detalle/{$ARG_VEHICULO_ID}/{$ARG_PESTANA}"
private const val RUTA_REGISTRAR_KILOMETRAJE = "registrar-kilometraje/{$ARG_VEHICULO_ID}"

/** Avisa al detalle del vehículo que vuelva a leer los datos tras guardar. */
private const val CLAVE_KM_REGISTRADO = "km_registrado"

private fun rutaVehiculoDetalle(vehiculoId: String, pestana: PestanaVehiculo) =
    "vehiculo-detalle/$vehiculoId/${pestana.name}"

private fun rutaRegistrarKilometraje(vehiculoId: String) =
    "registrar-kilometraje/$vehiculoId"

private val RUTAS_SIN_BARRA_INFERIOR = setOf(
    RUTA_LOGIN, RUTA_REGISTRO, RUTA_RECUPERAR_CONTRASENA, RUTA_NUEVA_CONTRASENA
)

class MainActivity : ComponentActivity() {

    // Estado de Compose fuera de una función @Composable: es válido, y
    // permite que MainActivity (que no es Composable) le avise a la UI
    // cuando llega el deep link de recuperación de contraseña, sin pasar
    // por un ViewModel ni un evento manual.
    private var deepLinkRecuperacion by mutableStateOf(false)

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
        manejarPosibleDeepLink(intent)
        setContent {
            TransAndinaFlotillaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        deepLinkRecuperacion = deepLinkRecuperacion,
                        onDeepLinkConsumido = { deepLinkRecuperacion = false }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        manejarPosibleDeepLink(intent)
    }

    /**
     * Si el intent viene del correo de recuperación (transandina://reset-password),
     * el SDK de Supabase importa la sesión de recuperación automáticamente.
     * Si no es ese tipo de intent, esto no hace nada.
     */
    private fun manejarPosibleDeepLink(intent: Intent) {
        SupabaseProvider.client.handleDeeplinks(intent) {
            deepLinkRecuperacion = true
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
private fun AppNavigation(
    deepLinkRecuperacion: Boolean,
    onDeepLinkConsumido: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    var rolActual by remember { mutableStateOf<RolUsuario?>(null) }
    val mostrarBarraInferior = rutaActual !in RUTAS_SIN_BARRA_INFERIOR && rolActual != null
    val itemsMenu = rolActual?.let(::itemsParaRol) ?: emptyList()

    // En cuanto llega el deep link de recuperación, saltamos directo a la
    // pantalla de nueva contraseña, sin importar en qué pantalla estaba
    // la app en ese momento.
    LaunchedEffect(deepLinkRecuperacion) {
        if (deepLinkRecuperacion) {
            navController.navigate(RUTA_NUEVA_CONTRASENA) {
                launchSingleTop = true
            }
            onDeepLinkConsumido()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (mostrarBarraInferior) {
                TransAndinaBottomBar(
                    items = itemsMenu,
                    rutaActual = rutaActual,
                    onSeleccionar = { item ->
                        navController.navigate(item.ruta) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
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
                    },
                    onRegistrarse = { navController.navigate(RUTA_REGISTRO) },
                    onRecuperarContrasena = { navController.navigate(RUTA_RECUPERAR_CONTRASENA) }
                )
            }
            composable(RUTA_REGISTRO) {
                RegistroScreen(
                    onRegistroExitoso = { rol ->
                        rolActual = rol
                        navController.navigate(rutaInicialParaRol(rol)) {
                            popUpTo(RUTA_LOGIN) { inclusive = true }
                        }
                    },
                    onIrALogin = { navController.popBackStack() }
                )
            }
            composable(RUTA_RECUPERAR_CONTRASENA) {
                RecuperarContrasenaScreen(
                    onVolverALogin = { navController.popBackStack() }
                )
            }
            composable(RUTA_NUEVA_CONTRASENA) {
                NuevaContrasenaScreen(
                    onContrasenaActualizada = {
                        rolActual = null
                        navController.navigate(RUTA_LOGIN) {
                            popUpTo(0)
                        }
                    }
                )
            }
            composable(BottomNavItem.Inicio.ruta) {
                rolActual?.let { HomeScreen(rol = it) }
            }
            composable(BottomNavItem.Vehiculo.ruta) {
                VehiculoHubScreen(
                    onAbrirDetalle = { vehiculoId, pestana ->
                        navController.navigate(rutaVehiculoDetalle(vehiculoId, pestana))
                    },
                    onRegistrarKilometraje = { vehiculoId ->
                        navController.navigate(rutaRegistrarKilometraje(vehiculoId))
                    }
                )
            }
            composable(RUTA_VEHICULO_DETALLE) { entrada ->
                val vehiculoId = entrada.arguments?.getString(ARG_VEHICULO_ID).orEmpty()
                val pestana = entrada.arguments?.getString(ARG_PESTANA)
                    ?.let { runCatching { PestanaVehiculo.valueOf(it) }.getOrNull() }
                    ?: PestanaVehiculo.INFORMACION
                // Cada vez que se guarda un kilometraje, este contador cambia y
                // el detalle vuelve a leer el vehículo con el km ya actualizado.
                val tokenRecarga by entrada.savedStateHandle
                    .getStateFlow(CLAVE_KM_REGISTRADO, 0)
                    .collectAsState()

                VehiculoDetalleScreen(
                    vehiculoId = vehiculoId,
                    pestanaInicial = pestana,
                    tokenRecarga = tokenRecarga,
                    onAtras = { navController.popBackStack() },
                    onRegistrarKilometraje = { id ->
                        navController.navigate(rutaRegistrarKilometraje(id))
                    }
                )
            }
            composable(RUTA_REGISTRAR_KILOMETRAJE) { entrada ->
                KilometrajeScreen(
                    vehiculoId = entrada.arguments?.getString(ARG_VEHICULO_ID),
                    onAtras = { navController.popBackStack() },
                    onRegistroExitoso = {
                        val anterior = navController.previousBackStackEntry?.savedStateHandle
                        anterior?.set(
                            CLAVE_KM_REGISTRADO,
                            (anterior.get<Int>(CLAVE_KM_REGISTRADO) ?: 0) + 1
                        )
                        navController.popBackStack()
                    }
                )
            }
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