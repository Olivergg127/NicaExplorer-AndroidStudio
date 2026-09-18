# AGENTS.md — Instrucciones para agentes de código

> Lee este archivo completo antes de tocar el repositorio. Aplica a cualquier agente
> automático (Codex, opencode, etc.) y también a desarrolladores nuevos.

## # Project identity

- **Nombre:** NicaExplorer.
- **Equipo:** Los Punto y Coma.
- **Propósito:** app turística de Nicaragua que combina turismo, cultura y tecnología:
  explorar ciudades, consultar monumentos, verlos en un **visor 3D** (Unity), hablar con
  el asistente **Itzae** (IA), descubrir comercios locales y consultar un mapa propio.
- **Plataforma:** Android.
- **Tecnologías principales:** Kotlin, Jetpack Compose, Material 3, Firebase
  (Authentication + Cloud Firestore + Firebase AI Logic/Gemini; App Check figura como
  dependencia pero aún **no se inicializa** en el código), Unity 6 exportado como módulo
  Android (`unityLibrary`) y MapLibre Native Android (variante OpenGL) con teselas de
  OpenStreetMap.
- **Package / applicationId:** `com.lospuntoycoma.nicaexplorer` (no cambiar).
- **Versión actual:** `versionCode = 3`, `versionName = "1.1.1"`.

Contexto detallado en `INIT.md`, arquitectura en `ARCHITECTURE.md`, visión de producto
en `PROJECT_CONTEXT.md`, entorno y build en `DEVELOPMENT.md`, reglas de estilo en
`CONVENTIONS.md`, estado y pendientes en `ROADMAP.md`.

## # Golden rules

1. **Inspeccionar antes de editar.** Lee los archivos afectados y su contexto/imports.
2. **Nunca inventar arquitectura, clases, archivos ni APIs.** Si algo no existe en el
   código, no lo documentes ni lo referencies como si existiera.
3. **Cambios pequeños e incrementales.** Prefiere el cambio mínimo que resuelve el
   problema; evita refactors masivos no solicitados.
4. **No romper funcionalidad existente.** Si dudas, pregunta antes de asumir.
5. **Compilar después de cambios significativos** (ver "Build/test rules").
6. **Corregir los errores propios antes de terminar.**
7. **No modificar Firebase, credenciales, `google-services.json`, `keystore.properties`,
   claves ni configuraciones sensibles** sin una solicitud explícita.
8. **No cambiar el package name / applicationId.**
9. **No eliminar funciones aparentemente sin uso sin verificar referencias** (usa Grep;
   puede haber rutas, imports o llamadas indirectas).
10. **No migrar Compose a XML** ni viceversa.
11. **No migrar MapLibre a otra librería de mapas** ni cambiar su estilo/backend sin una
    razón justificada y verificada.
12. **No reemplazar Unity mientras siga siendo el visor 3D.**
13. **No reintroducir realidad aumentada (AR) como función principal** salvo instrucción
    explícita. El código AR existe como legado; la función vigente es el **visor 3D**.
14. **No modificar la solución estable de MapLibre** (`MapaActivity` nativa + MapLibre
    OpenGL) sin una razón justificada. Fue la única variante que funcionó en un teléfono
    real; la integración directa en Compose dio problemas de renderer/lifecycle.
15. **Mantener el idioma:** código, identificadores y comentarios importantes en
    **español** cuando tenga sentido; nombres de APIs/servicios propios de librerías en
    inglés (p. ej. `FirebaseFirestore`, `MapView`, `viewModelScope`).
16. **No agregar dependencias nuevas** sin necesidad justificada.
17. **No hacer commit, amend, push ni PR** salvo que se solicite explícitamente.
18. **Responder siempre en español.** Toda la comunicación del agente con el usuario
    (explicaciones, planes, resúmenes, informes de archivos modificados, mensajes de
    commit y de PR) debe escribirse en **español**, aunque el código o las herramientas
    usen términos en inglés.

## # Agent workflow

Antes de implementar una función:

