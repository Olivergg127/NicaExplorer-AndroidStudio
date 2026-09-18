# PROJECT_MAP.md — Mapa de archivos importantes

> Estructura **real** del repositorio. Una línea por archivo relevante. No lista
> directorios generados (`build/`, `.gradle/`, `tools/node_modules/`, IL2CPP generado).

## Raíz

```text
NicaExplorer/
├── AGENTS.md                 Reglas obligatorias para agentes.
├── INIT.md                   Puesta al día rápida del proyecto.
├── ARCHITECTURE.md           Arquitectura real y flujos.
├── PROJECT_MAP.md            Este archivo.
├── PROJECT_CONTEXT.md        Producto, visión y decisiones.
├── DEVELOPMENT.md            Entorno, versiones y build.
├── CONVENTIONS.md            Convenciones de código y UI.
├── ROADMAP.md                Estado implementado / pendiente.
├── CHANGELOG_AGENT.md        Bitácora de cambios de agentes.
├── README.md                 Documentación original del equipo.
├── settings.gradle.kts       Módulos incluidos (:app, :unityLibrary, xrmanifest).
├── build.gradle.kts          Plugins raíz (AGP, Kotlin, Google Services, Compose).
├── gradle.properties         Propiedades Gradle + configuración de export Unity.
├── gradlew.bat               Wrapper de Gradle para Windows.
├── firestore.rules           Reglas de seguridad de Firestore.
├── keystore.properties       Firma release (LOCAL, fuera de Git; no exponer).
├── local.properties          SDK local (fuera de Git; no exponer).
├── .gitignore                Ignora build, secretos, launcher, backups, node_modules.
├── docs/
│   ├── paleta-colores.md         Detalle de la paleta "Guardabarranco".
│   ├── Paleta-Colores-NicaExplorer.docx  Versión Word de la paleta.
│   └── solicitudes-comercios-firebase.md Reglas + Cloud Function para solicitudes.
├── tools/
│   ├── package.json / package-lock.json  Dependencia firebase-admin.
│   ├── normalize_users.mjs   Normaliza usuarios en Firestore (dry-run / --apply).
│   ├── seed_firestore.mjs    Seed idempotente de ciudades y lugares.
│   └── verify_firestore.mjs  Verifica campos requeridos del catálogo.
├── unityLibrary/             Módulo Android exportado desde Unity (visor 3D).
├── nicaexp_web/              Backend de administración (CodeIgniter 4 + Firestore).
├── launcher/                 Export Unity legado, ignorado por Git (no es módulo).
├── shared/                   Scripts Gradle compartidos de Unity (common/keepUnitySymbols).
└── .artifacts/               Capturas/logs de pruebas del mapa (sin versionar).
```

## app/

