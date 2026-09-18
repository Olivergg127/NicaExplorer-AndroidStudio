/* NicaExplorer · switch de tema claro/oscuro
   El tema inicial se aplica inline en el <head> para evitar el parpadeo. */
(function () {
    'use strict';

    var STORAGE_KEY = 'nica-theme';

    function currentTheme() {
        return document.documentElement.getAttribute('data-bs-theme') === 'light' ? 'light' : 'dark';
    }

    function syncButtons(theme) {
        document.querySelectorAll('[data-nica-theme-toggle]').forEach(function (button) {
            button.setAttribute('aria-pressed', theme === 'light' ? 'true' : 'false');
            button.setAttribute('title', theme === 'light' ? 'Cambiar a tema oscuro' : 'Cambiar a tema claro');
        });
    }

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-bs-theme', theme);
        try {
            window.localStorage.setItem(STORAGE_KEY, theme);
        } catch (error) {
            /* almacenamiento no disponible */
        }
        syncButtons(theme);
    }

    document.addEventListener('click', function (event) {
        var toggle = event.target.closest('[data-nica-theme-toggle]');
        if (!toggle) {
            return;
        }
        event.preventDefault();
        applyTheme(currentTheme() === 'dark' ? 'light' : 'dark');
    });

    document.addEventListener('DOMContentLoaded', function () {
        syncButtons(currentTheme());
    });
})();
