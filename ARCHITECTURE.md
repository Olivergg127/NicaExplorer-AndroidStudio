# ARCHITECTURE.md — Arquitectura real de NicaExplorer

> Todo lo aquí descrito fue verificado contra el código del repositorio. Si algo no existe
> en el código, no aparece como implementado.

## Visión general

NicaExplorer es una app Android **single-activity** con UI en Jetpack Compose, salvo el
mapa, que se ejecuta en una **Activity nativa** separada (`MapaActivity`) y Unity, que se
ejecuta en `UnityArActivity` (extiende `UnityPlayerActivity`).

```text
MainActivity (ComponentActivity)
├── MapLibre.getInstance(applicationContext)
├── UserPreferences.init(this)
└── setContent
    ├── LaunchedEffect -> SampleData.loadFromFirestore()   (Firestore -> fallback local)
    └── NicaExplorerTheme
        └── Surface
            └── AppNavigation(navController)                (NavHost / Routes)
                ├── Splash / Login / Register / Recuperar
                ├── Home (drawer + bottom nav)
                │     └── CitySelection -> Catalog (monumentos) -> ver 3D (Intent)
                ├── Assistant (Itzae)
                ├── Comercios / ComercioDetalle / SolicitudComercio
                ├── RutasInteligentes (solo Juigalpa)
                ├── Profile / EditarPerfil / LugaresGuardados / Historial
                ├── Configuracion / AcercaDe
                ├── AdminPanel (ADMIN / AUDITOR)
                ├── Mapa -> lanza MapaActivity (nativa)
                └── ArPlaceholder (legado, sin uso)

Componentes externos invocados por Intent:
├── MapaActivity            -> MapLibre Native (OpenGL) + teselas OSM
└── UnityArActivity         -> UnityPlayer (escena Visor3D; AR como legado)
```

## Módulos Gradle

`settings.gradle.kts` incluye:

- `:app` → aplicación Android (Compose + lógica de negocio).
- `:unityLibrary` → módulo de librería exportado desde Unity.
- `:unityLibrary:xrmanifest.androidlib` → manifiesto XR requerido por Unity/ARCore.

`launcher/`, `shared/`, `tools/`, `NicaExplorer_BurstDebugInformation_DoNotShip/` y
`unityLibrary_backup_20260904/` **no** son módulos incluidos en la build (algunos están en
`.gitignore`).

## Capa de presentación (Compose)

- **Tema:** `ui/theme/Theme.kt` (`NicaExplorerTheme`, modo claro/oscuro, `dynamicColor = false`),
  colores en `Color.kt` (paleta "Guardabarranco") y tipografías en `Type.kt`.
- **Navegación:** `navigation/AppNavigation.kt` define un `NavHost` con transiciones
  slide/fade; `navigation/Routes.kt` centraliza rutas y constructores de ruta.
- **Ruta inicial:** `Routes.SPLASH`. `SplashScreen` decide Home/Login según
  `FirebaseAuth.currentUser`.
- **Compartición de estado:** `UserViewModel` se crea una sola vez en `AppNavigation` y se
  pasa a Home/Profile/EditarPerfil/AdminPanel.
- **Componentes reutilizables:** `ui/components/` (`NicaTopBar`, `NicaButton`, `CityCard`,
  `MonumentCard`, `MonumentRow`, `ComercioCard`/`ComercioCover`, `EmptyState`).

Pantallas registradas en el `NavHost`:

