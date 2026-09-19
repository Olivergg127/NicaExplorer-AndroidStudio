<?php

namespace App\Commands;

use App\Libraries\FirebaseFactory;
use CodeIgniter\CLI\BaseCommand;
use CodeIgniter\CLI\CLI;
use Throwable;

/**
 * Asigna latitud/longitud a las ciudades conocidas para que el mapa principal
 * pueda centrarse en ellas.
 *
 * Uso:
 *   php spark nica:seed-coordenadas-ciudades
 *
 * Es idempotente: solo escribe latitud/longitud (merge) y no toca otros campos.
 */
class SeedCoordenadasCiudades extends BaseCommand
{
    protected $group       = 'NicaExplorer';
    protected $name        = 'nica:seed-coordenadas-ciudades';
    protected $description = 'Asigna coordenadas a las ciudades conocidas.';
    protected $usage       = 'nica:seed-coordenadas-ciudades';

    /** @var array<string, array{0: float, 1: float}> */
    private const COORDENADAS = [
        'juigalpa'  => [12.1069, -85.3667],
        'leon'      => [12.4379, -86.8780],
        'managua'   => [12.1364, -86.2514],
        'matagalpa' => [12.9256, -85.9170],
    ];

    public function run(array $params)
    {
        try {
            $db = FirebaseFactory::instance()->database();
            $actualizadas = 0;

            foreach (self::COORDENADAS as $cityId => [$latitud, $longitud]) {
                $documento = $db->collection('ciudades')->document($cityId);

                if (! $documento->snapshot()->exists()) {
                    CLI::write("Aviso: no existe la ciudad '{$cityId}', se omite.", 'yellow');

                    continue;
                }

                $documento->set([
                    'latitud'  => $latitud,
                    'longitud' => $longitud,
                ], ['merge' => true]);

                CLI::write(sprintf('  %s -> %s, %s', $cityId, $latitud, $longitud), 'green');
                $actualizadas++;
            }

            CLI::write("Ciudades actualizadas: {$actualizadas}.", 'green');

            return EXIT_SUCCESS;
        } catch (Throwable $e) {
            CLI::error('Error al escribir en Firestore: ' . $e->getMessage());

            return EXIT_ERROR;
        }
    }
}
