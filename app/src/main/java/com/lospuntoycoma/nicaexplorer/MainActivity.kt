package com.lospuntoycoma.nicaexplorer

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.maplibre.android.MapLibre
import com.lospuntoycoma.nicaexplorer.data.UserPreferences
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.data.CatalogSync
import com.lospuntoycoma.nicaexplorer.data.ValoracionesRepository
import com.lospuntoycoma.nicaexplorer.navigation.AppNavigation
import com.lospuntoycoma.nicaexplorer.ui.theme.NicaExplorerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MainActivity : ComponentActivity() {

    /**
     * Aplica el idioma guardado (es/en) al contexto de la Activity antes de crearla.
     * Se hace aquí (y no con `LocalContext` en Compose) porque sobrescribir `LocalContext`
     * rompía `ActivityResultRegistryOwner` (permisos, selectores) y cerraba la app.
     */
    override fun attachBaseContext(newBase: Context) {
        UserPreferences.init(newBase)
        // "es", "en" o null (= seguir el idioma del sistema).
        val language = runBlocking { UserPreferences.languageFlow().first() }
        if (language.isNullOrBlank()) {
            // Sistema: alinea Locale.getDefault() con el del dispositivo.
            Locale.setDefault(newBase.resources.configuration.locales[0])
            super.attachBaseContext(newBase)
        } else {
            // Idioma explícito: contexto localizado con ese Locale.
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = Configuration(newBase.resources.configuration).apply { setLocale(locale) }
            super.attachBaseContext(newBase.createConfigurationContext(config))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // MapLibre se inicializa una sola vez, antes de crear cualquier MapView.
        MapLibre.getInstance(applicationContext)
        UserPreferences.init(this)
        enableEdgeToEdge()
        setContent {
            // Al crear (o recrear por cambio de idioma): carga catálogo, refresco y valoraciones.
            LaunchedEffect(Unit) {
                SampleData.loadCatalog()
                CatalogSync.watch()
                ValoracionesRepository.refreshPublicas()
            }
            val darkThemePref by UserPreferences.darkThemeFlow().collectAsState(initial = null)
            NicaExplorerTheme(darkTheme = darkThemePref ?: isSystemInDarkTheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(navController = rememberNavController())
                }
            }
        }
    }
}
