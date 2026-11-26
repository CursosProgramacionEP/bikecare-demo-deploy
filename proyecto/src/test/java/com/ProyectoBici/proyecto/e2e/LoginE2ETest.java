package com.ProyectoBici.proyecto.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.ProyectoBici.proyecto.entity.Usuario;

/**
 * E2E 2: login correcto. Entra con credenciales validas,
 * llega al inicio y puede abrir el panel de control.
 */
@Tag("e2e")
class LoginE2ETest extends E2EBaseTest {

    private static final String CONTRASENIA = "ClaveSegura123";

    private Usuario usuario;

    @BeforeAll
    void crearUsuarioDePrueba() {
        usuario = crearUsuario("Carlos", correoUnico("login"), CONTRASENIA);
    }

    @Test
    void loginCorrectoEntraYVeElPanel() {
        iniciarSesion(usuario.getCorreo(), CONTRASENIA);

        assertThat(page.locator(".user-name")).hasText("Hola, Carlos");

        page.navigate(url("/welcome/panelControl"));
        assertThat(page.locator(".container-panel h1")).hasText("Panel de Control");
        assertThat(page.locator(".container-panel .title-area p"))
                .containsText("No hay bicicletas registradas");
    }
}
