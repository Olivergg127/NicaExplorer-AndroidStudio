<?php

namespace App\Libraries;

/**
 * Matriz de roles y permisos del panel de NicaExplorer.
 *
 * Roles:
 *  - ADMIN   (Administrador): control total del sistema.
 *  - EDITOR  (Editor): mantiene el contenido operativo (lugares, comercios,
 *            rutas) pero no gestiona catálogos, ciudades ni usuarios.
 *  - AUDITOR (Auditor): solo consulta.
 *  - USUARIO (Usuario): usuario final de la app; sin acceso al panel.
 *
 * Operaciones: C = crear, L = leer/consultar, M = modificar, E = eliminar.
 *
 * Los módulos coinciden con las claves de Config\NicaResources, más el
 * pseudo-módulo "dashboard" (portada del panel) y "perfil" (datos propios).
 */
class PanelPermissions
{
    public const ROLE_ADMIN   = 'ADMIN';
    public const ROLE_EDITOR  = 'EDITOR';
    public const ROLE_AUDITOR = 'AUDITOR';
    public const ROLE_USER    = 'USUARIO';

    private const OPS = ['C', 'L', 'M', 'E'];

    /**
     * rol => módulo => operaciones permitidas.
     *
     * "solicitudes_comercios" no aparece en la matriz original; se asocia al
     * módulo "comercios" porque aprobar/rechazar una solicitud es una edición
     * del ecosistema de comercios. Se documenta como decisión.
     *
     * @var array<string, array<string, list<string>>>
     */
    private const MATRIX = [
        self::ROLE_ADMIN => [
            'dashboard'             => ['L'],
            'perfil'                => ['L', 'M'],
            'ciudades'              => ['C', 'L', 'M', 'E'],
            'categorias_lugares'    => ['C', 'L', 'M', 'E'],
            'categorias_comercios'  => ['C', 'L', 'M', 'E'],
            'lugares'               => ['C', 'L', 'M', 'E'],
            'rutas'                 => ['C', 'L', 'M', 'E'],
            'comercios'             => ['C', 'L', 'M', 'E'],
            'solicitudes_comercios' => ['C', 'L', 'M', 'E'],
            'usuarios'              => ['C', 'L', 'M', 'E'],
        ],
        self::ROLE_EDITOR => [
            'dashboard'             => ['L'],
            'perfil'                => ['L', 'M'],
            'ciudades'              => ['L'],
            'categorias_lugares'    => ['L'],
            'categorias_comercios'  => ['L'],
            'lugares'               => ['C', 'L', 'M'],
            'rutas'                 => ['C', 'L', 'M'],
            'comercios'             => ['C', 'L', 'M'],
            'solicitudes_comercios' => ['C', 'L', 'M'],
            'usuarios'              => [],
        ],
        self::ROLE_AUDITOR => [
            'dashboard'             => ['L'],
            'perfil'                => ['L', 'M'],
            'ciudades'              => ['L'],
            'categorias_lugares'    => ['L'],
            'categorias_comercios'  => ['L'],
            'lugares'               => ['L'],
            'rutas'                 => ['L'],
            'comercios'             => ['L'],
            'solicitudes_comercios' => ['L'],
            'usuarios'              => ['L'],
        ],
        self::ROLE_USER => [],
    ];

    /** @var array<string, string> */
    private const LABELS = [
        self::ROLE_ADMIN   => 'Administrador',
        self::ROLE_EDITOR  => 'Editor',
        self::ROLE_AUDITOR => 'Auditor',
        self::ROLE_USER    => 'Usuario',
    ];

    /**
     * @return list<string>
     */
    public static function roles(): array
    {
        return array_keys(self::LABELS);
    }

    public static function roleLabel(string $rol): string
    {
        return self::LABELS[$rol] ?? $rol;
    }

    /**
     * Un rol con acceso al panel debe existir en la matriz y no ser USUARIO.
     */
    public static function canAccessPanel(string $rol): bool
    {
        return $rol !== self::ROLE_USER && isset(self::MATRIX[$rol]);
    }

    /**
     * @return list<string>
     */
    public static function ops(string $rol, string $module): array
    {
        return self::MATRIX[$rol][$module] ?? [];
    }

    public static function can(string $rol, string $module, string $op): bool
    {
        return in_array(strtoupper($op), self::OPS, true)
            && in_array(strtoupper($op), self::ops($rol, $module), true);
    }

    /**
     * Módulos del catálogo de recursos que el rol puede al menos consultar.
     *
     * @param list<string> $modules
     *
     * @return list<string>
     */
    public static function readableModules(string $rol, array $modules): array
    {
        return array_values(array_filter(
            $modules,
            static fn (string $module): bool => self::can($rol, $module, 'L')
        ));
    }

    /**
     * Puede crear o editar contenido (usado por la subida de imágenes).
     */
    public static function canUpload(string $rol): bool
    {
        foreach (['lugares', 'comercios', 'rutas'] as $module) {
            if (self::can($rol, $module, 'C') || self::can($rol, $module, 'M')) {
                return true;
            }
        }

        return false;
    }

    /**
     * Resumen de permisos listo para el frontend del panel.
     *
     * @return array<string, list<string>>
     */
    public static function forFrontend(string $rol): array
    {
        if (! isset(self::MATRIX[$rol])) {
            return [];
        }

        $map = [];
        foreach (self::MATRIX[$rol] as $module => $ops) {
            $map[$module] = array_values($ops);
        }

        return $map;
    }
}
