# DEVELOPMENT.md — Entorno, build y herramientas

> Versiones y comandos verificados contra los archivos del repositorio. Shell del
> proyecto: **Windows PowerShell**.

## Requisitos

- **Android Studio** (con JDK embebido, JBR). `gradle.properties` fuerza
  `org.gradle.java.home` a `C:/Program Files/Android/Android Studio/jbr`.
- **JDK 17** (source/target compatibility y `jvmTarget = 17`).
- **Android SDK** con `compileSdk 34` instalado. La ruta se resuelve vía
  `local.properties` (`sdk.dir`), que está fuera de Git.
- **NDK 27.2.12479018** (requerido por el módulo Unity).
- **Unity 6000.5.1f1** (solo si se va a re-exportar `unityLibrary`; no es necesario para
  compilar la app si los binarios ya están presentes).
- **ADB** en el PATH para instalar y ver logs.
- **Node.js** (solo para `tools/*.mjs` con `firebase-admin`; no es necesario para la app).

## Versiones del proyecto

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
| `versionCode` / `versionName` | 3 / 1.1.1 |
| Firebase BOM | 34.16.0 |
| MapLibre (`android-sdk-opengl`) | 12.3.1 |
| Unity | 6000.5.1f1 |
| Unity `compileSdk` / `targetSdk` / `buildTools` | 36 / 36 / 36.0.0 |
| NDK (Unity) | 27.2.12479018 |
| ABI soportada | `arm64-v8a` (única) |
| Proyecto Firebase | `nica-explore` |

## Dependencias principales (`app/build.gradle.kts`)

- Compose: `androidx.compose.ui`, `ui-graphics`, `ui-tooling-preview`, `material3`,
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

## Sincronizar Gradle

- Android Studio: **File → Sync Project with Gradle Files**.
- O desde consola: `.\gradlew.bat --refresh-dependencies` (no suele ser necesario).

## Compilar

```powershell
# Debug (comando principal)
.\gradlew.bat :app:assembleDebug

# Debug saltando la compilación IL2CPP de Unity (más rápido en pruebas)
.\gradlew.bat :app:assembleDebug -PskipIl2CppBuild

# Release (requiere keystore.properties local, fuera de Git)
.\gradlew.bat :app:assembleRelease
```

Salida debug: `app\build\outputs\apk\debug\app-debug.apk`.

## Instalar y ejecutar

```powershell
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

También con el botón **Run** de Android Studio sobre el módulo `app`.

## Logs y depuración

```powershell
# Todos los logs de la app
adb logcat -s NicaExplorerMapActivity NicaExplorerMap FirebaseRepository UnityArActivity Unity

# Limpiar buffer antes de reproducir un problema
adb logcat -c
```

Tags relevantes en el código:
- `NicaExplorerMapActivity` → `MapaActivity` nativa (carga de estilo/cámara).
- `NicaExplorerMap` → `MapaScreen` (variante Compose, no cableada).
- `FirebaseRepository` → errores de Auth/Firestore.
- `UnityArActivity` → ciclo de vida del visor 3D/AR.
- `Unity` → mensajes del motor.

## Probar Firebase

1. Verificar que `app/google-services.json` corresponde al proyecto `nica-explore`
   (**no editar ni exponer**).
2. Registro/login: crear una cuenta y confirmar el documento `usuarios/{uid}` con rol
   `USUARIO`.
3. Catálogo: revisar `ciudades` y `lugares` en Firestore. Si están vacías o fallan, la app
   usa `SampleData` (fallback).
4. Reglas: `firestore.rules` debe estar publicado. Las pruebas de seguridad importantes son:
   no autoelevación de `rol`, no borrado, cambio de rol solo por ADMIN.
5. Herramientas opcionales (`tools/`, requieren `firebase-admin` + ADC vía
   `gcloud auth application-default login`):
   - `node tools/seed_firestore.mjs` → seed idempotente del catálogo.
   - `node tools/verify_firestore.mjs` → verifica campos requeridos.
   - `node tools/normalize_users.mjs` (dry-run) / `... --apply` → normaliza usuarios.
6. La colección `mail` está bloqueada por reglas y no es funcionalidad activa del cliente.

## MapLibre

- **Versión real:** `org.maplibre.gl:android-sdk-opengl:12.3.1` (variante **OpenGL ES**).
- **Activity utilizada:** `ui/screens/MapaActivity.kt` (Activity nativa, no Compose).
- **Estilo/base map:** `map/MapaConfig.kt` define `STYLE_JSON` con una fuente **raster**
  de OSM: `https://tile.openstreetmap.org/{z}/{x}/{y}.png`, `tileSize 256`, `maxzoom 19` y
  atribución "© OpenStreetMap contributors". No usa estilos vectoriales ni API key.
