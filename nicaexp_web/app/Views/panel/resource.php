<?= $this->extend('layout/panel') ?>

<?= $this->section('content') ?>
<?php
$idStrategy = $definition['id']['strategy'] ?? 'auto';
$idLabel    = $definition['id']['label'] ?? 'ID';
$singular   = $definition['singular'] ?? 'registro';

$ubicacionNames = ['latitud', 'longitud', 'ciudad', 'cityId', 'direccion', 'departamento'];
$estadoNames    = ['activo', 'orden', 'estado', 'tieneWhatsapp', 'rol', 'destacado', 'publicado'];

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
        <button type="button" class="btn btn-primary" id="nica-btn-new">
            <i class="bi bi-plus-lg"></i> Nuevo <?= esc($singular) ?>
        </button>
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

                    <?php if ($idStrategy === 'manual' || $idStrategy === 'auto'): ?>
                        <div class="nica-form-block">
                            <h6 class="nica-form-block-title">Identificador</h6>
                            <div class="mb-0" id="nica-id-block">
                                <label class="form-label" for="nica-doc-id">
                                    <?= esc($idLabel) ?><?= $idStrategy === 'manual' ? ' *' : '' ?>
                                </label>
                                <input
                                    type="text"
                                    class="form-control"
                                    name="_id"
                                    id="nica-doc-id"
                                    <?= $idStrategy === 'manual' ? 'required' : '' ?>
                                >
                                <div class="form-text">
                                    Clave del documento en Firestore.
                                    <?= $idStrategy === 'auto' ? 'Si se deja vacío se genera automáticamente.' : '' ?>
                                </div>
                            </div>
                        </div>
                    <?php endif; ?>

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
                                            <div class="input-group">
                                                <input type="text" class="form-control" name="<?= esc($name) ?>" id="<?= esc($id) ?>"
                                                       placeholder="https://... o sube un archivo" <?= $req ? 'required' : '' ?>>
                                                <button type="button" class="btn btn-outline-secondary nica-image-choose"
                                                        data-target="<?= esc($id) ?>">
                                                    <i class="bi bi-upload"></i> Subir
                                                </button>
                                            </div>
                                            <input type="file" class="d-none nica-image-file" accept="image/*"
                                                   data-target="<?= esc($id) ?>">
                                            <img class="nica-image-preview mt-2 d-none" alt="Vista previa">

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
    'columns'     => $columns,
    'fields'      => $fields,
    'refs'        => $refs ?? [],
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
