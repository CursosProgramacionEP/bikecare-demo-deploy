package com.ProyectoBici.proyecto.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.BicicletaComponente;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.microsoft.playwright.Locator;

/**
 * E2E 8: registrar un "Cambio de componente" desde el modulo de Mantenimiento.
 * Comprueba que el formulario exige elegir un componente, que muestra su
 * desgaste acumulado y que al guardar el acumulado vuelve a 0 km.
 */
@Tag("e2e")
class CambioComponenteE2ETest extends E2EBaseTest {

    private static final String CONTRASENIA = "ClaveSegura123";
    private static final String TIPO = "Cambio de componente";
    private static final String NOTAS = "Cambio de cadena por desgaste";

    private Usuario usuario;
    private Bicicleta bici;
    private BicicletaComponente componente;

    @BeforeAll
    void prepararDatos() {
        usuario = crearUsuario("Nestor", correoUnico("cambio"), CONTRASENIA);
        bici = crearBicicleta(usuario, "Specialized", "Sirrus 2.0", new BigDecimal("2000.00"));
        // Instalada a los 1000 km y con 2000 km hoy: 1000 km acumulados de desgaste.
        componente = crearComponenteInstalado(bici, "Cadena",
                new BigDecimal("1000.00"), new BigDecimal("2000.00"));
    }

    @Test
    void cambiarComponenteReiniciaSusKilometrosAcumulados() {
        iniciarSesion(usuario.getCorreo(), CONTRASENIA);
        page.navigate(url("/welcome/mantenimiento"));

        page.locator("#idBicicleta").selectOption(String.valueOf(bici.getIdBicicleta()));
        assertThat(page.locator("#kilometrajeServicio")).hasValue("2000.00");

        // Al inicio el bloque de componentes esta oculto.
        assertThat(page.locator("#campoComponentes")).isHidden();
        page.locator("#tipoServicio").selectOption(TIPO);
        assertThat(page.locator("#campoComponentes")).isVisible();

        Locator grupo = page.locator(".mant-componentes-grupo[data-bici='" + bici.getIdBicicleta() + "']");
        assertThat(grupo).isVisible();
        assertThat(grupo).containsText("Cadena");
        assertThat(grupo).containsText("1,000.0 km acumulados");

        // Sin marcar ningun componente el formulario no se envia.
        page.locator(".mant-btn").click();
        assertThat(page.locator("#componentesError")).isVisible();
        assertEquals(url("/welcome/mantenimiento"), page.url(),
                "No debe navegar si no se selecciono un componente");

        Locator casilla = grupo.locator("input[name=componentes]").first();
        assertEquals(String.valueOf(componente.getIdBicicletaComponente()),
                casilla.getAttribute("value"));
        casilla.check();
        assertThat(casilla).isChecked();

        page.locator("#costo").fill("80.00");
        page.locator("#descripcion").fill(NOTAS);
        page.locator(".mant-btn").click();
        page.waitForURL("**/welcome/mantenimiento");

        Locator reciente = page.locator(".mant-items .mant-item").first();
        assertThat(reciente).containsText(TIPO);
        assertThat(reciente).containsText("Specialized Sirrus 2.0");
        assertThat(reciente).containsText(NOTAS);

        // El componente se guardo como reemplazado hoy: el desgaste vuelve a 0.
        BicicletaComponente actualizado = bicicletaComponenteRepository
                .findById(componente.getIdBicicletaComponente())
                .orElseThrow(() -> new AssertionError("El componente ya no existe"));
        assertEquals(0, actualizado.getKilometrajeAcumulado().compareTo(BigDecimal.ZERO));
        assertEquals(0, new BigDecimal("2000.00").compareTo(actualizado.getKilometrajeInstalacion()));
        assertEquals(0, new BigDecimal("2000.00").compareTo(actualizado.getKilometrajeActual()));
    }
}
