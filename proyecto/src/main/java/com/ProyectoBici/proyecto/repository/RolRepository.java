package com.ProyectoBici.proyecto.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ProyectoBici.proyecto.entity.Rol;

// HU02: Inicio de sesion y control por rol
@Repository 
public interface RolRepository extends JpaRepository<Rol,Integer>{
    Optional<Rol> findByNombreRol(String nombreRol);
}
