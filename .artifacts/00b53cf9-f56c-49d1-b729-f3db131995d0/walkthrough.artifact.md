# Walkthrough - Integración Dinámica de Comercios (Firestore) en Itzae

Se ha implementado una conexión en tiempo real entre la base de datos de comercios (Firestore) y el asistente Itzae.

## Cambios Realizados

### [GeminiRepository.kt](file:///C:/Users/josep/StudioProjects/NicaExplorer-AndroidStudio/app/src/main/java/com/lospuntoycoma/nicaexplorer/data/GeminiRepository.kt)
- **Contexto Dinámico**: La función `generateContent` ahora recibe una lista de comercios. Si la lista contiene datos, estos se inyectan en el prompt antes de enviarlo a Gemini, permitiendo al modelo conocer los negocios reales registrados en la app.

### [AssistantScreen.kt](file:///C:/Users/josep/StudioProjects/NicaExplorer-AndroidStudio/app/src/main/java/com/lospuntoycoma/nicaexplorer/ui/screens/AssistantScreen.kt)
- **Carga de Datos al Enviar**: Al enviar un mensaje, la pantalla consulta automáticamente `FirebaseRepository.getComerciosActivos()`. La lista resultante se envía a Itzae para que pueda dar recomendaciones basadas en la base de datos actual.

## Verificación

> [!TIP]
> 1. Abre el asistente Itzae en la app.
> 2. Pregunta: "¿Qué comercios hay disponibles?".
> 3. Itzae ahora responderá listando los comercios que tengas en tu colección de Firestore (siempre que estén marcados como `activo: true`).
> 4. Los cambios son instantáneos: si agregas un comercio en Firestore, Itzae podrá mencionarlo en la siguiente consulta.
