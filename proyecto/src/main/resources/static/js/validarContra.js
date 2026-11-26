document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('.register-form');
  const password = document.getElementById('password');
  const confirmPassword = document.getElementById('confirmPassword');
  
  
  const errorMsgMatch = document.getElementById('passwordError');
 
  const errorMsgLength = document.getElementById('passwordErrorLenght');

  // Función de validación dinámica mientras el usuario escribe
  function validarPasswords() {
    const passVal = password.value;
    const confirmVal = confirmPassword.value;
    let esValido = true;

    // 1. Validar longitud de la contraseña principal (Primera casilla)
    if (passVal.length > 0 && passVal.length < 8) {
      errorMsgLength.textContent = 'La contraseña debe tener al menos 8 caracteres.';
      errorMsgLength.style.display = 'block';
      esValido = false;
    } else {
      errorMsgLength.style.display = 'none';
    }

    // 2. Validar coincidencia (Segunda casilla)
    if (confirmVal.length > 0 && passVal !== confirmVal) {
      errorMsgMatch.textContent = 'Las contraseñas no coinciden.';
      errorMsgMatch.style.display = 'block';
      esValido = false;
    } else {
      errorMsgMatch.style.display = 'none';
    }

    return esValido;
  }

  // Eventos en tiempo real
  password.addEventListener('input', validarPasswords);
  confirmPassword.addEventListener('input', validarPasswords);

  // Validación final al intentar enviar el formulario
  form.addEventListener('submit', (e) => {
    const passVal = password.value;
    const confirmVal = confirmPassword.value;
    let hayError = false;

    // Limpiar errores visuales
    errorMsgLength.style.display = 'none';
    errorMsgMatch.style.display = 'none';

    // Validación de la primera casilla
    if (passVal.length < 8) {
      e.preventDefault();
      errorMsgLength.textContent = 'La contraseña debe tener al menos 8 caracteres.';
      errorMsgLength.style.display = 'block';
      password.focus();
      hayError = true;
    }

    // Validación de la segunda casilla
    if (passVal !== confirmVal) {
      e.preventDefault();
      errorMsgMatch.textContent = 'Las contraseñas no coinciden.';
      errorMsgMatch.style.display = 'block';
      
      // Solo hace foco aquí si la primera casilla no tuvo error
      if (!hayError) {
        confirmPassword.focus();
      }
    }
  });
});