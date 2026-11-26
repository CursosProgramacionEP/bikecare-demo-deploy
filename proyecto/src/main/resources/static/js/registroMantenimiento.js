/**
 * Registro de mantenimiento:
 *  - Completa el kilometraje con el odómetro registrado de la bicicleta elegida.
 *  - Al elegir "Cambio de componente", muestra los componentes activos de esa
 *    bicicleta para seleccionar cuáles se reemplazaron.
 */
(function () {
  "use strict";

  var selectBici = document.getElementById("idBicicleta");
  var kmInput = document.getElementById("kilometrajeServicio");
  var kmHint = document.getElementById("kmHint");
  var selectTipo = document.getElementById("tipoServicio");
  var campoComponentes = document.getElementById("campoComponentes");
  var componentesError = document.getElementById("componentesError");
  var gruposComponentes = campoComponentes
    ? campoComponentes.querySelectorAll(".mant-componentes-grupo")
    : [];

  if (!selectBici || !kmInput) {
    return;
  }

  var HINT_DEFAULT = "Selecciona una bici para rellenar este campo";
  var HINT_TOMADO = "Kilometraje total que marca tu bici hoy. Lo ves en el Garaje.";
  var HINT_SIN_KM = "Esta bicicleta aún no tiene kilometraje registrado. Ingresa el valor manualmente.";
  var TIPO_CAMBIO_COMPONENTE = "cambio de componente";

  // ---------- Kilometraje de la bici ----------

  function setHint(texto, alerta) {
    if (!kmHint) {
      return;
    }
    kmHint.textContent = texto;
    kmHint.className = "mant-hint" + (alerta ? " mant-hint--alerta" : "");
  }

  function aplicarKilometraje() {
    var opcion = selectBici.options[selectBici.selectedIndex];

    if (!opcion || !opcion.value) {
      kmInput.value = "";
      setHint(HINT_DEFAULT, false);
      return;
    }

    var km = parseFloat(opcion.getAttribute("data-km"));

    if (isNaN(km) || km <= 0) {
      kmInput.value = "";
      setHint(HINT_SIN_KM, true);
      return;
    }

    kmInput.value = km.toFixed(2);
    setHint(HINT_TOMADO, false);
  }

  // ---------- Cambio de componente ----------

  function esCambioComponente() {
    return !!selectTipo && selectTipo.value.trim().toLowerCase() === TIPO_CAMBIO_COMPONENTE;
  }

  function grupoDeBiciSeleccionada() {
    var id = selectBici.value;
    for (var i = 0; i < gruposComponentes.length; i++) {
      if (gruposComponentes[i].getAttribute("data-bici") === id) {
        return gruposComponentes[i];
      }
    }
    return null;
  }

  function limpiarSeleccion(grupo) {
    if (!grupo) {
      return;
    }
    var casillas = grupo.querySelectorAll('input[type="checkbox"]');
    for (var i = 0; i < casillas.length; i++) {
      casillas[i].checked = false;
    }
  }

  function ocultarErrorComponentes() {
    if (componentesError) {
      componentesError.hidden = true;
    }
  }

  function actualizarComponentes() {
    if (!campoComponentes) {
      return;
    }

    var mostrar = esCambioComponente() && selectBici.value !== "";
    campoComponentes.hidden = !mostrar;

    for (var i = 0; i < gruposComponentes.length; i++) {
      var grupo = gruposComponentes[i];
      var coincide = mostrar && grupo.getAttribute("data-bici") === selectBici.value;
      grupo.hidden = !coincide;
      if (!coincide) {
        limpiarSeleccion(grupo);
      }
    }

    if (!mostrar) {
      ocultarErrorComponentes();
    }
  }

  function validarComponentes() {
    if (!esCambioComponente()) {
      ocultarErrorComponentes();
      return true;
    }

    var grupo = grupoDeBiciSeleccionada();
    var marcados = grupo ? grupo.querySelectorAll('input[type="checkbox"]:checked') : [];
    var valido = marcados.length > 0;

    if (componentesError) {
      componentesError.hidden = valido;
    }
    return valido;
  }

  // ---------- Eventos ----------

  selectBici.addEventListener("change", function () {
    aplicarKilometraje();
    actualizarComponentes();
  });

  if (selectTipo && campoComponentes) {
    selectTipo.addEventListener("change", actualizarComponentes);

    var form = selectBici.closest("form");
    if (form) {
      form.addEventListener("submit", function (evento) {
        if (!validarComponentes()) {
          evento.preventDefault();
          if (componentesError) {
            componentesError.scrollIntoView({ block: "nearest" });
          }
        }
      });
    }

    if (campoComponentes) {
      campoComponentes.addEventListener("change", function (evento) {
        if (evento.target && evento.target.type === "checkbox") {
          ocultarErrorComponentes();
        }
      });
    }
  }

  // Si el navegador restaura el formulario con datos ya elegidos.
  aplicarKilometraje();
  actualizarComponentes();
})();
