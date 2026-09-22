# Agent Change Log

> Bitácora de cambios hechos por agentes de código. Agrega una entrada por cada sesión con
> cambios importantes. No sustituye al historial de Git; lo complementa con contexto.
> Formato: fecha, objetivo, archivos, cambios, build, resultado, riesgos y pendientes.

## 2026-09-21

### App/Backend — Exploración pública, cuentas COMERCIO y comercios administrables
- Objetivo: permitir entrar sin registrarse, diferenciar usuario normal y usuario comercio, y
  que cada cuenta COMERCIO administre sus propios comercios con aprobación del admin.
- Decisión del usuario: subida real de imágenes (reutilizando el almacén del backend:
  GitHub > Firebase Storage > local) y comercios nuevos **pendientes de aprobación**.
  El flujo de "Solicitud de comercio" se reemplaza por el registro directo.
- Backend (`nicaexp_web`):
  - Nuevo `app/Controllers/Api/Upload.php` + ruta `POST /api/v1/upload`: sube imágenes
    verificando el ID token de Firebase (header `Authorization: Bearer`) y el rol
    `COMERCIO/EDITOR/ADMIN`; usa `ImageStorage` (prioridad GitHub).
  - `Config/NicaResources.php` (`comercios`): nuevos campos `logoUrl`, `diasAtencion`,
    `redesSociales`, `servicios` (stringlist), `productos` (stringlist), `infoAdicional`,
    `aprobado` (bool, default true) y `propietarioUid` (internal). El admin aprueba desde
    el panel.
- App Android:
  - `UserRole`: nuevo rol `COMERCIO` (no accede al panel web).
  - `Comercio`: campos nuevos (`logoUrl`, `galeria`, `diasAtencion`, `redesSociales`,
    `servicios`, `productos`, `infoAdicional`, `aprobado`, `propietarioUid`) y
    `visiblePublicamente = activo && aprobado`.
  - `RegisterScreen`: selector "Usuario normal" / "Usuario comercio"; el registro
    (`FirebaseRepository.registerUser`) guarda el rol elegido.
  - **Modo visitante:** `SplashScreen` entra siempre a `Home`; `LoginScreen` añade
    "Continuar sin registrarme"; `HomeScreen`/`ProfileScreen`/`ConfiguracionScreen`
    distinguen invitado (sin sesión) de usuario.
  - Nuevas pantallas `MisComerciosScreen` (lista, estados, eliminar) y
    `ComercioFormScreen` (crear/editar con subida de portada, logo y galería).
    ViewModels `MisComerciosViewModel` y `ComercioFormViewModel`.
  - `ImageUploader`: subida multipart a `/api/v1/upload` con token de Firebase.
  - `ComercioDetalleScreen`: muestra logo/galería/días/servicios/productos/info adicional
    y avisa si el comercio está pendiente o inactivo; `ComercioDetalleViewModel` cae a
    Firestore como respaldo.
  - Navegación: rutas `mis_comercios` y `comercio_form`; guarda por rol para `MIS_COMERCIOS`.
    El botón de comercios ahora lleva a generar cuenta/administrar ("Registra tu comercio").
  - `ApiRepository`: mapea los campos nuevos y filtra el catálogo público por
    `activo && aprobado`.
- Seguridad (`firestore.rules`):
  - `usuarios` permite auto-registro con rol `USUARIO` o `COMERCIO`.
  - `comercios`: lectura pública si `aprobado && activo` (o dueño/admin); creación solo
    por `COMERCIO/EDITOR/ADMIN` a su propio uid y siempre `aprobado=false, activo=false`;
    el dueño puede editar pero **no** cambiar `propietarioUid` ni `aprobado`; borrado solo
    del dueño o admin.
- Build: `.\gradlew.bat :app:assembleDebug` — **BUILD SUCCESSFUL**.
- Pendiente: desplegar el backend (endpoint de subida) en Render; probar en dispositivo
  el ciclo completo (registro comercio → aprobación en panel → activar → mapa).
- Nota: el comercio creado desde la app usa ID aleatorio de Firestore (no slug) para que
  no sea adivinable mientras está pendiente.

### App — Texto expandible ("Ver más") y renombre en rutas creativas
- Objetivo: evitar bloques largos de texto mostrando "Ver más/Ver menos" solo cuando el
  texto se desborda, y renombrar "Cómo funciona esta ruta" → "Objetivos de esta ruta".
- Archivos:
  - `app/.../ui/components/TextoExpandible.kt` — nuevo componente reutilizable; recorta a
    `maxLines` y muestra el botón solo si hay desborde real (`onTextLayout.hasVisualOverflow`).
  - `app/.../ui/screens/CatalogScreen.kt` — descripción del lugar, "Historia" de
    *Información adicional* (nuevo `InfoRow(expandible = true)`) y descripción/historia de
    la ciudad usan `TextoExpandible`.
  - `app/.../ui/screens/ComercioDetalleScreen.kt` — descripción del comercio.
  - `app/.../ui/screens/MapaScreen.kt` — descripción del comercio en el bottom sheet.
  - `app/.../ui/screens/RutasInteligentesScreen.kt` — descripción de la ruta y renombre del
    encabezado a "Objetivos de esta ruta".
- Build: `.\gradlew.bat :app:assembleDebug` — BUILD SUCCESSFUL.
- Pendiente: no se aplicó a las tarjetas de lista (`PlaceCard` ya recorta a 2 líneas;
  `CityCard` tiene `onClick` y el botón chocaría con el tap).

### Unity — Integración del modelo "Paz Hermano Lobo" (León) y reexport de `unityLibrary`
- Objetivo: agregar la escultura "Paz Hermano Lobo" al visor 3D.
- Proyecto Unity fuente (fuera del repo, `C:/UnityProjects/NicaExplorer`):
  - `Assets/Models/Leon/estatuapazhermanolobo.fbx` (copiado desde Descargas).
  - `Assets/Editor/PrepararPrefabPazHermanoLobo.cs` — nuevo; importa el FBX, crea el prefab
    `PazHermanoLobo` (raíz identidad, hijo con rotación `(-90,180,0)`, altura normalizada) y
    registra `los_motivos_del_lobo_escultura` en el catálogo de `Visor3DController`.
  - `Assets/Prefabs/PazHermanoLobo.prefab` y `Assets/Scenes/Visor3D.unity` (catálogo).
  - Verificación: previews renderizadas en batchmode confirmaron el modelo de pie y **de
    frente a la cámara** (la cámara del visor está en `-Z` mirando a `+Z`).
- Repo Android (`unityLibrary`):
  - Se reexportó la librería desde Unity y se actualizó únicamente
    `src/main/assets/bin/Data/` (`data.unity3d`, `boot.config`, `unity_app_guid`).
  - `global-metadata.dat`, `libunity.so`, `lib_burst_generated.so`, `libs/` y el
    `build.gradle` con el parche de símbolos se conservaron (idénticos) para reutilizar el
    IL2CPP precompilado. `:unityLibrary:buildIl2Cpp` quedó `UP-TO-DATE`.
- Build: `.\gradlew.bat :app:assembleDebug` — BUILD SUCCESSFUL (APK incluye el nuevo
  `data.unity3d` de ~100 MB).
- Docs: `ARCHITECTURE.md`, `README.md`, `PROJECT_CONTEXT.md` y `ROADMAP.md` actualizados con
  el mapeo `los_motivos_del_lobo_escultura` → `PazHermanoLobo`.
- Verificación en el panel/API: el lugar ya existía en Firestore como
  **"Los Motivos del Lobo (Escultura)"** (`id = los_motivos_del_lobo_escultura`, León) con
  `modeloUnity` vacío; se actualizó a `PazHermanoLobo` por la API (`PATCH /api/v1/lugares/...`).
- Pendiente: probar el botón "Ver en 3D" en un dispositivo real.

## 2026-09-20

### App — Rediseño de la vista de mapa ("EjemploVista")
- Objetivo: rehacer la vista del mapa basándose en la imagen `EjemploVista.jpeg`
  (estructura y estilo).
