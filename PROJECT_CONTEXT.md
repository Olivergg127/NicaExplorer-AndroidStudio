# PROJECT_CONTEXT.md — Producto y decisiones

> Conocimiento de producto de NicaExplorer. No describe arquitectura (para eso,
> `ARCHITECTURE.md`). Solo se documentan decisiones confirmadas en el código o en la
> documentación del repositorio.

## Visión

NicaExplorer nace como una app turística de Nicaragua que une **turismo, cultura y
tecnología** en una experiencia interactiva. Busca que tanto visitantes como locales
descubran el patrimonio del país y, al mismo tiempo, dar visibilidad a emprendimientos
locales.

Lema: **"Explora. Conecta. Inspira."**

## Público objetivo

- Turistas nacionales y extranjeros que visitan Nicaragua.
- Población local interesada en su patrimonio.
- Pequeños comercios y restaurantes que quieren visibilidad (solicitud de incorporación).
- Contexto: **prototipo académico** del equipo Los Punto y Coma.

## Problema

La información turística y cultural de Nicaragua está dispersa y no siempre es accesible o
interactiva. Los negocios locales tienen poca presencia digital. No hay una experiencia
única que combine mapa, historia, 3D e IA.

## Propuesta de valor

Una sola app que permite explorar ciudades y monumentos, entender su historia, **verlos en
3D**, preguntar a un asistente de IA con contexto local, descubrir comercios y seguir rutas
culturales, todo con una identidad visual nicaragüense.

## Ciudades y categorías actuales

- **Ciudades vigentes (3):** Juigalpa, León y Managua. Cualquier otra ciudad no está
  soportada y `SampleData.loadFromFirestore()` filtra por estos IDs.
- **Monumentos (8):**

  | Ciudad | Monumento | `monumentId` |
  |---|---|---|
  | Juigalpa | Homenaje a la Madre Juigalpina | `homenaje_madre_juigalpina` |
  | Juigalpa | Toro Chontaleño | `toro_chontaleno` |
  | Juigalpa | Estatua del Museo de Juigalpa | `estatua_museo_juigalpa` |
  | León | Tumba de Rubén Darío | `tumba_ruben_dario` |
  | León | Estatua de San Benito | `estatua_san_benito` |
  | Managua | Árbol de la Vida | `arbol_vida` |
  | Managua | Estatua de Rubén Darío | `ruben_dario` |
  | Managua | Campana de la Paz | `campana_de_la_paz` |

  > En el visor 3D ya está integrada "Los Motivos del Lobo (Escultura)" (León,
  > `los_motivos_del_lobo_escultura` → `PazHermanoLobo`), con el lugar registrado en
  > Firestore (`modeloUnity = PazHermanoLobo`).

- **Categorías de monumento (ejemplos reales):** Monumento cultural, Tradición ganadera,
  Patrimonio arqueológico, Monumento urbano, Monumento conmemorativo, Monumento histórico,
  Patrimonio religioso.

## Funcionalidades de producto

- Autenticación con correo y contraseña (registro, login, recuperación).
- Exploración por ciudad y catálogo de monumentos con historia, año, categoría y
  **afluencia estimada** (BAJA/MODERADA/ALTA).
- **Afluencia estimada**: es orientativa del prototipo; **no** es tiempo real ni GPS. Se
  usa para sugerir alternativas más tranquilas.
- Visor 3D de monumentos con Unity (rotación, zoom, restablecer, salir).
- Asistente **Itzae** (IA) con contexto de monumento, catálogo local y comercios reales.
- Comercios/restaurantes locales: listado, detalle, contacto por WhatsApp, ver en Google
  Maps y formulario de solicitud de incorporación.
- Lugares guardados e historial de exploración (locales, por usuario).
- **Ruta cultural/inteligente** predefinida, implementada **solo para Juigalpa**.
- Panel administrativo con roles USUARIO, ADMIN y AUDITOR.
- Consejos de turismo responsable por monumento.
- Mapa turístico con MapLibre + OpenStreetMap (vista de Nicaragua).

## Identidad

- Paleta **"Guardabarranco"** (ave nacional): turquesa (lagunas), azul (cielo/lagos),
  terracota (cerámica de San Juan de Oriente), dorado reservado.
- Interfaz predominantemente **oscura** con acentos verde/turquesa; botón de Itzae en
  terracota.
- Degradados por ciudad: Juigalpa púrpura chontaleño, León dorado colonial, Managua verde
  capital.
- Detalle completo en `docs/paleta-colores.md` y `ui/theme/Color.kt`.

## Asistente Itzae

- Nombre e identidad propia; se presenta como "Itzae, el Asistente de NicaExplorer".
- Reglas de estilo: respuestas concisas, sin presentarse en cada mensaje, sin markdown,
  texto plano.
- Prioriza Juigalpa, Managua y León; el catálogo local es la fuente de verdad de los
  monumentos; para comercios usa conocimiento general pero sin inventar datos inexistentes.
- Habla siempre de **"afluencia estimada"**, nunca de datos en tiempo real.

## Comercios locales

- Se leen de la colección `comercios` (solo `activo == true`).
- Categorías como restaurantes/cafeterías (ejemplos presentes como imágenes locales:
  AmerriPizza, Coffee Break, Mi Choza, aunque los datos reales provienen de Firestore).
- Los negocios pueden **solicitar** su incorporación (colección `solicitudes_comercios`,
  estado `pendiente`). La revisión/estado se hace fuera de la app (Firebase Console/Admin SDK),
  según `docs/solicitudes-comercios-firebase.md`.

## Mapa turístico

- MapLibre Native Android con teselas raster de OpenStreetMap.
- Hoy muestra Nicaragua completa; **no** centra por ciudad, no tiene marcadores en la
  versión accesible y no usa ubicación del usuario.

## Decisions already made

- La **realidad aumentada (AR) fue reemplazada por el visor 3D** como función principal. El
  código AR permanece como legado.
- Android principal usa **Compose** para toda la UI.
- **Unity continúa** como el visor 3D.
- Se usa **MapLibre** para el mapa (no otra librería).
- El mapa estable se ejecuta en una **Activity nativa + renderer OpenGL**, porque la
  integración directa en Compose presentó problemas de renderer/lifecycle en el dispositivo
  de prueba y la variante Vulkan provocaba crash nativo.
- Se mantiene una **experiencia visual oscura con identidad turquesa/verde**.
- **Firestore es la fuente principal** del catálogo y `SampleData` es el fallback local.
- Los **datos personales locales** (guardados/historial) se almacenan con DataStore por `uid`.
- Toda cuenta creada desde la app empieza con rol **USUARIO**.
- La ruta inteligente se limita a **Juigalpa** hasta tener comercios en otras ciudades.

## Do not assume

- **No asumir AR** como función vigente: es visor 3D.
- **No asumir GPS, navegación, rutas en tiempo real ni afluencia en tiempo real.**
- **No asumir que todos los negocios están en Firebase** ni que las imágenes remotas
  existen; hay fallbacks locales.
- **No asumir que existen rutas** en León o Managua: solo Juigalpa.
- **No asumir que hay ubicación del usuario** en el mapa.
- **No asumir que una función del diseño o de una captura ya existe en código.** Verifica en
  `ROADMAP.md` y en el código.
- **No asumir que `MapaScreen`/`MapaViewModel` están en uso**: no están cableados.
- **No asumir que el proyecto Unity fuente está en este repo**: está fuera.
