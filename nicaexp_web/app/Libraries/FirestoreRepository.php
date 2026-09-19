<?php

namespace App\Libraries;

use Google\Cloud\Core\Timestamp;
use Google\Cloud\Firestore\FieldValue;
use Google\Cloud\Firestore\FirestoreClient;
use InvalidArgumentException;

/**
 * Repositorio genérico sobre una colección de Firestore.
 *
 * Toda la semántica (campos, tipos, validación, id) proviene de
 * Config\NicaResources, de modo que un mismo repositorio sirve para
 * todas las colecciones administradas por el backend.
 */
class FirestoreRepository
{
    private FirestoreClient $db;

    /** @var array<string, mixed> */
    private array $resource;

    /**
     * @param array<string, mixed> $resource
     */
    public function __construct(FirestoreClient $db, array $resource)
    {
        $this->db       = $db;
        $this->resource = $resource;
    }

    /**
     * @return array<string, mixed>
     */
    public function resource(): array
    {
        return $this->resource;
    }

    /**
     * @return array<string, array<string, mixed>>
     */
    public function fields(): array
    {
        return $this->resource['fields'];
    }

    /**
     * Lista los documentos, con búsqueda simple en memoria (prototipo).
     *
     * @return list<array{id: string, data: array<string, mixed>}>
     */
    public function all(string $search = '', int $limit = 1000): array
    {
        $query = $this->db->collection($this->resource['collection']);

        if (! empty($this->resource['order_by'])) {
            [$field, $direction] = $this->resource['order_by'];
            $query = $query->orderBy($field, strtolower($direction) === 'desc' ? 'DESCENDING' : 'ASCENDING');
        }

        $rows = [];
        foreach ($query->limit($limit)->documents() as $snapshot) {
            if (! $snapshot->exists()) {
                continue;
            }
            $rows[] = ['id' => $snapshot->id(), 'data' => $this->normalizeArray($snapshot->data())];
        }

        if ($search !== '') {
            $needle = mb_strtolower(trim($search));
            $fields = $this->resource['search_fields'] ?? array_keys($this->fields());
            $rows   = array_values(array_filter($rows, static function (array $row) use ($needle, $fields): bool {
                foreach ($fields as $field) {
                    $value = $row['data'][$field] ?? null;
                    if (is_scalar($value) && str_contains(mb_strtolower((string) $value), $needle)) {
                        return true;
                    }
                }

                return false;
            }));
        }

        return $rows;
    }

    /**
     * @return array{id: string, data: array<string, mixed>}|null
     */
    public function find(string $id): ?array
    {
        $snapshot = $this->db->collection($this->resource['collection'])->document($id)->snapshot();

        if (! $snapshot->exists()) {
            return null;
        }

        return ['id' => $snapshot->id(), 'data' => $this->normalizeArray($snapshot->data())];
    }

    public function count(): int
    {
        return (int) $this->db->collection($this->resource['collection'])->count();
    }

    /**
     * @param array<string, mixed> $input
     *
     * @return array{id: string, data: array<string, mixed>}
     */
    public function create(array $input): array
    {
        $data = $this->sanitize($input, false);
        $id   = $this->resolveIdForCreate($input);

        if ($id !== null) {
            $reference = $this->db->collection($this->resource['collection'])->document($id);
            $reference->set($data);

            return ['id' => $id, 'data' => $data];
        }

        $reference = $this->db->collection($this->resource['collection'])->newDocument();
        $reference->set($data);

        return ['id' => $reference->id(), 'data' => $data];
    }

    /**
     * @param array<string, mixed> $input
     */
    public function update(string $id, array $input): bool
    {
        if ($this->find($id) === null) {
            return false;
        }

        $data = $this->sanitize($input, true);
        if ($data === []) {
            return true;
        }

        $this->db->collection($this->resource['collection'])->document($id)->set($data, ['merge' => true]);

        return true;
    }

    public function delete(string $id): bool
    {
        if ($this->find($id) === null) {
            return false;
        }

        $this->db->collection($this->resource['collection'])->document($id)->delete();

        return true;
    }

    /**
     * Valida el payload según la definición de campos.
     *
     * @param array<string, mixed> $input
     *
     * @return list<string> lista de errores (vacía si todo está correcto)
     */
    public function validate(array $input, bool $isUpdate = false): array
    {
        $errors = [];

        foreach ($this->fields() as $name => $field) {
            if (! empty($field['server'])) {
                continue;
            }

            $present = array_key_exists($name, $input);
            $value   = $input[$name] ?? null;

            if (! empty($field['required']) && ! $isUpdate && ! $present) {
                $errors[] = sprintf('El campo "%s" es obligatorio.', $field['label'] ?? $name);

                continue;
            }

            if (! $present) {
                continue;
            }

            if (($field['type'] ?? 'string') === 'enum' && $value !== '' && $value !== null
                && ! in_array((string) $value, $field['options'] ?? [], true)) {
                $errors[] = sprintf('Valor inválido para "%s".', $field['label'] ?? $name);
            }

            if (($field['type'] ?? '') === 'email' && $value !== '' && $value !== null
                && filter_var((string) $value, FILTER_VALIDATE_EMAIL) === false) {
                $errors[] = sprintf('El correo de "%s" no es válido.', $field['label'] ?? $name);
            }
        }

        if ($this->resource['id']['strategy'] === 'manual' && ! $isUpdate) {
            $id = trim((string) ($input['_id'] ?? ''));
            if ($id === '') {
                $errors[] = 'El ID del documento es obligatorio.';
            }
        }

        return $errors;
    }

