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
     * Aplica el idioma guardado a TODA la Activity antes de crearla. Es la forma
     * robusta en Android: el contexto de la Activity ya queda localizado y no se
     * rompen otros CompositionLocals (p. ej. ActivityResultRegistryOwner).
     */
    override fun attachBaseContext(newBase: Context) {
        UserPreferences.init(newBase)
        val language = runBlocking { UserPreferences.languageFlow().first() }
        if (language.isNullOrBlank()) {
            // "Sistema": alinea Locale.getDefault() con el idioma del dispositivo.
            Locale.setDefault(newBase.resources.configuration.locales[0])
            super.attachBaseContext(newBase)
        } else {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = Configuration(newBase.resources.configuration)
            config.setLocale(locale)
            super.attachBaseContext(newBase.createConfigurationContext(config))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializa MapLibre una sola vez antes de crear cualquier MapView.
        MapLibre.getInstance(applicationContext)
        UserPreferences.init(this)
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(Unit) {
                SampleData.loadCatalog()
                CatalogSync.watch()
                ValoracionesRepository.refreshPublicas()
            }
            val darkThemePref by UserPreferences.darkThemeFlow().collectAsState(initial = null)
            NicaExplorerTheme(darkTheme = darkThemePref ?: isSystemInDarkTheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
