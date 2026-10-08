<?php
/**
 * Landing pública de NicaExplore.
 *
 * Página promocional de la app Android servida en la raíz del dominio (/).
 * Es autónoma: no usa el layout del panel ni Bootstrap, solo sus propios
 * assets (assets/landing.css y assets/landing.js) para mantenerse ligera.
 *
 * Variables esperadas:
 *   @var string $apkUrl   URL de descarga del APK (GitHub Releases).
 *   @var string $repoUrl  URL del repositorio del proyecto.
 *   @var string $version  Nombre de versión visible de la app.
 *   @var string $year     Año actual para el pie de página.
 */

$apkUrl  = $apkUrl  ?? 'https://github.com/Olivergg127/NicaExplorer-AndroidStudio/releases/download/v1.1.3/NicaExplorer.apk';
$repoUrl = $repoUrl ?? 'https://github.com/Olivergg127/NicaExplorer-AndroidStudio';
$version = $version ?? '1.1.3';
$year    = $year    ?? date('Y');

/** Iconos SVG en línea (trazo, 24x24) para no depender de librerías externas. */
$icon = static function (string $name, string $class = 'icon'): string {
    $paths = [
        'cube'      => '<path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><path d="m3.3 7 8.7 5 8.7-5"/><path d="M12 22V12"/>',
        'sparkles'  => '<path d="M9.937 15.5A2 2 0 0 0 8.5 14.063l-6.135-1.582a.5.5 0 0 1 0-.962L8.5 9.936A2 2 0 0 0 9.937 8.5l1.582-6.135a.5.5 0 0 1 .963 0L14.063 8.5A2 2 0 0 0 15.5 9.937l6.135 1.581a.5.5 0 0 1 0 .964L15.5 14.063a2 2 0 0 0-1.437 1.437l-1.582 6.135a.5.5 0 0 1-.963 0z"/><path d="M20 3v4"/><path d="M22 5h-4"/><path d="M4 17v2"/><path d="M5 18H3"/>',
        'map'       => '<path d="M14.106 5.553a2 2 0 0 0 1.788 0l3.659-1.83A1 1 0 0 1 21 4.619v12.764a1 1 0 0 1-.553.894l-4.553 2.277a2 2 0 0 1-1.788 0l-4.212-2.106a2 2 0 0 0-1.788 0l-3.659 1.83A1 1 0 0 1 3 19.381V6.618a1 1 0 0 1 .553-.894l4.553-2.277a2 2 0 0 1 1.788 0z"/><path d="M15 5.764v15"/><path d="M9 3.236v15"/>',
        'store'     => '<path d="M3 9l1.5-5h15L21 9"/><path d="M3 9v10a1 1 0 0 0 1 1h16a1 1 0 0 0 1-1V9"/><path d="M3 9h18"/><path d="M9 20v-5h6v5"/>',
        'route'     => '<circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/>',
        'star'      => '<path d="M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.123 2.123 0 0 0 1.595 1.16l5.166.756a.53.53 0 0 1 .294.904l-3.736 3.638a2.123 2.123 0 0 0-.611 1.878l.882 5.14a.53.53 0 0 1-.771.56l-4.618-2.428a2.122 2.122 0 0 0-1.973 0L6.396 21.01a.53.53 0 0 1-.77-.56l.881-5.139a2.122 2.122 0 0 0-.611-1.879L2.16 9.795a.53.53 0 0 1 .294-.906l5.165-.755a2.122 2.122 0 0 0 1.597-1.16z"/>',
        'bookmark'  => '<path d="m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z"/>',
        'gauge'     => '<path d="m12 14 4-4"/><path d="M3.34 19a10 10 0 1 1 17.32 0"/>',
        'compass'   => '<circle cx="12" cy="12" r="10"/><polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"/>',
        'download'  => '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" x2="12" y1="15" y2="3"/>',
        'arrow'     => '<line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/>',
        'menu'      => '<line x1="4" x2="20" y1="6" y2="6"/><line x1="4" x2="20" y1="12" y2="12"/><line x1="4" x2="20" y1="18" y2="18"/>',
        'close'     => '<path d="M18 6 6 18"/><path d="m6 6 12 12"/>',
        'check'     => '<polyline points="20 6 9 17 4 12"/>',
        'chevron'   => '<polyline points="6 9 12 15 18 9"/>',
        'github'    => '<path d="M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4"/><path d="M9 18c-4.51 2-5-2-7-2"/>',
        'shield'    => '<path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/><path d="m9 12 2 2 4-4"/>',
        'layers'    => '<path d="M12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83Z"/><path d="m22 17.65-9.17 4.16a2 2 0 0 1-1.66 0L2 17.65"/><path d="m22 12.65-9.17 4.16a2 2 0 0 1-1.66 0L2 12.65"/>',
        'heart'     => '<path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/>',
        'smartphone'=> '<rect width="14" height="20" x="5" y="2" rx="2" ry="2"/><path d="M12 18h.01"/>',
        'android'   => '<path d="M6 10v7a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2v-7"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/><path d="M4 10h16"/><path d="m8 3 1.2 2.2"/><path d="m16 3-1.2 2.2"/>',
        'whatsapp'  => '<path d="M20.5 11.6A8.4 8.4 0 0 1 7 19.3l-4 1 1.1-3.8a8.4 8.4 0 1 1 16.4-4.9Z"/><path d="M9 9.5c0 3 2.5 5.5 5.5 5.5.6 0 1.2-.6 1.2-1.2l-1.6-.9-1 1a5.6 5.6 0 0 1-2.2-2.2l1-1-.9-1.6c-.6 0-1.2.6-1.2 1.2Z"/>',
    ];
    $body = $paths[$name] ?? '';
    return '<svg class="' . esc($class, 'attr') . '" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' . $body . '</svg>';
};

