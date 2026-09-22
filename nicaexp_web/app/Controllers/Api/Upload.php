<?php

namespace App\Controllers\Api;

use App\Libraries\FirebaseFactory;
use App\Libraries\ImageStorage;
use CodeIgniter\HTTP\ResponseInterface;
use Throwable;

/**
 * Subida de imágenes para la app Android.
 *
 * A diferencia de Panel\Resources::upload (sesión del panel), este endpoint
 * autentica al usuario con su ID token de Firebase Auth y exige rol
 * COMERCIO/EDITOR/ADMIN. La API key (filtro apikey) sigue siendo obligatoria.
 *
 * Respuesta: { "ok": true, "url": "https://..." }
 */
class Upload extends BaseApiController
{
    private const MAX_BYTES = 5 * 1024 * 1024;

    private const ALLOWED = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

    /** Roles con permiso para usar el almacén de imágenes. */
    private const ROLES = ['COMERCIO', 'EDITOR', 'ADMIN'];

    public function image(): ResponseInterface
    {
        $token = $this->bearerToken();
        if ($token === '') {
            return $this->json([
                'error'   => 'no_autorizado',
                'message' => 'Falta el token de sesión de Firebase.',
            ], 401);
        }

        $uid = $this->verifiedUid($token);
        if ($uid === null) {
            return $this->json([
                'error'   => 'no_autorizado',
                'message' => 'Token de sesión inválido o expirado.',
            ], 401);
        }

        if (! in_array($this->userRole($uid), self::ROLES, true)) {
            return $this->json([
                'error'   => 'prohibido',
                'message' => 'Tu cuenta no tiene permiso para subir imágenes.',
            ], 403);
        }

        $file = $this->request->getFile('image');
        if ($file === null || ! $file->isValid() || $file->hasMoved()) {
            return $this->json(['error' => 'No se recibió ninguna imagen válida.'], 400);
        }

        if (! in_array($file->getMimeType(), self::ALLOWED, true)) {
            return $this->json(['error' => 'Formato no permitido. Usa JPG, PNG, WEBP o GIF.'], 422);
        }

        if ($file->getSize() > self::MAX_BYTES) {
            return $this->json(['error' => 'La imagen supera el máximo de 5 MB.'], 422);
        }

        $name    = $file->getRandomName();
        $storage = new ImageStorage();

        if ($storage->enabled()) {
            try {
                $url = $storage->upload($file->getTempName(), $name, $file->getMimeType());
            } catch (Throwable $e) {
                return $this->json([
                    'error'   => 'almacenamiento',
                    'message' => 'No se pudo subir la imagen: ' . $e->getMessage(),
                ], 500);
            }

            return $this->json(['ok' => true, 'url' => $url]);
        }

        $destination = FCPATH . 'uploads';
        if (! is_dir($destination) && ! mkdir($destination, 0755, true) && ! is_dir($destination)) {
            return $this->json(['error' => 'No se pudo crear el directorio de subidas.'], 500);
        }

        $file->move($destination, $name);

        return $this->json(['ok' => true, 'url' => $this->absoluteUrl($name)]);
    }

    private function bearerToken(): string
    {
        $header = trim($this->request->getHeaderLine('Authorization'));
        if (stripos($header, 'Bearer ') === 0) {
            return trim(substr($header, 7));
        }

        return trim($this->request->getHeaderLine('X-Firebase-Token'));
    }

    private function verifiedUid(string $token): ?string
    {
        try {
            $verified = FirebaseFactory::instance()->factory()->createAuth()->verifyIdToken($token);

            return (string) $verified->claims()->get('sub');
        } catch (Throwable) {
            return null;
        }
    }

    private function userRole(string $uid): string
    {
        try {
            $repository = $this->repository('usuarios');
            $document   = $repository?->find($uid);

            return trim((string) ($document['data']['rol'] ?? ''));
        } catch (Throwable) {
            return '';
        }
    }

    private function absoluteUrl(string $name): string
    {
        $base = rtrim((string) config('Nica')->publicBaseUrl, '/');

        if ($base === '') {
            $uri  = $this->request->getUri();
            $base = $uri->getScheme() . '://' . $uri->getHost();
            if ($uri->getPort() !== null) {
                $base .= ':' . $uri->getPort();
            }
        }

        return $base . '/uploads/' . $name;
    }
}
