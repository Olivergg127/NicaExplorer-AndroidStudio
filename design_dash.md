# Refined Dashboard Design Skill — NicaExplorer

## Purpose

Diseñar y refinar dashboards modernos, profesionales, minimalistas y visualmente cuidados para NicaExplorer y proyectos similares.

El resultado **NO** debe parecer:

- un Bootstrap genérico;
- una plantilla administrativa gratuita;
- un dashboard generado automáticamente por IA;
- una colección de cards sin jerarquía;
- una interfaz excesivamente futurista;
- una interfaz saturada de gradientes, glassmorphism o efectos innecesarios.

El objetivo es producir interfaces que parezcan diseñadas deliberadamente por un equipo de producto profesional.

---

# NicaExplorer Dashboard Context

El panel pertenece a **NicaExplorer**.

## Identidad visual

La interfaz debe sentirse:

- oscura;
- moderna;
- refinada;
- tecnológica;
- minimalista;
- relacionada con turismo y cultura;
- coherente con la identidad verde/turquesa de NicaExplorer.

Evitar:

- estética gaming;
- estética corporativa genérica;
- apariencia Bootstrap;
- estética excesivamente futurista;
- demasiados efectos visuales;
- componentes que parezcan generados automáticamente.

## Áreas funcionales probables

El dashboard puede incluir, según lo que exista realmente en el proyecto:

- Dashboard general
- Lugares turísticos
- Monumentos
- Modelos 3D
- Ciudades
- Comercios
- Restaurantes
- Mapa
- Usuarios
- Solicitudes
- Contenido
- Configuración

No crear secciones que no sean necesarias.

El dashboard debe sentirse como parte del mismo producto NicaExplorer, no como una plantilla administrativa separada.

---

# Core Design Philosophy

Prioriza:

1. Claridad.
2. Jerarquía visual.
3. Espaciado consistente.
4. Tipografía refinada.
5. Densidad equilibrada.
6. Navegación intuitiva.
7. Coherencia entre componentes.
8. Microinteracciones discretas.
9. Funcionalidad antes que decoración.
10. Identidad visual propia.

Cada decisión visual debe tener una razón.

No añadir elementos simplemente para "hacer que se vea moderno".

---

# Visual Direction

Usar una estética:

- moderna;
- minimalista;
- sobria;
- profesional;
- ligeramente premium;
- limpia;
- humana;
- orientada a producto SaaS moderno.

Referencias conceptuales:

- Linear
- Vercel
- Stripe Dashboard
- Notion
- Raycast
- GitHub moderno
- Arc
- Framer
- interfaces fintech modernas

**No copiar estas interfaces literalmente.**

Usarlas únicamente como referencia de:

- jerarquía;
- espaciado;
- composición;
- tipografía;
- sobriedad.

---

# Avoid Generic Bootstrap Look

Evitar patrones como:

- sidebar azul genérica;
- navbar oscura + cards blancas;
- botones Bootstrap estándar;
- `border-radius: 4px` en todo;
- tablas sin diseño;
- cards con sombras fuertes;
- colores primarios Bootstrap;
- badges por todas partes;
- iconos sin consistencia;
- formularios tradicionales sin refinamiento.

Nunca hacer una composición genérica como:

```text
[Sidebar azul]
[Navbar gris]
[Card][Card][Card][Card]
[Tabla]
```

sin una razón de diseño.

---

# Avoid "AI Generated UI"

Evitar interfaces con:

- demasiados gradientes;
- círculos decorativos aleatorios;
- blobs;
- efectos glow;
- glassmorphism excesivo;
- tarjetas flotando por todas partes;
- sombras enormes;
- colores neón;
- títulos gigantes sin propósito;
- textos de marketing dentro de dashboards administrativos;
- iconos decorativos innecesarios;
- emojis como iconos principales;
- demasiadas estadísticas falsas.

Una buena interfaz no debe intentar demostrar que es moderna.

Debe sentirse moderna naturalmente.

---

# Layout

Usar una estructura clara.

Preferir:

