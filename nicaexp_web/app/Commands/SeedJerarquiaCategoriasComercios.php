<?php

namespace App\Commands;

use App\Libraries\FirebaseFactory;
use CodeIgniter\CLI\BaseCommand;
use CodeIgniter\CLI\CLI;
use Google\Cloud\Firestore\FieldValue;
use Throwable;

/**
 * Crea la jerarquía de categorías de comercios: una categoría superior
 * "Comercios locales" y deja las categorías existentes como sus subcategorías.
 *
 * Uso:
 *   php spark nica:seed-categorias-comercios-jerarquia
 *
 * Es idempotente: crea/actualiza la categoría superior y solo asigna
 * `categoriaPadre` a las categorías que aún no tienen una.
 */
class SeedJerarquiaCategoriasComercios extends BaseCommand
{
    protected $group       = 'NicaExplorer';
    protected $name        = 'nica:seed-categorias-comercios-jerarquia';
    protected $description = 'Crea la categoría superior "Comercios locales" y anida las categorías existentes.';
    protected $usage       = 'nica:seed-categorias-comercios-jerarquia';

    private const ROOT_ID     = 'comercios_locales';
    private const ROOT_NOMBRE = 'Comercios locales';

    public function run(array $params)
    {
        try {
            $db = FirebaseFactory::instance()->database();
            $categorias = $db->collection('categorias_comercios');

            // 1) Categoría superior (raíz).
            $categorias->document(self::ROOT_ID)->set([
                'nombre'        => self::ROOT_NOMBRE,
                'descripcion'   => 'Categoría superior que agrupa las categorías de comercios locales.',
                'orden'         => 0,
                'activo'        => true,
                'actualizadoEn' => FieldValue::serverTimestamp(),
            ], ['merge' => true]);

            CLI::write('  ' . self::ROOT_NOMBRE . ' -> ' . self::ROOT_ID, 'green');

            // 2) Las categorías existentes pasan a ser subcategorías.
            $anidadas = 0;
            foreach ($categorias->documents() as $documento) {
                if ($documento->id() === self::ROOT_ID) {
                    continue;
                }

                $padre = trim((string) ($documento->data()['categoriaPadre'] ?? ''));
                if ($padre !== '') {
                    continue;
                }

                $categorias->document($documento->id())->set([
                    'categoriaPadre' => self::ROOT_NOMBRE,
                ], ['merge' => true]);

                CLI::write(sprintf('  %s -> %s', (string) ($documento->data()['nombre'] ?? $documento->id()), self::ROOT_NOMBRE), 'green');
                $anidadas++;
            }

            CLI::write("Jerarquía lista: 1 categoría superior, {$anidadas} subcategorías asignadas.", 'green');

            return EXIT_SUCCESS;
        } catch (Throwable $e) {
            CLI::error('Error al escribir en Firestore: ' . $e->getMessage());

            return EXIT_ERROR;
        }
    }
}
