package com.ProyectoBici.proyecto.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.RutaService;
import com.ProyectoBici.proyecto.shared.message.FlashMessages;

import jakarta.servlet.http.HttpSession;

/**
 * Controlador web responsable de la adaptación HTTP del módulo Registro de Rutas.
 * Gestiona el despliegue del formulario, la captura y conversión de parámetros enviados
 * desde la vista, y delega las reglas de negocio de incremento de kilometraje a {@link RutaService}.
 */
@Controller
@RequestMapping("/welcome/registroRuta")
public class RutaController {

    @Autowired
    private RutaService rutaService;

    /**
     * Muestra el formulario para registrar un recorrido, cargando las bicicletas asociadas al ciclista autenticado.
     *
     * @param session Objeto {@link HttpSession} para verificar que exista una sesión activa con "usuarioLogueado".
     * @param model Objeto {@link Model} en el que se inyectan el usuario, el módulo activo ("registroRuta"),
     *              la lista de bicicletas para el selector y la fecha actual.
     * @return El nombre de la plantilla {@code "welcome"} si el usuario está autenticado,
     *         o {@code "redirect:/login"} si no existe sesión válida.
     */
    @GetMapping
    public String verFormulario(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        List<Bicicleta> bicis = rutaService.listarBicisDeUsuario(usuario.getId_usuario());
        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", "registroRuta");
        model.addAttribute("bicicletas", bicis);
        model.addAttribute("hoy", LocalDate.now());
        return "welcome";
    }

    /**
     * Procesa la solicitud HTTP POST para registrar una nueva ruta.
     * Valida la autenticación, la selección de la bicicleta, la coherencia de la fecha (no futura),
     * convierte la distancia de texto a formato numérico con dos decimales y delega la actualización del odómetro al servicio.
     *
     * @param idBici Identificador numérico de la bicicleta seleccionada en el formulario.
     * @param distanciaRaw Distancia ingresada en formato texto desde la vista para su posterior conversión.
     * @param fechaRecorrido Fecha en la que se realizó el trayecto (formato YYYY-MM-DD).
     * @param origenRegistro Cadena descriptiva del origen del registro (manual o sincronizado).
     * @param session Objeto {@link HttpSession} para validar al ciclista autenticado.
     * @param redirect Objeto {@link RedirectAttributes} para adjuntar mensajes flash de confirmación o alerta tras la redirección.
     * @return Redirección a {@code "redirect:/welcome/garaje"} si el registro se completó con éxito,
     *         {@code "redirect:/welcome/registroRuta"} si ocurrió un error de validación o negocio,
     *         o {@code "redirect:/login"} si la sesión expiró.
     */
    @PostMapping
    public String guardarRuta(
            @RequestParam(value = "idBicicleta", required = false) Integer idBici,
            @RequestParam(value = "distanciaKm", required = false) String distanciaRaw,
            @RequestParam(value = "fechaRecorrido", required = false) String fechaRecorrido, // reservado: sin tabla ruta aún
            @RequestParam(value = "origenRegistro", required = false) String origenRegistro, // reservado: sin tabla ruta aún
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        if (idBici == null) {
            FlashMessages.error(redirect, "Selecciona una bicicleta para registrar la ruta.");
            return "redirect:/welcome/registroRuta";
        }

        // Validación de la fecha del trayecto: no se aceptan fechas futuras.
        // (En el formulario ya está limitada con th:max, pero se valida aquí
        //  igual para que no se pueda saltar la restricción.)
        if (fechaRecorrido != null && !fechaRecorrido.isBlank()) {
            LocalDate fecha;
            try {
                fecha = LocalDate.parse(fechaRecorrido.trim());
            } catch (DateTimeParseException e) {
                fecha = null;
                FlashMessages.error(redirect, "La fecha del trayecto no es válida.");
                return "redirect:/welcome/registroRuta";
            }
            if (fecha.isAfter(LocalDate.now())) {
                FlashMessages.error(redirect, "La fecha del trayecto no puede ser una fecha futura.");
                return "redirect:/welcome/registroRuta";
            }
        }

        // Adaptación HTTP: texto del input -> número (el rango lo valida el servicio).
        BigDecimal distancia;
        try {
            if (distanciaRaw == null || distanciaRaw.isBlank()) {
                throw new NumberFormatException();
            }
            distancia = new BigDecimal(distanciaRaw.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            FlashMessages.error(redirect, "La distancia no es válida.");
            return "redirect:/welcome/registroRuta";
        }

        try {
            Bicicleta bici = rutaService.registrarRecorrido(usuario.getId_usuario(), idBici, distancia);
            FlashMessages.success(redirect,
                    "Ruta de " + distancia.toPlainString() + " km registrada en "
                            + bici.getMarca() + " " + bici.getModelo()
                            + ". Odómetro: " + bici.getKilometrajeTotal().toPlainString() + " km.");
        } catch (IllegalArgumentException e) {
            FlashMessages.error(redirect, e.getMessage());
            return "redirect:/welcome/registroRuta";
        }
        return "redirect:/welcome/garaje";
    }
}