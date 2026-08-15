package com.lospuntoycoma.nicaexplorer.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RECUPERAR_CONTRASENA = "recuperar_contrasena"
    const val HOME = "home"
    const val CITY_SELECTION = "city_selection"
    const val CATALOG = "catalog/{cityId}?monumentId={monumentId}"
    const val PROFILE = "profile"
    const val ASSISTANT = "assistant"
    const val ADMIN_PANEL = "admin_panel"
    const val AR_PLACEHOLDER = "ar_placeholder/{cityId}/{monumentId}"

    const val EDITAR_PERFIL = "editar_perfil"
    const val LUGARES_GUARDADOS = "lugares_guardados"
    const val HISTORIAL = "historial"
    const val CONFIGURACION = "configuracion"
    const val ACERCA_DE = "acerca_de"
    const val COMERCIOS = "comercios"
    const val COMERCIO_DETALLE = "comercio_detalle/{comercioId}"

    fun catalog(cityId: String) = "catalog/$cityId"
    fun catalogWithMonument(cityId: String, monumentId: String) = "catalog/$cityId?monumentId=$monumentId"
    fun arPlaceholder(cityId: String, monumentId: String) = "ar_placeholder/$cityId/$monumentId"
    fun comercioDetalle(comercioId: String) = "comercio_detalle/$comercioId"
}
