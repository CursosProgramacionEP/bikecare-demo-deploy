package com.ProyectoBici.proyecto.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Datos de un comprobante de pago emitido para un método de pago registrado.
 */
@Data
public class ComprobantePago {

    private int idComprobante;
    private String numeroComprobante;
    private LocalDateTime fechaEmision;
    private String nombreUsuario;
    private String correoUsuario;
    private String tipoMetodo;
    private String detalle;
    private String estado;
    private String concepto;
}
