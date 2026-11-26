package com.ProyectoBici.proyecto.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.microsoft.playwright.options.SelectOption;

/**
 * E2E 5: registrar una bicicleta desde Mi Garaje y verla
 * en el garaje y en el panel de control.
 */
@Tag("e2e")
class RegistrarBicicletaE2ETest extends E2EBaseTest {

    private static final String CONTRASENIA = "ClaveSegura123";

    private Usuario usuario;

    @BeforeAll
    void crearUsuarioDePrueba() {
        usuario = crearUsuario("Diego", correoUnico("bici"), CONTRASENIA);
    }

    @Test
    void registrarBicicletaLaMuestraEnGarajeYPanel() {
        iniciarSesion(usuario.getCorreo(), CONTRASENIA);

        page.navigate(url("/welcome/garaje"));
        page.locator("a.btn-primary").click();
        page.waitForURL("**/welcome/garaje/registrarBici");

        page.locator("#marca").fill("Trek");
        page.locator("#modelo").fill("Marlin 8");
        page.locator("#tipo").selectOption(new SelectOption().setIndex(1));
        page.locator("#kilometraje").fill("1500");
        page.locator("button.btn-guardar").click();

        // Vuelve al garaje con la bici nueva.
        page.waitForURL("**/welcome/garaje");
        assertThat(page.locator(".bike-card-title")).hasText("Trek Marlin 8");
        assertThat(page.locator(".bike-card-meta")).containsText("1,500.0 km");

        Bicicleta bici = bicicletaRepository.listarDeUsuario(usuario.getId_usuario()).get(0);
        assertEquals("Trek", bici.getMarca());
        assertEquals("Marlin 8", bici.getModelo());
        assertEquals(0, new BigDecimal("1500.00").compareTo(bici.getKilometrajeTotal()));

        // Y el panel de control la cuenta.
        page.navigate(url("/welcome/panelControl"));
        assertThat(page.locator(".container-panel .title-area p"))
                .hasText("Tienes 1 bicicleta registrada.");
        assertEquals(3, page.locator(".container-panel .stat-card").count(),
                "El panel debe mostrar 3 tarjetas de resumen");
    }
}
