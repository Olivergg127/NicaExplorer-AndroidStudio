<?php

namespace App\Commands;

use App\Libraries\FirebaseFactory;
use CodeIgniter\CLI\BaseCommand;
use CodeIgniter\CLI\CLI;
use Throwable;

/**
 * Reescribe el host de las URLs de imágenes de Firestore para que apunten a
 * Nica.publicBaseUrl (útil al cambiar de IP LAN a localhost, o viceversa).
 *
 * Uso:
 *   php spark nica:rebase-imagenes
 */
class RebaseImagenes extends BaseCommand
{
    protected $group       = 'NicaExplorer';
    protected $name        = 'nica:rebase-imagenes';
    protected $description = 'Reescribe el host de las URLs de imágenes según Nica.publicBaseUrl.';
    protected $usage       = 'nica:rebase-imagenes';

    private string $base = '';

    public function run(array $params)
    {
        $this->base = rtrim((string) config('Nica')->publicBaseUrl, '/');
        if ($this->base === '') {
            CLI::error('Define Nica.publicBaseUrl en .env');

            return EXIT_ERROR;
        }

        try {
            $db = FirebaseFactory::instance()->database();
            $updated = 0;

            // Documentos con un campo de imagen único.
            foreach ([
                'ciudades' => ['imagenUrl'],
                'lugares'  => ['imagenUrl'],
                'rutas'    => ['imagenUrl'],
                'comercios' => ['imagenUrl'],
            ] as $collection => $fields) {
                foreach ($db->collection($collection)->documents() as $doc) {
                    $data   = $doc->data();
                    $change = [];

                    foreach ($fields as $field) {
                        $current = $data[$field] ?? null;
                        $next    = $this->rebase($current);
                        if ($next !== $current) {
                            $change[$field] = $next;
                        }
                    }

                    // Galerías (arreglos de URLs).
                    if (isset($data['galeria']) && is_array($data['galeria'])) {
                        $galeria = array_map(fn ($url) => $this->rebase($url), $data['galeria']);
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

            CLI::write("URLs reescritas hacia {$this->base} en {$updated} documentos.", 'green');

            return EXIT_SUCCESS;
        } catch (Throwable $e) {
            CLI::error('Error: ' . $e->getMessage());

            return EXIT_ERROR;
        }
    }

    private function rebase(?string $url): ?string
    {
        if ($url === null || $url === '') {
            return $url;
        }

        $parts = parse_url($url);
        if ($parts === false || ! isset($parts['path'])) {
            return $url;
        }

        if (! str_starts_with($parts['path'], '/uploads/')) {
            return $url;
        }

        return $this->base . $parts['path'];
    }
}
