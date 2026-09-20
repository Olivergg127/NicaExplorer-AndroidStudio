<?php

/**
 * Genera el archivo .env de CodeIgniter 4 a partir de variables de entorno.
 *
 * Se ejecuta al arrancar el contenedor. Importante: CI4 busca las claves de
 * configuración usando el nombre corto de la clase en minúsculas, por lo que
 * las claves se escriben como `nica.*` (en Linux, `Nica.*` NO se leería).
 */

/**
 * @param string $name
 * @param string $default
 */
function nica_env(string $name, string $default = ''): string
{
    $value = getenv($name);

    return ($value === false || $value === '') ? $default : (string) $value;
}

// URL pública del servicio en Render (o APP_BASE_URL si se define).
$baseUrl = rtrim(nica_env('APP_BASE_URL', nica_env('RENDER_EXTERNAL_URL')), '/');

// Cuenta de servicio montada como Secret File; si no existe, se usa ADC.
$credentials = nica_prepare_credentials(nica_env('nica_credentialsFile', '/etc/secrets/firebase.json'));

/**
 * Los Secret Files de Render se montan con permisos que www-data no puede
 * leer. Se copian a writable/ y se ceden al usuario de Apache.
 */
function nica_prepare_credentials(string $path): string
{
    if ($path === '' || ! is_file($path)) {
        return '';
    }

    $target = '/var/www/html/writable/firebase.json';

    if ($path !== $target) {
        if (! @copy($path, $target)) {
            return '';
        }

        @chmod($target, 0640);
        @chown($target, 'www-data');
        @chgrp($target, 'www-data');
    }

    return is_file($target) ? $target : '';
}

$config = [
    'CI_ENVIRONMENT'                => nica_env('CI_ENVIRONMENT', 'production'),
    'app.baseURL'                   => $baseUrl !== '' ? $baseUrl . '/' : '',
    'app.indexPage'                 => nica_env('app_indexPage'),
    'app.forceGlobalSecureRequests' => nica_env('app_forceGlobalSecureRequests', 'false'),
    'encryption.key'                => nica_env('encryption_key'),
    'session.driver'                => nica_env('session_driver', 'CodeIgniter\Session\Handlers\FileHandler'),
    'nica.projectId'                => nica_env('nica_projectId', 'nica-explore'),
    'nica.credentialsFile'          => is_file($credentials) ? $credentials : '',
    'nica.firestoreTransport'       => nica_env('nica_firestoreTransport', 'rest'),
    'nica.storageBucket'            => nica_env('nica_storageBucket'),
    'nica.githubRepo'               => nica_env('nica_githubRepo'),
    'nica.githubToken'              => nica_env('nica_githubToken'),
    'nica.githubBranch'             => nica_env('nica_githubBranch', 'main'),
    'nica.githubPath'               => nica_env('nica_githubPath', 'uploads'),
    'nica.publicBaseUrl'            => nica_env('nica_publicBaseUrl', $baseUrl),
    'nica.apiKey'                   => nica_env('nica_apiKey'),
    'nica.webApiKey'                => nica_env('nica_webApiKey'),
    'nica.adminUser'                => nica_env('nica_adminUser', 'admin'),
    'nica.adminPassword'            => nica_env('nica_adminPassword'),
];

$lines = [];
foreach ($config as $key => $value) {
    $value = str_replace(['\\', "'"], ['\\\\', "\\'"], (string) $value);
    $lines[] = $key . " = '" . $value . "'";
}

file_put_contents('/var/www/html/.env', implode("\n", $lines) . "\n");
