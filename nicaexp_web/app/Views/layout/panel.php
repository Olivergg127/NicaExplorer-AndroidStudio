<!DOCTYPE html>
<html lang="es" data-bs-theme="dark">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><?= esc($title ?? 'NicaExplorer') ?> · Panel NicaExplorer</title>
    <script>
        (function () {
            try {
                var theme = window.localStorage.getItem('nica-theme');
                if (theme !== 'light' && theme !== 'dark') {
                    theme = 'dark';
                }
                document.documentElement.setAttribute('data-bs-theme', theme);
            } catch (error) {
                document.documentElement.setAttribute('data-bs-theme', 'dark');
            }
        })();
    </script>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap">

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/overlayscrollbars@2.10.1/styles/overlayscrollbars.min.css">
    <link rel="stylesheet" href="https://cdn.datatables.net/2.1.8/css/dataTables.bootstrap5.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/admin-lte@4.0.0/dist/css/adminlte.min.css">
    <link rel="stylesheet" href="<?= base_url('assets/panel.css') ?>">
</head>
<body class="layout-fixed sidebar-expand-lg">
<?php
$adminUser   = (string) (session()->get('nica_admin_user') ?? 'admin');
$adminName   = (string) (session()->get('nica_admin_name') ?? $adminUser);
$adminRole   = (string) (session()->get('nica_admin_role') ?? \App\Libraries\PanelPermissions::ROLE_ADMIN);
$roleLabel   = \App\Libraries\PanelPermissions::roleLabel($adminRole);
$current     = $active ?? '';
$apiHealth   = site_url('api/v1/health?api_key=' . rawurlencode((string) config('Nica')->apiKey));
$navGroups   = [
    'General'          => ['dashboard'],
    'Contenido'        => ['ciudades', 'lugares', 'categorias_lugares', 'rutas'],
    'Ecosistema local' => ['comercios', 'categorias_comercios', 'solicitudes_comercios'],
    'Administración'   => ['usuarios'],
];
$navItems = [];
if (\App\Libraries\PanelPermissions::can($adminRole, 'dashboard', 'L')) {
    $navItems['dashboard'] = ['label' => 'Dashboard', 'icon' => 'bi-grid-1x2', 'url' => site_url('panel')];
}
foreach (\App\Libraries\ResourceManager::all() as $navKey => $navResource) {
    // Solo se muestran los módulos que el rol puede consultar.
    if (! \App\Libraries\PanelPermissions::can($adminRole, $navKey, 'L')) {
        continue;
    }
    $navItems[$navKey] = [
        'label' => $navResource['label'],
        'icon'  => $navResource['icon'] ?? 'bi-collection',
        'url'   => site_url('panel/' . $navKey),
    ];
}
$grouped = [];
foreach ($navGroups as $groupLabel => $groupKeys) {
    foreach ($groupKeys as $groupKey) {
        if (isset($navItems[$groupKey])) {
            $grouped[$groupLabel][$groupKey] = $navItems[$groupKey];
            unset($navItems[$groupKey]);
        }
    }
}
if ($navItems !== []) {
    $grouped['Otros'] = $navItems;
}
?>
<div class="app-wrapper">

    <nav class="app-header nica-topbar">
        <div class="nica-topbar-inner">
            <button type="button" class="nica-icon-btn d-lg-none" data-lte-toggle="sidebar" aria-label="Alternar menú">
                <i class="bi bi-list"></i>
            </button>
            <button type="button" class="nica-icon-btn d-none d-lg-inline-flex" data-lte-toggle="sidebar" aria-label="Alternar menú">
                <i class="bi bi-text-indent-left"></i>
            </button>

            <div class="nica-topbar-title">
                <span class="nica-context-label">NicaExplorer</span>
                <span class="nica-topbar-sep d-none d-lg-inline">/</span>
                <strong><?= esc($title ?? 'Inicio') ?></strong>
            </div>

            <div class="nica-topbar-actions">
                <button type="button" class="nica-icon-btn" data-nica-theme-toggle
                        title="Cambiar a tema claro" aria-label="Cambiar tema">
                    <i class="bi bi-sun nica-theme-icon-sun" aria-hidden="true"></i>
                    <i class="bi bi-moon-stars nica-theme-icon-moon" aria-hidden="true"></i>
                </button>
                <a class="nica-icon-btn" href="<?= esc($apiHealth) ?>" target="_blank" title="Estado de la API" aria-label="Estado de la API">
                    <i class="bi bi-activity"></i>
                </a>
                <div class="dropdown">
                    <button class="nica-user" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <span class="nica-user-avatar"><?= esc(mb_substr($adminName, 0, 1)) ?></span>
                        <span class="nica-user-name"><?= esc($adminName) ?></span>
                        <i class="bi bi-chevron-down small nica-text-faint"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li class="px-3 py-2">
                            <div class="small fw-semibold"><?= esc($adminName) ?></div>
                            <div class="small nica-text-faint"><?= esc($roleLabel) ?></div>
                        </li>
                        <li><hr class="dropdown-divider"></li>
                        <li>
                            <a class="dropdown-item" href="<?= site_url('panel/logout') ?>">
                                <i class="bi bi-box-arrow-right me-2"></i>Cerrar sesión
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </nav>

    <aside class="app-sidebar">
        <div class="sidebar-brand">
            <a href="<?= site_url('panel') ?>" class="brand-link" aria-label="NicaExplorer">
                <span class="brand-mark"><i class="bi bi-compass"></i></span>
                <span class="brand-text">Nica<span>Explorer</span></span>
            </a>
        </div>
        <div class="sidebar-wrapper">
            <nav>
                <ul class="nav sidebar-menu flex-column" data-lte-toggle="treeview" role="menu" data-accordion="false">
                    <?php foreach ($grouped as $groupLabel => $groupItems): ?>
                        <li class="nav-header"><?= esc($groupLabel) ?></li>
                        <?php foreach ($groupItems as $itemKey => $item): ?>
                            <li class="nav-item">
                                <a href="<?= esc($item['url']) ?>" class="nav-link <?= $current === $itemKey ? 'active' : '' ?>">
                                    <i class="nav-icon bi <?= esc($item['icon']) ?>"></i>
                                    <p><?= esc($item['label']) ?></p>
                                </a>
                            </li>
                        <?php endforeach; ?>
                    <?php endforeach; ?>

                    <li class="nav-header">Sistema</li>
                    <li class="nav-item">
                        <a href="<?= esc($apiHealth) ?>" target="_blank" class="nav-link">
                            <i class="nav-icon bi bi-activity"></i>
                            <p>Estado de la API</p>
                        </a>
                    </li>
                </ul>
            </nav>

            <div class="nica-sidebar-footer">
                <div class="nica-user">
                    <span class="nica-user-avatar"><?= esc(mb_substr($adminName, 0, 1)) ?></span>
                    <span class="nica-user-name" title="<?= esc($roleLabel) ?>"><?= esc($adminName) ?></span>
                    <a class="nica-icon-btn" href="<?= site_url('panel/logout') ?>" title="Cerrar sesión" aria-label="Cerrar sesión">
                        <i class="bi bi-box-arrow-right"></i>
                    </a>
                </div>
            </div>
        </div>
    </aside>

    <main class="app-main">
        <div class="app-content">
            <div class="container-fluid">
                <?php if ($error = session()->getFlashdata('error')): ?>
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="bi bi-exclamation-triangle me-2"></i><?= esc($error) ?>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                    </div>
                <?php endif; ?>
                <?php if ($success = session()->getFlashdata('success')): ?>
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <i class="bi bi-check-circle me-2"></i><?= esc($success) ?>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                    </div>
                <?php endif; ?>

                <?= $this->renderSection('content') ?>
            </div>
        </div>

        <footer class="app-footer">
            <div class="d-flex flex-wrap justify-content-between gap-2">
                <span><strong class="nica-text-muted">NicaExplorer Backend</strong> · Los Punto y Coma</span>
                <span>CodeIgniter <?= esc(\CodeIgniter\CodeIgniter::CI_VERSION) ?> · Firestore</span>
            </div>
        </footer>
    </main>
</div>

<script src="https://cdn.jsdelivr.net/npm/jquery@3.7.1/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/overlayscrollbars@2.10.1/browser/overlayscrollbars.browser.es6.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/admin-lte@4.0.0/dist/js/adminlte.min.js"></script>
<script src="https://cdn.datatables.net/2.1.8/js/dataTables.min.js"></script>
<script src="https://cdn.datatables.net/2.1.8/js/dataTables.bootstrap5.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script src="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.js"></script>
    <script src="<?= base_url('assets/theme.js') ?>"></script>
    <script>
        window.NICA_USER = <?= json_encode([
            'role'        => $adminRole,
            'roleLabel'   => $roleLabel,
            'permissions' => \App\Libraries\PanelPermissions::forFrontend($adminRole),
            'canUpload'   => \App\Libraries\PanelPermissions::canUpload($adminRole),
        ], JSON_UNESCAPED_UNICODE) ?>;
    </script>
    <script src="<?= base_url('assets/panel.js') ?>"></script>
<?= $this->renderSection('scripts') ?>
</body>
</html>
