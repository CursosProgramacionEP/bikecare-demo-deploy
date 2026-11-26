package com.ProyectoBici.proyecto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ProyectoBici.proyecto.entity.EstadoMetodoPago;
import com.ProyectoBici.proyecto.entity.MetodoPago;
import com.ProyectoBici.proyecto.entity.TipoMetodoPago;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {

    // Todos los métodos de un usuario (el más nuevo primero)
    @Query("select m from MetodoPago m where m.usuario.id_usuario = :idUsuario "
            + "order by m.fechaRegistro desc, m.idMetodoPago desc")
    List<MetodoPago> listarDeUsuario(@Param("idUsuario") int idUsuario);

    // Un método, pero SOLO si pertenece a ese usuario (evita ver/borrar métodos ajenos)
    @Query("select m from MetodoPago m where m.idMetodoPago = :idMetodo and m.usuario.id_usuario = :idUsuario")
    Optional<MetodoPago> buscarDeUsuario(@Param("idMetodo") int idMetodo, @Param("idUsuario") int idUsuario);

    // Panel de administración: pagos en un estado dado, el más antiguo primero (con su usuario ya cargado)
    @Query("select m from MetodoPago m join fetch m.usuario where m.estado = :estado "
            + "order by m.fechaRegistro asc, m.idMetodoPago asc")
    List<MetodoPago> listarPorEstado(@Param("estado") EstadoMetodoPago estado);

    // Un número de operación de Plin/Yape no puede registrarse dos veces
    boolean existsByTipoAndNumeroOperacion(TipoMetodoPago tipo, String numeroOperacion);

    // La misma tarjeta (marca + últimos 4 + vencimiento) no se registra dos veces por usuario
    @Query("select count(m) > 0 from MetodoPago m where m.usuario.id_usuario = :idUsuario "
            + "and m.tipo = :tipo and m.marcaTarjeta = :marca and m.ultimosDigitos = :ultimos "
            + "and m.mesExpiracion = :mes and m.anioExpiracion = :anio")
    boolean existeTarjeta(@Param("idUsuario") int idUsuario, @Param("tipo") TipoMetodoPago tipo,
            @Param("marca") String marca, @Param("ultimos") String ultimos,
            @Param("mes") int mes, @Param("anio") int anio);
}