# ARCHITECTURE.md — Arquitectura real de NicaExplore

> Todo lo aquí descrito fue verificado contra el código del repositorio. Si algo no existe
> en el código, no aparece como implementado. El proyecto tiene **dos componentes**: la app
> Android y el backend de administración (`nicaexp_web`).

## Visión general

NicaExplore es una app Android **single-activity** con UI en Jetpack Compose, salvo el mapa
nativo (`MapaActivity`) y Unity (`UnityArActivity`, que extiende `UnityPlayerActivity`).
Convive con un **backend CodeIgniter 4** que administra la misma base Firestore y expone una
API REST.

```text
MainActivity (ComponentActivity)
├── MapLibre.getInstance(applicationContext)
├── UserPreferences.init(this)
├── SampleData.loadCatalog()          (API REST -> catálogo en memoria)
├── CatalogSync.watch()                (refresco por versión cada 15 s)
├── ValoracionesRepository.refreshPublicas()
└── setContent
    └── NicaExplorerTheme
        └── Surface
            └── AppNavigation(navController)                (NavHost / Routes)
                ├── Splash / Login / Register / Recuperar
                ├── Home (drawer + bottom nav)
                │     └── CitySelection -> Catalog -> ver 3D (Intent a Unity)
                ├── Assistant (Itzae)
                ├── Mapa -> MapaPrincipalScreen (Compose + MapLibre)
                ├── Comercios / ComercioDetalle / MisComercios / ComercioForm
                ├── RutasInteligentes (por ciudad)
                ├── Profile / EditarPerfil / LugaresGuardados / Historial
                ├── Configuracion / AcercaDe
                ├── AdminPanel (ADMIN / AUDITOR)
                └── ArPlaceholder (legado, sin uso)

Componentes externos invocados por Intent:
└── UnityArActivity         -> UnityPlayer (escena Visor3D; AR como legado)

Backend (nicaexp_web) — CodeIgniter 4 + Firestore:
├── API REST v1 (/api/v1/*)  -> catálogo, health, version, valoraciones, upload
└── Panel web (/panel/*)     -> CRUD genérico (AdminLTE + DataTables + modales)
```

## Módulos Gradle

`settings.gradle.kts` incluye:

- `:app` → aplicación Android (Compose + lógica de negocio).
- `:unityLibrary` → módulo de librería exportado desde Unity.
- `:unityLibrary:xrmanifest.androidlib` → manifiesto XR requerido por Unity/ARCore.

`launcher/`, `shared/` y backups (`NicaExplorer_BurstDebugInformation_DoNotShip/`) **no** son
módulos incluidos en la build.

## Capa de presentación (Compose)

- **Tema:** `ui/theme/Theme.kt` (`NicaExplorerTheme`, modo claro/oscuro, `dynamicColor = false`),
  colores en `Color.kt` (paleta "Guardabarranco") y tipografías en `Type.kt`
  (`FontFamily.SansSerif` → Roboto del sistema).
- **Navegación:** `navigation/AppNavigation.kt` define un `NavHost` con transiciones
  slide/fade; `navigation/Routes.kt` centraliza rutas y constructores de ruta.
- **Ruta inicial:** `Routes.SPLASH`. El splash entra **siempre** a Home (exploración pública,
  sin login obligatorio); el login es opcional ("Continuar sin registrarme").
- **Compartición de estado:** `UserViewModel` se crea una sola vez en `AppNavigation` y se
  pasa a Home/Profile/EditarPerfil/AdminPanel.
- **Idioma (es/en):** textos en `res/values/strings_*.xml` (español) y `res/values-en/`
  (inglés). El selector está en `ConfiguracionScreen` (preferencia `app_language` en
  `UserPreferences`); se aplica en `MainActivity.attachBaseContext` y la Activity se recrea
  con `recreate()` al cambiar. El **contenido dinámico** de Firestore se resuelve en
  `ApiRepository`/`FirebaseRepository` usando los campos `*En` (`nombreEn`, `descripcionEn`,
  `historiaEn`, `lemaEn`, `categoriaEn`, `categoriaPadreEn`, `infoAdicionalEn`) según
  `Locale.getDefault().language`.
