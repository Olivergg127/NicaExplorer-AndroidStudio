#!/bin/sh
set -e

# Genera el .env de CI4 a partir de las variables de entorno del contenedor.
php /var/www/html/docker/write-env.php

# Apache debe escuchar en el puerto que asigna Render (PORT).
PORT="${PORT:-80}"
sed -ri "s/Listen 80$/Listen ${PORT}/" /etc/apache2/ports.conf
sed -ri "s/:80>/:${PORT}>/" /etc/apache2/sites-available/000-default.conf

exec apache2-foreground
