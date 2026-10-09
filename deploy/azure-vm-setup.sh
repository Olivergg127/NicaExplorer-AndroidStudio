#!/usr/bin/env bash
#
# azure-vm-setup.sh — Aprovisiona una VM Ubuntu de Azure para NicaExplorer (Hackathon).
#
# Instala y deja funcionando:
#   - Apache 2 + PHP 8.3 (con las extensiones que usa CodeIgniter 4)
#   - Composer
#   - MariaDB (base de datos dentro del servidor — entregable 4)
#   - El backend nicaexp_web servido en el puerto 80, con docroot en public/
#
# Cubre los entregables:
#   #2 Configuración del servidor (SSH)      -> lo verás al entrar por SSH
#   #3 Red y puertos (80/443/22)             -> se abren en Azure (NSG), no aquí
#   #4 Entorno y base de datos               -> PHP + MariaDB instalados aquí
#   #5 Conexión a la nube                    -> la app apunta a la IP pública de la VM
#
# USO (en la VM, con sudo):
#   sudo bash azure-vm-setup.sh
#
# Después: copia la carpeta nicaexp_web a /var/www/nicaexp y configura su .env.
# Si el archivo llega con saltos de línea de Windows, ejecuta antes:
#   sed -i 's/\r$//' azure-vm-setup.sh
#
set -euo pipefail

APP_DIR="/var/www/nicaexp"          # carpeta del backend (nicaexp_web)
PHP_VER="8.3"
DB_NAME="nicaexp"
DB_USER="nicaexp"
DB_PASS="cambia_esta_clave"          # <-- cámbiala

log() { echo -e "\n\033[1;36m==> $*\033[0m"; }

# ---------------------------------------------------------------------------
log "1/7  Paquetes base"
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y software-properties-common ca-certificates lsb-release gnupg \
    curl git unzip sed

# ---------------------------------------------------------------------------
log "2/7  PHP $PHP_VER (repositorio ondrej/php) + extensiones de CodeIgniter 4"
add-apt-repository -y ppa:ondrej/php
apt-get update
apt-get install -y \
    php${PHP_VER} php${PHP_VER}-cli php${PHP_VER}-common \
    php${PHP_VER}-intl php${PHP_VER}-mbstring php${PHP_VER}-zip \
    php${PHP_VER}-curl php${PHP_VER}-bcmath php${PHP_VER}-xml \
    php${PHP_VER}-gd php${PHP_VER}-mysql libapache2-mod-php${PHP_VER}

# Subidas de imágenes (el panel/app permiten hasta 5 MB)
cat > /etc/php/${PHP_VER}/apache2/conf.d/99-nica.ini <<'INI'
upload_max_filesize = 8M
post_max_size = 10M
memory_limit = 256M
INI

# ---------------------------------------------------------------------------
log "3/7  Apache 2 (rewrite + headers)"
apt-get install -y apache2
a2enmod rewrite headers
a2dissite 000-default >/dev/null 2>&1 || true

# VirtualHost apuntando al public/ de CodeIgniter 4
cat > /etc/apache2/sites-available/nicaexp.conf <<CONF
<VirtualHost *:80>
    ServerAdmin admin@nicaexp.local
    DocumentRoot ${APP_DIR}/public

    <Directory ${APP_DIR}/public>
        Options -Indexes +FollowSymLinks
        AllowOverride All
        Require all granted
    </Directory>

    # (Opcional) si más adelante pones HTTPS con Let's Encrypt,
    # deja este bloque y añade el .conf de certbot.

    ErrorLog \${APACHE_LOG_DIR}/nicaexp-error.log
    CustomLog \${APACHE_LOG_DIR}/nicaexp-access.log combined
</VirtualHost>
CONF
a2ensite nicaexp >/dev/null

# ---------------------------------------------------------------------------
log "4/7  Composer"
if ! command -v composer >/dev/null 2>&1; then
    curl -sS https://getcomposer.org/installer | php -- \
        --install-dir=/usr/local/bin --filename=composer
fi

# ---------------------------------------------------------------------------
log "5/7  MariaDB (base de datos dentro del servidor — entregable 4)"
apt-get install -y mariadb-server
systemctl enable --now mariadb

# Crea la base y el usuario (idempotente)
mysql <<SQL
CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;
SQL

# ---------------------------------------------------------------------------
log "6/7  Backend (CodeIgniter 4) en ${APP_DIR}"
if [ -d "${APP_DIR}" ]; then
    cd "${APP_DIR}"
    if [ -f composer.json ]; then
        composer install --no-dev --no-interaction --prefer-dist --optimize-autoloader \
            --ignore-platform-req=ext-grpc || true
    fi
    mkdir -p writable/cache writable/logs writable/session writable/uploads writable/debugbar
    chown -R www-data:www-data writable
    chmod -R 775 writable
    echo "   Backend detectado y dependencias instaladas."
else
    echo "   Aún no existe ${APP_DIR}."
    echo "   Copia ahí la carpeta nicaexp_web y vuelve a ejecutar este script"
    echo "   (o ejecuta: composer install --no-dev && chown -R www-data:www-data writable)."
fi

# ---------------------------------------------------------------------------
log "7/7  Reinicio de servicios"
systemctl restart apache2
systemctl restart mariadb

# ---------------------------------------------------------------------------
log "RESUMEN (para la demostración)"
echo "PHP:      $(php -v | head -n1)"
echo "Apache:   $(apache2 -v | head -n1)"
echo "MariaDB:  $(mysql --version)"
echo "Base:     ${DB_NAME} (usuario ${DB_USER})"
echo "IP local: $(hostname -I | awk '{print $1}')   (la IP PÚBLICA se ve en el portal de Azure)"
echo
echo "Recuerda:"
echo "  - Abrir en Azure (NSG): puertos 22 (SSH), 80 (HTTP) y 443 (HTTPS)."
echo "  - Colocar el service account de Firebase y apuntar 'nica.credentialsFile' en el .env."
echo "  - En la app: local.properties -> nica.apiBaseUrl = http://<IP-PUBLICA-AZURE>"
echo "  - Probar:  curl http://localhost/panel/login"
echo
echo "Listo."
