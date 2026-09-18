<?php

namespace App\Controllers\Api;

use App\Libraries\FirebaseFactory;
use CodeIgniter\HTTP\ResponseInterface;
use Throwable;

/**
 * Comprobación de salud y conectividad con Firestore.
 */
class Health extends BaseApiController
{
    public function index(): ResponseInterface
    {
        try {
            $database = FirebaseFactory::instance()->database();

            return $this->json([
                'status'    => 'ok',
                'project'   => config('Nica')->projectId,
                'transport' => config('Nica')->firestoreTransport,
                'ciudades'  => (int) $database->collection('ciudades')->count(),
                'time'      => date(DATE_ATOM),
            ]);
        } catch (Throwable $e) {
            return $this->json([
                'status'  => 'error',
                'message' => $e->getMessage(),
            ], 503);
        }
    }
}