- **Renderer:** OpenGL.
- **Por qué OpenGL:** la variante por defecto (`android-sdk`, Vulkan) provocaba un crash
  nativo al renderizar el `MapView` en el dispositivo de prueba (comentario en
  `app/build.gradle.kts`). La integración directa de `MapView` en Compose también dio
  problemas de renderer/lifecycle, por eso el mapa funcional vive en una Activity nativa.
- **Lifecycle:** `MapLibre.getInstance(...)` se llama en `MainActivity.onCreate` y también
  en `MapaActivity.onCreate`; `MapaActivity` delega todo el ciclo de vida al `MapView`.
- **Vista inicial:** `MapaConfig.NICARAGUA = LatLng(12.8654, -85.2072)`, zoom `6.3`.
- **Limitaciones actuales:** sin marcadores, sin centrado por ciudad/monumento, sin
  ubicación del usuario. `MapaScreen` (Compose con marcadores) existe pero **no está
  cableada**.
- **Permisos de ubicación:** el AAR declara `ACCESS_FINE/COARSE_LOCATION`; el manifest los
  elimina con `tools:node="remove"` porque la fase GPS aún no existe. No reintroducir sin
  implementar el flujo de permisos.

## Unity 3D Viewer

- **Invocación desde Android:** `CatalogScreen` (botón "Ver en 3D") crea un `Intent`
  explícito a `com.lospuntoycoma.nicaexplorer.ar.UnityArActivity` con `FLAG_ACTIVITY_NEW_TASK`
  y extras `cityId`, `monumentId`, `escena = "visor3d"`.
- **Activity/interfaz:** `ar/UnityArActivity.kt` extiende
  `com.unity3d.player.UnityPlayerActivity`; vive en su propia tarea (`taskAffinity` +
  `singleTask`), maneja Back como "Salir" (`moveTaskToBack`) y reenvía mensajes a Unity.
- **Paso del modelo:** se envía `monumentId`; Unity (`AndroidIntentReceiver` /
  `Visor3DController`) lo mapea al prefab correspondiente. El campo `Monument.modeloUnity`
  contiene el nombre del prefab y habilita el botón.
- **Mensajes nativos:** `UnityPlayer.UnitySendMessage("ARManager"|"Visor3D", "<método>", "")`
  para reiniciar/recargar/salir. `notificarUnityListo()` es llamado desde C#.
- **Archivos relevantes:**
  - `app/src/main/java/.../ar/UnityArActivity.kt`
  - `app/src/main/AndroidManifest.xml` (activities `UnityPlayerActivity` y `UnityArActivity`,
    con `tools:replace`).
  - `unityLibrary/` (módulo exportado; `libs/*.aar`, `jniLibs/arm64-v8a/*.so`,
    `Il2CppOutputProject/`).
  - `unityLibrary/build.gradle` (tarea `buildIl2Cpp`, ABI `arm64-v8a`).
- **Notas:** el proyecto Unity fuente **no está en el repo** (`C:/UnityProjects/NicaExplorer`).
  La primera apertura de Unity puede tardar por inicialización del motor. En emuladores
  suele fallar por dependencias ARM64/ARCore; se recomienda **teléfono físico ARM64**.

