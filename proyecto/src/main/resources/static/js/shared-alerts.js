/*
 * Comportamiento de fragments/alerts.html + API para alertas dinámicas desde JS:
 *   AppAlerts.show('success', 'Guardado', 'Listo');   // tipo, texto, título (opcional)
 *   AppAlerts.modal('Error', 'No se pudo completar la acción.');
 */
(function () {
    'use strict';

    var AUTOCLOSE_MS = 6000;

    function container() {
        var c = document.querySelector('.app-alerts');
        if (!c) {
            c = document.createElement('div');
            c.className = 'app-alerts';
            c.setAttribute('aria-live', 'polite');
            document.body.appendChild(c);
        }
        return c;
    }

    function dismiss(el) {
        if (!el || !el.parentNode) { return; }
        el.classList.add('app-alert--leaving');
        setTimeout(function () {
            if (el.parentNode) { el.parentNode.removeChild(el); }
        }, 200);
    }

    function show(type, text, title) {
        var el = document.createElement('div');
        el.className = 'app-alert app-alert--' + type;
        el.setAttribute('role', type === 'error' ? 'alert' : 'status');

        var body = document.createElement('div');
        body.className = 'app-alert__body';
        if (title) {
            var strong = document.createElement('strong');
            strong.textContent = title;
            body.appendChild(strong);
        }
        var span = document.createElement('span');
        span.textContent = text; // textContent: evita XSS
        body.appendChild(span);

        var close = document.createElement('button');
        close.type = 'button';
        close.className = 'app-alert__close';
        close.setAttribute('aria-label', 'Cerrar');
        close.setAttribute('data-close-alert', '');
        close.innerHTML = '&times;';

        el.appendChild(body);
        el.appendChild(close);
        container().appendChild(el);

        if (type === 'success' || type === 'info') {
            setTimeout(function () { dismiss(el); }, AUTOCLOSE_MS);
        }
        return el;
    }

    function modal(title, text) {
        var dlg = document.createElement('dialog');
        dlg.className = 'app-modal';

        var box = document.createElement('div');
        box.className = 'app-modal__box';

        var item = document.createElement('div');
        item.className = 'app-modal__item app-modal__item--error';
        var h2 = document.createElement('h2');
        h2.textContent = title;
        var p = document.createElement('p');
        p.textContent = text;
        item.appendChild(h2);
        item.appendChild(p);

        var form = document.createElement('form');
        form.method = 'dialog';
        var btn = document.createElement('button');
        btn.type = 'submit';
        btn.className = 'app-modal__btn';
        btn.textContent = 'Entendido';
        form.appendChild(btn);

        box.appendChild(item);
        box.appendChild(form);
        dlg.appendChild(box);
        dlg.addEventListener('close', function () {
            if (dlg.parentNode) { dlg.parentNode.removeChild(dlg); }
        });

        document.body.appendChild(dlg);
        if (typeof dlg.showModal === 'function') { dlg.showModal(); }
        return dlg;
    }

    // Botón "x" de cualquier alerta (también las creadas dinámicamente)
    document.addEventListener('click', function (e) {
        var btn = e.target.closest('[data-close-alert]');
        if (btn) { dismiss(btn.closest('.app-alert')); }
    });

    document.addEventListener('DOMContentLoaded', function () {
        // Modal enviado por el servidor
        var srvModal = document.getElementById('appModal');
        if (srvModal && typeof srvModal.showModal === 'function') { srvModal.showModal(); }

        // Alertas de éxito/info se cierran solas
        document.querySelectorAll('.app-alert[data-autoclose="true"]').forEach(function (el) {
            setTimeout(function () { dismiss(el); }, AUTOCLOSE_MS);
        });
    });

    window.AppAlerts = { show: show, modal: modal };
})();
