<?php

namespace App\Controllers\Api;

use App\Libraries\CatalogSignal;
use CodeIgniter\HTTP\ResponseInterface;

/**
 * CRUD REST genérico sobre las colecciones declaradas en Config\NicaResources.
 *
 * Rutas:
 *   GET    /api/v1/{resource}?search=&limit=
 *   POST   /api/v1/{resource}
 *   GET    /api/v1/{resource}/{id}
 *   PUT    /api/v1/{resource}/{id}
 *   PATCH  /api/v1/{resource}/{id}
 *   DELETE /api/v1/{resource}/{id}
 */
class Resources extends BaseApiController
{
    public function index(string $resource): ResponseInterface
    {
        $repository = $this->repository($resource);
        if ($repository === null) {
            return $this->notFound("La colección '{$resource}' no existe.");
        }

        $search = trim((string) ($this->request->getGet('search') ?? ''));
        $limit  = (int) ($this->request->getGet('limit') ?? 500);
        $limit  = max(1, min($limit, 1000));

        $rows = $repository->all($search, $limit);

        return $this->json([
            'resource' => $resource,
            'count'    => count($rows),
            'data'     => $rows,
        ]);
    }

    public function show(string $resource, string $id): ResponseInterface
    {
        $repository = $this->repository($resource);
        if ($repository === null) {
            return $this->notFound("La colección '{$resource}' no existe.");
        }

        $document = $repository->find($id);
        if ($document === null) {
            return $this->notFound("No existe '{$id}' en '{$resource}'.");
        }

        return $this->json(['data' => $document]);
    }

    public function create(string $resource): ResponseInterface
    {
        $repository = $this->repository($resource);
        if ($repository === null) {
            return $this->notFound("La colección '{$resource}' no existe.");
        }

        $payload = $this->payload();
        $errors  = $repository->validate($payload, false);
        if ($errors !== []) {
            return $this->invalid($errors);
        }

        $created = $repository->create($payload);
        CatalogSignal::touch();

        return $this->json(['data' => $repository->find($created['id'])], 201);
    }

    public function update(string $resource, string $id): ResponseInterface
    {
        $repository = $this->repository($resource);
        if ($repository === null) {
            return $this->notFound("La colección '{$resource}' no existe.");
        }

        $payload = $this->payload();
        $errors  = $repository->validate($payload, true);
        if ($errors !== []) {
            return $this->invalid($errors);
        }

        if (! $repository->update($id, $payload)) {
            return $this->notFound("No existe '{$id}' en '{$resource}'.");
        }

        CatalogSignal::touch();

        return $this->json(['data' => $repository->find($id)]);
    }

    public function delete(string $resource, string $id): ResponseInterface
    {
        $repository = $this->repository($resource);
        if ($repository === null) {
            return $this->notFound("La colección '{$resource}' no existe.");
        }

        if (! $repository->delete($id)) {
            return $this->notFound("No existe '{$id}' en '{$resource}'.");
        }

        CatalogSignal::touch();

        return $this->json(['deleted' => true, 'id' => $id]);
    }
}
