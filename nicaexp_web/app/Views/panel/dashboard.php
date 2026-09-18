<?= $this->extend('layout/panel') ?>

<?= $this->section('content') ?>
<?php
$byKey = [];
foreach ($summary as $row) {
    $byKey[$row['key']] = $row;
}

$primaryKeys = ['ciudades', 'lugares', 'rutas', 'comercios', 'usuarios'];
$apiHealth   = site_url('api/v1/health?api_key=' . rawurlencode((string) config('Nica')->apiKey));
?>

<div class="nica-page-head">
    <div class="nica-page-head-text">
        <p class="nica-page-eyebrow">NicaExplorer</p>
        <h1 class="nica-page-title">Resumen general</h1>
        <p class="nica-page-sub">
            Estado del catálogo turístico y del ecosistema local. Los conteos se obtienen
            en vivo desde Firestore.
        </p>
    </div>
    <div class="nica-page-actions">
        <a href="<?= esc($apiHealth) ?>" target="_blank" class="nica-btn-ghost">
            <i class="bi bi-activity"></i> Estado de la API
        </a>
    </div>
</div>

<div class="nica-section">
    <div class="nica-metrics">
        <?php foreach ($primaryKeys as $metricKey): ?>
            <?php if (! isset($byKey[$metricKey])) { continue; } $metric = $byKey[$metricKey]; ?>
            <a class="nica-metric" href="<?= site_url('panel/' . $metricKey) ?>">
                <div class="nica-metric-top">
                    <span class="nica-metric-label"><?= esc($metric['label']) ?></span>
                    <span class="nica-metric-icon"><i class="bi <?= esc($metric['icon']) ?>"></i></span>
                </div>
                <div>
                    <div class="nica-metric-value<?= $metric['count'] === null ? ' is-empty' : '' ?>">
                        <?= $metric['count'] === null ? '—' : esc((string) $metric['count']) ?>
                    </div>
                    <div class="nica-metric-meta"><?= esc($metric['description']) ?></div>
                </div>
            </a>
        <?php endforeach; ?>
    </div>
</div>

<div class="row g-4 nica-section">
    <div class="col-lg-7">
        <?php $solicitudes = $byKey['solicitudes_comercios'] ?? null; ?>
        <div class="nica-panel h-100 d-flex flex-column">
            <div class="nica-panel-head">
                <div class="d-flex align-items-center gap-3">
                    <span class="nica-icon-btn" aria-hidden="true"><i class="bi bi-envelope-paper"></i></span>
                    <div>
                        <h2 class="nica-panel-title">Solicitudes de comercios</h2>
                        <div class="nica-list-sub">Altas enviadas desde la aplicación móvil</div>
                    </div>
                </div>
            </div>
            <div class="nica-panel-body flex-grow-1">
                <?php if ($solicitudes === null): ?>
                    <p class="nica-text-muted mb-0">No se pudo consultar este recurso en Firestore.</p>
                <?php else: ?>
                    <div class="d-flex align-items-start justify-content-between gap-3">
                        <div>
                            <div class="nica-metric-value<?= $solicitudes['count'] === null ? ' is-empty' : '' ?>">
                                <?= $solicitudes['count'] === null ? '—' : esc((string) $solicitudes['count']) ?>
                            </div>
                            <div class="nica-metric-meta mb-3">solicitudes registradas</div>
                            <p class="nica-page-sub mb-0">
                                Revisa las altas enviadas desde la app y actualiza su estado
                                (pendiente, aprobada o rechazada).
                            </p>
                        </div>
                        <a href="<?= site_url('panel/solicitudes_comercios') ?>" class="btn btn-secondary btn-sm flex-shrink-0">
                            Revisar solicitudes <i class="bi bi-arrow-right ms-1"></i>
                        </a>
                    </div>
                <?php endif; ?>
            </div>
        </div>
    </div>

    <div class="col-lg-5">
        <div class="nica-panel h-100 d-flex flex-column">
            <div class="nica-panel-head">
                <h2 class="nica-panel-title">API REST</h2>
                <span class="badge text-bg-secondary">v1</span>
            </div>
            <div class="nica-panel-body flex-grow-1">
                <p class="mb-2 nica-text-muted small">
                    Base: <code><?= esc(site_url('api/v1')) ?></code>
                </p>
                <p class="mb-3 nica-text-muted small">
                    Requiere el header <code>X-API-KEY</code> (o <code>?api_key=</code>),
                    configurado en <code>.env</code> como <code>Nica.apiKey</code>.
                </p>
                <div class="nica-list">
                    <div class="nica-list-item px-0 py-2">
                        <span class="badge text-bg-success">GET</span>
                        <span class="nica-list-sub ms-2">/api/v1/{coleccion}</span>
                    </div>
                    <div class="nica-list-item px-0 py-2">
                        <span class="badge text-bg-success">GET</span>
                        <span class="nica-list-sub ms-2">/api/v1/{coleccion}/{id}</span>
                    </div>
                    <div class="nica-list-item px-0 py-2">
                        <span class="badge text-bg-info">POST</span>
                        <span class="nica-list-sub ms-2">/api/v1/{coleccion}</span>
                    </div>
                    <div class="nica-list-item px-0 py-2">
                        <span class="badge text-bg-warning">PUT</span>
                        <span class="nica-list-sub ms-2">/api/v1/{coleccion}/{id}</span>
                    </div>
                    <div class="nica-list-item px-0 py-2">
                        <span class="badge text-bg-danger">DELETE</span>
                        <span class="nica-list-sub ms-2">/api/v1/{coleccion}/{id}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<?= $this->endSection() ?>
