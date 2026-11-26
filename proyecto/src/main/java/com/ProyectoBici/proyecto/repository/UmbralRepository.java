package com.ProyectoBici.proyecto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ProyectoBici.proyecto.entity.Umbral;


@Repository
public interface UmbralRepository 
extends JpaRepository<Umbral, Integer> {

    @Query("select u from Umbral u "
    +"where u.bicicletaComponente.idBicicletaComponente = :idBc "
    + "and u.activo = 1 order by u.idUmbral")
    List<Umbral> listarActivosDeComponente(@Param("idBc") int idBc);
}

