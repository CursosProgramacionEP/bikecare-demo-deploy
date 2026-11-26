package com.ProyectoBici.proyecto.entity;

/**
 * Estado del método de pago.
 * Tarjeta -> ACTIVO. Plin/Yape -> PENDIENTE_VERIFICACION hasta que el admin la confirme (APROBADO).
 */
public enum EstadoMetodoPago {

    ACTIVO("Activo", "activo"),
    PENDIENTE_VERIFICACION("Pendiente de verificación", "pendiente"),
    // El administrador confirmó la operación. Reutiliza el estilo verde de "activo".
    APROBADO("Aprobado", "activo");

    private final String etiqueta;
    private final String css;

    EstadoMetodoPago(String etiqueta, String css) {
        this.etiqueta = etiqueta;
        this.css = css;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Sufijo de la clase CSS: pago-badge--{css}. */
    public String getCss() {
        return css;
    }
}
