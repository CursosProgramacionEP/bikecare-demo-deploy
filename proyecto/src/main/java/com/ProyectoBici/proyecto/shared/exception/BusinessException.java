package com.ProyectoBici.proyecto.shared.exception;

import com.ProyectoBici.proyecto.shared.message.MessageType;

import lombok.Getter;

/**
 * Error "esperable" (validación, regla de negocio). Su mensaje se muestra tal cual al usuario.
 * {@link GlobalExceptionHandler} lo convierte en un mensaje flash y redirige.
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final MessageType type;
    /** Ruta a la que redirigir (ej. "/login"). null = volver a la página anterior. */
    private final String redirectTo;

    public BusinessException(String message) {
        this(MessageType.ERROR, message, null);
    }

    public BusinessException(MessageType type, String message) {
        this(type, message, null);
    }

    public BusinessException(MessageType type, String message, String redirectTo) {
        super(message);
        this.type = type;
        this.redirectTo = redirectTo;
    }
}

