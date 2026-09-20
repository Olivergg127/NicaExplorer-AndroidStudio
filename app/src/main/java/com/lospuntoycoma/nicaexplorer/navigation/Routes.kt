package com.lospuntoycoma.nicaexplorer.navigation

import android.net.Uri

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RECUPERAR_CONTRASENA = "recuperar_contrasena"
    const val HOME = "home"
    const val MAPA = "mapa"
    const val CITY_SELECTION = "city_selection"
    const val CATALOG = "catalog/{cityId}?placeId={placeId}"
    const val PROFILE = "profile"
    const val ASSISTANT = "assistant"
    const val ASSISTANT_ROUTE = "$ASSISTANT?placeId={placeId}"
    const val ADMIN_PANEL = "admin_panel"
    const val AR_PLACEHOLDER = "ar_placeholder/{cityId}/{placeId}"

    const val EDITAR_PERFIL = "editar_perfil"
    const val LUGARES_GUARDADOS = "lugares_guardados"
    const val HISTORIAL = "historial"
    const val CONFIGURACION = "configuracion"
    const val ACERCA_DE = "acerca_de"
    const val COMERCIOS = "comercios"
    const val COMERCIOS_CON_FILTRO = "comercios?cityId={cityId}"
    const val COMERCIOS_SUBCATEGORIAS = "comercios_subcategorias/{cityId}/{padre}"
    const val COMERCIOS_SUBCATEGORIA = "comercios_subcategoria/{cityId}/{subcategoria}"
    const val COMERCIO_DETALLE = "comercio_detalle/{comercioId}"
    const val SOLICITUD_COMERCIO = "solicitud_comercio/{cityId}"
    const val RUTAS_INTELIGENTES = "rutas_inteligentes/{cityId}"

    fun catalog(cityId: String) = "catalog/$cityId"
    fun catalogWithPlace(cityId: String, placeId: String) = "catalog/$cityId?placeId=$placeId"
    fun assistant(placeId: String) = "$ASSISTANT?placeId=${Uri.encode(placeId)}"
    fun arPlaceholder(cityId: String, placeId: String) = "ar_placeholder/$cityId/$placeId"
    fun comercioDetalle(comercioId: String) = "comercio_detalle/$comercioId"
    fun comerciosSubcategorias(cityId: String, padre: String) =
        "comercios_subcategorias/${Uri.encode(cityId)}/${Uri.encode(padre)}"
    fun comerciosSubcategoria(cityId: String, subcategoria: String) =
        "comercios_subcategoria/${Uri.encode(cityId)}/${Uri.encode(subcategoria)}"
    fun comercios(cityId: String) = "comercios?cityId=$cityId"
    fun solicitudComercio(cityId: String) = "solicitud_comercio/${Uri.encode(cityId)}"
    fun rutasInteligentes(cityId: String) = "rutas_inteligentes/${Uri.encode(cityId)}"
}
