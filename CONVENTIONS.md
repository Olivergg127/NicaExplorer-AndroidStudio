# CONVENTIONS.md — Convenciones de código y UI

> Convenciones **observadas en el código actual**. Las secciones "Regla" son
> recomendaciones compatibles con el proyecto; las secciones "Observado" describen lo que
> ya existe. Ante la duda, imita el archivo vecino.

## Idioma

- **Regla:** el agente responde **siempre en español** al usuario (explicaciones, planes,
  informes y mensajes de commit/PR), aunque el código o las herramientas usen inglés.
- **Observado:** comentarios, identificadores de dominio y textos visibles en **español**
  (`nombre`, `ciudad`, `rol`, `obtener...`, `RutasInteligentesScreen`).
- **Observado:** los nombres de APIs de librerías se mantienen en inglés
  (`FirebaseFirestore`, `MapView`, `viewModelScope`, `LaunchedEffect`).
- **Regla:** mantén esa mezcla; no traduzcas APIs ni renombres el dominio a inglés.

## Estilo Kotlin

- **Observado:** `kotlin.code.style=official` en `gradle.properties`; JVM target 17.
- **Observado:** Kotlin moderno: `data class`, `sealed interface`, `enum class`, trailing
  commas, `when` exhaustivo, expresiones `if`/`when` como valor.
- **Observado:** los repositorios y contenedores de datos son `object` (singleton) y sus
  funciones son `suspend`; devuelven `Result<T>` o nullable, y usan `.await()` de
  `kotlinx.coroutines.tasks`.
- **Regla:** si añades una operación de datos, imita ese patrón (`object` + `suspend` +
  `Result`), no introduzcas inyección de dependencias ni frameworks nuevos.

## Paquetes y archivos

- **Observado:** organización por capa bajo `com.lospuntoycoma.nicaexplorer`:
  `data/`, `model/`, `map/`, `navigation/`, `ar/`, `util/`, `ui/screens/`,
  `ui/components/`, `ui/viewmodels/`, `ui/theme/`.
- **Regla:** una pantalla por archivo `XxxScreen.kt`; un modelo por archivo;
  un ViewModel por archivo `XxxViewModel.kt`.

## Compose

- **Observado:** cada pantalla expone un `@Composable fun XxxScreen(...)` con parámetros de
  navegación/callbacks al final (`onBack`, `onClick`, etc.). Las pantallas usan `Scaffold`
  y `NicaTopBar` para la barra superior.
- **Observado:** los componentes reutilizables viven en `ui/components/` y usan PascalCase
  (`NicaTopBar`, `NicaButton`, `MonumentCard`, `ComercioCover`).
- **Observado:** se usa `@OptIn(ExperimentalMaterial3Api::class)` cuando hace falta
  (`ModalBottomSheet`, `TopAppBar`, `ModalNavigationDrawer`).
- **Observado:** el estado local usa `remember { mutableStateOf(...) }` +
  `by`/`collectAsState`; los efectos con `LaunchedEffect`; el fondo con
  `nicaAppBackgroundBrush()`.
- **Regla:** reutiliza los componentes existentes antes de crear uno nuevo. No cambies
  patrones de fondo/tema: usa `nicaAppBackgroundBrush()` y `nicaBottomNavBarBrush()` en
  lugar de `AppBackgroundBrush`/`BottomNavBarBrush` directos (esos solo cubren modo oscuro).

## ViewModels

- **Observado:** patrón `private val _uiState = MutableStateFlow(...)` +
  `val uiState: StateFlow<...> = _uiState.asStateFlow()`; estado expuesto como data class
  `XxxUiState(isLoading, ..., error)`; trabajo en `viewModelScope.launch`.
- **Observado:** `UserViewModel` se comparte desde `AppNavigation`; los demás se obtienen
  con `viewModel()` dentro de la pantalla.
- **Regla:** mantén ese patrón; no mezcles estado en `remember` cuando corresponde a un
  ViewModel.

## Navegación

