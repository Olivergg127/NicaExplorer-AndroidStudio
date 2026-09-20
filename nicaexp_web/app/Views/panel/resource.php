<?= $this->extend('layout/panel') ?>

<?= $this->section('content') ?>
<?php
$singular    = $definition['singular'] ?? 'registro';
$hasLocation = $hasLocation ?? false;

$ubicacionNames = ['latitud', 'longitud', 'ciudad', 'cityId', 'direccion', 'departamento'];
$estadoNames    = ['activo', 'orden', 'estado', 'tieneWhatsapp', 'rol', 'destacado', 'publicado'];

$rol            = (string) (session()->get('nica_admin_role') ?? \App\Libraries\PanelPermissions::ROLE_ADMIN);
$ops            = \App\Libraries\PanelPermissions::ops($rol, (string) $resource);
$canCreate      = in_array('C', $ops, true);
$canEdit        = in_array('M', $ops, true);
$canDelete      = in_array('E', $ops, true);
$canUpload      = \App\Libraries\PanelPermissions::canUpload($rol);

$blocks = [
    'Identificador'        => [],
    'Información básica'   => [],
    'Ubicación'            => [],
    'Contenido'            => [],
    'Multimedia'           => [],
    'Listas y relaciones'  => [],
    'Estado y publicación' => [],
];

foreach ($fields as $field) {
    $type = $field['type'] ?? 'string';
    $name = $field['name'] ?? '';

    if (in_array($type, ['image', 'images'], true)) {
        $block = 'Multimedia';
    } elseif (in_array($type, ['stringlist', 'stops'], true)) {
        $block = 'Listas y relaciones';
    } elseif (in_array($name, $ubicacionNames, true)) {
        $block = 'Ubicación';
    } elseif ($type === 'text') {
        $block = 'Contenido';
    } elseif (in_array($name, $estadoNames, true)) {
        $block = 'Estado y publicación';
    } else {
        $block = 'Información básica';
    }

    $blocks[$block][] = $field;
}
?>

<div class="nica-page-head">
    <div class="nica-page-head-text">
        <p class="nica-page-eyebrow"><?= esc($definition['label']) ?></p>
        <h1 class="nica-page-title"><?= esc($definition['label']) ?></h1>
        <?php if (! empty($definition['description'])): ?>
            <p class="nica-page-sub"><?= esc($definition['description']) ?></p>
        <?php endif; ?>
    </div>
    <div class="nica-page-actions">
        <?php if ($canCreate): ?>
            <button type="button" class="btn btn-primary" id="nica-btn-new">
                <i class="bi bi-plus-lg"></i> Nuevo <?= esc($singular) ?>
            </button>
        <?php endif; ?>
    </div>
</div>

<div class="nica-panel">
    <div class="nica-panel-body is-flush">
        <div class="table-responsive">
            <table id="nica-table" class="table table-hover" style="width:100%"></table>
        </div>
    </div>
</div>