```text
app/
├── build.gradle.kts                     Configuración del módulo app (deps, SDK, firma).
├── google-services.json                 Config Firebase (SENSIBLE; no editar/exponer).
├── proguard-rules.pro                   Reglas ProGuard (release).
├── release/baselineProfiles/            Perfiles base de release.
└── src/main/
    ├── AndroidManifest.xml              Activities, permisos y tools:replace de Unity.
    ├── java/com/lospuntoycoma/nicaexplorer/
    │   ├── MainActivity.kt              Punto de entrada: MapLibre, prefs, tema, NavHost.
    │   ├── NicaExplorerApp.kt           Application: configura Coil (caché en memoria y disco).
    │   ├── ar/UnityArActivity.kt        Activity que aloja Unity (visor 3D / AR legado).
    │   ├── data/
    │   │   ├── ApiClient.kt                 Cliente HTTP de la API (X-API-KEY, org.json).
    │   │   ├── ApiRepository.kt             Contenido desde la API (ciudades, lugares, rutas, comercios).
    │   │   ├── FirebaseRepository.kt        Auth + Firestore (usuarios, solicitudes, roles).
    │   │   ├── GeminiRepository.kt          Itzae: Firebase AI Logic (gemini-3.5-flash-lite).
    │   │   ├── SolicitudComercioRepository.kt Crea solicitudes_comercios.
    │   │   ├── SampleData.kt                Catálogo dinámico desde Firestore (ciudades, lugares, rutas).
    │   │   └── UserPreferences.kt           DataStore: tema, notificaciones, guardados, historial.
    │   ├── map/
    │   │   ├── MapaConfig.kt                URL OSM, STYLE_JSON, centro/zoom.
    │   │   └── MapaMarkerFactory.kt         Icono de marcador por categoría.
    │   ├── model/
    │   │   ├── City.kt                      Modelo de ciudad.
    │   │   ├── Monument.kt                  Modelo de monumento + enum Afluencia.
    │   │   ├── Comercio.kt                  Modelo de comercio.
    │   │   ├── RutaTuristica.kt             Ruta + ReferenciaParadaRuta.
    │   │   ├── SolicitudComercio.kt         Datos de solicitud de negocio.
    │   │   ├── UserProfile.kt               Perfil de usuario.
    │   │   └── UserRole.kt                  Enum ADMIN/USUARIO/AUDITOR.
    │   ├── navigation/
    │   │   ├── Routes.kt                    Constantes y constructores de rutas.
    │   │   └── AppNavigation.kt             NavHost con todas las pantallas.
    │   ├── ui/components/
    │   │   ├── CityCard.kt                  Tarjeta de ciudad.
    │   │   ├── ComercioCard.kt              Tarjeta y portada de comercio (Coil + fallback).
    │   │   ├── EmptyState.kt                Estado vacío con acción.
    │   │   ├── MonumentCard.kt              Tarjeta horizontal de monumento.
    │   │   ├── MonumentRow.kt               Fila de monumento (lista).
    │   │   ├── NicaButton.kt                Botón de marca con degradado.
    │   │   └── TopBar.kt                    Barra superior reutilizable (NicaTopBar).
    │   ├── ui/screens/
    │   │   ├── SplashScreen.kt              Splash + decisión de sesión.
    │   │   ├── LoginScreen.kt               Inicio de sesión.
    │   │   ├── RegisterScreen.kt            Registro.
    │   │   ├── RecoveryPasswordScreen.kt    Recuperación de contraseña.
    │   │   ├── HomeScreen.kt                Inicio con drawer, bottom nav y destacados.
    │   │   ├── CitySelectionScreen.kt       Búsqueda/selección de ciudades.
    │   │   ├── CatalogScreen.kt             Detalle de monumento, comercios y rutas.
    │   │   ├── AssistantScreen.kt           Chat de Itzae.
    │   │   ├── ComerciosScreen.kt           Lista de comercios (filtro por ciudad).
    │   │   ├── ComercioDetalleScreen.kt     Detalle, WhatsApp, Google Maps, copiar teléfono.
    │   │   ├── SolicitudComercioScreen.kt   Formulario de solicitud de negocio.
    │   │   ├── RutasInteligentesScreen.kt   Ruta turística predefinida.
    │   │   ├── ProfileScreen.kt             Perfil y rol.
    │   │   ├── EditarPerfilScreen.kt        Edición de nombre.
    │   │   ├── LugaresGuardadosScreen.kt    Lugares guardados (DataStore).
    │   │   ├── HistorialExploracionScreen.kt Historial de exploración (DataStore).
    │   │   ├── ConfiguracionScreen.kt       Tema y notificaciones.
    │   │   ├── AcercaDeScreen.kt            Información del proyecto/equipo.
    │   │   ├── AdminPanelScreen.kt          Panel de usuarios (ADMIN/AUDITOR).
    │   │   ├── MapaActivity.kt              Mapa NATIVO con MapLibre (solución estable).
    │   │   ├── MapaScreen.kt                Mapa Compose con marcadores (NO cableado).
    │   │   └── ArPlaceholderScreen.kt       Placeholder legado de AR (sin uso).
    │   ├── ui/theme/
    │   │   ├── Color.kt                     Paleta "Guardabarranco" y degradados.
    │   │   ├── Theme.kt                     NicaExplorerTheme + brushes por tema.
    │   │   └── Type.kt                      Tipografía Material 3.
    │   ├── ui/viewmodels/
    │   │   ├── UserViewModel.kt             Perfil/rol del usuario actual.
    │   │   ├── ComerciosViewModel.kt        Lista de comercios activos.
    │   │   ├── ComercioDetalleViewModel.kt  Detalle de un comercio.
    │   │   ├── SolicitudComercioViewModel.kt Validación/envío de solicitudes.
    │   │   └── MapaViewModel.kt             Carga comercios para el mapa (NO cableado).
    │   └── util/TelefonoUtils.kt            Normalización de teléfono y link de WhatsApp.
    └── res/
        ├── drawable/                        Imágenes de ciudades/monumentos/comercios + isotipo + icono de marcador.
        └── values/
            ├── strings.xml                  Solo `app_name`.
            └── themes.xml                   Tema base `Theme.NicaExplorer`.
```

