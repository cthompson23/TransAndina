package com.transandina.flotilla.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.ui.navigation.BottomNavItem
import com.transandina.flotilla.ui.navigation.itemsParaRol
import com.transandina.flotilla.ui.theme.TransAndinaFlotillaTheme

/**
 * Barra inferior blanca con íconos navy y el ítem activo en naranja, sin
 * etiquetas (Figma `1:1225`). Solo dibuja: quién navega es MainActivity.
 */
@Composable
fun TransAndinaBottomBar(
    items: List<BottomNavItem>,
    rutaActual: String?,
    onSeleccionar: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = rutaActual == item.ruta,
                onClick = { onSeleccionar(item) },
                icon = {
                    Icon(
                        imageVector = item.icono,
                        // Hoy los ítems no tienen etiqueta; queda pendiente
                        // darles una descripción para lectores de pantalla.
                        contentDescription = item.etiqueta.ifBlank { null }
                    )
                },
                label = null,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransAndinaBottomBarPreview() {
    TransAndinaFlotillaTheme {
        TransAndinaBottomBar(
            items = itemsParaRol(RolUsuario.conductor),
            rutaActual = BottomNavItem.Inicio.ruta,
            onSeleccionar = {}
        )
    }
}
