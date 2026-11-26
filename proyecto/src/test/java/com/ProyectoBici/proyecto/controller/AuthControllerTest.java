package com.ProyectoBici.proyecto.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void testProcesarLoginExitoso() throws Exception {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setCorreo("test@bici.com");

        when(usuarioService.autenticar("test@bici.com", "123")).thenReturn(usuarioMock);

        mockMvc.perform(post("/login")
                .param("correo", "test@bici.com")
                .param("contrasenia", "123")) // <--- Corregido a contrasenia
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));
    }

    @Test
    public void testProcesarLoginFallido() throws Exception {
        when(usuarioService.autenticar(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/login")
                .param("correo", "malo@bici.com")
                .param("contrasenia", "000")) // <--- Corregido a contrasenia
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    public void testLogoutInvalidaLaSesion() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuarioLogueado", new Usuario());

        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        assertTrue(session.isInvalid());
    }
}