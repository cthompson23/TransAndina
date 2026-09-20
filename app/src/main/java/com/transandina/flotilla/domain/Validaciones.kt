package com.transandina.flotilla.domain

/**
 * Validaciones de formato de los datos personales. Viven aquí, y no en el
 * ViewModel, para poder probarlas sin Android y para que el registro público
 * y "Registrar administrador" usen exactamente las mismas reglas.
 *
 * La base también valida (correo único, cédula única), pero ahí el error
 * llega como un mensaje genérico; esto permite decir qué campo está mal
 * antes de llamar a Supabase.
 */

// Sin ser el RFC completo: algo antes de la arroba, algo después y al menos
// un punto con dos letras al final. Alcanza para atajar los errores de dedo.
private val CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

/** Cédula de Costa Rica: solo dígitos, como pide el formulario. */
private val CEDULA = Regex("^\\d{9,12}$")

/** Lo que se le quita a un teléfono antes de contar sus dígitos. */
private val SEPARADORES_TELEFONO = Regex("[\\s()+-]")

fun esCorreoValido(valor: String): Boolean = CORREO.matches(valor.trim())

fun esCedulaValida(valor: String): Boolean = CEDULA.matches(valor.trim())

/**
 * Acepta separadores y el código de país (8 dígitos en Costa Rica, más si
 * viene con el 506 adelante), pero no letras.
 */
fun esTelefonoValido(valor: String): Boolean {
    val soloDigitos = valor.trim().replace(SEPARADORES_TELEFONO, "")
    return soloDigitos.length in 8..15 && soloDigitos.all(Char::isDigit)
}
