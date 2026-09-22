<?php

namespace App\Controllers\Api;

use App\Libraries\FirebaseFactory;
use CodeIgniter\HTTP\ResponseInterface;
use Throwable;

/**
 * Valoraciones públicas (solo lectura) para el ranking de recomendados.
 *
 * Devuelve las valoraciones sin el uid del autor. La app agrega los promedios
 * por ciudad/tipo. La escritura la sigue haciendo la app directamente en
 * Firestore (colección `valoraciones`).
 *
 * Respuesta: { "data": [ { "id": "...", "data": { tipo, refId, cityId, estrellas } } ] }
 */
class Valoraciones extends BaseApiController
{
    public function index(): ResponseInterface
    {
        $limit = (int) ($this->request->getGet('limit') ?? 2000);
        $limit = max(1, min($limit, 5000));

        $rows = [];

        try {
            $database = FirebaseFactory::instance()->database();

            foreach ($database->collection('valoraciones')->limit($limit)->documents() as $document) {
                $data = $document->data();

                $rows[] = [
                    'id'   => $document->id(),
                    'data' => [
                        'tipo'      => (string) ($data['tipo'] ?? ''),
                        'refId'     => (string) ($data['refId'] ?? ''),
                        'cityId'    => (string) ($data['cityId'] ?? ''),
                        'estrellas' => (int) ($data['estrellas'] ?? 0),
                    ],
                ];
            }
        } catch (Throwable $e) {
            return $this->json([
                'error'   => 'lectura',
                'message' => 'No se pudieron leer las valoraciones: ' . $e->getMessage(),
            ], 500);
        }

        return $this->json([
            'resource' => 'valoraciones',
            'count'    => count($rows),
            'data'     => $rows,
        ]);
    }
}
