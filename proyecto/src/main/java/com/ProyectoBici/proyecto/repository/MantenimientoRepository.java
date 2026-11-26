package com.ProyectoBici.proyecto.repository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ProyectoBici.proyecto.entity.Mantenimiento;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Integer> {

    // Lista el historial de mantenimientos del usuario ordenado del más reciente al más antiguo
    @Query("select m from Mantenimiento m "
         + "join fetch m.bicicleta b "
         + "left join fetch m.componente c "
         + "where b.usuario.id_usuario = :idUsuario "
         + "order by m.fechaServicio desc, m.idMantenimiento desc")
    List<Mantenimiento> listarPorUsuario(@Param("idUsuario") int idUsuario);

    // Ultimos mantenimientos del usuario, limitados directamente en la consulta
    @Query("select m from Mantenimiento m "
         + "join fetch m.bicicleta b "
         + "left join fetch m.componente c "
         + "where b.usuario.id_usuario = :idUsuario "
         + "order by m.fechaServicio desc, m.idMantenimiento desc")
    List<Mantenimiento> listarUltimosPorUsuario(@Param("idUsuario") int idUsuario, Pageable pageable);

    // Filtra el historial para una bicicleta específica del usuario
    @Query("select m from Mantenimiento m "
         + "join fetch m.bicicleta b "
         + "left join fetch m.componente c "
         + "where b.usuario.id_usuario = :idUsuario and b.idBicicleta = :idBici "
         + "order by m.fechaServicio desc, m.idMantenimiento desc")
    List<Mantenimiento> listarPorBicicletaDeUsuario(@Param("idUsuario") int idUsuario, @Param("idBici") int idBici);

    // Consulta la suma de costos acumulados para un usuario
    @Query("select COALESCE(SUM(m.costo), 0) from Mantenimiento m "
         + "join m.bicicleta b "
         + "where b.usuario.id_usuario = :idUsuario")
    BigDecimal sumarCostosAcumuladosPorUsuario(@Param("idUsuario") int idUsuario);
}