- **Componentes reutilizables:** `ui/components/` (`NicaTopBar`, `NicaButton`, `CityCard`,
  `PlaceCard`, `PlaceRow`, `ComercioCard`/`ComercioCover`, `CoverImage`, `RatingStars`,
  `ValoracionRow`, `TextoExpandible`, `RutaMapa`, `NicaRefreshBox`, `EmptyState`,
  `MascotaFlotante`).

Pantallas registradas en el `NavHost`:

| Ruta | Pantalla | Notas |
|---|---|---|
| `splash` | `SplashScreen` | entrada pública a Home |
| `login` / `register` / `recuperar_contrasena` | `LoginScreen`, `RegisterScreen`, `RecoveryPasswordScreen` | Firebase Auth |
| `home` | `HomeScreen` | drawer + bottom nav + recomendados por valoración |
| `city_selection` | `CitySelectionScreen` | busca ciudades |
| `catalog/{cityId}?placeId=` | `CatalogScreen` | detalle de lugar + comercios + rutas |
| `assistant?placeId=` | `AssistantScreen` | Itzae (Gemini) |
| `mapa` | `MapaPrincipalScreen` | mapa Compose con pines de lugares y comercios |
| `comercios?cityId=` / `comercio_detalle/{id}` | `ComerciosScreen`, `ComercioDetalleScreen` | catálogo vía API; detalle con respaldo Firestore |
| `comercios_subcategorias/...` / `comercios_subcategoria/...` | `ComerciosSubcategoriasScreen`, `ComerciosSubcategoriaScreen` | navegación por categorías |
| `mis_comercios` / `comercio_form?comercioId=` | `MisComerciosScreen`, `ComercioFormScreen` | solo rol COMERCIO/ADMIN; Firestore + subida de imágenes |
| `rutas_inteligentes/{cityId}` | `RutasInteligentesScreen` | por ciudad; solo Juigalpa con datos |
| `profile` / `editar_perfil` | `ProfileScreen`, `EditarPerfilScreen` | perfil + rol |
| `lugares_guardados` / `historial` | `LugaresGuardadosScreen`, `HistorialExploracionScreen` | DataStore local |
| `configuracion` / `acerca_de` | `ConfiguracionScreen`, `AcercaDeScreen` | tema/notificaciones/versión |
| `admin_panel` | `AdminPanelScreen` | guardas ADMIN/AUDITOR |
| `ar_placeholder/{cityId}/{placeId}` | `ArPlaceholderScreen` | legado, sin navegación entrante |

**Pantallas no cableadas:** `MapaScreen` (la ruta `mapa` abre `MapaPrincipalScreen`),
`MapaActivity` (registrada en el manifiesto pero sin `startActivity`) y
`SolicitudComercioScreen` (su ruta `solicitud_comercio/{cityId}` no tiene `composable`; el
registro de comercios la reemplazó). Forman parte de la deuda técnica.

## Modelo de datos

Ubicado en `model/`:

- `City(id, name, description, placeCount, gradientStart, gradientEnd, icon, imageRes,
  imageKey, imagenUrl, galeria, lema, historia, departamento, latitud, longitud, orden, activo)`
- `Place(id, name, city, cityId, category, afluencia, description, history, yearBuilt,
  modeloUnity, gradientStart, gradientEnd, icon, imageRes, imageKey, imagenUrl, latitud,
  longitud, consejosResponsables)` + enum `Afluencia` (`BAJA`, `MODERADA`, `ALTA`, con
  `displayName` y `quietnessPriority`).
- `Comercio(id, nombre, categoria, categoriaPadre, descripcion, ciudad, cityId, direccion,
  horario, diasAtencion, imagenUrl, logoUrl, galeria, latitud, longitud, telefono, whatsapp,
  tieneWhatsapp, correo, redesSociales, servicios, productos, infoAdicional, activo,
  aprobado, propietarioUid)`. `visiblePublicamente = activo && aprobado`.
- `CategoriaComercio(id, nombre, categoriaPadre, orden, activo)`
- `RutaTuristica(...)` + `ReferenciaParadaRuta` (sealed: `Lugar`, `ComercioLocal`)
- `Valoracion(tipo, refId, cityId, uid, estrellas)` + `TipoValoracion` (CIUDAD/LUGAR/COMERCIO)
- `RedesSociales` (catálogo y parseo `clave|valor`).
- `SolicitudComercio(...)` (legado), `UserProfile(uid, nombre, correo, rol)` y `UserRole`
  (ADMIN/EDITOR/USUARIO/COMERCIO/AUDITOR).

