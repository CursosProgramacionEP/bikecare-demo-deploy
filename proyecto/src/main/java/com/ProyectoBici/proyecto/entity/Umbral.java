package com.ProyectoBici.proyecto.entity;

import java.math.BigDecimal;

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
 * Límite de kilómetros de un componente instalado.
 * Cuando el uso llega a porcentajeAlerta % del limiteKm se avisa; al 100 % toca cambiarlo.
 */
@Getter
@Setter
@Entity
@Table(name = "umbral")
public class Umbral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_umbral")
    private int idUmbral;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bicicleta_componente", nullable = false)
    private BicicletaComponente bicicletaComponente;

    @Column(name = "limite_km")
    private BigDecimal limiteKm;

    @Column(name = "porcentaje_alerta")
    private Integer porcentajeAlerta = 80;

    @Column(name = "activo")
    private Integer activo = 1;
}
