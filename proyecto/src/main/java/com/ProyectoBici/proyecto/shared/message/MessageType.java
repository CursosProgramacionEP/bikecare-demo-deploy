package com.ProyectoBici.proyecto.shared.message;

/** Tipos de mensaje que entiende el fragmento fragments/alerts.html. */
public enum MessageType {

    SUCCESS("success", "Éxito"),
    WARNING("warning", "Advertencia"),
    ERROR("error", "Error"),
    INFO("info", "Información");

    private final String css;
    private final String defaultTitle;

    MessageType(String css, String defaultTitle) {
        this.css = css;
        this.defaultTitle = defaultTitle;
    }

    /** Sufijo de la clase CSS: app-alert--{css}. */
    public String getCss() {
        return css;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }
}
