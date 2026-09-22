<?php

namespace Config;

/**
 * Definición declarativa de las colecciones de Firestore que administra el backend.
 *
 * A partir de este mapa se generan automáticamente:
 *  - los endpoints REST (/api/v1/{clave})
 *  - las tablas DataTables y los formularios modales del panel web
 *
 * Tipos de campo soportados:
 *  string, text, email, url, int, float, bool, enum, list, image, timestamp, reference
 *
 * Campo de tipo "reference":
 *  Almacena el valor de otro documento (normalmente un id) y en el panel se
 *  dibuja como un select con las opciones de la colección indicada:
 *   'collection'  => colección de origen de las opciones
 *   'value_field' => campo que se guarda ('id' para el id del documento)
 *   'label_field' => campo que se muestra al usuario
 *
 * Estructura de una ciudad (plantilla: Juigalpa)
 * -------------------------------------------------
 * El documento de una ciudad contiene su identidad y sus recursos visuales:
 *   - portada (imagenUrl): imagen principal que se muestra en tarjetas.
 *   - galeria: lista de URLs para el carrusel de la ciudad.
 * Y se relaciona con:
 *   - lugares/{lugarId}         -> lugares y sitios turísticos (cityId)
 *   - comercios/{id}            -> comercios locales (cityId/ciudad)
 *   - rutas/{rutaId}            -> rutas turísticas (cityId + paradas)
 */
