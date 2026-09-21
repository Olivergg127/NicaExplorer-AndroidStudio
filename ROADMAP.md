# ROADMAP.md — Estado real y próximos pasos

> Regla: una función **solo** aparece en "Implementado" si existe en el código y es
> alcanzable. Una idea, un diseño o una captura no cuentan.
> Prioridad: **P0** estabilidad, **P1** funcionalidad principal, **P2** mejora, **P3** futuro.

## Implementado

### Autenticación y cuenta (P1)
- Registro, inicio de sesión y recuperación de contraseña con Firebase Auth
  (`FirebaseRepository`, `LoginScreen`, `RegisterScreen`, `RecoveryPasswordScreen`).
- Decisión de sesión en `SplashScreen` según `FirebaseAuth.currentUser`.
- Perfil con nombre, correo y rol; edición de nombre (`EditarPerfilScreen` +
  `updateUserName`).
- Cierre de sesión (`signOut`) desde Home, Perfil y Configuración.

### Catálogo y exploración (P1)
- 3 ciudades y 8 monumentos, con carga desde Firestore y **fallback local** (`SampleData`).
- Selección de ciudad con búsqueda (`CitySelectionScreen`).
- Detalle de monumento (`CatalogScreen`): descripción, historia, año, categoría,
  afluencia estimada, consejos de turismo responsable, guardar lugar.
- Registro automático de exploración (historial) al abrir un monumento.
- Lugares guardados e historial (`LugaresGuardadosScreen`, `HistorialExploracionScreen`),
  persistidos en DataStore por `uid`.

### Visor 3D con Unity (P1)
- Botón "Ver en 3D" habilitado según `Monument.modeloUnity`.
- `UnityArActivity` + contrato de Intent (`cityId`, `monumentId`, `escena=visor3d`).
- Mapeo `monumentId` → prefab vía Unity (`Visor3DController`).
- Salida con `moveTaskToBack` sin destruir el motor; reinicio por `onNewIntent`.
- **"Los Motivos del Lobo (Escultura)" (León)** integrado: `los_motivos_del_lobo_escultura`
  → prefab `PazHermanoLobo`, y `modeloUnity = PazHermanoLobo` registrado en Firestore.
  Pendiente: probar el botón "Ver en 3D" en un dispositivo real.

### Asistente Itzae (P1)
- Chat con Firebase AI Logic (`gemini-3.5-flash-lite`), `GeminiRepository`.
- Contexto de monumento, catálogo local (afluencia) y comercios reales de Firestore.
- Preguntas rápidas contextuales según la ciudad del monumento.

### Comercios locales (P1)
- Listado de comercios activos con filtro por ciudad (`ComerciosScreen`).
- Detalle con horario, dirección, copiar teléfono, WhatsApp y Google Maps
  (`ComercioDetalleScreen`, `TelefonoUtils`).
- Formulario de solicitud de incorporación (`SolicitudComercioScreen` →
  `solicitudes_comercios`, estado `pendiente`).
- **Categorías de comercios** (colección `categorias_comercios`) administradas desde el
  panel; al crear/editar un comercio la categoría se elige de un **selector**.
  `php spark nica:seed-categorias-comercios` crea el catálogo desde los comercios
  existentes.
- **Comercios recomendados en el detalle de ciudad:** al final de la vista de la ciudad,
  una **nube de categorías superiores** y los comercios en **minicards** (imagen, título y
  subcategoría), **limitados a 6**. "Ver todos" abre las **subcategorías** (cards) y, al
  elegir una, todos los comercios de esa subcategoría
  (`ComerciosSubcategoriasScreen` / `ComerciosSubcategoriaScreen`).

### Rutas turísticas (P1)
- Rutas cargadas desde el backend por ciudad (`rutas.paradas`); la cantidad de paradas es
  dinámica. Resuelve cada parada contra los lugares y comercios existentes y muestra
  "Rutas próximamente" para ciudades sin ruta publicada.
- **Mapa de la ruta** (`ui/components/RutaMapa.kt`): pines numerados por parada y el camino
  que las une (trazado por carretera de OSRM, con líneas rectas como respaldo). Al pulsar un
  pin, la lista se desplaza a la tarjeta de esa parada. Solo aparecen las paradas que tengan
  `latitud`/`longitud`.

### Administración y roles (P2)
- Roles `USUARIO`, `EDITOR`, `ADMIN`, `AUDITOR` (`UserRole`).
- Panel con contadores, búsqueda, filtros y cambio de rol confirmado por ADMIN
  (`AdminPanelScreen`, `getAllUsers`, `updateUserRole`).
- Guardas de acceso en `AppNavigation` (solo ADMIN/AUDITOR; el Editor no administra
  usuarios) y reglas en `firestore.rules`.
- **Panel web con permisos por rol (2026-09-20):** login con cuentas Firebase (rol leído
  de Firestore) además del admin de respaldo en `.env`; matriz `PanelPermissions`
  (C/L/M/E por módulo) aplicada con `PanelPermissionFilter` en todas las rutas del panel;
  UI (navegación y botones) filtrada por permisos. El rol `USUARIO` no accede al panel.

