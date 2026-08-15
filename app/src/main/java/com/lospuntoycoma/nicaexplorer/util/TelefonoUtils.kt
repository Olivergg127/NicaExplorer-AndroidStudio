package com.lospuntoycoma.nicaexplorer.util

/**
 * Utilidades para normalizar números de teléfono y construir enlaces de WhatsApp.
 */
object TelefonoUtils {

    private const val NICARAGUA_COUNTRY_CODE = "505"
    private const val NICARAGUA_LOCAL_LENGTH = 8

    /**
     * Elimina espacios, guiones y el signo "+", dejando solo dígitos.
     */
    fun normalizarNumero(raw: String): String = raw.filter { it.isDigit() }

    /**
     * Normaliza el número de WhatsApp:
     * - si es un número local de 8 dígitos, le antepone 505;
     * - si ya viene con 505, no lo duplica;
     * - cualquier otro caso se deja tal cual.
     * Ejemplo: "8722-5057" -> "50587225057"
     */
    fun buildWhatsAppNumber(raw: String): String {
        val digits = normalizarNumero(raw)
        if (digits.isEmpty()) return ""
        return if (digits.length == NICARAGUA_LOCAL_LENGTH &&
            !digits.startsWith(NICARAGUA_COUNTRY_CODE)
        ) {
            NICARAGUA_COUNTRY_CODE + digits
        } else {
            digits
        }
    }

    /**
     * Construye el enlace de WhatsApp en formato https://wa.me/<numero>.
     * Ejemplo: "8722-5057" -> https://wa.me/50587225057
     */
    fun buildWhatsAppLink(raw: String): String {
        val number = buildWhatsAppNumber(raw)
        if (number.isBlank()) return ""
        return "https://wa.me/$number"
    }
}
