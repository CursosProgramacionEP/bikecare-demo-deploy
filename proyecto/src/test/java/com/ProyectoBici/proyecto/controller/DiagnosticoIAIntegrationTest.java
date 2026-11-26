package com.ProyectoBici.proyecto.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.Rol;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.repository.BicicletaRepository;
import com.ProyectoBici.proyecto.repository.RolRepository;
import com.ProyectoBici.proyecto.repository.UsuarioRepository;

/**
 * Prueba de integración del módulo Diagnóstico IA.
 *
 * No depende de datos previos en la BD: antes de cada test se crean un rol,
 * un usuario y una bicicleta propios. Con @Transactional todo se revierte
 * al terminar cada test, así que no queda basura entre pruebas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "gemini.api.key=") // fuerza el fallback local
class DiagnosticoIAIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BicicletaRepository bicicletaRepository;

    private Usuario usuario;
    private Bicicleta bici;

    @BeforeEach
    void prepararDatos() {
        Rol rol = new Rol();
        rol.setNombreRol("Rol de prueba");
        rol = rolRepository.save(rol);

        usuario = new Usuario();
        usuario.setNombre("prueba");
        usuario.setApellidos("UTP");
        usuario.setCorreo("diag_" + UUID.randomUUID() + "@correo.com");
        usuario.setContrasenia("clave-demo-123");
        usuario.setRol(rol);
        usuario.setEsPremium(true); // el módulo Diagnóstico IA es Premium
        usuario = usuarioRepository.save(usuario);

        bici = new Bicicleta();
        bici.setMarca("Trek");
        bici.setModelo("Marlin 8");
        bici.setUsuario(usuario);
        bici = bicicletaRepository.save(bici);
    }

    private Usuario usuarioEnSesion() {
        return usuario;
    }

    private String idBici() {
        return String.valueOf(bici.getIdBicicleta());
    }

    @Test
    void getMuestraElFormulario() throws Exception {
        mockMvc.perform(get("/welcome/diagnosticoIA")
                .sessionAttr("usuarioLogueado", usuarioEnSesion()))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("bicicletas", "mostrarResultado"))
                .andExpect(model().attribute("mostrarResultado", false));
    }

    @Test
    void postValidoDevuelveResultadoLocal() throws Exception {
        mockMvc.perform(multipart("/welcome/diagnosticoIA/analizar")
                .with(csrf())
                .param("idBicicleta", idBici())
                .param("descripcion", "la cadena salta al cambiar de piñon")
                .param("componentes", "Cadena")
                .sessionAttr("usuarioLogueado", usuarioEnSesion()))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attribute("mostrarResultado", true))
                .andExpect(model().attribute("fuenteDiagnostico", "local"))
                .andExpect(model().attributeExists("resultado"));
    }

    @Test
    void postSinBicicletaRedirigeConError() throws Exception {
        mockMvc.perform(multipart("/welcome/diagnosticoIA/analizar")
                .with(csrf())
                .param("descripcion", "ruido al frenar")
                .sessionAttr("usuarioLogueado", usuarioEnSesion()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome/diagnosticoIA"));
    }

    @Test
    void postSinEvidenciaRedirigeConError() throws Exception {
        mockMvc.perform(multipart("/welcome/diagnosticoIA/analizar")
                .with(csrf())
                .param("idBicicleta", idBici())
                .sessionAttr("usuarioLogueado", usuarioEnSesion()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome/diagnosticoIA?biciId=" + idBici()));
    }

    @Test
    void postDescripcionMuyLargaRedirigeConError() throws Exception {
        String larga = "a".repeat(2001);
        mockMvc.perform(multipart("/welcome/diagnosticoIA/analizar")
                .with(csrf())
                .param("idBicicleta", idBici())
                .param("descripcion", larga)
                .sessionAttr("usuarioLogueado", usuarioEnSesion()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome/diagnosticoIA?biciId=" + idBici()));
    }

    @Test
    void postConFotoInvalidaRedirigeConError() throws Exception {
        MockMultipartFile foto = new MockMultipartFile("foto", "doc.pdf", "application/pdf", "x".getBytes());
        mockMvc.perform(multipart("/welcome/diagnosticoIA/analizar")
                .with(csrf())
                .file(foto)
                .param("idBicicleta", idBici())
                .param("descripcion", "ruido")
                .sessionAttr("usuarioLogueado", usuarioEnSesion()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome/diagnosticoIA?biciId=" + idBici()));
    }

    @Test
    void postConFotoValidaDevuelveResultado() throws Exception {
        MockMultipartFile foto = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", new byte[] { 1, 2, 3 });
        mockMvc.perform(multipart("/welcome/diagnosticoIA/analizar")
                .with(csrf())
                .file(foto)
                .param("idBicicleta", idBici())
                .sessionAttr("usuarioLogueado", usuarioEnSesion()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("mostrarResultado", true))
                .andExpect(model().attribute("fuenteDiagnostico", "local"));
    }

    @Test
    void postSinSesionRedirigeALogin() throws Exception {
        mockMvc.perform(multipart("/welcome/diagnosticoIA/analizar")
                .with(csrf())
                .param("idBicicleta", idBici())
                .param("descripcion", "ruido"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}