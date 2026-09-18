<?php

namespace App\Libraries;

use Config\Nica;
use Google\Cloud\Firestore\FirestoreClient;
use Kreait\Firebase\Factory;
use RuntimeException;

/**
 * Construye el cliente de Firestore compartido con la app Android.
 *
 * Credenciales:
 *  - Si Nica.credentialsFile apunta a un JSON, se usa ese service account.
 *  - Si está vacío, se usan las Application Default Credentials (gcloud ADC).
 *
 * Transporte:
 *  - 'rest' por defecto, porque el PHP del entorno no tiene la extensión grpc.
 */
class FirebaseFactory
{
    private static ?FirebaseFactory $instance = null;

    private Nica $config;

    private ?Factory $factory = null;

    private ?FirestoreClient $database = null;

    public function __construct(?Nica $config = null)
    {
        $this->config = $config ?? config('Nica');
    }

    public static function instance(): self
    {
        return self::$instance ??= new self();
    }

    public function factory(): Factory
    {
        if ($this->factory instanceof Factory) {
            return $this->factory;
        }

        $factory = new Factory();

        if ($this->config->projectId !== '') {
            $factory = $factory->withProjectId($this->config->projectId);
        }

        if ($this->config->credentialsFile !== '') {
            if (! is_file($this->config->credentialsFile)) {
                throw new RuntimeException('No se encontró el archivo de credenciales: ' . $this->config->credentialsFile);
            }

            $factory = $factory->withServiceAccount($this->config->credentialsFile);
        }

        return $this->factory = $factory->withFirestoreClientConfig([
            'transport' => $this->config->firestoreTransport !== '' ? $this->config->firestoreTransport : 'rest',
        ]);
    }

    public function database(): FirestoreClient
    {
        return $this->database ??= $this->factory()->createFirestore()->database();
    }
}
