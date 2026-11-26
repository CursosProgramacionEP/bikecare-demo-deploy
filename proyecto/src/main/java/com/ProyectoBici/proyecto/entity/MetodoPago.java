package com.ProyectoBici.proyecto.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Método de pago (o comprobante de pago) de un usuario.
 *
 * SEGURIDAD: nunca se guarda el número completo de la tarjeta ni el CVV.
 * Solo marca, últimos 4 dígitos, titular y vencimiento.
 */
@Getter
@Setter
@Entity
@Table(name = "metodo_pago", uniqueConstraints = @UniqueConstraint(name = "uk_metodo_pago_operacion", columnNames = {
        "tipo", "numero_operacion" }))
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_pago")
    private int idMetodoPago;

    // Dueño del método de pago (el usuario que hizo login)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoMetodoPago tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoMetodoPago estado;

    // ---- Solo TARJETA ----
    @Column(name = "titular", length = 100)
    private String titular;

    @Column(name = "marca_tarjeta", length = 20)
    private String marcaTarjeta;

    @Column(name = "ultimos_digitos", length = 4)
    private String ultimosDigitos;

    @Column(name = "mes_expiracion")
    private Integer mesExpiracion;

    @Column(name = "anio_expiracion")
    private Integer anioExpiracion;

    // ---- Solo PLIN / YAPE ----
    @Column(name = "numero_operacion", length = 20)
    private String numeroOperacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
