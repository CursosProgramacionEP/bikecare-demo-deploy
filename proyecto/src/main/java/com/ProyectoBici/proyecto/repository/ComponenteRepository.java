package com.ProyectoBici.proyecto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ProyectoBici.proyecto.entity.Componente;


@Repository
public interface ComponenteRepository extends JpaRepository<Componente, Integer> {

    List<Componente> findAllByOrderByIdComponenteAsc();
}
