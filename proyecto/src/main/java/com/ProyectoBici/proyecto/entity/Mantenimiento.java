package com.ProyectoBici.proyecto.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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

@Getter
@Setter
@Entity
@Table(name = "mantenimiento")
public class Mantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mantenimiento")
    private int idMantenimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bicicleta", nullable = false)
    private Bicicleta bicicleta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_componente")
    private Componente componente;

    @Column(name = "tipo_servicio", nullable = false, length = 80)
    private String tipoServicio;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "costo", precision = 8, scale = 2)
    private BigDecimal costo = BigDecimal.ZERO;

    @Column(name = "kilometraje_servicio", precision = 10, scale = 2, nullable = false)
    private BigDecimal kilometrajeServicio;

    @Column(name = "fecha_servicio", nullable = false)
    private LocalDate fechaServicio;

    @Column(name = "taller_mecanico", length = 100)
    private String tallerMecanico;
}