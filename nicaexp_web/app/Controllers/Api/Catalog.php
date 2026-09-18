<?php

namespace App\Controllers\Api;

use App\Libraries\FirebaseFactory;
use CodeIgniter\HTTP\ResponseInterface;
use Throwable;

/**
 * Versión del catálogo: la app la consulta periódicamente para detectar cambios.
 */
class Catalog extends BaseApiController
{
    public function version(): ResponseInterface
    {
        try {
            $snapshot = FirebaseFactory::instance()->database()
                ->collection('meta')->document('catalogo')->snapshot();

            $updatedAt = '';
            if ($snapshot->exists()) {
                $value = $snapshot->get('updatedAt');
                if ($value !== null && method_exists($value, 'formatAsString')) {
                    $updatedAt = $value->formatAsString();
                } elseif (is_string($value)) {
                    $updatedAt = $value;
                }
            }

            return $this->json(['version' => $updatedAt]);
        } catch (Throwable $e) {
            return $this->json(['version' => '', 'error' => $e->getMessage()], 200);
        }
    }
}
