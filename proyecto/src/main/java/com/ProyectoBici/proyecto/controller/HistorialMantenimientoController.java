package com.ProyectoBici.proyecto.controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.Mantenimiento;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.MantenimientoHistorialService;
import jakarta.servlet.http.HttpSession;

/**
 * Controlador web responsable de la visualización, filtrado y exportación
 * de los registros históricos de mantenimientos de las bicicletas del usuario.
 */
@Controller
@RequestMapping("/welcome/historial")
public class HistorialMantenimientoController {

    @Autowired
    private MantenimientoHistorialService historialService;

    /**
     * Muestra la vista con el historial de mantenimientos realizados, permitiendo filtrar por bicicleta.
     * Calcula además el costo total acumulado invertido por el usuario autenticado.
     *
     * @param biciId Identificador opcional de la bicicleta para filtrar el historial.
     * @param session Sesión HTTP utilizada para verificar la autenticación del usuario.
     * @param model Modelo UI de Spring para inyectar datos del usuario, módulo activo, bicicletas, historial y costo total.
     * @return El nombre de la plantilla {@code "welcome"} si la sesión es válida, o {@code "redirect:/login"} si no está autenticado.
     */
    @GetMapping
    public String verHistorial(
            @RequestParam(value = "biciId", required = false) Integer biciId,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        List<Bicicleta> bicicletas = historialService.listarBicicletasUsuario(usuario.getId_usuario());
        List<Mantenimiento> historial = historialService.obtenerHistorial(usuario.getId_usuario(), biciId);
        BigDecimal costoTotal = historialService.obtenerCostoAcumulado(usuario.getId_usuario());

        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", "historial");
        model.addAttribute("bicicletas", bicicletas);
        model.addAttribute("biciSeleccionadaId", biciId);
        model.addAttribute("historial", historial);

        // Enviamos el costo acumulado a la vista
        model.addAttribute("costoTotal", costoTotal);

        return "welcome";
    }

    /**
     * Genera y descarga un archivo en formato CSV con el reporte consolidado de mantenimientos del usuario.
     *
     * @param session Sesión HTTP utilizada para comprobar que el usuario tenga acceso.
     * @return {@link ResponseEntity} con el archivo CSV adjunto (HTTP 200) o estado 401 si no hay sesión iniciada.
     */
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportarReporte(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        String csvContent = historialService.generarReporteCSV(usuario.getId_usuario());
        byte[] output = csvContent.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_mantenimientos_bikecare.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(output);
    }
}