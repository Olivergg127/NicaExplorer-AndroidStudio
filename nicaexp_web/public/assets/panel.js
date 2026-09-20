/* global jQuery, DataTable, bootstrap, Swal */
(function ($) {
    'use strict';

    $(function () {
        const cfg = window.NICA_RESOURCE;
        if (!cfg || !document.getElementById('nica-table')) {
            return;
        }

        const csrfHeaders = (cfg.csrf && cfg.csrf.token) ? { [cfg.csrf.header]: cfg.csrf.token } : {};
        $.ajaxSetup({ headers: csrfHeaders });

        const escapeHtml = (value) => String(value === null || value === undefined ? '' : value).replace(/[&<>"']/g, (c) => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;',
        }[c]));

        const badgeClass = (value) => {
            const v = String(value).toLowerCase();
            if (['true', 'activo', 'aprobada', 'admin', 'baja', '1'].includes(v)) return 'success';
            if (['rechazada', 'alta', 'false', '0'].includes(v)) return 'danger';
            if (['pendiente', 'auditor', 'moderada'].includes(v)) return 'warning';
            return 'secondary';
        };

        const renderValue = (column, value) => {
            if (column.type === 'bool') {
                return '<span class="badge text-bg-' + (value ? 'success' : 'secondary') + '">' + (value ? 'Sí' : 'No') + '</span>';
            }
            if (column.type === 'enum') {
                const label = (value === '' || value === null || value === undefined) ? '—' : value;
                return '<span class="badge text-bg-' + badgeClass(value) + '">' + escapeHtml(label) + '</span>';
            }
            if (column.type === 'image') {
                if (!value) return '<span class="text-body-secondary">—</span>';
                return '<a href="' + escapeHtml(value) + '" target="_blank"><img src="' + escapeHtml(value) + '" class="nica-thumb" alt="imagen" loading="lazy"></a>';
            }
            if (column.type === 'images') {
                if (!value) return '<span class="text-body-secondary">—</span>';
                const urls = String(value).split(',').map((u) => u.trim()).filter(Boolean);
                if (!urls.length) return '<span class="text-body-secondary">—</span>';
                const extra = urls.length > 1 ? ' <span class="badge text-bg-secondary">+' + (urls.length - 1) + '</span>' : '';
                return '<a href="' + escapeHtml(urls[0]) + '" target="_blank"><img src="' + escapeHtml(urls[0]) + '" class="nica-thumb" alt="imagen" loading="lazy"></a>' + extra;
            }
            return escapeHtml(value);
        };

        const Toast = Swal.mixin({
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 2600,
            timerProgressBar: true,
        });

        const emptyState = ''
            + '<div class="nica-empty">'
            + '<span class="nica-empty-icon"><i class="bi bi-inbox"></i></span>'
            + '<div class="nica-empty-title">Todavía no hay registros</div>'
            + '<div class="nica-empty-text">Crea el primer ' + escapeHtml(String(cfg.singular).toLowerCase())
            + ' para que aparezca en la aplicación y en el mapa.</div>'
            + '<button type="button" class="btn btn-primary nica-empty-new"><i class="bi bi-plus-lg"></i> Nuevo ' + escapeHtml(cfg.singular) + '</button>'
            + '</div>';

        const noResultsState = ''
            + '<div class="nica-empty">'
            + '<span class="nica-empty-icon"><i class="bi bi-search"></i></span>'
            + '<div class="nica-empty-title">Sin resultados</div>'
            + '<div class="nica-empty-text">Ajusta la búsqueda o los filtros para encontrar lo que buscas.</div>'
            + '</div>';

        const idFieldName = (cfg.id && cfg.id.strategy === 'field') ? cfg.id.field : null;

        // Los identificadores no se muestran en la tabla (id, cityId, userId, uid…).
        const isIdentifier = (name) => name === 'id'
            || (idFieldName !== null && name === idFieldName)
            || /Id$/.test(name);

        const table = $('#nica-table').DataTable({
            ajax: { url: cfg.urls.data, dataSrc: 'data' },
            columns: [
                ...cfg.columns
                    .filter((column) => !isIdentifier(column.data))
                    .map((column) => ({
                        data: column.data,
                        title: column.label,
                        render: (value) => renderValue(column, value),
                    })),
                {
                    data: '_acc',
                    title: '',
                    orderable: false,
                    searchable: false,
                    className: 'text-end text-nowrap',
                    render: (id) => ''
                        + '<button type="button" class="nica-icon-btn nica-edit" data-id="' + escapeHtml(id) + '" title="Editar" aria-label="Editar"><i class="bi bi-pencil"></i></button>'
                        + '<button type="button" class="nica-icon-btn nica-delete" data-id="' + escapeHtml(id) + '" title="Eliminar" aria-label="Eliminar"><i class="bi bi-trash"></i></button>',
                },
            ],
            order: [[0, 'asc']],
            pageLength: 25,
            lengthMenu: [10, 25, 50, 100],
            language: {
                search: '',
                searchPlaceholder: 'Buscar…',
                lengthMenu: 'Mostrar _MENU_ registros',
                info: 'Mostrando _START_ a _END_ de _TOTAL_ registros',
                infoEmpty: 'Sin registros',
                infoFiltered: '(filtrado de _MAX_ registros)',
                zeroRecords: noResultsState,
                emptyTable: emptyState,
                loadingRecords: 'Cargando…',
                processing: 'Procesando…',
                paginate: { first: 'Primero', last: 'Último', next: 'Siguiente', previous: 'Anterior' },
            },
        });

        const modal = new bootstrap.Modal(document.getElementById('nica-modal'));
        const $form = $('#nica-form');
        const $errors = $('#nica-form-errors');
        const $currentId = $('#nica-current-id');
        const $docId = $('#nica-doc-id');

        // ------------------------------------------------------------------
        // Editores dinámicos (listas de texto, imágenes y paradas)
        // ------------------------------------------------------------------
        const dynamicContainer = (name) => $form.find('.nica-dynamic[data-name="' + name + '"]');

        function clearDynamic($container) {
            const name = $container.data('name');
            $container.empty().append('<input type="hidden" name="' + name + '[]" value="">');
        }

        function renumberStops() {
            $form.find('.nica-stop-order').each(function (index) {
                $(this).text(index + 1);
            });
        }

        function addStringRow(name, value) {
            const $row = $('<div class="input-group input-group-sm mb-2 nica-item"></div>');
            $row.append($('<input type="text" class="form-control">').attr('name', name + '[]').val(value || ''));
            $row.append('<button type="button" class="btn btn-outline-danger nica-remove" title="Quitar"><i class="bi bi-x-lg"></i></button>');
            dynamicContainer(name).append($row);
        }

        function uploadImage(file, $row) {
            const $bar = $row.find('.nica-progress');
            const $fill = $row.find('.nica-progress .progress-bar');
            const $url = $row.find('.nica-url');
            const $thumb = $row.find('.nica-thumb');

            // Preview inmediato del archivo local.
            $thumb.attr('src', URL.createObjectURL(file)).removeClass('d-none');
            $bar.removeClass('d-none');
            $fill.removeClass('bg-danger bg-success').css('width', '0%').text('0%');

            const formData = new FormData();
            formData.append('image', file);

            $.ajax({
                url: cfg.urls.upload,
                method: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                xhr: function () {
                    const xhr = new window.XMLHttpRequest();
                    xhr.upload.addEventListener('progress', function (event) {
                        if (event.lengthComputable) {
                            const percent = Math.round((event.loaded / event.total) * 100);
                            $fill.css('width', percent + '%').text(percent + '%');
                        }
                    });
                    return xhr;
                },
            })
                .done(function (response) {
                    $url.val(response.url);
                    $thumb.attr('src', response.url);
                    $fill.addClass('bg-success').css('width', '100%').text('100%');
                    setTimeout(() => $bar.addClass('d-none'), 900);
                    Toast.fire({ icon: 'success', title: 'Imagen subida.' });
                })
                .fail(function (xhr) {
                    $fill.addClass('bg-danger').css('width', '100%').text('Error');
                    showError(xhr);
                });
        }

        function addImageRow(name, url) {
            const $row = $(
                '<div class="nica-item card mb-2">'
                + '<div class="card-body p-2">'
                + '<div class="d-flex gap-2 align-items-center">'
                + '<img class="nica-thumb rounded border d-none" alt="" style="width:56px;height:56px;object-fit:cover">'
                + '<div class="flex-grow-1">'
                + '<div class="input-group input-group-sm">'
                + '<input type="text" class="form-control nica-url" placeholder="URL de la imagen o súbela">'
                + '<button type="button" class="btn btn-outline-secondary nica-img-choose" title="Subir imagen"><i class="bi bi-upload"></i></button>'
                + '</div>'
                + '<div class="progress nica-progress mt-1 d-none" style="height:6px"><div class="progress-bar" style="width:0%"></div></div>'
                + '</div>'
                + '<button type="button" class="btn btn-outline-danger nica-remove" title="Quitar"><i class="bi bi-x-lg"></i></button>'
                + '</div>'
                + '<input type="file" class="d-none nica-img-file" accept="image/*">'
                + '</div></div>'
            );

            $row.find('.nica-url').attr('name', name + '[]').val(url || '');
            if (url) {
                $row.find('.nica-thumb').attr('src', url).removeClass('d-none');
            }
            dynamicContainer(name).append($row);
        }

        function stopOptions(tipo) {
            const options = (cfg.refs && cfg.refs[tipo]) ? cfg.refs[tipo] : [];
            const cityId = ($form.find('[name="cityId"]').val() || '').toString().trim();

            // Las paradas solo ofrecen lugares/comercios de la ciudad de la ruta.
            if (!cityId) {
                return options;
            }

            return options.filter(function (item) {
                return !item.cityId || item.cityId === cityId;
            });
        }

        function refreshStopRefs($row) {
            const tipo = $row.find('.nica-stop-tipo').val();
            const current = $row.find('.nica-stop-ref').data('current');
            const $ref = $row.find('.nica-stop-ref').empty();
            $ref.append('<option value="">— Seleccionar —</option>');
            stopOptions(tipo).forEach(function (item) {
                $ref.append($('<option></option>').attr('value', item.id).text(item.nombre + ' (' + item.id + ')'));
            });
            if (current) {
                $ref.val(current);
            }
            updateStopValue($row);
        }

        function updateStopValue($row) {
            const tipo = $row.find('.nica-stop-tipo').val();
            const ref = $row.find('.nica-stop-ref').val();
            $row.find('.nica-stop-value').val(ref ? (tipo + ':' + ref) : '');
        }

        function addStopRow(name, value) {
            const parts = String(value || '').split(':');
            const rawTipo = (parts.length === 2 && parts[0]) ? parts[0] : 'lugar';
            const tipo = rawTipo === 'monumento' ? 'lugar' : rawTipo;
            const ref = (parts.length === 2) ? parts[1] : '';

            const $row = $(
                '<div class="nica-item card mb-2">'
                + '<div class="card-body p-2">'
                + '<div class="d-flex gap-2 align-items-center">'
                + '<span class="badge text-bg-secondary nica-stop-order">0</span>'
                + '<select class="form-select form-select-sm nica-stop-tipo" style="max-width:150px">'
                + '<option value="lugar">Lugar</option>'
                + '<option value="comercio">Comercio</option>'
                + '</select>'
                + '<select class="form-select form-select-sm nica-stop-ref"></select>'
                + '<button type="button" class="btn btn-outline-danger nica-remove" title="Quitar"><i class="bi bi-x-lg"></i></button>'
                + '</div>'
                + '<input type="hidden" name="' + name + '[]" class="nica-stop-value">'
                + '</div></div>'
            );

            $row.find('.nica-stop-tipo').val(tipo);
            $row.find('.nica-stop-ref').data('current', ref);
            dynamicContainer(name).append($row);
            refreshStopRefs($row);
            renumberStops();
        }

        function renderDynamic(field, values) {
            const $container = dynamicContainer(field.name);
            if (!$container.length) {
                return;
            }

            clearDynamic($container);

            const list = Array.isArray(values) ? values : [];
            list.forEach(function (value) {
                if (field.type === 'stringlist') {
                    addStringRow(field.name, String(value));
                } else if (field.type === 'images') {
                    addImageRow(field.name, String(value));
                } else if (field.type === 'stops') {
                    addStopRow(field.name, String(value));
                }
            });
        }

        // ------------------------------------------------------------------
        // Imagen única (tipo "image")
        // ------------------------------------------------------------------
        function syncImagePreview($input) {
            const url = ($input.val() || '').toString().trim();
            const $field = $input.closest('.nica-image-field');
            if (!$field.length) {
                return;
            }

            const $wrap = $field.find('.nica-image-preview-wrap');
            const $preview = $field.find('.nica-image-preview');

            if (!url) {
                $wrap.addClass('d-none');
                $preview.attr('src', '');
                return;
            }

            $preview.attr('src', url);
            $wrap.removeClass('d-none');
        }

        // ------------------------------------------------------------------
        // Categorías dependientes (categoría superior -> subcategoría)
        // ------------------------------------------------------------------
        const referenceOptionsFor = (name) => (cfg.references && cfg.references[name]) ? cfg.references[name] : [];

        const dependentParentNames = () => Array.from(new Set(
            cfg.fields
                .filter((field) => field.type === 'reference' && field.dependsOn)
                .map((field) => field.dependsOn)
        ));

        function fillReferenceSelect($select, options, selected) {
            $select.empty().append('<option value="">— Seleccionar —</option>');
            options.forEach((option) => {
                $select.append($('<option>').attr('value', option.value).text(option.label));
            });
            if (selected !== undefined && selected !== null) {
                $select.val(selected);
            }
        }

        /** Muestra en las subcategorías solo las hijas de la categoría superior elegida. */
        function refreshDependentSelects(parentName) {
            const dependents = cfg.fields.filter(
                (field) => field.type === 'reference' && field.dependsOn === parentName
            );
            if (!dependents.length) {
                return;
            }

            const parentValue = ($form.find('[name="' + parentName + '"]').val() || '').toString().trim();

            dependents.forEach((field) => {
                const $select = $form.find('[name="' + field.name + '"]');
                if (!$select.length) {
                    return;
                }

                const current = ($select.val() || '').toString().trim();
                // Sin categoría superior no se ofrecen subcategorías (ni la raíz).
                const options = parentValue === ''
                    ? []
                    : referenceOptionsFor(field.name)
                        .filter((option) => (option.parent || '') === parentValue);

                fillReferenceSelect($select, options, null);
                if (current && options.some((option) => option.value === current)) {
                    $select.val(current);
                }
            });
        }

        /** Al editar, si falta la categoría superior pero hay subcategoría, se infiere del catálogo. */
        function inferParentFromDependent() {
            cfg.fields
                .filter((field) => field.type === 'reference' && field.dependsOn)
                .forEach((field) => {
                    const $select = $form.find('[name="' + field.name + '"]');
                    const $parent = $form.find('[name="' + field.dependsOn + '"]');
                    if (!$select.length || !$parent.length) {
                        return;
                    }

                    const current = ($select.val() || '').toString().trim();
                    const parentValue = ($parent.val() || '').toString().trim();
                    if (!current || parentValue) {
                        return;
                    }

                    const option = referenceOptionsFor(field.name).find((item) => item.value === current);
                    if (option && option.parent) {
                        $parent.val(option.parent);
                    }
                });
        }

        // ------------------------------------------------------------------
        // Rellenar / limpiar formulario
        // ------------------------------------------------------------------
        function resetForm() {
            $form[0].reset();
            $currentId.val('');
            $errors.addClass('d-none').html('');
            $('.nica-image-preview-wrap').addClass('d-none');
            $('.nica-image-preview').attr('src', '');
            $('.nica-image-file').val('');

            if ($docId.length) {
                $docId.prop('disabled', false).prop('required', cfg.id && cfg.id.strategy === 'manual');
            }

            if (idFieldName) {
                $form.find('[name="' + idFieldName + '"]').prop('readonly', false);
            }
        }

        function fillForm(raw) {
            cfg.fields.forEach(function (field) {
                if (['stringlist', 'images', 'stops'].includes(field.type)) {
                    renderDynamic(field, raw ? raw[field.name] : []);
                    return;
                }

                const $el = $form.find('[name="' + field.name + '"]');
                if (!$el.length) {
                    return;
                }

                const value = raw ? raw[field.name] : undefined;

                if (field.type === 'bool') {
                    $el.prop('checked', value === true || value === 'true' || value === 1 || value === '1');
                } else if ($el.is('select') && value !== null && value !== undefined && value !== '') {
                    const normalized = String(value);
                    const exists = $el.find('option').filter(function () {
                        return this.value === normalized;
                    }).length > 0;

                    if (!exists) {
                        // Conserva valores heredados que aún no están en el catálogo.
                        $el.append($('<option>').attr('value', normalized).text(normalized));
                    }

                    $el.val(normalized);
                } else {
                    $el.val((value === null || value === undefined) ? '' : value);
                }
            });

            cfg.fields.forEach(function (field) {
                if (field.type === 'image') {
                    syncImagePreview($form.find('[name="' + field.name + '"]'));
                }
            });

            inferParentFromDependent();
            dependentParentNames().forEach(function (parentName) {
                refreshDependentSelects(parentName);
            });
        }

        function showError(xhr) {
            const data = (xhr && xhr.responseJSON) ? xhr.responseJSON : {};

            if (Array.isArray(data.errors) && data.errors.length) {
                $errors
                    .html('<ul class="mb-0">' + data.errors.map((item) => '<li>' + escapeHtml(item) + '</li>').join('') + '</ul>')
                    .removeClass('d-none');
                return;
            }

            Toast.fire({ icon: 'error', title: data.error || data.message || 'Ocurrió un error.' });
        }

        // ------------------------------------------------------------------
        // Eventos
        // ------------------------------------------------------------------
        function openNew() {
            resetForm();
            fillForm(null);
            $('#nica-modal-title').text('Nuevo ' + cfg.singular);
            modal.show();
        }

        $('#nica-btn-new').on('click', openNew);

        $('#nica-table').on('click', '.nica-empty-new', openNew);

        $('#nica-table').on('click', '.nica-edit', function () {
            const row = table.row($(this).closest('tr')).data();
            resetForm();
            fillForm(row._raw || {});
            $currentId.val(row.id);
            $('#nica-modal-title').text('Editar ' + cfg.singular);

            if ($docId.length) {
                $docId.val(row.id).prop('disabled', true);
            }

            if (idFieldName) {
                $form.find('[name="' + idFieldName + '"]').prop('readonly', true);
            }

            modal.show();
        });

        $('#nica-table').on('click', '.nica-delete', function () {
            const id = $(this).data('id');

            Swal.fire({
                title: '¿Eliminar registro?',
                text: 'Se eliminará «' + id + '» de Firestore. Esta acción no se puede deshacer.',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Sí, eliminar',
                cancelButtonText: 'Cancelar',
                confirmButtonColor: '#f16a6a',
                cancelButtonColor: '#1d2622',
            }).then((result) => {
                if (!result.isConfirmed) {
                    return;
                }

                $.ajax({ url: cfg.urls.delete, method: 'POST', data: { id: id } })
                    .done(function (response) {
                        Toast.fire({ icon: 'success', title: response.message || 'Registro eliminado.' });
                        table.ajax.reload(null, false);
                    })
                    .fail(showError);
            });
        });

        // Añadir elementos a editores dinámicos.
        $(document).on('click', '.nica-add', function () {
            const name = $(this).data('name');
            const type = $(this).data('type');

            if (type === 'images') {
                addImageRow(name, '');
            } else if (type === 'stops') {
                addStopRow(name, '');
            } else {
                addStringRow(name, '');
            }
        });

        // Quitar fila dinámica.
        $(document).on('click', '.nica-remove', function () {
            $(this).closest('.nica-item').remove();
            renumberStops();
        });

        // Subida incremental de imágenes del carrusel/galería.
        $(document).on('click', '.nica-img-choose', function () {
            $(this).closest('.nica-item').find('.nica-img-file').trigger('click');
        });

        $(document).on('change', '.nica-img-file', function () {
            const file = this.files && this.files[0];
            if (!file) {
                return;
            }
            uploadImage(file, $(this).closest('.nica-item'));
        });

        // Actualiza el preview al editar la URL manualmente.
        $(document).on('input', '.nica-url', function () {
            const value = ($(this).val() || '').trim();
            $(this).closest('.nica-item').find('.nica-thumb').attr('src', value).toggleClass('d-none', !value);
        });

        // Paradas: tipo y referencia.
        $(document).on('change', '.nica-stop-tipo', function () {
            const $row = $(this).closest('.nica-item');
            $row.find('.nica-stop-ref').data('current', '');
            refreshStopRefs($row);
        });

        $(document).on('change', '.nica-stop-ref', function () {
            updateStopValue($(this).closest('.nica-item'));
        });

        // Imagen única: botón y archivo.
        $(document).on('click', '.nica-image-choose', function () {
            const target = $(this).data('target');
            $('#' + target).closest('.input-group').next('.nica-image-file').trigger('click');
        });

        $(document).on('change', '.nica-image-file', function () {
            const file = this.files && this.files[0];
            if (!file) {
                return;
            }

            const target = $(this).data('target');
            const formData = new FormData();
            formData.append('image', file);

            Toast.fire({ icon: 'info', title: 'Subiendo imagen...' });

            $.ajax({
                url: cfg.urls.upload,
                method: 'POST',
                data: formData,
                processData: false,
                contentType: false,
            })
                .done(function (response) {
                    $('#' + target).val(response.url);
                    syncImagePreview($('#' + target));
                    Toast.fire({ icon: 'success', title: 'Imagen subida.' });
                })
                .fail(showError);
        });

        // Quitar la imagen seleccionada para poder elegir otra.
        $(document).on('click', '.nica-image-remove', function () {
            const $field = $(this).closest('.nica-image-field');
            $field.find('input[type="text"]').val('');
            $field.find('.nica-image-file').val('');
            $field.find('.nica-image-preview-wrap').addClass('d-none');
            $field.find('.nica-image-preview').attr('src', '');
        });

        $(document).on('input change', '#nica-form input[type="text"]', function () {
            const name = $(this).attr('name');
            const field = cfg.fields.find((f) => f.name === name);
            if (field && field.type === 'image') {
                syncImagePreview($(this));
            }
        });

        // Al cambiar una categoría superior, se filtran sus subcategorías.
        $form.on('change', 'select', function () {
            refreshDependentSelects($(this).attr('name'));
        });

        // Al elegir una ciudad, refleja su nombre y filtra las paradas de la ruta.
        $form.on('change', 'select[name="cityId"]', function () {
            const $ciudad = $form.find('[name="ciudad"]');
            if ($ciudad.length && $(this).val()) {
                $ciudad.val($(this).find('option:selected').text().trim());
            }

            $form.find('.nica-stop-tipo').each(function () {
                refreshStopRefs($(this).closest('.nica-item'));
            });
        });

        // ------------------------------------------------------------------
        // Selector de ubicación (mapa Leaflet + búsqueda Nominatim)
        // ------------------------------------------------------------------
        let nicaMap = null;
        let nicaMarker = null;

        function writeCoords(lat, lng) {
            $form.find('[name="latitud"]').val(Number(lat).toFixed(6));
            $form.find('[name="longitud"]').val(Number(lng).toFixed(6));
        }

        function placeMarker(lat, lng, focus) {
            if (!nicaMap) {
                return;
            }

            const latlng = [Number(lat), Number(lng)];

            if (nicaMarker) {
                nicaMarker.setLatLng(latlng);
            } else {
                nicaMarker = window.L.marker(latlng, { draggable: true }).addTo(nicaMap);
                nicaMarker.on('dragend', function () {
                    const point = nicaMarker.getLatLng();
                    writeCoords(point.lat, point.lng);
                });
            }

            if (focus) {
                nicaMap.setView(latlng, Math.max(nicaMap.getZoom(), 15));
            }

            writeCoords(latlng[0], latlng[1]);
        }

        function initLocationPicker() {
            if (!cfg.hasLocation) {
                return;
            }

            const container = document.getElementById('nica-map');
            if (!container || !window.L) {
                return;
            }

            const lat = parseFloat($form.find('[name="latitud"]').val());
            const lng = parseFloat($form.find('[name="longitud"]').val());
            const hasCoords = Number.isFinite(lat) && Number.isFinite(lng);
            const center = hasCoords ? [lat, lng] : [12.1069, -85.3667];
            const zoom = hasCoords ? 15 : 7;

            if (!nicaMap) {
                nicaMap = window.L.map(container, { scrollWheelZoom: true }).setView(center, zoom);
                window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '&copy; OpenStreetMap',
                }).addTo(nicaMap);
                nicaMap.on('click', function (event) {
                    placeMarker(event.latlng.lat, event.latlng.lng, false);
                });
            } else {
                nicaMap.setView(center, zoom);
                if (nicaMarker) {
                    nicaMap.removeLayer(nicaMarker);
                    nicaMarker = null;
                }
            }

            if (hasCoords) {
                placeMarker(lat, lng, false);
            }

            setTimeout(function () {
                nicaMap.invalidateSize();
            }, 160);
        }

        function searchPlace(query) {
            const q = String(query || '').trim();
            if (!q || !nicaMap) {
                return;
            }

            const url = 'https://nominatim.openstreetmap.org/search?format=json&limit=1&q=' + encodeURIComponent(q);
            fetch(url, { headers: { Accept: 'application/json' } })
                .then((response) => response.json())
                .then((data) => {
                    if (Array.isArray(data) && data.length) {
                        placeMarker(parseFloat(data[0].lat), parseFloat(data[0].lon), true);
                    } else {
                        Toast.fire({ icon: 'warning', title: 'Sin resultados para esa búsqueda.' });
                    }
                })
                .catch(() => Toast.fire({ icon: 'error', title: 'No se pudo buscar la dirección.' }));
        }

        document.getElementById('nica-modal').addEventListener('shown.bs.modal', initLocationPicker);

        $(document).on('click', '#nica-map-search-btn', function () {
            searchPlace($('#nica-map-search').val());
        });

        $(document).on('keydown', '#nica-map-search', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                searchPlace($(this).val());
            }
        });

        // Envío del formulario.
        $form.on('submit', function (event) {
            event.preventDefault();

            const formData = new FormData($form[0]);

            cfg.fields.forEach((field) => {
                if (field.type === 'bool') {
                    const el = $form.find('[name="' + field.name + '"]')[0];
                    formData.set(field.name, el && el.checked ? '1' : '0');
                }
            });

            if ($docId.length && $docId.prop('disabled')) {
                formData.delete('_id');
            }

            $.ajax({
                url: cfg.urls.save,
                method: 'POST',
                data: formData,
                processData: false,
                contentType: false,
            })
                .done(function (response) {
                    modal.hide();
                    Toast.fire({ icon: 'success', title: response.message || 'Guardado.' });
                    table.ajax.reload(null, false);
                })
                .fail(showError);
        });
    });
})(jQuery);
