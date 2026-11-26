package com.ProyectoBici.proyecto.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ProyectoBici.proyecto.dto.BicicletaCard;
import com.ProyectoBici.proyecto.dto.ComponenteEstado;
import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.BicicletaService;
import com.ProyectoBici.proyecto.shared.exception.ResourceNotFoundException;
import com.ProyectoBici.proyecto.shared.exception.UnauthorizedException;
import com.ProyectoBici.proyecto.shared.message.FlashMessages;
import com.ProyectoBici.proyecto.shared.message.MessageType;
import com.ProyectoBici.proyecto.shared.notification.NotificationService;
import com.ProyectoBici.proyecto.shared.report.ReportData;
import com.ProyectoBici.proyecto.shared.report.ReportFormat;
import com.ProyectoBici.proyecto.shared.report.ReportService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Ejemplo de integración del paquete shared:
 *  - ReportService  -> descarga de un reporte CSV del garaje
 *  - NotificationService -> avisos del sistema
 *  - FlashMessages  -> mensajes de éxito tras un redirect
 *  - Excepciones (UnauthorizedException, ResourceNotFoundException) -> las atiende
 *    GlobalExceptionHandler; aquí no hace falta ningún try/catch ni redirect de error.
 */
@Controller
@RequestMapping("/welcome/garaje")
@RequiredArgsConstructor
public class ReporteAvisoController {

    private final BicicletaService bicicletaService;
    private final ReportService reportService;
    private final NotificationService notificationService;

    // ---------- Reporte CSV del estado del garaje ----------

    @GetMapping("/reporte")
    public ResponseEntity<byte[]> descargarReporte(HttpSession session) {
        Usuario usuario = usuarioEnSesion(session);

        ReportData datos = ReportData.of("Estado del garaje",
                "Bicicleta", "Tipo", "Km totales", "Componente", "Km usados", "Límite km", "Desgaste %", "Nivel");

        int criticos = 0;
        List<BicicletaCard> cards = bicicletaService.listarConComponentes(usuario.getId_usuario());
        for (BicicletaCard card : cards) {
            Bicicleta bici = card.getBicicleta();
            String nombre = bici.getMarca() + " " + bici.getModelo();
            for (ComponenteEstado c : card.getComponentes()) {
                datos.addRow(nombre, bici.getTipo(), bici.getKilometrajeTotal(), c.getNombre(),
                        c.getKmUsados(), c.getLimiteKm(), c.getPorcentaje(), c.getNivel());
                if ("critico".equals(   c.getNivel())) {
                    criticos++;
                }
            }
        }

        // Si no hay filas, generate() lanza BusinessException("No hay datos...") y el handler
        // global vuelve a la página anterior con una advertencia.
        ResponseEntity<byte[]> descarga = reportService.generate(ReportFormat.CSV, datos).asDownload();

        if (criticos > 0) {
            notificationService.notify(usuario.getId_usuario(), MessageType.WARNING, "Mantenimiento pendiente",
                    criticos + " componente(s) están en estado crítico. Revisa tu garaje.");
        }
        return descarga;
    }

    // ---------- Aviso de mantenimiento para una bici ----------

    @PostMapping("/{idBici}/aviso-mantenimiento")
    public String crearAviso(@PathVariable("idBici") int idBici, HttpSession session, RedirectAttributes redirect) {
        Usuario usuario = usuarioEnSesion(session);

        // Si la bici no existe o no es del usuario -> ResourceNotFoundException -> mensaje + redirect
        Bicicleta bici = bicicletaService.buscarDeUsuario(idBici, usuario.getId_usuario())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró esa bicicleta.", "/welcome/garaje"));

        String nombre = bici.getMarca() + " " + bici.getModelo();
        notificationService.notify(usuario.getId_usuario(), MessageType.INFO, "Recordatorio de mantenimiento",
                "Revisa el estado de los componentes de " + nombre + ".");

        FlashMessages.success(redirect, "Aviso creado para " + nombre + ".");
        return "redirect:/welcome/garaje";
    }

    // ---------- Marcar una notificación como leída ----------

    @PostMapping("/notificaciones/{id}/leer")
    public String marcarLeida(@PathVariable("id") String id, HttpSession session) {
        Usuario usuario = usuarioEnSesion(session);
        notificationService.markAsRead(usuario.getId_usuario(), id);
        return "redirect:/welcome/garaje";
    }

    // ---------- Sesión ----------

    private Usuario usuarioEnSesion(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            throw new UnauthorizedException(); // el handler redirige a /login con un aviso
        }
        return usuario;
    }
}
