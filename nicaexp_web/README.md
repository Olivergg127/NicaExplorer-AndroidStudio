# NicaExplorer Web Backend

Backend de administración de **NicaExplorer**, construido con **CodeIgniter 4** y
**Cloud Firestore** (la **misma base de datos** que usa la app Android, proyecto
`nica-explore`). No usa MySQL.

Incluye:

- **API REST v1** (JSON) con CRUD para todas las colecciones.
- **Panel web** con tema claro/oscuro propio de **NicaExplorer** (sobre AdminLTE 4/Bootstrap),
  sidebar lateral, tablas **DataTables** y **modales** para crear/editar/eliminar (sin
  vistas separadas por formulario). Incluye switch de tema con preferencia persistente.
- **API key** para la API y **login** para el panel.

## Requisitos

- PHP 8.3 con extensiones: `intl`, `fileinfo`, `curl`, `openssl`, `mbstring`, `zip`.
- Composer 2.
- Credenciales de Google Cloud/Firebase (ver más abajo).

> En este equipo ya quedaron configurados: Composer en
> `%LOCALAPPDATA%\Programs\Composer` (en el PATH de usuario), las extensiones
> `intl`/`fileinfo` y el bundle de CA (`curl.cainfo`/`openssl.cafile`) en el `php.ini`
> de `C:\WebStack\php`. Ver `../DEVELOPMENT.md`.

## Instalación

```powershell
cd nicaexp_web
composer install
Copy-Item .env.example .env
php spark key:generate
```

Edita `.env` y define al menos:

```ini
Nica.projectId        = nica-explore
Nica.apiKey           = <clave para la API>
Nica.adminUser        = admin
Nica.adminPassword    = <clave del panel>
Nica.firestoreTransport = rest
```

## Credenciales de Firebase

El backend necesita acceso a Firestore con privilegios de administrador (las reglas de
seguridad del cliente no aplican al backend). Dos opciones:

1. **Application Default Credentials (ADC)** — recomendado para desarrollo. Deja
   `Nica.credentialsFile` vacío y ejecuta una vez:

   ```powershell
   gcloud auth application-default login
   ```

   El SDK de Google Cloud ya está instalado en este equipo.

2. **Cuenta de servicio** — apunta `Nica.credentialsFile` a un JSON descargado de
   Firebase/Google Cloud. **Nunca** lo subas al repositorio.

### Transporte

`Nica.firestoreTransport = rest` porque este PHP **no** tiene la extensión `grpc`.
`google/cloud-firestore` se instaló con `--ignore-platform-req=ext-grpc`. Si algún día
se instala `grpc`, puede cambiarse a `grpc`.

## Ejecutar

Para que sea accesible desde cualquier dispositivo de la red local (PC + móvil):

```powershell
php spark serve --host 0.0.0.0 --port 8080
```

- Panel (local): <http://localhost:8080/panel>
- API (local): <http://localhost:8080/api/v1>
- Desde otro dispositivo: `http://<IP-DE-TU-PC>:8080` (ej. `http://192.168.123.39:8080`)

Requisitos para el acceso desde el móvil:

1. `app.baseURL` en `.env` debe usar la IP LAN de la PC:
   `app.baseURL = 'http://192.168.123.39:8080/'` (si cambia la IP, actualízala).
2. PC y móvil en la **misma red Wi-Fi/LAN**.
3. Abrir el puerto en el Firewall de Windows (una vez, en PowerShell **como administrador**):

   ```powershell
   netsh advfirewall firewall add rule name="NicaExplorer Backend 8080" dir=in action=allow protocol=TCP localport=8080
   ```

4. En el móvil, abre `http://192.168.123.39:8080/panel` y verifica que carga.

## Imágenes

El panel permite **subir imágenes** para los campos de tipo `image` (por ejemplo, la
imagen de portada de una ciudad o la imagen de un lugar/comercio):

- Los archivos se guardan en `public/uploads/` (ignorado por Git).
- El panel devuelve la URL y la guarda en el campo `imagenUrl` del documento.
- La URL se construye con `base_url()`, por lo que apunta a la IP de `app.baseURL`.

Los campos de imagen se definen en `app/Config/NicaResources.php` con `'type' => 'image'`:
`ciudades.imagenUrl`, `lugares.imagenUrl` y `comercios.imagenUrl`.

## Integración con la app Android

La app Android y este backend comparten **la misma base de Firestore** (`nica-explore`),
así que cualquier alta/edición/borrado del panel se refleja en la app.

- **Datos** (ciudades, lugares, comercios, etc.): la app los lee de Firestore, la misma
  colección que escribe/edita el backend.
- **Imágenes**: la app carga `imagenUrl` con Coil (HTTP) y, si no hay URL o falla,
  usa el drawable local de respaldo (`imagenKey`).
- La app **no** necesita la API key: no consume `/api/v1`; consume Firestore y las
  imágenes servidas por este backend. La API REST queda para integraciones externas.
- Para desarrollo, la app tiene `android:usesCleartextTraffic="true"` porque sirve
  imágenes por HTTP en la red local. En producción, usar HTTPS y quitarlo.


