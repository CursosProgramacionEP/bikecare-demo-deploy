package com.ProyectoBici.proyecto.repository;

import com.ProyectoBici.proyecto.entity.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RolRepositoryTest {

    @Autowired
    private RolRepository rolRepository;

    @Test
    public void testGuardarYBuscarRol() {
        // 1. Creamos un rol de prueba
        Rol rol = new Rol();
        
        // Usamos un método genérico o el id/nombre predeterminado
        // Si tu entidad Rol tiene otro atributo, lo ajustamos, pero por ahora guardamos la entidad base:
        Rol rolGuardado = rolRepository.save(rol);

        // 2. Comprobamos que se asignó un ID (asumiendo que usa getIdRol() al igual que Bicicleta usa getIdBicicleta())
        assertNotNull(rolGuardado.getIdRol()); 

        // 3. Verificamos que se puede encontrar en la base de datos
        Optional<Rol> rolEncontrado = rolRepository.findById(rolGuardado.getIdRol());
        assertTrue(rolEncontrado.isPresent());
    }
}