$features = [
    ['cube',      'Visor 3D de monumentos',      'Recorre cada monumento desde todos los ángulos con el visor 3D: gira, acerca, restablece y sal.'],
    ['sparkles',  'Asistente Itzae con IA',      'Conversa con Itzae y resuelve tus dudas sobre monumentos, historia y comercios con contexto local.'],
    ['map',       'Mapa turístico',              'Explora el mapa con MapLibre y OpenStreetMap: pines de lugares y comercios, filtros y tu ubicación.'],
    ['store',     'Comercios locales',           'Descubre restaurantes y emprendimientos con contacto por WhatsApp, correo, redes y Google Maps.'],
    ['route',     'Rutas turísticas',            'Sigue rutas culturales con paradas numeradas y trazado por carretera para no perderte nada.'],
    ['star',      'Recomendados',                'Encuentra los lugares y comercios mejor valorados por la comunidad, ordenados por estrellas.'],
    ['bookmark',  'Guardados e historial',       'Guarda tus lugares favoritos y vuelve a tu historial de exploración cuando quieras.'],
    ['gauge',     'Afluencia estimada',          'Consulta el nivel de afluencia (baja, moderada o alta) y descubre alternativas más tranquilas.'],
];

$steps = [
    ['01', 'Elige tu ciudad',    'Empieza por Juigalpa, León, Managua o Masaya y abre su catálogo de lugares.'],
    ['02', 'Explóralo todo',     'Lee la historia, el año, la categoría y la afluencia estimada de cada monumento.'],
    ['03', 'Míralo en 3D',       'Abre el visor 3D y recorre el monumento desde cualquier ángulo.'],
    ['04', 'Pregunta a Itzae',   'Chatea con el asistente y descubre comercios locales para tu visita.'],
];

