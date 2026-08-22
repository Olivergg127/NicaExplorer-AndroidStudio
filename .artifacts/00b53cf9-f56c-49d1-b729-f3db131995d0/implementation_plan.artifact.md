# Plan de Integración Dinámica de Comercios en Itzae

Este plan detalla los cambios para que el asistente Itzae pueda recomendar dinámicamente los comercios registrados en Firestore.

## User Review Required

> [!NOTE]
> Para que Itzae conozca los comercios de tu base de datos, extraeremos la lista de negocios activos cada vez que le preguntes algo. Esto permite que, si registras un nuevo restaurante hoy, el asistente pueda recomendarlo mañana sin necesidad de actualizar el código.

## Proposed Changes

### [Gemini Repository]

#### [MODIFY] [GeminiRepository.kt](file:///C:/Users/josep/StudioProjects/NicaExplorer-AndroidStudio/app/src/main/java/com/lospuntoycoma/nicaexplorer/data/GeminiRepository.kt)
- Modificar la función `generateContent` para aceptar una lista opcional de objetos `Comercio`.
- Si la lista no está vacía, se formateará como una cadena de texto descriptiva que se inyectará en la consulta al modelo Gemini como contexto adicional.

### [Interfaz del Asistente]

#### [MODIFY] [AssistantScreen.kt](file:///C:/Users/josep/StudioProjects/NicaExplorer-AndroidStudio/app/src/main/java/com/lospuntoycoma/nicaexplorer/ui/screens/AssistantScreen.kt)
- Importar `FirebaseRepository`.
- En la función `sendMessage`, antes de llamar al asistente, obtener los comercios activos usando `FirebaseRepository.getComerciosActivos()`.
- Pasar esta lista a la función `generateContent` del repositorio de Gemini.

## Verification Plan

### Manual Verification
1.  Abre la pantalla de Itzae.
2.  Pregunta: "¿Qué comercios hay en Managua?" o "¿Recomienda un restaurante?".
3.  Verifica que Itzae mencione nombres de comercios que tengas registrados en tu consola de Firebase Firestore.
4.  Confirma que las respuestas sigan siendo concretas y en texto plano.
