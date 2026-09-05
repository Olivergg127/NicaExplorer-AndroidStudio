# 🎨 Paleta de Colores "Guardabarranco"

**Proyecto:** NicaExplorer · App de turismo AR de Nicaragua
**Versión:** 1.0 — Agosto 2026
**Referencia técnica:** `app/src/main/java/com/lospuntoycoma/nicaexplorer/ui/theme/Color.kt`

---

## Concepto

La paleta está inspirada en el **guardabarranco**, ave nacional de Nicaragua:

- El **verde turquesa** de su cuerpo → lagunas, reservas naturales y paisajes
- El **azul** de su corona → cielo y lagos (Xolotlán y Cocibolca)
- El **terracota** de su pecho rufo → cerámica de San Juan de Oriente y artesanía nicaragüense

Los fondos claros tienen un tinte cálido de **arena volcánica del Pacífico**.

---

## 1. Colores principales (roles de UI)

| Rol | Nombre comercial | HEX | RGB | Uso en la app |
|---|---|---|---|---|
| **Primario** | Turquesa Guardabarranco | `#00A08A` | 0 · 160 · 138 | Botones principales, elementos activos, navegación |
| **Secundario** | Azul Corona | `#4098D7` | 64 · 152 · 215 | Botones secundarios, enlaces, iconografía |
| **Terciario** | Terracota Cerámica | `#C1553B` | 193 · 85 · 59 | Acentos cálidos, destacados, asistente IA |
| **Gradiente de marca** | "Vuelo" | `#00A08A → #4098D7` | — | Splash, login, encabezados, placeholders |

---

## 2. Familias tonales

Cada color principal tiene 4 variantes para estados, superficies y modo oscuro.

### 🟢 Turquesa Guardabarranco (primario)

| Variante | HEX | RGB | Uso |
|---|---|---|---|
| Base | `#00A08A` | 0 · 160 · 138 | Color primario de la app |
| Oscura | `#00695C` | 0 · 105 · 92 | Contenedores primarios en modo oscuro |
| Clara | `#7FD9CF` | 127 · 217 · 207 | Texto sobre contenedor oscuro |
| Superficie | `#E0F4F1` | 224 · 244 · 241 | Fondos tintados, chips |

### 🔵 Azul Corona (secundario)

| Variante | HEX | RGB | Uso |
|---|---|---|---|
| Base | `#4098D7` | 64 · 152 · 215 | Color secundario |
| Oscura | `#175E92` | 23 · 94 · 146 | Contenedores secundarios en modo oscuro |
| Clara | `#90C8EE` | 144 · 200 · 238 | Texto sobre contenedor oscuro |
| Superficie | `#E7F2FB` | 231 · 242 · 251 | Fondos tintados |

### 🟠 Terracota Cerámica (terciario)

| Variante | HEX | RGB | Uso |
|---|---|---|---|
| Base | `#C1553B` | 193 · 85 · 59 | Terciario, botón del asistente IA |
| Oscura | `#8A3A26` | 138 · 58 · 38 | Contenedores terciarios en modo oscuro |
| Clara | `#EDA48F` | 237 · 164 · 143 | Texto sobre contenedor oscuro |
| Superficie | `#FAEBE5` | 250 · 235 · 229 | Fondos tintados |

---

## 3. Neutrales

| Uso | Modo claro | HEX | Modo oscuro | HEX |
|---|---|---|---|---|
| Fondo de pantalla | Arena cálida | `#FDFAF4` | Carbón | `#121212` |
| Tarjetas / superficies | Blanco cálido | `#FFFDF8` | Gris superficie | `#1E1E1E` |
| Variante de superficie | Beige suave | `#F5EFE6` | Gris variante | `#2D2D2D` |
| Texto principal | Negro suave | `#1C1B1F` | Blanco hueso | `#F5F0F5` |
| Texto secundario | — | — | — | — |
| Texto atenuado | Gris | `#939393` | — | — |

---

## 4. Colores funcionales

| Función | Nombre | HEX | Notas |
|---|---|---|---|
| WhatsApp | Verde WhatsApp NicaExplorer | `#2E7D32` | **Exclusivo** para botones/chips de WhatsApp |
| WhatsApp superficie | — | `#E8F5E9` | Fondo del chip "WhatsApp disponible" |
| WhatsApp claro | — | `#81C784` | Degradado del botón (con `#43A047`) |
| Éxito | Verde éxito | `#43A047` | Confirmaciones, formularios |
| Error | Rojo error | `#E53935` | Errores, validaciones |
| Dorado (reserva) | Oro Sol Nica | `#FFB300` | Acento futuro: sol, sacuanjoche |
| Dorado claro (reserva) | — | `#FFE082` | — |

---

## 5. Gradientes

| Nombre | Composición | Uso |
|---|---|---|
| **Vuelo** (marca) | `#00A08A → #4098D7` horizontal | Splash, login, headers, placeholders de imagen |
| WhatsApp | `#2E7D32 → #43A047` horizontal | Botón "Contactar por WhatsApp" |
| Asistente IA | Secundario → Terciario (`#4098D7 → #C1553B`) | Botón "Hablar con el asistente IA" |
| Realidad aumentada | Primario → Secundario (`#00A08A → #4098D7`) | Botón "Ver en realidad aumentada" |

---

## 6. Gradientes por ciudad (catálogo)

Cada ciudad conserva una identidad propia en sus pantallas de monumentos:

| Ciudad | Gradiente | HEX |
|---|---|---|
| León | Dorado colonial → marrón | `#D4A017 → #8B4513` |
| Juigalpa | Púrpura chontaleño | `#7B2D8E → #4A1A5C` |
| Managua | Verde capital | `#00897B → #005B4F` |

---

## 7. Reglas de uso

1. **Contraste:** sobre turquesa, azul o terracota el texto siempre va **blanco** (`#FFFFFF`). Contraste verificado ≥ 3:1 en los tres colores base.
2. **Verde WhatsApp:** no usar para ningún elemento que no sea WhatsApp.
3. **Blancos puros:** evitar `#FFFFFF` en fondos de pantalla; usar siempre los blancos cálidos de la sección Neutrales.
4. **Dorado:** reservado; no combinar con terracota en el mismo componente.
5. **Modo oscuro:** usar las variantes oscuras/clara de cada familia, nunca la base sobre fondo oscuro para textos pequeños.
6. **Gradiente de marca:** siempre horizontal (izquierda→derecha), sin ángulos personalizados.

---

*Documento generado para handoff a diseño. Cualquier cambio de paleta debe reflejarse primero en `Color.kt` y luego actualizarse aquí.*
