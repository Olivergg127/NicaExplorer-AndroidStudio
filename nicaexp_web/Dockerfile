# Backend NicaExplorer (CodeIgniter 4) para despliegue en Render (Docker).
FROM php:8.3-apache

ENV APACHE_DOCUMENT_ROOT=/var/www/html/public

# Extensiones requeridas por CI4 y por Firestore con transporte REST (sin grpc).
RUN apt-get update && apt-get install -y --no-install-recommends \
        libicu-dev libzip-dev libonig-dev zlib1g-dev unzip git \
    && docker-php-ext-install -j"$(nproc)" intl mbstring zip \
    && a2enmod rewrite \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY --from=composer:2 /usr/bin/composer /usr/bin/composer

WORKDIR /var/www/html

# Dependencias primero para aprovechar la caché de capas.
COPY composer.json composer.lock ./
RUN composer install --no-dev --no-interaction --prefer-dist --optimize-autoloader \
        --ignore-platform-req=ext-grpc

COPY . .

RUN rm -rf tests .git \
    && mkdir -p writable/cache writable/logs writable/session writable/debugbar writable/uploads \
    && chown -R www-data:www-data writable \
    && sed -ri "s!/var/www/html!${APACHE_DOCUMENT_ROOT}!g" /etc/apache2/sites-available/*.conf \
    && sed -ri "s!AllowOverride None!AllowOverride All!g" /etc/apache2/apache2.conf \
    && sed -i 's/\r$//' docker/entrypoint.sh docker/write-env.php \
    && chmod +x docker/entrypoint.sh

# Render inyecta PORT; el entrypoint ajusta Apache a ese puerto.
EXPOSE 8080
CMD ["/var/www/html/docker/entrypoint.sh"]
