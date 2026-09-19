<?php

namespace App\Commands;

use App\Libraries\FirebaseFactory;
use App\Libraries\ImageStorage;
use CodeIgniter\CLI\BaseCommand;
use CodeIgniter\CLI\CLI;
use Throwable;

/**
 * Migra las imágenes locales (public/uploads) a Firebase Storage y reescribe
 * en Firestore las URLs que apuntaban a /uploads/.
 *
 * Uso:
 *   php spark nica:storage-migrate
 *
 * Requiere Nica.storageBucket configurado. No borra los archivos locales.
 */
class StorageMigrate extends BaseCommand
{
    protected $group       = 'NicaExplorer';
    protected $name        = 'nica:storage-migrate';
    protected $description = 'Sube public/uploads a Firebase Storage y reescribe las URLs en Firestore.';
    protected $usage       = 'nica:storage-migrate';

    /** @var array<string, string> nombre de archivo => URL en Storage */
    private array $urls = [];

    public function run(array $params)
    {
        $storage = new ImageStorage();

        if (! $storage->enabled()) {
            CLI::error('Define Nica.storageBucket antes de migrar (ej. nica-explore.firebasestorage.app).');

            return EXIT_ERROR;
        }

        $uploadsDir = FCPATH . 'uploads';
        if (! is_dir($uploadsDir)) {
            CLI::error('No existe el directorio ' . $uploadsDir);

            return EXIT_ERROR;
        }

        try {
            $uploaded = $this->uploadLocalFiles($storage, $uploadsDir);
            $updated  = $this->rewriteFirestore();

            CLI::write("Subidas a Storage: {$uploaded}. Documentos actualizados: {$updated}.", 'green');

            return EXIT_SUCCESS;
        } catch (Throwable $e) {
            CLI::error('Error: ' . $e->getMessage());

            return EXIT_ERROR;
        }
    }

    /**
     * Sube cada imagen de public/uploads y guarda su nueva URL por nombre.
     */
    private function uploadLocalFiles(ImageStorage $storage, string $uploadsDir): int
    {
        $count = 0;

        foreach (glob($uploadsDir . DIRECTORY_SEPARATOR . '*.{jpg,jpeg,png,webp,gif}', GLOB_BRACE) ?: [] as $path) {
            $name = basename($path);

            try {
                $this->urls[$name] = $storage->upload($path, $name);
                $count++;
            } catch (Throwable $e) {
                CLI::write("Aviso: no se pudo subir {$name}: " . $e->getMessage(), 'yellow');
            }
        }

        return $count;
    }

    /**
     * Reescribe en Firestore las URLs que apuntan a /uploads/.
     */
    private function rewriteFirestore(): int
    {
        $db      = FirebaseFactory::instance()->database();
        $updated = 0;

        foreach (['ciudades', 'lugares', 'rutas', 'comercios'] as $collection) {
            foreach ($db->collection($collection)->documents() as $doc) {
                $data   = $doc->data();
                $change = [];

                $current = $data['imagenUrl'] ?? null;
                $next    = $this->migrateUrl($current);
                if ($next !== $current) {
                    $change['imagenUrl'] = $next;
                }

                if (isset($data['galeria']) && is_array($data['galeria'])) {
                    $galeria = array_map(fn ($url) => $this->migrateUrl($url), $data['galeria']);
                    if ($galeria !== $data['galeria']) {
                        $change['galeria'] = array_values($galeria);
                    }
                }

                if ($change !== []) {
                    $db->collection($collection)->document($doc->id())->set($change, ['merge' => true]);
                    $updated++;
                }
            }
        }

        return $updated;
    }

    private function migrateUrl(?string $url): ?string
    {
        if ($url === null || $url === '') {
            return $url;
        }

        $parts = parse_url($url);
        if ($parts === false || ! isset($parts['path']) || ! str_starts_with($parts['path'], '/uploads/')) {
            return $url;
        }

        $name = basename($parts['path']);

        return $this->urls[$name] ?? $url;
    }
}
