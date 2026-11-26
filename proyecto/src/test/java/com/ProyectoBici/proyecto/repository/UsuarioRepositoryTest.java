package com.ProyectoBici.proyecto.repository;

import com.ProyectoBici.proyecto.entity.Rol;
import com.ProyectoBici.proyecto.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración de UsuarioRepository contra la BD real (MySQL).
 *
 * Restricciones de la tabla usuario que este test debe respetar:
 *  - contrasenia NOT NULL
 *  - id_rol      NOT NULL (FK hacia rol)
 *  - correo      UNIQUE   → por eso se genera un correo distinto en cada
 *                           ejecución con UUID y no un correo fijo.
 * Al final se borra lo que se creó para no dejar datos de prueba en la BD.
 */
@SpringBootTest
public class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Test
    public void testFindByCorreo() {
        // 1. Arrange: rol obligatorio (FK) + usuario con TODOS los campos requeridos
        Rol rol = new Rol();
        rol.setNombreRol("Rol de prueba");
        rol = rolRepository.save(rol);

        String correo = "test_utp_" + UUID.randomUUID() + "@correo.com";

        Usuario usuario = new Usuario();
        usuario.setNombre("Prueba");
        usuario.setApellidos("UTP");
        usuario.setCorreo(correo);
        usuario.setContrasenia("clave-demo-123");
        usuario.setRol(rol);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        try {
            // 2. Act: buscamos por el correo que acabamos de crear
            Optional<Usuario> usuarioEncontrado = usuarioRepository.findByCorreo(correo);

            // 3. Assert: se encontró y coincide con el guardado
            assertTrue(usuarioEncontrado.isPresent(), "El usuario debería haber sido encontrado");
            assertEquals(correo, usuarioEncontrado.get().getCorreo());
            assertEquals(usuarioGuardado.getId_usuario(), usuarioEncontrado.get().getId_usuario());
        } finally {
            // 4. Limpieza: primero el usuario (depende del rol) y luego el rol
            usuarioRepository.delete(usuarioGuardado);
            rolRepository.delete(rol);
        }
    }
}
