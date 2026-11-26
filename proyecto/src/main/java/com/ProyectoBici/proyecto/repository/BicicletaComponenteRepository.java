package com.ProyectoBici.proyecto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ProyectoBici.proyecto.entity.BicicletaComponente;

@Repository
public interface BicicletaComponenteRepository extends JpaRepository<BicicletaComponente, Integer> {

    // Componentes activos de una bici (trae también el componente del catálogo)
    @Query("select bc from BicicletaComponente bc join fetch bc.componente "
            + "where bc.bicicleta.idBicicleta = :idBici and bc.estado = 'activo' "
            + "order by bc.idBicicletaComponente")
    List<BicicletaComponente> listarActivosDeBicicleta(@Param("idBici") int idBici);
}