- Archivos:
  - `app/.../ui/screens/MapaPrincipalScreen.kt` — reescrita: encabezado (ciudad + campana),
    título/subtítulo, buscador con menú de filtros (Todo/Lugares/Comercios), rejilla de
    categorías (desde `categoriaPadre` de los comercios, con ícono/color por tipo), mapa
    con esquinas redondeadas y botones flotantes (navegación, capas, mi ubicación).
  - `app/.../map/MapaMarkerFactory.kt` — nuevo `ubicacionIcon` (punto azul con halo).
  - Pines de comercios **coloreados por categoría** (`MapaPin.color`).
- Build/pruebas:
  - `.\gradlew.bat :app:assembleDebug` — BUILD SUCCESSFUL.
  - Verificado en dispositivo el diseño, el selector de ciudad, el menú de filtros y el
    filtrado por categoría (Restaurantes). El color por categoría quedó compilando; no se
    pudo verificar visualmente porque el dispositivo se desconectó.
- Pendiente: clustering; lugares sin coordenadas no aparecen.

### Roles y permisos — Nuevo rol Editor + autorización por rol en el panel web
- Objetivo: implementar 4 roles (ADMIN, EDITOR, AUDITOR, USUARIO) con matriz de permisos
  por módulo (C/L/M/E), aplicada en backend/UI sin reemplazar la autenticación existente.
  El rol USUARIO no accede al panel.
- Archivos nuevos:
  - `nicaexp_web/app/Libraries/PanelPermissions.php` — matriz rol/módulo/operación.
  - `nicaexp_web/app/Filters/PanelPermissionFilter.php` — autoriza cada acción del panel.
- Archivos actualizados (backend):
  - `Panel/Auth.php` (login con cuentas Firebase vía Identity Toolkit + rol desde Firestore;
    admin de `.env` como respaldo), `Panel/Dashboard.php` (resumen filtrado),
    `Panel/Resources.php` (bloquea borrar el propio usuario o al último ADMIN),
    `Config/Nica.php` (`webApiKey`), `Config/Filters.php`, `Config/Routes.php`,
    `Config/NicaResources.php` (EDITOR en el enum de roles), `Views/layout/panel.php`,
    `Views/panel/{resource,dashboard,login}.php`, `public/assets/panel.js`,
    `docker/write-env.php` (`nica.webApiKey`).
- Archivos actualizados (app): `model/UserRole.kt` (EDITOR), `ui/screens/AdminPanelScreen.kt`,
  `ui/screens/ProfileScreen.kt`, `ui/viewmodels/UserViewModel.kt`.
- Otros: `firestore.rules` (EDITOR como rol asignable).
- Hallazgos:
  - CI4 no acepta filtros separados por coma en una ruta; debe usarse un array
    (`['filter' => ['panelauth', 'panelcan']]`) o lanza `FilterException`.
  - Firestore rechaza ids de documento que empiezan por `__` (reservados).
- Build/pruebas:
  - `php -l` en todos los PHP y `node --check panel.js` — OK.
  - Matriz de permisos verificada con script (24 aserciones, todas OK).
  - En Render (deploy `aa027d5`): 20/20 pruebas con un usuario Firebase temporal
    (EDITOR/AUDITOR/USUARIO) y con el admin de `.env`; usuario temporal eliminado al final.
  - `.\gradlew.bat :app:assembleDebug` — BUILD SUCCESSFUL.
- Riesgos/pendientes:
  - No existe bitácora de auditoría (el Auditor no tiene registros que revisar).
  - `solicitudes_comercios` se asocia a los permisos del módulo `comercios` (decisión).

## 2026-09-19

### Agent / task — Preparar el backend para desplegar en Render + Firebase Storage
- Objetivo: dejar el backend `nicaexp_web` listo para desplegarse en un hosting gratuito
  (Render, Web Service Docker) y sacar las imágenes del disco local (efímero en la nube)
  usando Firebase Storage. No se desplegó: faltan cuentas/credenciales del equipo.
- Archivos creados:
  - `nicaexp_web/app/Libraries/ImageStorage.php` — subida a Firebase Storage (URL de
    descarga con token) con fallback local.
  - `nicaexp_web/app/Commands/StorageMigrate.php` — comando `nica:storage-migrate`.
  - `nicaexp_web/Dockerfile`, `nicaexp_web/.dockerignore`,
    `nicaexp_web/docker/entrypoint.sh`, `nicaexp_web/docker/write-env.php`.
  - `render.yaml` (Blueprint del servicio en la raíz del repo).
- Archivos actualizados:
  - `app/Config/Nica.php` (nuevo `storageBucket`), `app/Libraries/FirebaseFactory.php`
    (`storage()`/`bucket()`), `app/Controllers/Panel/Resources.php` (upload),
    `app/Commands/SeedImagenes.php` y `SeedPlantilla.php` (usan `ImageStorage`).
  - `nicaexp_web/.env.example` (claves `nica.*` en minúsculas + `nica.storageBucket`).
  - `nicaexp_web/README.md`, `ARCHITECTURE.md`, `ROADMAP.md`, `AGENTS.md` (este log).
- Cambios:
  - `ImageStorage` sube a `uploads/<archivo>` del bucket y devuelve la URL
    `firebasestorage.googleapis.com/...`; con `nica.storageBucket` vacío se conserva el
    modo local `public/uploads`.
  - `nica:storage-migrate` sube las imágenes locales y reescribe en Firestore las URLs
    `.../uploads/...` por las de Storage (sin borrar archivos locales).
  - Docker: PHP 8.3 + Apache, docroot `public`, `AllowOverride All`, puerto tomado de
    `PORT`; el `.env` se genera al arrancar desde variables de entorno.
  - **Hallazgo importante:** CI4 resuelve las claves de config por el nombre corto de la
    clase en minúsculas. En Windows `Nica.publicBaseUrl` funciona por `getenv()`
    (case-insensitive), pero en Linux **no se leería**; el contenedor genera el `.env`
    con `nica.*`.
- Build:
  - `php -l` en los 8 archivos PHP tocados → sin errores de sintaxis.
  - `php spark list` y `php spark nica:storage-migrate` → comando registrado y
    `ImageStorage` carga correctamente (avisa que falta `nica.storageBucket`).
  - No se pudo construir la imagen Docker: `docker` no está instalado en esta máquina.
- Resultado: código y despliegue preparados; funcionalidad local intacta (fallback).
- Riesgos/notas:
  - Sin `docker` local no se validó la imagen; el primer deploy en Render debe revisarse.
  - El bucket real (`*.firebasestorage.app` vs `*.appspot.com`) y los permisos de la
    cuenta de servicio deben confirmarse en el proyecto `nica-explore`.
- Pendiente:
  - Crear el servicio en Render, el Secret File `firebase.json` y las variables de entorno.
  - Ejecutar `php spark nica:storage-migrate` y apuntar la app a la URL de Render.

### Agent / task — Migración real de imágenes a GitHub y credenciales de Firestore
- Objetivo: ejecutar la migración de imágenes con un proveedor viable (Firebase Storage
  está bloqueado: `billingEnabled: false` en `nica-explore`).
