// Diagnóstico IA: muestra el nombre del archivo elegido bajo "Archivos subidos".
(function () {
  var MAX_BYTES = 5 * 1024 * 1024;

  var fotoInput = document.getElementById("dia-foto");
  var audioInput = document.getElementById("dia-audio");
  if (!fotoInput || !audioInput) {
    return; // el módulo no está en pantalla
  }

  var vacio = document.getElementById("dia-adjuntos-vacio");
  var fotoPrev = document.getElementById("dia-foto-prev");
  var fotoNombre = document.getElementById("dia-foto-nombre");
  var audioPrev = document.getElementById("dia-audio-prev");
  var audioNombre = document.getElementById("dia-audio-nombre");

  function refrescar() {
    var hayFoto = !fotoPrev.hasAttribute("hidden");
    var hayAudio = !audioPrev.hasAttribute("hidden");
    if (hayFoto || hayAudio) {
      vacio.setAttribute("hidden", "");
    } else {
      vacio.removeAttribute("hidden");
    }
  }

  fotoInput.addEventListener("change", function () {
    var archivo = fotoInput.files && fotoInput.files[0];
    if (!archivo) {
      return;
    }
    if (archivo.size > MAX_BYTES) {
      alert("La foto supera los 5 MB.");
      fotoInput.value = "";
      return;
    }
    if (archivo.type !== "image/jpeg" && archivo.type !== "image/png") {
      alert("La foto debe ser JPG o PNG.");
      fotoInput.value = "";
      return;
    }
    fotoNombre.textContent = archivo.name || "foto";
    fotoPrev.removeAttribute("hidden");
    refrescar();
  });

  audioInput.addEventListener("change", function () {
    var archivo = audioInput.files && audioInput.files[0];
    if (!archivo) {
      return;
    }
    if (archivo.size > MAX_BYTES) {
      alert("El audio supera los 5 MB.");
      audioInput.value = "";
      return;
    }
    audioNombre.textContent = archivo.name || "audio";
    audioPrev.removeAttribute("hidden");
    refrescar();
  });

  document.getElementById("dia-foto-quitar").addEventListener("click", function () {
    fotoInput.value = "";
    fotoPrev.setAttribute("hidden", "");
    refrescar();
  });

  document.getElementById("dia-audio-quitar").addEventListener("click", function () {
    audioInput.value = "";
    audioPrev.setAttribute("hidden", "");
    refrescar();
  });
})();