1. Leer `AGENTS.md`.
2. Leer `INIT.md`.
3. Revisar `ARCHITECTURE.md` (y `DEVELOPMENT.md` si toca build/mapa/Unity).
4. Ejecutar `git status` y revisar cambios pendientes (sin descartarlos).
5. Inspeccionar los archivos afectados con las herramientas de lectura/búsqueda.
6. Explicar brevemente el plan.
7. Implementar el cambio mínimo.
8. Compilar (`.\gradlew.bat :app:assembleDebug`).
9. Revisar errores y corregir los propios.
10. Informar qué archivos se modificaron.
11. Actualizar la documentación (`ARCHITECTURE.md`, `ROADMAP.md`, `CHANGELOG_AGENT.md`)
    si cambió la arquitectura o el comportamiento.

## # Safety / preservation rules

Trata como **zonas delicadas** y no las modifiques sin razón y sin verificar:

- **Firebase:** `app/google-services.json`, proyecto `nica-explore`, Authentication,
  Cloud Firestore, App Check. No tocar credenciales ni el archivo de configuración.
- **Autenticación:** flujo de registro/login/recuperación y `FirebaseRepository`.
- **Reglas de seguridad:** `firestore.rules` (protege `uid`, `rol`, autoelevación,
  actualización de nombre, cambio de rol por ADMIN y bloqueo de borrado).
- **Navegación:** `navigation/Routes.kt` y `navigation/AppNavigation.kt`.
- **Integración Android ↔ Unity:** `ar/UnityArActivity.kt` y el contrato de Intent
  (`cityId`, `monumentId`, `escena=visor3d`) y mensajes `UnitySendMessage`.
- **MapLibre Native Activity:** `ui/screens/MapaActivity.kt`, `map/MapaConfig.kt`,
  `map/MapaMarkerFactory.kt`. No romper la variante OpenGL ni el estilo OSM.
- **Renderer OpenGL:** no cambiar la dependencia `org.maplibre.gl:android-sdk-opengl`.
- **Configuración Gradle:** `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`,
  `gradle.properties`, `unityLibrary/build.gradle`, `shared/*.gradle`.
- **AndroidManifest:** activities, permisos y `tools:replace` de Unity.
- **Firma y secretos:** `keystore.properties`, `*.jks`, `*.keystore`, `local.properties`,
  cuentas de servicio/ADC. Nunca versionar ni exponer.
- **Recursos Unity generados** (`unityLibrary/src/main/Il2CppOutputProject/...`): son
  salida de la exportación de Unity; normalmente no se editan a mano.

## # Build/test rules

Shell del proyecto: **Windows PowerShell**. Usar el wrapper incluido (`gradlew.bat`).

Compilación debug (comando principal):

```powershell
.\gradlew.bat :app:assembleDebug
```

Salida: `app\build\outputs\apk\debug\app-debug.apk`.

Compilación release (requiere `keystore.properties` local, fuera de Git):

```powershell
.\gradlew.bat :app:assembleRelease
```

Instalar en un dispositivo conectado y autorizado:

```powershell
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Notas verificadas:
- El módulo `:unityLibrary` ejecuta la tarea `buildIl2Cpp` durante el ensamblado. Los
  binarios `libil2cpp.so`/`libunity.so` ya están precompilados en el repo, así que en
  condiciones normales Gradle puede reutilizarlos.
- Para saltar la compilación de IL2CPP en pruebas:
  `.\gradlew.bat :app:assembleDebug -PskipIl2CppBuild`.
- **No existe configuración de tests unitarios/instrumentados en el repositorio**, por lo
  que `./gradlew test` o `./gradlew lint` no están validados aquí. No los publiques como
  comandos oficiales sin comprobarlos primero.

## # Definition of Done

Una tarea **no** se considera terminada si:

- no compila;
- deja errores introducidos por el cambio;
- rompe la navegación;
- rompe Firebase (auth, lectura/escritura Firestore);
- rompe el mapa MapLibre (Activity nativa, renderer, estilo, zoom/desplazamiento);
- rompe el visor 3D de Unity (apertura, modelo por `monumentId`, salida);
- no se informa qué archivos se modificaron y por qué;
- introduce secretos o cambia configuración sensible sin autorización.

Consulta `ROADMAP.md` para saber qué está realmente implementado antes de afirmar que una
función existe.
