package com.ProyectoBici.proyecto.shared.message;

import java.io.Serializable;

import lombok.Getter;

/**
 * Mensaje para el usuario (éxito, advertencia, error o info).
 * Es Serializable porque los flash attributes viajan por la sesión HTTP.
 */
@Getter
public class AppMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final MessageType type;
    private final String title;
    private final String text;
    /** true = se muestra en un modal en lugar de una alerta en línea. */
    private final boolean modal;

    private AppMessage(MessageType type, String title, String text, boolean modal) {
        this.type = type;
        this.title = title;
        this.text = text;
        this.modal = modal;
    }

    public static AppMessage of(MessageType type, String text) {
        return new AppMessage(type, type.getDefaultTitle(), text, false);
    }

    public static AppMessage success(String text) {
        return of(MessageType.SUCCESS, text);
    }

    public static AppMessage warning(String text) {
        return of(MessageType.WARNING, text);
    }

    public static AppMessage error(String text) {
        return of(MessageType.ERROR, text);
    }

    public static AppMessage info(String text) {
        return of(MessageType.INFO, text);
    }

    public static AppMessage errorModal(String title, String text) {
        return new AppMessage(MessageType.ERROR, title, text, true);
    }
}