```text
Sidebar
│
├── Branding
├── Navegación principal
├── Secciones secundarias
└── Perfil / ajustes

Main
│
├── Header contextual
├── Acciones principales
├── Resumen relevante
└── Contenido
```

El layout debe respirar.

Evitar llenar toda la pantalla.

Usar un ancho máximo cuando sea apropiado.

Ejemplo:

```css
max-width: 1440px;
margin: 0 auto;
```

---

# Sidebar

La sidebar debe ser:

- discreta;
- compacta;
- fácil de recorrer;
- sin fondos visualmente agresivos.

Preferir:

- icono + etiqueta;
- estado activo claramente visible;
- separación lógica entre grupos.

Ejemplo visual:

```text
NicaExplorer Admin

GENERAL

⌂ Dashboard
◇ Lugares
⌖ Mapa
▣ Modelos 3D

GESTIÓN

◫ Comercios
◎ Usuarios
▤ Solicitudes

SISTEMA

⚙ Configuración
```

Evitar sidebar con demasiados colores o elementos encerrados individualmente.

---

# Dashboard Hierarchy

La primera pantalla debe responder rápidamente:

- ¿Qué está ocurriendo?
- ¿Qué requiere atención?
- ¿Qué acciones puedo ejecutar?

No llenar el dashboard únicamente con números.

Usar aproximadamente:

- 3–5 métricas realmente importantes;
- actividad reciente;
- alertas;
- acciones rápidas;
- visualizaciones solo si aportan información.

---

# Cards

Usar cards solo cuando ayudan a agrupar información.

Preferir:

- bordes suaves;
- fondo ligeramente diferenciado;
- sombra casi inexistente;
- buen padding.

Ejemplo:

```css
border: 1px solid var(--border);
border-radius: 14px;
box-shadow: 0 1px 2px rgba(0,0,0,.03);
```

Evitar:

```css
box-shadow: 0 20px 60px rgba(...);
```

excepto overlays o modales.

---

# Typography

La tipografía debe ser una parte central del diseño.

Preferir fuentes:

- Inter
- Geist
- Manrope
- IBM Plex Sans
- Plus Jakarta Sans

Si el proyecto ya tiene una fuente definida, conservarla.

Jerarquía sugerida:

```text
Page title      28–32px / 600
Section title   18–20px / 600
Card title      14–16px / 500–600
Body            14–15px / 400
Meta            12–13px / 400
```

No usar bold excesivo.

No convertir todo en texto grande.

---

# Spacing System

Usar una escala coherente.

Preferir:

```text
4
8
12
16
20
24
32
40
48
```

Ejemplo:

```css
gap: 16px;
padding: 24px;
```

Evitar valores arbitrarios como:

```css
padding: 17px 29px 13px 21px;
```

sin justificación.

---

# Colors

La paleta debe ser limitada.

Usar:

1. Background.
2. Surface.
3. Border.
4. Primary text.
5. Secondary text.
6. Accent.
7. Semantic colors.

No diseñar utilizando 10 colores principales.

Para NicaExplorer, una orientación válida puede ser:

```text
Background      #0B0F0E
Surface         #111715
Surface Alt     #151D1A

Text Primary    #F3F6F4
Text Secondary  #8F9D96

Accent          #39C98D
Accent Soft     rgba(57, 201, 141, .12)

Border          rgba(255,255,255,.07)
```

Estos valores son orientativos.

Si el proyecto ya tiene tokens de color, reutilizarlos antes de crear otros nuevos.

---

# Buttons

Limitar variantes.

Preferir:

### Primary
Para acción principal.

### Secondary
Para acciones importantes pero secundarias.

### Ghost
Para acciones de baja prioridad.

### Danger
Solo para operaciones destructivas.

No usar múltiples colores de botones sin necesidad.

Los botones deben tener:

- altura coherente;
- iconos opcionales;
- estados hover;
- estados disabled;
- focus visible.

---

# Tables

Las tablas deben ser funcionales y refinadas.

Incluir cuando corresponda:

- búsqueda;
- filtros;
- ordenamiento;
- paginación;
- acciones contextuales.

Evitar:

- bordes completos tipo Excel;
- fondos alternados demasiado fuertes;
- acciones enormes por fila.

