<!DOCTYPE html>
<html lang="es" data-bs-theme="dark">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Iniciar sesión · Panel NicaExplorer</title>
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
    <link rel="stylesheet" href="<?= base_url('assets/panel.css') ?>">
</head>
<body class="login-page">
<?php $error = session()->getFlashdata('error'); ?>
<button type="button" class="nica-icon-btn nica-login-theme" data-nica-theme-toggle
        title="Cambiar a tema claro" aria-label="Cambiar tema">
    <i class="bi bi-sun nica-theme-icon-sun" aria-hidden="true"></i>
    <i class="bi bi-moon-stars nica-theme-icon-moon" aria-hidden="true"></i>
</button>
<main class="nica-login">
    <div class="nica-login-brand">
        <span class="nica-login-mark"><i class="bi bi-compass"></i></span>
        <span class="nica-login-brand-text">Nica<span>Explorer</span></span>
    </div>

    <div class="nica-login-card">
        <h1 class="nica-login-title">Iniciar sesión</h1>
        <p class="nica-login-sub">Accede al panel de administración.</p>

        <?php if ($error): ?>
            <div class="alert alert-danger mb-3" role="alert">
                <i class="bi bi-exclamation-triangle me-2"></i><?= esc($error) ?>
            </div>
        <?php endif; ?>

        <form action="<?= site_url('panel/login') ?>" method="post" autocomplete="off">
            <?= csrf_field() ?>

            <div class="mb-3">
                <label class="form-label" for="nica-username">Usuario o correo</label>
                <input type="text" name="username" id="nica-username" class="form-control"
                       placeholder="admin o tu correo" required autofocus>
            </div>

            <div class="mb-4">
                <label class="form-label" for="nica-password">Contraseña</label>
                <input type="password" name="password" id="nica-password" class="form-control"
                       placeholder="••••••••" required>
            </div>

            <button type="submit" class="btn btn-primary w-100">
                <i class="bi bi-box-arrow-in-right"></i> Entrar
            </button>
        </form>
    </div>

    <p class="nica-login-footer">NicaExplorer Backend · Los Punto y Coma</p>
</main>
<script src="<?= base_url('assets/theme.js') ?>"></script>
</body>
</html>