## Fuentes de datos

### API REST (catálogo)

El catálogo (ciudades, lugares, rutas, comercios, categorías y ranking de valoraciones) se
lee de la **API REST** del backend (`ApiRepository` → `ApiClient` → `HttpURLConnection`). La
URL base y la API key se inyectan en `BuildConfig` desde `local.properties`
(`nica.apiBaseUrl`, `nica.apiKey`).

- `SampleData` mantiene el catálogo en memoria (estado Compose) y `CatalogSync` refresca
  cuando cambia `/api/v1/version` (cada 15 s).
- **No hay catálogo hardcodeado**: si la API no responde, las listas quedan vacías.
- Detalle completo de endpoints en la sección "API REST" y en `README.md`.

### Firebase

- **Authentication** (correo/contraseña): registro, login, recuperación, `signOut`.
- **Cloud Firestore** (escritura directa desde la app):
  - `usuarios/{uid}` → `uid`, `nombre`, `correo`, `rol`, `fechaRegistro`.
  - `comercios/{id}` → comercios propios (propietario), `aprobado`/`activo`.
  - `valoraciones/{tipo}_{refId}_{uid}` → estrellas del usuario.
  - `solicitudes_comercios/{id}` → solicitudes de incorporación (`estado = pendiente`).
  - `mail/{id}` → bloqueada por reglas; no la usa el cliente.
- **Firebase AI Logic** (`firebase-ai`): `GeminiRepository` usa
  `Firebase.ai(backend = GenerativeBackend.googleAI())` con el modelo
  `gemini-3.5-flash-lite` e instrucciones de sistema para Itzae. El asistente responde en el
  idioma de la app mediante una directiva de idioma añadida al prompt según
  `Locale.getDefault().language`.
- **Ubicación para Itzae:** `AssistantScreen` obtiene la **ciudad** del usuario
  (`UbicacionHelper.ciudadActual`, GPS/última ubicación conocida, o selector manual) y la pasa
  a `GeminiRepository.generateContent(ciudadUsuario=…)`. El chat filtra comercios y lugares a
  esa ciudad y el modelo prioriza recomendaciones cercanas. Solo se envía la ciudad (no las
  coordenadas).
- **App Check** (`firebase-appcheck-playintegrity`) está incluido como dependencia, pero
  **sin inicializar** en el código.

`FirebaseRepository` expone: `getCurrentUser`, `getUserProfile`, `registerUser`,
`loginUser`, `sendPasswordResetEmail`, `updateUserName`, `getAllUsers`, `updateUserRole`,
`getComerciosActivos`, `getComercioById`, `getComerciosDeUsuario`, `crearComercio`,
`actualizarComercio`, `eliminarComercio`, `signOut`.

### Almacenamiento local

`data/UserPreferences.kt` (DataStore Preferences, nombre `nicaexplorer_prefs`), con claves
por usuario (`uid`):

- `dark_theme` (Boolean?, `null` = seguir sistema)
- `notifications_enabled` (Boolean, por defecto `true`)
- `lugares_guardados_$uid` (Set<String> de `placeId`)
- `historial_$uid` (Set<String> de `"placeId|timestampMillis"`)

## Integración Android ↔ Unity (visor 3D)

Flujo real desde `CatalogScreen` (botón **"Ver en 3D"**, habilitado si
`place.modeloUnity` no está vacío):

