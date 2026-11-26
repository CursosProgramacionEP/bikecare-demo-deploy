/* ============================================================================
 * addComponentes.js — formulario "Añadir componentes a la bici".
 * - Los bloques .componente-form (detalle marca/modelo/fecha/km) nacen ocultos
 *   por CSS (.componente-form { display:none }) y solo se muestran (.activo)
 *   cuando su checkbox queda marcado.
 * - Actualiza el contador, el aviso "sin selección" y habilita Guardar.
 * ========================================================================== */
(function () {
    // 1) Localizar piezas del formulario. Si no estamos en este módulo, salir.
    var form = document.getElementById('formComponentes');
    if (!form) return;
    var contador = document.getElementById('contadorSeleccion');
    var sinSeleccion = document.getElementById('sinSeleccion');
    var btnGuardar = document.getElementById('btnGuardarComponentes');

    // 2) Solo checkboxes habilitados (los "Ya instalado" vienen disabled).
    var checks = Array.prototype.slice.call(
        form.querySelectorAll('input[type="checkbox"][name="componentes"]:not(:disabled)')
    );

    // 3) Mostrar/ocultar el detalle según el checkbox y refrescar el estado.
    function actualizar() {
        var marcados = 0;
        checks.forEach(function (check) {
            var detalle = document.getElementById('detalle-' + check.value);
            var activo = check.checked;
            if (activo) marcados++;
            // El detalle existe solo si el componente NO estaba instalado (th:unless).
            if (detalle) detalle.classList.toggle('activo', activo);
        });
        // Contador superior: "0 seleccionados" / "1 seleccionado" / "N seleccionados".
        if (contador) {
            contador.textContent = marcados + (marcados === 1 ? ' seleccionado' : ' seleccionados');
        }
        // Aviso central visible solo cuando no hay nada marcado.
        if (sinSeleccion) sinSeleccion.style.display = marcados === 0 ? '' : 'none';
        // Guardar habilitado solo si hay al menos uno marcado.
        if (btnGuardar) btnGuardar.disabled = marcados === 0;
    }

    // 4) Escuchar cambios y pintar el estado inicial (todo oculto al cargar).
    checks.forEach(function (check) {
        check.addEventListener('change', actualizar);
    });
    actualizar();
})();
