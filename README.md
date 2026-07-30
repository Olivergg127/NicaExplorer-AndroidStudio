# NicaExplorer 🇳🇮

NicaExplorer es una aplicación móvil desarrollada como proyecto académico cuyo objetivo es promover el turismo en Nicaragua mediante el uso de Realidad Aumentada (AR), modelos 3D y un asistente inteligente basado en Inteligencia Artificial.

La aplicación permite explorar monumentos y sitios turísticos de forma interactiva, ofreciendo una experiencia inmersiva donde el usuario puede visualizar modelos 3D en su entorno utilizando la cámara del dispositivo móvil.

---

# Objetivo del proyecto

Desarrollar una plataforma móvil que incentive el turismo nacional mediante tecnologías emergentes como:

- Realidad Aumentada (AR)
- Modelos 3D
- Inteligencia Artificial
- Android Nativo con Jetpack Compose
- Firebase

---

# Tecnologías utilizadas

## Frontend

- Kotlin
- Jetpack Compose
- Material Design 3

## Backend

- Firebase Authentication
- Firebase Firestore

## Inteligencia Artificial

- Google Gemini API

## Motor 3D

- Unity 6
- AR Foundation
- Google ARCore

## Herramientas

- Android Studio
- Unity
- Blender
- Git
- GitHub
- Figma

---

# Arquitectura del proyecto

Android Studio funciona como la aplicación principal.

Unity se encuentra integrado como **Unity as a Library**, permitiendo ejecutar experiencias de Realidad Aumentada directamente desde la aplicación Android.

La comunicación entre Android y Unity se realiza mediante **Intent Extras**, enviando información sobre la ciudad y el monumento seleccionado para cargar el modelo correspondiente.

---

# Funcionalidades implementadas

- Inicio de sesión con Firebase Authentication.
- Registro de usuarios.
- Navegación mediante Jetpack Compose.
- Splash Screen con autenticación automática.
- Catálogo de monumentos.
- Perfil de usuario.
- Integración inicial del asistente basado en Google Gemini.
- Integración de Unity como Library.
- Experiencia de Realidad Aumentada utilizando AR Foundation.
- Colocación de modelos 3D.
- Reubicación del modelo.
- Escalado del modelo mediante gestos táctiles.
- Sistema inicial de rotación del modelo.
- Restablecimiento automático de posición, escala y rotación.

---

# Estado actual del proyecto

Actualmente el proyecto se encuentra en una fase **Beta 1.0**, enfocada en validar la arquitectura general de la aplicación, la integración entre Android Studio y Unity, y el funcionamiento de la experiencia de Realidad Aumentada.

La mayoría de las funcionalidades principales ya se encuentran implementadas y operativas, mientras que algunos apartados continúan en proceso de optimización para futuras versiones.

---

# Funcionalidades en desarrollo

## Modelos 3D y texturas

Los modelos utilizados actualmente corresponden a versiones preliminares destinadas a validar la integración de la Realidad Aumentada.

Las texturas oficiales aún no han sido incorporadas debido a que el equipo se encuentra gestionando los permisos y recursos digitales necesarios con museos y sitios turísticos para utilizar modelos y materiales oficiales.

En versiones futuras, estos modelos serán reemplazados por versiones completamente texturizadas y optimizadas que representarán fielmente cada monumento y atractivo turístico.

---

## Sistema de interacción con modelos

Actualmente el sistema permite:

- Colocar modelos.
- Reubicar modelos.
- Escalar modelos.
- Rotación básica mediante gestos táctiles.

La interacción de rotación aún se encuentra en proceso de optimización con el objetivo de ofrecer una experiencia más intuitiva y fluida en diferentes dispositivos Android.

En próximas versiones se implementará un sistema de manipulación más preciso y natural para mejorar la experiencia del usuario.

---

# Próximas funcionalidades

- Modelos 3D oficiales completamente texturizados.
- Catálogo ampliado de ciudades y monumentos.
- Optimización del sistema de interacción en AR.
- Mejoras en el asistente inteligente.
- Información histórica ampliada.
- Sistema de favoritos.
- Rutas turísticas.
- Integración con mapas.
- Optimización de rendimiento.

---

# Equipo de desarrollo

Los Punto y Coma

Proyecto desarrollado para la Hackathon Universitaria.

---

# Requisitos

- Android 10 o superior.
- Dispositivo compatible con Google ARCore.
- Cámara trasera.
- Conexión a Internet para autenticación e Inteligencia Artificial.

---

# Instalación

1. Clonar el repositorio.

```bash
git clone https://github.com/Olivergg127/Los-punto-y-coma.git
```

2. Abrir el proyecto en Android Studio.

3. Configurar Firebase.

4. Abrir Unity para realizar modificaciones en la experiencia AR cuando sea necesario.

5. Ejecutar la aplicación en un dispositivo compatible con ARCore.

---

# Video de navegacion
https://drive.google.com/file/d/1Hv8G8RbysrzM5twwemFSqcf_BdQe7Fv5/view?usp=drivesdk

Video uso de aplicación

# Estado del repositorio

Actualmente el proyecto se encuentra en desarrollo activo.

Las funcionalidades principales ya son operativas y continuarán mejorándose durante las siguientes iteraciones del proyecto.

---

# Licencia

Proyecto desarrollado con fines académicos.

Todos los derechos de los modelos, imágenes, contenido turístico y recursos oficiales pertenecen a sus respectivos propietarios.

Los modelos, texturas y recursos gráficos oficiales serán incorporados únicamente con la autorización correspondiente de las instituciones y sitios turísticos involucrados.