package com.ProyectoBici.proyecto.shared.exception;

import com.ProyectoBici.proyecto.shared.message.MessageType;

/** No hay sesión iniciada. Redirige al login con un aviso. */
public class UnauthorizedException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException() {
        this("Tu sesión no está activa. Inicia sesión para continuar.");
    }

    public UnauthorizedException(String message) {
        super(MessageType.WARNING, message, "/login");
    }
}