## API REST

Todas las rutas requieren el header `X-API-KEY` o el parámetro `?api_key=`.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/health` | Estado y conectividad con Firestore |
| GET | `/api/v1/{coleccion}` | Listar (`?search=` y `?limit=`) |
| GET | `/api/v1/{coleccion}/{id}` | Ver un documento |
| POST | `/api/v1/{coleccion}` | Crear |
| PUT/PATCH | `/api/v1/{coleccion}/{id}` | Actualizar |
| DELETE | `/api/v1/{coleccion}/{id}` | Eliminar |

Colecciones: `ciudades`, `lugares`, `comercios`, `solicitudes_comercios`, `usuarios`.

Ejemplo:

```powershell
$key = (Select-String 'nicaexp_web\.env' '^Nica.apiKey').Line.Split('=')[1].Trim()
Invoke-RestMethod http://localhost:8080/api/v1/lugares -Headers @{ 'X-API-KEY' = $key }
```

## Panel web

- Login en `/panel/login` con `Nica.adminUser` / `Nica.adminPassword`.
- Dashboard con contadores por colección.
- Cada colección tiene su pantalla con **DataTable** (búsqueda, orden y paginación en
  cliente) y botones **Nuevo / Editar / Eliminar** que abren **modales**.
- Los formularios se generan automáticamente a partir de `Config\NicaResources`.

Para cambiar campos, etiquetas, opciones o columnas visibles, edita únicamente
`app/Config/NicaResources.php`: la API, la tabla y el formulario se adaptan solos.

## Estructura relevante

```text
nicaexp_web/
├── app/
│   ├── Config/
│   │   ├── Nica.php            Configuración (projectId, credenciales, apiKey, admin).
│   │   ├── NicaResources.php   Definición de colecciones y campos.
│   │   ├── Routes.php          Rutas de la API y del panel.
│   │   └── Filters.php         Alias de filtros (apikey, panelauth).
│   ├── Controllers/
│   │   ├── Api/                Health y CRUD REST genérico.
│   │   └── Panel/              Auth, Dashboard y CRUD del panel.
│   ├── Filters/                ApiKeyFilter y PanelAuthFilter.
│   ├── Libraries/
│   │   ├── FirebaseFactory.php      Cliente de Firestore (ADC o service account).
│   │   ├── FirestoreRepository.php  CRUD genérico + validación + formato.
│   │   └── ResourceManager.php      Acceso a repositorios por clave.
│   └── Views/
│       ├── layout/panel.php    Layout del panel (tema oscuro, sidebar y topbar).
│       └── panel/              login, dashboard y resource (DataTable + modales).
└── public/assets/             panel.js (DataTables/AJAX), panel.css (tema claro/oscuro) y theme.js (switch).
```

## Notas de seguridad

- `.env`, `vendor/` y `writable/*` están en `.gitignore`.
- La API key y la clave del panel se guardan solo en `.env`.
- El panel usa sesión de CI4 y protección CSRF en los POST.
- Firestore se consume con credenciales de administrador (ADC/cuenta de servicio):
  no expongas este backend sin API key y con HTTPS en producción.

## Estructura de una ciudad (plantilla)

La colección `ciudades` guarda la identidad y los recursos visuales de la ciudad, y se
relaciona con `lugares`, `comercios` y `rutas`:

- `ciudades/{cityId}`: `nombre`, `lema`, `descripcion`, `historia`, `departamento`,
  **`imagenUrl` (portada)**, **`galeria` (carrusel, una URL por línea)**, `imagenKey`
  (legado), `latitud`, `longitud`, `orden`, `activo`.
- `rutas/{rutaId}`: `cityId`, `nombre`, `descripcion`, `duracionEstimada`, `notaDuracion`,
  `objetivos`, `paradas` (`monumento:<id>` o `comercio:<id>`), `imagenUrl`, `orden`, `activo`.
- `lugares` y `comercios`: añaden `cityId`, `galeria`, `orden`, `activo` y coordenadas.

Para crear la estructura completa de la ciudad plantilla (Juigalpa), con portada, carrusel,
historia y su ruta turística:

```powershell
php spark nica:seed-plantilla
```

El comando copia las imágenes locales de la app a `public/uploads/` y escribe los
documentos en Firestore (usa `Nica.publicBaseUrl` para las URLs).

Para enlazar **todas** las imágenes de la app (ciudades, lugares y comercios) en Firestore:

```powershell
php spark nica:seed-imagenes
```

- **ciudades**: copia la imagen de `imagenKey` como portada y arma `galeria` (portada +
  imágenes de sus lugares).
- **lugares**: copia la imagen de `imagenKey` y asigna `imagenUrl`.
- **comercios**: resuelve la imagen por nombre y asigna `imagenUrl`.

Para crear el catálogo de categorías de lugares a partir de las categorías ya usadas por
los lugares (idempotente; no borra ni sobrescribe categorías existentes):

```powershell
php spark nica:seed-categorias
```

El panel gestiona ese catálogo en **Categorías de lugares** y los formularios de lugares
usan un select tanto para la ciudad (`cityId`) como para la categoría.