### Mapa (P1)
- **Mapa principal** (`MapaPrincipalScreen`, Compose + MapLibre `textureMode` con teselas
  OSM): pines de **lugares** y **comercios**, **selector de ciudad**, filtro por tipo
  (Todos/Lugares/Comercios) y botón "Inicio".
- **Rediseño "Descubre a tu alrededor" (2026-09-20):** encabezado con ciudad + campana,
  buscador con filtro (Todo/Lugares/Comercios), rejilla de **categorías** (tomadas de
  `categoriaPadre` de los comercios, con ícono/color por tipo), mapa con esquinas
  redondeadas y botones flotantes (navegación, capas, mi ubicación). Los pines de
  comercios se **colorean según su categoría**; se agregó el punto azul de ubicación
  (`MapaMarkerFactory.ubicacionIcon`). Probado en dispositivo (diseño); el color por
  categoría quedó compilando a la espera de verificación visual.
- **Detección de ciudad con GPS** (`LocationManager`, sin dependencia nueva): el chip
  "Mi ubicación" ubica la ciudad conocida más cercana (≤ 25 km) y centra el mapa.
- Coordenadas de ciudades: `php spark nica:seed-coordenadas-ciudades` (Juigalpa, León,
  Managua, Matagalpa).
- `MapaActivity` nativa (MapLibre OpenGL) queda como legado; ya no se abre desde la Home.
- Acceso desde el drawer de Home ("Mapa").
- Pendiente: clustering si hay muchos pines; lugares/comercios sin coordenadas no aparecen.

### Base / infraestructura (P0)
- Tema Compose claro/oscuro con paleta "Guardabarranco"; preferencia de tema en DataStore.
- Degradados de fondo/barra según tema.
- `firestore.rules` con protección de `uid`/`rol`, bloqueo de autoelevación, nombre
  propio, cambio de rol por ADMIN, `mail` bloqueada y borrado bloqueado.
- Herramientas de mantenimiento en `tools/` (seed, verify, normalize).

### Backend de administración (P2)
- Proyecto `nicaexp_web/` en **CodeIgniter 4 + Cloud Firestore** (misma base que la app).
- **API REST v1** con API key y CRUD de `ciudades`, `lugares`, `comercios`,
  `solicitudes_comercios` y `usuarios`.
- **Panel web AdminLTE 4** (sidebar, login) con tablas **DataTables** y **modales** para
  crear/editar/eliminar, usando **jQuery + AJAX**.
- **Subida de imágenes** (portada de ciudad, imagen de lugar/comercio) servidas desde
  `public/uploads/`; la app Android las consume por `imagenUrl` con Coil.
- La app acepta todo el catálogo publicado en Firestore (se pueden agregar ciudades/lugares
  desde el panel).
- **IDs automáticos y formularios limpios:** ciudades, lugares, rutas, comercios y
  categorías generan su id por slug del nombre (`toro_chontaleno`); los identificadores y
  campos internos (`uid`, `imagenKey`) ya no se muestran en formularios ni tablas.
- **Selector de ubicación:** los formularios con `latitud`/`longitud` incluyen un mapa
  (Leaflet + OSM) donde se fija el punto con clic/arrastre o buscando una dirección
  (Nominatim). La imagen de portada tiene botón para quitarla y subir otra.
- **Categorías jerárquicas (lugares y comercios):** ambos catálogos tienen `categoriaPadre`;
  una categoría superior ("Lugares turísticos", "Comercios locales") agrupa subcategorías
  (`php spark nica:seed-categorias-lugares-jerarquia` y
  `nica:seed-categorias-comercios-jerarquia`). Los modales separan categoría superior (solo
  raíz) y **subcategoría** (dependiente de la superior elegida).
- **Taxonomía de comercios:** 7 categorías superiores (Restaurantes y comida, Hospedaje,
  Comercios locales, Entretenimiento, Naturaleza y aventura, Servicios, Transporte) con 38
  subcategorías (`php spark nica:seed-taxonomia-comercios`).
- **Subcategorías de lugares:** bajo "Lugares turísticos": Museos, Parques, Monumentos,
  Sitios históricos, Miradores, Reservas naturales, Iglesias y Sitios culturales; los
  lugares existentes se remapearon (`php spark nica:seed-subcategorias-lugares`).
- Pendiente: paginación/búsqueda del lado del servidor y completar el despliegue
  (ver "Backend en la nube" más abajo).

## En progreso

- **Limpieza de mapa legado (P0).** `MapaScreen.kt` sigue **sin cablear** (la ruta `mapa`
  ahora abre `MapaPrincipalScreen`); `MapaViewModel.kt` sí se reutiliza. Decidir si se
  elimina `MapaScreen` y la `MapaActivity` nativa.
- **Centrado por ciudad/monumento (P2).** `MapaConfig.NICARAGUA` tiene un comentario que
  indica que el enfoque en Juigalpa se incorporará en la fase de comercios/marcadores.
- **Rutas para León y Managua (P2).** La UI ya muestra "Rutas próximamente"; solo Juigalpa
  tiene datos.
