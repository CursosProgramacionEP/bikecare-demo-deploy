package com.ProyectoBici.proyecto.shared.exception;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ProyectoBici.proyecto.shared.message.AppMessage;
import com.ProyectoBici.proyecto.shared.message.FlashMessages;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Captura los errores de todos los controladores, los convierte en mensajes flash
 * y redirige. La vista los pinta con fragments/alerts.html.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /** Destino cuando no hay ruta explícita ni Referer válido (application.properties). */
    @Value("${bikecare.shared.default-redirect:/welcome}")
    private String defaultRedirect;

    // ---------- Errores de negocio / validación ----------

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, HttpServletRequest request, RedirectAttributes redirect) {
        log.warn("{} en {} {}: {}", ex.getClass().getSimpleName(), request.getMethod(),
                request.getRequestURI(), ex.getMessage());
        FlashMessages.add(redirect, AppMessage.of(ex.getType(), ex.getMessage()));
        return redirectTo(request, ex.getRedirectTo());
    }

    // ---------- Foto demasiado grande (multipart) ----------

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleUpload(MaxUploadSizeExceededException ex, HttpServletRequest request,
            RedirectAttributes redirect) {
        log.warn("Archivo demasiado grande en {}", request.getRequestURI());
        FlashMessages.warning(redirect, "El archivo es demasiado grande. Elige una imagen más liviana.");
        return redirectTo(request, null);
    }

    // ---------- Cualquier otro error (red de seguridad) ----------

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception ex, HttpServletRequest request, HttpServletResponse response,
            RedirectAttributes redirect) throws IOException {

        // 404, 405, 415... los dejamos a la página de error de Spring Boot con su código HTTP
        if (ex instanceof ErrorResponse errorResponse) {
            response.sendError(errorResponse.getStatusCode().value());
            return null; // null = petición ya atendida
        }

        log.error("Error inesperado en {} {}", request.getMethod(), request.getRequestURI(), ex);
        FlashMessages.errorModal(redirect, "Algo salió mal",
                "Ocurrió un error inesperado. Inténtalo de nuevo; si el problema continúa, contacta a soporte.");
        return new ModelAndView(redirectTo(request, null));
    }

    // ---------- Utilidades ----------

    /** Ruta explícita > página anterior (mismo sitio) > ruta por defecto. */
    private String redirectTo(HttpServletRequest request, String preferred) {
        String target = preferred != null ? preferred : refererPath(request);
        if (target == null) {
            target = defaultRedirect;
        }

        // Evita un bucle infinito si la propia página (GET) es la que falla
        String actual = request.getRequestURI().substring(request.getContextPath().length());
        if ("GET".equalsIgnoreCase(request.getMethod()) && target.equals(actual)) {
            target = "/login";
        }
        return "redirect:" + target;
    }

    /** Ruta (sin dominio) del Referer, solo si apunta a este mismo servidor. */
    private String refererPath(HttpServletRequest request) {
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (referer == null || referer.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(referer);
            if (uri.getHost() == null || !uri.getHost().equalsIgnoreCase(request.getServerName())) {
                return null;
            }
            String path = uri.getRawPath();
            String ctx = request.getContextPath();
            if (path != null && !ctx.isEmpty() && path.startsWith(ctx)) {
                path = path.substring(ctx.length());
            }
            // Solo rutas internas: nada de "//otro-sitio.com"
            if (path == null || !path.startsWith("/") || path.startsWith("//")) {
                return null;
            }
            return path;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
