package com.ProyectoBici.proyecto.shared.notification;

import java.util.List;

import com.ProyectoBici.proyecto.shared.message.MessageType;

/** Envío y consulta de notificaciones del sistema. Invocable desde cualquier controlador o servicio. */
public interface NotificationService {

    Notification notify(int userId, MessageType type, String title, String message);

    List<Notification> unread(int userId);

    List<Notification> all(int userId);

    long countUnread(int userId);

    void markAsRead(int userId, String notificationId);

    void markAllAsRead(int userId);
}