Preferir líneas divisorias ligeras.

Ejemplo:

```text
Nombre          Ciudad      Estado      Actualizado      ⋯
──────────────────────────────────────────────────────────
Catedral León   León        Publicado   Hace 2 h         ⋯
Toro Chontaleño Juigalpa    Borrador    Ayer             ⋯
```

---

# Forms

Los formularios deben tener:

- labels visibles;
- mensajes de error útiles;
- placeholders únicamente como ejemplo;
- grupos lógicos;
- ancho cómodo.

No usar placeholders como sustituto del label.

Preferir formularios divididos en bloques como:

```text
Información básica
Ubicación
Contenido
Multimedia
Configuración
```

en vez de una sola columna interminable.

---

# Empty States

No mostrar simplemente:

```text
No hay datos.
```

Crear empty states útiles:

```text
Todavía no hay comercios registrados.

Agrega el primer comercio para que aparezca
en el mapa y en las recomendaciones.

[Agregar comercio]
```

---

# Feedback

Toda acción debe producir feedback:

- loading;
- success;
- error;
- empty;
- disabled.

Ejemplo:

```text
Guardando...
Cambios guardados
No se pudo guardar
```

Evitar acciones silenciosas.

---

# Icons

Usar una sola librería.

Preferencias:

- Lucide
- Heroicons
- Phosphor

No mezclar estilos de iconos.

No utilizar emojis como sustituto de iconos profesionales.

---

# Motion

Las animaciones deben ser discretas.

Usar aproximadamente:

```text
150ms – 250ms
```

para:

- hover;
- apertura de menú;
- modal;
- cambio de tabs;
- dropdown.

Evitar animaciones constantes.

---

# Responsive Design

Debe funcionar bien en:

- desktop;
- laptop;
- tablet.

Para dashboards administrativos, mobile puede simplificarse.

La sidebar puede convertirse en drawer.

Las tablas pueden convertirse en listas/cards cuando sea necesario.

---

# Accessibility

Siempre considerar:

- contraste adecuado;
- navegación con teclado;
- focus states;
- labels accesibles;
- aria attributes cuando aplique;
- botones con área táctil suficiente.

---

# Data Visualization

No agregar gráficas porque "un dashboard necesita gráficas".

Solo usar gráficas cuando respondan una pregunta.

Preferir:

- líneas;
- barras;
- donut solo en casos simples.

Evitar:

- 3D charts;
- gauges innecesarios;
- gráficas decorativas.

---

# Dashboard Pages

Cuando se solicite un sistema administrativo completo, considerar:

```text
Dashboard
Lugares
Ciudades
Mapa
Modelos 3D
Comercios
Usuarios
Solicitudes
Contenido
Configuración
```

pero crear únicamente las secciones necesarias.

---

# Context Awareness

Antes de diseñar:

1. inspeccionar el proyecto;
2. identificar colores existentes;
3. revisar componentes;
4. revisar tipografía;
5. revisar navegación;
6. reutilizar patrones existentes.

No rediseñar toda la aplicación salvo que se solicite.

---

# Implementation Rules

Antes de escribir código:

1. Explica brevemente qué vas a modificar.
2. Identifica componentes reutilizables.
3. Evita duplicación.
4. Mantén componentes pequeños.
5. Mantén consistencia visual.
6. Revisa primero el diseño actual.
7. Conserva funcionalidades existentes.
8. No reemplaces librerías o arquitectura sin necesidad.
9. No hagas cambios destructivos sin autorización.
10. No agregues dependencias solo por estética.

Después de implementar:

1. Verifica responsive.
2. Revisa estados hover/focus.
3. Revisa dark/light si aplica.
4. Ejecuta build.
5. Corrige errores introducidos.
6. Resume cambios.
7. Indica archivos modificados.

---

# NicaExplorer-Specific Rules

Para el panel administrativo de NicaExplorer:

