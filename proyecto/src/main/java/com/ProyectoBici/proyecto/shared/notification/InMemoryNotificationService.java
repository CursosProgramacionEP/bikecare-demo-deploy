package com.ProyectoBici.proyecto.shared.notification;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.stereotype.Service;

import com.ProyectoBici.proyecto.shared.message.MessageType;

/**
 * Implementación en memoria (no requiere tablas). Se pierde al reiniciar la aplicación.
 * Para persistirlas, crea otra implementación de NotificationService con un repositorio JPA
 * (y la tabla correspondiente, ya que ddl-auto=validate) y quita @Service de esta.
 */
@Service
public class InMemoryNotificationService implements NotificationService {

    private static final int MAX_POR_USUARIO = 50;

    private final ConcurrentHashMap<Integer, Deque<Notification>> store = new ConcurrentHashMap<>();

    @Override
    public Notification notify(int userId, MessageType type, String title, String message) {
        Notification n = new Notification(userId, type, title, message);
        Deque<Notification> cola = store.computeIfAbsent(userId, k -> new ConcurrentLinkedDeque<>());
        cola.addFirst(n); // las más recientes primero
        while (cola.size() > MAX_POR_USUARIO) {
            cola.pollLast();
        }
        return n;
    }

    @Override
    public List<Notification> unread(int userId) {
        return all(userId).stream().filter(n -> !n.isRead()).toList();
    }

    @Override
    public List<Notification> all(int userId) {
        Deque<Notification> cola = store.get(userId);
        return cola == null ? List.of() : new ArrayList<>(cola);
    }

    @Override
    public long countUnread(int userId) {
        return unread(userId).size();
    }

    @Override
    public void markAsRead(int userId, String notificationId) {
        all(userId).stream()
                .filter(n -> n.getId().equals(notificationId))
                .forEach(Notification::markAsRead);
    }

    @Override
    public void markAllAsRead(int userId) {
        all(userId).forEach(Notification::markAsRead);
    }
}
