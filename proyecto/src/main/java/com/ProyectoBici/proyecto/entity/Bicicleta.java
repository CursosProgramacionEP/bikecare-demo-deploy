package com.ProyectoBici.proyecto.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Bicicleta de un usuario. Su kilometraje_total es la base para calcular el
 * desgaste de cada componente (el mantenimiento es SOLO por kilometraje).
 */
@Getter
@Setter
@Entity
@Table(name = "bicicleta")
public class Bicicleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bicicleta")
    private int idBicicleta;

    // Dueño de la bici (el usuario que hizo login)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "marca")
    private String marca;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "numero_serie")
    private String numeroSerie;

    @Column(name = "kilometraje_total")
    private BigDecimal kilometrajeTotal = BigDecimal.ZERO;

    @Column(name = "terreno_habitual")
    private String terrenoHabitual;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "estado")
    private String estado = "activa";

    // Datos informativos: NO influyen en el mantenimiento
    @Column(name = "peso")
    private BigDecimal peso;

    @Column(name = "transmision")
    private String transmision;

    @Column(name = "tamano_cuadro")
    private String tamanoCuadro;

    @Column(name = "notas")
    private String notas;

    // Nombre del archivo de la foto (se guarda en la carpeta uploads/bicicletas)
    @Column(name = "foto")
    private String foto;
}
