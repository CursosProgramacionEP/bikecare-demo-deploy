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
import com.ProyectoBici.proyecto.entity.Mantenimiento;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.microsoft.playwright.Locator;

/**
 * E2E 7: registrar un mantenimiento simple (sin componentes) desde el modulo
 * de Mantenimiento y comprobar que aparece en la lista de recientes y en el
 * historial, con su costo sumado al total acumulado.
 */
@Tag("e2e")
class RegistrarMantenimientoE2ETest extends E2EBaseTest {

    private static final String CONTRASENIA = "ClaveSegura123";
    private static final String TIPO = "Preventivo";
    private static final String NOTAS = "Revision de frenos y cadena";

    private Usuario usuario;
    private Bicicleta bici;

    @BeforeAll
    void prepararDatos() {
        usuario = crearUsuario("Marta", correoUnico("mantenimiento"), CONTRASENIA);
        bici = crearBicicleta(usuario, "Trek", "Marlin 8", new BigDecimal("1250.00"));
    }

    @Test
    void registrarMantenimientoSimpleLoMuestraEnListaEHistorial() {
        iniciarSesion(usuario.getCorreo(), CONTRASENIA);
        page.navigate(url("/welcome/mantenimiento"));

        page.locator("#idBicicleta").selectOption(String.valueOf(bici.getIdBicicleta()));
        // El JS completa el odometro con el kilometraje actual de la bici.
        assertThat(page.locator("#kilometrajeServicio")).hasValue("1250.00");

        page.locator("#tipoServicio").selectOption(TIPO);
        // Un mantenimiento simple no pide componentes.
        assertThat(page.locator("#campoComponentes")).isHidden();

        page.locator("#costo").fill("45.50");
        page.locator("#tallerMecanico").fill("Taller Central");
        page.locator("#descripcion").fill(NOTAS);
        page.locator(".mant-btn").click();

        page.waitForURL("**/welcome/mantenimiento");

        Locator reciente = page.locator(".mant-items .mant-item").first();
        assertThat(reciente).containsText(TIPO);
        assertThat(reciente).containsText("Trek Marlin 8");
        assertThat(reciente).containsText("1250.00 km");
        assertThat(reciente).containsText("S/ 45.50");
        assertThat(reciente).containsText(NOTAS);
        assertEquals(1, page.locator(".mant-items .mant-item").count(),
                "Solo deberia existir el mantenimiento recien creado");

        // Se guardo en la BD con los datos del formulario.
        List<Mantenimiento> guardados = mantenimientoRepository.listarPorUsuario(usuario.getId_usuario());
        assertEquals(1, guardados.size());
        assertEquals(TIPO, guardados.get(0).getTipoServicio());
        assertEquals(0, new BigDecimal("45.50").compareTo(guardados.get(0).getCosto()));
        assertEquals("Taller Central", guardados.get(0).getTallerMecanico());
        assertEquals(NOTAS, guardados.get(0).getDescripcion());

        // Y aparece en el historial con su costo y el total acumulado.
        page.navigate(url("/welcome/historial"));
        Locator fila = page.locator(".tabla-mantenimiento tbody tr").first();
        assertThat(fila).containsText("Trek Marlin 8");
        assertThat(fila.locator(".tipo-badge")).hasText(TIPO);
        assertThat(fila.locator(".col-costo")).hasText("S/ 45.50");
        assertThat(fila).containsText("Taller Central");
        assertThat(fila).containsText("1,250.0 km");
        assertTrue(page.getByText("S/ 45.50").count() >= 2,
                "El costo debe verse en la fila y en el total acumulado");
    }
}
