package com.transandina.flotilla.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesTest {

    @Test
    fun `acepta correos con la forma habitual`() {
        assertTrue(esCorreoValido("ana@transandina.cr"))
        assertTrue(esCorreoValido("ana.maria+flotilla@correo.co.cr"))
        assertTrue(esCorreoValido("  ana@correo.com  "))
    }

    @Test
    fun `rechaza correos sin arroba, sin dominio o con espacios`() {
        assertFalse(esCorreoValido("ana.transandina.cr"))
        assertFalse(esCorreoValido("ana@"))
        assertFalse(esCorreoValido("ana@correo"))
        assertFalse(esCorreoValido("ana @correo.com"))
        assertFalse(esCorreoValido(""))
    }

    @Test
    fun `la cedula son solo digitos, entre nueve y doce`() {
        assertTrue(esCedulaValida("112340567"))
        assertTrue(esCedulaValida("155812345678"))
        assertFalse(esCedulaValida("1-1234-0567"))
        assertFalse(esCedulaValida("12345678"))
        assertFalse(esCedulaValida("1234567890123"))
        assertFalse(esCedulaValida("11234A567"))
    }

    @Test
    fun `el telefono admite separadores y codigo de pais`() {
        assertTrue(esTelefonoValido("88887777"))
        assertTrue(esTelefonoValido("8888 7777"))
        assertTrue(esTelefonoValido("+506 8888-7777"))
        assertTrue(esTelefonoValido("(506) 8888 7777"))
    }

    @Test
    fun `el telefono rechaza letras o muy pocos digitos`() {
        assertFalse(esTelefonoValido("8888777"))
        assertFalse(esTelefonoValido("8888777A"))
        assertFalse(esTelefonoValido(""))
    }
}
