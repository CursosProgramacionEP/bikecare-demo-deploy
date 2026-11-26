package com.ProyectoBici.proyecto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Usuario
 */
@Data
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_usuario;
    private String nombre;
    private String apellidos;
    private String correo;
    private String contrasenia;

    @ManyToOne
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    /**
     * Plan Premium. Lo activa el administrador cuando aprueba un pago
     * y habilita el Diagnóstico IA.
     */
    @Column(name = "es_premium", nullable = false, columnDefinition = "boolean default false")
    private boolean esPremium = false;

    @Column(name = "intentos_fallidos", nullable = false, columnDefinition = "int default 0")
    private int intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;
}