- **Búsqueda en catálogo (P3).** El icono de búsqueda en `CatalogScreen`/`NicaTopBar` es un
  `IconButton(onClick = { })` sin implementación.
- **Notificaciones (P3).** Existe preferencia `notifications_enabled` y el switch en
  Configuración, pero **no hay implementación** de notificaciones; el icono de campana en
  Home no hace nada.
- **App Check (P2).** La dependencia `firebase-appcheck-playintegrity` está incluida, pero
  **no hay inicialización/`installAppCheckProviderFactory` en el código**; falta cablearlo.
- **Limpieza de AR legado (P3).** `ArPlaceholderScreen` y la ruta `ar_placeholder` siguen
  registradas pero no reciben navegación; el paquete `ar/` conserva ARCore/AAR.

## Próximos pasos

Ordenados por prioridad aproximada. **Ninguno está implementado** salvo que se indique.

- **P0 — Estabilidad y deuda técnica**
  - Decidir y ejecutar el destino de `MapaScreen`/`MapaViewModel` (cablear o eliminar).
  - Inicializar App Check o retirar la dependencia si no se usará.
- **P1 — Mapa turístico**
  - Centrado dinámico del mapa en Juigalpa/León/Managua.
  - Marcadores turísticos de monumentos y comercios.
  - Botón "Ver en el mapa" en `CatalogScreen`/`ComercioDetalleScreen`.
  - Destacar el monumento/comercio seleccionado.
  - Tarjetas inferiores de información.
  - "Explorar alrededor" de un punto.
- **P2 — Descubrimiento**
  - Categorías y filtros de comercios/monumentos.
  - Rutas turísticas para León y Managua.
  - Búsqueda real en catálogo y comercios.
- **P2 — Ubicación (requiere permisos)**
  - Flujo de permisos GPS (hoy eliminados del manifest) y ubicación del usuario.
- **P3 — Integración avanzada**
  - Itzae contextual al mapa (recomendaciones según lo visible/seleccionado).
  - Notificaciones (recordatorios, novedades).
  - Recomendaciones personalizadas.
- **P3 — Futuro de producto**
  - Favoritos de comercios, reseñas, apertura de WhatsApp Business mejorada.
  - Analítica/moderación de solicitudes de comercios desde Android (con claims de admin).

## Backend en la nube — Render + imágenes en GitHub (2026-09-19)

Se despliega el backend en **Render (Web Service Docker, plan free)**. Las imágenes van a
un **repositorio público de GitHub** (`Olivergg127/NicaExplorer-assets`), porque Firebase
Storage requiere plan Blaze (tarjeta) y el proyecto `nica-explore` **no tiene facturación**.

Hecho:

- `App\Libraries\ImageStorage`: prioridad **GitHub > Firebase Storage > modo local**.
- `Panel\Resources::upload()`, `SeedImagenes` y `SeedPlantilla` usan `ImageStorage`.
- `php spark nica:storage-migrate`: sube `public/uploads` y reescribe las URLs en Firestore.
  **Ejecutado**: 20 imágenes subidas al repo de assets y 16 documentos actualizados.
- Cuenta de servicio **`nicaexp-backend@nica-explore.iam.gserviceaccount.com`**
  (rol `roles/datastore.user`) y su clave JSON para Render.
- `nicaexp_web/Dockerfile` + `docker/entrypoint.sh` + `docker/write-env.php` (PHP 8.3 +
  Apache, puerto de Render y `.env` con claves `nica.*` en minúsculas) y `render.yaml`.

Desplegado y verificado (2026-09-19):

- **Servicio Render:** `nicaexplorer-backend` (free, Docker) →
  <https://nicaexplorer-backend.onrender.com>. Auto-deploy activado en cada push a `main`.
- `/api/v1/health` → `status: ok`, `project: nica-explore`, `transport: rest`.
- Login del panel verificado; `/panel/login` responde 200.
- Secret File `firebase.json` montado y accesible por `www-data` (el entrypoint lo copia a
  `writable/`).
- La app Android (`local.properties`) apunta a la URL de Render.

Pendiente (P2):

- Reemplazar el token de `gh` (usado para escribir en el repo de assets) por un PAT
  *fine-grained* limitado a `Olivergg127/NicaExplorer-assets`.
- Quitar `usesCleartextTraffic` del manifest (ya todo es HTTPS).
- Rotar la API key/admin password del panel y guardarlos en un gestor de secretos.

Notas del plan free: el servicio duerme tras 15 min sin tráfico (despierta en ~1 min) y
concede 750 h/mes (un servicio siempre activo usa ~730 h). La app consulta
`/api/v1/version` cada 15 s, lo que en la práctica mantiene el servicio despierto.

## Notas de honestidad

- No se ofrecen GPS, navegación GPS, rutas en tiempo real ni afluencia en tiempo real.
- La afluencia y la duración de la ruta son **orientativas del prototipo**.
- La ruta inteligente está desarrollada principalmente para Juigalpa porque es la ciudad
  con comercios cargados en el prototipo.
