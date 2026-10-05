/*
 * NicaExplore · Landing page
 * Interacciones ligeras sin dependencias: menú móvil, cabecera al hacer
 * scroll y animaciones de aparición. Respeta prefers-reduced-motion.
 */
(function () {
    'use strict';

    var docEl = document.documentElement;
    var header = document.querySelector('[data-header]');
    var toggle = document.querySelector('[data-menu-toggle]');
    var menu = document.getElementById('menu');
    var reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    /* ---- Cabecera con fondo al hacer scroll ---- */
    if (header) {
        var onScroll = function () {
            header.classList.toggle('is-scrolled', window.scrollY > 12);
        };
        onScroll();
        window.addEventListener('scroll', onScroll, { passive: true });
    }

    /* ---- Menú móvil ---- */
    var closeMenu = function () {
        document.body.classList.remove('menu-open');
        if (toggle) {
            toggle.setAttribute('aria-expanded', 'false');
        }
    };

    if (toggle) {
        toggle.addEventListener('click', function () {
            var open = document.body.classList.toggle('menu-open');
            toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
        });
    }

    if (menu) {
        menu.addEventListener('click', function (event) {
            if (event.target.closest('a')) {
                closeMenu();
            }
        });
    }

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            closeMenu();
        }
    });

    /* ---- Animaciones al entrar en pantalla ---- */
    var reveals = document.querySelectorAll('.reveal');

    if (reduceMotion || !('IntersectionObserver' in window)) {
        reveals.forEach(function (el) {
            el.classList.add('is-visible');
        });
        return;
    }

    var observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add('is-visible');
                observer.unobserve(entry.target);
            }
        });
    }, {
        root: null,
        rootMargin: '0px 0px -8% 0px',
        threshold: 0.12
    });

    reveals.forEach(function (el) {
        observer.observe(el);
    });
})();
