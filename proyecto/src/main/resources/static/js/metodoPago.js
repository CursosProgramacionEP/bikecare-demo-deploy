/*
 * HU-14 · Selección dinámica del método de pago (Tarjeta / Plin / Yape).
 *  - Tarjeta  -> muestra los campos de tarjeta y oculta el QR.
 *  - Plin/Yape -> oculta la tarjeta, muestra el QR de la billetera y habilita el número de operación.
 *  - Valida en el navegador (el servidor vuelve a validar todo).
 * Las secciones ocultas son <fieldset disabled>: sus campos no se validan ni se envían.
 */
(function () {
    'use strict';

    var form = document.getElementById('formPago');
    if (!form) { return; }

    var radios = form.querySelectorAll('input[name="tipo"]');
    var seccionTarjeta = document.getElementById('seccionTarjeta');
    var seccionQr = document.getElementById('seccionQr');
    var qrContenedor = document.getElementById('qrContenedor');
    var qrImagen = document.getElementById('qrImagen');
    var qrTitulo = document.getElementById('qrTitulo');
    var qrPasos = document.getElementById('qrPasos');
    var qrError = document.getElementById('qrError');
    var marcaEtiqueta = document.getElementById('marcaTarjeta');
    var btnPagar = document.getElementById('btnPagar');
    var textoBoton = btnPagar.textContent;

    var campos = {
        numeroTarjeta: document.getElementById('numeroTarjeta'),
        titular: document.getElementById('titular'),
        expiracion: document.getElementById('expiracion'),
        cvv: document.getElementById('cvv'),
        numeroOperacion: document.getElementById('numeroOperacion')
    };

    var PASOS_QR = [
        'Escanea este código QR desde la app.',
        'Confirma el pago.',
        'Copia el número de operación del comprobante y pégalo abajo.'
    ];

    var BILLETERAS = {
        YAPE: { nombre: 'Yape', qr: seccionQr.getAttribute('data-qr-yape'), clase: 'pago-qr--yape' },
        PLIN: { nombre: 'Plin', qr: seccionQr.getAttribute('data-qr-plin'), clase: 'pago-qr--plin' }
    };

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------
    function digitos(texto) { return (texto || '').replace(/\D/g, ''); }

    function luhn(numero) {
        var suma = 0, duplicar = false;
        for (var i = numero.length - 1; i >= 0; i--) {
            var d = numero.charCodeAt(i) - 48;
            if (duplicar) { d *= 2; if (d > 9) { d -= 9; } }
            suma += d;
            duplicar = !duplicar;
        }
        return suma % 10 === 0;
    }

    function detectarMarca(d) {
        if (/^4/.test(d)) { return 'VISA'; }
        if (/^3[47]/.test(d)) { return 'AMEX'; }
        if (/^(5[1-5]|222[1-9]|22[3-9]\d|2[3-6]\d\d|27[01]\d|2720)/.test(d)) { return 'MASTERCARD'; }
        return '';
    }

    function tipoSeleccionado() {
        for (var i = 0; i < radios.length; i++) {
            if (radios[i].checked) { return radios[i].value; }
        }
        return '';
    }

    function alerta(texto) {
        if (window.AppAlerts) { window.AppAlerts.show('error', texto); }
    }

    // ------------------------------------------------------------------
    // Errores en línea
    // ------------------------------------------------------------------
    function mostrarError(nombre, mensaje) {
        var el = form.querySelector('[data-error-for="' + nombre + '"]');
        if (el) { el.textContent = mensaje || ''; }
        var campo = campos[nombre];
        if (campo) {
            campo.classList.toggle('pago-invalido', !!mensaje);
            campo.setAttribute('aria-invalid', mensaje ? 'true' : 'false');
        }
    }

    function limpiarErrores() {
        Object.keys(campos).forEach(function (nombre) { mostrarError(nombre, ''); });
    }

    // ------------------------------------------------------------------
    // Reglas de validación (mismas que MetodoPagoService)
    // ------------------------------------------------------------------
    var reglas = {
        numeroTarjeta: function () {
            var d = digitos(campos.numeroTarjeta.value);
            if (!d) { return 'Ingresa el número de tarjeta.'; }
            if (d.length < 13 || d.length > 19 || !luhn(d)) { return 'El número de tarjeta no es válido.'; }
            return '';
        },
        titular: function () {
            var t = campos.titular.value.trim().replace(/\s+/g, ' ');
            return t.length < 3 ? 'Ingresa el nombre del titular tal como figura en la tarjeta.' : '';
        },
        expiracion: function () {
            var m = /^(0[1-9]|1[0-2])\/(\d{2})$/.exec(campos.expiracion.value.trim());
            if (!m) { return 'Usa el formato MM/AA.'; }
            var hoy = new Date();
            var mes = parseInt(m[1], 10);
            var anio = 2000 + parseInt(m[2], 10);
            if (anio < hoy.getFullYear() || (anio === hoy.getFullYear() && mes < hoy.getMonth() + 1)) {
                return 'La tarjeta está vencida.';
            }
            if (anio > hoy.getFullYear() + 20) { return 'La fecha de expiración no es válida.'; }
            return '';
        },
        cvv: function () {
            var largo = detectarMarca(digitos(campos.numeroTarjeta.value)) === 'AMEX' ? 4 : 3;
            return new RegExp('^\\d{' + largo + '}$').test(campos.cvv.value) ? '' : 'El CVV debe tener ' + largo + ' dígitos.';
        },
        numeroOperacion: function () {
            var v = campos.numeroOperacion.value.trim();
            if (!v) { return 'Ingresa el número de operación de tu pago.'; }
            return /^[A-Za-z0-9]{6,20}$/.test(v) ? '' : 'Debe tener entre 6 y 20 letras o números.';
        }
    };

    function camposDe(tipo) {
        if (tipo === 'TARJETA') { return ['numeroTarjeta', 'titular', 'expiracion', 'cvv']; }
        return tipo ? ['numeroOperacion'] : [];
    }

    // ------------------------------------------------------------------
    // Mostrar / ocultar secciones
    // ------------------------------------------------------------------
    function activarSeccion(seccion, activa) {
        seccion.classList.toggle('pago-oculto', !activa);
        seccion.disabled = !activa;
        seccion.setAttribute('aria-hidden', activa ? 'false' : 'true');
    }

    function mostrarQr(billetera) {
        qrContenedor.className = 'pago-qr pago-campo--full ' + billetera.clase;
        qrTitulo.textContent = 'Paga con ' + billetera.nombre;
        qrError.classList.add('pago-oculto');
        qrImagen.classList.remove('pago-oculto');
        qrImagen.alt = 'Código QR de ' + billetera.nombre;
        qrImagen.src = billetera.qr;

        qrPasos.innerHTML = '';
        PASOS_QR.forEach(function (paso) {
            var li = document.createElement('li');
            li.textContent = paso;
            qrPasos.appendChild(li);
        });
    }

    function actualizar() {
        var tipo = tipoSeleccionado();
        var billetera = BILLETERAS[tipo];

        activarSeccion(seccionTarjeta, tipo === 'TARJETA');
        activarSeccion(seccionQr, !!billetera);
        if (billetera) { mostrarQr(billetera); }

        limpiarErrores();
        btnPagar.disabled = !tipo;
    }

    qrImagen.addEventListener('error', function () {
        qrImagen.classList.add('pago-oculto');
        qrError.classList.remove('pago-oculto');
    });

    // ------------------------------------------------------------------
    // Formato de entrada
    // ------------------------------------------------------------------
    function mostrarMarca(d) {
        var marca = detectarMarca(d);
        marcaEtiqueta.textContent = marca;
        marcaEtiqueta.className = 'pago-marca' + (marca ? ' pago-marca--' + marca.toLowerCase() : '');
    }

    function formatearNumeroTarjeta() {
        var input = campos.numeroTarjeta;
        var digitosAntes = digitos(input.value.slice(0, input.selectionStart || 0)).length;
        var d = digitos(input.value);
        var amex = /^3[47]/.test(d);
        d = d.slice(0, amex ? 15 : 19);

        var grupos = amex ? [4, 6, 5] : [4, 4, 4, 4, 3];
        var partes = [], inicio = 0;
        grupos.forEach(function (g) {
            if (inicio < d.length) { partes.push(d.substr(inicio, g)); inicio += g; }
        });
        input.value = partes.join(' ');

        // Mantener el cursor junto al mismo dígito
        var pos = 0, vistos = 0;
        while (pos < input.value.length && vistos < digitosAntes) {
            if (/\d/.test(input.value.charAt(pos))) { vistos++; }
            pos++;
        }
        input.setSelectionRange(pos, pos);
        mostrarMarca(d);
    }

    function formatearExpiracion(e) {
        var d = digitos(campos.expiracion.value).slice(0, 4);
        var borrando = e && e.inputType === 'deleteContentBackward';
        if (d.length === 1 && parseInt(d, 10) > 1) { d = '0' + d; }
        if (d.length >= 3) {
            campos.expiracion.value = d.slice(0, 2) + '/' + d.slice(2);
        } else if (d.length === 2 && !borrando) {
            campos.expiracion.value = d + '/';
        } else {
            campos.expiracion.value = d;
        }
    }

    // ------------------------------------------------------------------
    // Eventos
    // ------------------------------------------------------------------
    Array.prototype.forEach.call(radios, function (radio) {
        radio.addEventListener('change', actualizar);
    });

    campos.numeroTarjeta.addEventListener('input', formatearNumeroTarjeta);
    campos.expiracion.addEventListener('input', formatearExpiracion);
    campos.cvv.addEventListener('input', function () {
        campos.cvv.value = digitos(campos.cvv.value).slice(0, 4);
    });
    campos.titular.addEventListener('input', function () {
        campos.titular.value = campos.titular.value.replace(/[^\p{L} .'-]/gu, '').toUpperCase();
    });
    campos.numeroOperacion.addEventListener('input', function () {
        campos.numeroOperacion.value = campos.numeroOperacion.value.replace(/[^A-Za-z0-9]/g, '').toUpperCase().slice(0, 20);
    });

    // Validar al salir del campo; limpiar el error al volver a escribir
    Object.keys(campos).forEach(function (nombre) {
        var campo = campos[nombre];
        campo.addEventListener('blur', function () {
            if (campo.value !== '') { mostrarError(nombre, reglas[nombre]()); }
        });
        campo.addEventListener('input', function () { mostrarError(nombre, ''); });
    });

    form.addEventListener('submit', function (e) {
        var tipo = tipoSeleccionado();
        if (!tipo) {
            e.preventDefault();
            alerta('Selecciona un método de pago.');
            return;
        }

        var primerError = '';
        camposDe(tipo).forEach(function (nombre) {
            var mensaje = reglas[nombre]();
            mostrarError(nombre, mensaje);
            if (mensaje && !primerError) { primerError = nombre; }
        });

        if (primerError) {
            e.preventDefault();
            campos[primerError].focus();
            alerta('Revisa los campos marcados en rojo.');
            return;
        }

        // Evita doble envío
        btnPagar.disabled = true;
        btnPagar.textContent = 'Procesando…';
    });

    // Al volver con el botón "atrás" el navegador restaura la página tal cual
    window.addEventListener('pageshow', function (e) {
        if (e.persisted) {
            btnPagar.textContent = textoBoton;
            actualizar();
        }
    });

    actualizar();
})();
