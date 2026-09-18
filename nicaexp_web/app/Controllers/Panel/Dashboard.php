<?php

namespace App\Controllers\Panel;

use App\Controllers\BaseController;
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

        foreach (ResourceManager::all() as $key => $definition) {
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
