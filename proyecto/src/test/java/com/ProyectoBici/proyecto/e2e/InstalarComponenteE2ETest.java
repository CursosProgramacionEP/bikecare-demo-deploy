package com.ProyectoBici.proyecto.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.BicicletaComponente;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.microsoft.playwright.Locator;

/**
 * E2E 6: instalar un componente en una bicicleta desde Mi Garaje.
 * Comprueba que se guarda con el kilometraje actual de la bici como base.
 */
@Tag("e2e")
class InstalarComponenteE2ETest extends E2EBaseTest {

    private static final String CONTRASENIA = "ClaveSegura123";

    private Usuario usuario;
    private Bicicleta bici;

    @BeforeAll
    void prepararBicicleta() {
        usuario = crearUsuario("Lucia", correoUnico("componente"), CONTRASENIA);
        bici = crearBicicleta(usuario, "Giant", "Escape 3", new BigDecimal("1200.00"));
    }

    @Test
    void instalarComponenteLoMuestraEnElGaraje() {
        iniciarSesion(usuario.getCorreo(), CONTRASENIA);

        page.navigate(url("/welcome/garaje"));
        page.locator("a.btn-card").click();
        page.waitForURL("**/welcome/garaje/" + bici.getIdBicicleta() + "/componentes");

        Locator componente = page.locator("input[name='componentes']:not(:disabled)").first();
        assertTrue(componente.count() > 0, "El catalogo deberia tener componentes disponibles");
        // El checkbox nativo esta oculto por CSS: se pulsa su etiqueta, como el usuario.
        componente.locator("xpath=..").click();
        assertThat(componente).isChecked();

        String idComponente = componente.getAttribute("value");
        page.locator("#marca_" + idComponente).fill("Shimano");
        page.locator("#modelo_" + idComponente).fill("HG601");

        assertThat(page.locator("#btnGuardarComponentes")).isEnabled();
        page.locator("#btnGuardarComponentes").click();
        page.waitForURL("**/welcome/garaje");

        assertThat(page.locator(".bike-components-title")).containsText("Componentes (1)");
        assertThat(page.locator(".component-name").first()).containsText("Cadena");

        List<BicicletaComponente> activos =
                bicicletaComponenteRepository.listarActivosDeBicicleta(bici.getIdBicicleta());
        assertEquals(1, activos.size());
        assertEquals("Shimano", activos.get(0).getMarca());
        assertEquals("HG601", activos.get(0).getModelo());
        // Se instalo sin fecha ni km: se asume el km actual de la bici -> desgaste 0.
        assertEquals(0, activos.get(0).getKilometrajeAcumulado().compareTo(BigDecimal.ZERO));
    }
}
