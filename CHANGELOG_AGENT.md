# Agent Change Log

> Bitácora de cambios hechos por agentes de código. Agrega una entrada por cada sesión con
> cambios importantes. No sustituye al historial de Git; lo complementa con contexto.
> Formato: fecha, objetivo, archivos, cambios, build, resultado, riesgos y pendientes.

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