$cities = [
    [
        'nombre' => 'Juigalpa',
        'dep'    => 'Chontales',
        'img'    => 'city-juigalpa.jpg',
        'mod'    => 'city--juigalpa',
        'texto'  => 'Corazón del trópico seco y cuna de tradiciones ganaderas. Su color chontaleño la distingue.',
    ],
    [
        'nombre' => 'León',
        'dep'    => 'León',
        'img'    => 'city-leon.jpg',
        'mod'    => 'city--leon',
        'texto'  => 'Ciudad colonial y universitaria, con la tumba de Rubén Darío y su imponente catedral.',
    ],
    [
        'nombre' => 'Managua',
        'dep'    => 'Managua',
        'img'    => 'city-managua.jpg',
        'mod'    => 'city--managua',
        'texto'  => 'La capital, con el Árbol de la Vida, la Campana de la Paz y la estatua de Rubén Darío.',
    ],
];

$commerces = [
    ['comercio-amerripizza.jpg', 'AmerriPizza',   'Pizzería · Juigalpa'],
    ['comercio-coffee-break.jpg', 'Coffee Break',  'Cafetería · Juigalpa'],
    ['comercio-mi-choza.jpg',     'Mi Choza',      'Restaurante · Juigalpa'],
];

$tech = ['Kotlin', 'Jetpack Compose', 'Material 3', 'Unity 6', 'MapLibre', 'OpenStreetMap', 'Firebase', 'IA Gemini'];

$faqs = [
    ['¿NicaExplore es gratis?', 'Sí. La descarga y el uso de la app son gratuitos. Es un proyecto turístico sin fines de lucro.'],
    ['¿Necesito registrarme para usarla?', 'No es obligatorio. Puedes continuar sin registrarte y explorar el catálogo. Al crear una cuenta desbloqueas tus lugares guardados, tu historial y tu perfil.'],
    ['¿En qué teléfonos funciona?', 'Requiere Android 11 o superior. El visor 3D y el mapa funcionan mejor en un teléfono físico (arquitectura ARM64).'],
    ['¿La afluencia es en tiempo real?', 'No. La afluencia estimada (baja, moderada o alta) es orientativa del prototipo y sirve para sugerir alternativas más tranquilas; no es GPS ni seguimiento en vivo.'],
    ['¿Hay rutas en todas las ciudades?', 'La ruta cultural ya está disponible en Juigalpa. En León y Managua verás "Rutas próximamente" mientras se cargan más comercios.'],
    ['¿Cómo instalo el APK?', 'Descárgalo con el botón de la app, abre el archivo y, si el sistema lo pide, habilita la instalación desde este origen.'],
];
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>NicaExplore · Turismo, cultura y tecnología de Nicaragua</title>
    <meta name="description" content="NicaExplore es la app turística de Nicaragua: explora ciudades y monumentos, míralos en 3D, conversa con el asistente Itzae y descubre comercios locales. Descárgala gratis para Android.">
    <meta name="theme-color" content="#070d0b">
    <link rel="canonical" href="<?= base_url('/') ?>">

    <meta property="og:type" content="website">
    <meta property="og:site_name" content="NicaExplore">
    <meta property="og:title" content="NicaExplore · Explora. Conecta. Inspira.">
    <meta property="og:description" content="Ciudades, monumentos en 3D, un asistente con IA y los comercios locales de Nicaragua en una sola app.">
    <meta property="og:image" content="<?= base_url('assets/img/nicaexplorer_logo.png') ?>">
    <meta property="og:locale" content="es_NI">
    <meta name="twitter:card" content="summary_large_image">

    <link rel="icon" href="<?= base_url('favicon.ico') ?>">
    <link rel="apple-touch-icon" href="<?= base_url('assets/img/nicaexplorer_isotipo.png') ?>">

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Fraunces:ital,opsz,wght@0,9..144,400..700;1,9..144,400..700&family=Inter:wght@400;500;600;700&display=swap">
    <link rel="stylesheet" href="<?= base_url('assets/landing.css') ?>">
