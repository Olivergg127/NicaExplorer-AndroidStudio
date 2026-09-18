<?php

namespace App\Libraries;

use Google\Cloud\Firestore\FieldValue;
use Throwable;

/**
 * Señal de cambios del catálogo.
 *
 * Cualquier alta/edición/borrado del contenido (desde el panel, la API o los seeds)
 * actualiza el documento `meta/catalogo` con un timestamp. La app consulta
 * /api/v1/version y, cuando cambia, recarga el catálogo sin necesidad de reabrirse.
 */
class CatalogSignal
{
    public static function touch(): void
    {
        try {
            FirebaseFactory::instance()->database()
                ->collection('meta')
                ->document('catalogo')
                ->set([
                    'updatedAt' => FieldValue::serverTimestamp(),
                    'version'   => FieldValue::increment(1),
                ], ['merge' => true]);
        } catch (Throwable) {
            // No interrumpir la operación principal si falla la señal.
        }
    }
}
