<?php

namespace App\Commands;

use App\Libraries\FirebaseFactory;
use App\Libraries\ImageStorage;
use CodeIgniter\CLI\BaseCommand;
use CodeIgniter\CLI\CLI;
use Throwable;

/**
 * Copia las imágenes locales de la app al backend y las enlaza en Firestore.
 *
 * Uso:
 *   php spark nica:seed-imagenes
 *
 * Funciona dinámicamente para todas las ciudades, lugares y comercios:
 *  - ciudades: copia `imagenKey` y arma `galeria` (portada + imágenes de sus lugares).
 *  - lugares:  copia `imagenKey` y asigna `imagenUrl`.
 *  - comercios: resuelve la imagen por nombre y asigna `imagenUrl`.
 */
class SeedImagenes extends BaseCommand
{
    protected $group       = 'NicaExplorer';
    protected $name        = 'nica:seed-imagenes';
    protected $description = 'Copia las imágenes de la app al backend y las enlaza en Firestore.';
    protected $usage       = 'nica:seed-imagenes';

    private string $sourceDir = '';
    private string $destDir = '';
    private string $baseUrl = '';

    private ?ImageStorage $storage = null;

    /** @var array<string, string> nombre de archivo => URL publicada en Storage */
    private array $published = [];

    public function run(array $params)
    {
        $source = realpath(ROOTPATH . '../app/src/main/res/drawable');
        if ($source === false) {
            CLI::error('No se encontró el directorio de drawables de la app.');

            return EXIT_ERROR;
        }
        $this->sourceDir = $source;
        $this->destDir   = FCPATH . 'uploads';
        $this->storage   = new ImageStorage();
        if (! is_dir($this->destDir) && ! mkdir($this->destDir, 0755, true) && ! is_dir($this->destDir)) {
            CLI::error('No se pudo crear ' . $this->destDir);

            return EXIT_ERROR;
        }

        $base = rtrim((string) config('Nica')->publicBaseUrl, '/');
        $this->baseUrl = $base !== '' ? $base : 'http://localhost:8080';

        try {
            $db = FirebaseFactory::instance()->database();

            // 1) Ciudades: portada + galería (portada + imágenes de sus lugares).
            $cityCount = 0;
            foreach ($db->collection('ciudades')->documents() as $city) {
                $data = $city->data();
                $key  = trim((string) ($data['imagenKey'] ?? $city->id()));

                $update  = [];
                $galeria = [];
                $portada = null;

                $file = $this->copyForKey($key);
                if ($file !== null) {
                    $portada = $this->url($file);
                    $update['imagenUrl'] = $portada;
                    $galeria[] = $portada;
                }

                foreach ($db->collection('lugares')->where('cityId', '=', $city->id())->documents() as $lugar) {
                    $lugarUrl = $this->ensureLugarImage($lugar);
                    if ($lugarUrl !== null && ! in_array($lugarUrl, $galeria, true)) {
                        $galeria[] = $lugarUrl;
                    }
                }

                if ($galeria !== []) {
                    $update['galeria'] = array_values(array_slice($galeria, 0, 6));
                }

                if ($update !== []) {
                    $db->collection('ciudades')->document($city->id())->set($update, ['merge' => true]);
                    $cityCount++;
                }
            }

            // 2) Lugares: imagen por `imagenKey`.
            $lugarCount = 0;
            foreach ($db->collection('lugares')->documents() as $lugar) {
                if ($this->ensureLugarImage($lugar) !== null) {
                    $lugarCount++;
                }
            }

            // 3) Comercios: imagen resuelta por nombre.
            $comercioCount = 0;
            foreach ($db->collection('comercios')->documents() as $comercio) {
                $key = $this->comercioKey((string) ($comercio->data()['nombre'] ?? ''));
                if ($key === null) {
                    continue;
                }

                $file = $this->copyForKey($key);
                if ($file === null) {
                    continue;
                }

                $db->collection('comercios')->document($comercio->id())
                    ->set(['imagenUrl' => $this->url($file)], ['merge' => true]);
                $comercioCount++;
            }

            CLI::write("Imágenes enlazadas -> ciudades: {$cityCount}, lugares: {$lugarCount}, comercios: {$comercioCount}", 'green');

            return EXIT_SUCCESS;
        } catch (Throwable $e) {
            CLI::error('Error al escribir en Firestore: ' . $e->getMessage());

            return EXIT_ERROR;
        }
    }

    private function ensureLugarImage(object $snapshot): ?string
    {
        $data = $snapshot->data();
        $key  = trim((string) ($data['imagenKey'] ?? ''));

        if ($key === '') {
            $existing = trim((string) ($data['imagenUrl'] ?? ''));

            return $existing !== '' ? $existing : null;
        }

        $file = $this->copyForKey($key);
        if ($file === null) {
            $existing = trim((string) ($data['imagenUrl'] ?? ''));

            return $existing !== '' ? $existing : null;
        }

        $url = $this->url($file);
        if (($data['imagenUrl'] ?? '') !== $url) {
            FirebaseFactory::instance()->database()
                ->collection('lugares')->document($snapshot->id())
                ->set(['imagenUrl' => $url], ['merge' => true]);
        }

        return $url;
    }

    private function comercioKey(string $nombre): ?string
    {
        $normalized = strtolower((string) preg_replace('/[^a-z0-9]/i', '', $nombre));

        return match (true) {
            str_contains($normalized, 'amerri') => 'amerripizza',
            str_contains($normalized, 'coffee') => 'coffee_break',
            str_contains($normalized, 'choza')  => 'mi_choza',
            default => null,
        };
    }

    /**
     * Copia un drawable por su clave (nombre de archivo sin extensión).
     */
    private function copyForKey(string $key): ?string
    {
        $key = strtolower(trim($key));
        if ($key === '') {
            return null;
        }

        foreach (['jpg', 'jpeg', 'png', 'webp'] as $extension) {
            $origin = $this->sourceDir . DIRECTORY_SEPARATOR . $key . '.' . $extension;
            if (is_file($origin)) {
                $file = $key . '.' . $extension;

                if ($this->storage !== null && $this->storage->enabled()) {
                    $this->published[$file] = $this->storage->upload($origin, $file);
                } else {
                    copy($origin, $this->destDir . DIRECTORY_SEPARATOR . $file);
                }

                return $file;
            }
        }

        CLI::write("Aviso: sin imagen local para '{$key}'", 'yellow');

        return null;
    }

    private function url(string $file): string
    {
        return $this->published[$file] ?? $this->baseUrl . '/uploads/' . $file;
    }
}