<div class="modal fade" id="nica-modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
            <form id="nica-form" autocomplete="off">
                <div class="modal-header">
                    <h5 class="modal-title" id="nica-modal-title">Nuevo <?= esc($singular) ?></h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="_current_id" id="nica-current-id" value="">

                    <?php foreach ($blocks as $blockTitle => $blockFields): ?>
                        <?php if ($blockTitle === 'Identificador' || $blockFields === []) { continue; } ?>
                        <div class="nica-form-block">
                            <h6 class="nica-form-block-title"><?= esc($blockTitle) ?></h6>
                            <div class="row g-3">
                                <?php foreach ($blockFields as $field): ?>
                                    <?php
                                    $name = $field['name'];
                                    $type = $field['type'];
                                    $id   = 'nica-field-' . $name;
                                    $req  = ! empty($field['required']);
                                    $wide = in_array($type, ['text', 'image', 'images', 'stringlist', 'stops'], true);
                                    ?>
                                    <div class="col-md-<?= $wide ? '12' : '6' ?>">
                                        <label class="form-label" for="<?= esc($id) ?>">
                                            <?= esc($field['label']) ?><?= $req ? ' *' : '' ?>
                                        </label>

                                        <?php if ($type === 'bool'): ?>
                                            <div class="form-check form-switch">
                                                <input class="form-check-input" type="checkbox" role="switch"
                                                       name="<?= esc($name) ?>" id="<?= esc($id) ?>" value="1">
                                                <label class="form-check-label" for="<?= esc($id) ?>"><?= esc($field['label']) ?></label>
                                            </div>

                                        <?php elseif ($type === 'enum'): ?>
                                            <select class="form-select" name="<?= esc($name) ?>" id="<?= esc($id) ?>" <?= $req ? 'required' : '' ?>>
                                                <option value="">— Seleccionar —</option>
                                                <?php foreach (($field['options'] ?? []) as $option): ?>
                                                    <option value="<?= esc($option) ?>"><?= esc($option) ?></option>
                                                <?php endforeach; ?>
                                            </select>

                                        <?php elseif ($type === 'reference'): ?>
                                            <select class="form-select" name="<?= esc($name) ?>" id="<?= esc($id) ?>" <?= $req ? 'required' : '' ?>>
                                                <option value="">— Seleccionar —</option>
                                                <?php foreach (($references[$name] ?? []) as $option): ?>
                                                    <option value="<?= esc($option['value']) ?>"><?= esc($option['label']) ?></option>
                                                <?php endforeach; ?>
                                            </select>
                                            <?php if (($references[$name] ?? []) === []): ?>
                                                <div class="form-text">Aún no hay opciones en esta colección.</div>
                                            <?php endif; ?>

                                        <?php elseif (in_array($type, ['stringlist', 'images', 'stops'], true)): ?>
                                            <div class="nica-dynamic" data-name="<?= esc($name) ?>" data-type="<?= esc($type) ?>">
                                                <input type="hidden" name="<?= esc($name) ?>[]" value="">
                                            </div>
                                            <button type="button" class="nica-btn-ghost mt-2 nica-add"
                                                    data-name="<?= esc($name) ?>" data-type="<?= esc($type) ?>">
                                                <i class="bi bi-plus-lg"></i><?= $type === 'images' ? 'Añadir imagen' : ($type === 'stops' ? 'Añadir parada' : 'Añadir elemento') ?>
                                            </button>

                                        <?php elseif ($type === 'image'): ?>
                                            <div class="nica-image-field">
                                                <div class="input-group">
                                                    <input type="text" class="form-control" name="<?= esc($name) ?>" id="<?= esc($id) ?>"
                                                           placeholder="<?= $canUpload ? 'https://... o sube un archivo' : 'https://...' ?>" <?= $req ? 'required' : '' ?>>
                                                    <?php if ($canUpload): ?>
                                                        <button type="button" class="btn btn-outline-secondary nica-image-choose"
                                                                data-target="<?= esc($id) ?>">
                                                            <i class="bi bi-upload"></i> Subir
                                                        </button>
                                                    <?php endif; ?>
                                                </div>
                                                <input type="file" class="d-none nica-image-file" accept="image/*"
                                                       data-target="<?= esc($id) ?>">
                                                <div class="nica-image-preview-wrap mt-2 d-none">
                                                    <img class="nica-image-preview" alt="Vista previa">
                                                    <button type="button" class="nica-image-remove">
                                                        <i class="bi bi-trash3"></i> Quitar imagen
                                                    </button>
                                                </div>
                                            </div>

                                        <?php elseif ($type === 'text'): ?>
                                            <textarea class="form-control" name="<?= esc($name) ?>" id="<?= esc($id) ?>"
                                                      rows="3" <?= $req ? 'required' : '' ?>></textarea>

                                        <?php else: ?>
                                            <?php
                                            $inputType = match ($type) {
                                                'email' => 'email',
                                                'url'   => 'url',
                                                default => 'text',
                                            };
                                            ?>
                                            <input type="<?= $inputType ?>" class="form-control"
                                                   name="<?= esc($name) ?>" id="<?= esc($id) ?>"
                                                   <?= $req ? 'required' : '' ?>>
                                        <?php endif; ?>
                                    </div>
                                <?php endforeach; ?>
                            </div>
                            <?php if ($blockTitle === 'Ubicación' && $hasLocation): ?>
                                <div class="nica-location-picker">
                                    <div class="input-group input-group-sm">
                                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                                        <input type="text" class="form-control" id="nica-map-search"
                                               placeholder="Buscar dirección o lugar…">
                                        <button type="button" class="btn btn-outline-secondary" id="nica-map-search-btn">
                                            Buscar
                                        </button>
                                    </div>
                                    <div id="nica-map" class="nica-map" role="application"
                                         aria-label="Mapa para elegir coordenadas"></div>
                                    <div class="form-text">
                                        Haz clic en el mapa (o arrastra el marcador) para fijar la latitud y longitud.
                                    </div>
                                </div>
                            <?php endif; ?>
                        </div>
                    <?php endforeach; ?>

                    <div id="nica-form-errors" class="alert alert-danger mt-3 d-none"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-check-lg"></i> Guardar
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
<?= $this->endSection() ?>

<?= $this->section('scripts') ?>
<script>
window.NICA_RESOURCE = <?= json_encode([
    'key'         => $resource,
    'singular'    => $singular,
    'id'          => $definition['id'] ?? ['strategy' => 'auto'],
    'titleField'  => $definition['title_field'] ?? 'id',
    'hasLocation' => $hasLocation,
    'columns'     => $columns,
    'fields'      => $fields,
    'references'  => $references ?? [],
    'refs'        => $refs ?? [],
    'permissions' => [
        'create' => $canCreate,
        'edit'   => $canEdit,
        'delete' => $canDelete,
        'upload' => $canUpload,
    ],
    'urls'        => [
        'data'   => site_url('panel/' . $resource . '/data'),
        'save'   => site_url('panel/' . $resource . '/save'),
        'delete' => site_url('panel/' . $resource . '/delete'),
        'upload' => site_url('panel/upload'),
    ],
    'csrf'        => [
        'header' => csrf_header(),
        'token'  => csrf_hash(),
    ],
], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES) ?>;
</script>
<?= $this->endSection() ?>