## unityLibrary/

```text
unityLibrary/
├── build.gradle                     Config del módulo Unity + tarea buildIl2Cpp.
├── proguard-unity.txt               Reglas ProGuard de Unity.
├── libs/                            arcore_client, ARPresto, UnityARCore,
│                                    unityandroidpermissions (AAR) + unity-classes.jar.
├── xrmanifest.androidlib/           Manifiesto XR (módulo requerido por Unity/ARCore).
└── src/main/
    ├── java/com/unity3d/player/UnityPlayerActivity.java  Activity base de Unity.
    ├── jniLibs/arm64-v8a/           libil2cpp.so, libunity.so, libmain.so, lib_burst_generated.so.
    └── Il2CppOutputProject/         Salida IL2CPP generada (no editar a mano).
```

> El proyecto Unity original (Assets, escenas, scripts C#) **no está en este repositorio**;
> vive en la ruta indicada por `unity.projectPath` en `gradle.properties`
> (`C:/UnityProjects/NicaExplorer`). Solo se versiona el módulo exportado.

## nicaexp_web/ (backend de administración)

```text
nicaexp_web/
├── README.md                            Guía de instalación, API y panel.
├── .env.example                         Plantilla de configuración (sin secretos).
├── composer.json / composer.lock        Dependencias PHP (CI4, kreait/firebase-php, cloud-firestore).
├── spark                                CLI de CodeIgniter.
├── app/
│   ├── Config/
│   │   ├── Nica.php                     projectId, credenciales, transporte, apiKey, admin.
│   │   ├── NicaResources.php            Definición de colecciones y campos (fuente única).
│   │   ├── Routes.php                   Rutas de /api/v1 y /panel.
│   │   ├── Filters.php                  Alias `apikey` y `panelauth`.
│   │   └── Security.php                 CSRF (regenerate = false para AJAX).
│   ├── Controllers/
│   │   ├── Api/                         BaseApiController, Health (health-check), Resources (CRUD).
│   │   └── Panel/                       Auth (login/logout), Dashboard y Resources (CRUD web).
│   ├── Filters/
│   │   ├── ApiKeyFilter.php             Exige X-API-KEY en /api/v1/*.
│   │   └── PanelAuthFilter.php          Exige sesión de admin en /panel/*.
│   ├── Libraries/
│   │   ├── FirebaseFactory.php          Cliente Firestore (ADC o service account, REST).
│   │   ├── FirestoreRepository.php      CRUD genérico + validación + formato de filas.
│   │   └── ResourceManager.php          Repositorios por clave de colección.
│   ├── Commands/SeedPlantilla.php      Seed de la ciudad plantilla (php spark nica:seed-plantilla).
│   ├── Commands/SeedImagenes.php       Enlaza todas las imágenes de la app en Firestore (php spark nica:seed-imagenes).
│   ├── Commands/SeedCategorias.php     Crea el catálogo de categorías de lugares (php spark nica:seed-categorias).
│   └── Views/
│       ├── layout/panel.php             Layout del panel (tema oscuro NicaExplorer, sidebar y topbar).
│       └── panel/{login,dashboard,resource}.php  Login, dashboard y tabla+modales.
├── public/
│   ├── index.php                        Front controller.
│   └── assets/{panel.js,panel.css,theme.js}  DataTables, modales AJAX, diseño y switch de tema.
└── writable/                            Logs, caché y sesiones (no versionado).
```

> `vendor/`, `.env` y `writable/*` están ignorados por `nicaexp_web/.gitignore`.
> Toda la definición de colecciones/campos vive en `app/Config/NicaResources.php`.
> Las colecciones administradas incluyen `ciudades`, `lugares`, `categorias_lugares`
> (catálogo de categorías), `rutas`, `comercios`, `solicitudes_comercios` y `usuarios`.
> Los campos de tipo `reference` se dibujan como select en el panel (p. ej. `cityId` →
> `ciudades`, `categoria` → `categorias_lugares`); los identificadores no se muestran en
> las tablas.
