package com.ProyectoBici.proyecto.shared.exception;

import com.ProyectoBici.proyecto.shared.message.MessageType;

/** Un recurso (bicicleta, componente, usuario...) no existe o no pertenece al usuario. */
public class ResourceNotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(MessageType.WARNING, message);
    }

    public ResourceNotFoundException(String message, String redirectTo) {
        super(MessageType.WARNING, message, redirectTo);
    }
}
