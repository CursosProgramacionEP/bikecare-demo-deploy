package com.ProyectoBici.proyecto.entity;

/** Métodos de pago que acepta BikeCare. */
public enum TipoMetodoPago {

    TARJETA("Tarjeta de crédito/débito", "💳"),
    PLIN("Plin", "📱"),
    YAPE("Yape", "📱");

    private final String etiqueta;
    private final String icono;

    TipoMetodoPago(String etiqueta, String icono) {
        this.etiqueta = etiqueta;
        this.icono = icono;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getIcono() {
        return icono;
    }

    /** Plin y Yape se pagan con QR y se registran con su número de operación. */
    public boolean esBilletera() {
        return this != TARJETA;
    }
}
