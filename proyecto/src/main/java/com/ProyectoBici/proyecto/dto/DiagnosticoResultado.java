package com.ProyectoBici.proyecto.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Resultado preliminar del módulo Diagnóstico IA (mock basado en reglas).
 * No se persiste: solo se muestra en la parte inferior del módulo.
 */
@Getter
@AllArgsConstructor
public class DiagnosticoResultado {

    /** BAJO, MEDIO o ALTO (para la barra y la lógica). */
    private final String severidad;

    /** Etiqueta visible: "Bajo", "Media", "Alta". */
    private final String etiqueta;

    /** Mensaje sobre uso seguro de la bici. */
    private final String mensajeUso;

    /** "Posible causa": título corto. */
    private final String causaTitulo;

    /** "Posible causa": explicación basada en los síntomas. */
    private final String causaDetalle;

    /** "Soluciones propuestas": pasos ordenados. */
    private final List<Paso> soluciones;

    @Getter
    @AllArgsConstructor
    public static class Paso {
        private final String titulo;
        private final String detalle;
    }
}