```text
CatalogScreen
  -> Intent.setClassName(packageName, "com.lospuntoycoma.nicaexplorer.ar.UnityArActivity")
     extras: cityId, monumentId (=placeId), escena = "visor3d"
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
- El **proyecto Unity fuente** vive fuera del repositorio (`C:/UnityProjects/NicaExplorer`
  según `gradle.properties`). En este repo solo está el módulo exportado `unityLibrary` y el
  código IL2CPP generado.
- Relación `monumentId` → prefab: `homenaje_madre_juigalpina`→`HomenajeMadreJuigalpina`,
  `toro_chontaleno`→`ToroChontaleno`, `estatua_museo_juigalpa`→`EstatuaMuseoJuigalpa`,
  `tumba_ruben_dario`→`TumbaRubenDario`, `estatua_san_benito`→`SanBenito`,
  `arbol_vida`→`ArbolDeLaVida`, `ruben_dario`→`EstatuaRubenDario`,
  `campana_de_la_paz`→`CampanaDeLaPaz`, `los_motivos_del_lobo_escultura`→`PazHermanoLobo`.

## MapLibre

Implementaciones presentes en el código:

### Implementación en uso: `MapaPrincipalScreen` (Compose)

- `ui/screens/MapaPrincipalScreen.kt` es el mapa conectado a la ruta `mapa`.
- Usa `MapaViewModel` (comercios vía API), `MapaMarkerFactory`, `UbicacionHelper` y
  `MapaConfig`. Dibuja pines de **lugares** y **comercios**, filtros por tipo, rejilla de
  categorías y el punto azul de ubicación del usuario.
- Detecta la ciudad más cercana (≤ 25 km) con `LocationManager` (sin dependencia nueva).

### Variante nativa (legado): `MapaActivity`

- `ui/screens/MapaActivity.kt` extiende `android.app.Activity` (no Compose) y aísla el
  `MapView` del ciclo de vida de Compose.
- Aplica `Style.Builder().fromJson(MapaConfig.STYLE_JSON)` y cámara en
  `MapaConfig.NICARAGUA` (`LatLng(12.8654, -85.2072)`) con `INITIAL_ZOOM = 6.3`.
- Registrada en el manifiesto pero **no se lanza** desde el código.

### Variante Compose sin cablear: `MapaScreen`

- `ui/screens/MapaScreen.kt` y `MapaViewModel.kt` implementan un mapa con `AndroidView` y
  `ModalBottomSheet` de detalle. `MapaScreen` **no se referencia** desde el `NavHost`;
  `MapaViewModel` sí se reutiliza.

`map/MapaConfig.kt` centraliza: `OSM_TILE_URL`
(`https://tile.openstreetmap.org/{z}/{x}/{y}.png`), `STYLE_JSON` (fuente raster OSM),
`ATTRIBUTION`, centro y zoom. `map/MapaMarkerFactory.kt` resuelve los iconos
(`comercioIcon`, `numeroIcon`, `puntoIcon`, `ubicacionIcon`).

## Flujo principal del usuario

1. Splash → Home (exploración pública; login opcional).
2. Home (ciudades, recomendados por valoración, drawer + bottom nav).
3. Selección de ciudad → Catálogo (`CatalogScreen`): portada/carrusel, historia, lugares,
   afluencia estimada, turismo responsable, comercios y rutas.
4. Detalle de lugar: guardar, "Ver en 3D", asistente IA.
5. Botón "Ver en 3D" → `UnityArActivity` → escena Visor3D con el prefab del lugar.
6. Mapa → `MapaPrincipalScreen` (MapLibre/OSM con pines y ubicación).
7. Perfil: guardados, historial, configuración, mis comercios (rol COMERCIO), admin.

## Decisiones de diseño relevantes

- **Mapa estable** en Compose (`MapaPrincipalScreen`) con renderer **OpenGL**
  (`android-sdk-opengl`) en lugar de la variante Vulkan por defecto, que provocó un crash
  nativo en el dispositivo de prueba. La variante de Activity nativa queda como legado.
- **Catálogo vía API REST** (no hardcodeado), con refresco por versión.
- **Datos locales por `uid`** para no mezclar lugares/historial entre cuentas del mismo
  dispositivo.
- **Unity permanece** como visor 3D; AR queda como código legado.

## Backend de administración (`nicaexp_web`)

Backend separado (**CodeIgniter 4 + Cloud Firestore**, ver `nicaexp_web/README.md`) que
administra las mismas colecciones de Firestore y gestiona las imágenes.

- **Landing pública de la app:** `GET /` → `Home::index` → vista `landing`
  (`app/Views/landing.php`), página autónoma y responsive que promociona la app Android con
  assets propios (`public/assets/landing.css`, `landing.js` e `img/`). El panel de
  administración permanece en `/panel`.

