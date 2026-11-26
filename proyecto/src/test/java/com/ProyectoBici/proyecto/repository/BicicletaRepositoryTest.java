package com.ProyectoBici.proyecto.repository;

import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.Rol;
import com.ProyectoBici.proyecto.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración de BicicletaRepository contra la BD real (MySQL).
 *
 * Restricciones que respeta este test:
 *  - bicicleta.id_usuario NOT NULL (FK) → la bici siempre tiene usuario
 *  - usuario.contrasenia  NOT NULL y usuario.id_rol NOT NULL → el usuario
 *    de apoyo se crea completo (antes el test fallaba por eso).
 * Al final se borra lo que se creó, en orden inverso a las dependencias.
 */
@SpringBootTest
public class BicicletaRepositoryTest {

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Inyectamos el repositorio de usuario para cumplir la relación

    @Autowired
    private RolRepository rolRepository;

    @Test
    public void testGuardarYBuscarBicicleta() {
        // 1. Creamos un rol y un usuario COMPLETOS para cumplir con las
        //    restricciones NOT NULL y la llave foránea obligatoria
        Rol rol = new Rol();
        rol.setNombreRol("Rol de prueba");
        rol = rolRepository.save(rol);

        Usuario usuario = new Usuario();
        usuario.setNombre("Prueba");
        usuario.setApellidos("UTP");
        usuario.setCorreo("bici_test_" + java.util.UUID.randomUUID() + "@correo.com");
        usuario.setContrasenia("clave-demo-123");
        usuario.setRol(rol);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 2. Creamos la bicicleta y le asociamos el usuario obligatorio
        Bicicleta bici = new Bicicleta();
        bici.setMarca("Trek");
        bici.setModelo("Marlin 8");
        bici.setUsuario(usuarioGuardado);

        // 3. Guardamos la bicicleta en la base de datos
        Bicicleta biciGuardada = bicicletaRepository.save(bici);

        try {
            // 4. Comprobamos que se generó un ID correctamente
            assertNotNull(biciGuardada.getIdBicicleta());

            // 5. Verificamos que se puede encontrar en la base de datos
            Optional<Bicicleta> biciEncontrada = bicicletaRepository.findById(biciGuardada.getIdBicicleta());
            assertTrue(biciEncontrada.isPresent());
            assertEquals("Trek", biciEncontrada.get().getMarca());
        } finally {
            // 6. Limpieza en orden inverso: bici → usuario → rol
            bicicletaRepository.delete(biciGuardada);
            usuarioRepository.delete(usuarioGuardado);
            rolRepository.delete(rol);
        }
    }
}