## Backend web `nicaexp_web` (CodeIgniter 4 + Firestore)

Backend de administración separado de la app Android. No usa MySQL: comparte la misma
base de Firestore (`nica-explore`). Ver `nicaexp_web/README.md` para el detalle.

- **Stack:** CodeIgniter 4.7.4, `kreait/firebase-php` 8.5, `google/cloud-firestore` 2.3.
- **Transporte Firestore:** `rest` (este PHP no tiene la extensión `grpc`).
- **Credenciales:** Application Default Credentials (`gcloud`) o JSON de cuenta de
  servicio vía `Nica.credentialsFile` (nunca versionado).
- **Interfaz:** panel con tema claro/oscuro propio de NicaExplorer (sobre AdminLTE 4/Bootstrap) +
  DataTables 2 + modales; login y API key.
- **API:** `GET /api/v1/health` y CRUD `/api/v1/{coleccion}[/{id}]` con header `X-API-KEY`.
- **Panel:** `http://localhost:8080/panel` (login con `Nica.adminUser`/`Nica.adminPassword`).

Comandos:

```powershell
cd nicaexp_web
composer install                                   # tras clonar (vendor/ no se versiona)
php spark serve --host 0.0.0.0 --port 8080         # accesible desde la red local
```

### Acceso desde un dispositivo Android real

La app consume la API REST del backend. La URL base y la API key se inyectan en la app
desde `local.properties` (no versionado): `nica.apiBaseUrl` y `nica.apiKey`.

**Opción A — túnel USB (recomendada si la Wi-Fi aísla los clientes):**

```powershell
# Backend escuchando en el PC
cd nicaexp_web
php spark serve --port 8080

# En otra terminal: túnel del puerto del móvil al PC
adb reverse tcp:8080 tcp:8080
```

- `local.properties`: `nica.apiBaseUrl=http://localhost:8080`
- `.env` (backend): `app.baseURL = 'http://localhost:8080/'` y
  `Nica.publicBaseUrl = http://localhost:8080`
- El panel se abre en el PC en <http://localhost:8080/panel>.
- El túnel `adb reverse` debe re-ejecutarse cada vez que se reconecta el dispositivo.

**Opción B — red local (si NO hay aislamiento de clientes):**

- `nica.apiBaseUrl=http://<IP-LAN>:8080`, `app.baseURL='http://<IP-LAN>:8080/'`,
  `Nica.publicBaseUrl=http://<IP-LAN>:8080`.
- Abrir el puerto 8080 en el Firewall de Windows (PowerShell **como administrador**):

  ```powershell
  netsh advfirewall firewall add rule name="NicaExplorer Backend 8080" dir=in action=allow protocol=TCP localport=8080
  ```

Al cambiar de host, reescribe las URLs de las imágenes en Firestore con
`php spark nica:rebase-imagenes`.

La app permite HTTP sin cifrar (`android:usesCleartextTraffic="true"`) para desarrollo;
deberá retirarse cuando el backend use HTTPS.

### Entorno PHP/Composer de este equipo

Configurado a nivel de usuario/sistema para que el backend funcione:

- **Composer 2.10.3** en `%LOCALAPPDATA%\Programs\Composer` (`composer.phar` + `composer.bat`),
  agregado al **PATH de usuario**.
- **PHP 8.3.32** (`C:\WebStack\php`, desde winget). En `php.ini` se habilitaron:
  - `extension=intl`
  - `extension=fileinfo`
  - `curl.cainfo` y `openssl.cafile` → `C:\WebStack\php\extras\ssl\cacert.pem`
    (bundle de CA descargado de curl.se).
- **Google Cloud SDK** ya instalado; `gcloud auth application-default login` genera el ADC en
  `%APPDATA%\gcloud\application_default_credentials.json` (tipo `authorized_user`).
