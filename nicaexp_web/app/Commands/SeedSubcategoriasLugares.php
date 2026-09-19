<?php

namespace App\Commands;

use App\Libraries\FirebaseFactory;
use CodeIgniter\CLI\BaseCommand;
use CodeIgniter\CLI\CLI;
use Google\Cloud\Firestore\FieldValue;
use Throwable;

/**
 * Reemplaza las subcategorías de "Lugares turísticos" por las nuevas y remapea
 * los lugares huérfanos a la subcategoría adecuada.
 *
 * Uso:
 *   php spark nica:seed-subcategorias-lugares
 *
 * Borra las subcategorías actuales que cuelgan de "Lugares turísticos", inserta
 * las nuevas y reasigna `lugares.categoria`.
 */
class SeedSubcategoriasLugares extends BaseCommand
{
    protected $group       = 'NicaExplorer';
    protected $name        = 'nica:seed-subcategorias-lugares';
    protected $description = 'Reemplaza las subcategorías de lugares y remapea los lugares huérfanos.';
    protected $usage       = 'nica:seed-subcategorias-lugares';

    private const PADRE_ID     = 'lugares_turisticos';
    private const PADRE_NOMBRE = 'Lugares turísticos';

    /** @var list<string> */
    private const NUEVAS_SUBCATEGORIAS = [
        'Museos',
        'Parques',
        'Monumentos',
        'Sitios históricos',
        'Miradores',
        'Reservas naturales',
        'Iglesias',
        'Sitios culturales',
    ];

    /**
     * Remapeo por categoría anterior (para lugares sin caso específico).
     *
     * @var array<string, string>
     */
    private const MAPA_POR_CATEGORIA = [
        'Monumento conmemorativo' => 'Monumentos',
        'Monumento cultural'      => 'Monumentos',
        'Monumento histórico'     => 'Sitios históricos',
        'Monumento urbano'        => 'Monumentos',
        'Patrimonio arqueológico' => 'Museos',
        'Patrimonio religioso'    => 'Iglesias',
        'Tradición ganadera'      => 'Monumentos',
    ];

    /**
     * Casos específicos por id de lugar (tienen prioridad sobre el mapa anterior).
     *
     * @var array<string, string>
     */
    private const MAPA_LUGARES = [
        'ruben_dario'                => 'Monumentos',
        'parque_central_de_juigalpa' => 'Parques',
    ];

    public function run(array $params)
    {
        try {
            $db = FirebaseFactory::instance()->database();
            $categorias = $db->collection('categorias_lugares');

            // 1) Borrar las subcategorías actuales de "Lugares turísticos".
            $borradas = 0;
            foreach ($categorias->documents() as $documento) {
                if ($documento->id() === self::PADRE_ID) {
                    continue;
                }

                $padre = trim((string) ($documento->data()['categoriaPadre'] ?? ''));
                if ($padre !== self::PADRE_NOMBRE) {
                    continue;
                }

                $nombre = (string) ($documento->data()['nombre'] ?? $documento->id());
                $categorias->document($documento->id())->delete();
                CLI::write('  borrada: ' . $nombre, 'yellow');
                $borradas++;
            }

            // 2) Insertar las nuevas subcategorías.
            $orden = 0;
            foreach (self::NUEVAS_SUBCATEGORIAS as $nombre) {
                $orden++;
                $categorias->document($this->slug($nombre))->set([
                    'nombre'         => $nombre,
                    'categoriaPadre' => self::PADRE_NOMBRE,
                    'orden'          => $orden,
                    'activo'         => true,
                    'actualizadoEn'  => FieldValue::serverTimestamp(),
                ], ['merge' => true]);

                CLI::write(sprintf('  nueva: %s -> %s', $nombre, $this->slug($nombre)), 'green');
            }

            // 3) Remapear los lugares huérfanos.
            $remapeados = 0;
            $sinMapa    = [];

            foreach ($db->collection('lugares')->documents() as $lugar) {
                $data     = $lugar->data();
                $anterior = trim((string) ($data['categoria'] ?? ''));

                if (in_array($anterior, self::NUEVAS_SUBCATEGORIAS, true)) {
                    continue;
                }

                $nueva = self::MAPA_LUGARES[$lugar->id()]
                    ?? self::MAPA_POR_CATEGORIA[$anterior]
                    ?? null;

                if ($nueva === null) {
                    $sinMapa[] = $lugar->id() . ' [' . $anterior . ']';

                    continue;
                }

                $db->collection('lugares')->document($lugar->id())->set([
                    'categoria'      => $nueva,
                    'categoriaPadre' => self::PADRE_NOMBRE,
                ], ['merge' => true]);

                CLI::write(sprintf('  %s: %s -> %s', $lugar->id(), $anterior !== '' ? $anterior : '(vacío)', $nueva), 'green');
                $remapeados++;
            }

            CLI::write("Subcategorías: {$borradas} borradas, " . count(self::NUEVAS_SUBCATEGORIAS) . ' insertadas.', 'green');
            CLI::write("Lugares remapeados: {$remapeados}.", 'green');

            if ($sinMapa !== []) {
                CLI::write('Lugares sin mapeo (revisar): ' . implode(', ', $sinMapa), 'yellow');
            }

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
