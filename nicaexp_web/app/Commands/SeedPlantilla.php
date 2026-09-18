<?php

namespace App\Commands;

use App\Libraries\FirebaseFactory;
use CodeIgniter\CLI\BaseCommand;
use CodeIgniter\CLI\CLI;
use Google\Cloud\Firestore\FieldValue;
use Throwable;

/**
 * Crea la estructura completa de una ciudad plantilla en Firestore.
 *
 * Uso:
 *   php spark nica:seed-plantilla
 *
 * Deja el documento de ciudad con portada, carrusel (galeria), historia y
 * coordenadas; crea su ruta turística; y enlaza las imágenes copiándolas al
 * directorio público del backend (public/uploads).
 */
class SeedPlantilla extends BaseCommand
{
    protected $group       = 'NicaExplorer';
    protected $name        = 'nica:seed-plantilla';
    protected $description = 'Crea la estructura completa de la ciudad plantilla (Juigalpa) en Firestore.';
    protected $usage       = 'nica:seed-plantilla';

    public function run(array $params)
    {
        $cityId = 'juigalpa';
        $base   = rtrim((string) config('Nica')->publicBaseUrl, '/');
        if ($base === '') {
            $base = 'http://localhost:8080';
        }

        try {
            $images = $this->copyImages();
        } catch (Throwable $e) {
            CLI::error('No se pudieron copiar las imágenes: ' . $e->getMessage());

            return EXIT_ERROR;
        }

        $url = static fn (string $file): string => $base . '/uploads/' . $file;

        $portada = isset($images['portada']) ? $url($images['portada']) : null;
        $galeria = [];
        foreach (['galeria_1', 'galeria_2', 'galeria_3'] as $key) {
            if (isset($images[$key])) {
                $galeria[] = $url($images[$key]);
            }
        }

        try {
            $db = FirebaseFactory::instance()->database();

            // 1) Documento de la ciudad (plantilla).
            $db->collection('ciudades')->document($cityId)->set([
                'nombre'        => 'Juigalpa',
                'lema'          => 'Corazón de la cultura chontaleña',
                'descripcion'   => 'Ciudad ganadera y cultural del centro de Nicaragua, capital del departamento de Chontales.',
                'historia'      => 'Juigalpa es la capital del departamento de Chontales. Su nombre proviene de "Xiuatlpan" y su historia está ligada a la ganadería, la arqueología y la vida cultural del centro del país. En su Parque Central se concentran monumentos que honran a la madre juigalpina, la tradición ganadera y el legado precolombino de la región.',
                'departamento'  => 'Chontales',
                'imagenUrl'     => $portada,
                'galeria'       => $galeria,
                'imagenKey'     => 'juigalpa',
                'latitud'       => 12.1069,
                'longitud'      => -85.3667,
                'gradientStart' => 0xFF7B2D8E,
                'gradientEnd'   => 0xFF4A1A5C,
                'monumentCount' => 3,
                'orden'         => 1,
                'activo'        => true,
                'actualizadoEn' => FieldValue::serverTimestamp(),
            ], ['merge' => true]);

            // 2) Ruta turística de la ciudad.
            $db->collection('rutas')->document('ruta_cultural_juigalpa')->set([
                'cityId'           => $cityId,
                'nombre'           => 'Ruta cultural de Juigalpa',
                'descripcion'      => 'Un recorrido por el patrimonio de Juigalpa que combina lugares con distinta afluencia estimada y una parada en un negocio local.',
                'duracionEstimada' => '2–3 horas',
                'notaDuracion'     => 'Duración orientativa del prototipo; no usa GPS ni información en tiempo real.',
                'objetivos'        => [
                    'Distribuir las visitas entre lugares con distinta afluencia estimada.',
                    'Promover el patrimonio local de Juigalpa.',
                    'Conectar a los turistas con negocios locales de Juigalpa.',
                    'Fomentar prácticas de turismo responsable.',
                ],
                'paradas' => [
                    'lugar:homenaje_madre_juigalpina',
                    'lugar:estatua_museo_juigalpa',
                    'lugar:toro_chontaleno',
                    'comercio:Restaurantes-1',
                ],
                'imagenUrl' => $portada,
                'orden'     => 1,
                'activo'    => true,
            ], ['merge' => true]);

            // 3) Lugares de la ciudad: activo/orden e imagen del backend.
            $lugares = [
                'homenaje_madre_juigalpina' => ['orden' => 1, 'img' => 'galeria_1'],
                'estatua_museo_juigalpa'    => ['orden' => 2, 'img' => 'galeria_3'],
                'toro_chontaleno'           => ['orden' => 3, 'img' => 'galeria_2'],
            ];
            foreach ($lugares as $id => $data) {
                $payload = ['activo' => true, 'orden' => $data['orden']];
                if (isset($images[$data['img']])) {
                    $payload['imagenUrl'] = $url($images[$data['img']]);
                }
                $db->collection('lugares')->document($id)->set($payload, ['merge' => true]);
            }

            CLI::write('Estructura de la ciudad plantilla creada:', 'green');
            CLI::write("  ciudades/{$cityId}  (portada + galeria de " . count($galeria) . ' imágenes)');
            CLI::write('  rutas/ruta_cultural_juigalpa  (4 paradas)');
            CLI::write('  lugares: activo/orden/imagenUrl actualizados');

            return EXIT_SUCCESS;
        } catch (Throwable $e) {
            CLI::error('Error al escribir en Firestore: ' . $e->getMessage());

            return EXIT_ERROR;
        }
    }

    /**
     * Copia las imágenes locales de la app al directorio público del backend.
     *
     * @return array<string, string> clave => nombre de archivo
     */
    private function copyImages(): array
    {
        $sourceDir = realpath(ROOTPATH . '../app/src/main/res/drawable');
        if ($sourceDir === false) {
            throw new \RuntimeException('No se encontró el directorio de drawables de la app: ' . ROOTPATH . '../app/src/main/res/drawable');
        }

        $destinationDir = FCPATH . 'uploads';
        if (! is_dir($destinationDir) && ! mkdir($destinationDir, 0755, true) && ! is_dir($destinationDir)) {
            throw new \RuntimeException('No se pudo crear ' . $destinationDir);
        }

        $map = [
            'portada'   => 'juigalpa.jpg',
            'galeria_1' => 'homenajealamadrejuigalpina.jpg',
            'galeria_2' => 'torochontaleno.jpeg',
            'galeria_3' => 'museojuigalpa.jpg',
        ];

        $result = [];
        foreach ($map as $key => $fileName) {
            $origin = $sourceDir . DIRECTORY_SEPARATOR . $fileName;
            if (! is_file($origin)) {
                CLI::write("Aviso: no existe {$fileName}, se omite.", 'yellow');

                continue;
            }

            $extension       = pathinfo($fileName, PATHINFO_EXTENSION);
            $destinationName = 'ciudad_juigalpa_' . $key . '.' . $extension;
            copy($origin, $destinationDir . DIRECTORY_SEPARATOR . $destinationName);
            $result[$key] = $destinationName;
        }

        return $result;
    }
}
