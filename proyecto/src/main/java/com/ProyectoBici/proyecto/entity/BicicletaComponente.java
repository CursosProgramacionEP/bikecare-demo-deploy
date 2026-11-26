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

/**
 * Componente instalado en una bici concreta.
 *
 * kilometrajeInstalacion y kilometrajeActual son lecturas del ODÓMETRO DE LA BICI:
 *   - kilometrajeInstalacion: km que tenía la bici cuando se instaló el componente.
 *   - kilometrajeActual: km de la bici en la última actualización.
 * Km recorridos por el componente = kilometrajeActual - kilometrajeInstalacion.
 */
@Getter
@Setter
@Entity
@Table(name = "bicicleta_componente")
public class BicicletaComponente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bicicleta_componente")
    private int idBicicletaComponente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bicicleta", nullable = false)
    private Bicicleta bicicleta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_componente", nullable = false)
    private Componente componente;

    @Column(name = "marca")
    private String marca;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "numero_serie")
    private String numeroSerie;

    @Column(name = "fecha_instalacion")
    private LocalDate fechaInstalacion;

    @Column(name = "kilometraje_instalacion")
    private BigDecimal kilometrajeInstalacion = BigDecimal.ZERO;

    @Column(name = "kilometraje_actual")
    private BigDecimal kilometrajeActual = BigDecimal.ZERO;

    @Column(name = "estado")
    private String estado = "activo";

    /**
     * Kilómetros acumulados por el componente desde su instalación.
     * Se calcula como kilometrajeActual - kilometrajeInstalacion.
     */
    public BigDecimal getKilometrajeAcumulado() {
        BigDecimal actual = kilometrajeActual != null ? kilometrajeActual : BigDecimal.ZERO;
        BigDecimal instalacion = kilometrajeInstalacion != null ? kilometrajeInstalacion : BigDecimal.ZERO;
        return actual.subtract(instalacion).max(BigDecimal.ZERO);
    }
}
