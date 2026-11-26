package com.ProyectoBici.proyecto.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ProyectoBici.proyecto.entity.Rol;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.repository.RolRepository;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private com.ProyectoBici.proyecto.repository.UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    public void testAutenticarExitoso() {
        String correo = "test@bici.com";
        String password = "123";

        Usuario usuarioMock = new Usuario();
        usuarioMock.setCorreo(correo);
        usuarioMock.setContrasenia(passwordEncoder.encode(password));

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuarioMock));

        Usuario resultado = usuarioService.autenticar(correo, password);

        assertNotNull(resultado);
        assertEquals(correo, resultado.getCorreo());
    }

    @Test
    public void testAutenticarPasswordIncorrecto() {
        String correo = "test@bici.com";
        
        Usuario usuarioMock = new Usuario();
        usuarioMock.setCorreo(correo);
        usuarioMock.setContrasenia(passwordEncoder.encode("passwordCorrecto"));

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuarioMock));

        // Intentamos con contraseña errónea
        Usuario resultado = usuarioService.autenticar(correo, "passwordMalo");

        assertNull(resultado);
    }

    @Test
    public void testAutenticarMigraContraseniaEnTextoPlano() {
        String correo = "legacy@bici.com";
        String password = "12345678";

        Usuario usuarioMock = new Usuario();
        usuarioMock.setCorreo(correo);
        usuarioMock.setContrasenia(password); // registro antiguo, sin hash

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuarioMock));

        Usuario resultado = usuarioService.autenticar(correo, password);

        assertNotNull(resultado);
        assertNotEquals(password, resultado.getContrasenia());
        assertTrue(passwordEncoder.matches(password, resultado.getContrasenia()));
        verify(usuarioRepository).save(usuarioMock);
    }

    @Test
    public void testEsTallerRolValido() {
        Usuario usuario = new Usuario();
        Rol rol = new Rol();
        rol.setNombreRol("taller");
        usuario.setRol(rol);

        boolean esTaller = UsuarioService.esTaller(usuario);
        assertTrue(esTaller);
    }
}
