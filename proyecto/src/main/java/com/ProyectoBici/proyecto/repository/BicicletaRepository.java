package com.ProyectoBici.proyecto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ProyectoBici.proyecto.entity.Bicicleta;

//HU04: Registrar ruta de bici
@Repository
public interface BicicletaRepository extends JpaRepository<Bicicleta, Integer> {

    // Todas las bicis de un usuario (la más nueva primero)
    @Query("select b from Bicicleta b where b.usuario.id_usuario = :idUsuario "
            + "order by b.fechaRegistro desc, b.idBicicleta desc")
    List<Bicicleta> listarDeUsuario(@Param("idUsuario") int idUsuario);

    // Una bici, pero SOLO si pertenece a ese usuario (evita ver/modificar bicis ajenas)
    @Query("select b from Bicicleta b where b.idBicicleta = :idBici and b.usuario.id_usuario = :idUsuario")
    Optional<Bicicleta> buscarDeUsuario(@Param("idBici") int idBici, @Param("idUsuario") int idUsuario);
}
