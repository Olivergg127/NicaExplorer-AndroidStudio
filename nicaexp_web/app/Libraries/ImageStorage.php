<?php

namespace App\Libraries;

use Config\Nica;
use Google\Cloud\Storage\Bucket;
use RuntimeException;

/**
 * Almacenamiento de imágenes del panel.
 *
 * Prioridad de proveedores:
 *  1. GitHub (githubRepo + githubToken) — repositorio público servido por raw.
 *  2. Firebase Storage (storageBucket).
 *  3. Modo local (public/uploads), gestionado por el llamador.
 */
class ImageStorage
{
    private Nica $config;

    private FirebaseFactory $firebase;

    public function __construct(?Nica $config = null, ?FirebaseFactory $firebase = null)
    {
        $this->config   = $config ?? config('Nica');
        $this->firebase = $firebase ?? FirebaseFactory::instance();
    }

    public function enabled(): bool
    {
        return $this->githubEnabled() || $this->firebaseEnabled();
    }

    public function provider(): string
    {
        if ($this->githubEnabled()) {
            return 'github';
        }

        if ($this->firebaseEnabled()) {
            return 'firebase';
        }

        return 'local';
    }

    /**
     * Sube un archivo local y devuelve su URL pública.
     *
     * @param string      $sourcePath Ruta absoluta del archivo de origen.
     * @param string      $name       Nombre destino dentro de la carpeta de assets.
     * @param string|null $mime       MIME del archivo; se detecta si es null.
     */
    public function upload(string $sourcePath, string $name, ?string $mime = null): string
    {
        if (! is_file($sourcePath)) {
            throw new RuntimeException('No existe el archivo a subir: ' . $sourcePath);
        }

        if ($this->githubEnabled()) {
            return $this->uploadToGitHub($sourcePath, $name);
        }

        if ($this->firebaseEnabled()) {
            return $this->uploadToFirebase($sourcePath, $name, $mime);
        }

        throw new RuntimeException('No hay almacenamiento de imágenes configurado.');
    }

    /**
     * Sube un archivo del directorio local public/uploads y devuelve su nueva
     * URL; null si el archivo ya no existe en disco.
     */
    public function uploadFromUploads(string $filename, string $uploadsDir): ?string
    {
        $path = rtrim($uploadsDir, '/\\') . DIRECTORY_SEPARATOR . $filename;
        if (! is_file($path)) {
            return null;
        }

        return $this->upload($path, $filename);
    }

    private function githubEnabled(): bool
    {
        return trim($this->config->githubRepo) !== '' && trim($this->config->githubToken) !== '';
    }

    private function firebaseEnabled(): bool
    {
        return trim($this->config->storageBucket) !== '';
    }

    private function uploadToGitHub(string $sourcePath, string $name): string
    {
        $contents = file_get_contents($sourcePath);
        if ($contents === false) {
            throw new RuntimeException('No se pudo leer el archivo a subir.');
        }

        $path    = trim($this->config->githubPath !== '' ? $this->config->githubPath : 'uploads', '/') . '/' . ltrim($name, '/');
        $branch  = $this->config->githubBranch !== '' ? $this->config->githubBranch : 'main';
        $payload = [
            'message' => 'Subir ' . basename($path),
            'content' => base64_encode($contents),
            'branch'  => $branch,
        ];

        $sha = $this->githubFileSha($path);
        if ($sha !== null) {
            $payload['sha'] = $sha;
        }

        $response = $this->githubRequest('PUT', $path, ['json' => $payload]);
        $status   = $response->getStatusCode();

        if ($status >= 300) {
            throw new RuntimeException('GitHub rechazó la subida (' . $status . '): ' . $response->getBody());
        }

        return sprintf(
            'https://raw.githubusercontent.com/%s/%s/%s',
            $this->config->githubRepo,
            $branch,
            $path
        );
    }

    private function githubFileSha(string $path): ?string
    {
        $response = $this->githubRequest('GET', $path, []);
        if ($response->getStatusCode() !== 200) {
            return null;
        }

        $data = json_decode((string) $response->getBody(), true);

        return is_array($data) && isset($data['sha']) ? (string) $data['sha'] : null;
    }

    /**
     * @param array<string, mixed> $options
     */
    private function githubRequest(string $method, string $path, array $options): \CodeIgniter\HTTP\ResponseInterface
    {
        $options['headers'] = [
            'Authorization'        => 'Bearer ' . $this->config->githubToken,
            'Accept'               => 'application/vnd.github+json',
            'User-Agent'           => 'NicaExplorer-Backend',
            'X-GitHub-Api-Version' => '2022-11-28',
        ];
        $options['http_errors'] = false;
        $options['timeout']     = 60;

        return \Config\Services::curlrequest()->request(
            $method,
            'https://api.github.com/repos/' . $this->config->githubRepo . '/contents/' . $path,
            $options
        );
    }

    private function uploadToFirebase(string $sourcePath, string $name, ?string $mime = null): string
    {
        $objectName = 'uploads/' . ltrim($name, '/');
        $token      = bin2hex(random_bytes(16));
        $bucketName = $this->bucket()->name();

        $handle = fopen($sourcePath, 'rb');
        if ($handle === false) {
            throw new RuntimeException('No se pudo abrir el archivo a subir.');
        }

        try {
            $object = $this->bucket()->upload($handle, [
                'name'     => $objectName,
                'metadata' => [
                    'contentType' => $mime ?? (mime_content_type($sourcePath) ?: 'application/octet-stream'),
                ],
            ]);

            // Token que habilita la descarga pública sin exponer el bucket.
            $object->update(['metadata' => ['firebaseStorageDownloadTokens' => $token]]);
        } finally {
            if (is_resource($handle)) {
                fclose($handle);
            }
        }

        return sprintf(
            'https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media&token=%s',
            rawurlencode($bucketName),
            rawurlencode($objectName),
            $token
        );
    }

    private function bucket(): Bucket
    {
        return $this->firebase->bucket();
    }
}