| Ruta | Pantalla | Notas |
|---|---|---|
| `splash` | `SplashScreen` | decide Home/Login |
| `login` / `register` / `recuperar_contrasena` | `LoginScreen`, `RegisterScreen`, `RecoveryPasswordScreen` | Firebase Auth |
| `home` | `HomeScreen` | drawer + bottom nav |
| `city_selection` | `CitySelectionScreen` | busca en `SampleData.cities` |
| `catalog/{cityId}?monumentId=` | `CatalogScreen` | detalle de monumento + comercios + rutas |
| `assistant?monumentId=` | `AssistantScreen` | Itzae (Gemini) |
| `comercios?cityId=` / `comercio_detalle/{id}` / `solicitud_comercio/{cityId}` | `ComerciosScreen`, `ComercioDetalleScreen`, `SolicitudComercioScreen` | Firestore |
| `rutas_inteligentes/{cityId}` | `RutasInteligentesScreen` | solo Juigalpa |
| `profile` / `editar_perfil` | `ProfileScreen`, `EditarPerfilScreen` | perfil + rol |
| `lugares_guardados` / `historial` | `LugaresGuardadosScreen`, `HistorialExploracionScreen` | DataStore local |
| `configuracion` / `acerca_de` | `ConfiguracionScreen`, `AcercaDeScreen` | tema/notificaciones/versión |
| `admin_panel` | `AdminPanelScreen` | guardas ADMIN/AUDITOR |
| `mapa` | composable que lanza `MapaActivity` y hace `popBackStack()` | mapa nativo |
| `ar_placeholder/{cityId}/{monumentId}` | `ArPlaceholderScreen` | legado, sin navegación entrante |

## Modelo de datos

Ubicado en `model/`:

- `City(id, name, description, monumentCount, gradientStart, gradientEnd, icon, imageRes, imageKey)`
- `Monument(id, name, city, cityId, category, afluencia, description, history, yearBuilt,
  modeloUnity, gradientStart, gradientEnd, icon, imageRes, imageKey, consejosResponsables)`
- `Afluencia` (enum: `BAJA`, `MODERADA`, `ALTA`, con `displayName` y `quietnessPriority`)
- `Comercio(id, nombre, categoria, descripcion, ciudad, direccion, horario, imagenUrl,
  latitud, longitud, telefono, whatsapp, tieneWhatsapp, activo)`
- `RutaTuristica(...)` + `ReferenciaParadaRuta` (sealed: `Monumento`, `ComercioLocal`)
- `SolicitudComercio(...)` y `UserProfile(uid, nombre, correo, rol)`, `UserRole` (ADMIN/USUARIO/AUDITOR)

## Fuentes de datos

### Firebase

- **Authentication** (correo/contraseña): registro, login, recuperación, `signOut`.
- **Cloud Firestore** (colecciones usadas por el cliente):
  - `usuarios/{uid}` → `uid`, `nombre`, `correo`, `rol`, `fechaRegistro`.
  - `ciudades/{cityId}` → catálogo (lectura).
  - `lugares/{monumentId}` → catálogo (lectura).
  - `comercios/{id}` → lectura solo de `activo == true`.
  - `solicitudes_comercios/{id}` → creación de solicitudes (estado `pendiente`).
  - `mail/{id}` → bloqueada por reglas; no la usa el cliente.
- **Firebase AI Logic** (`firebase-ai`): `GeminiRepository` usa
  `Firebase.ai(backend = GenerativeBackend.googleAI())` con el modelo
  `gemini-3.5-flash-lite` e instrucciones de sistema para Itzae.
- **App Check** (`firebase-appcheck-playintegrity`) está incluido como dependencia.

`FirebaseRepository` expone: `getCurrentUser`, `getUserProfile`, `registerUser`,
`loginUser`, `sendPasswordResetEmail`, `updateUserName`, `getAllUsers`, `updateUserRole`,
`getComerciosActivos`, `getComercioById`, `signOut`.

### Fallback local

`data/SampleData.kt` es la fuente **fallback**: 3 ciudades y 8 monumentos embebidos. Si
Firestore responde con colecciones no vacías, reemplaza ciudades/monumentos; si falla o
viene vacío, conserva los datos locales. `loadFromFirestore()` mapea `imageKey` a drawables
locales.

### Almacenamiento local

`data/UserPreferences.kt` (DataStore Preferences, nombre `nicaexplorer_prefs`), con claves
por usuario (`uid`):

