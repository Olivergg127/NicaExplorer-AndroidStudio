<?php

namespace App\Controllers\Panel;

use App\Controllers\BaseController;
use App\Libraries\PanelPermissions;
use App\Libraries\ResourceManager;
use Throwable;

/**
 * Panel principal con el resumen de cada colección.
 */
class Dashboard extends BaseController
{
    public function index()
    {
        $summary = [];
        $rol     = (string) (session()->get('nica_admin_role') ?? PanelPermissions::ROLE_ADMIN);

        foreach (ResourceManager::all() as $key => $definition) {
            // Solo se resumen los módulos que el rol puede consultar.
            if (! PanelPermissions::can($rol, $key, 'L')) {
                continue;
            }

            $count = null;

            try {
                $repository = ResourceManager::repository($key);
                $count      = $repository?->count();
            } catch (Throwable $e) {
                $count = null;
            }

            $summary[] = [
                'key'         => $key,
                'label'       => $definition['label'],
                'singular'    => $definition['singular'],
                'icon'        => $definition['icon'] ?? 'bi-collection',
                'description' => $definition['description'] ?? '',
                'count'       => $count,
            ];
        }

        return view('panel/dashboard', [
            'title'   => 'Panel de administración',
            'active'  => 'dashboard',
            'summary' => $summary,
        ]);
    }
}
