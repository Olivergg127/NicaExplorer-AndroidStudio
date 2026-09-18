<?php

namespace Config;

use CodeIgniter\Config\BaseConfig;

/**
 * Configuración del backend NicaExplorer.
 *
 * Los valores se leen desde el archivo .env (no versionado) usando el
 * prefijo "Nica.", por ejemplo: Nica.projectId = nica-explore
 */
class Nica extends BaseConfig
{
    /** Proyecto de Firebase/Firestore compartido con la app Android. */
    public string $projectId = 'nica-explore';

    /**
     * Ruta a un JSON de cuenta de servicio.
     * Vacío = usa Application Default Credentials (gcloud ADC).
     */
    public string $credentialsFile = '';

    /** Transporte de Firestore: 'rest' (sin extensión grpc) o 'grpc'. */
    public string $firestoreTransport = 'rest';

    /**
     * URL base absoluta para las imágenes subidas (debe ser accesible desde el
     * móvil). Ej.: http://192.168.123.39:8080 . Si está vacío, se usa el host
     * de la petición.
     */
    public string $publicBaseUrl = '';

    /** API key exigida por los endpoints /api/v1/* (header X-API-KEY). */
    public string $apiKey = '';

    /** Credenciales del panel web. */
    public string $adminUser = 'admin';

    public string $adminPassword = '';
}
