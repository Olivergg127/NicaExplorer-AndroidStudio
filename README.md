# NicaExplorer 🇳🇮

**Explora. Conecta. Inspira.**

NicaExplorer es una aplicación móvil Android orientada al turismo de Nicaragua. Integra información cultural de ciudades y monumentos, fotografías, modelos 3D, comercios locales, turismo responsable e inteligencia artificial mediante Itzae. Busca facilitar la exploración del patrimonio y apoyar la visibilidad de experiencias y emprendimientos locales.

## Funcionalidades actuales

- Registro, inicio de sesión y recuperación de contraseña.
- Perfil de usuario y edición del nombre.
- Ciudades y catálogo de monumentos con información histórica y cultural.
- Fotografías locales, lugares guardados e historial de exploración.
- Visor3D con Unity: rotación, zoom, restablecimiento y salida.
- Asistente IA Itzae con contexto de monumentos, ciudades y afluencia.
- Comercios/restaurantes locales y formulario de solicitud de incorporación.
- Consejos de turismo responsable.
- Afluencia estimada/orientativa (BAJA, MODERADA, ALTA).
- Ruta cultural/inteligente predefinida de Juigalpa.
- Panel administrativo con roles USUARIO, ADMIN y AUDITOR.

No se ofrecen GPS, navegación GPS, rutas en tiempo real ni afluencia en tiempo real. La afluencia y los tiempos de la ruta son orientativos del prototipo.

## Video demostrativo

