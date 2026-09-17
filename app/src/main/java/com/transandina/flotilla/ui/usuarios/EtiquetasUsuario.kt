package com.transandina.flotilla.ui.usuarios

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.transandina.flotilla.R
import com.transandina.flotilla.data.model.EstadoCuenta
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.ui.components.ChipEstado
import com.transandina.flotilla.ui.components.NivelEstado

// Textos y chips compartidos por las pantallas de usuarios.

@Composable
internal fun etiquetaRol(rol: RolUsuario): String = when (rol) {
    RolUsuario.conductor -> stringResource(R.string.registro_rol_conductor)
    RolUsuario.mecanico -> stringResource(R.string.registro_rol_mecanico)
    RolUsuario.encargado -> stringResource(R.string.rol_encargado)
}

@Composable
internal fun etiquetaEstadoCuenta(estado: EstadoCuenta): String = when (estado) {
    EstadoCuenta.activo -> stringResource(R.string.cuenta_activo)
    EstadoCuenta.suspendido -> stringResource(R.string.cuenta_suspendido)
    EstadoCuenta.desactivado -> stringResource(R.string.cuenta_desactivado)
}

internal fun nivelEstadoCuenta(estado: EstadoCuenta): NivelEstado = when (estado) {
    EstadoCuenta.activo -> NivelEstado.OK
    EstadoCuenta.suspendido -> NivelEstado.AVISO
    EstadoCuenta.desactivado -> NivelEstado.NEUTRO
}

/** "Activo" en verde, "Suspendido" en ámbar y "Desactivado" en gris (Figma `102:258`). */
@Composable
internal fun ChipEstadoCuenta(estado: EstadoCuenta, modifier: Modifier = Modifier) {
    ChipEstado(
        texto = etiquetaEstadoCuenta(estado),
        nivel = nivelEstadoCuenta(estado),
        modifier = modifier
    )
}