- Conservar la identidad oscura + verde/turquesa.
- No convertir el dashboard en una plantilla Bootstrap.
- Evitar estética gaming.
- Evitar exceso de elementos turísticos decorativos dentro de áreas administrativas.
- La información turística debe sentirse integrada, no decorativa.
- El mapa debe ser protagonista solo cuando el contexto lo requiera.
- El visor 3D debe representarse de forma sobria.
- Comercios, monumentos, ciudades y modelos 3D deben compartir una arquitectura visual coherente.
- Mantener consistencia con el resto del ecosistema NicaExplorer.
- Reutilizar tokens, colores, componentes y patrones existentes cuando sea posible.
- Si existe panel actual, mejorarlo de forma incremental antes de reemplazarlo completamente.

---

# Preferred NicaExplorer Admin Structure

Una posible estructura visual, solo si encaja con las funciones reales:

```text
NicaExplorer Admin
│
├── Dashboard
│
├── Turismo
│   ├── Lugares
│   ├── Ciudades
│   ├── Monumentos
│   └── Modelos 3D
│
├── Ecosistema local
│   ├── Comercios
│   ├── Restaurantes
│   └── Solicitudes
│
├── Experiencia
│   ├── Mapa
│   ├── Contenido
│   └── Recomendaciones
│
├── Usuarios
│
└── Sistema
    └── Configuración
```

No implementar esta estructura automáticamente si no coincide con el proyecto real.

---

# Design Review Checklist

Antes de terminar, verificar:

- ¿Parece un Bootstrap genérico?
- ¿Hay demasiadas cards?
- ¿Hay demasiados bordes?
- ¿Hay demasiados colores?
- ¿Hay demasiadas sombras?
- ¿Hay información redundante?
- ¿La jerarquía visual es clara?
- ¿Se entiende cuál es la acción principal?
- ¿La navegación es coherente?
- ¿El espaciado es consistente?
- ¿Los componentes parecen pertenecer al mismo sistema?
- ¿La interfaz parece diseñada por una persona y no generada automáticamente?
- ¿Se siente realmente como NicaExplorer?
- ¿Se mantuvieron las funciones existentes?
- ¿Se evitó añadir decoración innecesaria?
- ¿El resultado funciona bien en resoluciones comunes?

Si alguna respuesta es problemática, refinar antes de finalizar.

---

# Agent Workflow

Cuando recibas una tarea de diseño o implementación de dashboard:

1. Lee este skill completo.
2. Inspecciona la implementación actual.
3. Identifica la arquitectura y stack reales.
4. Revisa componentes reutilizables.
5. Revisa colores, tipografía y tokens actuales.
6. Define brevemente el enfoque visual.
7. Explica qué archivos/componentes vas a tocar.
8. Implementa de forma incremental.
9. No rompas funciones existentes.
10. Ejecuta el build.
11. Corrige errores introducidos.
12. Revisa el resultado visual.
13. Refina cualquier elemento que parezca genérico o generado por IA.
14. Resume los cambios.

---

# Example Agent Instruction

Cuando el usuario solicite rediseñar el dashboard, interpretar la tarea aproximadamente así:

```text
Usa este skill como guía principal de diseño.

Analiza primero el diseño y la arquitectura existentes.

Quiero diseñar o refinar el dashboard administrativo de NicaExplorer
con un estilo moderno, refinado, minimalista y profesional.

No quiero:
- Bootstrap genérico.
- Cards por todas partes.
- Exceso de gradientes.
- Glassmorphism.
- Apariencia generada por IA.
- Estética gaming.
- Componentes sin jerarquía.

Quiero una interfaz que parezca diseñada deliberadamente por
un equipo profesional de producto.

Conserva las funcionalidades existentes.
Reutiliza componentes, tokens y patrones existentes cuando sea posible.

Primero analiza y define brevemente el sistema visual.
Después implementa.

Al finalizar:
- ejecuta el build;
- corrige errores introducidos;
- revisa responsive;
- informa qué archivos modificaste;
- resume las decisiones visuales principales.
```

---

# Final Principle

No busques que la interfaz se vea "impresionante".

Busca que se vea:

- simple;
- intencional;
- refinada;
- rápida;
- coherente;
- profesional;
- agradable de usar;
- propia de NicaExplorer.

Una buena interfaz administrativa debe desaparecer detrás del trabajo del usuario.