- **Observado:** todas las rutas se declaran en `navigation/Routes.kt` (constantes en
  `SCREAMING_SNAKE_CASE`, constructores en camelCase, p. ej. `catalog(cityId)`).
- **Observado:** las rutas usan minúsculas y `snake_case` (`city_selection`,
  `comercio_detalle/{comercioId}`); los parámetros en camelCase.
- **Regla:** no escribas strings de ruta en las pantallas; agrega una constante/función en
  `Routes.kt` y regístrala en `AppNavigation.kt`.

## Modelos

- **Observado:** `data class` inmutables; enums con `displayName` cuando hay texto visible
  (`Afluencia`). Las referencias entre modelos usan IDs (`cityId`, `monumentId`), no objetos
  anidados.
- **Observado:** `RutaTuristica` referencia paradas con un `sealed interface`
  (`ReferenciaParadaRuta`) para no duplicar datos.
- **Regla:** no agregues campos derivados que puedan calcularse; no dupliques fuentes de
  verdad (p. ej. no copies el nombre del monumento dentro de la ruta).

## Tema, colores e iconos

- **Observado:** toda la paleta está en `ui/theme/Color.kt` (familias turquesa, azul,
  terracota, WhatsApp, dorado y neutrales). Los componentes consumen
  `MaterialTheme.colorScheme.*`.
- **Observado excepción:** los colores de nivel de afluencia (`BAJA/MODERADA/ALTA`) están
  repetidos como literales en `CatalogScreen.kt` y `RutasInteligentesScreen.kt`
  (`0xFF2E7D32`, `0xFFF9A825`, `0xFFC62828`).
- **Regla:** evita nuevos colores hardcodeados; si necesitas un color de dominio repetido,
  centralízalo en `Color.kt`. Iconos: `material-icons-extended` (`Icons.Filled.*`,
  `Icons.Outlined.*`).
- **Observado:** el verde WhatsApp (`GreenPrimary`, `GreenLight`) es exclusivo para
  elementos de WhatsApp (`docs/paleta-colores.md`).

## Recursos

- **Observado:** `res/values/strings.xml` solo contiene `app_name`; **los textos de la UI
  están escritos en español directamente en Compose**.
- **Regla:** por consistencia, mantén los textos nuevos en español dentro del Composable,
  tal como el resto de pantallas. No introduzcas `strings.xml` a medias ni otro idioma.
- **Observado:** drawables en minúsculas y sin separadores (`homenajealamadrejuigalpina.jpg`,
  `fondotarjeta.jpeg`); los iconos vectoriales usan prefijo `ic_`
  (`ic_map_marker_comercio.xml`).
- **Regla:** para un nuevo drawable de imagen usa minúsculas sin guiones; para un vector usa
  `ic_<nombre>.xml`.

## Errores y mensajes

- **Observado:** los mensajes al usuario están en español y los errores de Firebase se
  traducen a mensajes amigables en las pantallas (`LoginScreen`, `RegisterScreen`).
  Los repositorios registran con `Log.e`/`Log.w` y un `TAG`.
- **Regla:** no expongas mensajes técnicos crudos al usuario; registra el detalle con `Log`
  y muestra un texto en español.

## Comentarios

- **Observado:** comentarios `//` y KDoc en español para explicar decisiones no obvias
  (por ejemplo por qué OpenGL, por qué Activity nativa, o el ciclo de vida de Unity).
- **Regla:** comenta el "por qué", no el "qué". Evita comentarios obvios.

## Cosas a evitar

- No cambiar el package name ni `applicationId`.
- No introducir dependencias nuevas sin necesidad justificada.
- No migrar Compose a XML ni MapLibre a otra librería.
- No usar referencias totalmente cualificadas inline
  (`com.lospuntoycoma.nicaexplorer.R.drawable...`); prefiere `import` (hay varios casos
  heredados en el código; no los propagues).
- No duplicar lógica entre `CatalogScreen` y `RutasInteligentesScreen` si puedes
  reutilizar un componente.
