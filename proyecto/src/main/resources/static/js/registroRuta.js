/**
 * Registro de rutas: alterna entre ingreso manual y archivo GPX.
 * En modo GPX lee el archivo en el navegador para mostrar la distancia al instante
 * (vista previa). El valor definitivo siempre lo calcula el servidor al guardar.
 */
(function () {
  "use strict";

  var RADIO_TIERRA_KM = 6371.0088;

  var origen = document.getElementById("origenRegistro");
  var campoGpx = document.getElementById("campoGpx");
  var inputGpx = document.getElementById("archivoGpx");
  var estado = document.getElementById("gpxEstado");
  var distancia = document.getElementById("distanciaKm");
  var hintDistancia = document.getElementById("hintDistancia");
  var fecha = document.getElementById("fechaRecorrido");
  var nombreArchivo = document.getElementById("gpxNombre");
  var btnQuitar = document.getElementById("btnQuitarGpx");

  if (!origen || !campoGpx || !inputGpx || !distancia) {
    return;
  }

  var MENSAJE_INICIAL = estado.textContent;
  var SIN_ARCHIVO = nombreArchivo.textContent;
  var FECHA_ORIGINAL = fecha ? fecha.value : "";
  var MAX_BYTES = 5 * 1024 * 1024;

  function esGpx() {
    return origen.value === "GPX";
  }

  function setEstado(texto, clase) {
    estado.textContent = texto;
    estado.className = "campo-hint" + (clase ? " " + clase : "");
  }

  function mostrarNombre(archivo) {
    nombreArchivo.textContent = archivo ? archivo.name : SIN_ARCHIVO;
    nombreArchivo.className = "gpx-nombre" + (archivo ? " con-archivo" : "");
    btnQuitar.hidden = !archivo;
  }

  function limpiarArchivo() {
    inputGpx.value = "";
    mostrarNombre(null);
    setEstado(MENSAJE_INICIAL, "");
  }

  function aplicarModo() {
    var gpx = esGpx();
    campoGpx.hidden = !gpx;
    hintDistancia.hidden = !gpx;
    inputGpx.required = gpx;

    if (gpx) {
      distancia.value = "";
      distancia.required = false;
      distancia.disabled = true;
      distancia.placeholder = "Se calcula del GPX";
    } else {
      distancia.disabled = false;
      distancia.required = true;
      distancia.placeholder = "Ej. 25.50";
      distancia.value = "";
      limpiarArchivo();
    }
  }

  function haversine(lat1, lon1, lat2, lon2) {
    var rad = Math.PI / 180;
    var dLat = (lat2 - lat1) * rad;
    var dLon = (lon2 - lon1) * rad;
    var a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * rad) *
        Math.cos(lat2 * rad) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);
    return 2 * RADIO_TIERRA_KM * Math.asin(Math.min(1, Math.sqrt(a)));
  }

  function hijos(doc, nombre) {
    return Array.prototype.slice.call(doc.getElementsByTagNameNS("*", nombre));
  }

  function puntosDe(segmento, nombrePunto) {
    var pts = [];
    var nodos = hijos(segmento, nombrePunto);
    for (var i = 0; i < nodos.length; i++) {
      var lat = parseFloat(nodos[i].getAttribute("lat"));
      var lon = parseFloat(nodos[i].getAttribute("lon"));
      if (
        isNaN(lat) ||
        isNaN(lon) ||
        lat < -90 ||
        lat > 90 ||
        lon < -180 ||
        lon > 180
      ) {
        throw new Error("El archivo GPX tiene coordenadas no válidas.");
      }
      pts.push({
        lat: lat,
        lon: lon,
        time: nodos[i].getElementsByTagNameNS("*", "time")[0],
      });
    }
    return pts;
  }

  /** Devuelve {km, primeraFecha} o lanza Error con mensaje para el usuario. */
  function analizarGpx(texto) {
    var doc = new DOMParser().parseFromString(texto, "application/xml");
    if (
      doc.getElementsByTagName("parsererror").length > 0 ||
      !doc.documentElement ||
      doc.documentElement.localName !== "gpx"
    ) {
      throw new Error("El archivo no es un GPX válido.");
    }

    var segmentos = [];
    hijos(doc, "trk").forEach(function (trk) {
      hijos(trk, "trkseg").forEach(function (seg) {
        segmentos.push(puntosDe(seg, "trkpt"));
      });
    });
    var totalPuntos = segmentos.reduce(function (n, s) {
      return n + s.length;
    }, 0);

    if (totalPuntos === 0) {
      segmentos = hijos(doc, "rte").map(function (rte) {
        return puntosDe(rte, "rtept");
      });
      totalPuntos = segmentos.reduce(function (n, s) {
        return n + s.length;
      }, 0);
    }
    if (totalPuntos < 2) {
      throw new Error(
        "El archivo GPX no contiene suficientes puntos de recorrido para calcular la distancia.",
      );
    }

    var km = 0;
    var primeraFecha = null;
    segmentos.forEach(function (pts) {
      for (var i = 1; i < pts.length; i++) {
        km += haversine(pts[i - 1].lat, pts[i - 1].lon, pts[i].lat, pts[i].lon);
      }
      if (!primeraFecha && pts.length > 0 && pts[0].time) {
        var t = new Date(pts[0].time.textContent.trim());
        if (!isNaN(t.getTime())) {
          // Fecha local en formato yyyy-mm-dd (lo que espera <input type="date">).
          var mm = String(t.getMonth() + 1).padStart(2, "0");
          var dd = String(t.getDate()).padStart(2, "0");
          primeraFecha = t.getFullYear() + "-" + mm + "-" + dd;
        }
      }
    });
    return { km: km, primeraFecha: primeraFecha };
  }

  inputGpx.addEventListener("change", function () {
    distancia.value = "";
    var archivo = inputGpx.files && inputGpx.files[0];
    if (!archivo) {
      limpiarArchivo();
      return;
    }
    if (!/\.gpx$/i.test(archivo.name)) {
      limpiarArchivo();
      setEstado("El archivo debe tener extensión .gpx.", "error");
      return;
    }
    if (archivo.size > MAX_BYTES) {
      limpiarArchivo();
      setEstado("El archivo supera el máximo de 5 MB.", "error");
      return;
    }

    mostrarNombre(archivo);
    setEstado("Leyendo archivo...", "");
    archivo
      .text()
      .then(function (texto) {
        var r = analizarGpx(texto);
        var km = r.km.toFixed(2);
        distancia.value = km;
        var msg = "Distancia calculada: " + km + " km";
        if (
          r.primeraFecha &&
          fecha &&
          (!fecha.max || r.primeraFecha <= fecha.max)
        ) {
          fecha.value = r.primeraFecha;
          msg += " · Fecha detectada: " + r.primeraFecha;
        }
        setEstado(msg, "ok");
      })
      .catch(function (e) {
        // Archivo inválido: se descarta para que no quede adjunto por error.
        inputGpx.value = "";
        distancia.value = "";
        mostrarNombre(null);
        setEstado(
          e && e.message ? e.message : "No se pudo leer el archivo GPX.",
          "error",
        );
      });
  });

  // Quitar el archivo elegido (por ejemplo, si se subió uno equivocado).
  btnQuitar.addEventListener("click", function () {
    distancia.value = "";
    if (fecha) {
      fecha.value = FECHA_ORIGINAL;
    }
    limpiarArchivo();
    inputGpx.focus();
  });

  origen.addEventListener("change", aplicarModo);
  aplicarModo();
})();
