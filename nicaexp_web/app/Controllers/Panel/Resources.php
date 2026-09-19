<?php

namespace App\Controllers\Panel;

use App\Controllers\BaseController;
use App\Libraries\CatalogSignal;
use App\Libraries\FirestoreRepository;
use App\Libraries\ImageStorage;
use App\Libraries\ResourceManager;
use CodeIgniter\Exceptions\PageNotFoundException;
use CodeIgniter\HTTP\ResponseInterface;
use Throwable;

/**
 * CRUD del panel web: una sola vista con DataTables + modales para toda colección.
 */
class Resources extends BaseController
{
    public function index(string $resource)
    {
        $definition = ResourceManager::definition($resource);
        if ($definition === null) {
            throw PageNotFoundException::forPageNotFound("La colección '{$resource}' no existe.");
        }

        $options = $this->referenceOptions($definition);

        return view('panel/resource', [
            'title'      => $definition['label'],
            'active'     => $resource,
            'resource'   => $resource,
            'definition' => $definition,
            'fields'     => $this->formFields($definition),
            'columns'    => $this->columns($definition),
            'refs'       => $options['stops'],
            'references' => $options['references'],
        ]);
    }

    public function data(string $resource): ResponseInterface
    {
        $repository = $this->repository($resource);

        try {
            $rows = $repository->tableRows();
        } catch (Throwable $e) {
            return $this->response->setStatusCode(500)->setJSON([
                'data'  => [],
                'error' => $e->getMessage(),
            ]);
        }

        return $this->response->setJSON(['data' => $rows]);
    }

    public function save(string $resource): ResponseInterface
    {
        $repository = $this->repository($resource);
        $payload    = $this->request->getPost();
        unset($payload['csrf_test_name']);

        $currentId = trim((string) ($payload['_current_id'] ?? ''));
        unset($payload['_current_id']);

        if ($currentId === '') {
            $errors = $repository->validate($payload, false);
            if ($errors !== []) {
                return $this->response->setStatusCode(422)->setJSON(['error' => 'validacion', 'errors' => $errors]);
            }

            $created = $repository->create($payload);
            CatalogSignal::touch();

            return $this->response->setStatusCode(201)->setJSON([
                'ok'      => true,
                'id'      => $created['id'],
                'message' => 'Registro creado correctamente.',
            ]);
        }

        $errors = $repository->validate($payload, true);
        if ($errors !== []) {
            return $this->response->setStatusCode(422)->setJSON(['error' => 'validacion', 'errors' => $errors]);
        }

        if (! $repository->update($currentId, $payload)) {
            return $this->response->setStatusCode(404)->setJSON(['error' => 'No existe el registro.']);
        }

        CatalogSignal::touch();

        return $this->response->setJSON([
            'ok'      => true,
            'id'      => $currentId,
            'message' => 'Registro actualizado correctamente.',
        ]);
    }

    public function delete(string $resource): ResponseInterface
    {
        $repository = $this->repository($resource);
        $id         = trim((string) $this->request->getPost('id'));

        if ($id === '' || ! $repository->delete($id)) {
            return $this->response->setStatusCode(404)->setJSON(['error' => 'No existe el registro.']);
        }

        CatalogSignal::touch();

        return $this->response->setJSON(['ok' => true, 'id' => $id, 'message' => 'Registro eliminado.']);
    }

    /**
     * Sube una imagen y devuelve su URL pública (para los campos de tipo "image").
     */
    public function upload(): ResponseInterface
    {
        $file = $this->request->getFile('image');

        if ($file === null || ! $file->isValid() || $file->hasMoved()) {
            return $this->response->setStatusCode(400)->setJSON(['error' => 'No se recibió ninguna imagen válida.']);
        }

        $allowed = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
        if (! in_array($file->getMimeType(), $allowed, true)) {
            return $this->response->setStatusCode(422)->setJSON([
                'error' => 'Formato no permitido. Usa JPG, PNG, WEBP o GIF.',
            ]);
        }

        if ($file->getSize() > 5 * 1024 * 1024) {
            return $this->response->setStatusCode(422)->setJSON([
                'error' => 'La imagen supera el máximo de 5 MB.',
            ]);
        }

        $name    = $file->getRandomName();
        $storage = new ImageStorage();

        if ($storage->enabled()) {
            try {
                $url = $storage->upload($file->getTempName(), $name, $file->getMimeType());
            } catch (Throwable $e) {
                return $this->response->setStatusCode(500)->setJSON([
                    'error' => 'No se pudo subir la imagen a Firebase Storage: ' . $e->getMessage(),
                ]);
            }

            return $this->response->setJSON(['ok' => true, 'url' => $url]);
        }

        $destination = FCPATH . 'uploads';
        if (! is_dir($destination) && ! mkdir($destination, 0755, true) && ! is_dir($destination)) {
            return $this->response->setStatusCode(500)->setJSON(['error' => 'No se pudo crear el directorio de subidas.']);
        }

        $file->move($destination, $name);

        return $this->response->setJSON([
            'ok'  => true,
            'url' => $this->absoluteUploadUrl($name),
        ]);
    }

