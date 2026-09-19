<?php

namespace App\Commands;

use App\Libraries\FirebaseFactory;
use CodeIgniter\CLI\BaseCommand;
use CodeIgniter\CLI\CLI;
use Google\Cloud\Firestore\FieldValue;
use Throwable;

/**
 * Inserta la taxonomía de categorías de comercios: 7 categorías superiores con
 * sus subcategorías.
 *
 * Uso:
 *   php spark nica:seed-taxonomia-comercios
 *
 * Es idempotente: crea/actualiza cada categoría por su slug. Las categorías
 * superiores quedan sin `categoriaPadre`; las subcategorías apuntan a su superior.
 */
class SeedTaxonomiaComercios extends BaseCommand
{
    protected $group       = 'NicaExplorer';
    protected $name        = 'nica:seed-taxonomia-comercios';
    protected $description = 'Inserta la taxonomía (categorías superiores y subcategorías) de comercios.';
    protected $usage       = 'nica:seed-taxonomia-comercios';

    /**
     * @var list<array{nombre: string, orden: int, hijos: list<string>}>
     */
    private const TAXONOMIA = [
        [
            'nombre' => 'Restaurantes y comida',
            'orden'  => 2,
            'hijos'  => ['Restaurantes', 'Cafeterías', 'Comida rápida', 'Comida típica', 'Heladerías/pastelerías', 'Bares'],
        ],
        [
            'nombre' => 'Hospedaje',
            'orden'  => 3,
            'hijos'  => ['Hoteles', 'Hostales', 'Posadas', 'Casas de huéspedes'],
        ],
        [
            'nombre' => 'Comercios locales',
            'orden'  => 4,
            'hijos'  => ['Tiendas', 'Supermercados', 'Artesanías', 'Tiendas de ropa', 'Souvenirs', 'Mercados'],
        ],
        [
            'nombre' => 'Entretenimiento',
            'orden'  => 5,
            'hijos'  => ['Cines', 'Centros recreativos', 'Discotecas', 'Centros deportivos', 'Actividades familiares'],
        ],
        [
            'nombre' => 'Naturaleza y aventura',
            'orden'  => 6,
            'hijos'  => ['Senderismo', 'Balnearios', 'Ríos', 'Cascadas', 'Actividades al aire libre', 'Ecoturismo'],
        ],
        [
            'nombre' => 'Servicios',
            'orden'  => 7,
            'hijos'  => ['Bancos', 'Farmacias', 'Gasolineras', 'Hospitales/clínicas', 'Talleres', 'Servicios turísticos'],
        ],
        [
            'nombre' => 'Transporte',
            'orden'  => 8,
            'hijos'  => ['Terminales', 'Paradas de buses', 'Taxis', 'Alquiler de vehículos', 'Transporte turístico'],
        ],
    ];

    public function run(array $params)
    {
        try {
            $categorias = FirebaseFactory::instance()->database()->collection('categorias_comercios');

            $superiores = 0;
            $subcategorias = 0;

            foreach (self::TAXONOMIA as $padre) {
                $padreId = $this->slug($padre['nombre']);

                $categorias->document($padreId)->set([
                    'nombre'        => $padre['nombre'],
                    'categoriaPadre' => '',
                    'orden'         => $padre['orden'],
                    'activo'        => true,
                    'actualizadoEn' => FieldValue::serverTimestamp(),
                ], ['merge' => true]);

                CLI::write(sprintf('  [%d] %s -> %s', $padre['orden'], $padre['nombre'], $padreId), 'green');
                $superiores++;

                $orden = 0;
                foreach ($padre['hijos'] as $hijo) {
                    $orden++;
                    $hijoId = $this->slug($hijo);

                    $categorias->document($hijoId)->set([
                        'nombre'        => $hijo,
                        'categoriaPadre' => $padre['nombre'],
                        'orden'         => $orden,
                        'activo'        => true,
                        'actualizadoEn' => FieldValue::serverTimestamp(),
                    ], ['merge' => true]);

                    CLI::write(sprintf('      %s -> %s', $hijo, $hijoId), 'white');
                    $subcategorias++;
                }
            }

            CLI::write("Taxonomía lista: {$superiores} categorías superiores, {$subcategorias} subcategorías.", 'green');

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