- Decisiones:
  - Imágenes → repo público **`Olivergg127/NicaExplorer-assets`**; `ImageStorage` ahora
    prioriza **GitHub > Firebase Storage > local**.
  - Firestore → cuenta de servicio **`nicaexp-backend@nica-explore.iam.gserviceaccount.com`**
    con `roles/datastore.user` y clave JSON (guardada fuera del repo, en
    `%TEMP%\opencode\nicaexp-secrets\`).
- Cambios de código:
  - `app/Libraries/ImageStorage.php`: subida por GitHub Contents API (`raw.githubusercontent.com`).
  - `app/Config/Nica.php`: `githubRepo`, `githubToken`, `githubBranch`, `githubPath`.
  - `docker/write-env.php`, `.env.example` y `render.yaml`: variables `nica_github*`.
- Ejecutado:
  - `php spark nica:storage-migrate` → **20 imágenes subidas** y **16 documentos** de
    Firestore actualizados a URLs de GitHub.
  - Verificado: `https://raw.githubusercontent.com/.../uploads/juigalpa.jpg` → HTTP 200
    `image/jpeg`.
  - `php -l` en los PHP tocados → sin errores.
- Pendiente:
  - Desplegar en Render vía API y apuntar la app a la URL pública.
  - Reemplazar el token de `gh` por un PAT *fine-grained* limitado al repo de assets.

### Agent / task — Despliegue en Render (API) y verificación end-to-end
- Objetivo: desplegar el backend en Render y dejarlo operativo sin depender de localhost.
- Hecho vía API de Render (`POST /v1/services`) con la API key del workspace:
  - Servicio `nicaexplorer-backend` (`srv-dand1gh42hec73e3eju0`, free, Docker, root
    `nicaexp_web`, health check `/panel/login`, auto-deploy en `main`).
  - URL pública: <https://nicaexplorer-backend.onrender.com>.
  - Variables de entorno (`nica_*`), Secret File `firebase.json` y credenciales del panel
    generadas (guardadas fuera del repo en `%TEMP%\opencode\nicaexp-secrets\`).
- Incidencias corregidas (commits `f6b41af`, `0ad5a67`, `ceedd46`):
  - Build fallaba por `mbstring` → se añadió `libonig-dev`.
  - Firestore no leía el Secret File (permisos de `www-data`) → el entrypoint lo copia a
    `writable/firebase.json`.
  - `Call to undefined function bccomp()` → se añadió la extensión `bcmath`.
- Verificado:
  - `/api/v1/health` → `ok`, `nica-explore`, `rest`, 4 ciudades.
  - `/api/v1/version` y `/api/v1/ciudades/juigalpa` con `imagenUrl`/`galeria` en GitHub.
  - Login del panel (`POST /panel/login`) → 200 y panel cargado.
  - `local.properties` actualizado: `nica.apiBaseUrl` → Render y `nica.apiKey` nueva.
- Pendiente:
  - PAT *fine-grained* para el repo de assets; rotar API key/password; quitar
    `usesCleartextTraffic`.

### Agent / task — Panel: IDs automáticos, selector de ubicación en mapa y quitar imagen
- Objetivo: simplificar los formularios del panel (sin identificadores visibles),
  permitir fijar latitud/longitud con un mapa y quitar la portada para reemplazarla.
- Cambios:
  - `app/Config/NicaResources.php`: `ciudades` y `lugares` pasan de `id` manual a
    `auto` con `slug_source => 'nombre'`; también `rutas`, `comercios` y
    `categorias_lugares`. `imagenKey` se marca `internal`.
  - `app/Libraries/FirestoreRepository.php`: genera el id como slug (`toro_chontaleno`)
    y evita colisiones con sufijo `_2`, `_3`… cuando no se envía `_id`.
  - `Panel\Resources`: `formFields()` y `columns()` ocultan identificadores y campos
    `internal`; se pasa `hasLocation` a la vista.
  - `app/Views/panel/resource.php`: se elimina el bloque "Identificador"; se añade el
    selector de ubicación (Leaflet + Nominatim) y el botón "Quitar imagen".
  - `public/assets/panel.js` / `panel.css`: mapa que fija lat/lng al hacer clic o
    arrastrar el marcador, búsqueda por dirección, y limpieza del preview de imagen.
  - `app/Views/layout/panel.php`: carga de Leaflet 1.9.4 (CSS/JS) desde CDN.
- Verificado en Render (commit `c3c905d`):
  - `lugares`, `ciudades` y `comercios` muestran el mapa; `usuarios` no (sin coords).
  - Sin bloque de ID, sin `monumentId` ni `imagenKey` en formularios/tablas.
  - Creación real vía panel sin `_id` → id `prueba_slug_qa`; documento leído y eliminado.
- Pendiente:
  - Revisar en pantalla (visual) el mapa y el botón de quitar imagen.

### Agent / task — App: mapa de la ruta turística con paradas numeradas
- Objetivo: mostrar en `RutasInteligentesScreen` un mapa con pines numerados por parada
  y el camino que las une; al pulsar un pin, desplazar la lista a esa parada.
- Cambios (app Android):
  - `model/Place.kt`: nuevos campos `latitud`/`longitud` (nullable).
  - `data/ApiRepository.kt`: mapea `latitud`/`longitud` de los lugares.
  - `map/MapaMarkerFactory.kt`: `numeroIcon()` dibuja un marcador circular con el número
    (texto adaptado a 1/2/3 dígitos).
  - `ui/components/RutaMapa.kt` (nuevo): `MapView` de MapLibre en `AndroidView`,
    pines numerados, `Polyline` con el trazado por carretera de OSRM (respaldo: líneas
    rectas) y encuadre automático de las paradas.
  - **Clave del renderizado (importante para no repetir el problema):** gestionar el
    ciclo de vida del `MapView` a mano dentro de Compose (llamar `onCreate`/`onStart`/
    `onResume` desde efectos) dejaba la vista en **negro** porque `getMapAsync` nunca
    devolvía el mapa. El patrón que SÍ funciona es crear el `MapView` dentro del
    `factory` del `AndroidView`, llamar `MapLibre.getInstance(viewContext)` y
    `getMapAsync` ahí mismo, y dejar que el `MapView` se gestione solo (sin
    `onCreate`/`onStart` manuales).
  - El mapa es el **primer ítem del `LazyColumn`** (alto `360.dp`, con `key`) para que
    haga scroll con el contenido. Se usa `textureMode(true)` (TextureView) para que se
    desplace y recorte bien dentro de un contenedor con scroll.
  - Encuadre solo una vez: `rememberSaveable` guarda si ya se encuadró y la última cámara
    (capturada con `addOnCameraIdleListener`); al salir y volver a la vista se **restaura**
    la cámara sin repetir el zoom. Se limita el zoom máximo a `16.5` para que no se acerque
    demasiado cuando las paradas están juntas.
  - Gestos del mapa: el `MapView` llama `requestDisallowInterceptTouchEvent(true)` al
    tocarlo, para que el scroll de la lista no le robe el arrastre/zoom.
  - Ruta por calles: `RutaMapaRepository` consulta OSRM y **exige el header
    `User-Agent`** (sin él el servidor devolvía vacío y quedaban líneas rectas).
  - `ui/screens/RutasInteligentesScreen.kt`: el mapa es el primer ítem del `LazyColumn`
    y hace scroll con la lista de paradas (`LazyListState`); el clic en un pin hace
    `animateScrollToItem` a su tarjeta.
- Nota de dinamismo: el número de paradas es el que devuelve el backend por ciudad
  (`rutas.paradas`); se generan tantos pines como paradas con coordenadas existan.
- Build/instalación: `:app:assembleDebug` → **BUILD SUCCESSFUL**; `adb install -r` → Success.
- Pendiente:
  - Los pines solo aparecen si el lugar/comercio tiene `latitud`/`longitud` en Firestore;
    cargar coordenadas desde el panel (selector de mapa) o seeds.

### Agent / task — Categorías de comercios + sección "Comercios recomendados" en la ruta
- Objetivo: poder clasificar los comercios por categorías y mostrar en la vista de ruta
  una sección de comercios recomendados con un selector de categorías.
- Backend (`nicaexp_web`):
  - Nueva colección `categorias_comercios` con CRUD en el panel (menú *Ecosistema local*),
    generada desde `Config\NicaResources` (mismo patrón que `categorias_lugares`).
  - `comercios.categoria` pasa de texto libre a **selector** (`reference` a
    `categorias_comercios`, guarda el `nombre`).
  - Comando `php spark nica:seed-categorias-comercios` (idempotente); ejecutado en local:
    creó `restaurante` y `cafeteria_y_restaurante`.
  - Desplegado en Render (commit `e7fc258`); `/api/v1/categorias_comercios` operativo.
- App:
  - `model/CategoriaComercio.kt` y `ApiRepository.getCategoriasComercios()`.
  - `ComerciosViewModel` ahora carga también las categorías (`ComerciosUiState.categorias`).
  - `RutasInteligentesScreen`: al final, sección **"Comercios recomendados"** con un
    carrusel de chips de categorías (`LazyRow` + `FilterChip`, "Todos" por defecto) que
    filtra los comercios activos de la ciudad; se reutiliza `ComercioCard`.
- Verificado en el dispositivo: la sección aparece; al elegir "cafeteria y restaurante"
  queda solo Coffee Break.
- Pendiente:
  - Revisar el resto de pantallas donde se muestre/edite `categoria` (SolicitudComercioScreen
    sigue usando texto libre).

### Agent / task — "Comercios recomendados" al detalle de ciudad y renombre a "Rutas Creativas"
- Objetivo: sacar los comercios recomendados de la vista de rutas, llevarlos al final del
  detalle de ciudad, con nube de categorías + carrusel, y renombrar "Rutas inteligentes".
- Cambios:
  - `RutasInteligentesScreen`: se elimina la sección "Comercios recomendados"; el título
    pasa a **"Rutas Creativas"** (también el acceso `RutasInteligentesAccessCard` en
    `CatalogScreen` y la tarjeta "Cómo funciona esta ruta").
  - `CatalogScreen` (detalle de ciudad): al final, **"Comercios recomendados"** con **nube
    de categorías** (`FlowRow` de `FilterChip`) y **carrusel** (`LazyRow` de
    `ComercioMiniCard`) de los comercios de la categoría elegida. Por defecto se selecciona
    **"Restaurante"** (comparación ignorando mayúsculas y espacios).
- Verificado en el dispositivo: Juigalpa muestra la nube y, con "Restaurante" por defecto,
  el carrusel lista AmerriPizza y mi choza; la vista de ruta ya no tiene esa sección.
- Nota: solo cambió el texto visible; las clases/rutas internas conservan su nombre
  (`RutasInteligentesScreen`, `Routes.RUTAS_INTELIGENTES`) para no tocar la navegación.

### Agent / task — Mapa principal: pines, selector de ciudad, GPS y recomendaciones
- Objetivo: mapa principal con pines de lugares/comercios, selector de ciudad, filtro por
  tipo y detección de la ciudad actual.
- App:
  - Nueva `ui/screens/MapaPrincipalScreen.kt` (Compose) con MapLibre (`textureMode`):
    pines de **lugares** (azul) y **comercios** (turquesa), encuadre por ciudad y clic en
    pin → detalle del lugar/comercio.
  - Panel inferior: chips de ciudades ("Todas" + ciudades + **Mi ubicación**) y filtro por
    tipo (**Todos / Lugares / Comercios**); botón **"Inicio"** para limpiar la selección.
  - GPS con **`LocationManager`** (sin dependencia nueva): `data/UbicacionHelper.kt`
    (permiso, última ubicación conocida, ciudad más cercana por Haversine a ≤ 25 km).
  - `map/MapaMarkerFactory.kt`: `puntoIcon()` (pin circular por color).
  - `AndroidManifest.xml`: se **re-habilitan** `ACCESS_COARSE/FINE_LOCATION` (antes se
    eliminaban con `tools:node="remove"`).
  - `navigation/AppNavigation.kt`: el ítem "Mapa" abre `MapaPrincipalScreen` (ya no lanza
    la `MapaActivity` nativa, que queda como legado).
  - `MapaViewModel` (antes sin usar) se reutiliza para cargar comercios con coordenadas.
- Backend:
  - `php spark nica:seed-coordenadas-ciudades` (idempotente): asigna lat/lng a Juigalpa,
    León, Managua y Matagalpa. Ejecutado: solo Juigalpa tenía coordenadas.
- Verificado en el dispositivo: pines visibles; seleccionar **Managua** centra el mapa;
  **"Mi ubicación"** detecta Juigalpa y muestra sus pines.
- Pendiente:
  - Lugares/comercios sin coordenadas no aparecen; cargarlas desde el panel (mapa).
  - Auto-scroll para dejar visible el chip de la ciudad seleccionada (mejora menor).

### Agent / task — Jerarquía en "Categorías de lugares" (categoría superior + subcategorías)
- Objetivo: que `categorias_lugares` soporte una categoría superior y subcategorías.
- Backend:
  - `Config/NicaResources`: nuevo campo `categoriaPadre` (tipo `reference` a
    `categorias_lugares`, guarda el **nombre**). La tabla muestra la columna
    "Categoría superior" y el modal un select (mismo CRUD genérico).
  - `php spark nica:seed-categorias-lugares-jerarquia` (idempotente): crea la raíz
    **"Lugares turísticos"** (`lugares_turisticos`) y anida las 7 categorías existentes.
    Ejecutado en local.
  - Desplegado en Render (`64fc9ce`).
- Verificado: la raíz queda sin padre, las 7 subcategorías con "Lugares turísticos" y la
  tabla/modal del módulo muestran el nuevo campo.
- Nota: se guarda el nombre del padre (no el id) para que la tabla sea legible; si se
  renombra la categoría superior, las subcategorías no se actualizan solas.

### Agent / task — Selects dependientes: categoría superior vs. subcategoría
- Objetivo: en los modales, separar categorías (raíz) de subcategorías y que la subcategoría
  dependa de la categoría superior elegida (antes se mezclaban).
- Backend:
  - `Panel\Resources::referenceOptions()`: cada opción incluye su `parent` y soporta
    `only_root` (solo categorías raíz).
  - `Panel\Resources::formFields()`: expone `dependsOn`.
  - `Views/panel/resource.php`: el JSON `NICA_RESOURCE` ahora incluye `references`.
  - `Config/NicaResources`: `categorias_lugares.categoriaPadre` con `only_root`; `lugares`
    con `categoriaPadre` (solo raíz) y `categoria` ("Subcategoría") con
    `'depends_on' => 'categoriaPadre'`.
- Frontend `panel.js`: selects dependientes (las subcategorías se filtran por la categoría
  superior), infiere la superior al editar a partir de la subcategoría guardada, y no ofrece
  la raíz como subcategoría.
- Desplegado en Render (`8e19e1c`).
- Verificado (config): `categoriaPadre` muestra solo "Lugares turísticos"; `categoria` trae
  las 7 subcategorías con su `parent` y `dependsOn = categoriaPadre`.
- Nota: `categorias_comercios` todavía **no** tiene jerarquía, por lo que sus selects no se
  ven afectados. Falta probar la interacción en el navegador.

### Agent / task — Jerarquía y selects dependientes en Categorías de comercios
- Objetivo: replicar en `categorias_comercios` la jerarquía de lugares (categoría superior +
  subcategorías) y adaptar el modal de comercios.
- Backend:
  - `Config/NicaResources`: `categorias_comercios.categoriaPadre` (`only_root`, `list`);
    `comercios` con `categoriaPadre` (solo raíz) y `categoria` ("Subcategoría") con
    `'depends_on' => 'categoriaPadre'`.
  - `php spark nica:seed-categorias-comercios-jerarquia` (idempotente): crea la raíz
    **"Comercios locales"** (`comercios_locales`) y anida las 2 categorías existentes.
    Ejecutado en local.
  - Se reutiliza el mecanismo genérico de selects dependientes (`referenceOptions` con
    `parent`/`only_root`, `formFields` con `dependsOn`, `panel.js`).
  - Desplegado en Render (`1a84430`).
- Verificado (config): `categoriaPadre` muestra solo "Comercios locales"; `categoria` trae
  "cafeteria y restaurante" y "Restaurante" con padre "Comercios locales" y
  `dependsOn = categoriaPadre`.
- Nota: `solicitudes_comercios.categoria` sigue siendo texto libre (no se tocó).

### Agent / task — Taxonomía de categorías de comercios
- Se insertó la taxonomía pedida: **7 categorías superiores** y **38 subcategorías**:
  - Restaurantes y comida, Hospedaje, Comercios locales, Entretenimiento,
    Naturaleza y aventura, Servicios, Transporte.
  - El orden de las superiores usa la numeración indicada (2..8).
- Comando idempotente `php spark nica:seed-taxonomia-comercios`
  (`app/Commands/SeedTaxonomiaComercios.php`); ejecutado en local.
- Verificado: `categorias_comercios` tiene 45 documentos (7 raíz + 38 hijas).
- Nota: los comercios de prueba siguen con `categoria` = "Restaurante" / "cafeteria y
  restaurante" (nombres que ya no existen en el catálogo); al editarlos en el panel se
  reasignan con el nuevo selector de categoría superior/subcategoría.

### Agent / task — Nuevas subcategorías de "Lugares turísticos" y remapeo de huérfanos
- Se eliminaron las 7 subcategorías viejas y se insertaron estas 8: **Museos, Parques,
  Monumentos, Sitios históricos, Miradores, Reservas naturales, Iglesias, Sitios culturales**.
- Se analizaron los lugares con categorías huérfanas (10) y se remapearon a la subcategoría
  adecuada:
  - **Monumentos:** Árbol de la Vida, Campana de la Paz, Homenaje a la Madre Juigalpina,
    Estatua de Rubén Darío, Toro Chontaleño.
  - **Sitios históricos:** Palacio Municipal de la Cultura, Tumba de Rubén Darío.
  - **Museos:** Estatua del Museo de Juigalpa.
  - **Iglesias:** Estatua de San Benito.
  - **Parques:** Parque Central de Juigalpa.
- Comando `php spark nica:seed-subcategorias-lugares` (borra las viejas, inserta las nuevas
  y remapea los lugares), idempotente. Ejecutado en local.
- Verificado: `categorias_lugares` = 1 raíz + 8 subcategorías; los 10 lugares quedaron con
  categoría válida (sin huérfanos).

### Agent / task — "Comercios recomendados": categorías padre, minicards y subcategorías
- Vista de ciudad: "Comercios recomendados" ahora muestra una **nube de categorías
  superiores** (presentes en los comercios de la ciudad) y los comercios en **minicards**
  (imagen, título y **subcategoría**), **limitados a 6**; botón "Ver todos".
- Nuevas vistas:
  - `ComerciosSubcategoriasScreen`: cards de subcategorías (con conteo) de la categoría
    superior elegida.
  - `ComerciosSubcategoriaScreen`: todos los comercios de una subcategoría.
- Datos: se agregó `categoriaPadre` a `Comercio` y `CategoriaComercio` (modelos +
  `ApiRepository` + `FirebaseRepository`).
- Navegación: rutas `comercios_subcategorias` y `comercios_subcategoria`;
  `onVerTodosComercios` de `CatalogScreen` se reemplazó por
  `onVerSubcategoriasComercio(parent)`.
- Verificado en el dispositivo: nube "Restaurantes y comida" → 3 minicards (Comida rápida,
  Cafeterías, Restaurantes); "Ver todos" → cards de subcategorías; tocar "Restaurantes" →
  "Restaurante Mi Choza".
- Pendiente: subir los cambios de la app Android a `main`.

## 2026-09-17

### Agent / task
- Objetivo: crear la base de conocimiento inicial para agentes de código (documentación
  completa y mantenible) sin cambiar funcionalidad de la app.
- Archivos creados:
  - `AGENTS.md`
  - `INIT.md`
  - `ARCHITECTURE.md`
  - `PROJECT_MAP.md`
  - `PROJECT_CONTEXT.md`
  - `DEVELOPMENT.md`
  - `CONVENTIONS.md`
  - `ROADMAP.md`
  - `CHANGELOG_AGENT.md` (este archivo)
- Archivos actualizados: `AGENTS.md`, `INIT.md` y `CONVENTIONS.md` (se incorporó la regla
  de responder siempre en español). No existían documentos previos; se conservó `README.md`
  y la documentación en `docs/` sin modificar.
- Cambios:
  - Se inspeccionó todo el repositorio (Gradle, manifest, navegación, pantallas Compose,
    data layer, Firebase, Unity, MapLibre, recursos, reglas y herramientas) y se documentó
    la arquitectura real.
  - Se documentó que `MapaScreen`/`MapaViewModel` **no están cableados** y que el mapa
    funcional es `MapaActivity` nativa.
  - Se registró el estado de Git: la integración de MapLibre está **sin commitear**.
  - Se añadió la regla obligatoria de responder **siempre en español** (`AGENTS.md` regla 18)
    y su recordatorio en `INIT.md` y `CONVENTIONS.md`.
- Build:
  - `.\gradlew.bat :app:assembleDebug` → **FALLA** (preexistente, no atribuible a esta
    tarea) en `:unityLibrary:buildIl2Cpp`: "Could not find file ...libil2cpp.sym.so to
    copy". El IL2CPP interno reporta "0 items updated" y la tarea de Unity intenta mover un
    símbolo que no se regeneró. El código de la app (`:app:compileDebugKotlin`) compila.
  - `.\gradlew.bat :app:assembleDebug -PskipIl2CppBuild` → **BUILD SUCCESSFUL**. Genera el
    APK. (Nota: esa bandera de prueba escribe un `libil2cpp.so` dummy; los binarios
    rastreados se restauraron después con `git restore`.)
- Resultado: documentación creada; no se modificó código de la aplicación. Build del APK
  verificado con `-PskipIl2CppBuild`.
- Riesgos/notas:
  - Hay cambios pendientes en el árbol de trabajo (no se descartaron): MapLibre
    (`MapaActivity`, `MapaScreen`, `MapaViewModel`, `map/`, `Routes.MAPA`, drawer, manifest,
    `app/build.gradle.kts`, `MainActivity`), además de `app/google-services.json`,
    `HomeScreen.kt` y `compile-data.json`. No se tocaron.
  - El proyecto Unity fuente no está en el repositorio; la integración se documentó a partir
    de `UnityArActivity`, `README.md` y la salida IL2CPP.
- Pendiente:
  - Decidir el destino de `MapaScreen`/`MapaViewModel`.
  - Cablear o retirar `ArPlaceholderScreen` y la ruta `ar_placeholder`.
  - Inicializar App Check o retirar la dependencia.
  - Ver `ROADMAP.md` para los próximos pasos de mapa, rutas y ubicación.

### Agent / task — Backend web (CI4 + Firestore)
- Objetivo: crear un backend administrable para la información de la app usando
  CodeIgniter 4 con Cloud Firestore (misma base que la app), en la carpeta nueva
  `nicaexp_web/`.
- Archivos creados (principales):
  - Proyecto CI4 completo en `nicaexp_web/` (generado con `composer create-project`).
  - `nicaexp_web/app/Config/Nica.php`, `NicaResources.php`, `Routes.php`, `Filters.php`,
    `Security.php` (regenerate=false).
  - `nicaexp_web/app/Libraries/{FirebaseFactory,FirestoreRepository,ResourceManager}.php`.
  - `nicaexp_web/app/Filters/{ApiKeyFilter,PanelAuthFilter}.php`.
  - `nicaexp_web/app/Controllers/Api/{BaseApiController,Health,Resources}.php`.
  - `nicaexp_web/app/Controllers/Panel/{Auth,Dashboard,Resources}.php`.
  - `nicaexp_web/app/Views/layout/panel.php`, `app/Views/panel/{login,dashboard,resource}.php`.
  - `nicaexp_web/public/assets/{panel.js,panel.css}`.
  - `nicaexp_web/{README.md,.env.example}` (`.env` NO versionado).
- Cambios de entorno (fuera del repo, a nivel de usuario/equipo):
  - Instalado **Composer 2.10.3** en `%LOCALAPPDATA%\Programs\Composer` + `composer.bat` +
    PATH de usuario.
  - `php.ini` de `C:\WebStack\php`: habilitadas `intl` y `fileinfo`; configurados
    `curl.cainfo` y `openssl.cafile` apuntando a `C:\WebStack\php\extras\ssl\cacert.pem`.
  - `composer require kreait/firebase-php` y `google/cloud-firestore`
    (`--ignore-platform-req=ext-grpc`); transporte Firestore en **REST**.
- Funcionalidad:
  - **API REST v1** con API key (`X-API-KEY`) y CRUD para `ciudades`, `lugares`,
    `comercios`, `solicitudes_comercios` y `usuarios`.
  - **Panel web AdminLTE 4** (sidebar lateral) con login, dashboard y CRUD por
    **DataTables + modales** (sin vistas de formulario separadas).
- Verificación (servidor en `localhost:8080`):
  - `/api/v1/health` sin key → 401; con key → 200 (`project: nica-explore`).
  - `/api/v1/ciudades` → 3 documentos reales de Firestore.
  - Login del panel → OK; `/panel/ciudades` carga DataTable.
  - Flujo de panel crear/eliminar en `comercios` → 201 / 200 y limpieza correcta.
  - CRUD REST crear/leer/borrar en `comercios` → verificado y documento de prueba borrado.
- Riesgos/notas:
  - `Nica.credentialsFile` vacío usa ADC (`gcloud`); alternativa: JSON de cuenta de
    servicio (fuera de Git).
  - El backend consume Firestore con privilegios de administrador: no exponer sin API key
    y HTTPS.
  - No se versionan `.env`, `vendor/` ni `writable/*`.
- Pendiente:
  - Subida de imágenes (hoy los campos de imagen son claves/rutas, no archivos).
  - Paginación/búsqueda del lado del servidor si las colecciones crecen.
  - Despliegue (hosting PHP) y endurecimiento de credenciales del panel.

### Agent / task — Imágenes, jQuery/AJAX y consumo desde Android
- Objetivo: todos los CRUD con DataTables + modales usando jQuery/`$.ajax`; gestión de
  imágenes (portada de ciudad, imagen de lugar/comercio); adaptar la app Android para
  consumir lo gestionado; compilar la app y dejar el backend accesible en LAN.
- Backend (`nicaexp_web/`):
  - `public/assets/panel.js` reescrito con **jQuery + `$.ajax`** (antes `fetch`), con
    `$.ajaxSetup` para el token CSRF y subida de imágenes por AJAX.
  - Campos de tipo `image` en `NicaResources` (`ciudades.imagenUrl`, `lugares.imagenUrl`,
    `comercios.imagenUrl`) y render de miniatura en DataTables.
  - Endpoint `POST /panel/upload` (`Panel\Resources::upload`): valida tipo/tamaño (máx.
    5 MB, JPG/PNG/WEBP/GIF) y guarda en `public/uploads/` (ignorado por Git), devolviendo
    `base_url('uploads/...')`.
  - `app/Views/panel/resource.php`: input de imagen con botón "Subir imagen" y vista previa.
- Android (`app/`):
  - `City` y `Monument` ahora tienen `imagenUrl`.
  - `SampleData.loadFromFirestore()` acepta **todo** el catálogo publicado (ya no filtra a
    las 3 ciudades/8 monumentos) y lee `imagenUrl`, con respaldo local por `imagenKey`.
  - Nuevo `ui/components/CoverImage.kt`: carga la URL remota con **Coil** y usa el drawable
    local como respaldo/error.
  - `CityCard`, `MonumentCard`, `MonumentRow`, `HomeScreen`, `CatalogScreen` y
    `RutasInteligentesScreen` usan `CoverImage`.
  - `AndroidManifest.xml`: `android:usesCleartextTraffic="true"` (imágenes HTTP en LAN;
    quitar al pasar a HTTPS).
- Build/Gradle:
  - `unityLibrary/build.gradle`: la tarea `buildIl2Cpp` ya no falla si IL2CPP no regenera
    `libil2cpp.sym.so`; conserva los símbolos precompilados. **Fix de build** (puede
    perderse al reexportar desde Unity).
- Red local:
  - `.env` → `app.baseURL = 'http://192.168.123.39:8080/'`.
  - Servidor: `php spark serve --host 0.0.0.0 --port 8080`.
  - Regla de firewall pendiente de ejecutar como administrador:
    `netsh advfirewall firewall add rule name="NicaExplorer Backend 8080" dir=in action=allow protocol=TCP localport=8080`
- Build:
  - `.\gradlew.bat :app:assembleDebug` → **BUILD SUCCESSFUL**. APK de ~228 MB con
    `libil2cpp.so` real (75 MB) y `libmaplibre.so` (verificado dentro del APK).
- Verificación backend: subida de imagen (200 y URL descargable image/png), CRUD con
  `imagenUrl`, y `/panel/comercios` con la columna de imagen. Documentos/archivos de prueba
  eliminados.
- Base de datos: app Android y backend comparten el proyecto **`nica-explore`**
  (`google-services.json` en la app; ADC en el backend). Verificado con `health`.
- Riesgos/notas:
  - Si cambia la IP LAN (DHCP), actualizar `app.baseURL` y volver a subir imágenes para que
    las URLs nuevas apunten a la IP correcta (las anteriores dejarían de resolver).
  - `usesCleartextTraffic` es solo para desarrollo.
- Pendiente:
  - Ejecutar la regla de firewall (requiere administrador).
  - Migrar imágenes a un almacenamiento público (Firebase Storage u hosting) para producción.
  - Búsqueda/paginación del lado del servidor si crecen las colecciones.

### Agent / task — Estructura de ciudad plantilla (Juigalpa) end-to-end
- Objetivo: tomar la vista de ciudad como plantilla, crear toda la estructura de datos
  (ciudad + rutas + imágenes de portada/carrusel), adaptar el backend y hacer que la app
  Android consuma dinámicamente la nueva información.
- Diseño (vista → datos): la vista de ciudad queda como **portada + carrusel → info
  (lema/descripción/historia/departamento) → rutas turísticas → monumentos → comercios**.
- BD (Firestore, plantilla `juigalpa`):
  - `ciudades/juigalpa` con `lema`, `descripcion`, `historia`, `departamento`,
    `imagenUrl` (portada), `galeria` (3 URLs de carrusel), `latitud/longitud`, `orden`,
    `activo`, `actualizadoEn`.
  - Nueva colección `rutas/ruta_cultural_juigalpa` con `paradas`
    (`monumento:<id>` / `comercio:<id>`), `objetivos`, `duracionEstimada`, `imagenUrl`.
  - `lugares` de Juigalpa con `activo`, `orden` e `imagenUrl` enlazada a imágenes servidas
    por el backend.
  - Imágenes copiadas al backend (`public/uploads/ciudad_juigalpa_*.jpg|jpeg`) y servidas
    como URLs absolutas (verificado 200 image/jpeg).
- Backend (`nicaexp_web/`):
  - `Config\NicaResources`: `ciudades` extendida; nueva colección `rutas`; `lugares` y
    `comercios` con `galeria`, `orden`, `activo`, coordenadas.
  - Nuevo comando `php spark nica:seed-plantilla` (`app/Commands/SeedPlantilla.php`):
    copia imágenes y crea la estructura completa de la ciudad plantilla.
  - El panel y la API gestionan automáticamente `rutas` (DataTables + modales, jQuery/AJAX)
    y los nuevos campos de ciudad.
- Android (`app/`):
  - `City` con `lema`, `historia`, `departamento`, `galeria`, `latitud/longitud`, `orden`,
    `activo`. `RutaTuristica` con `imagenUrl`, `orden`, `activo`.
  - `SampleData.loadFromFirestore()` ahora también lee la colección `rutas` y expone
    `rutasByCity` / `rutasDeCiudad(cityId)` (con respaldo local de
    `RutasTuristicasData`). Las rutas ya no están hardcodeadas.
  - `CatalogScreen`: nueva cabecera `CiudadHero` con **portada y carrusel** (`HorizontalPager`
    sobre `ciudades.galeria`) + `CiudadInfoCard` (departamento, descripción, historia
    expandible). La tarjeta de rutas usa las rutas cargadas de Firestore.
  - `RutasInteligentesScreen` resuelve la ruta desde `SampleData`.
- Build: `.\gradlew.bat :app:assembleDebug` → **BUILD SUCCESSFUL**.
- Riesgos/notas:
  - Las URLs de imágenes apuntan a `Nica.publicBaseUrl` (IP LAN); si cambia la IP hay que
    re-subir/actualizar. En producción conviene HTTPS + almacenamiento público.
  - El carrusel usa API `HorizontalPager` (opt-in `ExperimentalFoundationApi`).
- Pendiente:
  - Instalar el nuevo APK en el teléfono (se desconectó durante la sesión).
  - Replicar la estructura para León y Managua (la app ya acepta el esquema).
  - Gestión de la galería como lista de imágenes con subida múltiple en el panel.

### Agent / task — Caché de imágenes + app totalmente dinámica
- Objetivo: cachear imágenes (memoria + disco), descargar todas las imágenes de la app al
  backend y eliminar el código/datos hardcodeados para que la app sea 100% dinámica.
- Backend (`nicaexp_web/`):
  - Nuevo comando `php spark nica:seed-imagenes` (`app/Commands/SeedImagenes.php`):
    copia **todas** las imágenes locales de la app a `public/uploads/` y las enlaza en
    Firestore de forma dinámica: ciudades (portada + `galeria`), lugares (`imagenKey` ->
    `imagenUrl`) y comercios (por nombre -> `imagenUrl`).
  - Ejecutado: **3 ciudades, 8 lugares y 3 comercios** con imagen y galería (verificado:
    URLs `200 image/jpeg`).
- Android (`app/`):
  - Nuevo `NicaExplorerApp` (Application) que configura Coil con `MemoryCache` (25 %) y
    `DiskCache` (512 MB) y `respectCacheHeaders(false)`; registrado en el manifest. Las
    imágenes ya descargadas no se vuelven a pedir al salir/volver a una lista.
  - `SampleData.kt` reescrito: **sin catálogo hardcodeado** (se eliminaron 3 ciudades y 8
    monumentos embebidos y el mapeo `drawableForKey`); todo se carga de Firestore. El
    `monumentCount` se calcula desde los lugares.
  - Eliminado `data/RutasTuristicasData.kt` (ruta hardcodeada); las rutas vienen de
    `rutas/rutas` en Firestore.
  - `ComercioCard.kt`: eliminado el mapeo local hardcodeado (`localCoverRes`); la portada usa
    solo `imagenUrl` con placeholder.
  - `modeloUnity` (visor 3D) se sigue leyendo de Firestore y es editable desde el panel.
- Build: `.\gradlew.bat :app:assembleDebug` → **BUILD SUCCESSFUL**.
- Riesgos/notas:
  - Las imágenes viven en `public/uploads` (no versionado); los drawables de la app se
    conservan solo como fuente para `nica:seed-imagenes`.
  - En producción conviene HTTPS y almacenamiento público de imágenes.
- Pendiente: instalar el APK (teléfono desconectado) y, si se desea, quitar físicamente los
  drawables de catálogo (hoy sin referencias en el código).

### Agent / task — App 100% vía API + limpieza de recursos locales
- Objetivo: que la app consuma **todo** el contenido desde la API del backend, quitar
  recursos hardcodeados y aligerar la app.
- Android:
  - Nuevo `data/ApiClient.kt` (HttpURLConnection + org.json) y `data/ApiRepository.kt`
    (mapea ciudades, lugares, rutas y comercios desde `/api/v1/*`).
  - `SampleData.loadCatalog()` reemplaza a `loadFromFirestore()`: el catálogo se carga
    desde la API; sin respaldo local.
  - Comercios (listado, detalle, mapa, Itzae) ahora usan `ApiRepository` en lugar de
    Firestore.
  - `BuildConfig.API_BASE_URL` / `API_KEY` desde `local.properties` (no versionado);
    `buildConfig = true` en `app/build.gradle.kts`.
  - **Eliminados 14 drawables** de catálogo (ciudades, monumentos y comercios) que ahora
    sirve el backend; quedan solo los recursos de UI (isotipo, logo, asistente, fondo,
    icono de marcador).
- Backend (`nicaexp_web/`):
  - Nuevo comando `php spark nica:rebase-imagenes` que reescribe el host de las URLs de
    imágenes según `Nica.publicBaseUrl`.
  - `.env`: `app.baseURL` y `Nica.publicBaseUrl` -> `http://localhost:8080` (túnel USB).
- Conectividad:
  - La **Wi-Fi del entorno aísla los clientes**: el móvil no alcanza al PC por LAN
    (ping y TCP "No route to host"; el gateway sí responde).
  - Solución: `adb reverse tcp:8080 tcp:8080` + base URL `http://localhost:8080`.
- Verificación:
  - El APK instalado descarga imágenes del backend (`/uploads/*` -> 200) y se comprobó
    por captura de pantalla que la app muestra el contenido servido por la API.
- Build: `.\gradlew.bat :app:assembleDebug` → **BUILD SUCCESSFUL**. Instalado en Infinix X6886.
- Notas: el túnel `adb reverse` debe re-ejecutarse al reconectar el equipo; en una red sin
  aislamiento pueden usarse las URLs LAN. Los `db` de Firebase siguen usándose para auth.

### Agent / task — Panel: editores dinámicos, subida incremental y scroll del modal
- Objetivo: mejorar el panel (CodeIgniter 4): carrusel con file input por imagen y subida
  incremental con progreso, arreglar el scroll del modal y eliminar las textareas de
  "una línea por item" en favor de editores dinámicos.
- Nuevos tipos de campo en `Config\NicaResources`:
  - `images` (galería/carrusel): lista de imágenes con file input propio, preview y subida
    individual con barra de progreso.
  - `stringlist`: lista de textos con filas dinámicas (añadir/quitar).
  - `stops` (paradas de ruta): filas con `select` de tipo (Monumento/Comercio) y `select`
    de referencia (poblado con lugares y comercios), guardando `tipo:id`.
- Aplicado a: `ciudades.galeria` (images), `lugares.galeria` (images),
  `lugares.consejosResponsables` (stringlist), `comercios.galeria` (images),
  `rutas.objetivos` (stringlist), `rutas.paradas` (stops).
- `FirestoreRepository`: castea `images`/`stringlist`/`stops` como arreglos de strings.
- `Panel\Resources::index`: inyecta `refs` (lugares y comercios) para el editor de paradas.
- `Views/panel/resource.php`: contenedores `.nica-dynamic` + botón "Añadir"; sin textareas
  de líneas.
- `public/assets/panel.js`: reescrito para renderizar/rellenar los editores dinámicos,
  subir imágenes por fila con `$.ajax` + `xhr.upload.progress` y preview inmediato, y
  componer las paradas en `tipo:id`.
- `public/assets/panel.css`: scroll garantizado del modal (`#nica-modal .modal-body`
  `overflow-y:auto`; el `form` es la columna flex con `max-height:88vh`).
- Verificación (servidor local):
  - `/panel/ciudades` incluye `data-type="images"`; `/panel/rutas` incluye
    `data-type="stops"`, `data-type="stringlist"` y `refs`.
  - Guardado de una ruta de prueba con `objetivos[]` y `paradas[]` → 201, verificado por
    API y eliminado.
- Notas: las URLs manuales siguen disponibles en cada fila de imagen (además del file
  input). Los campos de texto libre (descripción, historia, notas) siguen siendo textarea.

### Agent / task — Refresco automático, pull-to-refresh y scroll del carrusel
- Objetivo: que la app detecte cambios en la BD y se actualice sin reabrir; añadir
  pull-to-refresh; y quitar el salto al tope al cambiar de monumento en el carrusel.
- Backend (`nicaexp_web/`):
  - Nuevo `App\Libraries\CatalogSignal::touch()`: actualiza `meta/catalogo` con
    `updatedAt` y `version` en cada alta/edición/borrado (panel y API REST).
  - Nuevo endpoint `GET /api/v1/version` (`Api\Catalog::version`) que devuelve la versión
    del catálogo.
  - `CatalogSignal::touch()` invocado en `Panel\Resources::save/delete` y
    `Api\Resources::create/update/delete`.
- Android (`app/`):
  - `ApiRepository.getCatalogVersion()` y `data/CatalogSync.kt`: sondea `/api/v1/version`
    cada 15 s y, cuando cambia, llama a `SampleData.loadCatalog()` sin reabrir la app.
  - `SampleData.refresh()` + `isRefreshing` para pull-to-refresh.
  - Nuevo `ui/components/NicaRefreshBox.kt` (pull-refresh de Material) y **pull-to-refresh**
    en `HomeScreen`, `CitySelectionScreen`, `CatalogScreen` y `ComerciosScreen`.
  - `CatalogScreen`: eliminado `scrollState.animateScrollTo(0)` al cambiar de monumento
    (las flechas del carrusel ya no saltan al tope).
- Verificación (dispositivo real + túnel USB):
  - Se cambió `ciudades/juigalpa.imagenUrl` a `sync_test.jpg` vía API; ~15 s después la app
    **pidió sola** `/uploads/sync_test.jpg` (recarga automática confirmada). Datos y archivo
    de prueba restaurados/eliminados.
- Build: `.\gradlew.bat :app:assembleDebug` → **BUILD SUCCESSFUL**; instalado en Infinix X6886.
- Notas:
  - Se añadió `androidx.compose.material:material` (BOM) para el pull-refresh; `PullToRefreshBox`
    de material3 no existe en la versión del BOM actual.
  - El sondeo corre mientras `MainActivity` está activa; el endpoint tarda milisegundos.

### Agent / task — Concepto "lugar" y mejora del indicador de refresh
- Objetivo: reemplazar el concepto "monumento" por "lugar" y mejorar la animación del
  pull-to-refresh (era una barra a todo el ancho).
- Android (`app/`) — renombrado de concepto:
  - `model/Monument.kt` → `model/Place.kt` (clase `Monument` → `Place`).
  - `ui/components/MonumentCard.kt` → `PlaceCard.kt` y `MonumentRow.kt` → `PlaceRow.kt`.
  - Identificadores: `Monument(s)` → `Place(s)`, `monumentId` → `placeId`,
    `monumentsByCity` → `placesByCity`, `allMonuments` → `allPlaces`,
    `recommendedMonuments` → `recommendedPlaces`, `monumentCount` → `placeCount`,
    `ReferenciaParadaRuta.Monumento` → `.Lugar`, `ParadaMonumento` → `ParadaLugar`.
  - Textos de UI "Monumento(s)" → "Lugar(es)".
  - **Contrato preservado**: el extra de Intent hacia Unity sigue siendo `monumentId`
    (`putExtra("monumentId", …)`), y el parser de paradas acepta `lugar:` y el legado
    `monumento:`. El campo Firestore `monumentCount` se sigue leyendo (mapea a `placeCount`).
- Backend (`nicaexp_web/`):
  - `NicaResources`: etiqueta "Lugares / Monumentos" → "Lugares".
  - `Panel\Resources::referenceOptions`: clave `refs.monumento` → `refs.lugar`.
  - `panel.js`: el editor de paradas usa tipo "Lugar"/"Comercio" y normaliza el legado
    `monumento:` → `lugar:`.
  - Datos migrados: `rutas/ruta_cultural_juigalpa.paradas` ahora usa `lugar:*`.
    `SeedPlantilla` actualizado.
- Refresh:
  - `NicaRefreshBox`: el indicador ya no ocupa el ancho completo; ahora es el círculo
    animado (estilo Google) centrado arriba.
- Build: `.\gradlew.bat :app:assembleDebug` → **BUILD SUCCESSFUL**; instalado en Infinix X6886.
- Notas: el dispositivo se reconectó de forma intermitente por USB; el túnel
  `adb reverse tcp:8080 tcp:8080` quedó activo.

### Agent / task — Refinamiento visual del panel administrativo (`nicaexp_web`)
- Objetivo: rediseñar el panel web (solo diseño) con identidad oscura NicaExplorer,
  alejándolo del look genérico de plantilla Bootstrap/AdminLTE, sin romper funcionalidad.
- Guía: skill `.opencode/skill/dashboard-design/SKILL.md`.
- Alcance:
  - `public/assets/panel.css`: sistema de diseño propio (tokens oscuros, tipografía Inter,
    escala de espaciado 4→48, radios 8/12/14, sombras mínimas). Reemplaza los ajustes
    menores previos por overrides de Bootstrap/AdminLTE, tablas, DataTables 2, formularios,
    modales, dropdowns y SweetAlert en oscuro.
  - `app/Views/layout/panel.php`: `data-bs-theme="dark"`, topbar contextual (sin breadcrumb),
    sidebar agrupada (General / Contenido / Ecosistema local / Administración / Sistema),
    estado activo en acento, perfil y cierre de sesión, footer sobrio.
  - `app/Views/panel/dashboard.php`: jerarquía de dashboard (5 métricas primarias, panel de
    solicitudes de comercios y bloque API REST).
  - `app/Views/panel/resource.php`: cabecera de página con acción primaria y formulario modal
    agrupado en bloques (Identificador, Información básica, Ubicación, Contenido, Multimedia,
    Listas y relaciones, Estado y publicación).
  - `app/Views/panel/login.php`: rediseño oscuro con branding, labels visibles y estado de error.
  - `public/assets/panel.js`: sólo markup y textos (botones de acción como iconos, empty
    state con CTA, estados de carga/feedback y tema oscuro de SweetAlert). Se conservan IDs,
    selectores, endpoints y flujo AJAX.
- Sin cambios en controladores, rutas, Firestore, `.env`, `NicaResources` ni lógica de subida.
- Verificación: `php -l` en las 4 vistas (sin errores); `node --check` de `panel.js` (OK);
  servidor `php spark serve` con login real → `/panel`, `/panel/lugares`, `/panel/comercios`
  responden 200, sin `Fatal error`/`Warning`/`Notice`/`Deprecated`.
- Notas: no se agregaron dependencias; se sumó la fuente Inter vía Google Fonts (los CDN ya
  se usaban) y se mantiene Bootstrap Icons como única librería de iconos. La estructura
  AdminLTE (grid, toggle del sidebar) se conserva para no alterar el comportamiento.

### Agent / task — Tema claro y switch de tema en el panel (`nicaexp_web`)
- Objetivo: añadir una versión clara del panel manteniendo el tema oscuro actual y permitir
  alternar entre ambos con un switch.
- Cambios:
  - `public/assets/panel.css`: tokens `--nica-*` por tema (`:root,[data-bs-theme="dark"]` y
    `[data-bs-theme="light"]`) y mapeo de variables Bootstrap/AdminLTE en `[data-bs-theme]`.
    Se tokenizaron los valores que estaban fijos (hover, bordes, sombras, alertas, progreso,
    scrollbar, topbar) para que ambos temas sean coherentes. El sidebar ahora se basa en
    tokens compartidos.
  - `public/assets/theme.js` (nuevo): alterna `data-bs-theme` y persiste la elección en
    `localStorage['nica-theme']`; sincroniza icono/aria de los botones.
  - `app/Views/layout/panel.php` y `app/Views/panel/login.php`: script inline en el `<head>`
    que aplica el tema guardado antes de pintar (sin parpadeo) y botón switch (sol/luna) en
    la topbar y en el login.
- Sin cambios en controladores, rutas, Firestore, `.env` ni lógica de datos.
- Verificación: `php -l` en las vistas (OK), `node --check` de `panel.js` y `theme.js` (OK),
  llaves del CSS balanceadas; capturas reales en Edge de dashboard/tabla/login en ambos
  temas; prueba de interacción del switch (click → `data-bs-theme="light"` y
  `localStorage['nica-theme']="light"`).
- Notas: el tema por defecto sigue siendo oscuro; la preferencia del usuario manda y no se
  agregan dependencias.

### Agent / task — IDs en selects, catálogo de categorías y ocultar IDs en tablas (`nicaexp_web`)
- Objetivo: evitar escribir IDs a mano (mostrar selects con el ítem correspondiente), crear
  un CRUD de categorías de lugares con su catálogo, y ocultar los IDs en las tablas.
- Modelo (`app/Config/NicaResources.php`):
  - Nuevo tipo de campo `reference` (`collection`, `value_field`, `label_field`).
  - Nueva colección `categorias_lugares` (nombre, descripción, icono, orden, activo) con su
    propio CRUD y entrada en el menú (grupo Contenido).
  - `cityId` pasa a `reference` → ciudades en `lugares`, `rutas`, `comercios` y
    `solicitudes_comercios`; `lugares.categoria` pasa a `reference` → `categorias_lugares`
    (guarda el nombre de la categoría para no romper el texto que lee la app Android).
- Backend (`app/Controllers/Panel/Resources.php`):
  - `referenceOptions()` ahora resuelve paradas y referencias genéricas, devolviendo
    `references` por campo con `{value, label}`.
- Vistas/JS:
  - `panel/resource.php`: los campos `reference` se dibujan como `<select>` con el ítem.
  - `panel.js`: se ocultan las columnas identificadoras (`id`, `cityId`, `userId`, el campo
    id del recurso como `uid`); al elegir ciudad se refleja el nombre en el campo de texto
    visible; en edición se conserva un valor heredado que no esté en el catálogo.
- Catálogo:
  - Nuevo comando `php spark nica:seed-categorias` (idempotente). Ejecutado: creó 7
    categorías (`monumento_conmemorativo`, `monumento_cultural`, `monumento_historico`,
    `monumento_urbano`, `patrimonio_arqueologico`, `patrimonio_religioso`,
    `tradicion_ganadera`).
- Verificación: `php -l` y `node --check` OK; servidor real → `/panel/categorias_lugares` 200,
  API `/api/v1/categorias_lugares` devuelve 7, los modales de lugar muestran selects de
  Ciudad (4 + vacío) y Categoría (7 + vacío), y las tablas de lugares/usuarios ya no muestran
  `cityId`/`uid`. Capturas revisadas.
- Notas: la app Android sigue leyendo `categoria` y `ciudad` como texto; no se modificó el
  contrato. Renombrar una categoría no reescribe los lugares existentes (el valor guardado es
  el nombre).
