package com.ProyectoBici.proyecto.e2e;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.BicicletaComponente;
import com.ProyectoBici.proyecto.entity.Componente;
import com.ProyectoBici.proyecto.entity.Rol;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.repository.BicicletaComponenteRepository;
import com.ProyectoBici.proyecto.repository.BicicletaRepository;
import com.ProyectoBici.proyecto.repository.ComponenteRepository;
import com.ProyectoBici.proyecto.repository.MantenimientoRepository;
import com.ProyectoBici.proyecto.repository.RolRepository;
import com.ProyectoBici.proyecto.repository.UsuarioRepository;
import com.ProyectoBici.proyecto.service.UsuarioService;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;

/**
 * Base de los tests E2E con Playwright.
 *
 * Levanta la aplicacion en un puerto aleatorio con el perfil "e2e"
 * (BD bikecare_e2e, ddl-auto=create-drop) y abre un Chromium headless.
 * Cada test corre en una pagina nueva para no compartir sesion.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class E2EBaseTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected RolRepository rolRepository;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    protected BicicletaRepository bicicletaRepository;

    @Autowired
    protected ComponenteRepository componenteRepository;

    @Autowired
    protected BicicletaComponenteRepository bicicletaComponenteRepository;

    @Autowired
    protected MantenimientoRepository mantenimientoRepository;

    @Autowired
    protected UsuarioService usuarioService;

    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    void iniciarNavegador() {
        sembrarDatosBase();
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    void cerrarNavegador() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void abrirPagina() {
        context = browser.newContext(
                new Browser.NewContextOptions().setLocale("es-PE"));
        page = context.newPage();
        page.setDefaultTimeout(15_000);
        page.setDefaultNavigationTimeout(15_000);
    }

    @AfterEach
    void cerrarPagina() {
        if (context != null) {
            context.close();
        }
    }

    // ------------------------------------------------------------------
    // Helpers de datos
    // ------------------------------------------------------------------

    /**
     * Con create-drop la BD arranca vacia: sin el rol "ciclista" el registro
     * falla, y sin componentes el modulo de instalacion no tiene que mostrar.
     * Es idempotente para poder llamarse en cada clase.
     */
    protected void sembrarDatosBase() {
        crearRolSiNoExiste("ciclista");
        crearRolSiNoExiste("taller");

        if (componenteRepository.count() == 0) {
            crearComponente("Cadena", "Transmision", new BigDecimal("3000"));
            crearComponente("Pastillas de freno", "Frenos", new BigDecimal("1500"));
        }
    }

    protected Rol crearRolSiNoExiste(String nombre) {
        return rolRepository.findByNombreRol(nombre).orElseGet(() -> {
            Rol rol = new Rol();
            rol.setNombreRol(nombre);
            return rolRepository.save(rol);
        });
    }

    protected Componente crearComponente(String nombre, String categoria, BigDecimal vidaUtil) {
        Componente componente = new Componente();
        componente.setNombre(nombre);
        componente.setCategoria(categoria);
        componente.setDescripcion(nombre);
        componente.setVidaUtilReferencial(vidaUtil);
        componente.setIcono(nombre.substring(0, 1).toUpperCase());
        return componenteRepository.save(componente);
    }

    protected Usuario crearUsuario(String nombre, String correo, String contrasenia) {
        usuarioService.guardarUsuario(nombre, "Prueba", correo, contrasenia);
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalStateException("Usuario no creado: " + correo));
    }

    protected Bicicleta crearBicicleta(Usuario usuario, String marca, String modelo, BigDecimal kilometraje) {
        Bicicleta bici = new Bicicleta();
        bici.setUsuario(usuario);
        bici.setMarca(marca);
        bici.setModelo(modelo);
        bici.setTipo("Urbana");
        bici.setKilometrajeTotal(kilometraje);
        return bicicletaRepository.save(bici);
    }

    /**
     * Instala en la bicicleta un componente del catalogo con el kilometraje de
     * instalacion y el actual indicados. La diferencia es el desgaste acumulado.
     */
    protected BicicletaComponente crearComponenteInstalado(Bicicleta bici, String nombreComponente,
            BigDecimal kmInstalacion, BigDecimal kmActual) {
        Componente componente = componenteRepository.findAll().stream()
                .filter(c -> nombreComponente.equals(c.getNombre()))
                .findFirst()
                .orElseGet(() -> crearComponente(nombreComponente, "General", new BigDecimal("3000")));

        BicicletaComponente bc = new BicicletaComponente();
        bc.setBicicleta(bici);
        bc.setComponente(componente);
        bc.setMarca("Shimano");
        bc.setModelo("HG601");
        bc.setFechaInstalacion(LocalDate.now().minusMonths(3));
        bc.setKilometrajeInstalacion(kmInstalacion);
        bc.setKilometrajeActual(kmActual);
        bc.setEstado("activo");
        return bicicletaComponenteRepository.save(bc);
    }

    protected String correoUnico(String prefijo) {
        return prefijo + "." + UUID.randomUUID() + "@e2e.test";
    }

    // ------------------------------------------------------------------
    // Helpers de navegacion
    // ------------------------------------------------------------------

    protected String url(String ruta) {
        return "http://localhost:" + port + ruta;
    }

    /** Inicia sesion por la UI y espera a llegar al inicio. */
    protected void iniciarSesion(String correo, String contrasenia) {
        page.navigate(url("/login"));
        page.locator("#email").fill(correo);
        page.locator("#password").fill(contrasenia);
        page.locator("button[type=submit]").click();
        // El primer redirect de Tomcat puede venir como /welcome;jsessionid=...
        try {
            page.waitForURL(Pattern.compile(".*/welcome(;jsessionid=[^/?#]+)?$"));
        } catch (PlaywrightException e) {
            throw new AssertionError("El login no llego a /welcome. URL actual: " + page.url()
                    + "\nTexto visible:\n" + page.locator("body").innerText(), e);
        }
    }
}
