# NicaExplore

**Explora. Conecta. Inspira.**

NicaExplore (nombre visible; el identificador técnico del paquete sigue siendo
`com.lospuntoycoma.nicaexplorer`) es una aplicación móvil Android orientada al turismo de
Nicaragua, acompañada de un **panel web de administración**. Combina información cultural
de ciudades y monumentos, fotografías, un **visor 3D** (Unity), un asistente de IA
(**Itzae**), comercios locales, rutas turísticas y un mapa propio (MapLibre +
OpenStreetMap).

El proyecto consta de **dos componentes** que comparten la misma base de datos Cloud
Firestore (proyecto `nica-explore`):

| Componente | Carpeta | Tecnología | Rol |
|---|---|---|---|
| **App Android** | `app/` + `unityLibrary/` | Kotlin + Jetpack Compose + Unity + MapLibre | Experiencia del turista |
| **Panel web / Backend** | `nicaexp_web/` | CodeIgniter 4 (PHP) + Firestore | Administración y API REST |

> **Equipo:** Los Punto y Coma. **Contexto:** prototipo académico.

---

## Tabla de contenidos

1. [Funcionalidades](#funcionalidades)
2. [Ciudades y monumentos](#ciudades-y-monumentos)
3. [Arquitectura general](#arquitectura-general)
4. [Proyecto 1 — App Android](#proyecto-1--app-android)
5. [Proyecto 2 — Panel web / Backend](#proyecto-2--panel-web--backend)
6. [API REST](#api-rest)
7. [Modelo de datos (Firestore)](#modelo-de-datos-firestore)
8. [Roles y seguridad](#roles-y-seguridad)
9. [Unity y visor 3D](#unity-y-visor-3d)
10. [Instalación y ejecución](#instalación-y-ejecución)
11. [Despliegue](#despliegue)
12. [Alcance y transparencia](#alcance-y-transparencia)
13. [Equipo](#equipo)

---

## Funcionalidades

### App Android
- Registro, inicio de sesión y recuperación de contraseña (Firebase Auth). **Exploración
  pública**: no es obligatorio registrarse ("Continuar sin registrarme").
- **Tipos de cuenta**: *Usuario normal* (`USUARIO`) o *Usuario comercio* (`COMERCIO`).
- Perfil de usuario y edición del nombre; preferencias de tema oscuro/claro y notificaciones.
- Ciudades y catálogo de monumentos con descripción, historia, año y categoría.
- **Afluencia estimada** orientativa (BAJA / MODERADA / ALTA) con sugerencia de alternativa
  más tranquila.
- **Visor 3D con Unity**: rotación, zoom, restablecer y salir.
- **Asistente IA Itzae** (Firebase AI Logic / Gemini) con contexto de monumento, catálogo
  local y comercios reales.
- Comercios/restaurantes locales: listado, filtro por ciudad y subcategoría, detalle,
  contacto por WhatsApp, correo, redes sociales y Google Maps.
- **Comercios propios** para cuentas `COMERCIO`: alta/edición con subida de imágenes, logo y
  galería; aprobación por administrador desde el panel.
- Lugares guardados e historial de exploración (locales, por `uid`, en DataStore).
- **Mapa turístico** ("Descubre a tu alrededor"): pines de lugares y comercios, filtros,
  categorías y ubicación del usuario (ciudad conocida más cercana por GPS, ≤ 25 km).
- **Rutas turísticas** por ciudad, con mapa de paradas numeradas y trazado por carretera.
- **Recomendados por valoración**: top de lugares (5) y comercios (10) por estrellas.
- Panel de administración móvil (roles `ADMIN`/`AUDITOR`).

### Panel web / Backend
- **Landing pública** en la raíz del dominio (`/`): página promocional responsive de la app
  Android (hero, características, cómo funciona, ciudades, comercios, galería, FAQ y
  descarga del APK), con diseño propio e identidad oscura/turquesa.
- **API REST v1 (JSON)** con API key y CRUD de todas las colecciones.
- **Panel web** con tema claro/oscuro propio (`AdminLTE 4`/Bootstrap), sidebar, **DataTables**
  2 y **modales** para crear/editar/eliminar (jQuery + AJAX).
- Formularios **autogenerados** a partir de la definición declarativa de colecciones.
- Login con **cuentas Firebase** (rol leído de Firestore) o admin de respaldo en `.env`.
- **Matriz de permisos** por rol (C/L/M/E por módulo).
- **Subida de imágenes** (GitHub > Firebase Storage > local) y selector de ubicación
  (Leaflet + OSM + Nominatim).

---

## Ciudades y monumentos

Las únicas ciudades vigentes del prototipo son **Juigalpa, León y Managua**. El catálogo
contiene exactamente ocho monumentos:

| Ciudad | Monumento | `monumentId` |
|---|---|---|
| Juigalpa | Homenaje a la Madre Juigalpina | `homenaje_madre_juigalpina` |
| Juigalpa | Toro Chontaleño | `toro_chontaleno` |
| Juigalpa | Estatua Museo Juigalpa | `estatua_museo_juigalpa` |
| León | Tumba de Rubén Darío | `tumba_ruben_dario` |
| León | Estatua de San Benito | `estatua_san_benito` |
| Managua | Árbol de la Vida | `arbol_vida` |
| Managua | Estatua de Rubén Darío | `ruben_dario` |
| Managua | Campana de la Paz | `campana_de_la_paz` |

> Además, "Los Motivos del Lobo (Escultura)" (León, `los_motivos_del_lobo_escultura`) está
> registrado en Firestore con `modeloUnity = PazHermanoLobo` y su modelo integrado en el
> visor 3D.

**Categorías de monumento (ejemplos reales):** Monumento cultural, Tradición ganadera,
Patrimonio arqueológico, Monumento urbano, Monumento conmemorativo, Monumento histórico,
Patrimonio religioso.

---

## Arquitectura general

```text
┌─────────────────────────┐        ┌──────────────────────────────┐
│      App Android        │        │    Panel web / Backend       │
│  Kotlin + Compose +     │        │  CodeIgniter 4 + PHP 8.3     │
│  Unity (visor 3D) +     │        │  AdminLTE 4 + DataTables     │
│  MapLibre (mapa)        │        │  API REST v1 (JSON)          │
└───────────┬─────────────┘        └───────────────┬──────────────┘
            │                                      │
            │ Firestore (lectura/escritura)        │ Firestore (admin)
            │ API REST (catálogo, upload, ranking) │
            │ Firebase Auth / AI Logic             │
            ▼                                      ▼
        ┌──────────────────────────────────────────────┐
        │        Cloud Firestore — proyecto nica-explore │
        └──────────────────────────────────────────────┘
             ▲                    ▲                 ▲
             │                    │                 │
      Firebase Authentication   Firebase AI      GitHub assets
        (cuentas/roles)        (Gemini/Itzae)   (imágenes públicas)
```

**Modelo de acceso a datos (importante):**

- La **app** lee el **catálogo** (ciudades, lugares, rutas, categorías, comercios y ranking
  de valoraciones) a través de la **API REST** del backend, que a su vez lee Firestore.
- La **app** escribe **directamente en Firestore** lo que es propio del usuario:
  perfiles (`usuarios`), comercios propios (`comercios`), valoraciones (`valoraciones`) y
  solicitudes (`solicitudes_comercios`).
- El **panel web** administra todo Firestore con credenciales de administrador.
- Las **imágenes** se sirven desde un repositorio público de GitHub
  (`Olivergg127/NicaExplorer-assets`), porque el proyecto `nica-explore` no tiene facturación
  y Firebase Storage requiere el plan Blaze.

---

## Proyecto 1 — App Android

### Stack y tecnologías

- **Lenguaje:** Kotlin.
- **UI:** Jetpack Compose + Material 3 (toda la UI principal). La actividad del mapa nativo
  (`MapaActivity`) usa XML/Views.
- **Arquitectura:** single-activity (`MainActivity`) + `AppNavigation` (Navigation Compose) +
  `ViewModel` con `StateFlow`.
- **Autenticación y datos:** Firebase Authentication, Cloud Firestore, Firebase AI Logic
  (Gemini) y la dependencia App Check (Play Integrity) *incluida pero sin inicializar*.
- **Visor 3D:** Unity 6 (`6000.5.1f1`) exportado como módulo Android `unityLibrary`.
- **Mapa:** MapLibre Native Android (variante **OpenGL**), teselas raster de OpenStreetMap.
- **Preferencias locales:** DataStore Preferences.
- **Imágenes:** Coil.

### Versiones comprobadas

| Componente | Versión |
|---|---|
| Gradle (wrapper) | 8.13 |
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.3.0 |
| Compose BOM | 2024.08.00 |
| JVM / Java | 17 |
| `compileSdk` (app) | 34 |
| `minSdk` | 30 |
| `targetSdk` (app) | 34 |
| `versionCode` / `versionName` | 4 / 1.1.2 |
| Firebase BOM | 34.16.0 |
| MapLibre (`android-sdk-opengl`) | 12.3.1 |
| Unity | 6000.5.1f1 |
| Unity `compileSdk` / `targetSdk` / `buildTools` | 36 / 36 / 36.0.0 |
| NDK (Unity) | 27.2.12479018 |
| ABI soportada | `arm64-v8a` (única) |
| Proyecto Firebase | `nica-explore` |

### Dependencias principales (`app/build.gradle.kts`)

- Compose: `ui`, `ui-graphics`, `ui-tooling-preview`, `material3`, `material`,
  `material-icons-extended` (vía BOM `2024.08.00`).
- `androidx.core:core-ktx:1.12.0`, `androidx.appcompat:appcompat:1.6.1`,
  `androidx.lifecycle:lifecycle-runtime-ktx:2.7.0`,
  `androidx.activity:activity-compose:1.8.2`.
- `androidx.datastore:datastore-preferences:1.1.1`.
- `androidx.navigation:navigation-compose:2.7.6`.
- `io.coil-kt:coil-compose:2.7.0`.
- `org.maplibre.gl:android-sdk-opengl:12.3.1`.
- Firebase (BOM `34.16.0`): `firebase-auth`, `firebase-firestore`, `firebase-ai`,
  `firebase-appcheck-playintegrity`.
- Unity: `project(":unityLibrary")` + `unityLibrary/libs/unity-classes.jar`.

### Estructura del código (`com.lospuntoycoma.nicaexplorer`)

```text
app/src/main/java/com/lospuntoycoma/nicaexplorer/
├── MainActivity.kt            Punto de entrada; inicializa MapLibre, prefs y catálogo.
├── NicaExplorerApp.kt         Application; configura la caché de imágenes de Coil.
├── ar/
│   └── UnityArActivity.kt     Activity que aloja Unity (visor 3D + AR legado).
├── data/
│   ├── FirebaseRepository.kt      Auth + Firestore (perfiles, comercios propios).
│   ├── ApiRepository.kt           Catálogo vía API REST.
│   ├── ApiClient.kt               Cliente HTTP (HttpURLConnection).
│   ├── GeminiRepository.kt        Asistente Itzae (Firebase AI / Gemini).
│   ├── SampleData.kt              Catálogo en memoria (estado Compose).
│   ├── CatalogSync.kt             Refresco automático por versión.
│   ├── ValoracionesRepository.kt  Escritura en Firestore + ranking desde la API.
│   ├── ImageUploader.kt           Subida de imágenes (POST /api/v1/upload).
│   ├── SolicitudComercioRepository.kt
│   ├── UserPreferences.kt         DataStore (tema, guardados, historial).
│   └── UbicacionHelper.kt         GPS opcional (ciudad más cercana).
├── map/
│   ├── MapaConfig.kt          Estilo OSM/URL/cámara.
│   └── MapaMarkerFactory.kt   Iconos de marcadores.
├── model/                     City, Place, Comercio, RutaTuristica, Valoracion, etc.
├── navigation/
│   ├── Routes.kt              Todas las rutas.
│   └── AppNavigation.kt       NavHost y flujo de pantallas.
├── ui/
│   ├── components/            Componentes reutilizables (cards, botón, topbar...).
│   ├── screens/               Pantallas (ver listado abajo).
│   ├── theme/                 Color.kt, Theme.kt, Type.kt.
│   └── viewmodels/            UserViewModel, ComerciosViewModel, etc.
└── util/
    └── TelefonoUtils.kt       Normalización de teléfono/WhatsApp.
```

### Pantallas (26 archivos en `ui/screens/`)

`SplashScreen`, `LoginScreen`, `RegisterScreen`, `RecoveryPasswordScreen`, `HomeScreen`,
`CitySelectionScreen`, `CatalogScreen`, `AssistantScreen`, `ComerciosScreen`,
`ComerciosSubcategoriasScreen` (dos composables), `ComercioDetalleScreen`,
`ComercioFormScreen`, `MisComerciosScreen`, `ProfileScreen`, `EditarPerfilScreen`,
`ConfiguracionScreen`, `AcercaDeScreen`, `LugaresGuardadosScreen`,
`HistorialExploracionScreen`, `AdminPanelScreen`, `RutasInteligentesScreen`,
`SolicitudComercioScreen`, `ArPlaceholderScreen`, `MapaActivity` (nativa),
`MapaPrincipalScreen`, `MapaScreen`.

**Pantallas no cableadas a la navegación:** `MapaScreen` (la ruta `mapa` abre
`MapaPrincipalScreen`), `MapaActivity` (registrada en el manifiesto pero sin `startActivity`)
y `SolicitudComercioScreen` (reemplazada por el registro directo de comercios). Forman parte
de la deuda técnica conocida (ver `ROADMAP.md`).

### Flujo de navegación

`SPLASH → HOME` (exploración pública). Desde `HOME`: `CITY_SELECTION → CATALOG`
(con `placeId` opcional), `PROFILE`, `ASSISTANT`, `MAPA`, `ADMIN_PANEL`,
`LUGARES_GUARDADOS`, `MIS_COMERCIOS`, `LOGIN`, detalle de comercio. El botón "Ver en 3D" del
catálogo abre Unity por `Intent` explícito.

### Tema

Paleta **"Guardabarranco"** (ave nacional): turquesa (lagunas), azul (cielo/lagos),
terracota (cerámica de San Juan de Oriente) y dorado reservado. Interfaz predominantemente
oscura con acentos verde/turquesa. Tipografía: `FontFamily.SansSerif` (Roboto del sistema en
Android estándar). Detalle en `ui/theme/Color.kt` y `docs/paleta-colores.md`.

---

## Proyecto 2 — Panel web / Backend

Carpeta `nicaexp_web/`. Backend de administración construido con **CodeIgniter 4** y
**Cloud Firestore** (la misma base que la app; **no usa MySQL**).

### Stack y tecnologías

- **Framework:** CodeIgniter 4 (`^4.7`).
- **PHP:** 8.3 con extensiones `intl`, `fileinfo`, `curl`, `openssl`, `mbstring`, `zip`,
  `bcmath`.
- **Firestore SDK:** `google/cloud-firestore:^2.3` (transporte **REST**, sin `grpc`) y
  `kreait/firebase-php:^8.5`.
- **Composer:** 2.
- **Frontend del panel:** AdminLTE 4 / Bootstrap, DataTables 2, jQuery/AJAX, Leaflet + OSM
  (selector de ubicación).

### Estructura relevante

```text
nicaexp_web/
├── app/
│   ├── Config/
│   │   ├── Nica.php            Configuración (projectId, credenciales, apiKey, admin).
│   │   ├── NicaResources.php   Definición de colecciones y campos (fuente de verdad).
│   │   ├── Routes.php          Rutas de la API y del panel.
│   │   └── Filters.php         Alias de filtros (apikey, panelauth, panelcan).
│   ├── Controllers/
│   │   ├── Api/                Health, Catalog, Resources, Upload, Valoraciones.
│   │   └── Panel/              Auth, Dashboard, Resources.
│   ├── Commands/               Seeds y nica:storage-migrate.
│   ├── Filters/                ApiKeyFilter, PanelAuthFilter, PanelPermissionFilter.
│   ├── Libraries/
│   │   ├── FirebaseFactory.php      Cliente de Firestore/Storage (ADC o service account).
│   │   ├── FirestoreRepository.php  CRUD genérico + validación + formato.
│   │   ├── ImageStorage.php         Subida de imágenes (GitHub > Storage > local).
│   │   ├── ResourceManager.php      Acceso a repositorios por clave.
│   │   └── PanelPermissions.php     Matriz de permisos por rol.
│   └── Views/
│       ├── landing.php         Landing pública de la app (raíz del dominio).
│       ├── layout/panel.php    Layout del panel (sidebar/topbar, tema claro/oscuro).
│       └── panel/              login, dashboard y resource (DataTable + modales).
├── docker/                    entrypoint.sh (puerto) y write-env.php (.env desde env vars).
├── Dockerfile                 Imagen PHP 8.3 + Apache.
├── public/assets/             panel.js, panel.css, theme.js, landing.css, landing.js.
├── public/assets/img/         Logos, capturas y fotos de la landing.
└── render.yaml                (en la raíz del repo) Blueprint de despliegue.
```

### Configuración declarativa (`NicaResources.php`)

A partir de este mapa se generan automáticamente **los endpoints REST** y **las tablas y
formularios del panel**. Tipos de campo soportados: `string`, `text`, `email`, `url`, `int`,
`float`, `bool`, `enum`, `list`, `image`, `images`, `image`, `stringlist`, `stops`,
`timestamp`, `reference`.

Colecciones administradas: `ciudades`, `categorias_lugares`, `categorias_comercios`,
`lugares`, `rutas`, `comercios`, `solicitudes_comercios`, `usuarios`.

### Comandos `spark`

| Comando | Descripción |
|---|---|
| `nica:storage-migrate` | Sube `public/uploads` a Firebase Storage y reescribe URLs. |
| `nica:seed-plantilla` | Crea la ciudad plantilla (Juigalpa), su ruta e imágenes. |
| `nica:seed-imagenes` | Enlaza `imagenUrl`/`galeria` en ciudades, lugares y comercios. |
| `nica:seed-categorias` | Crea `categorias_lugares` desde las categorías usadas. |
| `nica:seed-categorias-comercios` | Crea `categorias_comercios` desde las usadas. |
| `nica:seed-categorias-lugares-jerarquia` | Crea la categoría superior "Lugares turísticos". |
| `nica:seed-categorias-comercios-jerarquia` | Crea "Comercios locales" y anida subcategorías. |
| `nica:seed-taxonomia-comercios` | 7 categorías superiores + subcategorías de comercios. |
| `nica:seed-subcategorias-lugares` | Subcategorías de lugares y remapeo de huérfanos. |
| `nica:seed-coordenadas-ciudades` | Asigna lat/long a las ciudades conocidas. |
| `nica:rebase-imagenes` | Reescribe el host de URLs de imágenes según `Nica.publicBaseUrl`. |

> Los seeds son **idempotentes** (usan `merge`) y llevan `serverTimestamp()`.

---

## API REST

Base: `{baseURL}/api/v1`. **Todas** las rutas requieren autenticación con el header
**`X-API-KEY`** o el parámetro **`?api_key=`** (`ApiKeyFilter`). Si `nica.apiKey` está vacío,
se deniega todo.

### Endpoints

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| GET | `/api/v1/health` | Estado y conectividad con Firestore (conteo de `ciudades`). | API key |
| GET | `/api/v1/version` | Versión del catálogo (`meta/catalogo.updatedAt`). | API key |
| GET | `/api/v1/valoraciones` | Ranking público de valoraciones (sin `uid`). | API key |
| POST | `/api/v1/upload` | Sube una imagen (campo `image`, máx. 5 MB). | API key + token Firebase (rol COMERCIO/EDITOR/ADMIN) |
| GET | `/api/v1/{coleccion}` | Lista documentos (`?search=`, `?limit=` 1..1000). | API key |
| GET | `/api/v1/{coleccion}/{id}` | Obtiene un documento. | API key |
| POST | `/api/v1/{coleccion}` | Crea un documento. | API key |
| PUT / PATCH | `/api/v1/{coleccion}/{id}` | Actualiza un documento (merge). | API key |
| DELETE | `/api/v1/{coleccion}/{id}` | Elimina un documento. | API key |

### Formato de respuesta

**Listar** (`GET /api/v1/{coleccion}`):

```json
{
  "resource": "ciudades",
  "count": 3,
  "data": [
    { "id": "juigalpa", "data": { "nombre": "Juigalpa", "...": "..." } }
  ]
}
```

**Health** (`GET /api/v1/health`):

```json
{ "status": "ok", "project": "nica-explore", "transport": "rest", "ciudades": 3, "time": "..." }
```

**Errores uniformes:** 401 `{"error":"No autorizado"}`, 404 `no_encontrado`,
422 `validacion` (con `errors`), 500 `lectura`/`almacenamiento`.

### Endpoints que consume la app Android

La app **no** consume el CRUD completo. Consume solo:

| Endpoint | Uso en la app | Repositorio |
|---|---|---|
| `GET /api/v1/ciudades` | Catálogo de ciudades. | `ApiRepository.getCiudades()` |
| `GET /api/v1/lugares` | Catálogo de monumentos/lugares. | `ApiRepository.getLugares()` |
| `GET /api/v1/rutas` | Rutas turísticas por ciudad. | `ApiRepository.getRutas()` |
| `GET /api/v1/comercios` | Comercios visibles públicamente. | `ApiRepository.getComercios()` |
| `GET /api/v1/comercios/{id}` | Detalle de un comercio (con fallback a Firestore). | `ApiRepository.getComercio()` |
| `GET /api/v1/categorias_comercios` | Categorías de comercios. | `ApiRepository.getCategoriasComercios()` |
| `GET /api/v1/valoraciones` | Ranking público de estrellas (sin `uid`). | `ApiRepository.getValoraciones()` |
| `GET /api/v1/version` | Detección de cambios del catálogo (cada 15 s). | `CatalogSync` |
| `POST /api/v1/upload` | Subida de imágenes del comercio (multipart, campo `image`). | `ImageUploader` |

**Notas:**
- El cliente HTTP es `ApiClient` con `HttpURLConnection` (sin Retrofit/OkHttp). La base y la
  key vienen de `BuildConfig.API_BASE_URL` y `BuildConfig.API_KEY`, inyectadas desde
  `local.properties` (`nica.apiBaseUrl`, `nica.apiKey`).
- Escritura de catálogo **no** se hace por la API desde la app: los perfiles, comercios
  propios, valoraciones y solicitudes se escriben **directamente en Firestore**.
- El trazado de rutas por carretera usa **OSRM** (`router.project-osrm.org`), un servicio
  externo, no la API de NicaExplore.
- El panel web consume `/api/v1/health?api_key=` desde su dashboard ("Estado de la API").

### Panel web (rutas)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | **Landing pública** de la app (`Home::index` → `Views/landing.php`). |
| GET | `/panel/login` | Formulario de login. |
| POST | `/panel/login` | Procesa login (CSRF). |
| GET | `/panel/logout` | Cierra sesión. |
| GET | `/panel` | Dashboard con conteos por colección. |
| GET | `/panel/{recurso}` | Pantalla CRUD genérica (DataTable + modal). |
| GET | `/panel/{recurso}/data` | Datos JSON para DataTables. |
| POST | `/panel/{recurso}/save` | Crear/actualizar (CSRF). |
| POST | `/panel/{recurso}/delete` | Eliminar (CSRF). |
| POST | `/panel/upload` | Subida de imágenes (CSRF). |

### Landing pública (`/`)

Página promocional de la app Android servida en la **raíz del dominio**
(`GET /` → `Home::index` → `app/Views/landing.php`). Es autónoma: no usa el layout del panel
ni Bootstrap, solo sus propios assets en `public/assets/`:

- `landing.css` — diseño oscuro responsive con la identidad Guardabarranco.
- `landing.js` — menú móvil, cabecera al hacer scroll y animaciones de aparición.
- `img/` — logos (`nicaexplorer_logo`/`nicaexplorer_isotipo`, `itzae`), capturas de la app y
  fotos de ciudades, monumentos y comercios.

Secciones: hero y descarga del APK, características, cómo funciona, ciudades (Juigalpa, León
y Managua), comercios locales, galería de la app, stack tecnológico, preguntas frecuentes,
CTA final y pie de página. El enlace de descarga apunta a GitHub Releases y el panel de
administración sigue disponible en `/panel`.

---

## Modelo de datos (Firestore)

Proyecto `nica-explore`. Colecciones:

| Colección | Documento | Contenido principal |
|---|---|---|
| `usuarios` | `{uid}` | Perfil: `nombre`, `correo`, `rol`, `fechaRegistro`. |
| `ciudades` | `{cityId}` | `nombre`, `lema`, `descripcion`, `historia`, `imagenUrl`, `galeria`, `lat/long`, `activo`. |
| `lugares` | `{lugarId}` | `nombre`, `cityId`, `categoria`, `afluencia`, `historia`, `modeloUnity`, `imagenUrl`, `galeria`, `consejosResponsables`. |
| `rutas` | `{rutaId}` | `cityId`, `nombre`, `duracionEstimada`, `objetivos`, `paradas`, `imagenUrl`. |
| `comercios` | `{id}` | `nombre`, categorías, `ciudad`/`cityId`, contacto, redes, `aprobado`+`activo`, `propietarioUid`. |
| `categorias_lugares` | `{id}` | `nombre`, `categoriaPadre`, `icono`, `orden`, `activo`. |
| `categorias_comercios` | `{id}` | `nombre`, `categoriaPadre`, `icono`, `orden`, `activo`. |
| `solicitudes_comercios` | `{id}` | Solicitudes de incorporación (`estado = pendiente`). |
| `valoraciones` | `{tipo}_{refId}_{uid}` | `tipo`, `refId`, `cityId`, `estrellas` (escrito por la app). |
| `meta` | `catalogo` | `updatedAt` (versión del catálogo). |

- `firestore.rules` protege `uid`/`rol`, bloquea autoelevación y borrado, y limita el cambio
  de rol a `ADMIN`.
- La colección `mail` está bloqueada por reglas y no es funcionalidad activa.
- Al ser NoSQL, no se aplican formas normales; se usan identificadores lógicos (`uid`,
  `cityId`, `refId`).

---

## Roles y seguridad

| Rol | App Android | Panel web |
|---|---|---|
| `USUARIO` | Funciones normales; edita solo su nombre. | Sin acceso. |
| `COMERCIO` | Gestiona sus comercios (crear/editar/eliminar). | Sin acceso. |
| `EDITOR` | — | C/L/M en lugares, rutas, comercios y solicitudes. |
| `AUDITOR` | Consulta el panel móvil (solo lectura). | Solo lectura (L) en todo. |
| `ADMIN` | Panel móvil y control total. | Control total (C/L/M/E). |

La seguridad se aplica en tres capas: `firestore.rules` (cliente), el `AdminPanelScreen` de la
app y la matriz `PanelPermissions` + `PanelPermissionFilter` del panel web.

---

## Unity y visor 3D

Flujo real:

```text
Android: monumentId
        ↓ Intent (cityId, monumentId, escena=visor3d)
Unity: AndroidIntentReceiver
        ↓
Visor3DController
        ↓
prefab correspondiente → Instantiate
```

Mapeo `monumentId` → prefab:

| `monumentId` | Prefab |
|---|---|
| `homenaje_madre_juigalpina` | HomenajeMadreJuigalpina |
| `toro_chontaleno` | ToroChontaleno |
| `estatua_museo_juigalpa` | EstatuaMuseoJuigalpa |
| `tumba_ruben_dario` | TumbaRubenDario |
| `estatua_san_benito` | SanBenito |
| `arbol_vida` | ArbolDeLaVida |
| `ruben_dario` | EstatuaRubenDario |
| `campana_de_la_paz` | CampanaDeLaPaz |
| `los_motivos_del_lobo_escultura` | PazHermanoLobo |

`UnityArActivity` vive en su propia tarea (`singleTask`), maneja Back como "Salir"
(`moveTaskToBack`) y reenvía mensajes a Unity con `UnityPlayer.UnitySendMessage`. El proyecto
Unity fuente **no está en el repo** (`C:/UnityProjects/NicaExplorer`).

> Requiere **teléfono físico ARM64**: Unity solo exporta `arm64-v8a` y ARCore no funciona
> bien en emuladores. `firebase-appcheck-playintegrity` también necesita servicios de Google
> Play válidos.

---

## Instalación y ejecución

### App Android

1. Clonar el repositorio:

   ```powershell
   git clone https://github.com/Olivergg127/NicaExplorer-AndroidStudio.git
   cd NicaExplorer-AndroidStudio
   ```

2. Abrir en Android Studio y **Sync Project with Gradle Files**.
3. Colocar `app/google-services.json` (proyecto `nica-explore`) — **no versionado**.
4. Confirmar que `unityLibrary/` está presente.
5. Configurar el backend en `local.properties` (no versionado):

   ```properties
   nica.apiBaseUrl=https://nicaexplorer-backend.onrender.com
   nica.apiKey=<API key del backend>
   ```

6. Compilar e instalar:

   ```powershell
   .\gradlew.bat :app:assembleDebug          # comando principal
   .\gradlew.bat :app:assembleDebug -PskipIl2CppBuild   # más rápido en pruebas
   adb devices
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

Release firmada (requiere `keystore.properties` local, fuera de Git):

```powershell
.\gradlew.bat :app:assembleRelease
```

### Panel web / Backend

Requisitos: PHP 8.3 (`intl`, `fileinfo`, `curl`, `openssl`, `mbstring`, `zip`), Composer 2 y
credenciales de Google Cloud/Firebase.

```powershell
cd nicaexp_web
composer install
Copy-Item .env.example .env
php spark key:generate
php spark serve --host 0.0.0.0 --port 8080
```

- Panel (local): <http://localhost:8080/panel>
- API (local): <http://localhost:8080/api/v1>
- Desde otro dispositivo: `http://<IP-DE-TU-PC>:8080`

`.env` mínimo:

```ini
nica.projectId        = nica-explore
nica.apiKey           = <clave para la API>
nica.adminUser        = admin
nica.adminPassword    = <clave del panel>
nica.firestoreTransport = rest
```

> Usa el prefijo en **minúsculas** (`nica.`): CodeIgniter 4 resuelve las claves por el nombre
> corto de la clase en minúsculas; `Nica.` solo funciona en Windows y **falla en Linux**.

Credenciales de Firestore (dos opciones):

1. **ADC** (desarrollo): dejar `nica.credentialsFile` vacío y ejecutar
   `gcloud auth application-default login`.
2. **Cuenta de servicio**: apuntar `nica.credentialsFile` al JSON (**nunca** versionarlo).

### Acceso desde un dispositivo Android real

- **Opción A — túnel USB** (si la Wi-Fi aísla clientes):

  ```powershell
  adb reverse tcp:8080 tcp:8080
  # local.properties: nica.apiBaseUrl=http://localhost:8080
  ```

- **Opción B — red local**: `nica.apiBaseUrl=http://<IP-LAN>:8080` y abrir el puerto en el
  Firewall de Windows (PowerShell como administrador):

  ```powershell
  netsh advfirewall firewall add rule name="NicaExplorer Backend 8080" dir=in action=allow protocol=TCP localport=8080
  ```

Al cambiar de host, reescribe las URLs de las imágenes con `php spark nica:rebase-imagenes`.

---

## Despliegue

### Backend en Render (plan free)

El backend se despliega como **Web Service Docker** en Render. El archivo `render.yaml` de la
raíz describe el servicio (Root Directory = `nicaexp_web`, Dockerfile en `nicaexp_web/`).

- **URL pública:** <https://nicaexplorer-backend.onrender.com>
- **Landing pública:** servida en la **raíz** (`/`); el panel de administración sigue en
  `/panel` y el `healthCheckPath` del blueprint se mantiene en `/panel/login`.
- **Build:** `nicaexp_web/Dockerfile` (PHP 8.3 + Apache).
- **Arranque:** `docker/entrypoint.sh` genera el `.env` desde las variables de entorno
  (`docker/write-env.php`) y ajusta Apache al `PORT`.
- **Secreto:** la cuenta de servicio se sube como **Secret File** `firebase.json`
  (Render la monta en `/etc/secrets/firebase.json`; el entrypoint la copia a `writable/`).
- **Auto-deploy:** activado en cada push a `main`.

Variables de entorno (definidas en el panel de Render, no versionadas):

| Variable | Valor |
|---|---|
| `nica_credentialsFile` | `/etc/secrets/firebase.json` |
| `nica_projectId` | `nica-explore` |
| `nica_firestoreTransport` | `rest` |
| `nica_githubRepo` | `Olivergg127/NicaExplorer-assets` |
| `nica_githubToken` | token de GitHub (escritura de contenido) |
| `nica_githubBranch` / `nica_githubPath` | `main` / `uploads` |
| `nica_apiKey` | clave de la API REST |
| `nica_adminUser` / `nica_adminPassword` | credenciales del panel |
| `encryption_key` | salida de `php spark key:generate` |

Notas del plan gratuito: el servicio **duerme tras 15 min** sin tráfico y despierta en ~1 min;
concede 750 h/mes. La app consulta `/api/v1/version` cada 15 s, lo que en la práctica lo
mantiene despierto.

### Distribución del APK

GitHub Releases distribuye el APK:
<https://github.com/Olivergg127/NicaExplorer-AndroidStudio/releases/latest/download/NicaExplorer.apk>

### Migración a Microsoft Azure (propuesta, no implementada)

Al estar el backend dockerizado y configurado por variables de entorno, migrar a **Azure App
Service (Web App for Containers)** es viable. Puntos a adaptar: el **puerto**
(`WEBSITES_PORT` en lugar de `PORT`), la **URL base** (`APP_BASE_URL` en lugar de
`RENDER_EXTERNAL_URL`) y el **secreto** de Firebase (Key Vault/mount). Consideración
principal: App Service **no ofrece un tier gratuito comparable** al de Render.

---

## Alcance y transparencia

- Es un **prototipo académico** del equipo Los Punto y Coma.
- **No** se ofrecen GPS de navegación, rutas en tiempo real ni afluencia en tiempo real. La
  afluencia y la duración de las rutas son **orientativas**.
- La ruta turística está desarrollada principalmente para **Juigalpa** (ciudad con comercios
  cargados); otras ciudades muestran "Rutas próximamente".
- El mapa muestra coordenadas reales, pero el catálogo está limitado a Juigalpa, León y
  Managua.
- **Nunca** se versionan reglas, claves privadas, tokens, contraseñas, keystores ni
  credenciales (ADC).

---

## Equipo

- Oliver Javier Gutiérrez Castro — Desarrollo
- Joseph Esau Centeno Urbina — Desarrollo
- Verónica Michelle Robleto Trujillo — Diseño
- Arlen Rodolfo Urbina Urbina — Comunicación
- Elvis Josué Miranda Méndez — Marketing

---

## Documentación adicional

| Archivo | Contenido |
|---|---|
| `AGENTS.md` | Reglas para agentes de código y zonas delicadas. |
| `INIT.md` | Resumen del proyecto para nuevos colaboradores. |
| `ARCHITECTURE.md` | Arquitectura detallada de la app y del backend. |
| `DEVELOPMENT.md` | Entorno, build y herramientas. |
| `PROJECT_MAP.md` | Mapa de archivos y responsabilidades. |
| `PROJECT_CONTEXT.md` | Visión de producto y decisiones. |
| `CONVENTIONS.md` | Reglas de estilo. |
| `ROADMAP.md` | Estado real y próximos pasos. |
| `CHANGELOG_AGENT.md` | Bitácora de cambios de agentes. |
| `nicaexp_web/README.md` | Detalle del backend y del panel web. |
