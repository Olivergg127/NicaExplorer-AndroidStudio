<?php

namespace App\Controllers\Api;

use App\Controllers\BaseController;
use App\Libraries\FirestoreRepository;
use App\Libraries\ResourceManager;
use CodeIgniter\HTTP\ResponseInterface;

/**
 * Base común de los controladores REST.
 */
abstract class BaseApiController extends BaseController
{
    protected function repository(string $resource): ?FirestoreRepository
    {
        return ResourceManager::repository($resource);
    }

    /**
     * @param mixed $data
     */
    protected function json($data, int $status = 200): ResponseInterface
    {
        return $this->response->setStatusCode($status)->setJSON($data);
    }

    protected function notFound(string $message): ResponseInterface
    {
        return $this->json(['error' => 'no_encontrado', 'message' => $message], 404);
    }

    protected function invalid(array $errors): ResponseInterface
    {
        return $this->json(['error' => 'validacion', 'errors' => array_values($errors)], 422);
    }

    /**
     * Acepta JSON (cuerpo) o form-urlencoded.
     *
     * @return array<string, mixed>
     */
    protected function payload(): array
    {
        $json = $this->request->getJSON(true);
        if (is_array($json)) {
            unset($json['csrf_test_name']);

            return $json;
        }

        $post = $this->request->getPost();
        unset($post['csrf_test_name']);

        return is_array($post) ? $post : [];
    }
}
