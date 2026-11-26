package com.ProyectoBici.proyecto.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Catálogo de componentes (cadena, pastillas, etc.).
 * vidaUtilReferencial está en KILÓMETROS.
 */
@Getter
@Setter
@Entity
@Table(name = "componente")
public class Componente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_componente")
    private int idComponente;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "vida_util_referencial")
    private BigDecimal vidaUtilReferencial;

    @Column(name = "icono")
    private String icono;
}