</head>
<body>
    <a class="skip-link" href="#contenido">Saltar al contenido</a>

    <!-- ===================== Cabecera / Navegación ===================== -->
    <header class="site-header" id="inicio" data-header>
        <div class="shell nav">
            <a class="brand" href="#inicio" aria-label="NicaExplore, ir al inicio">
                <img class="brand__mark" src="<?= base_url('assets/img/nicaexplorer_isotipo.png') ?>" alt="" width="42" height="46">
                <span class="brand__name">Nica<span>Explore</span></span>
            </a>

            <nav class="nav__links" id="menu" aria-label="Navegación principal">
                <a href="#caracteristicas">Características</a>
                <a href="#como-funciona">Cómo funciona</a>
                <a href="#ciudades">Ciudades</a>
                <a href="#comercios">Comercios</a>
                <a href="#preguntas">Preguntas</a>
                <a class="btn btn--sm btn--primary nav__links-cta" href="<?= esc($apkUrl, 'attr') ?>"><?= $icon('download') ?> Descargar</a>
            </nav>

            <div class="nav__actions">
                <a class="btn btn--sm btn--primary nav__cta" href="<?= esc($apkUrl, 'attr') ?>"><?= $icon('download') ?> Descargar</a>
                <button class="nav__toggle" type="button" aria-controls="menu" aria-expanded="false" data-menu-toggle>
                    <span class="nav__toggle-open"><?= $icon('menu') ?></span>
                    <span class="nav__toggle-close"><?= $icon('close') ?></span>
                    <span class="sr-only">Abrir menú</span>
                </button>
            </div>
        </div>
    </header>

    <main id="contenido">
        <!-- ===================== Hero ===================== -->
        <section class="hero">
            <div class="hero__glow hero__glow--a" aria-hidden="true"></div>
            <div class="hero__glow hero__glow--b" aria-hidden="true"></div>
            <div class="shell hero__inner">
                <div class="hero__content reveal">
                    <span class="eyebrow"><span class="eyebrow__dot"></span> Turismo · Cultura · Tecnología de Nicaragua</span>
                    <h1 class="hero__title">
                        Explora. Conecta.
                        <span class="text-gradient">Inspira.</span>
                    </h1>
                    <p class="hero__lead">
                        <strong>NicaExplore</strong> reúne las ciudades y monumentos de Nicaragua,
                        un visor <strong>3D</strong>, el asistente con IA <strong>Itzae</strong> y los
                        <strong>comercios locales</strong> en una sola app para Android.
                    </p>

                    <div class="hero__actions">
                        <a class="btn btn--primary btn--lg" href="<?= esc($apkUrl, 'attr') ?>">
                            <?= $icon('android') ?> Descargar para Android
                        </a>
                        <a class="btn btn--ghost btn--lg" href="#caracteristicas">
                            Ver características <?= $icon('arrow') ?>
                        </a>
                    </div>

                    <p class="hero__meta">
                        <?= $icon('check') ?> Android 11+ &nbsp;·&nbsp; Versión <?= esc($version) ?> &nbsp;·&nbsp; Gratis
                    </p>

                    <div class="hero__chips">
                        <span class="chip"><?= $icon('cube') ?> Visor 3D</span>
                        <span class="chip"><?= $icon('sparkles') ?> Asistente IA</span>
                        <span class="chip"><?= $icon('map') ?> Mapa propio</span>
                    </div>
                </div>

                <div class="hero__visual reveal" data-delay="1">
                    <div class="phone">
                        <div class="phone__frame">
                            <div class="phone__notch" aria-hidden="true"></div>
                            <img class="phone__screen" src="<?= base_url('assets/img/app-home.png') ?>" alt="Pantalla de inicio de NicaExplore con ciudades y lugares recomendados" width="540" height="1200" loading="eager">
                        </div>
                        <div class="float-chip float-chip--1">
                            <img src="<?= base_url('assets/img/itzae.png') ?>" alt="" width="34" height="34">
                            <span><b>Itzae</b><small>Asistente IA</small></span>
                        </div>
                        <div class="float-chip float-chip--2">
                            <?= $icon('cube') ?>
                            <span><b>3D</b><small>Monumentos</small></span>
                        </div>
                        <div class="float-chip float-chip--3">
                            <?= $icon('star') ?>
                            <span><b>4.9</b><small>Valoraciones</small></span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="shell">
                <ul class="stats reveal" data-delay="2">
                    <li class="stat"><b>4</b><span>ciudades para explorar</span></li>
                    <li class="stat"><b>8+</b><span>monumentos en el catálogo</span></li>
                    <li class="stat"><b>3D</b><span>visión interactiva con Unity</span></li>
                    <li class="stat"><b>IA</b><span>asistente con contexto local</span></li>
                </ul>
            </div>
        </section>

        <!-- ===================== Características ===================== -->
        <section class="section" id="caracteristicas">
            <div class="shell">
                <header class="section__head reveal">
                    <span class="eyebrow"><span class="eyebrow__dot"></span> Características</span>
                    <h2 class="section__title">Todo lo que puedes hacer con NicaExplore</h2>
                    <p class="section__lead">Una experiencia pensada para turistas y locales: descubre el patrimonio, planifica tu visita y apoya a los emprendimientos del país.</p>
                </header>

                <div class="features">
                    <?php foreach ($features as $i => $f): ?>
                        <article class="feature reveal" data-delay="<?= $i % 4 ?>">
                            <span class="feature__icon"><?= $icon($f[0]) ?></span>
                            <h3><?= esc($f[1]) ?></h3>
                            <p><?= esc($f[2]) ?></p>
                        </article>
                    <?php endforeach; ?>
                </div>
            </div>
        </section>

        <!-- ===================== Cómo funciona ===================== -->
        <section class="section section--alt" id="como-funciona">
            <div class="shell">
                <header class="section__head reveal">
                    <span class="eyebrow"><span class="eyebrow__dot"></span> Cómo funciona</span>
                    <h2 class="section__title">De la curiosidad a la experiencia en 4 pasos</h2>
                    <p class="section__lead">Sin complicaciones: abre la app y empieza a descubrir Nicaragua.</p>
                </header>

                <div class="steps">
                    <?php foreach ($steps as $i => $s): ?>
                        <article class="step reveal" data-delay="<?= $i ?>">
                            <span class="step__num"><?= esc($s[0]) ?></span>
                            <h3><?= esc($s[1]) ?></h3>
                            <p><?= esc($s[2]) ?></p>
                        </article>
                    <?php endforeach; ?>
                </div>
            </div>
        </section>

        <!-- ===================== Ciudades ===================== -->
        <section class="section" id="ciudades">
            <div class="shell">
                <header class="section__head reveal">
                    <span class="eyebrow"><span class="eyebrow__dot"></span> Ciudades</span>
                    <h2 class="section__title">Empieza por una de nuestras ciudades</h2>
                    <p class="section__lead">Cada ciudad tiene su propia identidad visual, su historia y sus monumentos. El catálogo crece poco a poco.</p>
                </header>

                <div class="cities">
                    <?php foreach ($cities as $i => $c): ?>
                        <article class="city <?= esc($c['mod'], 'attr') ?> reveal" data-delay="<?= $i ?>">
                            <div class="city__media">
                                <img src="<?= base_url('assets/img/' . $c['img']) ?>" alt="Ciudad de <?= esc($c['nombre'], 'attr') ?>" width="600" height="750" loading="lazy">
                                <span class="city__dep"><?= esc($c['dep']) ?></span>
                            </div>
                            <div class="city__body">
                                <h3><?= esc($c['nombre']) ?></h3>
                                <p><?= esc($c['texto']) ?></p>
                            </div>
                        </article>
                    <?php endforeach; ?>
                </div>
            </div>
        </section>

        <!-- ===================== Comercios ===================== -->
        <section class="section section--alt" id="comercios">
            <div class="shell commerces__grid">
                <div class="commerces__intro reveal">
                    <span class="eyebrow"><span class="eyebrow__dot"></span> Ecosistema local</span>
                    <h2 class="section__title">Descubre y apoya a los comercios locales</h2>
                    <p class="section__lead">Restaurantes, cafeterías y emprendimientos nicaragüenses con toda su información a la mano: contacto por WhatsApp, correo, redes sociales y ubicación en Google Maps.</p>
                    <ul class="check-list">
                        <li><?= $icon('check') ?> Contacto directo por WhatsApp</li>
                        <li><?= $icon('check') ?> Galería, logo y descripción del negocio</li>
                        <li><?= $icon('check') ?> ¿Tienes un comercio? Solicita tu incorporación desde la app</li>
                    </ul>
                    <a class="btn btn--primary" href="<?= esc($apkUrl, 'attr') ?>"><?= $icon('store') ?> Regístralo en la app</a>
                </div>

                <div class="commerces__cards">
                    <?php foreach ($commerces as $i => $m): ?>
                        <figure class="commerce reveal" data-delay="<?= $i ?>">
                            <img src="<?= base_url('assets/img/' . $m[0]) ?>" alt="<?= esc($m[1], 'attr') ?>" width="560" height="420" loading="lazy">
                            <figcaption>
                                <b><?= esc($m[1]) ?></b>
                                <span><?= esc($m[2]) ?></span>
                            </figcaption>
                        </figure>
                    <?php endforeach; ?>
                </div>
            </div>
        </section>

        <!-- ===================== Galería ===================== -->
        <section class="section" id="galeria">
            <div class="shell">
                <header class="section__head reveal">
                    <span class="eyebrow"><span class="eyebrow__dot"></span> La app por dentro</span>
                    <h2 class="section__title">Un vistazo a la experiencia</h2>
                    <p class="section__lead">Interfaz oscura, identidad nicaragüense y toda la información clara y a la vista.</p>
                </header>

                <div class="gallery">
                    <figure class="gallery__item reveal">
                        <div class="gallery__phone"><img src="<?= base_url('assets/img/app-home.png') ?>" alt="Inicio con ciudades y lugares recomendados" width="540" height="1200" loading="lazy"></div>
                        <figcaption><b>Inicio</b> Ciudades, recomendados y acceso rápido</figcaption>
                    </figure>
                    <figure class="gallery__item reveal" data-delay="1">
                        <div class="gallery__phone"><img src="<?= base_url('assets/img/app-map.png') ?>" alt="Mapa turístico de Nicaragua" width="540" height="1200" loading="lazy"></div>
                        <figcaption><b>Mapa</b> Lugares y comercios sobre MapLibre + OSM</figcaption>
                    </figure>
                    <div class="gallery__aside">
                        <article class="gallery__card reveal" data-delay="2">
                            <span class="feature__icon"><?= $icon('cube') ?></span>
                            <h3>Visor 3D con Unity</h3>
                            <p>Rota, acerca y restablece la vista de cada monumento como si lo tuvieras enfrente.</p>
                        </article>
                        <article class="gallery__card reveal" data-delay="3">
                            <span class="feature__icon"><?= $icon('sparkles') ?></span>
                            <h3>Itzae, tu asistente</h3>
                            <p>Respuestas claras sobre el catálogo local y los comercios reales de cada ciudad.</p>
                        </article>
                    </div>
                </div>
            </div>
        </section>

        <!-- ===================== Tecnología ===================== -->
        <section class="section section--tech">
            <div class="shell tech reveal">
                <span class="eyebrow"><span class="eyebrow__dot"></span> Hecho con tecnología de primer nivel</span>
                <ul class="tech__badges">
                    <?php foreach ($tech as $t): ?>
                        <li><?= esc($t) ?></li>
                    <?php endforeach; ?>
                </ul>
                <p class="tech__note">Desarrollada por el equipo <strong>Los Punto y Coma</strong> como prototipo académico de turismo nicaragüense.</p>
            </div>
        </section>

        <!-- ===================== Preguntas frecuentes ===================== -->
        <section class="section section--alt" id="preguntas">
            <div class="shell">
                <header class="section__head reveal">
                    <span class="eyebrow"><span class="eyebrow__dot"></span> Preguntas frecuentes</span>
                    <h2 class="section__title">Antes de descargarla</h2>
                </header>

                <div class="faq">
                    <?php foreach ($faqs as $i => $q): ?>
                        <details class="faq__item reveal" data-delay="<?= $i % 3 ?>" <?= $i === 0 ? 'open' : '' ?>>
                            <summary>
                                <span><?= esc($q[0]) ?></span>
                                <?= $icon('chevron', 'faq__chevron') ?>
                            </summary>
                            <p><?= esc($q[1]) ?></p>
                        </details>
                    <?php endforeach; ?>
                </div>
            </div>
        </section>

        <!-- ===================== CTA final ===================== -->
        <section class="cta" id="descargar">
            <div class="shell">
                <div class="cta__card reveal">
                    <div class="cta__glow" aria-hidden="true"></div>
                    <img class="cta__isotipo" src="<?= base_url('assets/img/nicaexplorer_isotipo.png') ?>" alt="" width="96" height="106">
                    <h2>Descarga NicaExplore y empieza a explorar</h2>
                    <p>Gratis para Android. Explora sin registrarte o crea tu cuenta para guardar tus lugares favoritos.</p>
                    <div class="cta__actions">
                        <a class="btn btn--primary btn--lg" href="<?= esc($apkUrl, 'attr') ?>"><?= $icon('download') ?> Descargar APK</a>
                        <a class="btn btn--ghost btn--lg" href="<?= esc($repoUrl, 'attr') ?>" target="_blank" rel="noopener"><?= $icon('github') ?> Ver en GitHub</a>
                    </div>
                    <p class="cta__meta">Recomendado instalar en un teléfono Android físico (ARM64) para el visor 3D.</p>
                </div>
            </div>
        </section>
    </main>

    <!-- ===================== Pie de página ===================== -->
    <footer class="site-footer">
        <div class="shell footer__grid">
            <div class="footer__brand">
                <a class="brand" href="#inicio">
                    <img class="brand__mark" src="<?= base_url('assets/img/nicaexplorer_isotipo.png') ?>" alt="" width="40" height="44">
                    <span class="brand__name">Nica<span>Explore</span></span>
                </a>
                <p>Explora. Conecta. Inspira.<br>Turismo, cultura y tecnología de Nicaragua.</p>
            </div>

            <nav class="footer__col" aria-label="Secciones">
                <h4>App</h4>
                <a href="#caracteristicas">Características</a>
                <a href="#ciudades">Ciudades</a>
                <a href="#comercios">Comercios</a>
                <a href="#galeria">Galería</a>
            </nav>

            <nav class="footer__col" aria-label="Recursos">
                <h4>Recursos</h4>
                <a href="<?= esc($apkUrl, 'attr') ?>">Descargar APK</a>
                <a href="<?= esc($repoUrl, 'attr') ?>" target="_blank" rel="noopener">Repositorio en GitHub</a>
                <a href="<?= site_url('panel') ?>">Panel administrativo</a>
                <a href="<?= site_url('api/v1/health') ?>">Estado de la API</a>
            </nav>

            <div class="footer__col">
                <h4>Proyecto</h4>
                <p class="footer__team">Equipo <strong>Los Punto y Coma</strong></p>
                <p class="footer__small">Prototipo académico. La afluencia y la duración de las rutas son orientativas y no representan datos en tiempo real.</p>
            </div>
        </div>

        <div class="shell footer__bottom">
            <span>© <?= esc($year) ?> NicaExplore · Los Punto y Coma</span>
            <span>Hecho en Nicaragua <span class="footer__flag" aria-hidden="true">NI</span></span>
        </div>
    </footer>

    <script src="<?= base_url('assets/landing.js') ?>" defer></script>
</body>
</html>