    /**
     * Filas listas para DataTables (valores escalares / booleanos).
     *
     * @return list<array<string, mixed>>
     */
    public function tableRows(string $search = '', int $limit = 1000): array
    {
        $rows = [];

        foreach ($this->all($search, $limit) as $row) {
            $flat         = ['id' => $row['id']];
            $flat['_acc'] = $row['id'];
            $flat['_raw'] = $row['data'];

            foreach ($this->fields() as $name => $field) {
                $flat[$name] = $this->formatForTable($field, $row['data'][$name] ?? null);
            }

            $rows[] = $flat;
        }

        return $rows;
    }

    private function resolveIdForCreate(array $input): ?string
    {
        $strategy = $this->resource['id']['strategy'] ?? 'auto';

        if ($strategy === 'manual') {
            return trim((string) ($input['_id'] ?? ''));
        }

        if ($strategy === 'field') {
            $field = $this->resource['id']['field'] ?? 'id';

            return trim((string) ($input[$field] ?? ''));
        }

        // Estrategia 'auto': id explícito si viene, o slug del campo indicado.
        $provided = trim((string) ($input['_id'] ?? ''));
        if ($provided !== '') {
            return $provided;
        }

        $slugSource = (string) ($this->resource['id']['slug_source'] ?? '');
        if ($slugSource !== '') {
            $base = $this->slugify((string) ($input[$slugSource] ?? ''));
            if ($base !== '') {
                return $this->uniqueId($base);
            }
        }

        return null;
    }

    /**
     * Genera un id legible a partir de un texto ("Toro Chontaleño" -> "toro_chontaleno").
     */
    private function slugify(string $value): string
    {
        $value = trim($value);
        if ($value === '') {
            return '';
        }

        $ascii = function_exists('iconv')
            ? iconv('UTF-8', 'ASCII//TRANSLIT//IGNORE', $value)
            : $value;

        $ascii = $ascii === false ? $value : $ascii;
        $ascii = strtolower($ascii);
        $ascii = (string) preg_replace('/[^a-z0-9]+/', '_', $ascii);

        return trim($ascii, '_');
    }

    /**
     * Evita colisiones añadiendo un sufijo numérico (_2, _3...).
     */
    private function uniqueId(string $base): string
    {
        $collection = $this->db->collection($this->resource['collection']);
        $candidate  = $base;
        $suffix     = 1;

        while ($collection->document($candidate)->snapshot()->exists()) {
            $suffix++;
            $candidate = $base . '_' . $suffix;
        }

        return $candidate;
    }

    /**
     * @param array<string, mixed> $input
     *
     * @return array<string, mixed>
     */
    private function sanitize(array $input, bool $isUpdate): array
    {
        $data = [];

        foreach ($this->fields() as $name => $field) {
            $type = $field['type'] ?? 'string';

            // Los campos de servidor se calculan aquí, nunca desde la UI.
            if (! empty($field['server'])) {
                if (! $isUpdate && $type === 'timestamp') {
                    $data[$name] = FieldValue::serverTimestamp();
                }

                continue;
            }

            $present = array_key_exists($name, $input);

            if (! $present) {
                if (! $isUpdate && array_key_exists('default', $field)) {
                    $data[$name] = $field['default'];
                }

                continue;
            }

            $data[$name] = $this->cast($type, $input[$name]);
        }

        return $data;
    }

    private function cast(string $type, mixed $value): mixed
    {
        return match ($type) {
            'int'   => (int) $value,
            'float' => (float) str_replace(',', '.', (string) $value),
            'bool'  => filter_var($value, FILTER_VALIDATE_BOOLEAN),
            'list', 'images', 'stringlist', 'stops' => $this->castList($value),
            'timestamp' => $value === '' || $value === null ? null : $value,
            default => is_scalar($value) ? trim((string) $value) : $value,
        };
    }

    /**
     * @return list<string>
     */
    private function castList(mixed $value): array
    {
        if (is_array($value)) {
            return array_values(array_filter(array_map(static fn ($item) => trim((string) $item), $value), static fn ($item) => $item !== ''));
        }

        if (is_string($value)) {
            $lines = preg_split('/\r\n|\r|\n/', $value) ?: [];

            return array_values(array_filter(array_map('trim', $lines), static fn ($item) => $item !== ''));
        }

        return [];
    }

    private function formatForTable(array $field, mixed $value): mixed
    {
        $type = $field['type'] ?? 'string';

        if ($value === null) {
            return $type === 'bool' ? false : '';
        }

        if ($value instanceof Timestamp) {
            return $value->formatAsString();
        }

        return match ($type) {
            'bool' => (bool) $value,
            'list', 'images', 'stringlist', 'stops' => is_array($value) ? implode(', ', array_map('strval', $value)) : (string) $value,
            default => is_scalar($value) ? (string) $value : json_encode($value, JSON_UNESCAPED_UNICODE),
        };
    }

    /**
     * Convierte Timestamps y valores no escalares a algo serializable.
     *
     * @param array<string, mixed> $data
     *
     * @return array<string, mixed>
     */
    private function normalizeArray(array $data): array
    {
        foreach ($data as $key => $value) {
            $data[$key] = $this->normalizeValue($value);
        }

        return $data;
    }

    private function normalizeValue(mixed $value): mixed
    {
        if ($value instanceof Timestamp) {
            return $value->formatAsString();
        }

        if (is_array($value)) {
            return array_map(fn ($item) => $this->normalizeValue($item), $value);
        }

        if (is_object($value) && method_exists($value, 'get')) {
            try {
                $inner = $value->get();
                if ($inner instanceof \DateTimeInterface) {
                    return $inner->format('Y-m-d H:i:s');
                }
            } catch (InvalidArgumentException) {
                // Ignorar objetos que no se pueden convertir.
            }
        }

        return $value;
    }
}
