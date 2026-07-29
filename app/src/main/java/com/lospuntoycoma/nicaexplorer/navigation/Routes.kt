package com.lospuntoycoma.nicaexplorer.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CITY_SELECTION = "city_selection"
    const val CATALOG = "catalog/{cityId}"
    const val PROFILE = "profile"
    const val ASSISTANT = "assistant"
    const val AR_PLACEHOLDER = "ar_placeholder/{cityId}/{monumentId}"

    fun catalog(cityId: String) = "catalog/$cityId"
    fun arPlaceholder(cityId: String, monumentId: String) = "ar_placeholder/$cityId/$monumentId"
}
