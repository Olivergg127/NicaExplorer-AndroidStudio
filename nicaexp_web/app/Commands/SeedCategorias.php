<?php

namespace App\Commands;

use App\Libraries\FirebaseFactory;
use CodeIgniter\CLI\BaseCommand;
use CodeIgniter\CLI\CLI;
use Google\Cloud\Firestore\FieldValue;
use Throwable;

/**
 * Recopila las categorías usadas en los lugares y crea el catálogo
 * `categorias_lugares` en Firestore.
 *
 * Uso:
 *   php spark nica:seed-categorias
 *
 * Es idempotente: no borra categorías existentes ni sobrescribe `orden`/`activo`
 * de las ya creadas; solo agrega las que falten y actualiza su `nombre`.
 */
class SeedCategorias extends BaseCommand
{
    protected $group       = 'NicaExplorer';
    protected $name        = 'nica:seed-categorias';
    protected $description = 'Crea el catálogo de categorías de lugares a partir de las categorías ya usadas.';
    protected $usage       = 'nica:seed-categorias';

    public function run(array $params)
    {
        try {
            $db = FirebaseFactory::instance()->database();

            $names = [];
            foreach ($db->collection('lugares')->documents() as $lugar) {
                $categoria = trim((string) ($lugar->data()['categoria'] ?? ''));
                if ($categoria !== '') {
                    $names[$categoria] = true;
                }
            }

            if ($names === []) {
                CLI::write('No se encontraron categorías en la colección "lugares".', 'yellow');

                return EXIT_SUCCESS;
            }

            ksort($names, SORT_NATURAL | SORT_FLAG_CASE);

            $created = 0;
            $updated = 0;
            $order   = 0;

            foreach (array_keys($names) as $categoria) {
                $order++;
                $slug = $this->slug($categoria);
                if ($slug === '') {
                    CLI::write("Aviso: categoría sin slug válido '{$categoria}', se omite.", 'yellow');

                    continue;
                }

                $document = $db->collection('categorias_lugares')->document($slug);
                $exists   = $document->snapshot()->exists();

                $payload = [
                    'nombre'        => $categoria,
                    'actualizadoEn' => FieldValue::serverTimestamp(),
                ];

                if (! $exists) {
                    $payload['orden']  = $order;
                    $payload['activo'] = true;
                }

                $document->set($payload, ['merge' => true]);

                if ($exists) {
                    $updated++;
                } else {
                    $created++;
                }

                CLI::write(sprintf('  %s -> %s', $categoria, $slug), 'green');
            }

            CLI::write("Categorías procesadas: {$created} nuevas, {$updated} existentes.", 'green');

            return EXIT_SUCCESS;
        } catch (Throwable $e) {
            CLI::error('Error al escribir en Firestore: ' . $e->getMessage());

            return EXIT_ERROR;
        }
    }

    private function slug(string $value): string
    {
        $value = trim($value);
        $value = strtr($value, [
            'á' => 'a', 'é' => 'e', 'í' => 'i', 'ó' => 'o', 'ú' => 'u', 'ü' => 'u', 'ñ' => 'n',
            'Á' => 'a', 'É' => 'e', 'Í' => 'i', 'Ó' => 'o', 'Ú' => 'u', 'Ü' => 'u', 'Ñ' => 'n',
        ]);
        $value = strtolower($value);
        $value = (string) preg_replace('/[^a-z0-9]+/', '_', $value);

        return trim($value, '_');
    }
}
