package com.ProyectoBici.proyecto.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * E2E 1: un usuario nuevo se registra desde /registro y luego
 * puede iniciar sesion con esas credenciales.
 */
@Tag("e2e")
class RegistroE2ETest extends E2EBaseTest {

    @Test
    void registrarUsuarioYEntrarConLaCuentaCreada() {
        String correo = correoUnico("registro");
        String contrasenia = "ClaveSegura123";

        page.navigate(url("/registro"));
        page.locator("#firstname").fill("Ana");
        page.locator("#lastname").fill("Torres");
        page.locator("#email").fill(correo);
        page.locator("#password").fill(contrasenia);
        page.locator("#confirmPassword").fill(contrasenia);
        page.locator("#terms").check();
        page.locator("button[type=submit]").click();

        // El registro redirige al login.
        page.waitForURL("**/login");
        assertTrue(usuarioRepository.findByCorreo(correo).isPresent(),
                "El usuario deberia quedar guardado en la BD");

        // Y la cuenta recien creada sirve para entrar.
        iniciarSesion(correo, contrasenia);
        assertThat(page.locator(".user-name")).hasText("Hola, Ana");
    }
}
