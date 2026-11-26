package com.ProyectoBici.proyecto.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Fila de "Mis métodos registrados" ya lista para pintar (sin datos sensibles). */
@Getter
@AllArgsConstructor
public class MetodoPagoResumen {
    private final int id;
    private final String icono;
    private final String tipoEtiqueta;
    private final String detalle;
    private final String estadoEtiqueta;
    private final String estadoCss;
    private final LocalDateTime fechaRegistro;
}