class NicaResources
{
    /**
     * @return array<string, array<string, mixed>>
     */
    public function all(): array
    {
        return [
            'ciudades' => [
                'collection'   => 'ciudades',
                'label'        => 'Ciudades',
                'singular'     => 'Ciudad',
                'icon'         => 'bi-buildings',
                'description'  => 'Identidad, historia e imágenes de cada ciudad.',
                'id'           => ['strategy' => 'auto', 'slug_source' => 'nombre', 'label' => 'ID', 'editable' => false],
                'title_field'  => 'nombre',
                'search_fields' => ['nombre', 'descripcion', 'departamento'],
                'order_by'     => ['nombre', 'asc'],
                'fields'       => [
                    'nombre'        => ['label' => 'Nombre', 'type' => 'string', 'required' => true, 'list' => true],
                    'lema'          => ['label' => 'Lema / frase corta', 'type' => 'string', 'list' => true],
                    'descripcion'   => ['label' => 'Descripción corta', 'type' => 'text', 'list' => true],
                    'historia'      => ['label' => 'Historia', 'type' => 'text'],
                    'departamento'  => ['label' => 'Departamento', 'type' => 'string', 'list' => true],
                    'imagenUrl'     => ['label' => 'Imagen de portada', 'type' => 'image', 'list' => true],
                    'galeria'       => ['label' => 'Carrusel de imágenes', 'type' => 'images'],
                    'imagenKey'     => ['label' => 'Imagen local (clave, legado)', 'type' => 'string', 'internal' => true],
                    'latitud'       => ['label' => 'Latitud', 'type' => 'float'],
                    'longitud'      => ['label' => 'Longitud', 'type' => 'float'],
                    'gradientStart' => ['label' => 'Gradiente inicio (int)', 'type' => 'int'],
                    'gradientEnd'   => ['label' => 'Gradiente fin (int)', 'type' => 'int'],
                    'monumentCount' => ['label' => 'N.º de lugares', 'type' => 'int', 'default' => 0],
                    'orden'         => ['label' => 'Orden', 'type' => 'int', 'default' => 0],
                    'activo'        => ['label' => 'Activa', 'type' => 'bool', 'list' => true, 'default' => true],
                ],
            ],

            'categorias_lugares' => [
                'collection'   => 'categorias_lugares',
                'label'        => 'Categorías de lugares',
                'singular'     => 'Categoría',
                'icon'         => 'bi-tags',
                'description'  => 'Catálogo jerárquico: categoría superior y subcategorías de los lugares.',
                'id'           => ['strategy' => 'auto', 'slug_source' => 'nombre', 'label' => 'ID', 'editable' => false],
                'title_field'  => 'nombre',
                'search_fields' => ['nombre', 'categoriaPadre', 'descripcion'],
                'order_by'     => ['nombre', 'asc'],
                'fields'       => [
                    'nombre'         => ['label' => 'Nombre', 'type' => 'string', 'required' => true, 'list' => true],
                    'categoriaPadre' => ['label' => 'Categoría superior', 'type' => 'reference', 'collection' => 'categorias_lugares', 'value_field' => 'nombre', 'label_field' => 'nombre', 'only_root' => true, 'list' => true],
                    'descripcion'    => ['label' => 'Descripción', 'type' => 'text'],
                    'icono'       => ['label' => 'Icono (clase Bootstrap Icons)', 'type' => 'string'],
                    'orden'       => ['label' => 'Orden', 'type' => 'int', 'default' => 0, 'list' => true],
                    'activo'      => ['label' => 'Activa', 'type' => 'bool', 'list' => true, 'default' => true],
                ],
            ],

            'categorias_comercios' => [
                'collection'   => 'categorias_comercios',
                'label'        => 'Categorías de comercios',
                'singular'     => 'Categoría',
                'icon'         => 'bi-tags-fill',
                'description'  => 'Catálogo jerárquico: categoría superior y subcategorías de los comercios.',
                'id'           => ['strategy' => 'auto', 'slug_source' => 'nombre', 'label' => 'ID', 'editable' => false],
                'title_field'  => 'nombre',
                'search_fields' => ['nombre', 'categoriaPadre', 'descripcion'],
                'order_by'     => ['nombre', 'asc'],
                'fields'       => [
                    'nombre'         => ['label' => 'Nombre', 'type' => 'string', 'required' => true, 'list' => true],
                    'categoriaPadre' => ['label' => 'Categoría superior', 'type' => 'reference', 'collection' => 'categorias_comercios', 'value_field' => 'nombre', 'label_field' => 'nombre', 'only_root' => true, 'list' => true],
                    'descripcion'    => ['label' => 'Descripción', 'type' => 'text'],
                    'icono'          => ['label' => 'Icono (clase Bootstrap Icons)', 'type' => 'string'],
                    'orden'          => ['label' => 'Orden', 'type' => 'int', 'default' => 0, 'list' => true],
                    'activo'         => ['label' => 'Activa', 'type' => 'bool', 'list' => true, 'default' => true],
                ],
            ],

            'lugares' => [
                'collection'   => 'lugares',
                'label'        => 'Lugares',
                'singular'     => 'Lugar',
                'icon'         => 'bi-geo-alt',
                'description'  => 'Lugares y sitios turísticos de cada ciudad.',
                'id'           => ['strategy' => 'auto', 'slug_source' => 'nombre', 'label' => 'ID', 'editable' => false],
                'title_field'  => 'nombre',
                'search_fields' => ['nombre', 'ciudad', 'categoria', 'cityId'],
                'order_by'     => ['nombre', 'asc'],
                'fields'       => [
                    'nombre'               => ['label' => 'Nombre', 'type' => 'string', 'required' => true, 'list' => true],
                    'ciudad'               => ['label' => 'Ciudad (texto visible)', 'type' => 'string', 'required' => true, 'list' => true],
                    'cityId'               => ['label' => 'Ciudad', 'type' => 'reference', 'collection' => 'ciudades', 'value_field' => 'id', 'label_field' => 'nombre', 'required' => true],
                    'categoriaPadre'       => ['label' => 'Categoría superior', 'type' => 'reference', 'collection' => 'categorias_lugares', 'value_field' => 'nombre', 'label_field' => 'nombre', 'only_root' => true],
                    'categoria'            => ['label' => 'Subcategoría', 'type' => 'reference', 'collection' => 'categorias_lugares', 'value_field' => 'nombre', 'label_field' => 'nombre', 'depends_on' => 'categoriaPadre', 'list' => true],
                    'afluencia'            => ['label' => 'Afluencia', 'type' => 'enum', 'list' => true, 'options' => ['BAJA', 'MODERADA', 'ALTA']],
                    'descripcion'          => ['label' => 'Descripción', 'type' => 'text'],
                    'historia'             => ['label' => 'Historia', 'type' => 'text'],
                    'anioConstruccion'     => ['label' => 'Año de construcción', 'type' => 'string', 'list' => true],
                    'modeloUnity'          => ['label' => 'Modelo Unity (prefab)', 'type' => 'string', 'list' => true],
                    'imagenUrl'            => ['label' => 'Imagen del lugar', 'type' => 'image', 'list' => true],
                    'galeria'              => ['label' => 'Galería de imágenes', 'type' => 'images'],
                    'imagenKey'            => ['label' => 'Imagen local (clave, legado)', 'type' => 'string', 'internal' => true],
                    'consejosResponsables' => ['label' => 'Consejos de turismo responsable', 'type' => 'stringlist'],
                    'latitud'              => ['label' => 'Latitud', 'type' => 'float'],
                    'longitud'             => ['label' => 'Longitud', 'type' => 'float'],
                    'gradientStart'        => ['label' => 'Gradiente inicio (int)', 'type' => 'int'],
                    'gradientEnd'          => ['label' => 'Gradiente fin (int)', 'type' => 'int'],
                    'orden'                => ['label' => 'Orden', 'type' => 'int', 'default' => 0],
                    'activo'               => ['label' => 'Activo', 'type' => 'bool', 'list' => true, 'default' => true],
                ],
            ],

            'rutas' => [
                'collection'   => 'rutas',
                'label'        => 'Rutas turísticas',
                'singular'     => 'Ruta',
                'icon'         => 'bi-signpost-split',
                'description'  => 'Recorridos con paradas en lugares y comercios de una ciudad.',
                'id'           => ['strategy' => 'auto', 'slug_source' => 'nombre', 'label' => 'ID', 'editable' => false],
                'title_field'  => 'nombre',
                'search_fields' => ['nombre', 'cityId', 'descripcion'],
                'order_by'     => ['nombre', 'asc'],
                'fields'       => [
                    'cityId'           => ['label' => 'Ciudad', 'type' => 'reference', 'collection' => 'ciudades', 'value_field' => 'id', 'label_field' => 'nombre', 'required' => true],
                    'nombre'           => ['label' => 'Nombre', 'type' => 'string', 'required' => true, 'list' => true],
                    'descripcion'      => ['label' => 'Descripción', 'type' => 'text', 'list' => true],
                    'duracionEstimada' => ['label' => 'Duración estimada', 'type' => 'string', 'list' => true],
                    'notaDuracion'     => ['label' => 'Nota sobre la duración', 'type' => 'text'],
                    'objetivos'        => ['label' => 'Objetivos', 'type' => 'stringlist'],
                    'paradas'          => ['label' => 'Paradas de la ruta', 'type' => 'stops'],
                    'imagenUrl'        => ['label' => 'Imagen de la ruta', 'type' => 'image', 'list' => true],
                    'orden'            => ['label' => 'Orden', 'type' => 'int', 'default' => 0],
                    'activo'           => ['label' => 'Activa', 'type' => 'bool', 'list' => true, 'default' => true],
                ],
            ],

            'comercios' => [
                'collection'   => 'comercios',
                'label'        => 'Comercios',
                'singular'     => 'Comercio',
                'icon'         => 'bi-shop',
                'description'  => 'Restaurantes, cafeterías y negocios locales.',
                'id'           => ['strategy' => 'auto', 'slug_source' => 'nombre', 'label' => 'ID', 'editable' => false],
                'title_field'  => 'nombre',
                'search_fields' => ['nombre', 'categoria', 'ciudad'],
                'order_by'     => ['nombre', 'asc'],
                'fields'       => [
                    'nombre'        => ['label' => 'Nombre', 'type' => 'string', 'required' => true, 'list' => true],
                    'categoriaPadre' => ['label' => 'Categoría superior', 'type' => 'reference', 'collection' => 'categorias_comercios', 'value_field' => 'nombre', 'label_field' => 'nombre', 'only_root' => true],
                    'categoria'     => ['label' => 'Subcategoría', 'type' => 'reference', 'collection' => 'categorias_comercios', 'value_field' => 'nombre', 'label_field' => 'nombre', 'depends_on' => 'categoriaPadre', 'list' => true],
                    'ciudad'        => ['label' => 'Ciudad (texto visible)', 'type' => 'string', 'list' => true],
                    'cityId'        => ['label' => 'Ciudad', 'type' => 'reference', 'collection' => 'ciudades', 'value_field' => 'id', 'label_field' => 'nombre'],
                    'descripcion'   => ['label' => 'Descripción', 'type' => 'text'],
                    'direccion'     => ['label' => 'Dirección', 'type' => 'string'],
                    'horario'       => ['label' => 'Horario', 'type' => 'string'],
                    'imagenUrl'     => ['label' => 'Imagen del comercio', 'type' => 'image', 'list' => true],
                    'galeria'       => ['label' => 'Galería de imágenes', 'type' => 'images'],
                    'latitud'       => ['label' => 'Latitud', 'type' => 'float'],
                    'longitud'      => ['label' => 'Longitud', 'type' => 'float'],
                    'telefono'      => ['label' => 'Teléfono', 'type' => 'string'],
                    'whatsapp'      => ['label' => 'WhatsApp', 'type' => 'string'],
                    'tieneWhatsapp' => ['label' => 'Tiene WhatsApp', 'type' => 'bool', 'list' => true, 'default' => false],
                    'logoUrl'       => ['label' => 'Logo', 'type' => 'image'],
                    'diasAtencion'  => ['label' => 'Días de atención', 'type' => 'string'],
                    'correo'        => ['label' => 'Correo de contacto', 'type' => 'email', 'list' => true],
                    'redesSociales' => ['label' => 'Redes sociales (red|url)', 'type' => 'stringlist'],
                    'servicios'     => ['label' => 'Servicios', 'type' => 'stringlist'],
                    'productos'     => ['label' => 'Productos', 'type' => 'stringlist'],
                    'infoAdicional' => ['label' => 'Información adicional', 'type' => 'text'],
                    'orden'         => ['label' => 'Orden', 'type' => 'int', 'default' => 0],
                    // Requiere aprobación del administrador para publicarse.
                    'aprobado'      => ['label' => 'Aprobado', 'type' => 'bool', 'list' => true, 'default' => true],
                    'activo'        => ['label' => 'Activo', 'type' => 'bool', 'list' => true, 'default' => true],
                    // Cuenta propietaria (se asigna desde la app, no se edita en el panel).
                    'propietarioUid' => ['label' => 'Propietario (uid)', 'type' => 'string', 'internal' => true],
                ],
            ],

            'solicitudes_comercios' => [
                'collection'   => 'solicitudes_comercios',
                'label'        => 'Solicitudes de comercios',
                'singular'     => 'Solicitud',
                'icon'         => 'bi-envelope-paper',
                'description'  => 'Solicitudes de incorporación enviadas desde la app.',
                'id'           => ['strategy' => 'auto', 'label' => 'ID (opcional)', 'editable' => false],
                'title_field'  => 'nombreNegocio',
                'search_fields' => ['nombreNegocio', 'ciudad', 'nombreResponsable', 'correoResponsable'],
                'order_by'     => ['fechaSolicitud', 'desc'],
                'fields'       => [
                    'nombreNegocio'     => ['label' => 'Negocio', 'type' => 'string', 'required' => true, 'list' => true],
                    'ciudad'            => ['label' => 'Ciudad (texto visible)', 'type' => 'string', 'list' => true],
                    'cityId'            => ['label' => 'Ciudad', 'type' => 'reference', 'collection' => 'ciudades', 'value_field' => 'id', 'label_field' => 'nombre'],
                    'categoria'         => ['label' => 'Categoría', 'type' => 'string', 'list' => true],
                    'direccion'         => ['label' => 'Dirección', 'type' => 'string'],
                    'descripcion'       => ['label' => 'Descripción', 'type' => 'text'],
                    'telefono'          => ['label' => 'Teléfono', 'type' => 'string'],
                    'whatsapp'          => ['label' => 'WhatsApp', 'type' => 'string'],
                    'horario'           => ['label' => 'Horario', 'type' => 'string'],
                    'nombreResponsable' => ['label' => 'Responsable', 'type' => 'string', 'list' => true],
                    'correoResponsable' => ['label' => 'Correo', 'type' => 'email', 'list' => true],
                    'redesSociales'     => ['label' => 'Redes sociales', 'type' => 'string'],
                    'estado'            => ['label' => 'Estado', 'type' => 'enum', 'list' => true, 'options' => ['pendiente', 'aprobada', 'rechazada'], 'default' => 'pendiente'],
                    'fechaSolicitud'    => ['label' => 'Fecha de solicitud', 'type' => 'timestamp', 'list' => true, 'server' => true],
                    'userId'            => ['label' => 'userId', 'type' => 'string', 'list' => true, 'server' => true],
                ],
            ],

            'usuarios' => [
                'collection'   => 'usuarios',
                'label'        => 'Usuarios',
                'singular'     => 'Usuario',
                'icon'         => 'bi-people',
                'description'  => 'Perfiles y roles de los usuarios registrados.',
                'id'           => ['strategy' => 'field', 'field' => 'uid', 'label' => 'uid', 'editable' => false],
                'title_field'  => 'nombre',
                'search_fields' => ['nombre', 'correo', 'uid'],
                'order_by'     => ['nombre', 'asc'],
                'fields'       => [
                    'uid'           => ['label' => 'uid', 'type' => 'string', 'required' => true, 'list' => true],
                    'nombre'        => ['label' => 'Nombre', 'type' => 'string', 'required' => true, 'list' => true],
                    'correo'        => ['label' => 'Correo', 'type' => 'email', 'list' => true],
                    'rol'           => ['label' => 'Rol', 'type' => 'enum', 'list' => true, 'options' => ['USUARIO', 'ADMIN', 'EDITOR', 'AUDITOR'], 'default' => 'USUARIO'],
                    'fechaRegistro' => ['label' => 'Fecha de registro', 'type' => 'timestamp', 'list' => true, 'server' => true],
                ],
            ],
        ];
    }

    /**
     * @return array<string, mixed>|null
     */
    public function get(string $key): ?array
    {
        return $this->all()[$key] ?? null;
    }

    /**
     * @return list<string>
     */
    public function keys(): array
    {
        return array_keys($this->all());
    }
}