    /**
     * Construye una URL absoluta para la imagen subida, de modo que el móvil
     * pueda cargarla aunque el panel se haya abierto desde localhost.
     */
    private function absoluteUploadUrl(string $name): string
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

    private function repository(string $resource): FirestoreRepository
    {
        $repository = ResourceManager::repository($resource);
        if ($repository === null) {
            throw PageNotFoundException::forPageNotFound("La colección '{$resource}' no existe.");
        }

        return $repository;
    }

    /**
     * Opciones para los campos de tipo "stops" (paradas) y "reference".
     *
     * @param array<string, mixed> $definition
     *
     * @return array{
     *     stops: array<string, list<array{id: string, nombre: string}>>,
     *     references: array<string, list<array{value: string, label: string}>>
     * }
     */
    private function referenceOptions(array $definition): array
    {
        $result = [
            'stops'      => ['lugar' => [], 'comercio' => []],
            'references' => [],
        ];

        $needsStops = false;
        $references = [];

        foreach ($definition['fields'] as $name => $field) {
            $type = $field['type'] ?? 'string';

            if ($type === 'stops') {
                $needsStops = true;
            }

            if ($type === 'reference') {
                $references[$name] = $field;
            }
        }

        if ($needsStops) {
            try {
                foreach (ResourceManager::repository('lugares')->tableRows('', 1000) as $row) {
                    $result['stops']['lugar'][] = [
                        'id'     => (string) $row['id'],
                        'nombre' => trim((string) ($row['nombre'] ?? $row['id'])),
                    ];
                }
                foreach (ResourceManager::repository('comercios')->tableRows('', 1000) as $row) {
                    $result['stops']['comercio'][] = [
                        'id'     => (string) $row['id'],
                        'nombre' => trim((string) ($row['nombre'] ?? $row['id'])),
                    ];
                }
            } catch (Throwable) {
                // Si falla, el editor de paradas mostrará selects vacíos.
            }
        }

        foreach ($references as $name => $field) {
            $collection = (string) ($field['collection'] ?? '');
            if ($collection === '' || ! ResourceManager::exists($collection)) {
                continue;
            }

            $valueField = (string) ($field['value_field'] ?? 'id');
            $labelField = (string) ($field['label_field'] ?? 'nombre');

            try {
                $options = [];
                foreach (ResourceManager::repository($collection)->tableRows('', 1000) as $row) {
                    $value = $valueField === 'id'
                        ? (string) $row['id']
                        : trim((string) ($row[$valueField] ?? ''));

                    if ($value === '') {
                        continue;
                    }

                    $label = $labelField === 'id'
                        ? (string) $row['id']
                        : trim((string) ($row[$labelField] ?? ''));

                    $options[] = [
                        'value' => $value,
                        'label' => $label !== '' ? $label : $value,
                    ];
                }

                usort($options, static fn (array $a, array $b): int => strcasecmp($a['label'], $b['label']));
                $result['references'][$name] = $options;
            } catch (Throwable) {
                $result['references'][$name] = [];
            }
        }

        return $result;
    }

    /**
     * Definición de campos enviada al frontend para pintar el DataTable.
     *
     * @param array<string, mixed> $definition
     *
     * @return list<array<string, mixed>>
     */
    private function columns(array $definition): array
    {
        $columns = [];

        foreach ($definition['fields'] as $name => $field) {
            if (empty($field['list'])) {
                continue;
            }

            $columns[] = [
                'data'   => $name,
                'label'  => $field['label'] ?? $name,
                'type'   => $field['type'] ?? 'string',
                'options' => $field['options'] ?? [],
            ];
        }

        return $columns;
    }

    /**
     * Campos editables en el modal (los de tipo "server" no se muestran).
     *
     * @param array<string, mixed> $definition
     *
     * @return list<array<string, mixed>>
     */
    private function formFields(array $definition): array
    {
        $fields = [];

        foreach ($definition['fields'] as $name => $field) {
            if (! empty($field['server'])) {
                continue;
            }

            $fields[] = [
                'name'     => $name,
                'label'    => $field['label'] ?? $name,
                'type'     => $field['type'] ?? 'string',
                'required' => ! empty($field['required']),
                'options'  => $field['options'] ?? [],
                'default'  => $field['default'] ?? null,
            ];
        }

        return $fields;
    }
}