- `dark_theme` (Boolean?, `null` = seguir sistema)
- `notifications_enabled` (Boolean, por defecto `true`)
- `lugares_guardados_$uid` (Set<String> de `monumentId`)
- `historial_$uid` (Set<String> de `"monumentId|timestampMillis"`)

## Integración Android ↔ Unity (visor 3D)

Flujo real desde `CatalogScreen` (botón **"Ver en 3D"**, habilitado si
`monument.modeloUnity` no está vacío):

```text
CatalogScreen
  -> Intent.setClassName(packageName, "com.lospuntoycoma.nicaexplorer.ar.UnityArActivity")
     extras: cityId, monumentId, escena = "visor3d"
     FLAG_ACTIVITY_NEW_TASK
  -> UnityArActivity : UnityPlayerActivity
  -> Unity C# : AndroidIntentReceiver -> Visor3DController
  -> Instantiate del prefab asociado a monumentId
```

Detalles verificados:

- `UnityArActivity` vive en su propia tarea (`taskAffinity` propia + `singleTask`).
- Al volver a entrar, `onNewIntent` relee el Intent; el reinicio se envía cuando Unity está
  listo (`notificarUnityListo()` llamado desde C#).
- Mensajes nativos usados: `UnityPlayer.UnitySendMessage("ARManager", "ReiniciarExperiencia", "")`,
  `("Visor3D", "RecargarDesdeIntent", "")`, `("ARManager"/"Visor3D", "SalirExperiencia", "")`.
- `moverAlFondo()` llama a `moveTaskToBack(true)` (equivalente al botón "Salir").
- El **proyecto Unity fuente** (`Assets/`, escenas, C#) vive fuera del repositorio
  (`C:/UnityProjects/NicaExplorer` según `gradle.properties`). En este repo solo está el
  módulo exportado `unityLibrary` y el código IL2CPP generado. Las clases C# se infieren de
  la salida IL2CPP: `AndroidIntentReceiver`, `Visor3DController`, `Visor3DGestos`,
  `Visor3DPresentacionVisual`.
- Relación `monumentId` → prefab (según README del proyecto y `modeloUnity` en `SampleData`):
  `homenaje_madre_juigalpina`→`HomenajeMadreJuigalpina`, `toro_chontaleno`→`ToroChontaleno`,
  `estatua_museo_juigalpa`→`EstatuaMuseoJuigalpa`, `tumba_ruben_dario`→`TumbaRubenDario`,
  `estatua_san_benito`→`SanBenito`, `arbol_vida`→`ArbolDeLaVida`,
  `ruben_dario`→`EstatuaRubenDario`, `campana_de_la_paz`→`CampanaDeLaPaz`.

## MapLibre

Dos implementaciones conviven en el código; **solo una está conectada**:

### Implementación estable (en uso): `MapaActivity` nativa

- `ui/screens/MapaActivity.kt` extiende `android.app.Activity` (no Compose) y aísla el
  `MapView` del ciclo de vida de Compose.
- `MapLibre.getInstance(this)` en `onCreate`; crea `MapView(this, MapLibreMapOptions())` y
  `setContentView`; delega ciclo de vida (`onStart/onResume/onPause/onStop/onLowMemory/
  onSaveInstanceState/onDestroy`).
- Aplica `Style.Builder().fromJson(MapaConfig.STYLE_JSON)` y fija cámara en
  `MapaConfig.NICARAGUA` (`LatLng(12.8654, -85.2072)`) con `INITIAL_ZOOM = 6.3`.
- No agrega marcadores. Registra logs en `NicaExplorerMapActivity`.
- Se abre desde `HomeScreen` (drawer → "Mapa") por `Routes.MAPA`, que lanza el Intent y
  hace `popBackStack()`.

### Implementación Compose (sin cablear): `MapaScreen` + `MapaViewModel`

- `ui/screens/MapaScreen.kt` y `ui/viewmodels/MapaViewModel.kt` implementan un mapa en
  Compose con `AndroidView`, `MapLibreMapOptions().renderSurfaceOnTop(true)`, marcadores de
  comercios (`MapaMarkerFactory`) y `ModalBottomSheet` de detalle.
- **No se referencia desde el `NavHost`** (solo aparece un `import` sin uso en
  `AppNavigation.kt`). Se conserva como trabajo en progreso; la variante Compose fue la que
  dio problemas de renderer/lifecycle en el dispositivo de prueba.

`map/MapaConfig.kt` centraliza: `OSM_TILE_URL`
(`https://tile.openstreetmap.org/{z}/{x}/{y}.png`), `STYLE_JSON` (fuente raster OSM),
`ATTRIBUTION`, centro y zoom. `map/MapaMarkerFactory.kt` resuelve el icono del marcador
(hoy todos usan `R.drawable.ic_map_marker_comercio`).

## Flujo principal del usuario

1. Splash → Login/Registro (Firebase Auth).
2. Home (catálogo destacado, ciudades, comercios).
3. Selección de ciudad → Catálogo de monumentos (`CatalogScreen`).
4. Detalle de monumento: descripción, historia, afluencia estimada, turismo responsable,
   guardar lugar, comercios de la ciudad, rutas.
5. Botón "Ver en 3D" → `UnityArActivity` → escena Visor3D con el prefab del monumento.
6. Botón "Hablar con el asistente IA" → Itzae con contexto del monumento/comercios.
7. Drawer → Mapa → `MapaActivity` (MapLibre/OSM, vista de Nicaragua).

## Decisiones de diseño relevantes

- **Mapa en Activity nativa** para aislar el renderer del ciclo de vida de Compose.
- **Renderer OpenGL** (`android-sdk-opengl`) en lugar de la variante Vulkan por defecto,
  porque esta última provocó un crash nativo en el dispositivo de prueba.
- **Firestore primero, `SampleData` como fallback** para que el catálogo funcione sin red.
- **Datos locales por `uid`** para no mezclar lugares/historial entre cuentas del mismo
  dispositivo.
- **Unity permanece** como visor 3D; AR queda como código legado.

## Backend de administración (`nicaexp_web`) y consumo de imágenes

Existe un backend separado (**CodeIgniter 4 + Cloud Firestore**, ver `nicaexp_web/README.md`)
que administra las mismas colecciones de Firestore y además gestiona **imágenes**:

```text
Panel web (tema claro/oscuro NicaExplorer sobre AdminLTE 4 + DataTables + modales, jQuery/AJAX)
  ├── CRUD -> Firestore (misma base que la app, proyecto nica-explore)
  └── Subir imagen -> public/uploads/<archivo>
                       └── URL guardada en imagenUrl del documento

App Android
  ├── Lee ciudades/lugares/comercios desde Firestore (misma base)
  └── Carga imagenUrl con Coil (CoverImage); si falta o falla -> drawable local
```

- La app **consume la API REST** (`/api/v1/*`) del backend para todo el contenido
  (ciudades, lugares, rutas y comercios) y descarga las imágenes desde `/uploads/`.
  La API key y la URL base se inyectan en tiempo de compilación desde `local.properties`
  (`nica.apiBaseUrl`, `nica.apiKey`) -> `BuildConfig`. Firebase se usa solo para
  autenticación y datos del usuario (perfil, rol, solicitudes).
- `SampleData.loadCatalog()` -> `ApiRepository` -> `ApiClient` (HttpURLConnection + org.json).
  No hay catálogo hardcodeado: si la API no responde, las listas quedan vacías.
- Con `adb reverse tcp:8080 tcp:8080`, la app usa `http://localhost:8080` y llega al backend
  por USB (útil cuando la Wi-Fi aísla los clientes). Alternativa en red sin aislamiento:
  usar la IP LAN en `nica.apiBaseUrl`, `app.baseURL` y `Nica.publicBaseUrl`.
- Por desarrollo, la app permite **HTTP sin cifrar** (`usesCleartextTraffic`) para cargar
  imágenes del backend en la red local; en producción debe usarse HTTPS.

## Modelo de datos de una ciudad (plantilla: Juigalpa)

La vista de ciudad (`CatalogScreen`) define la estructura: **portada + carrusel de
imágenes → información (lema, descripción, historia, departamento) → rutas turísticas →
monumentos → comercios**. Firestore se organiza así:

### `ciudades/{cityId}`
| Campo | Tipo | Uso |
|---|---|---|
| `nombre` | string | Nombre de la ciudad |
| `lema` | string | Frase corta |
| `descripcion` | string | Descripción corta (tarjetas) |
| `historia` | string | Texto largo para la vista de ciudad |
| `departamento` | string | Departamento |
| `imagenUrl` | string (URL) | **Portada** de la ciudad |
| `galeria` | array&lt;string&gt; | **Carrusel** de imágenes (URLs) |
| `imagenKey` | string | Drawable local de respaldo (legado) |
| `latitud` / `longitud` | number | Ubicación (futuro mapa) |
| `gradientStart` / `gradientEnd` | int | Degradado de marca |
| `monumentCount` | int | N.º de lugares |
| `orden` / `activo` | int / bool | Orden y visibilidad |

### `rutas/{rutaId}` (nueva colección)
| Campo | Tipo | Uso |
|---|---|---|
| `cityId` | string | Ciudad de la ruta |
| `nombre` / `descripcion` | string | Identidad de la ruta |
| `duracionEstimada` / `notaDuracion` | string | Duración orientativa |
| `objetivos` | array&lt;string&gt; | Propósitos de la ruta |
| `paradas` | array&lt;string&gt; | `monumento:<id>` o `comercio:<id>` |
| `imagenUrl` | string (URL) | Imagen de la ruta |
| `orden` / `activo` | int / bool | Orden y visibilidad |

### `lugares/{monumentId}` y `comercios/{id}`
Se mantienen, con `cityId`, `orden`, `activo`, `imagenUrl`, `galeria` y (en lugares)
`latitud`/`longitud`.

### Flujo de imágenes
```text
Panel web -> subir imagen -> public/uploads/<archivo>
                            -> URL absoluta (Nica.publicBaseUrl) guardada en:
                               ciudades.imagenUrl (portada) + ciudades.galeria (carrusel)
                               rutas.imagenUrl, lugares/comercios.imagenUrl
App Android -> Coil carga esas URLs; si fallan, usa el drawable local (imagenKey)
```

### Carga dinámica en la app
`SampleData.loadFromFirestore()` lee `ciudades`, `lugares` y `rutas`, y expone
`SampleData.cities`, `SampleData.monumentsByCity` y `SampleData.rutasByCity` /
`rutasDeCiudad(cityId)`. **No hay catálogo hardcodeado**: si Firestore no devuelve datos,
las listas quedan vacías. También se eliminó la ruta local hardcodeada
(`RutasTuristicasData`) y el mapeo local de imágenes de comercios, y `modeloUnity`
(nombre del prefab del visor 3D) se gestiona desde el panel/backend.

Seeds del backend:
- `php spark nica:seed-plantilla` — estructura completa de Juigalpa (portada, carrusel,
  historia, ruta).
- `php spark nica:seed-imagenes` — copia **todas** las imágenes locales de la app a
  `public/uploads/` y las enlaza en Firestore (ciudades: portada + galería; lugares;
  comercios).
- `php spark nica:seed-categorias` — crea el catálogo `categorias_lugares` a partir de las
  categorías ya usadas por los lugares (idempotente).

### Caché de imágenes (Coil)
`NicaExplorerApp` (Application) configura `ImageLoader` con `MemoryCache` (25 % de la
memoria) y `DiskCache` (512 MB) y `respectCacheHeaders(false)`. Al salir de una lista y
volver, las imágenes se sirven desde memoria/disco sin volver a descargarse.
