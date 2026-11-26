package com.ProyectoBici.proyecto.shared.notification;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.ProyectoBici.proyecto.shared.message.MessageType;

import lombok.Getter;

/** Aviso del sistema dirigido a un usuario (ej. "cadena al 90% de desgaste"). */
@Getter
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id = UUID.randomUUID().toString();
    private final int userId;
    private final MessageType type;
    private final String title;
    private final String message;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private volatile boolean read;

    public Notification(int userId, MessageType type, String title, String message) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
    }

    public void markAsRead() {
        this.read = true;
    }
}
