# INIT.md — NicaExplorer para agentes nuevos

> Objetivo de este archivo: que un agente entienda el proyecto en pocos minutos y sepa
> dónde mirar. Para reglas obligatorias lee primero `AGENTS.md`.

## ¿Qué es NicaExplorer?

Aplicación Android de turismo de Nicaragua, desarrollada por el equipo **Los Punto y Coma**.
Combina catálogo cultural de ciudades y monumentos, visor 3D (Unity), asistente de IA
(llamado **Itzae**), comercios locales, rutas turísticas predefinidas y un mapa propio
(MapLibre + OpenStreetMap).

## ¿Qué problema busca resolver?

Centralizar en una sola app la exploración del patrimonio nicaragüense (Juigalpa, León y
Managua), dar visibilidad a emprendimientos locales y ofrecer una experiencia interactiva
con 3D e IA. Es un **prototipo académico**.

## Estado actual (resumen)

- **Implementado y estable:** autenticación con Firebase, catálogo de 3 ciudades y 8
  monumentos, detalle de monumento, visor 3D con Unity, asistente Itzae, comercios locales
  con detalle/WhatsApp/Google Maps, lugares guardados, historial, perfil, panel
  administrativo con roles, rutas turísticas (solo Juigalpa) y mapa nativo con MapLibre.
- **En progreso / sin cablear:** `MapaScreen` (Compose) y `MapaViewModel` con marcadores de
  comercios existen en el código pero **no están conectados a la navegación**; el mapa
  accesible hoy es `MapaActivity` (nativa, sin marcadores, vista de todo Nicaragua).
- **No implementado:** GPS, ubicación del usuario, rutas en tiempo real, marcadores
  turísticos en el mapa, "Ver en el mapa" desde un monumento, recomendaciones dinámicas.

## ¿Qué tecnologías utiliza?

- Kotlin + Jetpack Compose + Material 3.
- Firebase Authentication (correo/contraseña), Cloud Firestore, Firebase AI Logic
  (Gemini, modelo `gemini-3.5-flash-lite`) y la dependencia App Check (Play Integrity)
  incluida, aunque todavía **sin inicializar** en el código.
- Unity 6000.5.1f1 exportado como módulo Android `unityLibrary` (visor 3D; AR legado).
- MapLibre Native Android, variante **OpenGL**, `12.3.1`, con teselas raster de OSM.
- DataStore Preferences para datos locales por usuario.
- Coil para imágenes remotas.

## ¿Dónde empieza la aplicación?

- `app/src/main/AndroidManifest.xml` → lanza `MainActivity`.
- `MainActivity.kt` inicializa MapLibre y `UserPreferences`, carga el catálogo desde
  Firestore (`SampleData.loadFromFirestore()`) y monta `NicaExplorerTheme` + `AppNavigation`.
- `navigation/AppNavigation.kt` define el `NavHost` (ruta inicial `splash`).
- `navigation/Routes.kt` contiene todas las rutas.

## ¿Cómo está organizada? (paquete `com.lospuntoycoma.nicaexplorer`)

- `ar/` → `UnityArActivity` (aloja Unity; legado AR + escena Visor3D).
- `data/` → repositorios y datos: `FirebaseRepository`, `GeminiRepository`,
  `SolicitudComercioRepository`, `UserPreferences`, `SampleData` (fallback + carga
  Firestore), `RutasTuristicasData`.
- `map/` → `MapaConfig` (estilo OSM/URL/cámara) y `MapaMarkerFactory` (iconos).
- `model/` → `City`, `Monument`, `Afluencia`, `Comercio`, `RutaTuristica`,
  `SolicitudComercio`, `UserProfile`, `UserRole`.
- `navigation/` → `Routes`, `AppNavigation`.
- `ui/components/` → componentes reutilizables (cards, botón, top bar, empty state).
- `ui/screens/` → todas las pantallas, incluida `MapaActivity` (nativa).
- `ui/theme/` → `Color.kt`, `Theme.kt`, `Type.kt`.
- `ui/viewmodels/` → `UserViewModel`, `ComerciosViewModel`, `ComercioDetalleViewModel`,
  `SolicitudComercioViewModel`, `MapaViewModel` (sin cablear).
- `util/` → `TelefonoUtils` (normalización de teléfono/WhatsApp).

Además, en `nicaexp_web/` vive el **backend de administración** (CodeIgniter 4 + la misma
base Firestore): API REST con API key y panel web AdminLTE 4 con DataTables y modales.
Detalle en `nicaexp_web/README.md` y en `DEVELOPMENT.md`.

Mapa de archivos completo en `PROJECT_MAP.md`.

## ¿Qué debo leer primero?

1. `AGENTS.md` (reglas).
2. Este `INIT.md`.
3. `ARCHITECTURE.md` (cómo se conectan las piezas).
4. `PROJECT_MAP.md` (qué hace cada archivo).
5. `ROADMAP.md` (qué está realmente implementado).

## ¿Cómo compilo?

```powershell
.\gradlew.bat :app:assembleDebug
```

Salida: `app\build\outputs\apk\debug\app-debug.apk`.

## ¿Cómo ejecuto en Android?

```powershell
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

También se puede ejecutar con el botón **Run** de Android Studio (módulo `app`).

## ¿Qué funcionalidades requieren teléfono físico?

- **Visor 3D de Unity** (el motor y ARCore necesitan ARM64; Unity solo exporta
  `arm64-v8a`). En emuladores suele fallar por ABI/binarios nativos.
- **MapLibre con renderer OpenGL**: la solución estable se validó en un teléfono real.
- **App Check con Play Integrity** requiere servicios de Google Play válidos.

## ¿Qué partes son estables y no se deben tocar innecesariamente?

- `MapaActivity.kt` + `MapaConfig.kt` + dependencia `android-sdk-opengl` (mapa funcional).
- Contrato de Intent hacia Unity y `UnityArActivity` (visor 3D).
- `FirebaseRepository`, `firestore.rules`, `app/google-services.json`.
- `AppNavigation.kt` y `Routes.kt`.
- `app/build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `unityLibrary/build.gradle`.

## ¿Qué está en desarrollo?

- Cablear la experiencia de mapa con marcadores (`MapaScreen`/`MapaViewModel`).
- Centrado dinámico del mapa por ciudad/monumento y "Ver en el mapa".
- Rutas para León y Managua (hoy solo Juigalpa).
- Limpieza de código legado de AR (`ArPlaceholderScreen`, rutas `ar_placeholder`).

## Quick Start for Agents

> Recuerda: **todas las respuestas al usuario deben ser en español** (regla 18 de
> `AGENTS.md`).

1. Leer `AGENTS.md` y este `INIT.md`.
2. Ejecutar `git status` y revisar cambios pendientes. **No descartar ni hacer reset.**
3. Revisar `ARCHITECTURE.md` y `PROJECT_MAP.md`.
4. Identificar pantalla/módulo afectado y leerlo completo (con imports).
5. Si es razonable, ejecutar el build base antes de editar para conocer el estado previo.
6. Implementar el cambio mínimo.
7. Volver a compilar (`.\gradlew.bat :app:assembleDebug`).
8. Corregir errores propios.
9. Informar archivos modificados y actualizar `CHANGELOG_AGENT.md`.
