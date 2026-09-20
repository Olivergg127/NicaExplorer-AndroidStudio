<?php

namespace App\Filters;

use App\Libraries\PanelPermissions;
use App\Libraries\ResourceManager;
use CodeIgniter\Filters\FilterInterface;
use CodeIgniter\HTTP\RequestInterface;
use CodeIgniter\HTTP\ResponseInterface;

/**
 * Autoriza cada acción del panel según el rol del usuario en sesión.
 *
 * Deriva el módulo y la operación (C/L/M/E) de la ruta solicitada:
 *   GET  panel                        -> dashboard
 *   POST panel/upload                 -> subida de imágenes
 *   GET  panel/{modulo}               -> L
 *   GET  panel/{modulo}/data          -> L
 *   POST panel/{modulo}/save          -> C (nuevo) o M (edición)
 *   POST panel/{modulo}/delete        -> E
 *
 * Se aplica después de PanelAuthFilter (ya hay sesión con rol).
 */
class PanelPermissionFilter implements FilterInterface
{
    /**
     * @param list<string>|null $arguments
     *
     * @return ResponseInterface|null
     */
    public function before(RequestInterface $request, $arguments = null)
    {
        if (session()->get('nica_admin') !== true) {
            // PanelAuthFilter ya se encarga; aquí solo se evita el acceso anónimo.
            return $this->deny($request, 401, 'Sesión expirada. Vuelve a iniciar sesión.');
        }

        $rol = $this->role();

        [$module, $op] = $this->resolve($request);

        if ($module === null) {
            // Ruta desconocida: la decide el controlador (404).
            return null;
        }

        if ($op === 'UPLOAD') {
            if (PanelPermissions::canUpload($rol)) {
                return null;
            }

            return $this->deny($request, 403, 'No tienes permiso para subir archivos.');
        }

        if (! PanelPermissions::can($rol, $module, $op)) {
            return $this->deny($request, 403, 'No tienes permiso para realizar esta acción en este módulo.');
        }

        return null;
    }

    /**
     * @param list<string>|null $arguments
     */
    public function after(RequestInterface $request, ResponseInterface $response, $arguments = null)
    {
    }

    private function role(): string
    {
        $rol = trim((string) session()->get('nica_admin_role'));

        // Sesiones antiguas (o el admin de .env) no tienen rol: son ADMIN.
        return $rol === '' ? PanelPermissions::ROLE_ADMIN : $rol;
    }

    /**
     * @return array{0: string|null, 1: string}
     */
    private function resolve(RequestInterface $request): array
    {
        $segments = $this->segments($request);

        if ($segments === []) {
            return ['dashboard', 'L'];
        }

        $first = $segments[0];

        if ($first === 'upload') {
            return ['upload', 'UPLOAD'];
        }

        if ($first === 'perfil') {
            return ['perfil', $request->getMethod() === 'post' ? 'M' : 'L'];
        }

        if (! ResourceManager::exists($first)) {
            return [null, 'L'];
        }

        $action = $segments[1] ?? '';

        return match ($action) {
            ''      => [$first, 'L'],
            'data'  => [$first, 'L'],
            'delete' => [$first, 'E'],
            'save'  => [$first, $this->saveOp($request)],
            default => [$first, 'L'],
        };
    }

    /**
     * Un save sin "_current_id" crea (C); con él, edita (M).
     */
    private function saveOp(RequestInterface $request): string
    {
        $currentId = trim((string) ($request->getPost('_current_id') ?? ''));

        return $currentId === '' ? 'C' : 'M';
    }

    /**
     * @return list<string>
     */
    private function segments(RequestInterface $request): array
    {
        $path     = trim((string) $request->getUri()->getPath(), '/');
        $segments = $path === '' ? [] : explode('/', $path);

        $index = array_search('panel', $segments, true);
        if ($index === false) {
            return [];
        }

        return array_values(array_slice($segments, $index + 1));
    }

    private function deny(RequestInterface $request, int $status, string $message): ResponseInterface
    {
        if ($request->isAJAX()) {
            return service('response')->setStatusCode($status)->setJSON(['error' => $message]);
        }

        // En el dashboard no se redirige a sí mismo (evita un bucle).
        if ($this->segments($request) === []) {
            return service('response')->setStatusCode($status)->setBody($message);
        }

        return redirect()->to(site_url('panel'))->with('error', $message);
    }
}
