package com.ProyectoBici.proyecto.shared.message;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Utilidad para publicar mensajes hacia las vistas.
 *
 * <pre>
 * FlashMessages.success(redirect, "Bicicleta guardada.");   // antes de un redirect
 * FlashMessages.now(model, AppMessage.warning("Revisa..."));  // en la misma petición
 * </pre>
 *
 * Todos se guardan bajo la clave {@link #KEY}, que lee fragments/alerts.html.
 */
public final class FlashMessages {

    public static final String KEY = "appMessages";

    private FlashMessages() {
    }

    // ---------- Para redirects (flash attributes) ----------

    public static void add(RedirectAttributes redirect, AppMessage message) {
        @SuppressWarnings("unchecked")
        List<AppMessage> lista = (List<AppMessage>) redirect.getFlashAttributes().get(KEY);
        if (lista == null) {
            lista = new ArrayList<>();
            redirect.addFlashAttribute(KEY, lista);
        }
        lista.add(message);
    }

    public static void success(RedirectAttributes redirect, String text) {
        add(redirect, AppMessage.success(text));
    }

    public static void warning(RedirectAttributes redirect, String text) {
        add(redirect, AppMessage.warning(text));
    }

    public static void error(RedirectAttributes redirect, String text) {
        add(redirect, AppMessage.error(text));
    }

    public static void info(RedirectAttributes redirect, String text) {
        add(redirect, AppMessage.info(text));
    }

    public static void errorModal(RedirectAttributes redirect, String title, String text) {
        add(redirect, AppMessage.errorModal(title, text));
    }

    // ---------- Para vistas devueltas directamente (sin redirect) ----------

    public static void now(Model model, AppMessage message) {
        @SuppressWarnings("unchecked")
        List<AppMessage> lista = (List<AppMessage>) model.asMap().get(KEY);
        if (lista == null) {
            lista = new ArrayList<>();
            model.addAttribute(KEY, lista);
        }
        lista.add(message);
    }
}
