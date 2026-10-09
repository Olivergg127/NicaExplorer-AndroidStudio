# Despliegue en Azure — NicaExplorer (Hackathon)

Guía del despliegue real del **backend** (`nicaexp_web`, CodeIgniter 4) en una **VM Ubuntu
de Azure** y de la app Android apuntando a esa VM. Cubre los entregables de desarrollo.

## Estado actual (2026-10-09)

| Dato | Valor |
|---|---|
| VM | `68.211.72.101` (host `lospuntoycoma`), Ubuntu 24.04 LTS |
| Dominio TLS | `https://68.211.72.101.sslip.io` (Let's Encrypt; HTTP → HTTPS 301) |
| Stack | Apache 2 + PHP 8.3 + MariaDB (`127.0.0.1:3306`) |
| Docroot | `/var/www/nicaexp/public` |
| Base de datos | **Firestore** (REST). MariaDB queda instalada pero no la usa la app |
| Credenciales | `/etc/secrets/firebase.json` (`chmod 600`, dueño `www-data`) |
| APK | `https://68.211.72.101.sslip.io/builds/NicaExplorer.apk` |
| Puertos públicos (NSG) | 22 (SSH), 80 (HTTP), 443 (HTTPS) — nada más |

## 1. Crear la VM (entregable #2)

Portal de Azure → **Create a resource → Virtual machine**:
- **Image:** Ubuntu Server 24.04 LTS.
- **Size:** B1s (o la más pequeña disponible).
- **Authentication:** SSH public key o contraseña.
- **Inbound ports:** SSH (22), HTTP (80), HTTPS (443).
- Anota la **IP pública** (entregable #3).

## 2. Abrir puertos (entregable #3)

En la VM → **Networking → Add inbound port rule**: solo 22, 80 y 443.
**No** exponer 3306 ni otros puertos de servicio. Verificación externa: solo 22/80/443.

## 3. Conectarse por SSH (entregable #2)

```bash
ssh azureuser@68.211.72.101
```

## 4. Ejecutar el script de aprovisionamiento (entregable #4)

Sube `azure-vm-setup.sh` a la VM (por `scp` o copiando/pegando) y:

```bash
sed -i 's/\r$//' azure-vm-setup.sh      # si viene con saltos de línea de Windows
sudo bash azure-vm-setup.sh
```

Instala **Apache + PHP 8.3 + Composer + MariaDB** y deja el vhost apuntando a
`/var/www/nicaexp/public`.

## 5. Subir el backend

```bash
# Desde tu PC:
scp -r nicaexp_web azureuser@68.211.72.101:/tmp/nicaexp_web
# En la VM:
sudo mkdir -p /var/www/nicaexp
sudo cp -r /tmp/nicaexp_web/. /var/www/nicaexp/   # el '.' copia también los archivos ocultos (.gitignore, .dockerignore, .env.example)
cd /var/www/nicaexp
composer install --no-dev --no-interaction --prefer-dist --optimize-autoloader --ignore-platform-req=ext-grpc
cp .env.example .env    # y edítalo (projectId, apiKey, admin, credentialsFile, baseURL)
sudo chown -R www-data:www-data writable
sudo chmod 600 .env      # el .env lleva secretos: no debe ser legible por todos

# Carpeta pública de subidas: la app sube aquí las imágenes si no hay token de GitHub.
sudo mkdir -p public/uploads
sudo chown -R www-data:www-data public/uploads
sudo chmod 775 public/uploads
```

> **Imágenes:** si defines `nica.githubToken` en `.env`, las subidas van al repo
> `nica.githubRepo` (raw.githubusercontent.com). Si queda **vacío**, se guardan en
> `public/uploads` de la propia VM (`ImageStorage` cae al modo local). Si ves el error
> "Could not move file ... public/uploads", es que esta carpeta no pertenece a `www-data`.

### Credenciales de Firestore

Coloca la **cuenta de servicio** (`firebase.json`) en `/etc/secrets/firebase.json` y en
`.env` pon `nica.credentialsFile = /etc/secrets/firebase.json`:

```bash
sudo mkdir -p /etc/secrets
sudo chown www-data:www-data /etc/secrets/firebase.json
sudo chmod 600 /etc/secrets/firebase.json
```

**Nunca** subas ese archivo al repositorio.

## 6. HTTPS (dominio y certificado)

Con `sslip.io` no hace falta DNS propio: `68.211.72.101.sslip.io` resuelve a la IP.

```bash
sudo apt-get install -y certbot python3-certbot-apache
sudo certbot --apache -d 68.211.72.101.sslip.io
```

En `.env`: `app.baseURL = 'https://68.211.72.101.sslip.io/'`.

## 7. Publicar el APK (entregable #1)

```bash
sudo mkdir -p /var/www/nicaexp/public/builds
sudo cp app-debug.apk /var/www/nicaexp/public/builds/NicaExplorer.apk
sudo chown www-data:www-data /var/www/nicaexp/public/builds/NicaExplorer.apk
```

La landing lo enlaza vía `Home::index` → `base_url('builds/NicaExplorer.apk')`.

## 8. Apuntar la app a Azure (entregable #5)

En `local.properties` (no versionado):

```properties
nica.apiBaseUrl=https://68.211.72.101.sslip.io
nica.apiKey=<tu API key del backend>
```

Recompila el APK (`.\gradlew.bat :app:assembleDebug`) → la app ya **no usa localhost** ni
Render, sino la VM de Azure.

## 9. Endurecimiento aplicado (entregable #2)

- `.env` con permisos `600` y `firebase.json` en `600` (dueño `www-data`).
- Apache sin banner de versión: `ServerTokens Prod` y `ServerSignature Off`.
- MariaDB escuchando **solo** en `127.0.0.1` (no expuesta).
- NSG con únicamente 22/80/443.
- Pendiente opcional: **UFW** (hoy inactivo) y SSH solo con clave.

## 10. Verificación (para la demo)

```bash
php -v                     # entorno instalado
mysql --version            # base de datos instalada
systemctl status mariadb   # base de datos funcionando
curl -sk https://68.211.72.101.sslip.io/api/v1/health   # backend responde (con X-API-KEY)
sudo ss -tlnp              # solo 80/443/22 públicos; MariaDB en 127.0.0.1
```

Capturas sugeridas: SSH conectado, reglas de puertos del NSG, `php -v` + MariaDB, la app
apuntando al dominio de Azure y la descarga del APK desde la landing.