[Ver video demostrativo](https://drive.google.com/file/d/1Hv8G8RbysrzM5twwemFSqcf_BdQe7Fv5/view?usp=sharing)

## Ciudades y monumentos

Las únicas ciudades vigentes son Juigalpa, León y Managua. El catálogo contiene exactamente ocho monumentos:

| Ciudad | Monumento | monumentId |
|---|---|---|
| Juigalpa | Homenaje a la Madre Juigalpina | homenaje_madre_juigalpina |
| Juigalpa | Toro Chontaleño | toro_chontaleno |
| Juigalpa | Estatua Museo Juigalpa | estatua_museo_juigalpa |
| León | Tumba de Rubén Darío | tumba_ruben_dario |
| León | Estatua de San Benito | estatua_san_benito |
| Managua | Árbol de la Vida | arbol_vida |
| Managua | Estatua de Rubén Darío | ruben_dario |
| Managua | Campana de la Paz | campana_de_la_paz |

## Tecnologías

- Android Studio, Kotlin, Jetpack Compose y Material 3.
- Firebase Authentication para cuentas y sesión.
- Cloud Firestore como base de datos NoSQL orientada a documentos.
- Google Gemini mediante GeminiRepository para Itzae.
- Unity 6 integrado como módulo Android unityLibrary para Visor3D.
- Blender para la preparación de modelos 3D.
- Git y GitHub para control de versiones y entregas.

Versiones comprobadas: Android Gradle Plugin 8.13.2, Kotlin 2.3.0, Gradle 8.13, JVM 17, compileSdk/targetSdk 34, minSdk 30 y Unity 6000.5.1f1.

## Funcionamiento y datos

Android/Kotlin es la aplicación principal y Compose implementa sus interfaces. Firebase Authentication crea y autentica las cuentas. FirebaseRepository.registerUser() crea todo registro nuevo con rol = USUARIO y un documento usuarios/{uid} con uid, nombre, correo, rol y fechaRegistro.

Firestore es la fuente principal de ciudades y lugares. SampleData.kt funciona como fallback si Firestore no está disponible o no devuelve datos válidos.

Colecciones principales:

- usuarios/{uid}: perfil, nombre, correo, rol y fecha de registro.
- ciudades/{cityId}: ciudad, descripción e imagenKey.
- lugares/{monumentId}: nombre, ciudad, descripción, historia, categoría, período/año, afluencia, consejosResponsables, imagenKey, modeloUnity y monumentId.
- comercios/{id}: negocios activos, información de contacto y portada remota/local.
- solicitudes_comercios/{id}: solicitudes de incorporación creadas por usuarios autenticados.

La colección mail está bloqueada por reglas y no es una funcionalidad activa. Las solicitudes validan campos obligatorios, teléfono, correo, cityId, usuario autenticado, estado pendiente y timestamp del servidor.

La estructura usa identificadores lógicos uid, userId, cityId y monumentId para reducir redundancia. Al ser Firestore NoSQL, las formas normales como 2FN no se aplican formalmente; el entregable utiliza un diagrama de clases Firestore/BD no relacional.

## Roles y seguridad

- USUARIO: usa las funciones normales, lee su propio perfil y modifica únicamente su nombre. No puede cambiar rol ni acceder al panel administrativo.
- AUDITOR: accede al panel, consulta/lista usuarios, busca y filtra por rol y trabaja en modo solo lectura. No cambia roles, edita otros usuarios ni elimina información.
- ADMIN: accede al panel, consulta/lista usuarios y cambia únicamente el campo rol de otros usuarios mediante confirmación. No cambia su propio rol desde ese flujo y puede editar su propio nombre.

La seguridad también está aplicada en firestore.rules: protege uid y rol, bloquea autoelevación, restringe la actualización personal al nombre, limita al ADMIN a cambiar rol de otro usuario y bloquea eliminaciones. Se mantiene compatibilidad con la cuenta administrativa histórica del prototipo.

El panel incluye contadores, buscador por nombre/correo, filtros Todos/Usuario/Admin/Auditor, chips de rol, diálogo de cambio, confirmación, Snackbar y modo Auditor de solo lectura. El acceso visual y la ruta admin_panel verifican ADMIN o AUDITOR; USUARIO no puede entrar.

## Normalización de usuarios

tools/normalize_users.mjs permite auditoría dry-run y migración con --apply. Completa únicamente campos faltantes, conserva los existentes y consulta Firebase Authentication para recuperar correo y fecha de creación asociadas al UID. Los documentos actuales ya fueron normalizados con uid, nombre, correo, rol y fechaRegistro.

## Unity y Visor3D

El flujo real es:

```text
Android: monumentId
        ↓ Intent
Unity: AndroidIntentReceiver
        ↓
Visor3DController
        ↓
prefab correspondiente → Instantiate
```

Visor3DController relee el Intent en cada entrada, destruye la instancia anterior y rechaza IDs desconocidos sin fallback silencioso.

| monumentId | Prefab |
|---|---|
| homenaje_madre_juigalpina | HomenajeMadreJuigalpina |
| toro_chontaleno | ToroChontaleno |
| estatua_museo_juigalpa | EstatuaMuseoJuigalpa |
| tumba_ruben_dario | TumbaRubenDario |
| estatua_san_benito | SanBenito |
| arbol_vida | ArbolDeLaVida |
| ruben_dario | EstatuaRubenDario |
| campana_de_la_paz | CampanaDeLaPaz |

La Estatua de Rubén Darío usa Assets/Models/Managua/ruben_dario.fbx y Assets/Prefabs/EstatuaRubenDario.prefab. La primera apertura de Unity puede tardar algunos segundos por la inicialización del motor; las posteriores suelen ser más rápidas.

## Estructura resumida

```text
NicaExplorer/
├── app/
├── unityLibrary/
├── tools/
│   └── normalize_users.mjs
├── firestore.rules
├── README.md
├── build.gradle.kts
└── settings.gradle.kts
```

## Instalación y ejecución

1. Clonar el repositorio:
```powershell
git clone https://github.com/Olivergg127/NicaExplorer-AndroidStudio.git
cd NicaExplorer-AndroidStudio
```

2. Abrir la carpeta en Android Studio y ejecutar Sync Project with Gradle Files.
3. Colocar el google-services.json correspondiente en app/ y habilitar Authentication/Firestore según las reglas publicadas.
4. Confirmar que unityLibrary/ esté presente.
5. Compilar debug:
```powershell
.\gradlew.bat :app:assembleDebug
```

APK actual comprobado: app/build/outputs/apk/debug/app-debug.apk.

6. Instalar en un dispositivo autorizado:
```powershell
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Para una release firmada se requiere un keystore.properties local configurado fuera del repositorio y .\gradlew.bat :app:assembleRelease. No se incluyen credenciales, contraseñas, keystores, ADC, tokens ni API keys privadas.

## GitHub y control de versiones

Repositorio: https://github.com/Olivergg127/NicaExplorer-AndroidStudio

Git se utiliza para el control de versiones mediante ramas, commits, pull y push. GitHub aloja el repositorio y GitHub Releases se utiliza para distribuir versiones de la aplicación.

## Equipo — Los Punto y Coma

- Oliver Javier Gutiérrez Castro — Desarrollo
- Joseph Esau Centeno Urbina — Desarrollo
- Verónica Michelle Robleto Trujillo — Diseño
- Arlen Rodolfo Urbina Urbina — Comunicación
- Elvis Josué Miranda Méndez — Marketing

## Alcance y transparencia

La ruta inteligente está desarrollada principalmente para Juigalpa porque es la ciudad con comercios cargados en el prototipo. La afluencia es orientativa. No hay GPS, navegación ni rutas en tiempo real. Las funciones administrativas y la integración Unity forman parte de un prototipo académico.

Las reglas, claves privadas, tokens, contraseñas, keystores y credenciales ADC nunca deben versionarse.