```text
Panel web (tema claro/oscuro NicaExplore sobre AdminLTE 4 + DataTables + modales, jQuery/AJAX)
  ├── CRUD -> Firestore (misma base que la app, proyecto nica-explore)
  └── Subir imagen -> repo público de GitHub de assets (uploads/<archivo>)  [producción]
                       ├── URL raw.githubusercontent.com guardada en imagenUrl del documento
                       ├── alternativa: Firebase Storage (nica.storageBucket)
                       └── local de desarrollo: public/uploads/<archivo>

API REST v1 (/api/v1/*)  <- consumida por la app Android
  ├── health, version, valoraciones
  ├── {ciudades, lugares, rutas, comercios, categorias_*, usuarios, solicitudes_comercios}
  └── POST /upload (token Firebase + rol COMERCIO/EDITOR/ADMIN)

App Android
  ├── Lee catálogo desde /api/v1 (ApiRepository)
  ├── Escribe perfiles, comercios propios, valoraciones y solicitudes en Firestore
  └── Carga imagenUrl con Coil (CoverImage); si falta o falla -> drawable local
```

- **Subida de imágenes de la app:** `POST /api/v1/upload` (`Api\Upload`) valida el ID token
  de Firebase y el rol `COMERCIO/EDITOR/ADMIN`, sube con `ImageStorage` y devuelve la URL.
  La app la consume desde `data/ImageUploader.kt`.
- Con `adb reverse tcp:8080 tcp:8080`, la app usa `http://localhost:8080` y llega al backend
  por USB (útil cuando la Wi-Fi aísla los clientes). Alternativa en red sin aislamiento:
  usar la IP LAN en `nica.apiBaseUrl`, `app.baseURL` y `Nica.publicBaseUrl`.
- Por desarrollo, la app permite **HTTP sin cifrar** (`usesCleartextTraffic`) para cargar
  imágenes del backend en la red local; en producción debe usarse HTTPS.

### Modelo de datos de una ciudad (plantilla: Juigalpa)

La vista de ciudad (`CatalogScreen`) define la estructura: **portada + carrusel de
imágenes → información (lema, descripción, historia, departamento) → rutas turísticas →
lugares → comercios**. Firestore se organiza así:

**`ciudades/{cityId}`**

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
| `latitud` / `longitud` | number | Ubicación (mapa) |
| `gradientStart` / `gradientEnd` | int | Degradado de marca |
| `monumentCount` | int | N.º de lugares |
| `orden` / `activo` | int / bool | Orden y visibilidad |

**`rutas/{rutaId}`**

| Campo | Tipo | Uso |
|---|---|---|
| `cityId` | string | Ciudad de la ruta |
| `nombre` / `descripcion` | string | Identidad de la ruta |
| `duracionEstimada` / `notaDuracion` | string | Duración orientativa |
| `objetivos` | array&lt;string&gt; | Propósitos de la ruta |
| `paradas` | array&lt;string&gt; | `lugar:<id>` o `comercio:<id>` |
| `imagenUrl` | string (URL) | Imagen de la ruta |
| `orden` / `activo` | int / bool | Orden y visibilidad |

**`lugares/{lugarId}` y `comercios/{id}`** mantienen `cityId`, `orden`, `activo`,
`imagenUrl`, `galeria` y (en lugares) `latitud`/`longitud`.

### Flujo de imágenes

```text
Panel web -> subir imagen -> repo GitHub de assets (uploads/<archivo>)  [producción]
                          -> Firebase Storage si nica.storageBucket     [alternativa]
                          -> public/uploads/<archivo>                    [local]
                            -> URL guardada en:
                               ciudades.imagenUrl (portada) + ciudades.galeria (carrusel)
                               rutas.imagenUrl, lugares/comercios.imagenUrl
App Android -> Coil carga esas URLs; si fallan, usa el drawable local (imagenKey)
```

- `App\Libraries\ImageStorage` centraliza la subida con prioridad
  **GitHub > Firebase Storage > modo local**.
- `php spark nica:storage-migrate` sube las imágenes locales existentes al repo de assets y
  reescribe en Firestore las URLs `.../uploads/...` por las nuevas.

### Caché de imágenes (Coil)

`NicaExplorerApp` (Application) configura `ImageLoader` con `MemoryCache` (25 % de la
memoria) y `DiskCache` (512 MB) y `respectCacheHeaders(false)`. Al salir de una lista y
volver, las imágenes se sirven desde memoria/disco sin volver a descargarse.
