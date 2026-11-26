package com.ProyectoBici.proyecto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ProyectoBici.proyecto.entity.Usuario;

@Repository 
public interface UsuarioRepository 
extends JpaRepository<Usuario,Integer>{
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
    Optional<Usuario> findByResetToken(String resetToken);

    /** Todos los usuarios que tienen un rol con ese nombre (ignora mayúsculas). */
    List<Usuario> findByRolNombreRolIgnoreCase(String nombreRol);
}
